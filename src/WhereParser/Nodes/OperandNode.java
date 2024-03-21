/**
 * String Node
 * Represents a base item in a where-clause tree
 * @author Daniel Tregea
 */
package WhereParser.Nodes;

import WhereParser.TokenParser.ParseUtils;
import WhereParser.TokenParser.Token;
import Exceptions.IllegalOperationException;
import Exceptions.SyntaxErrorException;
import storageManager.Record;

import java.util.ArrayList;

public abstract class OperandNode {

    public static OperandNode parse(ArrayList<Token> tokens) throws SyntaxErrorException {
        ParseUtils.ensureToken(tokens);
        Token topToken = tokens.get(0);
        OperandNode node = null;
        switch (topToken.type) {
            case POW:
            case ADD:
            case SUBTRACT:
            case MULTIPLY:
            case DIVIDE:
                node = MathOpNode.parse(tokens);
                break;
            case NUMBER:
                node = NumberNode.parse(tokens);
                break;
            case STRING:
                node = StringNode.parse(tokens);
                break;
            case BOOLEAN:
                node = BooleanNode.parse(tokens);
                break;
            case IDENTIFIER:
                node = IDNode.parse(tokens);
                break;
            default:
                throw new SyntaxErrorException("Expected Operand, received: " + topToken.type);
        }
        return node;
    }

    public abstract Object evaluate(Record record);

    public abstract int compare(Record record, OperandNode o) throws IllegalOperationException;
}
