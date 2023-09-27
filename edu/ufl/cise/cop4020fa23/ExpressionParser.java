/*Copyright 2023 by Beverly A Sanders
 * 
 * This code is provided for solely for use of students in COP4020 Programming Language Concepts at the 
 * University of Florida during the fall semester 2023 as part of the course project.  
 * 
 * No other use is authorized. 
 * 
 * This code may not be posted on a public web site either during or after the course.  
 */
package edu.ufl.cise.cop4020fa23;

import static edu.ufl.cise.cop4020fa23.Kind.AND;
import static edu.ufl.cise.cop4020fa23.Kind.BANG;
import static edu.ufl.cise.cop4020fa23.Kind.BITAND;
import static edu.ufl.cise.cop4020fa23.Kind.BITOR;
import static edu.ufl.cise.cop4020fa23.Kind.COLON;
import static edu.ufl.cise.cop4020fa23.Kind.COMMA;
import static edu.ufl.cise.cop4020fa23.Kind.DIV;
import static edu.ufl.cise.cop4020fa23.Kind.EOF;
import static edu.ufl.cise.cop4020fa23.Kind.EQ;
import static edu.ufl.cise.cop4020fa23.Kind.EXP;
import static edu.ufl.cise.cop4020fa23.Kind.GE;
import static edu.ufl.cise.cop4020fa23.Kind.GT;
import static edu.ufl.cise.cop4020fa23.Kind.IDENT;
import static edu.ufl.cise.cop4020fa23.Kind.LE;
import static edu.ufl.cise.cop4020fa23.Kind.LPAREN;
import static edu.ufl.cise.cop4020fa23.Kind.LSQUARE;
import static edu.ufl.cise.cop4020fa23.Kind.LT;
import static edu.ufl.cise.cop4020fa23.Kind.MINUS;
import static edu.ufl.cise.cop4020fa23.Kind.MOD;
import static edu.ufl.cise.cop4020fa23.Kind.NUM_LIT;
import static edu.ufl.cise.cop4020fa23.Kind.OR;
import static edu.ufl.cise.cop4020fa23.Kind.PLUS;
import static edu.ufl.cise.cop4020fa23.Kind.QUESTION;
import static edu.ufl.cise.cop4020fa23.Kind.RARROW;
import static edu.ufl.cise.cop4020fa23.Kind.RES_blue;
import static edu.ufl.cise.cop4020fa23.Kind.RES_green;
import static edu.ufl.cise.cop4020fa23.Kind.RES_height;
import static edu.ufl.cise.cop4020fa23.Kind.RES_red;
import static edu.ufl.cise.cop4020fa23.Kind.RES_width;
import static edu.ufl.cise.cop4020fa23.Kind.RPAREN;
import static edu.ufl.cise.cop4020fa23.Kind.RSQUARE;
import static edu.ufl.cise.cop4020fa23.Kind.STRING_LIT;
import static edu.ufl.cise.cop4020fa23.Kind.TIMES;
import static edu.ufl.cise.cop4020fa23.Kind.CONST;
import static edu.ufl.cise.cop4020fa23.Kind.BLOCK_OPEN;
import static edu.ufl.cise.cop4020fa23.Kind.BLOCK_CLOSE;
import static edu.ufl.cise.cop4020fa23.Kind.SEMI;


import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import edu.ufl.cise.cop4020fa23.ast.AST;
import edu.ufl.cise.cop4020fa23.ast.BinaryExpr;
import edu.ufl.cise.cop4020fa23.ast.BooleanLitExpr;
import edu.ufl.cise.cop4020fa23.ast.ChannelSelector;
import edu.ufl.cise.cop4020fa23.ast.ConditionalExpr;
import edu.ufl.cise.cop4020fa23.ast.ConstExpr;
import edu.ufl.cise.cop4020fa23.ast.ExpandedPixelExpr;
import edu.ufl.cise.cop4020fa23.ast.Expr;
import edu.ufl.cise.cop4020fa23.ast.IdentExpr;
import edu.ufl.cise.cop4020fa23.ast.NumLitExpr;
import edu.ufl.cise.cop4020fa23.ast.PixelSelector;
import edu.ufl.cise.cop4020fa23.ast.PostfixExpr;
import edu.ufl.cise.cop4020fa23.ast.StringLitExpr;
import edu.ufl.cise.cop4020fa23.ast.UnaryExpr;
import edu.ufl.cise.cop4020fa23.exceptions.LexicalException;
import edu.ufl.cise.cop4020fa23.exceptions.PLCCompilerException;
import edu.ufl.cise.cop4020fa23.exceptions.SyntaxException;
/**
Expr::=  ConditionalExpr | LogicalOrExpr    
ConditionalExpr ::=  ?  Expr  :  Expr  :  Expr 
LogicalOrExpr ::= LogicalAndExpr (    (   |   |   ||   ) LogicalAndExpr)*
LogicalAndExpr ::=  ComparisonExpr ( (   &   |  &&   )  ComparisonExpr)*
ComparisonExpr ::= PowExpr ( (< | > | == | <= | >=) PowExpr)*
PowExpr ::= AdditiveExpr ** PowExpr |   AdditiveExpr
AdditiveExpr ::= MultiplicativeExpr ( ( + | -  ) MultiplicativeExpr )*
MultiplicativeExpr ::= UnaryExpr (( * |  /  |  % ) UnaryExpr)*
UnaryExpr ::=  ( ! | - | length | width) UnaryExpr  |  UnaryExprPostfix
UnaryExprPostfix::= PrimaryExpr (PixelSelector | ε ) (ChannelSelector | ε )
PrimaryExpr ::=STRING_LIT | NUM_LIT |  IDENT | ( Expr ) | Z 
    ExpandedPixel  
ChannelSelector ::= : red | : green | : blue
PixelSelector  ::= [ Expr , Expr ]
ExpandedPixel ::= [ Expr , Expr , Expr ]
Dimension  ::=  [ Expr , Expr ]                         

 */

