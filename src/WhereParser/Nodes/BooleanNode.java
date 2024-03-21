/**
 * String Node
 * Represents a boolean in a where-clause tree
 * @author Daniel Tregea
 */
package WhereParser.Nodes;

import WhereParser.TokenParser.ParseUtils;
import WhereParser.TokenParser.Token;
import Exceptions.IllegalOperationException;
import Exceptions.SyntaxErrorException;
import storageManager.Record;

import java.util.ArrayList;

public class BooleanNode extends OperandNode{

    public Token bool;

    public BooleanNode(Token bool) {
        this.bool = bool;
    }

    public static BooleanNode parse(ArrayList<Token> tokens) throws SyntaxErrorException {
        ParseUtils.ensureToken(tokens);
        Token bool = tokens.get(0);
        if (bool.type != Token.TokenType.BOOLEAN) {
            throw new SyntaxErrorException("Expected boolean, got: " + bool.value);
        }
        tokens.remove(0);
        return new BooleanNode(bool);
    }

    @Override
    public Object evaluate(Record record) {
        return Boolean.parseBoolean(bool.value);
    }

    @Override
    public int compare(Record record, OperandNode o) throws IllegalOperationException {
//        if(!(o instanceof BooleanNode)) {
//            throw new IllegalOperationException("Type Mismatch comparing " + evaluate(record) + " and " + o.evaluate(record));
//        }
        return evaluate(record).equals(o.evaluate(record)) ? 0 : 1;
    }
}
