package wci.backend;

import wci.intermediate.ICode;
import wci.intermediate.SymTab;
import wci.message.Message;
import wci.message.MessageHandler;
import wci.message.MessageListener;
import wci.message.MessageProducer;

public abstract class Backend implements MessageProducer
{
    protected static MessageHandler messageHandler;
    protected SymTab symTab;
    protected ICode iCode;

    public SymTab getSymTab()
    {
        return symTab;
    }

    public ICode getICode()
    {
        return iCode;
    }

    public static MessageHandler getMessageHandler()
    {
        return messageHandler;
    }

    public abstract void process(ICode iCode, SymTab symTab) throws Exception;

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
