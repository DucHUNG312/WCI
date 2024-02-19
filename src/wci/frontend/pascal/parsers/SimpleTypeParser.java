package wci.frontend.pascal.parsers;

import wci.frontend.Token;
import wci.frontend.pascal.PascalErrorCode;
import wci.frontend.pascal.PascalParserTD;
import wci.frontend.pascal.tokens.PascalTokenType;
import wci.intermediate.Definition;
import wci.intermediate.SymTabEntry;
import wci.intermediate.TypeSpec;
import wci.intermediate.symtabimpl.DefinitionImpl;

import java.util.EnumSet;

public class SimpleTypeParser extends PascalParserTD
{
    // Synchronization set for starting a simple type specification.
    static final EnumSet<PascalTokenType> SIMPLE_TYPE_START_SET = ConstantDefinitionParser.CONSTANT_START_SET.clone();
    static
    {
        SIMPLE_TYPE_START_SET.add(PascalTokenType.LEFT_PAREN);
        SIMPLE_TYPE_START_SET.add(PascalTokenType.COMMA);
        SIMPLE_TYPE_START_SET.add(PascalTokenType.SEMICOLON);
    }

    public SimpleTypeParser(PascalParserTD parent)
    {
        super(parent);
    }

    public TypeSpec parse(Token token) throws Exception
    {
        token = synchronize(SimpleTypeParser.SIMPLE_TYPE_START_SET);

        switch ((PascalTokenType)token.getType())
        {
            case IDENTIFIER:
            {
                String name = token.getText().toLowerCase();
                SymTabEntry id = symTabStack.lookup(name);
                if (id != null)
                {
                    Definition definition = id.getDefinition();
                    // It's either a type identifier
                    // or the start of a subrange type.
                    if (definition == DefinitionImpl.TYPE)
                    {
                        id.appendLineNumber(token.getLineNum());
                        token = nextToken(); // consume the identifier
                        // Return the type of the referent type.
                        return id.getTypeSpec();
                    }
                    else if ((definition != DefinitionImpl.CONSTANT) && (definition != DefinitionImpl.ENUMERATION_CONSTANT))
                    {
                        errorHandler.flag(token, PascalErrorCode.NOT_TYPE_IDENTIFIER, this);
                        token = nextToken(); // consume the identifier
                        return null;
                    }
                    else
                    {
                        SubrangeTypeParser subrangeTypeParser = new SubrangeTypeParser(this);
                        return subrangeTypeParser.parse(token);
                    }
                }
                else
                {
                    errorHandler.flag(token, PascalErrorCode.IDENTIFIER_UNDEFINED, this);
                    token = nextToken(); // consume the identifier
                    return null;
                }
            }
            case LEFT_PAREN:
            {
                EnumerationTypeParser enumerationTypeParser = new EnumerationTypeParser(this);
                return enumerationTypeParser.parse(token);
            }
            case COMMA:
            case SEMICOLON:
            {
                errorHandler.flag(token, PascalErrorCode.INVALID_TYPE, this);
                return null;
            }
            default:
            {
                SubrangeTypeParser subrangeTypeParser = new SubrangeTypeParser(this);
                return subrangeTypeParser.parse(token);
            }
        }
    }
}
