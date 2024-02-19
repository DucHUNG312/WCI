package wci.frontend.pascal.parsers;

import wci.frontend.Token;
import wci.frontend.TokenType;
import wci.frontend.pascal.PascalErrorCode;
import wci.frontend.pascal.PascalParserTD;
import wci.frontend.pascal.tokens.PascalTokenType;
import wci.intermediate.TypeFactory;
import wci.intermediate.TypeSpec;
import wci.intermediate.symtabimpl.Predefined;
import wci.intermediate.typeimpl.TypeFormImpl;
import wci.intermediate.typeimpl.TypeKeyImpl;

public class SubrangeTypeParser extends PascalParserTD
{
    public SubrangeTypeParser(PascalParserTD parent)
    {
        super(parent);
    }

    public TypeSpec parse(Token token) throws Exception
    {
        TypeSpec subrangeType = TypeFactory.createType(TypeFormImpl.SUBRANGE);
        Object minValue = null;
        Object maxValue = null;
        // Parse the minimum constant.
        Token constantToken = token;
        ConstantDefinitionParser constantParser = new ConstantDefinitionParser(this);
        minValue = constantParser.parseConstant(token);
        // Set the minimum constant's type.
        TypeSpec minType = constantToken.getType() == PascalTokenType.IDENTIFIER
                ? constantParser.getConstantType(constantToken)
                : constantParser.getConstantType(minValue);
        minValue = checkValueType(constantToken, minValue, minType);
        token = currentToken();
        Boolean sawDotDot = false;
        // Look for the .. token.
        if (token.getType() == PascalTokenType.DOT_DOT)
        {
            token = nextToken(); // consume the .. token
            sawDotDot = true;
        }
        TokenType tokenType = token.getType();
        // At the start of the maximum constant?
        if (ConstantDefinitionParser.CONSTANT_START_SET.contains(tokenType))
        {
            if (!sawDotDot)
            {
                errorHandler.flag(token, PascalErrorCode.MISSING_DOT_DOT, this);
            }
            // Parse the maximum constant.
            token = synchronize(ConstantDefinitionParser.CONSTANT_START_SET);
            constantToken = token;
            maxValue = constantParser.parseConstant(token);
            // Set the maximum constant's type.
            TypeSpec maxType = constantToken.getType() == PascalTokenType.IDENTIFIER
                    ? constantParser.getConstantType(constantToken)
                    : constantParser.getConstantType(maxValue);
            maxValue = checkValueType(constantToken, maxValue, maxType);
            // Are the min and max value types valid?
            if ((minType == null) || (maxType == null))
            {
                errorHandler.flag(constantToken, PascalErrorCode.INCOMPATIBLE_TYPES, this);
            }
            // Are the min and max value types the same?
            else if (minType != maxType)
            {
                errorHandler.flag(constantToken, PascalErrorCode.INVALID_SUBRANGE_TYPE, this);
            }
            // Min value > max value?
            else if ((minValue != null) && (maxValue != null) && ((Integer) minValue >= (Integer) maxValue))
            {
                errorHandler.flag(constantToken, PascalErrorCode.MIN_GT_MAX, this);
            }
        }
        else
        {
            errorHandler.flag(constantToken, PascalErrorCode.INVALID_SUBRANGE_TYPE, this);
        }
        subrangeType.setAttribute(TypeKeyImpl.SUBRANGE_BASE_TYPE, minType);
        subrangeType.setAttribute(TypeKeyImpl.SUBRANGE_MIN_VALUE, minValue);
        subrangeType.setAttribute(TypeKeyImpl.SUBRANGE_MAX_VALUE, maxValue);
        return subrangeType;
    }

    private Object checkValueType(Token token, Object value, TypeSpec type)
    {
        if (type == null)
        {
            return value;
        }
        if (type == Predefined.integerType)
        {
            return value;
        }
        else if (type == Predefined.charType)
        {
            char ch = ((String) value).charAt(0);
            return Character.getNumericValue(ch);
        }
        else if (type.getForm() == TypeFormImpl.ENUMERATION)
        {
            return value;
        }
        else
        {
            errorHandler.flag(token, PascalErrorCode.INVALID_SUBRANGE_TYPE, this);
            return value;
        }
    }
}
