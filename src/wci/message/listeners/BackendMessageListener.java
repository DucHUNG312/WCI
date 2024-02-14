package wci.message.listeners;

import wci.message.Message;
import wci.message.MessageListener;
import wci.message.MessageType;

public class BackendMessageListener implements MessageListener
{
    private static final String INTERPRETER_SUMMARY_FORMAT = "\n%,20d statements executed." + "\n%,20d runtime errors." + "\n%,20.2f seconds total execution time.\n";
    private static final String COMPILER_SUMMARY_FORMAT = "\n%,20d instructions generated." + "\n%,20.2f seconds total code generation time.\n";

    public void messageReceived(Message message)
    {
        MessageType type = message.getType();

        switch (type)
        {
            case INTERPRETER_SUMMARY:
            {
                Number body[] = (Number[]) message.getBody();
                int executionCount = (Integer) body[0];
                int runtimeErrors = (Integer) body[1];
                float elapsedTime = (Float) body[2];

                System.out.printf(INTERPRETER_SUMMARY_FORMAT,
                        executionCount, runtimeErrors,
                        elapsedTime);
                break;
            }

            case COMPILER_SUMMARY:
            {
                Number body[] = (Number[]) message.getBody();
                int instructionCount = (Integer) body[0];
                float elapsedTime = (Float) body[1];

                System.out.printf(COMPILER_SUMMARY_FORMAT,
                        instructionCount, elapsedTime);
                break;
            }
        }
    }
}