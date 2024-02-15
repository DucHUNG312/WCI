package wci.frontend.pascal.parsers;

import wci.frontend.EofToken;
import wci.frontend.Token;
import wci.frontend.TokenType;
import wci.frontend.pascal.PascalErrorCode;
import wci.frontend.pascal.PascalParserTD;
import wci.frontend.pascal.tokens.PascalTokenType;
import wci.intermediate.ICodeFactory;
import wci.intermediate.ICodeNode;
import wci.intermediate.ICodeNodeType;
import wci.intermediate.icodeimpl.ICodeKeyImpl;
import wci.intermediate.icodeimpl.ICodeNodeTypeImpl;

public class StatementParser extends PascalParserTD
{
    public StatementParser(PascalParserTD parent)
    {
        super(parent);
    }

    public ICodeNode parse(Token token) throws Exception
    {
        ICodeNode statementNode = null;

        switch ((PascalTokenType)token.getType())
        {
            case BEGIN:
            {
                CompoundStatementParser compoundParser = new CompoundStatementParser(this);
                statementNode = compoundParser.parse(token);
                break;
            }
            case IDENTIFIER:
            {
                AssignmentStatementParser assignmentParser = new AssignmentStatementParser(this);
                statementNode = assignmentParser.parse(token);
                break;
            }
            default:
            {
                statementNode = ICodeFactory.createICodeNode(ICodeNodeTypeImpl.NO_OP);
                break;
            }
        }

        // Set the current line number as an attribute.
        setLineNumber(statementNode, token);

        return statementNode;
    }

    protected void setLineNumber(ICodeNode node, Token token)
    {
        if (node != null)
        {
            node.setAttribute(ICodeKeyImpl.LINE, token.getLineNum());
        }
    }

    protected void parseList(Token token, ICodeNode parentNode, PascalTokenType terminator, PascalErrorCode errorCode) throws Exception
    {
        while (!(token instanceof EofToken) && (token.getType() != terminator))
        {
            ICodeNode statementNode = parse(token);
            parentNode.addChild(statementNode);

            token = currentToken();
            TokenType tokenType = token.getType();

            // Check token types here
            // Look for the semicolon between statements.
            if (tokenType == PascalTokenType.SEMICOLON)
            {
                token = nextToken(); // consume the SEMICOLON token
            }
            // If at the start of the next assignment statement,
            // then missing a semicolon.
            else if (tokenType == PascalTokenType.IDENTIFIER)
            {
                errorHandler.flag(token, PascalErrorCode.MISSING_SEMICOLON, this);
            }
            // Unexpected token.
            else if (tokenType != terminator)
            {
                errorHandler.flag(token, PascalErrorCode.UNEXPECTED_TOKEN, this);
                token = nextToken();  // consume the unexpected token
            }
        }

        if(token.getType() == terminator)
        {
            token = nextToken(); // consume the terminator token
        }
        else
        {
            errorHandler.flag(token, errorCode, this);
        }
    }

}
