/**
 * Token
 * Represents a single item in a WHERE clause
 * @author Daniel Tregea
 */

package DMLParser.TokenParser;

public class Token {
    public TokenType type;
    public String value;

    public enum TokenType {
        NUMBER,
        STRING,
        BOOLEAN,
        IDENTIFIER,
        GT,
        GTE,
        LT,
        LTE,
        AND,
        OR,
        LPAREN,
        RPAREN,
        EQUALS,
        NOTEQUALS,
        ADD,
        SUBTRACT,
        POW,
        MULTIPLY,
        DIVIDE
    }
    public Token(TokenType type, String value) {
        this.type = type;
        this.value = value;
    }

    @Override
    public String toString() {
        return type + " " + value;
    }
}