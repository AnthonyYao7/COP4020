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


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import edu.ufl.cise.cop4020fa23.ast.*;
import edu.ufl.cise.cop4020fa23.exceptions.LexicalException;
import edu.ufl.cise.cop4020fa23.exceptions.SyntaxException;
import edu.ufl.cise.cop4020fa23.exceptions.SyntaxException;

import static edu.ufl.cise.cop4020fa23.Kind.*;


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
		for (int i = 0; i < ASTNodeNames.values().length; i++) {
			FOLLOW.add(null);
		}

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
		for (int i = 0; i < ASTNodeNames.values().length; i++) {
			FIRST.add(null);
		}

		FIRST.set(ASTNodeNames.Expr.ordinal(), new HashSet<>(
				Arrays.asList(BANG, MINUS, RES_width, RES_height, QUESTION, STRING_LIT, NUM_LIT, BOOLEAN_LIT, IDENT, LPAREN, CONST, LSQUARE)));
		FIRST.set(ASTNodeNames.ConditionalExpr.ordinal(), new HashSet<>(
				Arrays.asList(QUESTION)));
		FIRST.set(ASTNodeNames.LogicalOrExpr.ordinal(), new HashSet<>(
				Arrays.asList(BANG, MINUS, RES_width, RES_height, STRING_LIT, NUM_LIT, BOOLEAN_LIT, IDENT, LPAREN, CONST, LSQUARE)));
		FIRST.set(ASTNodeNames.LogicalAndExpr.ordinal(), new HashSet<>(
				Arrays.asList(BANG, MINUS, RES_width, RES_height, STRING_LIT, NUM_LIT, BOOLEAN_LIT, IDENT, LPAREN, CONST, LSQUARE)));
		FIRST.set(ASTNodeNames.ComparisonExpr.ordinal(), new HashSet<>(
				Arrays.asList(BANG, MINUS, RES_width, RES_height, STRING_LIT, NUM_LIT, BOOLEAN_LIT, IDENT, LPAREN, CONST, LSQUARE)));
		FIRST.set(ASTNodeNames.PowExpr.ordinal(), new HashSet<>(
				Arrays.asList(BANG, MINUS, RES_width, RES_height, STRING_LIT, NUM_LIT, BOOLEAN_LIT, IDENT, LPAREN, CONST, LSQUARE)));
		FIRST.set(ASTNodeNames.AdditiveExpr.ordinal(), new HashSet<>(
				Arrays.asList(BANG, MINUS, RES_width, RES_height, STRING_LIT, NUM_LIT, BOOLEAN_LIT, IDENT, LPAREN, CONST, LSQUARE)));
		FIRST.set(ASTNodeNames.MultiplicativeExpr.ordinal(), new HashSet<>(
				Arrays.asList(BANG, MINUS, RES_width, RES_height, STRING_LIT, NUM_LIT, BOOLEAN_LIT, IDENT, LPAREN, CONST, LSQUARE)));
		FIRST.set(ASTNodeNames.UnaryExpr.ordinal(), new HashSet<>(
				Arrays.asList(BANG, MINUS, RES_width, RES_height, STRING_LIT, NUM_LIT, BOOLEAN_LIT, IDENT, LPAREN, CONST, LSQUARE)));
		FIRST.set(ASTNodeNames.PostfixExpr.ordinal(), new HashSet<>(
				Arrays.asList(STRING_LIT, NUM_LIT, BOOLEAN_LIT, IDENT, LPAREN, CONST, LSQUARE)));
		FIRST.set(ASTNodeNames.PrimaryExpr.ordinal(), new HashSet<>(
				Arrays.asList(STRING_LIT, NUM_LIT, BOOLEAN_LIT, IDENT, LPAREN, CONST, LSQUARE)));
		FIRST.set(ASTNodeNames.ChannelSelector.ordinal(), new HashSet<>(
				Arrays.asList(COLON)));
		FIRST.set(ASTNodeNames.PixelSelector.ordinal(), new HashSet<>(
				Arrays.asList( LSQUARE)));
		FIRST.set(ASTNodeNames.ExpandedPixelExpr.ordinal(), new HashSet<>(
				Arrays.asList(LSQUARE)));
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
	public AST parse() throws SyntaxException, LexicalException {
        return expr();
	}

	private Expr expr() throws SyntaxException, LexicalException {
//		IToken firstToken = t;

		if (in_first(ASTNodeNames.ConditionalExpr))
		{
			return conditionalExpr();
		}
		else if (in_first(ASTNodeNames.LogicalOrExpr))
		{
			return logicalOrExpr();
		}

		throw new SyntaxException();
	}

	private Expr conditionalExpr() throws SyntaxException, LexicalException {
		IToken firstToken = t;

		match(QUESTION);
		Expr expr1 = expr();
		match(RARROW);
		Expr expr2 = expr();
		match(COMMA);
		Expr expr3 = expr();

		return new ConditionalExpr(firstToken, expr1, expr2, expr3);
	}

	private Expr logicalOrExpr() throws SyntaxException, LexicalException {
		IToken firstToken = t;

		Expr expr1 = logicalAndExpr();

		while (in(BITOR, OR))
		{
			IToken orr = match(BITOR, OR);

			Expr expr2 = logicalAndExpr();

			expr1 = new BinaryExpr(firstToken, expr1, orr, expr2);
		}

		return expr1;
	}

	private Expr logicalAndExpr() throws SyntaxException, LexicalException {
		IToken firstToken = t;

		Expr expr1 = comparisonExpr();

		while (in(BITAND, AND))
		{
			IToken andd = match(BITAND, AND);

			Expr expr2 = comparisonExpr();

			expr1 = new BinaryExpr(firstToken, expr1, andd, expr2);
		}

		return expr1;
	}

	private Expr comparisonExpr() throws SyntaxException, LexicalException {
		IToken firstToken = t;

		Expr expr1 = powExpr();

		while (in(LT, LE, GT, GE, EQ))
		{
			IToken cmp = match(LT, LE, GT, GE, EQ);

			Expr expr2 = powExpr();

			expr1 = new BinaryExpr(firstToken, expr1, cmp, expr2);
		}

		return expr1;
	}

	private Expr powExpr() throws SyntaxException, LexicalException {
		IToken firstToken = t;

		Expr expr1 = additiveExpr();

		if (in(EXP))
		{
			IToken exponent = match(EXP);

			Expr expr2 = powExpr();

			return new BinaryExpr(firstToken, expr1, exponent, expr2);
		}

		return expr1;
	}

	private Expr additiveExpr() throws SyntaxException, LexicalException {
		IToken firstToken = t;

		Expr expr1 = multiplicativeExpr();

		while (in(PLUS, MINUS))
		{
			IToken adder = match(PLUS, MINUS);

			Expr expr2 = multiplicativeExpr();

			expr1 = new BinaryExpr(firstToken, expr1, adder, expr2);
		}

		return expr1;
	}

	private Expr multiplicativeExpr() throws SyntaxException, LexicalException {
		IToken firstToken = t;

		Expr expr1 = unaryExpr();

		while (in(TIMES, DIV, MOD))
		{
			IToken mult = match(TIMES, DIV, MOD);

			Expr expr2 = unaryExpr();

			expr1 = new BinaryExpr(firstToken, expr1, mult, expr2);
		}

		return expr1;
	}

	private Expr unaryExpr() throws SyntaxException, LexicalException {
		IToken firstToken = t;

		if (in(BANG, MINUS, RES_width, RES_height))
		{
			IToken tk = match(BANG, MINUS, RES_width, RES_height);

			Expr expr = unaryExpr();

			return new UnaryExpr(firstToken, tk, expr);
		}
		else if (in_first(ASTNodeNames.PostfixExpr))
		{
			return postfixExpr();
		}

		throw new SyntaxException();
	}

	private Expr postfixExpr() throws SyntaxException, LexicalException {
		IToken firstToken = t;

		Expr expr1 = primaryExpr();
		PixelSelector expr2 = null;
		ChannelSelector expr3 = null;

		if (in_first(ASTNodeNames.PixelSelector))
		{
			expr2 = pixelSelector();
		}

		if (in_first(ASTNodeNames.ChannelSelector))
		{
			expr3 = channelSelector();
		}

		if (expr2 == null && expr3 == null)
			return expr1;

		return new PostfixExpr(t, expr1, expr2, expr3);
	}

	private Expr primaryExpr() throws SyntaxException, LexicalException {
		IToken firstToken = t;

		if (in(STRING_LIT, NUM_LIT, BOOLEAN_LIT, IDENT, CONST))
		{
			IToken tok = match(STRING_LIT, NUM_LIT, BOOLEAN_LIT, IDENT, CONST);

			return switch (tok.kind())
			{
				case IDENT -> new IdentExpr(firstToken);
				case NUM_LIT -> new NumLitExpr(firstToken);
				case STRING_LIT -> new StringLitExpr(firstToken);
				case CONST -> new ConstExpr(firstToken);
				case BOOLEAN_LIT -> new BooleanLitExpr(firstToken);
				default -> null;
			};
		}
		else if (in(LPAREN))
		{
			match(LPAREN);
			Expr expr1 = expr();
			match(RPAREN);

			return expr1;
		}
		else
		{
            return expandedPixelExpr();
		}
	}
	private ChannelSelector channelSelector() throws SyntaxException, LexicalException {
		IToken firstToken = t;

		match(COLON);
		IToken second = match(RES_red, RES_green, RES_blue);

		return new ChannelSelector(firstToken, second);
	}
	private PixelSelector pixelSelector() throws SyntaxException, LexicalException {
		IToken firstToken = t;

		match(LSQUARE);

		Expr expr1 = expr();

		match(COMMA);

		Expr expr2 = expr();

		match(RSQUARE);

		return new PixelSelector(firstToken, expr1, expr2);
	}

	private Expr expandedPixelExpr() throws SyntaxException, LexicalException {
		IToken firstToken = t;

		match(LSQUARE);

		Expr expr1 = expr();

		match(COMMA);

		Expr expr2 = expr();

		match(COMMA);

		Expr expr3 = expr();

		match(RSQUARE);

		return new ExpandedPixelExpr(firstToken, expr1, expr2, expr3);
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

	private boolean in_first(ASTNodeNames name)
	{
		// For now, the predict sets are identically equal to the first sets, thus this will act accordingly
		return FIRST.get(name.ordinal()).contains(t.kind());
	}

	private boolean in_follow(ASTNodeNames name, IToken token)
	{
		return FOLLOW.get(name.ordinal()).contains(token.kind());
	}
}
