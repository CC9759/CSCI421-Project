/**
 * String Node
 * Represents a string in a where-clause tree
 * @author Daniel Tregea
 */
package WhereParser.Nodes;

import WhereParser.TokenParser.ParseUtils;
import WhereParser.TokenParser.Token;
import Exceptions.IllegalOperationException;
import Exceptions.SyntaxErrorException;
import storageManager.Record;

import java.util.ArrayList;

public class StringNode extends OperandNode{

    public Token str;

    public StringNode(Token str) {
        this.str = str;
    }

    public static StringNode parse(ArrayList<Token> tokens) throws SyntaxErrorException {
        ParseUtils.ensureToken(tokens);
        Token str = tokens.get(0);
        if (str.type != Token.TokenType.STRING) {
            throw new SyntaxErrorException("Expected string, got: " + str.value);
        }
        tokens.remove(0);
        return new StringNode(str);
    }

    @Override
    public Object evaluate(Record record) {
        return str.value;
    }

    @Override
    public int compare(Record record, OperandNode o) throws IllegalOperationException {
        String thisStr = (String) evaluate(record);
        String otherStr = (String) o.evaluate(record);

        return thisStr.compareTo(otherStr);
    }
}
