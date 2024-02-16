package wci.frontend.pascal.parsers;

import wci.frontend.Token;
import wci.frontend.pascal.PascalErrorCode;
import wci.frontend.pascal.PascalParserTD;
import wci.frontend.pascal.tokens.PascalTokenType;
import wci.intermediate.ICodeFactory;
import wci.intermediate.ICodeNode;
import wci.intermediate.icodeimpl.ICodeNodeTypeImpl;

import java.util.EnumSet;

public class WhileStatementParser extends StatementParser
{
    private static final EnumSet<PascalTokenType> DO_SET = StatementParser.STMT_START_SET.clone();
    static
    {
        DO_SET.add(PascalTokenType.DO);
        DO_SET.addAll(StatementParser.STMT_FOLLOW_SET);
    }
    public WhileStatementParser(PascalParserTD parent)
    {
        super(parent);
    }

    @Override
    public ICodeNode parse(Token token) throws Exception
    {
        token = nextToken(); // consume WHILE

        // Create LOOP, TEST, and NOT nodes.
        ICodeNode loopNode = ICodeFactory.createICodeNode(ICodeNodeTypeImpl.LOOP);
        ICodeNode breakNode = ICodeFactory.createICodeNode(ICodeNodeTypeImpl.TEST);
        ICodeNode notNode = ICodeFactory.createICodeNode(ICodeNodeTypeImpl.NOT);

        loopNode.addChild(breakNode);
        breakNode.addChild(notNode);

        // Parse the expression.
        // The NOT node adopts the expression subtree as its only child.
        ExpressionParser expressionParser = new ExpressionParser(this);
        notNode.addChild(expressionParser.parse(token));

        // Synchronize at the DO.
        token = synchronize(DO_SET);
        if(token.getType() == PascalTokenType.DO)
        {
            token = nextToken(); // consume the DO
        }
        else
        {
            errorHandler.flag(token, PascalErrorCode.MISSING_DO, this);
        }

        // Parse the statement.
        // The LOOP node adopts the statement subtree as its second child.
        StatementParser statementParser = new StatementParser(this);
        loopNode.addChild(statementParser.parse(token));
        return loopNode;
    }
}
