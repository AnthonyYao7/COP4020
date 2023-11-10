package edu.ufl.cise.cop4020fa23;

import edu.ufl.cise.cop4020fa23.ast.*;
import edu.ufl.cise.cop4020fa23.exceptions.PLCCompilerException;
import edu.ufl.cise.cop4020fa23.exceptions.TypeCheckException;

import java.util.List;

public class TypeCheckVisitor implements ASTVisitor {
    Program root = null;
    SymbolTable st = new SymbolTable();

    private boolean assignmentCompatible(Type lvalueType, Type exprType) {
        if(lvalueType == exprType){
            return true;
        }
        else if(lvalueType == Type.PIXEL && exprType == Type.INT) {
            return true;
        }
        else if(lvalueType == Type.IMAGE &&
                (exprType == Type.PIXEL ||
                exprType == Type.INT||
                exprType ==  Type.STRING)) {
            return true;
        }
        return false;
    }

    @Override
    public Object visitAssignmentStatement(AssignmentStatement assignmentStatement, Object arg) throws PLCCompilerException {
        st.enterScope();

        Type lvalueType = (Type) assignmentStatement.getlValue().visit(this, arg);
        Type exprType = (Type) assignmentStatement.getE().visit(this, arg);

        check(assignmentCompatible(lvalueType, exprType), assignmentStatement, "Types Incompatible");
        st.leaveScope();

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
            throw new TypeCheckException("Operand Types Don't Match");
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
        st.enterScope();
        statementBlock.getBlock().visit(this, arg);
        st.leaveScope();

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

        if (expr != null) {
            expr.visit(this, arg);
        }

        nameDef.visit(this, arg);

        check(
                expr == null ||
                expr.getType() == nameDef.getType() ||
                (expr.getType() == Type.STRING && nameDef.getType() == Type.IMAGE),
                declaration, "declaration has invalid type");

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
        List<GuardedBlock> guardedBlocks = doStatement.getGuardedBlocks();

        for(GuardedBlock block: guardedBlocks) {
            block.visit(this, arg);
        }

        return doStatement;
    }

    @Override
    public Object visitExpandedPixelExpr(ExpandedPixelExpr expandedPixelExpr, Object arg) throws PLCCompilerException {
        Type redType = (Type) expandedPixelExpr.getRed().visit(this, arg);
        Type greenType = (Type) expandedPixelExpr.getGreen().visit(this, arg);
        Type blueType = (Type) expandedPixelExpr.getBlue().visit(this, arg);

        check(redType == Type.INT, expandedPixelExpr, "Red component must be of type INT");
        check(greenType == Type.INT, expandedPixelExpr, "Green component must be of type INT");
        check(blueType == Type.INT, expandedPixelExpr, "Blue component must be of type INT");

        expandedPixelExpr.setType(Type.PIXEL);

        return Type.PIXEL;
    }

    @Override
    public Object visitGuardedBlock(GuardedBlock guardedBlock, Object arg) throws PLCCompilerException {
        Type guardType = (Type) guardedBlock.getGuard().visit(this, arg);
        check(guardType == Type.BOOLEAN, guardedBlock, "Guard type not type bool");
        guardedBlock.getBlock().visit(this, arg);

        return null;
    }

    @Override
    public Object visitIdentExpr(IdentExpr identExpr, Object arg) throws PLCCompilerException {
        NameDef nd = st.lookup(identExpr.getName());
        check(nd != null, identExpr, "identifier has not been declared "+ identExpr.getName());
        identExpr.setNameDef(nd);
        identExpr.setType(nd.getType());
        return nd.getType();
    }

    @Override
    public Object visitIfStatement(IfStatement ifStatement, Object arg) throws PLCCompilerException {
        List<GuardedBlock> guardedBlocks = ifStatement.getGuardedBlocks();

        for(GuardedBlock block: guardedBlocks) {
            block.visit(this, arg);
        }

        return ifStatement;
    }