public class ExpressionParser implements IParser {
	
	final ILexer lexer;
	private IToken t;

	enum ASTNodeNames
	{
		Expr,
		ConditionalExpr,
		LogicalOrExpr,
		LogicalAndExpr,
		ComparisonExpr,
		PowExpr,
		AdditiveExpr,
		MultiplicativeExpr,
		UnaryExpr,
		PostfixExpr,
		PrimaryExpr,
		PixelSelector,
		ChannelSelector,
		ExpandedPixelExpr
	};

	final static ArrayList<HashSet<Kind>> FOLLOW = new ArrayList<>(ASTNodeNames.values().length);

	static{
		FOLLOW.get(ASTNodeNames.Expr.ordinal()).add(RSQUARE);
		FOLLOW.get(ASTNodeNames.Expr.ordinal()).add(RPAREN);

		FOLLOW.set(ASTNodeNames.ConditionalExpr.ordinal(), FOLLOW.get(ASTNodeNames.Expr.ordinal()));

		FOLLOW.set(ASTNodeNames.LogicalOrExpr.ordinal(), FOLLOW.get(ASTNodeNames.Expr.ordinal()));

		FOLLOW.get(ASTNodeNames.LogicalAndExpr.ordinal()).add(BITOR);
		FOLLOW.get(ASTNodeNames.LogicalAndExpr.ordinal()).add(OR);

		FOLLOW.get(ASTNodeNames.ComparisonExpr.ordinal()).add(BITAND);
		FOLLOW.get(ASTNodeNames.ComparisonExpr.ordinal()).add(AND);

		FOLLOW.set(ASTNodeNames.PowExpr.ordinal(), FOLLOW.get(ASTNodeNames.ComparisonExpr.ordinal()));
		FOLLOW.get(ASTNodeNames.PowExpr.ordinal()).add(EQ);
		FOLLOW.get(ASTNodeNames.PowExpr.ordinal()).add(LE);
		FOLLOW.get(ASTNodeNames.PowExpr.ordinal()).add(GE);

		FOLLOW.set(ASTNodeNames.AdditiveExpr.ordinal(), FOLLOW.get(ASTNodeNames.PowExpr.ordinal()));
		FOLLOW.get(ASTNodeNames.AdditiveExpr.ordinal()).add(PLUS);
		FOLLOW.get(ASTNodeNames.AdditiveExpr.ordinal()).add(MINUS);

		FOLLOW.set(ASTNodeNames.MultiplicativeExpr.ordinal(), FOLLOW.get(ASTNodeNames.AdditiveExpr.ordinal()));
		FOLLOW.get(ASTNodeNames.MultiplicativeExpr.ordinal()).add(TIMES);
		FOLLOW.get(ASTNodeNames.MultiplicativeExpr.ordinal()).add(DIV);
		FOLLOW.get(ASTNodeNames.MultiplicativeExpr.ordinal()).add(MOD);

		FOLLOW.set(ASTNodeNames.UnaryExpr.ordinal(), FOLLOW.get(ASTNodeNames.MultiplicativeExpr.ordinal()));
		FOLLOW.get(ASTNodeNames.UnaryExpr.ordinal()).add(EXP);

		FOLLOW.set(ASTNodeNames.PostfixExpr.ordinal(), FOLLOW.get(ASTNodeNames.UnaryExpr.ordinal()));

		FOLLOW.set(ASTNodeNames.PrimaryExpr.ordinal(), new HashSet<>(Arrays.asList(RARROW, COMMA, SEMI)));

		FOLLOW.set(ASTNodeNames.PixelSelector.ordinal(), new HashSet<>(Arrays.asList(BLOCK_CLOSE, COMMA, SEMI)));

		FOLLOW.set(ASTNodeNames.ChannelSelector.ordinal(), new HashSet<>(Arrays.asList(PLUS, MINUS, TIMES, EXP, DIV, MOD)));

		FOLLOW.set(ASTNodeNames.ExpandedPixelExpr.ordinal(), FOLLOW.get(ASTNodeNames.PrimaryExpr.ordinal()));

	}

