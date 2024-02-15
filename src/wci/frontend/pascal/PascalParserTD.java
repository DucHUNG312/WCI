package wci.frontend.pascal;

import wci.frontend.*;
import wci.frontend.pascal.parsers.StatementParser;
import wci.intermediate.ICodeFactory;
import wci.intermediate.ICodeNode;
import wci.intermediate.SymTabEntry;
import wci.message.Message;

import static wci.frontend.pascal.tokens.PascalTokenType.*;
import static wci.frontend.pascal.PascalErrorCode.*;
import static wci.message.MessageType.*;

import java.io.IOException;

public class PascalParserTD extends Parser
{
    protected static PascalErrorHandler errorHandler = new PascalErrorHandler();

    /**
     * Constructor.
     * @param scanner the scanner to be used with this parser.
     */
    public PascalParserTD(Scanner scanner)
    {
        super(scanner);
    }

    public PascalParserTD(PascalParserTD parent)
    {
        super(parent.getScanner());
    }

    /**
     * Parse a Pascal source program and generate the symbol table
     * and the intermediate code.
     */
    public void parse() throws Exception
    {
        long startTime = System.currentTimeMillis();
        iCode = ICodeFactory.createICode();

        try
        {
            Token token = nextToken();
            ICodeNode rootNode = null;

            if(token.getType() == BEGIN)
            {
                StatementParser statementParser = new StatementParser(this);
                rootNode = statementParser.parse(token);
                token = currentToken();
            }
            else
            {
                errorHandler.flag(token, UNEXPECTED_TOKEN, this);
            }

            if(token.getType() != DOT)
            {
                errorHandler.flag(token, MISSING_PERIOD, this);
            }
            token = currentToken(); // consume dot

            if(rootNode != null)
            {
                iCode.setRoot(rootNode);
            }

            // Send the parser summary message.
            float elapsedTime = (System.currentTimeMillis() - startTime)/1000f;
            sendMessage(new Message(PARSER_SUMMARY,
                    new Number[] {token.getLineNum(),
                            getErrorCount(),
                            elapsedTime}));
        }
        catch (java.io.IOException ex)
        {
            errorHandler.abortTranslation(IO_ERROR, this);
        }
    }

    /**
     * Return the number of syntax errors found by the parser.
     * @return the error count.
     */
    public int getErrorCount()
    {
        return errorHandler.getErrorCount();
    }

    public static PascalErrorHandler getErrorHandler()
    {
        return errorHandler;
    }
}
