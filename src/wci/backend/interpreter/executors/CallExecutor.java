package wci.backend.interpreter.executors;

import wci.intermediate.*;
import wci.intermediate.symtabimpl.*;
import wci.backend.interpreter.*;

import static wci.intermediate.symtabimpl.SymTabKeyImpl.*;
import static wci.intermediate.symtabimpl.RoutineCodeImpl.*;
import static wci.intermediate.icodeimpl.ICodeKeyImpl.*;

public class CallExecutor extends StatementExecutor
{
    /**
     * Constructor.
     * @param the parent executor.
     */
    public CallExecutor(Executor parent)
    {
        super(parent);
    }

    /**
     * Execute procedure or function call statement.
     * @param node the root node of the call.
     * @return null.
     */
    public Object execute(ICodeNode node)
    {
        SymTabEntry routineId = (SymTabEntry) node.getAttribute(ID);
        RoutineCode routineCode =
                (RoutineCode) routineId.getAttribute(ROUTINE_CODE);
        CallExecutor callExecutor = routineCode == DECLARED
                ? new CallDeclaredExecutor(this)
                : new CallStandardExecutor(this);

        ++executionCount;  // count the call statement
        return callExecutor.execute(node);
    }
}
