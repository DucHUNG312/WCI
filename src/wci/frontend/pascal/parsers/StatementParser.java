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

import java.util.EnumSet;

public class StatementParser extends PascalParserTD
{
    // Synchronization set for starting a statement.
    protected static final EnumSet<PascalTokenType> STMT_START_SET = EnumSet.of(
            PascalTokenType.BEGIN,
            PascalTokenType.CASE,
            PascalTokenType.FOR,
            PascalTokenType.IF,
            PascalTokenType.REPEAT,
            PascalTokenType.WHILE,
            PascalTokenType.IDENTIFIER,
            PascalTokenType.SEMICOLON
    );

    // Synchronization set for following a statement.
    protected static final EnumSet<PascalTokenType> STMT_FOLLOW_SET = EnumSet.of(
            PascalTokenType.SEMICOLON,
            PascalTokenType.END,
            PascalTokenType.ELSE,
            PascalTokenType.UNTIL,
            PascalTokenType.DOT
    );

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
            case REPEAT:
            {
                RepeatStatementParser repeatParser = new RepeatStatementParser(this);
                statementNode = repeatParser.parse(token);
                break;
            }
            case WHILE:
            {
                WhileStatementParser whileParser = new WhileStatementParser(this);
                statementNode = whileParser.parse(token);
                break;
            }
            case FOR:
            {
                ForStatementParser forParser = new ForStatementParser(this);
                statementNode = forParser.parse(token);
                break;
            }
            case IF:
            {
                IfStatementParser ifParser = new IfStatementParser(this);
                statementNode = ifParser.parse(token);
                break;
            }
            case CASE:
            {
                CaseStatementParser caseParser = new CaseStatementParser(this);
                statementNode = caseParser.parse(token);
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
        // Synchronization set for the terminator.
        EnumSet<PascalTokenType> terminatorSet = STMT_START_SET.clone();
        terminatorSet.add(terminator);

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
            else if (STMT_START_SET.contains(tokenType))
            {
                errorHandler.flag(token, PascalErrorCode.MISSING_SEMICOLON, this);
            }

            // Synchronize at the start of the next statement
            // or at the terminator.
            token = synchronize(terminatorSet);
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
