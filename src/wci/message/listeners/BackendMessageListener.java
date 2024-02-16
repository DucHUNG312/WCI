package wci.message.listeners;

import wci.message.Message;
import wci.message.MessageListener;
import wci.message.MessageType;

public class BackendMessageListener implements MessageListener
{
    private boolean firstOutputMessage = true;
    private static final String ASSIGN_FORMAT = " >>> LINE %03d: %s = %s\n";
    private static final String INTERPRETER_SUMMARY_FORMAT = "\n%,20d statements executed." + "\n%,20d runtime errors." + "\n%,20.2f seconds total execution time.\n";
    private static final String COMPILER_SUMMARY_FORMAT = "\n%,20d instructions generated." + "\n%,20.2f seconds total code generation time.\n";

    public void messageReceived(Message message)
    {
        MessageType type = message.getType();

        switch (type)
        {
            case ASSIGN:
            {
                if (firstOutputMessage)
                {
                    System.out.println("\n===== OUTPUT =====\n");
                    firstOutputMessage = false;
                }
                Object body[] = (Object[]) message.getBody();
                int lineNumber = (Integer) body[0];
                String variableName = (String) body[1];
                Object value = body[2];
                System.out.printf(ASSIGN_FORMAT, lineNumber, variableName, value);
                break;
            }
            case RUNTIME_ERROR:
            {
                Object body[] = (Object []) message.getBody();
                String errorMessage = (String) body[0];
                Integer lineNumber = (Integer) body[1];
                System.out.print("*** RUNTIME ERROR");
                if (lineNumber != null)
                {
                    System.out.print(" AT LINE " + String.format("%03d", lineNumber));
                }
                System.out.println(": " + errorMessage);
                break;
            }
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