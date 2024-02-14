package wci.frontend;

import wci.message.*;

import java.io.BufferedReader;
import java.io.IOException;

public class Source implements MessageProducer
{
    public static final char EOL = '\n';
    public static final char EOF = (char)0;

    private BufferedReader reader;
    private String line;
    private int lineNum;
    private int currentPos;

    private MessageHandler messageHandler;

    public Source(BufferedReader reader)
    {
        this.lineNum = 0;
        this.currentPos = -2;
        this.reader = reader;
        this.messageHandler = new MessageHandler();
    }

    public int getLineNum()
    {
        return lineNum;
    }

    public String getLine()
    {
        return line;
    }

    public int getCurrentPos()
    {
        return currentPos;
    }

    public MessageHandler getMessageHandler()
    {
        return messageHandler;
    }

    public char currentChar() throws Exception
    {
        // First time?
        if(currentPos == -2)
        {
            readLine();
            return nextChar();
        }
        else if (line == null)
        {
            return EOF;
        }
        else if ((currentPos == -1) || (currentPos == line.length()))
        {
            return EOL;
        }
        else if(currentPos > line.length())
        {
            readLine();
            return nextChar();
        }
        else
        {
            return line.charAt(currentPos);
        }
    }

    public char nextChar() throws Exception
    {
        ++currentPos;
        return currentChar();
    }

    public char peekChar() throws Exception
    {
        currentChar(); // update current state
        if(line == null)
            return EOF;
        int nextPos = currentPos + 1;
        return nextPos < line.length() ? line.charAt(nextPos) : EOL;
    }

    public char peekNextChar() throws Exception
    {
        currentChar(); // update current state
        if(line == null)
            return EOF;
        int targetPos = currentPos + 2;
        return targetPos < line.length() ? line.charAt(targetPos) : EOL;
    }

    public void readLine() throws Exception
    {
        line = reader.readLine();
        currentPos = -1;

        if(line != null)
        {
            ++lineNum;
            sendMessage(new Message(MessageType.SOURCE_LINE, new Object[]{lineNum, line}));
        }
    }

    public void close() throws Exception
    {
        if(reader != null)
        {
            try
            {
                reader.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
                throw e;
            }
        }
    }

    @Override
    public void addMessageListener(MessageListener listener)
    {
        messageHandler.addListener(listener);
    }

    @Override
    public void removeMessageListener(MessageListener listener)
    {
        messageHandler.removeListener(listener);
    }

    @Override
    public void sendMessage(Message message)
    {
        messageHandler.sendMessage(message);
    }
}
