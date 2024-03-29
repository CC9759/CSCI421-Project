/**
 * Number Node
 * Represents an int/double in a where-clause tree
 * @author Daniel Tregea
 */
package WhereParser.Nodes;

import WhereParser.TokenParser.ParseUtils;
import WhereParser.TokenParser.Token;
import Exceptions.IllegalOperationException;
import Exceptions.SyntaxErrorException;
import storageManager.Record;
import java.util.ArrayList;

public class NumberNode extends OperandNode{

    public Token number;

    public NumberNode(Token number) {
        this.number = number;
    }

    public static NumberNode parse(ArrayList<Token> tokens) throws SyntaxErrorException {
        ParseUtils.ensureToken(tokens);
        Token number = tokens.get(0);
        if (number.type != Token.TokenType.NUMBER) {
            throw new SyntaxErrorException("Expected number, got: " + number.value);
        }
        tokens.remove(0);
        return new NumberNode(number);
    }

    public boolean isInt() {
        return !number.value.contains(".");
    }

    @Override
    public int compare(Record record, OperandNode o) throws IllegalOperationException {
        return MathOpNode.compareNumber(this, o, record);
    }

    @Override
    public Object evaluate(Record record) {
        if (!this.isInt()) {
            try {
                return Double.parseDouble(number.value);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid double: " + number.value);
            }
        } else {
            try {
                return Integer.parseInt(number.value);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid integer: " + number.value);
            }
        }
    }
}
