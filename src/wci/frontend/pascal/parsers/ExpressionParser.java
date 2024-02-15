package wci.frontend.pascal.parsers;

import wci.frontend.Token;
import wci.frontend.TokenType;
import wci.frontend.pascal.PascalErrorCode;
import wci.frontend.pascal.PascalParserTD;
import wci.frontend.pascal.tokens.PascalTokenType;
import wci.intermediate.ICodeFactory;
import wci.intermediate.ICodeNode;
import wci.intermediate.ICodeNodeType;
import wci.intermediate.SymTabEntry;
import wci.intermediate.icodeimpl.ICodeKeyImpl;
import wci.intermediate.icodeimpl.ICodeNodeTypeImpl;

import java.util.EnumSet;
import java.util.HashMap;

public class ExpressionParser extends StatementParser
{
    // Set of relational operators.
    private static final EnumSet<PascalTokenType> REL_OPS = EnumSet.of(
                    PascalTokenType.EQUALS,
                    PascalTokenType.NOT_EQUALS,
                    PascalTokenType.LESS_THAN,
                    PascalTokenType.LESS_EQUALS,
                    PascalTokenType.GREATER_THAN,
                    PascalTokenType.GREATER_EQUALS);

    private static final HashMap<PascalTokenType, ICodeNodeType> REL_OPS_MAP = new HashMap<PascalTokenType, ICodeNodeType>();
    static
    {
        REL_OPS_MAP.put(PascalTokenType.EQUALS,         ICodeNodeTypeImpl.EQ);
        REL_OPS_MAP.put(PascalTokenType.NOT_EQUALS,     ICodeNodeTypeImpl.NE);
        REL_OPS_MAP.put(PascalTokenType.LESS_THAN,      ICodeNodeTypeImpl.LT);
        REL_OPS_MAP.put(PascalTokenType.LESS_EQUALS,    ICodeNodeTypeImpl.LE);
        REL_OPS_MAP.put(PascalTokenType.GREATER_THAN,   ICodeNodeTypeImpl.GT);
        REL_OPS_MAP.put(PascalTokenType.GREATER_EQUALS, ICodeNodeTypeImpl.GE);
    };

    // Set of additive operators.
    private static final EnumSet<PascalTokenType> ADD_OPS = EnumSet.of(
                    PascalTokenType.PLUS,
                    PascalTokenType.MINUS,
                    PascalTokenType.OR);
    // Map additive operator tokens to node types.
    private static final HashMap<PascalTokenType, ICodeNodeTypeImpl> ADD_OPS_MAP = new HashMap<PascalTokenType, ICodeNodeTypeImpl>();
    static
    {
        ADD_OPS_MAP.put(PascalTokenType.PLUS,  ICodeNodeTypeImpl.ADD);
        ADD_OPS_MAP.put(PascalTokenType.MINUS, ICodeNodeTypeImpl.SUBTRACT);
        ADD_OPS_MAP.put(PascalTokenType.OR,    ICodeNodeTypeImpl.OR);
    };

    // Set of multiplicative operators.
    private static final EnumSet<PascalTokenType> MULT_OPS = EnumSet.of(
                    PascalTokenType.STAR,
                    PascalTokenType.SLASH,
                    PascalTokenType.DIV,
                    PascalTokenType.MOD,
                    PascalTokenType.AND);

    // Map multiplicative operator tokens to node types.
    private static final HashMap<PascalTokenType, ICodeNodeType> MULT_OPS_MAP = new HashMap<PascalTokenType, ICodeNodeType>();
    static
    {
        MULT_OPS_MAP.put(PascalTokenType.STAR,  ICodeNodeTypeImpl.MULTIPLY);
        MULT_OPS_MAP.put(PascalTokenType.SLASH, ICodeNodeTypeImpl.FLOAT_DIVIDE);
        MULT_OPS_MAP.put(PascalTokenType.DIV,   ICodeNodeTypeImpl.INTEGER_DIVIDE);
        MULT_OPS_MAP.put(PascalTokenType.MOD,   ICodeNodeTypeImpl.MOD);
        MULT_OPS_MAP.put(PascalTokenType.AND,   ICodeNodeTypeImpl.AND);
    };

    public ExpressionParser(PascalParserTD parent)
    {
        super(parent);
    }

    @Override
    public ICodeNode parse(Token token) throws Exception
    {
        return parseExpression(token);
    }

    private ICodeNode parseExpression(Token token) throws Exception
    {
        ICodeNode rootNode = parseSimpleExpression(token);

        token = currentToken();
        TokenType tokenType = token.getType();

        if(REL_OPS.contains(tokenType))
        {
            // Create a new operator node and adopt the current tree
            // as its first child.
            ICodeNodeType nodeType = REL_OPS_MAP.get(tokenType);
            ICodeNode opNode = ICodeFactory.createICodeNode(nodeType);
            opNode.addChild(rootNode);
            token = nextToken(); // consume the operator

            opNode.addChild(parseSimpleExpression(token));

            rootNode = opNode;
        }

        return rootNode;
    }

