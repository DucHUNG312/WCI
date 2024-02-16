package wci.frontend.pascal.parsers;

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

public class ForStatementParser extends StatementParser
{
    // Synchronization set for TO or DOWNTO.
    static final EnumSet<PascalTokenType> TO_DOWNTO_SET = ExpressionParser.EXPR_START_SET.clone();
    static
    {
        TO_DOWNTO_SET.add(PascalTokenType.TO);
        TO_DOWNTO_SET.add(PascalTokenType.DOWNTO);
        TO_DOWNTO_SET.addAll(StatementParser.STMT_FOLLOW_SET);
    }

    // Synchronization set for DO
    private static final EnumSet<PascalTokenType> DO_SET = StatementParser.STMT_START_SET.clone();
    static
    {
        DO_SET.add(PascalTokenType.DO);
        DO_SET.addAll(StatementParser.STMT_FOLLOW_SET);
    }

    public ForStatementParser(PascalParserTD parent)
    {
        super(parent);
    }

    @Override
    public ICodeNode parse(Token token) throws Exception
    {
        token = nextToken(); // consume FOR
        Token targetToken = token;

        ICodeNode compoundNode = ICodeFactory.createICodeNode(ICodeNodeTypeImpl.COMPOUND);
        ICodeNode loopNode = ICodeFactory.createICodeNode(ICodeNodeTypeImpl.LOOP);
        ICodeNode testNode = ICodeFactory.createICodeNode(ICodeNodeTypeImpl.TEST);

        AssignmentStatementParser assignmentParser = new AssignmentStatementParser(this);
        ICodeNode initAssignNode = assignmentParser.parse(token);

        // Set the current line number attribute.
        setLineNumber(initAssignNode, targetToken);

        compoundNode.addChild(initAssignNode);
        compoundNode.addChild(loopNode);

        // Synchronize at the TO or DOWNTO.
        token = synchronize(TO_DOWNTO_SET);
        TokenType direction = token.getType();

        // Look for the TO or DOWNTO.
        if ((direction == PascalTokenType.TO) || (direction == PascalTokenType.DOWNTO))
        {
            token = nextToken(); // consume the TO or DOWNTO
        }
        else
        {
            direction = PascalTokenType.TO;
            errorHandler.flag(token, PascalErrorCode.MISSING_TO_DOWNTO, this);
        }

        // Create a relational operator node: GT for TO, or LT for DOWNTO.
        ICodeNode relOpNode = ICodeFactory.createICodeNode(direction == PascalTokenType.TO ? ICodeNodeTypeImpl.GT : ICodeNodeTypeImpl.LT);

        // Copy the control VARIABLE node. The relational operator
        // node adopts the copied VARIABLE node as its first child.
        ICodeNode controlVarNode = initAssignNode.getChildren().get(0);
        relOpNode.addChild(controlVarNode.copy());

        // Parse the termination expression. The relational operator node
        // adopts the expression as its second child.
        ExpressionParser expressionParser = new ExpressionParser(this);
        relOpNode.addChild(expressionParser.parse(token));

        // The TEST node adopts the relational operator node as its only child.
        // The LOOP node adopts the TEST node as its first child.
        testNode.addChild(relOpNode);
        loopNode.addChild(testNode);

        // Synchronize at the DO
        token = synchronize(DO_SET);
        // Look for the TO or DOWNTO.
        if ((token.getType() == PascalTokenType.DO))
        {
            token = nextToken(); // consume the DO
        }
        else
        {
            direction = PascalTokenType.TO;
            errorHandler.flag(token, PascalErrorCode.MISSING_DO, this);
        }

        // Parse the nested statement. The LOOP node adopts the statement
        // node as its second child.
        StatementParser statementParser = new StatementParser(this);
        loopNode.addChild(statementParser.parse(token));

        // Create an assignment with a copy of the control variable
        // to advance the value of the variable.
        ICodeNode nextAssignNode = ICodeFactory.createICodeNode(ICodeNodeTypeImpl.ASSIGN);
        nextAssignNode.addChild(controlVarNode.copy());

        // Create the arithmetic operator node:
        // ADD for TO, or SUBTRACT for DOWNTO.
        ICodeNode arithOpNode = ICodeFactory.createICodeNode(direction == PascalTokenType.TO ? ICodeNodeTypeImpl.ADD : ICodeNodeTypeImpl.SUBTRACT);

        // The operator node adopts a copy of the loop variable as its
        // first child and the value 1 as its second child.
        arithOpNode.addChild(controlVarNode.copy());
        ICodeNode oneNode = ICodeFactory.createICodeNode(ICodeNodeTypeImpl.INTEGER_CONSTANT);
        oneNode.setAttribute(ICodeKeyImpl.VALUE, 1);
        arithOpNode.addChild(oneNode);

        // The next ASSIGN node adopts the arithmetic operator node as its
        // second child. The loop node adopts the next ASSIGN node as its
        // third child.
        nextAssignNode.addChild(arithOpNode);
        loopNode.addChild(nextAssignNode);
        // Set the current line number attribute.
        setLineNumber(nextAssignNode, targetToken);

        return compoundNode;
    }
}
