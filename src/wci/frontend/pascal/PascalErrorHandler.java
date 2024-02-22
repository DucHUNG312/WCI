package wci.frontend.pascal;

import wci.frontend.Parser;
import wci.frontend.Token;
import wci.message.Message;
import wci.message.MessageType;

public class PascalErrorHandler
{
    private static final int MAX_ERRORS = 25;
    private static int errorCount = 0;

    public static int getErrorCount()
    {
        return errorCount;
    }

    public static int getMaxErrors()
    {
        return MAX_ERRORS;
    }

    public void flag(Token token, PascalErrorCode errorCode, Parser parser)
    {
        parser.sendMessage(new Message(MessageType.SYNTAX_ERROR, new Object[]{
                token.getLineNumber(),
                token.getPosition(),
                token.getText(),
                errorCode.toString()
        }));

        if(++errorCount > MAX_ERRORS)
        {
            abortTranslation(PascalErrorCode.TOO_MANY_ERRORS, parser);
        }
    }

    public void abortTranslation(PascalErrorCode errorCode, Parser parser)
    {
        String fatalText = "FATAL ERROR: " + errorCode.toString();
        parser.sendMessage(new Message(MessageType.SYNTAX_ERROR, new Object[] {
                0,
                0,
                "",
                fatalText}));
        System.exit(errorCode.getStatus());
    }
}
