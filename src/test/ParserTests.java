/**
 * ParserTest
 * Unit tests for Parser
 * @author Daniel Tregea
 */
package test;

import WhereParser.Nodes.BoolOpNode;
import WhereParser.TokenParser.Parser;
import Exceptions.IllegalOperationException;
import Exceptions.SyntaxErrorException;
import catalog.AttributeSchema;
import catalog.AttributeType;
import storageManager.Attribute;
import storageManager.Record;

import java.util.ArrayList;
import java.util.Arrays;

public class ParserTests {

    public static void main(String[] args) {
        try {
            AttributeType idType = new AttributeType("integer");
            AttributeType nameType = new AttributeType(AttributeType.TYPE.VARCHAR, 32);
            AttributeType salaryType = new AttributeType("double");
            AttributeSchema idSchema = new AttributeSchema("id", idType, 0, true, true, false);
            AttributeSchema nameSchema = new AttributeSchema("name", nameType, 1, false, false, true);
            AttributeSchema salarySchema = new AttributeSchema("salary", salaryType, 1, false, false, true);
            Attribute id = new Attribute(idSchema, 1);
            Attribute name = new Attribute(nameSchema, "dot");
            Attribute salary = new Attribute(salarySchema, 2.0);
            ArrayList<Attribute> attributes = new ArrayList<>(Arrays.asList(id, name, salary));
            Record testRecord = new Record(attributes);


            testInput("name = \"dot\" and id = 1", testRecord, true);
            testInput("id < (500 * 500) and id >= 1 + 0", testRecord, true);
            testInput("id < (500 * 500) and id > 1 + 0", testRecord, false);
            testInput("id = 1 and salary > (1.4 ^ 2)", testRecord, true);
            testInput("id = 0 and salary > (1 / 0.0) or id = 1", testRecord, false);
            testInput("dot = \'hi\'", testRecord, false);



        }catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private static void testInput(String input, Record testRecord, boolean result) throws IllegalOperationException, SyntaxErrorException {
        System.out.println("Testing: " + input + " should " + (result ? "PASS" : "FAIL"));
        BoolOpNode head = Parser.parseWhere(input);
        try {
            boolean pass = head.evaluate(testRecord);
            System.out.println(pass == result ? "Pass" : "Fail");
            if (pass != result) {
                System.exit(1);
            }
        } catch (Exception e) {
            boolean pass = false;
            System.out.println(pass == result ? "Pass" : "Fail");
            if (pass != result) {
                System.exit(1);
            }
        }

    }
}
