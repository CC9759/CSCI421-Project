/**
 * Parse Utils
 * Contains utility methods for parsing
 * @author Daniel Tregea
 */
package DMLParser.TokenParser;

import Exceptions.SyntaxErrorException;
import java.util.ArrayList;

public class ParseUtils {
    public static void ensureToken(ArrayList<Token> tokens) throws SyntaxErrorException {
        if (tokens.isEmpty()) {
            throw new SyntaxErrorException("Expected Token");
        }
    }
}
