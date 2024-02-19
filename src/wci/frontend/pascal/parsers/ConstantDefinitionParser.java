package wci.frontend.pascal.parsers;

import wci.frontend.Token;
import wci.frontend.TokenType;
import wci.frontend.pascal.PascalErrorCode;
import wci.frontend.pascal.PascalParserTD;
import wci.frontend.pascal.tokens.PascalTokenType;
import wci.intermediate.Definition;
import wci.intermediate.SymTabEntry;
import wci.intermediate.TypeFactory;
import wci.intermediate.TypeSpec;
import wci.intermediate.symtabimpl.DefinitionImpl;
import wci.intermediate.symtabimpl.Predefined;
import wci.intermediate.symtabimpl.SymTabKeyImpl;

import java.util.EnumSet;

public class ConstantDefinitionParser extends PascalParserTD
{
    // Synchronization set for a constant identifier.
    private static final EnumSet<PascalTokenType> IDENTIFIER_SET = DeclarationParser.TYPE_START_SET.clone();
    static
    {
        IDENTIFIER_SET.add(PascalTokenType.IDENTIFIER);
    }
    // Synchronization set for starting a constant.
    static final EnumSet<PascalTokenType> CONSTANT_START_SET = EnumSet.of(
            PascalTokenType.IDENTIFIER,
            PascalTokenType.INTEGER,
            PascalTokenType.REAL,
            PascalTokenType.PLUS,
            PascalTokenType.MINUS,
            PascalTokenType.STRING,
            PascalTokenType.SEMICOLON
    );
    // Synchronization set for the = token.
    private static final EnumSet<PascalTokenType> EQUALS_SET = CONSTANT_START_SET.clone();
    static
    {
        EQUALS_SET.add(PascalTokenType.EQUALS);
        EQUALS_SET.add(PascalTokenType.SEMICOLON);
    }
    // Synchronization set for the start of the next definition or declaration.
    private static final EnumSet<PascalTokenType> NEXT_START_SET = DeclarationParser.TYPE_START_SET.clone();
    static
    {
        NEXT_START_SET.add(PascalTokenType.SEMICOLON);
        NEXT_START_SET.add(PascalTokenType.IDENTIFIER);
    }
    public ConstantDefinitionParser(PascalParserTD parent)
    {
        super(parent);
    }

    public void parse(Token token) throws Exception
    {
        token = synchronize(IDENTIFIER_SET);

        while (token.getType() == PascalTokenType.IDENTIFIER)
        {
            String name = token.getText().toLowerCase();
            SymTabEntry constantId = symTabStack.lookupLocal(name);

            if (constantId == null)
            {
                constantId = symTabStack.enterLocal(name);
                constantId.appendLineNumber(token.getLineNum());
            }
            else
            {
                errorHandler.flag(token, PascalErrorCode.IDENTIFIER_REDEFINED, this);
                constantId = null;
            }

            token = nextToken(); // consume identifier token

            token = synchronize(EQUALS_SET);
            if (token.getType() == PascalTokenType.EQUALS)
            {
                token = nextToken(); // consume the =
            }
            else
            {
                errorHandler.flag(token, PascalErrorCode.MISSING_EQUALS, this);
            }

            // Parse the constant value.
            Token constantToken = token;
            Object value = parseConstant(token);

            // Set identifier to be a constant and set its value.
            if (constantId != null)
            {
                constantId.setDefinition(DefinitionImpl.CONSTANT);
                constantId.setAttribute(SymTabKeyImpl.CONSTANT_VALUE, value);
                // Set the constant's type.
                TypeSpec constantType = constantToken.getType() == PascalTokenType.IDENTIFIER
                        ? getConstantType(constantToken)
                        : getConstantType(value);
                constantId.setTypeSpec(constantType);

                token = currentToken();
                TokenType tokenType = token.getType();

                // Look for one or more semicolons after a definition.
                if (tokenType == PascalTokenType.SEMICOLON)
                {
                    while (token.getType() == PascalTokenType.SEMICOLON)
                    {
                        token = nextToken(); // consume the SEMICOLON
                    }
                }
                // If at the start of the next definition or declaration,
                // then missing a semicolon.
                else if (NEXT_START_SET.contains(tokenType))
                {
                    errorHandler.flag(token, PascalErrorCode.MISSING_SEMICOLON, this);
                }
                token = synchronize(IDENTIFIER_SET);
            }
        }
    }

