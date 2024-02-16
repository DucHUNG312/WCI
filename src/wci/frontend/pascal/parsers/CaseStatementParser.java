package wci.frontend.pascal.parsers;

import wci.frontend.EofToken;
import wci.frontend.Token;
import wci.frontend.TokenType;
import wci.frontend.pascal.PascalErrorCode;
import wci.frontend.pascal.PascalParserTD;
import wci.frontend.pascal.tokens.PascalTokenType;
import wci.intermediate.ICodeFactory;
import wci.intermediate.ICodeNode;
import wci.intermediate.icodeimpl.ICodeKeyImpl;
import wci.intermediate.icodeimpl.ICodeNodeTypeImpl;

import java.util.EnumSet;
import java.util.HashSet;

public class CaseStatementParser extends StatementParser
{
    // Synchronization set for starting a CASE option constant.
    private static final EnumSet<PascalTokenType> CONSTANT_START_SET = EnumSet.of(
            PascalTokenType.IDENTIFIER,
            PascalTokenType.INTEGER,
            PascalTokenType.PLUS,
            PascalTokenType.MINUS,
            PascalTokenType.STRING
    );
    // Synchronization set for OF.
    private static final EnumSet<PascalTokenType> OF_SET = CONSTANT_START_SET.clone();
    static
    {
        OF_SET.add(PascalTokenType.OF);
        OF_SET.addAll(StatementParser.STMT_FOLLOW_SET);
    }

    // Synchronization set for COMMA.
    private static final EnumSet<PascalTokenType> COMMA_SET = CONSTANT_START_SET.clone();
    static
    {
        COMMA_SET.add(PascalTokenType.COMMA);
        COMMA_SET.add(PascalTokenType.COLON);
        COMMA_SET.addAll(StatementParser.STMT_START_SET);
        COMMA_SET.addAll(StatementParser.STMT_FOLLOW_SET);
    }
    public CaseStatementParser(PascalParserTD parent)
    {
        super(parent);
    }

    @Override
    public ICodeNode parse(Token token) throws Exception
    {
        token = nextToken(); // consume CASE

        // Create a SELECT node
        ICodeNode selectNode = ICodeFactory.createICodeNode(ICodeNodeTypeImpl.SELECT);

        // Parse the CASE expression.
        // The SELECT node adopts the expression subtree as its first child.
        ExpressionParser expressionParser = new ExpressionParser(this);
        selectNode.addChild(expressionParser.parse(token));

        ICodeNode loopNode = ICodeFactory.createICodeNode(ICodeNodeTypeImpl.NO_OP);

        // Synchronize at the OF.
        token = synchronize(OF_SET);
        if (token.getType() == PascalTokenType.OF)
        {
            token = nextToken(); // consume the OF
        }
        else
        {
            errorHandler.flag(token, PascalErrorCode.MISSING_OF, this);
        }

        // Set of CASE branch constants.
        HashSet<Object> constantSet = new HashSet<Object>();

        // Loop to parse each CASE branch until the END token
        // or the end of the source file.
        while (!(token instanceof EofToken) && (token.getType() != PascalTokenType.END))
        {
            // The SELECT node adopts the CASE branch subtree.
            selectNode.addChild(parseBranch(token, constantSet));
            token = currentToken();
            TokenType tokenType = token.getType();
            // Look for the semicolon between CASE branches.
            if (tokenType == PascalTokenType.SEMICOLON)
            {
                token = nextToken(); // consume the SEMICOLON
            }
            // If at the start of the next constant, then missing a semicolon.
            else if (CONSTANT_START_SET.contains(tokenType))
            {
                errorHandler.flag(token, PascalErrorCode.MISSING_SEMICOLON, this);
            }
        }
        // Look for the END token.
        if (token.getType() == PascalTokenType.END)
        {
            token = nextToken(); // consume END
        }
        else
        {
            errorHandler.flag(token, PascalErrorCode.MISSING_END, this);
        }
        return selectNode;
    }

