package wci;

import wci.backend.Backend;
import wci.backend.BackendFactory;
import wci.frontend.FrontendFactory;
import wci.frontend.Parser;
import wci.frontend.Source;
import wci.intermediate.ICode;
import wci.intermediate.SymTab;
import wci.message.Message;
import wci.message.MessageListener;
import wci.message.MessageType;

import java.io.BufferedReader;
import java.io.FileReader;

public class Pascal
{
    private Parser parser;
    private Source source;
    private ICode iCode;
    private SymTab symTab;
    private Backend backend;

    public Pascal(String operation, String filePath, String flags)
    {
        try
        {
            boolean intermediate = flags.indexOf('i') > -1;
            boolean xref         = flags.indexOf('x') > -1;

            source = new Source(new BufferedReader(new FileReader(filePath)));
            source.addMessageListener(new SourceMessageListener());

            parser = FrontendFactory.createParser("Pascal", "top-down", source);
            parser.addMessageListener(new ParserMessageListener());

            backend = BackendFactory.createBackend(operation);
            backend.addMessageListener(new BackendMessageListener());

            parser.parse();
            source.close();

            iCode = parser.getICode();
            symTab = parser.getSymTab();

            backend.process(iCode, symTab);
        }
        catch (Exception e)
        {
            System.out.println("***** Internal translator error. *****");
            e.printStackTrace();
        }
    }

    private static final String FLAGS = "[-ix]";
    private static final String USAGE = "Usage: Pascal execute|compile " + FLAGS + " <source file path>";

    public static void main(String args[])
    {
        try
        {
            String operation = args[0];

            if (!(   operation.equalsIgnoreCase("compile") || operation.equalsIgnoreCase("execute")))
            {
                throw new Exception();
            }

            int i = 0;
            String flags = "";

            while ((++i < args.length) && (args[i].charAt(0) == '-'))
            {
                flags += args[i].substring(1);
            }

            if (i < args.length)
            {
                String path = args[i];
                new Pascal(operation, path, flags);
            }
            else
            {
                throw new Exception();
            }
        }
        catch (Exception e)
        {
            System.out.println(USAGE);
        }
    }

    private static final String SOURCE_LINE_FORMAT = "%03d %s";

    private class SourceMessageListener implements MessageListener
    {
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

    private static final String PARSER_SUMMARY_FORMAT =
            "\n%,20d source lines." +
            "\n%,20d syntax errors." +
            "\n%,20.2f seconds total parsing time.\n";

    private class ParserMessageListener implements MessageListener
    {
        /**
         * Called by the parser whenever it produces a message.
         * @param message the message.
         */
        public void messageReceived(Message message)
        {
            MessageType type = message.getType();

            switch (type)
            {
                case PARSER_SUMMARY:
                {
                    Number body[] = (Number[]) message.getBody();
                    int statementCount = (Integer) body[0];
                    int syntaxErrors = (Integer) body[1];
                    float elapsedTime = (Float) body[2];

                    System.out.printf(PARSER_SUMMARY_FORMAT,
                            statementCount, syntaxErrors,
                            elapsedTime);
                    break;
                }
            }
        }
    }

    private static final String INTERPRETER_SUMMARY_FORMAT =
            "\n%,20d statements executed." +
            "\n%,20d runtime errors." +
            "\n%,20.2f seconds total execution time.\n";

    private static final String COMPILER_SUMMARY_FORMAT =
            "\n%,20d instructions generated." +
            "\n%,20.2f seconds total code generation time.\n";

    private class BackendMessageListener implements MessageListener
    {
        /**
         * Called by the back end whenever it produces a message.
         * @param message the message.
         */
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
}