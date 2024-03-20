/**
 * Logic-OP Node
 * Represents AND/OR operations in a where-clause tree
 * @author Daniel Tregea
 */
package DMLParser.Nodes;

import DMLParser.TokenParser.ParseUtils;
import DMLParser.TokenParser.Token;
import Exceptions.IllegalOperationException;
import Exceptions.SyntaxErrorException;
import storageManager.Record;
import java.util.ArrayList;

public class LogicOpNode implements BoolOpNode {

    public Token op;
    public BoolOpNode boolOp1, boolOp2;

    public LogicOpNode(Token op, BoolOpNode boolOp1, BoolOpNode boolOp2) {
        this.op = op;
        this.boolOp1 = boolOp1;
        this.boolOp2 = boolOp2;
    }

    public static LogicOpNode parse(ArrayList<Token> tokens) throws SyntaxErrorException {
        ParseUtils.ensureToken(tokens);
        Token op = tokens.remove(0);

        BoolOpNode boolOpNode1 = BoolOpNode.parseBoolNode(tokens);
        BoolOpNode boolOpNode2 = BoolOpNode.parseBoolNode(tokens);
        return new LogicOpNode(op, boolOpNode1, boolOpNode2);
    }

    @Override
    public boolean evaluate(Record record) throws IllegalOperationException {
        boolean res1, res2;
        if (op.type == Token.TokenType.AND) {
            res1 = boolOp1.evaluate(record);
            res2 = boolOp2.evaluate(record);

            return res1 && res2;
        } else if (op.type == Token.TokenType.OR) {
            res1 = boolOp1.evaluate(record);
            res2 = boolOp2.evaluate(record);
            return res1 || res2;
        }
        return false;
    }
}
