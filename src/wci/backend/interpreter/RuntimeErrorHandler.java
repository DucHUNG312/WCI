package wci.backend.interpreter;

import wci.backend.Backend;
import wci.intermediate.ICodeNode;
import wci.intermediate.icodeimpl.ICodeKeyImpl;
import wci.message.Message;
import wci.message.MessageType;

public class RuntimeErrorHandler
{
    private static final int MAX_ERRORS = 5;
    private static int errorCount = 0;

    public static int getErrorCount()
    {
        return errorCount;
    }

    public static int getMaxErrors()
    {
        return MAX_ERRORS;
    }

    public void flag(ICodeNode node, RuntimeErrorCode errorCode, Backend backend)
    {
        String lineNum = null;

        while ((node != null) && (node.getAttribute(ICodeKeyImpl.LINE) == null)) {
            node = node.getParent();
        }

        backend.sendMessage(new Message(MessageType.RUNTIME_ERROR, new Object[]{
                errorCode.toString(),
                (Integer) node.getAttribute(ICodeKeyImpl.LINE)
        }));

        if (++errorCount > MAX_ERRORS) {
            System.out.println("*** ABORTED AFTER TOO MANY RUNTIME ERRORS.");
            System.exit(-1);
        }
    }
}
