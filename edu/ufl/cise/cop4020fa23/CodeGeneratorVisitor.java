package edu.ufl.cise.cop4020fa23;

import edu.ufl.cise.cop4020fa23.ast.Dimension;
import edu.ufl.cise.cop4020fa23.ast.*;
import edu.ufl.cise.cop4020fa23.exceptions.CodeGenException;
import edu.ufl.cise.cop4020fa23.exceptions.PLCCompilerException;

import java.awt.*;
import java.util.List;
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

        if(lv.getType() == Type.IMAGE) {
            if(lv.getPixelSelector() == null && lv.getChannelSelector() == null) {
                if(expr.getType() == Type.IMAGE){
                    sb.append("ImageOps.copyInto((BufferedImage)");
                    lv.visit(this, sb);
                    sb.append(", (BufferedImage)");
                    expr.visit(this, sb);
                    sb.append(')');
                }
                else if(expr.getType() == Type.PIXEL) {
                    sb.append("ImageOps.setAllPixels((BufferedImage)");
                    lv.visit(this, sb);
                    sb.append(", (BufferedImage)");
                    expr.visit(this, sb);
                    sb.append(')');
                }
                else if(expr.getType() == Type.STRING) {
                    sb.append("BufferedImage loadedImage = FileURLIO.readImage(");
                    expr.visit(this, sb);
                    sb.append(");\n");
                    sb.append("ImageOps.copyInto(");
                    lv.visit(this, sb);
                    sb.append(", loadedImage)");
                }
            }
            else if(lv.getChannelSelector() != null) {
                throw new UnsupportedOperationException("ChannelSelector Not Null Case");
            }
            else if(lv.getPixelSelector() != null && lv.getChannelSelector() == null) {
                //figure it out later

            }
        }
        else if(lv.getType() == Type.PIXEL && lv.getChannelSelector() != null) {
            if(lv.getChannelSelector().color() == Kind.RES_red) {
                sb.append("PixelOps.setRed(");
            }
            else if(lv.getChannelSelector().color() == Kind.RES_green) {
                sb.append("PixelOps.setGreen(");
            }
            else if(lv.getChannelSelector().color() == Kind.RES_blue){
                sb.append("PixelOps.setBlue(");
            }
            lv.visit(this, sb);
            sb.append(',');
            expr.visit(this, sb);
            sb.append(',');
        }
        else {
            lv.visit(this, sb);
            sb.append('=');
            expr.visit(this, sb);
        }

        PixelSelector pixelSelector = lv.getPixelSelector();
        if (pixelSelector != null) {
            pixelSelector.visit(this, sb);
        }

        // Check if there is a ChannelSelector and visit it
        ChannelSelector channelSelector = lv.getChannelSelector();
        if (channelSelector != null) {
            channelSelector.visit(this, sb);
        }

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
                            sb.append("ImageOps.binaryPackedPixelPixelOp(ImageOps.OP.");
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
                        sb.append("ImageOps.binaryImageImageBooleanOp(ImageOPs.BoolOP.EQUALS,");
                        leftExpr.visit(this, sb);
                        sb.append(',');
                        rightExpr.visit(this, sb);
                        sb.append(')');
                    }
                    case PLUS, MINUS, TIMES, DIV, MOD -> {
                        if (rightExpr.getType() == Type.INT) {
                            sb.append("ImageOps.binaryImageScalarOp(ImageOps.OP.");
                        } else {
                            sb.append("ImageOps.binaryImageImageOp(ImageOps.OP.");
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
                    sb.append("FileURLIO.readImage(");
                    init.visit(this, sb);

                    if (dim != null) {
                        sb.append(',');
                        dim.getWidth().visit(this, sb);
                        sb.append(',');
                        dim.getHeight().visit(this, sb);
                    }
                    //sb.append(")");
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
                    //sb.append(")");
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
        StringBuilder sb;
        if (arg != null) {
            sb = (StringBuilder) arg;
        } else {
            sb = new StringBuilder();
        }

        dimension.getWidth().visit(this, sb);
        sb.append(',');
        dimension.getHeight().visit(this, sb);

        return (arg == null ? sb.toString() : null);
    }

    @Override
    public Object visitDoStatement(DoStatement doStatement, Object arg) throws PLCCompilerException {
        StringBuilder sb;
        if (arg != null) {
            sb = (StringBuilder) arg;
        } else {
            sb = new StringBuilder();
        }

        sb.append("do {\n");

        List<GuardedBlock> guardedBlocks = doStatement.getGuardedBlocks();

        for (int i = 0; i < guardedBlocks.size(); i++) {
            Expr guard = guardedBlocks.get(i).getGuard();
            Block block = guardedBlocks.get(i).getBlock();

            if(i == 0) {
                sb.append("if (");
            }
            else{
                sb.append("else if(");
            }
            guard.visit(this, sb);
            sb.append(") {\n");
            block.visit(this, sb);
            sb.append("}\n");
        }

        sb.append("} while (");

        for (int i = 0; i < doStatement.getGuardedBlocks().size(); ++i) {
            if (i != 0) {
                sb.append(" || ");
            }
            sb.append("(");
            doStatement.getGuardedBlocks().get(i).getGuard().visit(this, sb);
            sb.append(")");
        }

        sb.append(")\n");

        return (arg == null ? sb.toString() : null);
    }

    @Override
    public Object visitExpandedPixelExpr(ExpandedPixelExpr expandedPixelExpr, Object arg) throws PLCCompilerException {
        StringBuilder sb;
        if (arg != null) {
            sb = (StringBuilder) arg;
        } else {
            sb = new StringBuilder();
        }

        sb.append("PixelOps.pack(");
        expandedPixelExpr.getRed().visit(this, sb);
        sb.append(',');
        expandedPixelExpr.getGreen().visit(this, sb);
        sb.append(',');
        expandedPixelExpr.getBlue().visit(this, sb);
        sb.append(')');

        return (arg == null ? sb.toString() : null);
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
        StringBuilder sb;
        if (arg != null) {
            sb = (StringBuilder) arg;
        } else {
            sb = new StringBuilder();
        }

        List<GuardedBlock> guardedBlocks = ifStatement.getGuardedBlocks();

        for (int i = 0; i < guardedBlocks.size(); i++) {
            Expr guard = guardedBlocks.get(i).getGuard();
            Block block = guardedBlocks.get(i).getBlock();

            if(i == 0) {
                sb.append("if (");
            }
            else{
                sb.append("else if(");
            }
            guard.visit(this, sb);
            sb.append(") {\n");
            block.visit(this, sb);
            sb.append("}\n");
        }

        return (arg == null ? sb.toString() : null);
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

//        PixelSelector ps = lValue.getPixelSelector();
//        if (ps != null) {
//            ps.visit(this, lValue);
//        }
//        ChannelSelector cs = lValue.getChannelSelector();
//        if (cs != null) {
//            cs.visit(this, arg);
//        }

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
        StringBuilder sb;
        if (arg != null) {
            sb = (StringBuilder) arg;
        } else {
            sb = new StringBuilder();
        }

        pixelSelector.xExpr().visit(this, sb);
        sb.append(',');
        pixelSelector.yExpr().visit(this, sb);

        return (arg == null ? sb.toString() : null);
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
        sb.append("import java.awt.image.BufferedImage;\n");


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

        if(writeStatement.getExpr().getType() == Type.PIXEL) {
            sb.append("ConsoleIO.writePixel(");
        }
        else {
            sb.append("ConsoleIO.write(");
        }

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
//            sb.append("\"0x\"+Integer.toHexString(Color.");
//            sb.append(constExpr.getName());
//            sb.append(".getRGB())");

            sb.append("0x");
            String color = constExpr.getName();

            switch (color) {
                case "RED" -> {
                    String hexstring = Integer.toHexString(Color.RED.getRGB());
                    sb.append(hexstring);
                }
                case "BLUE" -> {
                    String hexstring = Integer.toHexString(Color.BLUE.getRGB());
                    sb.append(hexstring);
                }
                case "GREEN" -> {
                    String hexstring = Integer.toHexString(Color.GREEN.getRGB());
                    sb.append(hexstring);
                }
                case "BLACK" -> {
                    String hexstring = Integer.toHexString(Color.BLACK.getRGB());
                    sb.append(hexstring);
                }
                case "CYAN" -> {
                    String hexstring = Integer.toHexString(Color.CYAN.getRGB());
                    sb.append(hexstring);
                }
                case "DARK_GRAY" -> {
                    String hexstring = Integer.toHexString(Color.DARK_GRAY.getRGB());
                    sb.append(hexstring);
                }
                case "GRAY" -> {
                    String hexstring = Integer.toHexString(Color.GRAY.getRGB());
                    sb.append(hexstring);
                }
                case "LIGHT_GRAY" -> {
                    String hexstring = Integer.toHexString(Color.LIGHT_GRAY.getRGB());
                    sb.append(hexstring);
                }
                case "MAGENTA" -> {
                    String hexstring = Integer.toHexString(Color.MAGENTA.getRGB());
                    sb.append(hexstring);
                }
                case "ORANGE" -> {
                    String hexstring = Integer.toHexString(Color.ORANGE.getRGB());
                    sb.append(hexstring);
                }
                case "PINK" -> {
                    String hexstring = Integer.toHexString(Color.PINK.getRGB());
                    sb.append(hexstring);
                }
                case "WHITE" -> {
                    String hexstring = Integer.toHexString(Color.WHITE.getRGB());
                    sb.append(hexstring);
                }
                case "YELLOW" -> {
                    String hexstring = Integer.toHexString(Color.YELLOW.getRGB());
                    sb.append(hexstring);
                }
                default -> {
                    throw new CodeGenException("Color not recognized");
                }
            }
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
