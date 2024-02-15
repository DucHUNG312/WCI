package wci;

import wci.backend.Backend;
import wci.backend.BackendFactory;
import wci.frontend.FrontendFactory;
import wci.frontend.Parser;
import wci.frontend.Source;
import wci.intermediate.ICode;
import wci.intermediate.SymTab;
import wci.intermediate.SymTabStack;
import wci.message.listeners.BackendMessageListener;
import wci.message.listeners.ParserMessageListener;
import wci.message.listeners.SourceMessageListener;
import wci.util.CrossReferencer;

import java.io.BufferedReader;
import java.io.FileReader;

public class Pascal
{
    private static final String FLAGS = "[-ix]";
    private static final String USAGE = "Usage: Pascal execute|compile " + FLAGS + " <source file path>";

    private Parser parser;
    private Source source;
    private ICode iCode;
    private SymTabStack symTabStack;
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
            symTabStack = parser.getSymTabStack();

            if(xref)
            {
                CrossReferencer crossReferencer = new CrossReferencer();
                crossReferencer.print(symTabStack);
            }

            backend.process(iCode, symTabStack);
        }
        catch (Exception e)
        {
            System.out.println("***** Internal translator error. *****");
            e.printStackTrace();
        }
    }


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
}
