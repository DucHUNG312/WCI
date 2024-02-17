package wci.backend.interpreter.executors;

import wci.backend.interpreter.Executor;
import wci.intermediate.ICodeNode;
import wci.intermediate.icodeimpl.ICodeKeyImpl;

import java.util.ArrayList;
import java.util.HashMap;

public class SelectExecutor extends StatementExecutor
{
    private static HashMap<ICodeNode, HashMap<Object, ICodeNode>> jumpCache = new HashMap<ICodeNode, HashMap<Object, ICodeNode>>();
    public SelectExecutor(Executor parent)
    {
        super(parent);
    }

    @Override
    public Object execute(ICodeNode node)
    {
        // Is there already an entry for this SELECT node in the
        // jump table cache? If not, create a jump table entry.
        HashMap<Object, ICodeNode> jumpTable = jumpCache.get(node);
        if(jumpTable == null)
        {
            jumpTable = createJumpTable(node);
            jumpCache.put(node, jumpTable);
        }

        // Get the SELECT node's children.
        ArrayList<ICodeNode> selectChildren = node.getChildren();
        ICodeNode exprNode = selectChildren.get(0);

        ExpressionExecutor expressionExecutor = new ExpressionExecutor(this);
        Object selectValue = expressionExecutor.execute(exprNode);

        ICodeNode statementNode = jumpTable.get(selectValue);
        if(statementNode != null)
        {
            StatementExecutor statementExecutor = new StatementExecutor(this);
            statementExecutor.execute(statementNode);
        }

        ++executionCount;
        return null;
    }

    private HashMap<Object, ICodeNode> createJumpTable(ICodeNode node)
    {
        HashMap<Object, ICodeNode> jumpTable = new HashMap<Object, ICodeNode>();

        ArrayList<ICodeNode> selectChildrent = node.getChildren();
        for (int i = 1; i < selectChildrent.size(); i++)
        {
            ICodeNode branchNode = selectChildrent.get(i);
            ICodeNode constantsNode = branchNode.getChildren().get(0);
            ICodeNode statementNode = branchNode.getChildren().get(1);

            // Loop over the constants children of the branch's CONSTANTS_NODE.
            ArrayList<ICodeNode> constantsList = constantsNode.getChildren();
            for (ICodeNode constantNode : constantsList)
            {
                Object value = constantNode.getAttribute(ICodeKeyImpl.VALUE);
                jumpTable.put(value, statementNode);
            }
        }

        return jumpTable;
    }

    /*
    @Override
    public Object execute(ICodeNode node)
    {
        // Get the SELECT node's children
        ArrayList<ICodeNode> selectChildren = node.getChildren();
        ICodeNode exprNode = selectChildren.get(0);

        // Evaluate the SELECT expression
        ExpressionExecutor expressionExecutor = new ExpressionExecutor(this);
        Object selectValue = expressionExecutor.execute(exprNode);

        ICodeNode selectedBranchNode = searchBranches(selectValue, selectChildren);

        if(selectedBranchNode != null)
        {
            ICodeNode stmtNode = selectedBranchNode.getChildren().get(1);
            StatementExecutor statementExecutor = new StatementExecutor(this);
            statementExecutor.execute(stmtNode);
        }

        ++executionCount;
        return null;
    }
     */

    private ICodeNode searchBranches(Object selectValue, ArrayList<ICodeNode> selectChildren)
    {
        for (int i = 1; i < selectChildren.size(); i++)
        {
            ICodeNode branchNode = selectChildren.get(i);

            if(searchConstants(selectValue, branchNode))
            {
                return branchNode;
            }
        }

        return null;
    }

    private boolean searchConstants(Object selectValue, ICodeNode branchNode)
    {
        boolean integerMode = selectValue instanceof Integer;

        ICodeNode constantsNode = branchNode.getChildren().get(0);
        ArrayList<ICodeNode> constantsList = constantsNode.getChildren();

        if(selectValue instanceof Integer)
        {
            for (ICodeNode constantNode : constantsList)
            {
                int constant = (Integer)constantNode.getAttribute(ICodeKeyImpl.VALUE);
                if((Integer) selectValue == constant)
                {
                    return true; // match
                }
            }
        }
        else
        {
            for (ICodeNode constantNode : constantsList)
            {
                String constant = (String)constantNode.getAttribute(ICodeKeyImpl.VALUE);
                if((String) selectValue == constant)
                {
                    return true; // match
                }
            }
        }

        return false; // not match
    }
}
