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

import static edu.ufl.cise.cop4020fa23.Kind.EOF;

import edu.ufl.cise.cop4020fa23.exceptions.LexicalException;

import java.util.*;


public class Lexer implements ILexer {

	String input;

	int row = 1, col = 0, pos = 0, colCounter = 0;

    String s;

	enum State
	{
		START,
        FINISH,
        IDENT,
        NUM_LIT,
        NUM_LIT_0,
        STR_LIT_1,
        STR_LIT_2,
        OP_SEP,
        LEFT_BRACKET,
        EQUALS,
        LT,
        GT,
        AMP,
        BAR,
        STAR,
        HYPHEN,
        COLON,
        HASH,
        COMMENT,

        UNEXPECTED,
	}

    final static private Kind[] state_to_kind = new Kind[] {
        Kind.ERROR, /* START */
        Kind.ERROR, /* FINISH */
        Kind.IDENT, /* IDENT */
        Kind.NUM_LIT, /* NUM_LIT */
        Kind.NUM_LIT, /* NUM_LIT_0 */
        Kind.STRING_LIT, /* STR_LIT_1 */
        Kind.STRING_LIT, /* STR_LIT_2 */
        Kind.ERROR, /* OP_SEP */
        Kind.LSQUARE, /* LEFT_BRACKET */
        Kind.ASSIGN, /* EQUALS */
        Kind.LT, /* LT */
        Kind.GT, /* GT */
        Kind.BITAND, /* AMP */
        Kind.BITOR, /* BAR */
        Kind.TIMES, /* STAR */
        Kind.MINUS, /* HYPHEN */
        Kind.COLON, /* COLON */

        Kind.ERROR, /* UNEXPECTED */
    };


    final static private Map<String, Kind> reserved_words= new HashMap<>();
    static {
        /* Should probably add another enumeration for reserved words
           Kind.ERROR is a placeholder for now or something
         */
        reserved_words.put("TRUE", Kind.BOOLEAN_LIT);
        reserved_words.put("FALSE", Kind.BOOLEAN_LIT);
        reserved_words.put("image", Kind.RES_image);
        reserved_words.put("pixel", Kind.RES_pixel);
        reserved_words.put("int", Kind.RES_int);
        reserved_words.put("string", Kind.RES_string);
        reserved_words.put("void", Kind.RES_void);
        reserved_words.put("boolean", Kind.RES_boolean);
        reserved_words.put("write", Kind.RES_write);
        reserved_words.put("height", Kind.RES_height);
        reserved_words.put("width", Kind.RES_width);
        reserved_words.put("if", Kind.RES_if);
        reserved_words.put("fi", Kind.RES_fi);
        reserved_words.put("do", Kind.RES_do);
        reserved_words.put("od", Kind.RES_od);
        reserved_words.put("red", Kind.RES_red);
        reserved_words.put("green", Kind.RES_green);
        reserved_words.put("blue", Kind.RES_blue);

        // Add more reserved words as needed

    }

    final static private Set<String> constants = new HashSet<>();

    static {
        constants.add("Z");
        constants.add("BLACK");
        constants.add("BLUE");
        constants.add("CYAN");
        constants.add("DARK_GRAY");
        constants.add("GRAY");
        constants.add("GREEN");
        constants.add("LIGHT_GRAY");
        constants.add("MAGENTA");
        constants.add("ORANGE");
        constants.add("PINK");
        constants.add("RED");
        constants.add("WHITE");
        constants.add("YELLOW");
    }

    final static private Map<String, Kind> op_sep_kind = new HashMap<>();

    static {
        op_sep_kind.put(",", Kind.COMMA);
        op_sep_kind.put(";", Kind.SEMI);
        op_sep_kind.put("?", Kind.QUESTION);
        op_sep_kind.put(":", Kind.COLON);
        op_sep_kind.put("(", Kind.LPAREN);
        op_sep_kind.put(")", Kind.RPAREN);
        op_sep_kind.put("<", Kind.LT);
        op_sep_kind.put(">", Kind.GT);
        op_sep_kind.put("[", Kind.LSQUARE);
        op_sep_kind.put("]", Kind.RSQUARE);
        op_sep_kind.put("=", Kind.ASSIGN);
        op_sep_kind.put("==", Kind.EQ);
        op_sep_kind.put("<=", Kind.LE);
        op_sep_kind.put(">=", Kind.GE);
        op_sep_kind.put("!", Kind.BANG);
        op_sep_kind.put("&", Kind.BITAND);
        op_sep_kind.put("&&", Kind.AND);
        op_sep_kind.put("|", Kind.BITOR);
        op_sep_kind.put("||", Kind.OR);
        op_sep_kind.put("+", Kind.PLUS);
        op_sep_kind.put("-", Kind.MINUS);
        op_sep_kind.put("*", Kind.TIMES);
        op_sep_kind.put("**", Kind.EXP);
        op_sep_kind.put("/", Kind.DIV);
        op_sep_kind.put("%", Kind.MOD);
        op_sep_kind.put("<:", Kind.BLOCK_OPEN);
        op_sep_kind.put(":>", Kind.BLOCK_CLOSE);
        op_sep_kind.put("^", Kind.RETURN);
        op_sep_kind.put("->", Kind.RARROW);
        op_sep_kind.put("[]", Kind.BOX);

    }


