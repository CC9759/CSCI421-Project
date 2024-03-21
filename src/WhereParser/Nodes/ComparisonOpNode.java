/**
 * Comparison Node
 * Represents comparison operations in a where-clause tree
 * @author Daniel Tregea
 */
package WhereParser.Nodes;

import WhereParser.TokenParser.ParseUtils;
import WhereParser.TokenParser.Token;
import Exceptions.IllegalOperationException;
import Exceptions.SyntaxErrorException;
import storageManager.Record;

import java.util.ArrayList;
import java.util.Arrays;

public class ComparisonOpNode implements BoolOpNode {
    public Token comparison;
    public OperandNode leftOp, rightOp;

    public ComparisonOpNode(Token comparison, OperandNode leftOp, OperandNode rightOp) {
        this.comparison = comparison;
        this.leftOp = leftOp;
        this.rightOp = rightOp;
    }

    public static ComparisonOpNode parse(ArrayList<Token> tokens) throws SyntaxErrorException {
        ParseUtils.ensureToken(tokens);
        Token comparison = tokens.get(0);
        if (!Arrays.asList(">", ">=", "<", "<=", "=", "!=").contains(comparison.value)) {
            throw new SyntaxErrorException("Expected comparison, received: " + comparison.value);
        }
        tokens.remove(0);

        OperandNode leftOp = OperandNode.parse(tokens);
        OperandNode rightOp = OperandNode.parse(tokens);

        return new ComparisonOpNode(comparison, leftOp, rightOp);
    }

    @Override
    public boolean evaluate(Record record) throws IllegalOperationException {
        // compare on operand needs to be like compareTo
        switch (comparison.type) {
            case GT:
                return leftOp.compare(record, rightOp) > 0;
            case GTE:
                return leftOp.compare(record, rightOp) >= 0;
            case LT:
                return leftOp.compare(record, rightOp) < 0;
            case LTE:
                return leftOp.compare(record, rightOp) <= 0;
            case EQUALS:
                return leftOp.compare(record, rightOp) == 0;
            case NOTEQUALS:
                return leftOp.compare(record, rightOp) != 0;
        }
        return false;
    }
}
