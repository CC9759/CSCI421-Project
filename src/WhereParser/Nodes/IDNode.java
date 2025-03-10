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
import catalog.AttributeSchema;
import storageManager.Record;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    private Object getRecordValue(Record record) throws IllegalOperationException {
        try {
            List<AttributeSchema> matchingColumns = record.getAttributes().stream()
                    .filter(attr -> attr.getAttributeName().endsWith(id.value))
                    .collect(Collectors.toList());

            if (matchingColumns.isEmpty()) {
                throw new IllegalOperationException("Where column " + id.value + " not found");
            } else if (matchingColumns.size() != 1) {
                throw new IllegalOperationException("Where column " + id.value + " is ambiguous");
            }
            String matchingColumnName = matchingColumns.get(0).getAttributeName();
            return record.getAttribute(matchingColumnName).getData();
        } catch (NullPointerException e) {
            throw new IllegalOperationException("Property " + id.value + " does not exist");
        }
    }

    @Override
    public Object evaluate(Record record) throws IllegalOperationException {
        return getRecordValue(record);
    }

    @Override
    public int compare(Record record, OperandNode o) throws IllegalOperationException {
        Object data = getRecordValue(record);
        if (data instanceof Integer || data instanceof Double) {
            return MathOpNode.compareNumber(this, o, record);
        }
        else if (data instanceof Character || data instanceof String) {
            return ((String) data).compareTo((String) o.evaluate(record));
        } else {
            return ((Boolean) data).compareTo((Boolean) o.evaluate(record));
        }
    }
}