    static Kind resolve_final_state_name(State state, String s) {
        if (state == State.IDENT)
        {
            if (reserved_words.containsKey(s))
                return reserved_words.get(s);

            if (constants.contains(s))
                return Kind.CONST;
        }

        if (state == State.OP_SEP)
        {
            return op_sep_kind.get(s);
        }

        return state_to_kind[state.ordinal()];
    }


	final static private Character[] op_sep_values = new Character[] {
		',', ';', '?', '(', ')', ']',
		'!', '+', '/', '%', '^'
	};
	final static private Set<Character> op_sep = new HashSet<>(Arrays.asList(op_sep_values));

    final static private Map<Character, State> two_char_ops = new HashMap<>();
    static {
        two_char_ops.put('[', State.LEFT_BRACKET);
        two_char_ops.put('=', State.EQUALS);
        two_char_ops.put('<', State.LT);
        two_char_ops.put('>', State.GT);
        two_char_ops.put('&', State.AMP);
        two_char_ops.put('|', State.BAR);
        two_char_ops.put('*', State.STAR);
        two_char_ops.put('-', State.HYPHEN);
        two_char_ops.put(':', State.COLON);
        two_char_ops.put('#', State.HASH);
    }

    static boolean is_op_sep(char c)
    {
        return op_sep.contains(c);
    }

    static boolean is_two_char_op(char c) { return two_char_ops.containsKey(c); }
    
    static boolean is_printable(char c)
    {
        return 32 <= c && c <= 127;
    }

    static boolean between_inclusive(char c, char l, char u)
    {
        return l <= c && c <= u;
    }


    static boolean is_whitespace(char c) { return c == '\n' || c == '\r' || c == ' '; }
    static boolean is_newline(char c) { return c == '\n' || c == '\r'; }

	interface Transition
	{
		State apply(char c);
	}

	final static private Transition[] transitions = new Transition[]{
		/* START = */ new Transition() {
			public State apply(char c) {
                if (
                    between_inclusive(c, 'a', 'z') ||
                    between_inclusive(c, 'A', 'Z') ||
                    c == '_')
                {
                    return State.IDENT;
                }

                if (c == '0')
                {
                    return State.NUM_LIT_0;
                }

                if (between_inclusive(c, '1', '9'))
                {
                    return State.NUM_LIT;
                }

                if (c == '"')
                {
                    return State.STR_LIT_1;
                }

                if (c == '#')
                {
                    return State.HASH;
                }

                if (is_op_sep(c))
                {
                    return State.OP_SEP;
                }

                if (is_two_char_op(c))
                {
                    return two_char_ops.get(c);
                }
                
                if (is_whitespace(c))
                {
                    return State.START;
                }


                return State.FINISH;
			}
		}, /* FINISH = */ new Transition() {
			public State apply(char c) {
				return State.FINISH;
			}
		}, /* IDENT = */ new Transition() {
			public State apply(char c) {

				if (
                    between_inclusive(c, 'a', 'z') ||
                    between_inclusive(c, 'A', 'Z') ||
                    between_inclusive(c, '0', '9') ||
                    c == '_')
                {
                    return State.IDENT;
                }


                return State.FINISH;
			}
		},/* NUM_LIT = */ new Transition() {
            public State apply(char c) {
                if (between_inclusive(c, '0', '9'))
                {
                    return State.NUM_LIT;
                }

                return State.FINISH;
            }
        },/* NUM_LIT_0 = */ new Transition() {
            public State apply(char c) {
                return State.FINISH;
            }
        },/* STR_LIT_1 = */ new Transition() {
			public State apply(char c) {
                if (c == '"')
                {
                    return State.STR_LIT_2;
                }

                if (is_printable(c))
                {
                    return State.STR_LIT_1;
                }

                return State.UNEXPECTED;
			}
		},/* STR_LIT_2 = */ new Transition() {
            public State apply(char c) {
                return State.FINISH;
            }
        },/* OP_SEP = */ new Transition() {
            public State apply(char c) {
                return State.FINISH;
            }
        },/* LEFT_BRACKET = */ new Transition() {
            public State apply(char c) {
                if (c == ']')
                    return State.OP_SEP;
                return State.FINISH;
            }
        },/* EQUALS = */ new Transition() {
            public State apply(char c) {
                if (c == '=')
                    return State.OP_SEP;
                return State.FINISH;
            }
        },/* LT = */ new Transition() {
            public State apply(char c) {
                if (c == '=' || c == ':')
                    return State.OP_SEP;
                return State.FINISH;
            }
        },/* GT = */ new Transition() {
            public State apply(char c) {
                if (c == '=')
                    return State.OP_SEP;
                return State.FINISH;
            }
        },/* AMP = */ new Transition() {
            public State apply(char c) {
                if (c == '&')
                    return State.OP_SEP;
                return State.FINISH;
            }
        },/* BAR = */ new Transition() {
            public State apply(char c) {
                if (c == '|')
                    return State.OP_SEP;
                return State.FINISH;
            }
        },/* STAR = */ new Transition() {
            public State apply(char c) {
                if (c == '*')
                    return State.OP_SEP;
                return State.FINISH;
            }
        },/* HYPHEN = */ new Transition() {
            public State apply(char c) {
                if (c == '>')
                    return State.OP_SEP;
                return State.FINISH;
            }
        },/* COLON = */ new Transition() {
            public State apply(char c) {
                if (c == '>')
                    return State.OP_SEP;
                return State.FINISH;
            }
        },/* HASH = */ new Transition() {
            public State apply(char c) {
                if (c == '#')
                    return State.COMMENT;
                return State.UNEXPECTED;
            }
        },/* COMMENT = */ new Transition() {
            public State apply(char c) {
                if (is_newline(c))
                    return State.START;
                return State.COMMENT;
            }
        }
	};



