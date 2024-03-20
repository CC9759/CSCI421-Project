/**
 * String Node
 * Represents a boolean expression in a where-clause tree
 * @author Daniel Tregea
 */
package DMLParser.Nodes;

import DMLParser.TokenParser.ParseUtils;
import DMLParser.TokenParser.Token;
import Exceptions.IllegalOperationException;
import Exceptions.SyntaxErrorException;
import storageManager.Record;

import java.util.ArrayList;

public interface BoolOpNode {
    public static BoolOpNode parseBoolNode(ArrayList<Token> tokens) throws SyntaxErrorException {
        ParseUtils.ensureToken(tokens);
        Token topToken = tokens.get(0);
        BoolOpNode node= null;
        switch (topToken.type) {
            case AND:
            case OR:
                node = LogicOpNode.parse(tokens);
                break;
            case LT:
            case LTE:
            case GT:
            case GTE:
            case EQUALS:
            case NOTEQUALS:
                node = ComparisonOpNode.parse(tokens);
                break;
            default:
                throw new SyntaxErrorException("Expected Boolean Operation, received: " + topToken.type);
        };
        return node;
    }

    public abstract boolean evaluate(Record record) throws IllegalOperationException;
    // evaluate function
}