    @Override
    public Object visitLValue(LValue lValue, Object arg) throws PLCCompilerException {
        PixelSelector ps = lValue.getPixelSelector();
        if (ps != null) {
            ps.visit(this, lValue);
        }
        ChannelSelector cs = lValue.getChannelSelector();
        if (cs != null) {
            cs.visit(this, arg);
        }

        lValue.setNameDef(st.lookup(lValue.getName()));
        lValue.setType(lValue.getNameDef().getType());
        Type inferLValueType = null;

        if (lValue.getPixelSelector() != null && lValue.getChannelSelector() == null) {
            if (lValue.getType() == Type.IMAGE) {
                inferLValueType = Type.PIXEL;
            } else if (lValue.getType() == Type.PIXEL) {
                inferLValueType = Type.INT;
            }
        } else if (lValue.getPixelSelector() != null && lValue.getChannelSelector() != null) {
            inferLValueType = Type.INT;
        } else if (lValue.getChannelSelector() != null) {
            if (lValue.getType() == Type.IMAGE) {
                inferLValueType = Type.IMAGE;
            } else if (lValue.getType() == Type.PIXEL) {
                inferLValueType = Type.INT;
            }
        }

        if(inferLValueType != null) {
            lValue.setType(inferLValueType);
        }

        return lValue.getType();
    }

    @Override
    public Object visitNameDef(NameDef nameDef, Object arg) throws PLCCompilerException {
        Type type;

        check(nameDef.getType() != Type.VOID, nameDef, "Invalid DataType");

        if (nameDef.getDimension() != null){
            nameDef.getDimension().visit(this, arg);
            type = Type.IMAGE;
        } else {
            type = nameDef.getType();
        }

        nameDef.setType(type);
        int scope = st.insert(nameDef);
        nameDef.setJavaName(nameDef.getName() + '$' + scope);

        return type;
    }

    @Override
    public Object visitNumLitExpr(NumLitExpr numLitExpr, Object arg) throws PLCCompilerException {
        numLitExpr.setType(Type.INT);
        return Type.INT;
    }

    @Override
    public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws PLCCompilerException {
        Expr xExpr = pixelSelector.xExpr();
        Expr yExpr = pixelSelector.yExpr();


        if (arg instanceof LValue) {
            check(xExpr instanceof IdentExpr || xExpr instanceof NumLitExpr, pixelSelector, "xExpr is not Ident or NumLit");
            check(yExpr instanceof IdentExpr || yExpr instanceof NumLitExpr, pixelSelector, "xExpr is not Ident or NumLit");

            if (
                xExpr instanceof IdentExpr && st.lookup(((IdentExpr) xExpr).getName()) == null ||
                yExpr instanceof IdentExpr && st.lookup(((IdentExpr) yExpr).getName()) == null) {
                st.insert(new SyntheticNameDef(((IdentExpr) xExpr).getName()));
                st.insert(new SyntheticNameDef(((IdentExpr) yExpr).getName()));
            }
        }

        xExpr.visit(this, arg);
        yExpr.visit(this, arg);

        check(xExpr.getType() == Type.INT, pixelSelector, "xExpr is not Type INT");
        check(yExpr.getType() == Type.INT, pixelSelector, "yExpr is not Type INT");

        return null;
    }

    @Override
    public Object visitPostfixExpr(PostfixExpr postfixExpr, Object arg) throws PLCCompilerException {
        Type postfixExprType = (Type) postfixExpr.primary().visit(this, arg);
        PixelSelector ps = postfixExpr.pixel();
        ChannelSelector cs = postfixExpr.channel();

        Type inferPostfixExprType = getInferPostfixExprType(ps, cs, postfixExprType);

        assert ps != null;
        ps.visit(this, arg);
        assert cs != null;
        cs.visit(this, arg);

        postfixExpr.setType(inferPostfixExprType);
        return inferPostfixExprType;
    }

    private static Type getInferPostfixExprType(PixelSelector ps, ChannelSelector cs, Type postfixExprType) {
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
        Type returnType = (Type) returnStatement.getE().visit(this, arg);
        check(returnType == root.getType(), returnStatement, "Type mismatch");

        return returnType;
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
        booleanLitExpr.setType(Type.BOOLEAN);
        return Type.BOOLEAN;
    }

    @Override
    public Object visitConstExpr(ConstExpr constExpr, Object arg) throws PLCCompilerException {
        Type t;
        if (constExpr.getName().equals("Z")) {
            t = Type.INT;
        } else {
            t = Type.PIXEL;
        }

        constExpr.setType(t);
        return t;
    }

    public void check(boolean cond, Object obj, String err) throws TypeCheckException {
        if (!cond) {
            throw new TypeCheckException(err);
        }
    }
}
