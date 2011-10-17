
package hu.javasourcestat;

/**
 * The type of the tokens.
 * @author akarnokd, 2008.03.17.
 * @version $Revision 1.0$
 */
public enum TokenType {
	/** Whitespace. */
	WHITESPACE,
	/** Comment (//). */
	COMMENT,
	/** Multiline comment (/*). */
	MULTILINE_COMMENT,
	/** Javadoc comment. (/**) */
	JAVADOC,
	/** Identifier. */
	IDENTIFIER,
	/** Lower than (&lt;). */
	LT,
	/** Greater than (&gt;). */
	GT,
	/** Lower or equal (&lt;=). */
	LE,
	/** Greater or equal (&gt;=). */
	GE,
	/** Equal (==). */
	EQ,
	/** Assignment (=). */
	ASSIGNMENT,
	/** For keyword. */
	FOR,
	/** While keyword. */
	WHILE,
	/** Do keyword. */
	DO,
	/** If keyword. */
	IF,
	/** Dot (.). */
	DOT,
	/** Comma (,). */
	COMMA,
	/** Colon (:). */
	COLON,
	/** Semicolon (;). */
	SEMICOLON,
	/** Block start ({). */
	BLOCK_START,
	/** Block end (}). */
	BLOCK_END,
	/** At (@). */
	AT,
	/** Round Bracket start ((). */
	ROUND_BRACKET_START,
	/** Round Bracket end ((). */
	ROUND_BRACKET_END,
	/** Bracket start ([). */
	BRACKET_START,
	/** Bracket end (]). */
	BRACKET_END,
	/** Character literal. */
	CHARACTER_VALUE,
	/** String literal. */
	STRING_VALUE,
	/** Integer value. */
	INTEGER_VALUE,
	/** Long value. */
	LONG_VALUE,
	/** Floating point value. */
	FLOAT_VALUE,
	/** Double value. */
	DOUBLE_VALUE,
	/** Plus (+). */
	ADD,
	/** Minus (-). */
	SUBSTRACT,
	/** Multiply (*). */
	MULTIPLY,
	/** Divide (/). */
	DIVIDE,
	/** Modulo (%). */
	MODULO,
	/** Not (!). */
	EXCLAMATION,
	/** Boolean and (&amp;). */
	BOOL_AND,
	/** Boolean or (|). */
	BOOL_OR,
	/** Logical and (&amp;&amp;). */
	LOGICAL_AND,
	/** Logical or (||). */
	LOGICAL_OR,
	/** Increment (++). */
	INCREMENT,
	/** Decrement (--). */
	DECREMENT,
	/** Shift left (&lt;&lt;). */
	SHIFT_LEFT,
	/** Shift right (&gt;&gt;). */
	SHIFT_RIGHT,
	/** Sign shift right (&gt;&gt;&gt;). */
	SIGN_SHIFT_RIGHT,
	/** Plus assign (+=). */
	PLUS_ASSIGN,
	/** Minus assign (-=). */
	MINUS_ASSIGN,
	/** Multiply assign (*=). */
	MUL_ASSIGN,
	/** Divide assign (/=). */
	DIV_ASSIGN,
	/** Mod assign (%=). */
	MOD_ASSIGN,
	/** Shift left assign (&lt;&lt=). */
	SHIFT_LEFT_ASSIGN,
	/** Shift right assign (&gt;&gt;=). */
	SHIFT_RIGHT_ASSIGN,
	/** Keyword. */
	PACKAGE,
	/** Keyword. */
	IMPORT,
	/** Keyword. */
	STATIC,
	/** Keyword. */
	PUBLIC,
	/** Keyword. */
	PROTECTED,
	/** Keyword. */
	PRIVATE,
	/** Keyword. */
	CLASS,
	/** Keyword. */
	INTERFACE,
	/** Keyword. */
	ENUM,
	/** Keyword. */
	FINAL,
	/** Keyword. */
	ABSTRACT,
	/** Keyword. */
	EXTENDS,
	/** Keyword. */
	IMPLEMENTS,
	/** Keyword. */
	TRANSIENT,
	/** Keyword. */
	VOLATILE,
	/** Keyword. */
	VOID,
	/** Keyword. */
	RETURN,
	/** Keyword. */
	ASSERT,
	/** Keyword. */
	ELSE,
	/** Keyword. */
	NEW,
	/** Keyword. */
	BYTE,
	/** Keyword. */
	SHORT,
	/** Keyword. */
	INT,
	/** Keyword. */
	LONG,
	/** Keyword. */
	CHAR,
	/** Keyword. */
	FLOAT,
	/** Keyword. */
	DOUBLE,
	/** Keyword. */
	THROWS,
	/** Keyword. */
	THROW,
	/** Keyword. */
	NULL,
	/** Keyword. */
	TRY,
	/** Keyword. */
	FINALLY,
	/** Keyword. */
	CATCH,
	/** Keyword. */
	TRUE,
	/** Keyword. */
	FALSE,
}
