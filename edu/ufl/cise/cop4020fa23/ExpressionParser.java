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
import static edu.ufl.cise.cop4020fa23.Kind.RES_height;
import static edu.ufl.cise.cop4020fa23.Kind.RES_width;
import static edu.ufl.cise.cop4020fa23.Kind.RSQUARE;
import static edu.ufl.cise.cop4020fa23.Kind.STRING_LIT;
import static edu.ufl.cise.cop4020fa23.Kind.TIMES;
import static edu.ufl.cise.cop4020fa23.Kind.CONST;
import static edu.ufl.cise.cop4020fa23.Kind.BOOLEAN_LIT;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import edu.ufl.cise.cop4020fa23.ast.AST;
import edu.ufl.cise.cop4020fa23.ast.ConditionalExpr;
import edu.ufl.cise.cop4020fa23.ast.Expr;
import edu.ufl.cise.cop4020fa23.exceptions.LexicalException;
import edu.ufl.cise.cop4020fa23.exceptions.PLCCompilerException;
import edu.ufl.cise.cop4020fa23.exceptions.SyntaxException;
import edu.ufl.cise.cop4020fa23.ast.BinaryExpr;
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
		FOLLOW.set(ASTNodeNames.Expr.ordinal(), new HashSet<>(Arrays.asList(COMMA, RSQUARE, RARROW)));
		FOLLOW.set(ASTNodeNames.ConditionalExpr.ordinal(), new HashSet<>(Arrays.asList(COMMA, RSQUARE, RARROW)));
		FOLLOW.set(ASTNodeNames.LogicalOrExpr.ordinal(), new HashSet<>(Arrays.asList(COMMA, RSQUARE, RARROW)));
		FOLLOW.set(ASTNodeNames.LogicalAndExpr.ordinal(), new HashSet<>(Arrays.asList(BITOR, OR, COMMA, RSQUARE, RARROW)));
		FOLLOW.set(ASTNodeNames.ComparisonExpr.ordinal(), new HashSet<>(Arrays.asList(BITAND, AND, BITOR, OR, COMMA, RSQUARE, RARROW)));
		FOLLOW.set(ASTNodeNames.PowExpr.ordinal(), new HashSet<>(Arrays.asList(LT, GT, EQ, LE, GE, BITAND, AND, BITOR, OR, COMMA, RSQUARE, RARROW)));
		FOLLOW.set(ASTNodeNames.AdditiveExpr.ordinal(), new HashSet<>(Arrays.asList(EXP, LT, GT, EQ, LE, GE, BITAND, AND, BITOR, OR, COMMA, RSQUARE, RARROW)));
		FOLLOW.set(ASTNodeNames.MultiplicativeExpr.ordinal(), new HashSet<>(Arrays.asList(PLUS, MINUS, EXP, LT, GT, EQ, LE, GE, BITAND, AND, BITOR, OR, COMMA, RSQUARE, RARROW)));
		FOLLOW.set(ASTNodeNames.UnaryExpr.ordinal(), new HashSet<>(Arrays.asList(TIMES, DIV, MOD, PLUS, MINUS, EXP, LT, GT, EQ, LE, GE, BITAND, AND, BITOR, OR, COMMA, RSQUARE, RARROW)));
		FOLLOW.set(ASTNodeNames.PostfixExpr.ordinal(), new HashSet<>(Arrays.asList(TIMES, DIV, MOD, PLUS, MINUS, EXP, LT, GT, EQ, LE, GE, BITAND, AND, BITOR, OR, COMMA, RSQUARE, RARROW)));
		FOLLOW.set(ASTNodeNames.PrimaryExpr.ordinal(), new HashSet<>(Arrays.asList(TIMES, DIV, MOD, PLUS, MINUS, EXP, LT, GT, EQ, LE, GE, BITAND, AND, BITOR, OR, COMMA, RSQUARE, RARROW)));
		FOLLOW.set(ASTNodeNames.ChannelSelector.ordinal(), new HashSet<>(Arrays.asList(TIMES, DIV, MOD, PLUS, MINUS, EXP, LT, GT, EQ, LE, GE, BITAND, AND, BITOR, OR, COMMA, RSQUARE, RARROW)));
		FOLLOW.set(ASTNodeNames.PixelSelector.ordinal(), new HashSet<>(Arrays.asList(TIMES, DIV, MOD, PLUS, MINUS, EXP, LT, GT, EQ, LE, GE, BITAND, AND, BITOR, OR, COMMA, RSQUARE, RARROW)));
		FOLLOW.set(ASTNodeNames.ExpandedPixelExpr.ordinal(), new HashSet<>(Arrays.asList(TIMES, DIV, MOD, PLUS, MINUS, EXP, LT, GT, EQ, LE, GE, BITAND, AND, BITOR, OR, COMMA, RSQUARE, RARROW)));
	}

	final static ArrayList<HashSet<Kind>> FIRST = new ArrayList<>(ASTNodeNames.values().length);

	static {
		FIRST.set(ASTNodeNames.Expr.ordinal(), new HashSet<>(Arrays.asList(BANG, MINUS, RES_width, RES_height, QUESTION)));
		FIRST.set(ASTNodeNames.ConditionalExpr.ordinal(), new HashSet<>(Arrays.asList(QUESTION)));
		FIRST.set(ASTNodeNames.LogicalOrExpr.ordinal(), new HashSet<>(Arrays.asList(BANG, MINUS, RES_width, RES_height)));
		FIRST.set(ASTNodeNames.LogicalAndExpr.ordinal(), new HashSet<>(Arrays.asList(BANG, MINUS, RES_width, RES_height)));
		FIRST.set(ASTNodeNames.ComparisonExpr.ordinal(), new HashSet<>(Arrays.asList(BANG, MINUS, RES_width, RES_height)));
		FIRST.set(ASTNodeNames.PowExpr.ordinal(), new HashSet<>(Arrays.asList(BANG, MINUS, RES_width, RES_height)));
		FIRST.set(ASTNodeNames.AdditiveExpr.ordinal(), new HashSet<>(Arrays.asList(BANG, MINUS, RES_width, RES_height)));
		FIRST.set(ASTNodeNames.MultiplicativeExpr.ordinal(), new HashSet<>(Arrays.asList(BANG, MINUS, RES_width, RES_height)));
		FIRST.set(ASTNodeNames.UnaryExpr.ordinal(), new HashSet<>(Arrays.asList(BANG, MINUS, RES_width, RES_height)));
		FIRST.set(ASTNodeNames.PostfixExpr.ordinal(), new HashSet<>(Arrays.asList(STRING_LIT, NUM_LIT, BOOLEAN_LIT, IDENT, LPAREN, CONST, LSQUARE)));
		FIRST.set(ASTNodeNames.PrimaryExpr.ordinal(), new HashSet<>(Arrays.asList(STRING_LIT, NUM_LIT, BOOLEAN_LIT, IDENT, LPAREN, CONST, LSQUARE)));
		FIRST.set(ASTNodeNames.ChannelSelector.ordinal(), new HashSet<>(Arrays.asList(COLON)));
		FIRST.set(ASTNodeNames.PixelSelector.ordinal(), new HashSet<>(Arrays.asList( LSQUARE)));
		FIRST.set(ASTNodeNames.ExpandedPixelExpr.ordinal(), new HashSet<>(Arrays.asList(LSQUARE)));
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
//		IToken firstToken = t;

		if (in_first(ASTNodeNames.ConditionalExpr, t))
		{
			return conditionalExpr();
		}
		else if (in_first(ASTNodeNames.LogicalOrExpr, t))
		{
			return logicalOrExpr();
		}

		throw new PLCCompilerException();
	}

	private Expr conditionalExpr() throws PLCCompilerException {
		IToken firstToken = t;

		match(QUESTION);
		Expr expr1 = expr();
		match(RARROW);
		Expr expr2 = expr();
		match(COMMA);
		Expr expr3 = expr();

		return new ConditionalExpr(t, expr1, expr2, expr3);
	}

	private Expr logicalOrExpr() throws PLCCompilerException {
		IToken firstToken = t;

		Expr expr1 = logicalAndExpr();
		boolean at_least_one = false;

		while (in(BITOR, OR))
		{
			IToken orr = match(BITOR, OR);

			Expr expr2 = logicalAndExpr();

			expr1 = new BinaryExpr(t, expr1, orr, expr2);

			at_least_one = true;
		}

		if (!at_least_one)
			throw new SyntaxException();

		return expr1;
	}

	private Expr logicalAndExpr() throws PLCCompilerException {
		IToken firstToken = t;

		Expr expr1 = comparisonExpr();

		boolean at_least_one = false;

		while (in(BITAND, AND))
		{
			IToken andd = match(BITAND, AND);

			Expr expr2 = comparisonExpr();

			expr1 = new BinaryExpr(t, expr1, andd, expr2);

			at_least_one = true;
		}

		if (!at_least_one)
			throw new SyntaxException();

		return expr1;
	}

	private Expr comparisonExpr() throws PLCCompilerException {
		IToken firstToken = t;

		Expr expr1 = powExpr();

		boolean at_least_one = false;

		while (in(LT, LE, GT, GE, EQ))
		{
			IToken cmp = match(LT, LE, GT, GE, EQ);

			Expr expr2 = powExpr();

			expr1 = new BinaryExpr(t, expr1, cmp, expr2);

			at_least_one = true;
		}

		if (!at_least_one)
			throw new SyntaxException();

		return expr1;
	}

	private Expr powExpr() throws PLCCompilerException {
		IToken firstToken = t;

		Expr expr1 = additiveExpr();

		if (in(EXP))
		{
			IToken exponent = match(EXP);

			Expr expr2 = powExpr();

			return new BinaryExpr(t, expr1, exponent, expr2);
		}

		return expr1;
	}

	private Expr additiveExpr() throws PLCCompilerException {
		IToken firstToken = t;

		Expr expr1 = multiplicativeExpr();



		throw new UnsupportedOperationException("THE PARSER HAS NOT BEEN IMPLEMENTED YET");
	}


	private IToken match(Kind c) throws LexicalException, SyntaxException
	{
		if (t.kind() == c)
		{
			IToken temp = t;
			t = lexer.next();
			return temp;
		}

		throw new SyntaxException();
	}

	private IToken match(Kind... c) throws LexicalException, SyntaxException
	{
		for (Kind a : c)
		{
			if (t.kind() == a)
			{
				IToken temp = t;
				t = lexer.next();
				return temp;
			}
		}

		throw new SyntaxException();
	}

	private boolean in(Kind... c) throws LexicalException, SyntaxException
	{
		for (Kind a : c)
		{
			if (t.kind() == a)
			{
				return true;
			}
		}

		return false;
	}

	private boolean in_first(ASTNodeNames name, IToken token)
	{
		// For now, the predict sets are identically equal to the first sets, thus this will act accordingly
		return FIRST.get(name.ordinal()).contains(token.kind());
	}

	private boolean in_follow(ASTNodeNames name, IToken token)
	{
		return FOLLOW.get(name.ordinal()).contains(token.kind());
	}
}
