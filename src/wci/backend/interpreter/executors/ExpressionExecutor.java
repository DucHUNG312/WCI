package wci.backend.interpreter.executors;

import wci.backend.interpreter.Executor;
import wci.backend.interpreter.RuntimeErrorCode;
import wci.intermediate.ICodeNode;
import wci.intermediate.SymTabEntry;
import wci.intermediate.icodeimpl.ICodeKeyImpl;
import wci.intermediate.icodeimpl.ICodeNodeTypeImpl;
import wci.intermediate.symtabimpl.SymTabKeyImpl;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class ExpressionExecutor extends StatementExecutor
{
    private static final EnumSet<ICodeNodeTypeImpl> ARITH_OPS = EnumSet.of(
            ICodeNodeTypeImpl.ADD,
            ICodeNodeTypeImpl.SUBTRACT,
            ICodeNodeTypeImpl.MULTIPLY,
            ICodeNodeTypeImpl.FLOAT_DIVIDE,
            ICodeNodeTypeImpl.INTEGER_DIVIDE
    );
    public ExpressionExecutor(Executor parent)
    {
        super(parent);
    }

    @Override
    public Object execute(ICodeNode node)
    {
        ICodeNodeTypeImpl nodeType = (ICodeNodeTypeImpl) node.getType();

        switch (nodeType)
        {
            case VARIABLE:
            {
                SymTabEntry entry = (SymTabEntry) node.getAttribute(ICodeKeyImpl.ID);
                return entry.getAttribute(SymTabKeyImpl.DATA_VALUE);
            }
            case INTEGER_CONSTANT:
            {
                // Return the integer value.
                return (Integer) node.getAttribute(ICodeKeyImpl.VALUE);
            }
            case REAL_CONSTANT:
            {
                // Return the float value.
                return (Float) node.getAttribute(ICodeKeyImpl.VALUE);
            }
            case STRING_CONSTANT:
            {
                // Return the string value.
                return (String) node.getAttribute(ICodeKeyImpl.VALUE);
            }
            case NEGATE:
            {
                ArrayList<ICodeNode> children = node.getChildren();
                ICodeNode expressionNode = children.get(0);

                Object value = execute(expressionNode);
                if(value instanceof Integer)
                {
                    return -((Integer)value);
                }
                else
                {
                    return -((Float)value);
                }
            }
            case NOT:
            {
                ArrayList<ICodeNode> children = node.getChildren();
                ICodeNode expressionNode = children.get(0);

                // Execute the expression and return the "not" of its value.
                boolean value = (Boolean) execute(expressionNode);
                return !value;
            }
            default:
                return executeBinaryOperator(node, nodeType);
        }
    }

    private Object executeBinaryOperator(ICodeNode node, ICodeNodeTypeImpl nodeType)
    {
        List<ICodeNode> children = node.getChildren();
        ICodeNode operandNode1 = children.get(0);
        ICodeNode operandNode2 = children.get(1);

        Object operand1 = execute(operandNode1);
        Object operand2 = execute(operandNode2);

        boolean integerMode = (operand1 instanceof Integer) && (operand2 instanceof Integer);

        if(ARITH_OPS.contains(nodeType))
        {
            if(integerMode)
            {
                int value1 = (Integer) operand1;
                int value2 = (Integer) operand2;

                switch (nodeType)
                {
                    case ADD: return value1 + value2;
                    case SUBTRACT: return value1 - value2;
                    case MULTIPLY: return value1 * value2;
                    case FLOAT_DIVIDE:
                    {
                        // Check for division by zero.
                        if (value2 != 0)
                        {
                            return ((float) value1)/((float) value2);
                        }
                        else
                        {
                            errorHandler.flag(node, RuntimeErrorCode.DIVISION_BY_ZERO, this);
                            return 0;
                        }
                    }
                    case INTEGER_DIVIDE:
                    {
                        // Check for division by zero.
                        if (value2 != 0)
                        {
                            return value1/value2;
                        }
                        else
                        {
                            errorHandler.flag(node, RuntimeErrorCode.DIVISION_BY_ZERO, this);
                            return 0;
                        }
                    }
                    case MOD:
                    {
                        // Check for division by zero.
                        if (value2 != 0)
                        {
                            return value1%value2;
                        }
                        else
                        {
                            errorHandler.flag(node, RuntimeErrorCode.DIVISION_BY_ZERO, this);
                            return 0;
                        }
                    }
                }
            }
            else
            {
                float value1 = operand1 instanceof Integer ? (Integer) operand1 : (Float) operand1;
                float value2 = operand2 instanceof Integer ? (Integer) operand2 : (Float) operand2;

                // Float operations.
                switch (nodeType)
                {
                    case ADD: return value1 + value2;
                    case SUBTRACT: return value1 - value2;
                    case MULTIPLY: return value1 * value2;
                    case FLOAT_DIVIDE:
                    {
                        // Check for division by zero.
                        if (value2 != 0.0f)
                        {
                            return value1/value2;
                        }
                        else
                        {
                            errorHandler.flag(node, RuntimeErrorCode.DIVISION_BY_ZERO, this);
                            return 0.0f;
                        }
                    }
                }
            }
        }
        else if ((nodeType == ICodeNodeTypeImpl.AND) || (nodeType == ICodeNodeTypeImpl.OR))
        {
            boolean value1 = (Boolean) operand1;
            boolean value2 = (Boolean) operand2;
            switch (nodeType)
            {
                case AND: return value1 && value2;
                case OR: return value1 || value2;
            }
        }
        else if (integerMode)
        {
            int value1 = (Integer) operand1;
            int value2 = (Integer) operand2;
            // Integer operands.
            switch (nodeType)
            {
                case EQ: return value1 == value2;
                case NE: return value1 != value2;
                case LT: return value1 < value2;
                case LE: return value1 <= value2;
                case GT: return value1 > value2;
                case GE: return value1 >= value2;
            }
        }
        else
        {
            float value1 = operand1 instanceof Integer ? (Integer) operand1 : (Float) operand1;
            float value2 = operand2 instanceof Integer ? (Integer) operand2 : (Float) operand2;
            // Float operands.
            switch (nodeType)
            {
                case EQ: return value1 == value2;
                case NE: return value1 != value2;
                case LT: return value1 < value2;
                case LE: return value1 <= value2;
                case GT: return value1 > value2;
                case GE: return value1 >= value2;
            }
        }
        return 0; // should never get here
    }
}
