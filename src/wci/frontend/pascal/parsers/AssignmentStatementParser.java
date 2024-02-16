package wci.frontend.pascal.parsers;

import wci.frontend.Token;
import wci.frontend.pascal.PascalErrorCode;
import wci.frontend.pascal.PascalParserTD;
import wci.frontend.pascal.tokens.PascalTokenType;
import wci.intermediate.ICodeFactory;
import wci.intermediate.ICodeNode;
import wci.intermediate.SymTabEntry;
import wci.intermediate.icodeimpl.ICodeKeyImpl;
import wci.intermediate.icodeimpl.ICodeNodeTypeImpl;

import java.util.EnumSet;

public class AssignmentStatementParser extends StatementParser
{
    // Synchronization set for the := token.
    private static final EnumSet<PascalTokenType> COLON_EQUALS_SET = ExpressionParser.EXPR_START_SET.clone();
    static
    {
        COLON_EQUALS_SET.add(PascalTokenType.COLON_EQUALS);
        COLON_EQUALS_SET.addAll(StatementParser.STMT_FOLLOW_SET);
    }
    public AssignmentStatementParser(PascalParserTD parent)
    {
        super(parent);
    }

    @Override
    public ICodeNode parse(Token token) throws Exception
    {
        ICodeNode assignNode = ICodeFactory.createICodeNode(ICodeNodeTypeImpl.ASSIGN);

        // Look up the target identifer in the symbol table stack.
        // Enter the identifier into the table if it not found.
        String targetName = token.getText().toLowerCase();
        SymTabEntry targetId = symTabStack.lookup(targetName);
        if(targetId == null)
        {
            targetId = symTabStack.enterLocal(targetName);
        }
        targetId.appendLineNumber(token.getLineNum());

        token = nextToken(); // consume the identifier token

        // Create the variable node and set its name attribute.
        ICodeNode variableNode = ICodeFactory.createICodeNode(ICodeNodeTypeImpl.VARIABLE);
        variableNode.setAttribute(ICodeKeyImpl.ID, targetId);
        variableNode.setAttribute(ICodeKeyImpl.LEVEL, targetId.getCurrentNestingLevel());

        // The ASSIGN node adopts the variable node as its first child.
        assignNode.addChild(variableNode);

        // Look for the := token.
        token = synchronize(COLON_EQUALS_SET);
        if (token.getType() == PascalTokenType.COLON_EQUALS)
        {
            token = nextToken();  // consume the :=
        }
        else
        {
            errorHandler.flag(token, PascalErrorCode.MISSING_COLON_EQUALS, this);
        }

        ExpressionParser expressionParser = new ExpressionParser(this);
        assignNode.addChild(expressionParser.parse(token));

        return assignNode;
    }
}