    protected Object parseConstant(Token token) throws Exception
    {
        TokenType sign = null;
        // Synchronize at the start of a constant.
        token = synchronize(CONSTANT_START_SET);
        TokenType tokenType = token.getType();
        // Plus or minus sign?
        if ((tokenType == PascalTokenType.PLUS) || (tokenType == PascalTokenType.MINUS))
        {
            sign = tokenType;
            token = nextToken(); // consume sign
        }
        // Parse the constant.
        switch ((PascalTokenType) token.getType())
        {
            case IDENTIFIER:
            {
                return parseIdentifierConstant(token, sign);
            }
            case INTEGER:
            {
                Integer value = (Integer) token.getValue();
                nextToken(); // consume the number
                return sign == PascalTokenType.MINUS ? -value : value;
            }
            case REAL:
            {
                Float value = (Float) token.getValue();
                nextToken(); // consume the number
                return sign == PascalTokenType.MINUS ? -value : value;
            }
            case STRING:
            {
                if (sign != null)
                {
                    errorHandler.flag(token, PascalErrorCode.INVALID_CONSTANT, this);
                }
                nextToken(); // consume the string
                return (String) token.getValue();
            }
            default:
            {
                errorHandler.flag(token, PascalErrorCode.INVALID_CONSTANT, this);
                return null;
            }
        }
    }

    protected Object parseIdentifierConstant(Token token, TokenType sign) throws Exception
    {
        String name = token.getText();
        SymTabEntry id = symTabStack.lookup(name);
        nextToken(); // consume the identifier
        // The identifier must have already been defined
        // as an constant identifier.
        if (id == null)
        {
            errorHandler.flag(token, PascalErrorCode.IDENTIFIER_UNDEFINED, this);
            return null;
        }
        Definition definition = id.getDefinition();
        if (definition == DefinitionImpl.CONSTANT)
        {
            Object value = id.getAttribute(SymTabKeyImpl.CONSTANT_VALUE);
            id.appendLineNumber(token.getLineNum());
            if (value instanceof Integer)
            {
                return sign == PascalTokenType.MINUS ? -((Integer) value) : value;
            }
            else if (value instanceof Float)
            {
                return sign == PascalTokenType.MINUS ? -((Float) value) : value;
            }
            else if (value instanceof String)
            {
                if (sign != null)
                {
                    errorHandler.flag(token, PascalErrorCode.INVALID_CONSTANT, this);
                }
                return value;
            }
            else
            {
                return null;
            }
        }
        else if (definition == DefinitionImpl.ENUMERATION_CONSTANT)
        {
            Object value = id.getAttribute(SymTabKeyImpl.CONSTANT_VALUE);
            id.appendLineNumber(token.getLineNum());
            if (sign != null)
            {
                errorHandler.flag(token, PascalErrorCode.INVALID_CONSTANT, this);
            }
            return value;
        }
        else if (definition == null)
        {
            errorHandler.flag(token, PascalErrorCode.NOT_CONSTANT_IDENTIFIER, this);
            return null;
        }
        else
        {
            errorHandler.flag(token, PascalErrorCode.INVALID_CONSTANT, this);
            return null;
        }
    }

    protected TypeSpec getConstantType(Object value)
    {
        TypeSpec constantType = null;
        if (value instanceof Integer)
        {
            constantType = Predefined.integerType;
        }
        else if (value instanceof Float)
        {
            constantType = Predefined.realType;
        }
        else if (value instanceof String)
        {
            if (((String) value).length() == 1) {
                constantType = Predefined.charType;
            }
            else
            {
                constantType = TypeFactory.createStringType((String) value);
            }
        }
        return constantType;
    }

    protected TypeSpec getConstantType(Token identifier)
    {
        SymTabEntry id = symTabStack.lookup(identifier.getText());
        if (id == null)
        {
            return null;
        }
        Definition definition = id.getDefinition();
        if ((definition == DefinitionImpl.CONSTANT) || (definition == DefinitionImpl.ENUMERATION_CONSTANT))
        {
            return id.getTypeSpec();
        }
        else
        {
            return null;
        }
    }
}
