package edu.ufl.cise.cop4020fa23;

import edu.ufl.cise.cop4020fa23.ast.*;
import edu.ufl.cise.cop4020fa23.exceptions.CodeGenException;
import edu.ufl.cise.cop4020fa23.exceptions.PLCCompilerException;

import java.util.Objects;

public class CodeGeneratorVisitor implements ASTVisitor {
    @Override
    public Object visitAssignmentStatement(AssignmentStatement assignmentStatement, Object arg) throws PLCCompilerException {
        StringBuilder sb;
        if (arg != null) {
            sb = (StringBuilder) arg;
        } else {
            sb = new StringBuilder();
        }

        LValue lv = assignmentStatement.getlValue();
        Expr expr = assignmentStatement.getE();

        lv.visit(this, sb);
        sb.append('=');
        expr.visit(this, sb);

        return (arg == null ? sb.toString() : null);
    }

    @Override
    public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg) throws PLCCompilerException {
        StringBuilder sb;
        if (arg != null) {
            sb = (StringBuilder) arg;
        } else {
            sb = new StringBuilder();
        }

        Expr leftExpr = binaryExpr.getLeftExpr();
        Expr rightExpr = binaryExpr.getRightExpr();
        Kind op = binaryExpr.getOpKind();

        switch (leftExpr.getType()) {
            case PIXEL -> {
                switch (op) {
                    case BITAND, BITOR -> {
                        sb.append('(');
                        leftExpr.visit(this, sb);
                        sb.append(binaryExpr.getOp().text());
                        rightExpr.visit(this, sb);
                        sb.append(')');
                    }
                    case PLUS, MINUS, TIMES, DIV, MOD, EXP -> {
                        if (rightExpr.getType() == Type.INT) {
                            sb.append("ImageOps.binaryPackedPixelScalarOp(");
                        } else {
                            sb.append("ImageOps.binaryPackedPixelPixelOp(");
                        }

                        sb.append(switch (op) {
                            case PLUS ->  "PLUS";
                            case MINUS -> "MINUS";
                            case TIMES -> "TIMES";
                            case DIV -> "DIV";
                            case MOD -> "MOD";
                            case EXP -> "EXP";
                            default -> throw new CodeGenException("Invalid OP");
                        });
                        sb.append(',');
                        leftExpr.visit(this, sb);
                        sb.append(',');
                        rightExpr.visit(this, sb);
                        sb.append(')');
                    }
                }
            }
            case BOOLEAN -> {
                switch (op) {
                    case AND, OR -> {
                        sb.append('(');
                        leftExpr.visit(this, sb);
                        sb.append(binaryExpr.getOp().text());
                        rightExpr.visit(this, sb);
                        sb.append(')');
                    }
                    default -> {
                        throw new CodeGenException("Invalid OP");
                    }
                }
            }
            case INT -> {
                switch (op) {
                    case LT, GT, LE, GE, PLUS, MINUS, TIMES, DIV, MOD -> {
                        sb.append('(');
                        leftExpr.visit(this, sb);
                        sb.append(binaryExpr.getOp().text());
                        rightExpr.visit(this, sb);
                        sb.append(')');
                    }
                    case EXP -> {
                        sb.append("(int) Math.round(Math.pow(");
                        leftExpr.visit(this, sb);
                        sb.append(',');
                        rightExpr.visit(this, sb);
                        sb.append("))");
                    }
                    default -> {
                        throw new CodeGenException("Invalid OP");
                    }
                }
            }
            case IMAGE -> {
                switch (op) {
                    case EQ -> {
                        sb.append("ImageOps.binaryImageImageBooleanOp(EQUALS,");
                        leftExpr.visit(this, sb);
                        sb.append(',');
                        rightExpr.visit(this, sb);
                        sb.append(')');
                    }
                    case PLUS, MINUS, TIMES, DIV, MOD -> {
                        if (rightExpr.getType() == Type.INT) {
                            sb.append("ImageOps.binaryImageScalarOp(");
                        } else {
                            sb.append("ImageOps.binaryImageImageOp(");
                        }

                        sb.append(switch (op) {
                            case PLUS ->  "PLUS";
                            case MINUS -> "MINUS";
                            case TIMES -> "TIMES";
                            case DIV -> "DIV";
                            case MOD -> "MOD";
                            default -> throw new CodeGenException("Invalid OP"); // never happens lol
                        });
                        sb.append(',');
                        leftExpr.visit(this, sb);
                        sb.append(',');
                        rightExpr.visit(this, sb);
                        sb.append(')');
                    }
                    default -> {
                        throw new CodeGenException("Invalid OP");
                    }
                }
            }
            case STRING -> {
                switch (op) {
                    case EQ -> {
                        leftExpr.visit(this, sb);
                        sb.append(".equals(");
                        rightExpr.visit(this, sb);
                        sb.append(')');
                    }
                    case PLUS -> {
                        sb.append('(');
                        leftExpr.visit(this, sb);
                        sb.append('+');
                        rightExpr.visit(this, sb);
                        sb.append(')');
                    }
                }
            }
        }

        return (arg == null ? sb.toString() : null);
    }

    @Override
    public Object visitBlock(Block block, Object arg) throws PLCCompilerException {
        StringBuilder sb;
        if (arg != null) {
            sb = (StringBuilder) arg;
        } else {
            sb = new StringBuilder();
        }

        sb.append("{");
        for (Block.BlockElem be : block.getElems()) {
            be.visit(this, sb);
            sb.append(";\n");
        }
        sb.append("}\n");


        return (arg == null ? sb.toString() : null);
    }

    @Override
    public Object visitBlockStatement(StatementBlock statementBlock, Object arg) throws PLCCompilerException {
        StringBuilder sb;
        if (arg != null) {
            sb = (StringBuilder) arg;
        } else {
            sb = new StringBuilder();
        }

        statementBlock.getBlock().visit(this, sb);

        return (arg == null ? sb.toString() : null);
    }

    @Override
    public Object visitChannelSelector(ChannelSelector channelSelector, Object arg) throws PLCCompilerException {
        return null;
    }

    @Override
    public Object visitConditionalExpr(ConditionalExpr conditionalExpr, Object arg) throws PLCCompilerException {
        StringBuilder sb;
        if (arg != null) {
            sb = (StringBuilder) arg;
        } else {
            sb = new StringBuilder();
        }

        sb.append('(');
        conditionalExpr.getGuardExpr().visit(this, sb);
        sb.append('?');
        conditionalExpr.getTrueExpr().visit(this, sb);
        sb.append(':');
        conditionalExpr.getFalseExpr().visit(this, sb);
        sb.append(')');

        return (arg == null ? sb.toString() : null);
    }

    @Override
    public Object visitDeclaration(Declaration declaration, Object arg) throws PLCCompilerException {
        StringBuilder sb;
        if (arg != null) {
            sb = (StringBuilder) arg;
        } else {
            sb = new StringBuilder();
        }

        NameDef nd = declaration.getNameDef();
        Expr init = declaration.getInitializer();
        nd.visit(this, sb);

        if (init == null) {
            return (arg == null ? sb.toString() : null);
        }

        sb.append('=');

        Dimension dim = nd.getDimension();

        if (nd.getType() != Type.IMAGE) {
            init.visit(this, sb);
        } else {
            switch (init.getType()) {
                case STRING -> {
                    sb.append("FileURLIO.readImage(\"");
                    init.visit(this, sb);

                    if (dim != null) {
                        sb.append(',');
                        dim.getWidth().visit(this, sb);
                        sb.append(',');
                        dim.getHeight().visit(this, sb);
                    }
                }
                case IMAGE -> {
                    if (dim == null) {
                        sb.append("ImageOps.cloneImage(");
                        init.visit(this, sb);
                    } else {
                        sb.append("ImageOps.copyAndResize(");
                        init.visit(this, sb);
                        sb.append(',');
                        dim.getWidth().visit(this, sb);
                        sb.append(',');
                        dim.getHeight().visit(this, sb);
                    }
                }
                default -> {
                    throw new CodeGenException("Unexpected type");
                }
            }
            sb.append(")");
        }

        return (arg == null ? sb.toString() : null);
    }

    @Override
    public Object visitDimension(Dimension dimension, Object arg) throws PLCCompilerException {
        return null;
    }

    @Override
    public Object visitDoStatement(DoStatement doStatement, Object arg) throws PLCCompilerException {
        return null;
    }

    @Override
    public Object visitExpandedPixelExpr(ExpandedPixelExpr expandedPixelExpr, Object arg) throws PLCCompilerException {
        return null;
    }

    @Override
    public Object visitGuardedBlock(GuardedBlock guardedBlock, Object arg) throws PLCCompilerException {
        return null;
    }

    @Override
    public Object visitIdentExpr(IdentExpr identExpr, Object arg) throws PLCCompilerException {
        StringBuilder sb;
        if (arg != null) {
            sb = (StringBuilder) arg;
        } else {
            sb = new StringBuilder();
        }

        sb.append(identExpr.getNameDef().getJavaName());

        return (arg == null ? sb.toString() : null);
    }

    @Override
    public Object visitIfStatement(IfStatement ifStatement, Object arg) throws PLCCompilerException {
        return null;
    }

    @Override
    public Object visitLValue(LValue lValue, Object arg) throws PLCCompilerException {
        StringBuilder sb;
        if (arg != null) {
            sb = (StringBuilder) arg;
        } else {
            sb = new StringBuilder();
        }

        sb.append(lValue.getNameDef().getJavaName());

        return (arg == null ? sb.toString() : null);
    }

    @Override
    public Object visitNameDef(NameDef nameDef, Object arg) throws PLCCompilerException {
        StringBuilder sb;
        if (arg != null) {
            sb = (StringBuilder) arg;
        } else {
            sb = new StringBuilder();
        }

        sb.append(fixTyping(nameDef.getType()));
        sb.append(' ');
        sb.append(nameDef.getJavaName());

        return (arg == null ? sb.toString() : null);
    }

    @Override
    public Object visitNumLitExpr(NumLitExpr numLitExpr, Object arg) throws PLCCompilerException {
        StringBuilder sb;
        if (arg != null) {
            sb = (StringBuilder) arg;
        } else {
            sb = new StringBuilder();
        }

        sb.append(numLitExpr.getText());

        return (arg == null ? sb.toString() : null);
    }

    @Override
    public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws PLCCompilerException {
        return null;
    }

    @Override
    public Object visitPostfixExpr(PostfixExpr postfixExpr, Object arg) throws PLCCompilerException {
        StringBuilder sb;
        if (arg != null) {
            sb = (StringBuilder) arg;
        } else {
            sb = new StringBuilder();
        }

        Expr expr = postfixExpr.primary();
        ChannelSelector cs = postfixExpr.channel();
        PixelSelector ps = postfixExpr.pixel();

        if (expr.getType() == Type.PIXEL) {
            cs.visit(this, sb);
            expr.visit(this, sb);
        } else {
            if (ps != null && cs == null) {
                sb.append("ImageOps.getRGB(");
                expr.visit(this, sb);
                sb.append(',');
                ps.visit(this, sb);
                sb.append(')');
            } else if (ps != null) {
                cs.visit(this, sb);
                sb.append("(ImageOps.getRGB(");
                expr.visit(this, sb);
                sb.append(',');
                ps.visit(this, sb);
                sb.append("))");
            } else if (cs != null){
                sb.append("ImageOps.extract");
                sb.append(switch (cs.color()) {
                    case RES_red -> "Red";
                    case RES_green -> "Green";
                    case RES_blue -> "Blue";
                    default -> throw new CodeGenException("This never happens...");
                });
                sb.append('(');
                expr.visit(this, sb);
                sb.append(')');
            }
        }

        return (arg == null ? sb.toString() : null);
    }

    @Override
    public Object visitProgram(Program program, Object arg) throws PLCCompilerException {
        StringBuilder sb = new StringBuilder();

        sb.append("package ");
        sb.append((String) arg);
        sb.append(";\n");
        sb.append("import edu.ufl.cise.cop4020fa23.runtime.ConsoleIO;\n");
        sb.append("import edu.ufl.cise.cop4020fa23.runtime.ImageOps;\n");
        sb.append("import edu.ufl.cise.cop4020fa23.runtime.PixelOps;\n");
        sb.append("import edu.ufl.cise.cop4020fa23.runtime.FileURLIO;\n");

        sb.append("\npublic class ");
        sb.append(program.getName());
        sb.append("{\npublic static ");
        sb.append(fixTyping(program.getType()));
        sb.append(" apply(\n");

        for (int i = 0; i < program.getParams().size(); ++i) {
            if (i != 0) {
                sb.append(',');
            }
            program.getParams().get(i).visit(this, sb);
        }

        sb.append("\n)");
        program.getBlock().visit(this, sb);
        sb.append("\n}");

        return sb.toString();
    }

    @Override
    public Object visitReturnStatement(ReturnStatement returnStatement, Object arg) throws PLCCompilerException {
        StringBuilder sb;
        if (arg != null) {
            sb = (StringBuilder) arg;
        } else {
            sb = new StringBuilder();
        }

        Expr expr = returnStatement.getE();

        sb.append("return ");
        expr.visit(this, sb);

        return (arg == null ? sb.toString() : null);
    }

    @Override
    public Object visitStringLitExpr(StringLitExpr stringLitExpr, Object arg) throws PLCCompilerException {
        StringBuilder sb;
        if (arg != null) {
            sb = (StringBuilder) arg;
        } else {
            sb = new StringBuilder();
        }

        sb.append(stringLitExpr.getText());

        return (arg == null ? sb.toString() : null);
    }

    @Override
    public Object visitUnaryExpr(UnaryExpr unaryExpr, Object arg) throws PLCCompilerException {
        StringBuilder sb;
        if (arg != null) {
            sb = (StringBuilder) arg;
        } else {
            sb = new StringBuilder();
        }

        Kind op = unaryExpr.getOp();
        Expr expr = unaryExpr.getExpr();
        sb.append('(');

        switch (op) {
            case RES_width, RES_height -> {
                expr.visit(this, sb);
                sb.append(switch (op) {
                    case RES_width -> ".getWidth()";
                    case RES_height -> ".getHeight()";
                    default -> throw new CodeGenException("Invalid OP");
                });
            }
            default -> {
                sb.append(unaryExpr.getOpToken().text());
                expr.visit(this, sb);
            }
        }
        sb.append(')');

        return (arg == null ? sb.toString() : null);
    }

    @Override
    public Object visitWriteStatement(WriteStatement writeStatement, Object arg) throws PLCCompilerException {
        StringBuilder sb;
        if (arg != null) {
            sb = (StringBuilder) arg;
        } else {
            sb = new StringBuilder();
        }

        sb.append("ConsoleIO.write(");
        writeStatement.getExpr().visit(this, sb);
        sb.append(')');

        return (arg == null ? sb.toString() : null);
    }

    @Override
    public Object visitBooleanLitExpr(BooleanLitExpr booleanLitExpr, Object arg) throws PLCCompilerException {
        StringBuilder sb;
        if (arg != null) {
            sb = (StringBuilder) arg;
        } else {
            sb = new StringBuilder();
        }

        sb.append(booleanLitExpr.getText().toLowerCase());

        return (arg == null ? sb.toString() : null);
    }

    @Override
    public Object visitConstExpr(ConstExpr constExpr, Object arg) throws PLCCompilerException {
        StringBuilder sb;
        if (arg != null) {
            sb = (StringBuilder) arg;
        } else {
            sb = new StringBuilder();
        }

        if (Objects.equals(constExpr.getName(), "Z")) {
            sb.append("255");
        } else {
            sb.append("\"0x\"+Integer.toHexString(Color.");
            sb.append(constExpr.getName());
            sb.append(".getRGB())");
        }

        return (arg == null ? sb.toString() : null);
    }

    public String fixTyping(Type type) {
        return switch(type) {
            case INT -> "int";
            case BOOLEAN -> "boolean";
            case IMAGE -> "BufferedImage";
            case VOID -> "void";
            case PIXEL -> "int";
            case STRING -> "String";
        };
    }
}
