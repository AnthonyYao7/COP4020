package edu.ufl.cise.cop4020fa23;

import edu.ufl.cise.cop4020fa23.ast.*;
import edu.ufl.cise.cop4020fa23.exceptions.PLCCompilerException;

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

        if (
                leftExpr.getType() == Type.STRING &&
                op == Kind.EQ) {
            leftExpr.visit(this, sb);
            sb.append(".equals(");
            rightExpr.visit(this, sb);
            sb.append(')');
        } else if (op == Kind.EXP) {
            sb.append("((int)Math.round(Math.pow(");
            sb.append(leftExpr.visit(this, sb));
            sb.append(',');
            sb.append(rightExpr.visit(this, sb));
            sb.append(")))");
        } else {
            sb.append('(');
            leftExpr.visit(this, sb);
            sb.append(binaryExpr.getOp().toString());
            rightExpr.visit(this, sb);
            sb.append(')');
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

        sb.append(declaration.getNameDef().visit(this, sb));
        if (declaration.getInitializer() != null) {
            sb.append('=');
            sb.append(declaration.getInitializer().visit(this, sb));
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
        return identExpr.getNameDef().getJavaName();
    }

    @Override
    public Object visitIfStatement(IfStatement ifStatement, Object arg) throws PLCCompilerException {
        return null;
    }

    @Override
    public Object visitLValue(LValue lValue, Object arg) throws PLCCompilerException {
        return lValue.getNameDef().getJavaName();
    }

    @Override
    public Object visitNameDef(NameDef nameDef, Object arg) throws PLCCompilerException {
        StringBuilder sb;
        if (arg != null) {
            sb = (StringBuilder) arg;
        } else {
            sb = new StringBuilder();
        }

        sb.append(nameDef.getType());
        sb.append(' ');
        sb.append(nameDef.getJavaName());

        return (arg == null ? sb.toString() : null);
    }

    @Override
    public Object visitNumLitExpr(NumLitExpr numLitExpr, Object arg) throws PLCCompilerException {
        return numLitExpr.getText();
    }

    @Override
    public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws PLCCompilerException {
        return null;
    }

    @Override
    public Object visitPostfixExpr(PostfixExpr postfixExpr, Object arg) throws PLCCompilerException {
        return null;
    }

    @Override
    public Object visitProgram(Program program, Object arg) throws PLCCompilerException {
        StringBuilder sb = new StringBuilder();

        sb.append("package edu.ufl.cise.cop4020fa23;\n");
        sb.append("import edu.ufl.cise.cop4020fa23.runtime.ConsoleIO;\n");

        sb.append("\npublic class ");
        sb.append(program.getName());
        sb.append("{\npublic static ");
        sb.append(fixTyping(program.getType()));
        sb.append(" apply(\n");

        for (NameDef nd : program.getParams()) {
            nd.visit(this, sb);
            sb.append(',');
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
        return stringLitExpr.getText();
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
        sb.append(op.toString());
        expr.visit(this, sb);
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
//
//        sb.append("true");
//
        return (arg == null ? sb.toString() : null);
    }

    @Override
    public Object visitConstExpr(ConstExpr constExpr, Object arg) throws PLCCompilerException {
        return null;
    }

    public String fixTyping(Type type) {
        if(type == Type.INT) {
            return "int";
        }

        return null;
    }
}
