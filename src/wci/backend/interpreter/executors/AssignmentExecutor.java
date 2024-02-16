package wci.backend.interpreter.executors;

import wci.backend.interpreter.Executor;
import wci.intermediate.ICodeNode;
import wci.intermediate.SymTabEntry;
import wci.intermediate.icodeimpl.ICodeKeyImpl;
import wci.intermediate.symtabimpl.SymTabKeyImpl;
import wci.message.Message;
import wci.message.MessageType;

import java.util.ArrayList;

public class AssignmentExecutor extends StatementExecutor
{
    public AssignmentExecutor(Executor parent)
    {
        super(parent);
    }

    @Override
    public Object execute(ICodeNode node)
    {
        ArrayList<ICodeNode> children = node.getChildren();
        ICodeNode variableNode = children.get(0);
        ICodeNode expressionNode = children.get(1);

        ExpressionExecutor expressionExecutor = new ExpressionExecutor(this);
        Object value = expressionExecutor.execute(expressionNode);

        // Set the value as an attribute of the variable&apos;s symbol table entry.
        SymTabEntry variableId = (SymTabEntry) variableNode.getAttribute(ICodeKeyImpl.ID);
        variableId.setAttribute(SymTabKeyImpl.DATA_VALUE, value);

        sendMessage(node, variableId.getName(), value);

        ++executionCount;
        return null;
    }

    private void sendMessage(ICodeNode node, String variableName, Object value)
    {
        Object lineNumber = node.getAttribute(ICodeKeyImpl.LINE);

        // Send an ASSIGN message.
        if (lineNumber != null)
        {
            sendMessage(new Message(MessageType.ASSIGN, new Object[] {
                    lineNumber,
                    variableName,
                    value}));
        }
    }
}
