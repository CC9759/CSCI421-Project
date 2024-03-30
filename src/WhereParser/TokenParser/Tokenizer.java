/**
 * Tokenizer
 * Contains methods to tokenize a WHERE clause and output it in prefix notation
 *
 * @author Daniel Tregea
 */

package WhereParser.TokenParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

public class Tokenizer {

    public static ArrayList<Token> tokenize(String input) {
        List<Token> tokens = new ArrayList<>();
        StringBuilder token = new StringBuilder();
        boolean isString = false;
        int i = 0;
        while (i < input.length()) {
            char c = input.charAt(i);
            if (Character.isWhitespace(c)) {
                i++;
                continue;
            }
            switch (c) {
                case '+':
                    tokens.add(new Token(Token.TokenType.ADD, "+"));
                    break;
                case '-':
                    tokens.add(new Token(Token.TokenType.SUBTRACT, "-"));
                    break;
                case '*':
                    tokens.add(new Token(Token.TokenType.MULTIPLY, "*"));
                    break;
                case '/':
                    tokens.add(new Token(Token.TokenType.DIVIDE, "/"));
                    break;
                case '^':
                    tokens.add(new Token(Token.TokenType.POW, "^"));
                    break;
                case '=':
                    tokens.add(new Token(Token.TokenType.EQUALS, "="));
                    break;
                case '!':
                    if (i + 1 < input.length() && input.charAt(i + 1) == '=') {
                        tokens.add(new Token(Token.TokenType.NOTEQUALS, "!="));
                        i++; // Skip =
                    } else {
                        return null;
                    }
                    break;
                case '>':
                    if (i + 1 < input.length() && input.charAt(i + 1) == '=') {
                        tokens.add(new Token(Token.TokenType.GTE, ">="));
                        i++; // Skip =
                    } else {
                        tokens.add(new Token(Token.TokenType.GT, ">"));
                    }
                    break;
                case '<':
                    if (i + 1 < input.length() && input.charAt(i + 1) == '=') {
                        tokens.add(new Token(Token.TokenType.LTE, "<="));
                        i++; // Skip =
                    } else {
                        tokens.add(new Token(Token.TokenType.LT, "<"));
                    }
                    break;
                case '(':
                    tokens.add(new Token(Token.TokenType.LPAREN, "("));
                    break;
                case ')':
                    tokens.add(new Token(Token.TokenType.RPAREN, ")"));
                    break;
                default:
                    token.setLength(0); // Reset token builder
                    if (Character.isDigit(c)) {
                        while (i < input.length() && (Character.isDigit(input.charAt(i)) || input.charAt(i) == '.')) {
                            token.append(input.charAt(i));
                            i++;
                        }
                        tokens.add(new Token(Token.TokenType.NUMBER, token.toString()));
                        i--;
                    } else if (Character.isLetter(c) || isQuote(c)) {
                        isString = isQuote(input.charAt(i));
                        if (isString) { // skip quote
                            i++;
                        }
                        while (i < input.length() &&
                                (
                                        Character.isLetter(input.charAt(i)) ||
                                                (isString && !isQuote(input.charAt(i))) ||
                                                input.charAt(i) == '.'
                                )
                        ) {
                            token.append(input.charAt(i));
                            i++;
                        }
                        if (isString) { // skip quote
                            i++;
                        }

                        String tokenStr = token.toString();

                        if (tokenStr.equalsIgnoreCase("AND")) {
                            tokens.add(new Token(Token.TokenType.AND, tokenStr));
                        } else if (tokenStr.equalsIgnoreCase("OR")) {
                            tokens.add(new Token(Token.TokenType.OR, tokenStr));
                        } else if (tokenStr.equalsIgnoreCase("TRUE") || tokenStr.equalsIgnoreCase("FALSE")) {
                            tokens.add(new Token(Token.TokenType.BOOLEAN, tokenStr));
                        } else if (isString) {
                            tokens.add(new Token(Token.TokenType.STRING, tokenStr));
                            isString = false;
                        } else {
                            tokens.add(new Token(Token.TokenType.IDENTIFIER, tokenStr));
                        }

                    } else {
                        throw new IllegalArgumentException("Unexpected character: " + c);
                    }
                    break;

            }
            i++;
        }

        return convertToPrefix(tokens);
    }

    private static boolean isQuote(char ch) {
        return ch == '"' || ch == '\'';
    }

    // Get precedence of operators
    private static int getPrecedence(Token.TokenType tokenType) {
        switch (tokenType) {
            case OR: // OR is greater than AND
                return 1;
            case AND:
                return 2;
            case EQUALS:
            case NOTEQUALS:
            case LT:
            case LTE:
            case GT:
            case GTE:
                return 3;
            case ADD:
            case SUBTRACT:
                return 4;
            case MULTIPLY:
            case DIVIDE:
            case POW:
                return 5;
            default:
                return -1;
        }
    }

    // Convert to prefix notation
    // Built with reference to: https://mathcenter.oxford.emory.edu/site/cs171/shuntingYardAlgorithm/
    private static ArrayList<Token> convertToPrefix(List<Token> tokens) {
        // Shunting yard algorithm converts to postfix, we will do it in reverse to get prefix
        Collections.reverse(tokens);
        ArrayList<Token> result = new ArrayList<>();
        Stack<Token> operators = new Stack<>();

        for (Token token : tokens) {
            switch (token.type) {
                case STRING:
                case NUMBER:
                case IDENTIFIER:
                case BOOLEAN:
                    result.add(token);
                    break;
                case GT:
                case GTE:
                case LT:
                case LTE:
                case AND:
                case OR:
                case NOTEQUALS:
                case EQUALS:
                case ADD:
                case SUBTRACT:
                case POW:
                case MULTIPLY:
                case DIVIDE:
                    while (!operators.isEmpty() && getPrecedence(token.type) <= getPrecedence(operators.peek().type)) {
                        result.add(operators.pop());
                    }
                    operators.push(token);
                    break;
                case RPAREN: //push on right parenthesis since we are doing this in reverse
                    operators.push(token);
                    break;
                case LPAREN:
                    while (!operators.isEmpty() && operators.peek().type != Token.TokenType.RPAREN) {
                        result.add(operators.pop());
                    }
                    if (!operators.isEmpty() && operators.peek().type == Token.TokenType.RPAREN) {
                        operators.pop();
                    }
                    break;
            }
        }

        while (!operators.isEmpty()) {
            result.add(operators.pop());
        }

        Collections.reverse(result);
        return result;
    }
}