	public Lexer(String input) {
		this.input = input;
	}

	@Override
	public IToken next() throws LexicalException {

        /**
         * Notes:
         * Test 11: Should throw lexical exception because number is too large (all 9s)
         * Test 12: Haven't implemented ops and seps
         * Test 13: Haven't implemented EOF token
         * Test 14: Could be because special characters are not implemented yet
         * Test 15: I might have messed up the transition function for identifiers - yes (but now it's because haven't implemented ops and seps)
         * Test 16: Special characters
         */
        if (pos == input.length())
            return new Token(EOF, 0, 0, null, new SourceLocation(row, col));

		State cur = State.START, last = State.START;
		StringBuilder text = new StringBuilder();
        boolean last_whitespace = false;

        //col++;

		while (cur != State.FINISH && pos < input.length())
		{

            char c = input.charAt(pos++);

			//++colCounter;

			if (is_newline(c))
			{
				++row;
				colCounter = 0;
			}

            last = cur;
            cur = transitions[cur.ordinal()].apply(c);

            if (is_whitespace(c) || cur == State.HASH || cur == State.COMMENT)
            {
                if (cur != State.UNEXPECTED) {
                    last_whitespace = true;
                    col = colCounter;
                }
                else {
                    last_whitespace = false;
                    throw new LexicalException();
                }
            }
            else
            {
                if (!is_whitespace(c) && last_whitespace)
                    last_whitespace = false;
                text.append(c);
            }

            ++colCounter;

//            if (is_whitespace(c))
//            {
//                last_whitespace = true;
//
//                if (cur == State.START)
//                    continue;
//
//                last = cur;
//                cur = State.FINISH;
//            }
//            else
//            {
//                text.append(c);
//                last = cur;
//
//                cur = transitions[cur.ordinal()].apply(c);
//            }
		}


        if (!last_whitespace)
        {
            if (text.length() > 1) {
                text.deleteCharAt(text.length() - 1);
                --pos;
                --colCounter;
            }
        }

        s = text.toString();

        try {
            if (!s.isEmpty()) {
                boolean isNumeric = s.chars().allMatch(Character::isDigit);
                if (isNumeric) {
                    Integer.parseInt(s);
                }
            }
        }
        catch (Exception e) {
            throw new LexicalException();
        }

        System.out.println(text + " " + row + " " + col);

        if (s.isEmpty())
            return new Token(EOF, 0, 0, null, new SourceLocation(row, col));

        if (last == State.START) {
            return new Token(resolve_final_state_name(cur, s), 0, s.length(), s.toCharArray(), new SourceLocation(row, col));
        }

        return new Token(resolve_final_state_name(last, s), 0, s.length(), s.toCharArray(), new SourceLocation(row, col));
	}


}
