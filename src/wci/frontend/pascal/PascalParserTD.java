package wci.frontend.pascal;

import wci.frontend.*;
import wci.frontend.pascal.parsers.BlockParser;
import wci.frontend.pascal.parsers.StatementParser;
import wci.intermediate.ICode;
import wci.intermediate.ICodeFactory;
import wci.intermediate.ICodeNode;
import wci.intermediate.SymTabEntry;
import wci.intermediate.symtabimpl.DefinitionImpl;
import wci.intermediate.symtabimpl.Predefined;
import wci.intermediate.symtabimpl.SymTabKeyImpl;
import wci.message.Message;

import static wci.frontend.pascal.tokens.PascalTokenType.*;
import static wci.frontend.pascal.PascalErrorCode.*;
import static wci.message.MessageType.*;

import java.io.IOException;
import java.util.EnumSet;

public class PascalParserTD extends Parser
{
    protected static PascalErrorHandler errorHandler = new PascalErrorHandler();
    private SymTabEntry routineId; // name of the routine being parsed

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
        ICode iCode = ICodeFactory.createICode();

        Predefined.initialize(symTabStack);

        // Create a dummy program identifier symbol table entry
        routineId = symTabStack.enterLocal("DummyProgramName".toLowerCase());
        routineId.setDefinition(DefinitionImpl.PROGRAM);
        symTabStack.setProgramId(routineId);

        // Push a new symbol table onto the symbol table stack and set
        // the routine/s symbol table and intermediate code.
        routineId.setAttribute(SymTabKeyImpl.ROUTINE_SYMTAB, symTabStack.push());
        routineId.setAttribute(SymTabKeyImpl.ROUTINE_ICODE, iCode);
        BlockParser blockParser = new BlockParser(this);

        try
        {
            Token token = nextToken();
            ICodeNode rootNode = blockParser.parse(token, routineId);
            iCode.setRoot(rootNode);
            symTabStack.pop();

            token = currentToken();
            if(token.getType() != DOT)
            {
                errorHandler.flag(token, MISSING_PERIOD, this);
            }
            token = currentToken(); // consume dot

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

    public Token synchronize(EnumSet syncSet) throws Exception
    {
        Token token = currentToken();

        if(!syncSet.contains(token.getType()))
        {
            errorHandler.flag(token, UNEXPECTED_TOKEN, this);

            do
            {
                token = nextToken();
            } while (!(token instanceof EofToken) && !syncSet.contains(token.getType()));
        }

        return token;
    }
}
