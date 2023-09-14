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

	int row = 0, col = 0, pos = 0;

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
        

        UNEXPECTED,
	};

    final static private Kind[] state_to_kind = new Kind[] {
        Kind.ERROR, /* START */
        Kind.ERROR, /* FINISH */
        Kind.IDENT, /* IDENT */
        Kind.NUM_LIT, /* NUM_LIT */
        Kind.NUM_LIT, /* NUM_LIT_0 */
        Kind.STRING_LIT, /* STR_LIT_1 */
        Kind.STRING_LIT, /* STR_LIT_2 */

        Kind.ERROR, /* UNEXPECTED */
    };


    static Kind resolve_final_state_name(State state) {return state_to_kind[state.ordinal()];}

    final static private Map<String, Kind> reserved_words= new HashMap<>();
    static {
        /* Should probably add another enumeration for reserved words
           Kind.ERROR is a placeholder for now or something
         */
        reserved_words.put("public", Kind.ERROR);
        reserved_words.put("abstract", Kind.ERROR);
        reserved_words.put("assert", Kind.ERROR);
        reserved_words.put("boolean", Kind.ERROR);
        reserved_words.put("break", Kind.ERROR);
        reserved_words.put("byte", Kind.ERROR);
        reserved_words.put("case", Kind.ERROR);
        reserved_words.put("catch", Kind.ERROR);
        reserved_words.put("char", Kind.ERROR);
        reserved_words.put("class", Kind.ERROR);
        reserved_words.put("const", Kind.ERROR);
        // Add more reserved words as needed

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

                if (is_op_sep(c))
                {
                    return State.OP_SEP;
                }

                if (is_two_char_op(c))
                {
                    return two_char_ops.get(c);
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
         * Test 15: I might have messed up the transition function for identifiers - yes (but now its becasue havent implemented ops and seps)
         * Test 16: Special characters
         */
        if (pos == input.length())
            return new Token(EOF, 0, 0, null, new SourceLocation(row, col));

		State cur = State.START, last = State.START;
		StringBuilder text = new StringBuilder();
        boolean last_whitespace = false;


		while (cur != State.FINISH && pos < input.length())
		{
			char c = input.charAt(pos++);

			++col;

			if (is_newline(c))
			{
				++row;
				col = 0;
			}

            if (is_whitespace(c))
            {
                last_whitespace = true;

                if (cur == State.START)
                    continue;

                last = cur;
                cur = State.FINISH;
            }
            else
            {
                text.append(c);
                last = cur;

                cur = transitions[cur.ordinal()].apply(c);
            }
		}

        if (!last_whitespace)
        {
            text.deleteCharAt(text.length() - 1);

            --pos;
            --col;
        }

        String s = text.toString();

        if (s.isEmpty())
            return new Token(EOF, 0, 0, null, new SourceLocation(row, col));

        return new Token(resolve_final_state_name(last), 0, s.length(), s.toCharArray(), new SourceLocation(row, col));
	}


}
