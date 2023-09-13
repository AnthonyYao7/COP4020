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

import java.util.Arrays;
import java.util.HashSet;


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

    static Kind resolve_final_state_name(State state)
    {
        return state_to_kind[state.ordinal()];
    }

	final static private Character[] op_sep_values = new Character[] {
		',', ';', '?', ':', '(', ')', '<', '>', '[', ']',
		'=', '!', '&', '|', '+', '-', '*', '/', '%', '^'
	};
	final static private HashSet<Character> op_sep = new HashSet<>(Arrays.asList(op_sep_values));

    static boolean is_op_sep(char c)
    {
        return op_sep.contains(c);
    }

    static boolean is_printable(char c)
    {
        return 32 <= c && c <= 127;
    }

    static boolean between_inclusive(char c, char l, char u)
    {
        return l <= c && c <= u;
    }

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
        }
	};



	public Lexer(String input) {
		this.input = input;
	}

	@Override
	public IToken next() throws LexicalException {


		State cur = State.START, last = State.START;
		StringBuilder text = new StringBuilder();

		while (cur != State.FINISH && pos < input.length())
		{
			char c = input.charAt(pos++);

			++col;

			if (c == '\n' || c == '\r')
			{
				++row;
				col = 0;
			}



            if (c != ' ' && c != '\n' && c != '\r')
            {
                text.append(c);
                last = cur;

                cur = transitions[cur.ordinal()].apply(c);
            }
		}

        text.deleteCharAt(text.length() - 1);

        --pos;
        --col;

        String s = text.toString();

        return new Token(resolve_final_state_name(last), 0, s.length(), s.toCharArray(), new SourceLocation(row, col));


//		return new Token(EOF, 0, 0, null, new SourceLocation(1, 1));
	}


}
