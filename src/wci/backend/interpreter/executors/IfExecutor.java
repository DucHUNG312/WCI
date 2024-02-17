package wci.backend.interpreter.executors;

import wci.backend.interpreter.Executor;
import wci.intermediate.ICodeNode;
import wci.intermediate.icodeimpl.ICodeNodeTypeImpl;

import java.util.List;

public class IfExecutor extends StatementExecutor
{
    public IfExecutor(Executor parent)
    {
        super(parent);
    }

    @Override
    public Object execute(ICodeNode node)
    {
        List<ICodeNode> children = node.getChildren();
        ICodeNode exprNode = children.get(0);
        ICodeNode thenStmtNode = children.get(1);
        ICodeNode elseStmtNode = children.size() > 2 ? children.get(2) : null;

        ExpressionExecutor expressionExecutor = new ExpressionExecutor(this);
        StatementExecutor statementExecutor = new StatementExecutor(this);

        // Evaluate the expression to determine with statement to execute
        boolean b = (Boolean)expressionExecutor.execute(exprNode);
        if(b)
        {
            statementExecutor.execute(thenStmtNode);
        }
        else if(elseStmtNode != null)
        {
            statementExecutor.execute(elseStmtNode);
        }

        ++executionCount;
        return null;
    }
}
