package wci.frontend.pascal.parsers;

import wci.frontend.Token;
import wci.frontend.pascal.PascalErrorCode;
import wci.frontend.pascal.PascalParserTD;
import wci.frontend.pascal.tokens.PascalTokenType;
import wci.intermediate.ICodeFactory;
import wci.intermediate.ICodeNode;
import wci.intermediate.icodeimpl.ICodeNodeTypeImpl;

public class RepeatStatementParser extends StatementParser
{
    public RepeatStatementParser(PascalParserTD parent)
    {
        super(parent);
    }

    @Override
    public ICodeNode parse(Token token) throws Exception
    {
        token = nextToken(); // consume REPEAT

        ICodeNode loopNode = ICodeFactory.createICodeNode(ICodeNodeTypeImpl.LOOP);
        ICodeNode testNode = ICodeFactory.createICodeNode(ICodeNodeTypeImpl.TEST);

        StatementParser statementParser = new StatementParser(this);
        statementParser.parseList(token, loopNode, PascalTokenType.UNTIL, PascalErrorCode.MISSING_UNTIL);
        token = currentToken();

        ExpressionParser expressionParser = new ExpressionParser(this);
        testNode.addChild(expressionParser.parse(token));

        loopNode.addChild(testNode);

        return loopNode;
    }
}
