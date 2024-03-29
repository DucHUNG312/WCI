package wci.backend.interpreter;

import java.io.*;

import wci.frontend.*;
import wci.frontend.pascal.*;
import wci.intermediate.*;
import wci.intermediate.icodeimpl.*;
import wci.backend.*;
import wci.backend.interpreter.*;
import wci.backend.interpreter.executors.*;
import wci.message.*;

import static wci.intermediate.icodeimpl.ICodeNodeTypeImpl.*;
import static wci.intermediate.icodeimpl.ICodeKeyImpl.*;
import static wci.message.MessageType.INTERPRETER_SUMMARY;

public class Executor extends Backend
{
    protected static int executionCount;
    protected static RuntimeStack runtimeStack;
    protected static RuntimeErrorHandler errorHandler;

    protected static Scanner standardIn;       // standard input
    protected static PrintWriter standardOut;  // standard output

    static {
        executionCount = 0;
        runtimeStack = MemoryFactory.createRuntimeStack();
        errorHandler = new RuntimeErrorHandler();

        try {
            standardIn = new PascalScanner(
                             new Source(
                                 new BufferedReader(
                                     new InputStreamReader(System.in))));
            standardOut = new PrintWriter(
                              new PrintStream(System.out));
        }
        catch (IOException ignored) {
        }
    }

    /**
     * Constructor.
     */
    public Executor() {}

    /**
     * Constructor for subclasses.
     * @param the parent executor.
     */
    public Executor(Executor parent)
    {
        super();
    }

    /**
     * Getter.
     * @return the error handler.
     */
    public RuntimeErrorHandler getErrorHandler()
    {
        return errorHandler;
    }

    /**
     * Execute the source program by processing the intermediate code
     * and the symbol table stack generated by the parser.
     * @param iCode the intermediate code.
     * @param symTabStack the symbol table stack.
     * @throws Exception if an error occurred.
     */
    public void process(ICode iCode, SymTabStack symTabStack)
        throws Exception
    {
        this.symTabStack = symTabStack;
        long startTime = System.currentTimeMillis();

        SymTabEntry programId = symTabStack.getProgramId();

        // Construct an artificial CALL node to the main program.
        ICodeNode callNode = ICodeFactory.createICodeNode(CALL);
        callNode.setAttribute(ID, programId);

        // Execute the main program.
        CallDeclaredExecutor callExecutor = new CallDeclaredExecutor(this);
        callExecutor.execute(callNode);

        float elapsedTime = (System.currentTimeMillis() - startTime)/1000f;
        int runtimeErrors = errorHandler.getErrorCount();

        // Send the interpreter summary message.
        sendMessage(new Message(INTERPRETER_SUMMARY,
                                new Number[] {executionCount,
                                              runtimeErrors,
                                              elapsedTime}));
    }
}