    private ICodeNode parseSimpleExpression(Token token) throws Exception
    {
        TokenType signType = null;

        TokenType tokenType = token.getType();
        if ((tokenType == PascalTokenType.PLUS) || (tokenType == PascalTokenType.MINUS))
        {
            signType = tokenType;
            token = nextToken(); // consume the + or -
        }

        // Parse a term and make the root of its tree the root node.
        ICodeNode rootNode = parseTerm(token);

        if(signType == PascalTokenType.MINUS)
        {
            // Create a NEGATE node and adopt the current tree
            // as its child. The NEGATE node becomes the new root node.
            ICodeNode negateNode = ICodeFactory.createICodeNode(ICodeNodeTypeImpl.NEGATE);
            negateNode.addChild(rootNode);
            rootNode = negateNode;
        }

        token = currentToken();
        tokenType = token.getType();

        // Loop over additive operators
        while (ADD_OPS.contains(tokenType))
        {
            // Create a new operator node and adopt the current tree
            // as its first child.
            ICodeNodeType nodeType = ADD_OPS_MAP.get(tokenType);
            ICodeNode opNode = ICodeFactory.createICodeNode(nodeType);
            opNode.addChild(rootNode);

            token = nextToken(); // consume the operator

            opNode.addChild(parseTerm(token));

            rootNode = opNode;

            token = currentToken();
            tokenType = token.getType();
        }

        return rootNode;
    }

    private ICodeNode parseTerm(Token token) throws Exception
    {
        ICodeNode rootNode = parseFactor(token);

        token = currentToken();
        TokenType tokenType = token.getType();

        while (MULT_OPS.contains(tokenType))
        {
            ICodeNodeType nodeType = MULT_OPS_MAP.get(tokenType);
            ICodeNode opNode = ICodeFactory.createICodeNode(nodeType);
            opNode.addChild(rootNode);

            token = nextToken();

            opNode.addChild(parseFactor(token));

            rootNode = opNode;

            token = currentToken();
            tokenType = token.getType();
        }

        return rootNode;
    }

    private ICodeNode parseFactor(Token token) throws Exception
    {
        TokenType tokenType = token.getType();
        ICodeNode rootNode = null;

        switch ((PascalTokenType)tokenType)
        {
            case IDENTIFIER:
            {
                // Look up the identifier in the symbol table stack.
                // Flag the identifier as undefined if it's not found.
                String name = token.getText().toLowerCase();
                SymTabEntry id = symTabStack.lookup(name);
                if (id == null)
                {
                    errorHandler.flag(token, PascalErrorCode.IDENTIFIER_UNDEFINED, this);
                    id = symTabStack.enterLocal(name);
                }

                rootNode = ICodeFactory.createICodeNode(ICodeNodeTypeImpl.VARIABLE);
                rootNode.setAttribute(ICodeKeyImpl.ID, id);
                rootNode.setAttribute(ICodeKeyImpl.LEVEL, id.getCurrentNestingLevel());
                id.appendLineNumber(token.getLineNum());

                token = nextToken();  // consume the identifier
                break;
            }
            case INTEGER:
            {
                // Create an INTEGER_CONSTANT node as the root node.
                rootNode = ICodeFactory.createICodeNode(ICodeNodeTypeImpl.INTEGER_CONSTANT);
                rootNode.setAttribute(ICodeKeyImpl.VALUE, token.getValue());

                token = nextToken();  // consume the number
                break;
            }
            case REAL:
            {
                // Create an REAL_CONSTANT node as the root node.
                rootNode = ICodeFactory.createICodeNode(ICodeNodeTypeImpl.REAL_CONSTANT);
                rootNode.setAttribute(ICodeKeyImpl.VALUE, token.getValue());

                token = nextToken();  // consume the number
                break;
            }
            case STRING:
            {
                String value = (String) token.getValue();

                // Create a STRING_CONSTANT node as the root node.
                rootNode = ICodeFactory.createICodeNode(ICodeNodeTypeImpl.STRING_CONSTANT);
                rootNode.setAttribute(ICodeKeyImpl.VALUE, value);

                token = nextToken();  // consume the string
                break;
            }
            case NOT:
            {
                token = nextToken();  // consume the NOT

                // Create a NOT node as the root node.
                rootNode = ICodeFactory.createICodeNode(ICodeNodeTypeImpl.NOT);

                // Parse the factor.  The NOT node adopts the
                // factor node as its child.
                rootNode.addChild(parseFactor(token));

                break;
            }
            case LEFT_PAREN:
            {
                token = nextToken();      // consume the (

                // Parse an expression and make its node the root node.
                rootNode = parseExpression(token);

                // Look for the matching ) token.
                token = currentToken();
                if (token.getType() == PascalTokenType.RIGHT_PAREN)
                {
                    token = nextToken();  // consume the )
                }
                else
                {
                    errorHandler.flag(token, PascalErrorCode.MISSING_RIGHT_PAREN, this);
                }

                break;
            }
            default:
            {
                errorHandler.flag(token, PascalErrorCode.UNEXPECTED_TOKEN, this);
                break;
            }
        }

        return rootNode;
    }
}