	final static ArrayList<HashSet<Kind>> FIRST = new ArrayList<>(ASTNodeNames.values().length);

	static {
		for (int i = 0; i < ASTNodeNames.values().length; i++) {
			FIRST.add(new HashSet<Kind>());
		}

		// FIRST(LogicalOrExpr) = FIRST(LogicalAndExpr) = FIRST(ComparisonExpr) = ...
		FIRST.get(ASTNodeNames.LogicalOrExpr.ordinal()).addAll(FIRST.get(ASTNodeNames.LogicalAndExpr.ordinal()));
		FIRST.get(ASTNodeNames.LogicalAndExpr.ordinal()).addAll(FIRST.get(ASTNodeNames.ComparisonExpr.ordinal()));
		FIRST.get(ASTNodeNames.ComparisonExpr.ordinal()).addAll(FIRST.get(ASTNodeNames.PowExpr.ordinal()));
		FIRST.get(ASTNodeNames.PowExpr.ordinal()).addAll(FIRST.get(ASTNodeNames.AdditiveExpr.ordinal()));
		FIRST.get(ASTNodeNames.AdditiveExpr.ordinal()).addAll(FIRST.get(ASTNodeNames.MultiplicativeExpr.ordinal()));
		FIRST.get(ASTNodeNames.MultiplicativeExpr.ordinal()).addAll(FIRST.get(ASTNodeNames.UnaryExpr.ordinal()));


		// FIRST(Expr) = FIRST(ConditionalExpr) + FIRST(LogicalOrExpr)
		FIRST.get(ASTNodeNames.ConditionalExpr.ordinal()).add(QUESTION);

		FIRST.get(ASTNodeNames.Expr.ordinal()).addAll(FIRST.get(ASTNodeNames.ConditionalExpr.ordinal()));
		FIRST.get(ASTNodeNames.Expr.ordinal()).addAll(FIRST.get(ASTNodeNames.LogicalOrExpr.ordinal()));

		FIRST.get(ASTNodeNames.UnaryExpr.ordinal()).addAll(Arrays.asList(BANG, MINUS, RES_width, RES_height));


		// FIRST(PostfixExpr) = FIRST(PrimaryExpr) = ...
		FIRST.get(ASTNodeNames.PrimaryExpr.ordinal()).addAll(Arrays.asList(STRING_LIT, NUM_LIT, IDENT, LPAREN, CONST, LSQUARE));
		FIRST.get(ASTNodeNames.PostfixExpr.ordinal()).addAll(FIRST.get(ASTNodeNames.PrimaryExpr.ordinal()));

		// FIRST(ExpandedPixelExpr) = { [ }
		FIRST.get(ASTNodeNames.ExpandedPixelExpr.ordinal()).add(LSQUARE);

		// FIRST(ChannelSelector) = { : }
		FIRST.get(ASTNodeNames.ChannelSelector.ordinal()).add(COLON);

		// FIRST(PixelSelector) = { [ }
		FIRST.get(ASTNodeNames.PixelSelector.ordinal()).add(LSQUARE);
	}


	/**
	 * @param lexer
	 * @throws LexicalException 
	 */
	public ExpressionParser(ILexer lexer) throws LexicalException {
		super();
		this.lexer = lexer;
		t = lexer.next();
	}


	@Override
	public AST parse() throws PLCCompilerException {
		Expr e = expr();
		return e;
	}

	private Expr expr() throws PLCCompilerException {
		IToken firstToken = t;
		throw new UnsupportedOperationException("THE PARSER HAS NOT BEEN IMPLEMENTED YET");
	}

	private Expr conditionalExpr() throws PLCCompilerException {
		IToken firstToken = t;
		throw new UnsupportedOperationException("THE PARSER HAS NOT BEEN IMPLEMENTED YET");
	}

	private Expr logicalOrExpr() throws PLCCompilerException {
		IToken firstToken = t;
		throw new UnsupportedOperationException("THE PARSER HAS NOT BEEN IMPLEMENTED YET");
	}

	private Expr logicalAndExpr() throws PLCCompilerException {
		IToken firstToken = t;
		throw new UnsupportedOperationException("THE PARSER HAS NOT BEEN IMPLEMENTED YET");
	}

	private Expr comparisonExpr() throws PLCCompilerException {
		IToken firstToken = t;
		throw new UnsupportedOperationException("THE PARSER HAS NOT BEEN IMPLEMENTED YET");
	}

	private Expr powExpr() throws PLCCompilerException {
		IToken firstToken = t;
		throw new UnsupportedOperationException("THE PARSER HAS NOT BEEN IMPLEMENTED YET");
	}

	private Expr additiveExpr() throws PLCCompilerException {
		IToken firstToken = t;
		throw new UnsupportedOperationException("THE PARSER HAS NOT BEEN IMPLEMENTED YET");
	}



}
