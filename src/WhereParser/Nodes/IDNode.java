/**
 * IDNode
 * Represents record variables in a where-clause tree
 * @author Daniel Tregea
 */
package WhereParser.Nodes;

import WhereParser.TokenParser.ParseUtils;
import WhereParser.TokenParser.Token;
import Exceptions.IllegalOperationException;
import Exceptions.SyntaxErrorException;
import storageManager.Record;

import java.util.ArrayList;

public class IDNode extends OperandNode {

    public Token id;

    public IDNode(Token id) {
        this.id = id;
    }

    public static IDNode parse(ArrayList<Token> tokens) throws SyntaxErrorException {
        ParseUtils.ensureToken(tokens);
        Token id = tokens.get(0);
        if (id.type != Token.TokenType.IDENTIFIER) {
            throw new SyntaxErrorException("Expected id, got: " + id.value);
        }
        tokens.remove(0);
        return new IDNode(id);
    }

    @Override
    public Object evaluate(Record record) {
        return record.getAttribute(id.value).getData();
    }

    @Override
    public int compare(Record record, OperandNode o) throws IllegalOperationException {
        Object data = record.getAttribute(id.value).getData();
        // check for cast exceptions in the future
        if (data instanceof Integer || data instanceof Double) {
            return MathOpNode.compareNumber(this, o, record);
        }
//        else if(data instanceof Double) {
//            return Double.compare((Double) data, (Double) o.evaluate(record));
//        }
        else if (data instanceof Character || data instanceof String) {
            return ((String) data).compareTo((String) o.evaluate(record));
        } else {
            return ((Boolean) data).compareTo((Boolean) o.evaluate(record));
        }
    }
}
