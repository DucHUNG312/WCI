package wci.backend.interpreter.executors;

import wci.backend.interpreter.Executor;
import wci.intermediate.ICodeNode;
import wci.intermediate.SymTabEntry;
import wci.intermediate.icodeimpl.ICodeKeyImpl;
import wci.intermediate.icodeimpl.ICodeNodeTypeImpl;
import wci.intermediate.symtabimpl.SymTabKeyImpl;

import java.util.List;

public class LoopExecutor extends StatementExecutor
{
    public LoopExecutor(Executor parent)
    {
        super(parent);
    }

    @Override
    public Object execute(ICodeNode node)
    {
        boolean exitLoop = false;
        ICodeNode exprNode = null;
        List<ICodeNode> loopChildren = node.getChildren();

        ExpressionExecutor expressionExecutor = new ExpressionExecutor(this);
        StatementExecutor statementExecutor = new StatementExecutor(this);

        while (!exitLoop)
        {
            ++executionCount; // count the loop statement itself

            for (ICodeNode child : loopChildren)
            {
                ICodeNodeTypeImpl childType = (ICodeNodeTypeImpl) child.getType();

                if(childType == ICodeNodeTypeImpl.TEST)
                {
                    if(exprNode == null)
                    {
                        exprNode = child.getChildren().get(0);
                    }
                    exitLoop = (Boolean)expressionExecutor.execute(exprNode);
                }

                // Statement node
                else
                {
                    statementExecutor.execute(child);
                }

                // Exit if test expression value is true
                if(exitLoop)
                {
                    break;
                }
            }
        }

        return null;
    }
}
