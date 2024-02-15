package wci.backend.compiler;

import wci.backend.Backend;
import wci.intermediate.ICode;
import wci.intermediate.SymTab;
import wci.intermediate.SymTabStack;
import wci.message.Message;
import wci.message.MessageType;

public class Executor extends Backend
{
    @Override
    public void process(ICode iCode, SymTabStack symTabStack) throws Exception
    {
        long startTime = System.currentTimeMillis();
        float elapsedTime = (System.currentTimeMillis() - startTime)/1000f;
        int executionCount = 0;
        int runtimeErrors = 0;

        // Send the interpreter summary message.
        sendMessage(new Message(MessageType.INTERPRETER_SUMMARY, new Number[]{
                executionCount, runtimeErrors, elapsedTime
                }));
    }
}
