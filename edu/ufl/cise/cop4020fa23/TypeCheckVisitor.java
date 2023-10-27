package edu.ufl.cise.cop4020fa23;

import edu.ufl.cise.cop4020fa23.ast.*;
import edu.ufl.cise.cop4020fa23.exceptions.PLCCompilerException;
import edu.ufl.cise.cop4020fa23.exceptions.TypeCheckException;

import javax.swing.event.ChangeEvent;
import java.nio.channels.Channel;
import java.util.List;

public class TypeCheckVisitor implements ASTVisitor {
    Program root = null;
    SymbolTable st = null;

    @Override
    public Object visitAssignmentStatement(AssignmentStatement assignmentStatement, Object arg) throws PLCCompilerException {
        return null;
    }

    @Override
    public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg) throws PLCCompilerException {
        Type leftType = (Type) binaryExpr.getLeftExpr().visit(this, arg);
        Type rightType = (Type) binaryExpr.getRightExpr().visit(this, arg);
        Kind op = binaryExpr.getOp().kind();

        Type inferBinaryType;

        if (
                leftType == Type.PIXEL &&
                (op == Kind.BITAND || op == Kind.BITOR) && rightType == Type.PIXEL) {
            inferBinaryType = Type.PIXEL;
        }
        else if (
                leftType == Type.BOOLEAN &&
                (op == Kind.AND || op == Kind.OR) &&
                rightType == Type.BOOLEAN) {
            inferBinaryType = Type.BOOLEAN;
        }
        else if (
                leftType == Type.INT &&
                (op == Kind.LT || op == Kind.GT || op == Kind.LE || op == Kind.GE) &&
                rightType == Type.INT) {
            inferBinaryType = Type.BOOLEAN;
        }
        else if (op == Kind.EQ && leftType == rightType) {
            inferBinaryType = Type.BOOLEAN;
        }
        else if (
                leftType == Type.INT &&
                (op == Kind.EXP) &&
                rightType == Type.INT) {
            inferBinaryType = Type.INT;
        }
        else if (
                leftType == Type.PIXEL &&
                op == Kind.EXP &&
                rightType == Type.INT) {
            inferBinaryType = Type.PIXEL;
        }
        else if (op == Kind.PLUS && leftType == rightType) {
            inferBinaryType = leftType;
        }
        else if (
                (leftType == Type.INT || leftType == Type.PIXEL || leftType == Type.IMAGE) &&
                (op == Kind.MINUS || op == Kind.TIMES || op == Kind.DIV || op == Kind.MOD) &&
                (rightType == leftType)) {
            inferBinaryType = leftType;
        }
        else if (
                (leftType == Type.PIXEL || leftType == Type.IMAGE) &&
                (op == Kind.TIMES || op == Kind.DIV || op == Kind.MOD) &&
                rightType == Type.INT) {
            inferBinaryType = leftType;
        }
        else {
            throw new PLCCompilerException();
        }

        binaryExpr.setType(inferBinaryType);

        return inferBinaryType;
    }

    @Override
    public Object visitBlock(Block block, Object arg) throws PLCCompilerException {
        st.enterScope();
        List<Block.BlockElem> blockElems = block.getElems();

        for (Block.BlockElem elem : blockElems) {
            elem.visit(this, arg);
        }

        st.leaveScope();
        return block;
    }

    @Override
    public Object visitBlockStatement(StatementBlock statementBlock, Object arg) throws PLCCompilerException {
        return null;
    }

    @Override
    public Object visitChannelSelector(ChannelSelector channelSelector, Object arg) throws PLCCompilerException {
        return null;
    }

    @Override
    public Object visitConditionalExpr(ConditionalExpr conditionalExpr, Object arg) throws PLCCompilerException {
        check(
                conditionalExpr.getGuardExpr().visit(this, arg) == Type.BOOLEAN,
                conditionalExpr,
                "conditionalExpr guard expr is not boolean valued");
        check(
                conditionalExpr.getTrueExpr().visit(this, arg) == conditionalExpr.getFalseExpr().visit(this, arg),
                conditionalExpr,
                "conditionalExpr true and false exprs have different types"
        );

        conditionalExpr.setType(conditionalExpr.getTrueExpr().getType());

        return conditionalExpr.getType();
    }

    @Override
    public Object visitDeclaration(Declaration declaration, Object arg) throws PLCCompilerException {
        Expr expr = declaration.getInitializer();
        NameDef nameDef = declaration.getNameDef();

        expr.visit(this, arg);
        nameDef.visit(this, arg);

        check(
                expr == null ||
                expr.getType() == nameDef.getType() ||
                expr.getType() == Type.STRING ||
                nameDef.getType() == Type.IMAGE, declaration, "declaration has invalid type");

        declaration.setType(nameDef.getType());
        return declaration.getType();
    }

    @Override
    public Object visitDimension(Dimension dimension, Object arg) throws PLCCompilerException {
        Type typeW = (Type) dimension.getWidth().visit(this, arg);
        check(typeW == Type.INT, dimension, "image width must be an int");
        Type typeH = (Type) dimension.getHeight().visit(this, arg);
        check(typeH == Type.INT, dimension, "image height must be an int");
        return dimension;
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

    }

    @Override
    public Object visitIfStatement(IfStatement ifStatement, Object arg) throws PLCCompilerException {
        return null;
    }

    @Override
    public Object visitLValue(LValue lValue, Object arg) throws PLCCompilerException {
        return null;
    }

    @Override
    public Object visitNameDef(NameDef nameDef, Object arg) throws PLCCompilerException {
        Type type;
        if (nameDef.getDimension() == null){
            type = Type.IMAGE;
        }
        else {
            type = nameDef.getType();
        }
        nameDef.setType(type);
        return nameDef;
    }

    @Override
    public Object visitNumLitExpr(NumLitExpr numLitExpr, Object arg) throws PLCCompilerException {
        numLitExpr.setType(Type.INT);
        return Type.INT;
    }

    @Override
    public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws PLCCompilerException {
        return null;
    }

    @Override
    public Object visitPostfixExpr(PostfixExpr postfixExpr, Object arg) throws PLCCompilerException {
        Type postfixExprType = (Type) postfixExpr.primary().visit(this, arg);
        PixelSelector ps = postfixExpr.pixel();
        ChannelSelector cs = postfixExpr.channel();

        Type inferPostfixExprType = null;

        if (ps == null && cs == null) {
            inferPostfixExprType = postfixExprType;
        } else if (postfixExprType == Type.IMAGE && ps != null && cs == null) {
            inferPostfixExprType = Type.PIXEL;
        } else if (postfixExprType == Type.IMAGE && ps != null) {
            inferPostfixExprType = Type.INT;
        } else if (postfixExprType == Type.IMAGE) {
            inferPostfixExprType = Type.IMAGE;
        } else if (postfixExprType == Type.PIXEL && ps == null) {
            inferPostfixExprType = Type.INT;
        }

        postfixExpr.setType(inferPostfixExprType);
        return inferPostfixExprType;
    }

    @Override
    public Object visitProgram(Program program, Object arg) throws PLCCompilerException {
        root = program;
        Type type = Type.kind2type(program.getTypeToken().kind());
        program.setType(type);

        st.enterScope();
        List<NameDef> params = program.getParams();
        for (NameDef param : params) {
            param.visit(this, arg);
        }

        program.getBlock().visit(this, arg);
        st.leaveScope();
        return type;
    }

    @Override
    public Object visitReturnStatement(ReturnStatement returnStatement, Object arg) throws PLCCompilerException {
        return null;
    }

    @Override
    public Object visitStringLitExpr(StringLitExpr stringLitExpr, Object arg) throws PLCCompilerException {
        stringLitExpr.setType(Type.STRING);
        return Type.STRING;
    }

    @Override
    public Object visitUnaryExpr(UnaryExpr unaryExpr, Object arg) throws PLCCompilerException {
        Type exprType = (Type) unaryExpr.getExpr().visit(this, arg);
        Kind op = unaryExpr.getOp();

        Type inferUnaryExprType = null;

        if (exprType == Type.BOOLEAN && op == Kind.BANG) {
            inferUnaryExprType = Type.BOOLEAN;
        } else if (exprType == Type.INT && op == Kind.MINUS) {
            inferUnaryExprType = Type.INT;
        } else if (exprType == Type.IMAGE && (op == Kind.RES_width || op == Kind.RES_height)) {
            inferUnaryExprType = Type.INT;
        }

        unaryExpr.setType(inferUnaryExprType);
        return inferUnaryExprType;
    }

    @Override
    public Object visitWriteStatement(WriteStatement writeStatement, Object arg) throws PLCCompilerException {
        writeStatement.getExpr().visit(this, arg);
        return writeStatement;
    }

    @Override
    public Object visitBooleanLitExpr(BooleanLitExpr booleanLitExpr, Object arg) throws PLCCompilerException {
        return null;
    }

    @Override
    public Object visitConstExpr(ConstExpr constExpr, Object arg) throws PLCCompilerException {
        return null;
    }

    public void check(boolean cond, Object obj, String err) throws TypeCheckException {
        if (!cond) {
            throw new TypeCheckException(err);
        }
    }
}
