/**
 * Parser
 * Contains methods to tokenize and output a Where clause tree
 * @author Daniel Tregea
 */
package DMLParser.TokenParser;
import DMLParser.Nodes.BoolOpNode;
import Exceptions.SyntaxErrorException;

import java.util.ArrayList;

public class Parser {

    /**
     * Parses the where clause EXCLUDING the WHERE.
     * @param clause Where clause excluding WHERE
     * @return BoolOp tree able to evaluate with records
     * @throws SyntaxErrorException fix your syntax errors please
     */
    public static BoolOpNode parseWhere(String clause) throws SyntaxErrorException {
        ArrayList<Token> tokens = Tokenizer.tokenize(clause);
        BoolOpNode head = BoolOpNode.parseBoolNode(tokens);
        return head;
    }
}
