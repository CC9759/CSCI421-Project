/**
 * Math-op Node
 * Represents a math expression in a where-clause tree
 * @author Daniel Tregea
 */
package WhereParser.Nodes;

import WhereParser.TokenParser.ParseUtils;
import WhereParser.TokenParser.Token;
import Exceptions.IllegalOperationException;
import Exceptions.SyntaxErrorException;
import storageManager.Record;

import java.util.ArrayList;

public class MathOpNode extends OperandNode {

    private Token operator;
    private NumberNode num1, num2;

    public MathOpNode(Token operator, NumberNode num1, NumberNode num2) {
        this.operator = operator;
        this.num1 = num1;
        this.num2 = num2;
    }

    public static MathOpNode parse(ArrayList<Token> tokens) throws SyntaxErrorException {
        ParseUtils.ensureToken(tokens);
        Token operator = tokens.remove(0); // math op is verified from OperandNode.parse

        NumberNode num1 = NumberNode.parse(tokens);
        NumberNode num2 = NumberNode.parse(tokens);
        return new MathOpNode(operator, num1, num2);
    }

    @Override
    public Object evaluate(Record record) throws IllegalOperationException {
        Object result1 = num1.evaluate(record);
        Object result2 = num2.evaluate(record);

        // Convert integers to double if either of the operands is a double
        boolean isDoubleOperation = result1 instanceof Double || result2 instanceof Double;
        double double1 = isDoubleOperation ? ((Number) result1).doubleValue() : ((Number)0).doubleValue();
        double double2 = isDoubleOperation ? ((Number) result2).doubleValue() : ((Number)0).doubleValue();
        int int1 = !isDoubleOperation ? (Integer) result1 : 0;
        int int2 = !isDoubleOperation ? (Integer) result2 : 0;

        switch (operator.type) {
            case ADD:
                return isDoubleOperation ? double1 + double2 : int1 + int2;
            case SUBTRACT:
                return isDoubleOperation ? double1 - double2 : int1 - int2;
            case MULTIPLY:
                return isDoubleOperation ? double1 * double2 : int1 * int2;
            case DIVIDE:
                if ((isDoubleOperation && double2 == 0)|| (!isDoubleOperation && int2 == 0)) {
                    throw new IllegalOperationException("Cannot divide by 0");
                }
                // Convert to Number if its not clean integer division.
                if (isDoubleOperation || (int1 % int2 != 0)) {
                    return double1 / double2;
                } else {
                    return int1 / int2;
                }
            case POW:
                return Math.pow(((Number) result1).doubleValue(), ((Number) result2).doubleValue());
            default:
                return null;
        }
    }


    @Override
    public int compare(Record record, OperandNode o) throws IllegalOperationException {
//        if (!(o instanceof NumberNode || o instanceof MathOpNode)) {
//            throw new IllegalOperationException("Type Mismatch comparing " + evaluate(record) + " and " + o.evaluate(record));
//        }
        return compareNumber(this, o, record);
    }

    public static int compareNumber(OperandNode o1, OperandNode o2, Record record) throws IllegalOperationException {
        Object thisObj = o1.evaluate(record);
        Object otherObj = o2.evaluate(record);

        Double thisNum;
        Double otherNum;

        if (thisObj instanceof Integer) {
            thisNum = ((Integer) thisObj).doubleValue();
        } else if (thisObj instanceof Double) {
            thisNum = (Double) thisObj;
        } else {
            throw new IllegalArgumentException("Unsupported type: " + thisObj.getClass());
        }

        if (otherObj instanceof Integer) {
            otherNum = ((Integer) otherObj).doubleValue();
        } else if (otherObj instanceof Double) {
            otherNum = (Double) otherObj;
        } else {
            throw new IllegalArgumentException("Unsupported type: " + otherObj.getClass());
        }

        return thisNum.compareTo(otherNum);
    }
}
