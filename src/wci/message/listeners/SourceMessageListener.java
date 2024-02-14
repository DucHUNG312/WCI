package wci.message.listeners;

import wci.message.Message;
import wci.message.MessageListener;
import wci.message.MessageType;

public class SourceMessageListener implements MessageListener
{
    private static final String SOURCE_LINE_FORMAT = "%03d %s";
    @Override
    public void messageReceived(Message message)
    {
        MessageType type = message.getType();
        Object body[] = (Object[]) message.getBody();

        switch (type)
        {
            case SOURCE_LINE:
            {
                int lineNumber = (Integer) body[0];
                String lineText = (String) body[1];

                System.out.println(String.format(SOURCE_LINE_FORMAT, lineNumber, lineText));
                break;
            }
        }
    }
}