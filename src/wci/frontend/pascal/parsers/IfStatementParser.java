package wci.frontend.pascal.parsers;

import wci.frontend.Token;
import wci.frontend.pascal.PascalErrorCode;
import wci.frontend.pascal.PascalParserTD;
import wci.frontend.pascal.tokens.PascalTokenType;
import wci.intermediate.ICodeFactory;
import wci.intermediate.ICodeNode;
import wci.intermediate.icodeimpl.ICodeNodeTypeImpl;

import java.util.EnumSet;

public class IfStatementParser extends StatementParser
{
    // Synchronization set for THEN.
    private static final EnumSet<PascalTokenType> THEN_SET = StatementParser.STMT_START_SET.clone();
    static
    {
        THEN_SET.add(PascalTokenType.THEN);
        THEN_SET.addAll(StatementParser.STMT_FOLLOW_SET);
    }
    public IfStatementParser(PascalParserTD parent)
    {
        super(parent);
    }

    @Override
    public ICodeNode parse(Token token) throws Exception
    {
        token = nextToken(); // consume IF

        ICodeNode ifNode = ICodeFactory.createICodeNode(ICodeNodeTypeImpl.IF);

        // Parse the expression.
        // The IF node adopts the expression subtree as its first child.
        ExpressionParser expressionParser = new ExpressionParser(this);
        ifNode.addChild(expressionParser.parse(token));

        token = synchronize(THEN_SET);
        if(token.getType() == PascalTokenType.THEN)
        {
            token = nextToken(); // consume the THEN
        }
        else
        {
            errorHandler.flag(token, PascalErrorCode.MISSING_END, this);
        }

        // Parse the THEN statement.
        // The IF node adopts the statement subtree as its second child.
        StatementParser statementParser = new StatementParser(this);
        ifNode.addChild(statementParser.parse(token));
        token = currentToken();

        // Look for an ELSE.
        if (token.getType() == PascalTokenType.ELSE)
        {
            token = nextToken(); // consume the THEN
            // Parse the ELSE statement.
            // The IF node adopts the statement subtree as its third child.
            ifNode.addChild(statementParser.parse(token));
        }

        return ifNode;
    }
}
