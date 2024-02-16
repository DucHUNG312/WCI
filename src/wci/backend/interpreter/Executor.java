package wci.backend.interpreter;

import wci.backend.Backend;
import wci.backend.interpreter.executors.StatementExecutor;
import wci.frontend.pascal.parsers.StatementParser;
import wci.intermediate.ICode;
import wci.intermediate.ICodeNode;
import wci.intermediate.SymTabStack;
import wci.message.Message;
import wci.message.MessageType;

public class Executor extends Backend
{
    protected static int executionCount;
    protected static RuntimeErrorHandler errorHandler;
    static
    {
        executionCount = 0;
        errorHandler = new RuntimeErrorHandler();
    }

    public Executor(){}

    public Executor(Executor parent)
    {
        super();
    }

    public static int getExecutionCount()
    {
        return executionCount;
    }

    public static RuntimeErrorHandler getErrorHandler()
    {
        return errorHandler;
    }

    @Override
    public void process(ICode iCode, SymTabStack symTabStack) throws Exception
    {
        this.symTabStack = symTabStack;
        this.iCode = iCode;

        long startTime = System.currentTimeMillis();

        ICodeNode rootNode = iCode.getRoot();
        StatementExecutor statementExecutor = new StatementExecutor(this);
        statementExecutor.execute(rootNode);

        float elapsedTime = (System.currentTimeMillis() - startTime)/1000f;
        int runtimeErrors = errorHandler.getErrorCount();

        // Send the interpreter summary message.
        sendMessage(new Message(MessageType.INTERPRETER_SUMMARY, new Number[]{
                executionCount, runtimeErrors, elapsedTime
                }));
    }
}
