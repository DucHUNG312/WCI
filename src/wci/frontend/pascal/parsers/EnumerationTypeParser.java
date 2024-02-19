package wci.frontend.pascal.parsers;

import wci.frontend.Token;
import wci.frontend.TokenType;
import wci.frontend.pascal.PascalErrorCode;
import wci.frontend.pascal.PascalParserTD;
import wci.frontend.pascal.tokens.PascalTokenType;
import wci.intermediate.SymTabEntry;
import wci.intermediate.TypeFactory;
import wci.intermediate.TypeSpec;
import wci.intermediate.symtabimpl.DefinitionImpl;
import wci.intermediate.symtabimpl.SymTabKeyImpl;
import wci.intermediate.typeimpl.TypeFormImpl;
import wci.intermediate.typeimpl.TypeKeyImpl;

import java.util.ArrayList;
import java.util.EnumSet;

public class EnumerationTypeParser extends PascalParserTD
{
    // Synchronization set to start an enumeration constant.
    private static final EnumSet<PascalTokenType> ENUM_CONSTANT_START_SET = EnumSet.of(
            PascalTokenType.IDENTIFIER,
            PascalTokenType.COMMA);
    // Synchronization set to follow an enumeration definition.
    private static final EnumSet<PascalTokenType> ENUM_DEFINITION_FOLLOW_SET = EnumSet.of(
            PascalTokenType.RIGHT_PAREN,
            PascalTokenType.SEMICOLON);
    static
    {
        ENUM_DEFINITION_FOLLOW_SET.addAll(DeclarationParser.VAR_START_SET);
    }
    public EnumerationTypeParser(PascalParserTD parent)
    {
        super(parent);
    }

    public TypeSpec parse(Token token) throws Exception
    {
        TypeSpec enumerationType = TypeFactory.createType(TypeFormImpl.ENUMERATION);
        int value = -1;
        ArrayList<SymTabEntry> constants = new ArrayList<SymTabEntry>();
        token = nextToken(); // consume the opening (
        do
        {
            token = synchronize(ENUM_CONSTANT_START_SET);
            parseEnumerationIdentifier(token, ++value, enumerationType, constants);
            token = currentToken();
            TokenType tokenType = token.getType();
            // Look for the comma.
            if (tokenType == PascalTokenType.COMMA)
            {
                token = nextToken(); // consume the comma
                if (ENUM_DEFINITION_FOLLOW_SET.contains(token.getType()))
                {
                    errorHandler.flag(token, PascalErrorCode.MISSING_IDENTIFIER, this);
                }
            }
            else if (ENUM_CONSTANT_START_SET.contains(tokenType))
            {
                errorHandler.flag(token, PascalErrorCode.MISSING_COMMA, this);
            }
        } while (!ENUM_DEFINITION_FOLLOW_SET.contains(token.getType()));

        // Look for the closing ).
        if (token.getType() == PascalTokenType.RIGHT_PAREN)
        {
            token = nextToken(); // consume the )
        }
        else
        {
            errorHandler.flag(token, PascalErrorCode.MISSING_RIGHT_PAREN, this);
        }

        enumerationType.setAttribute(TypeKeyImpl.ENUMERATION_CONSTANTS, constants);
        return enumerationType;
    }

    private void parseEnumerationIdentifier(Token token, int value,TypeSpec enumerationType, ArrayList<SymTabEntry> constants) throws Exception
    {
        TokenType tokenType = token.getType();
        if (tokenType == PascalTokenType.IDENTIFIER)
        {
            String name = token.getText().toLowerCase();
            SymTabEntry constantId = symTabStack.lookupLocal(name);
            if (constantId != null)
            {
                errorHandler.flag(token, PascalErrorCode.IDENTIFIER_REDEFINED, this);
            }
            else
            {
                constantId = symTabStack.enterLocal(token.getText());
                constantId.setDefinition(DefinitionImpl.ENUMERATION_CONSTANT);
                constantId.setTypeSpec(enumerationType);
                constantId.setAttribute(SymTabKeyImpl.CONSTANT_VALUE, value);
                constantId.appendLineNumber(token.getLineNum());
                constants.add(constantId);
            }
            token = nextToken(); // consume the identifier
        }
        else
        {
            errorHandler.flag(token, PascalErrorCode.MISSING_IDENTIFIER, this);
        }
    }
}
