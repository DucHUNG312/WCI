package wci.frontend;

import wci.intermediate.ICode;
import wci.intermediate.SymTab;
import wci.intermediate.SymTabFactory;
import wci.intermediate.SymTabStack;
import wci.message.Message;
import wci.message.MessageHandler;
import wci.message.MessageListener;
import wci.message.MessageProducer;

public abstract class Parser implements MessageProducer
{
    protected static SymTabStack symTabStack;
    protected static MessageHandler messageHandler;

    static
    {
        symTabStack = SymTabFactory.createSymTabStack();
        messageHandler = new MessageHandler();
    }

    protected Scanner scanner;
    protected ICode iCode;

    protected Parser(Scanner scanner)
    {
        this.scanner = scanner;
        this.iCode = null;
    }

    public Scanner getScanner()
    {
        return scanner;
    }

    public ICode getICode()
    {
        return iCode;
    }

    public static SymTabStack getSymTabStack()
    {
        return symTabStack;
    }

    public static MessageHandler getMessageHandler()
    {
        return messageHandler;
    }

    public abstract void parse() throws Exception;

    public abstract int getErrorCount();

    public Token currentToken()
    {
        return scanner.currentToken();
    }

    public Token nextToken() throws Exception
    {
        return scanner.nextToken();
    }

    public void addMessageListener(MessageListener listener)
    {
        messageHandler.addListener(listener);
    }

    public void removeMessageListener(MessageListener listener)
    {
        messageHandler.removeListener(listener);
    }

    public void sendMessage(Message message)
    {
        messageHandler.sendMessage(message);
    }
}