    private ICodeNode parseBranch(Token token, HashSet<Object> constantSet) throws Exception
    {
        // Create an SELECT_BRANCH node and a SELECT_CONSTANTS node.
        // The SELECT_BRANCH node adopts the SELECT_CONSTANTS node as its
        // first child.
        ICodeNode branchNode = ICodeFactory.createICodeNode(ICodeNodeTypeImpl.SELECT_BRANCH);
        ICodeNode constantsNode = ICodeFactory.createICodeNode(ICodeNodeTypeImpl.SELECT_CONSTANTS);
        branchNode.addChild(constantsNode);

        // Parse the list of CASE branch constants.
        // The SELECT_CONSTANTS node adopts each constant.
        parseConstantList(token, constantsNode, constantSet);

        // Look for the : token.
        token = currentToken();
        if (token.getType() == PascalTokenType.COLON)
        {
            token = nextToken(); // consume the :
        }
        else
        {
            errorHandler.flag(token, PascalErrorCode.MISSING_COLON, this);
        }

        // Parse the CASE branch statement. The SELECT_BRANCH node adopts
        // the statement subtree as its second child.
        StatementParser statementParser = new StatementParser(this);
        branchNode.addChild(statementParser.parse(token));
        return branchNode;
    }

    private void parseConstantList(Token token, ICodeNode constantsNode, HashSet<Object> constantSet) throws Exception
    {
        while (CONSTANT_START_SET.contains(token.getType()))
        {
            // The constants list node adopts the constant node.
            constantsNode.addChild(parseConstant(token, constantSet));
            // Synchronize at the comma between constants.
            token = synchronize(COMMA_SET);
            // Look for the comma.
            if (token.getType() == PascalTokenType.COMMA)
            {
                token = nextToken(); // consume the ,
            }
            // If at the start of the next constant, then missing a comma.
            else if (CONSTANT_START_SET.contains(token.getType()))
            {
                errorHandler.flag(token, PascalErrorCode.MISSING_COMMA, this);
            }
        }
    }

    private ICodeNode parseConstant(Token token, HashSet<Object> constantSet) throws Exception
    {
        TokenType sign = null;
        ICodeNode constantNode = null;
        // Synchronize at the start of a constant.
        token = synchronize(CONSTANT_START_SET);
        TokenType tokenType = token.getType();
        // Plus or minus sign?
        if ((tokenType == PascalTokenType.PLUS) || (tokenType == PascalTokenType.MINUS))
        {
            sign = tokenType;
            token = nextToken(); // consume sign
        }
        // Parse the constant.
        switch ((PascalTokenType) token.getType())
        {
            case IDENTIFIER:
            {
                constantNode = parseIdentifierConstant(token, sign);
                break;
            }
            case INTEGER:
            {
                constantNode = parseIntegerConstant(token.getText(), sign);
                break;
            }
            case STRING:
            {
                constantNode = parseCharacterConstant(token, (String) token.getValue(), sign);
                break;
            }
            default:
            {
                errorHandler.flag(token, PascalErrorCode.INVALID_CONSTANT, this);
                break;
            }
        }
        // Check for reused constants.
        if (constantNode != null)
        {
            Object value = constantNode.getAttribute(ICodeKeyImpl.VALUE);
            if (constantSet.contains(value))
            {
                errorHandler.flag(token, PascalErrorCode.CASE_CONSTANT_REUSED, this);
            }
            else
            {
                constantSet.add(value);
            }
        }
        nextToken(); // consume the constant
        return constantNode;
    }

    private ICodeNode parseIdentifierConstant(Token token, TokenType sign) throws Exception
    {
        // Placeholder: Don't allow for now.
        errorHandler.flag(token, PascalErrorCode.INVALID_CONSTANT, this);
        return null;
    }

    private ICodeNode parseIntegerConstant(String value, TokenType sign)
    {
        ICodeNode constantNode = ICodeFactory.createICodeNode(ICodeNodeTypeImpl.INTEGER_CONSTANT);
        int intValue = Integer.parseInt(value);
        if (sign == PascalTokenType.MINUS)
        {
            intValue = -intValue;
        }
        constantNode.setAttribute(ICodeKeyImpl.VALUE, intValue);
        return constantNode;
    }

    private ICodeNode parseCharacterConstant(Token token, String value, TokenType sign)
    {
        ICodeNode constantNode = null;
        if (sign != null)
        {
            errorHandler.flag(token, PascalErrorCode.INVALID_CONSTANT, this);
        }
        else
        {
            if (value.length() == 1)
            {
                constantNode = ICodeFactory.createICodeNode(ICodeNodeTypeImpl.STRING_CONSTANT);
                constantNode.setAttribute(ICodeKeyImpl.VALUE, value);
            }
            else
            {
                errorHandler.flag(token, PascalErrorCode.INVALID_CONSTANT, this);
            }
        }
        return constantNode;
    }
}
