package wci.frontend.pascal.parsers;

import wci.frontend.Token;
import wci.frontend.TokenType;
import wci.frontend.pascal.PascalErrorCode;
import wci.frontend.pascal.PascalParserTD;
import wci.frontend.pascal.tokens.PascalTokenType;
import wci.intermediate.SymTabEntry;
import wci.intermediate.TypeFactory;
import wci.intermediate.TypeForm;
import wci.intermediate.TypeSpec;
import wci.intermediate.typeimpl.TypeFormImpl;
import wci.intermediate.typeimpl.TypeKeyImpl;

import java.util.ArrayList;
import java.util.EnumSet;

public class ArrayTypeParser extends PascalParserTD
{
    // Synchronization set for the [ token.
    private static final EnumSet<PascalTokenType> LEFT_BRACKET_SET = SimpleTypeParser.SIMPLE_TYPE_START_SET.clone();
    static
    {
        LEFT_BRACKET_SET.add(PascalTokenType.LEFT_BRACKET);
        LEFT_BRACKET_SET.add(PascalTokenType.RIGHT_BRACKET);
    }
    // Synchronization set for the ] token.
    private static final EnumSet<PascalTokenType> RIGHT_BRACKET_SET = EnumSet.of(
            PascalTokenType.RIGHT_BRACKET,
            PascalTokenType.OF,
            PascalTokenType.SEMICOLON
    );
    // Synchronization set for OF.
    private static final EnumSet<PascalTokenType> OF_SET = TypeSpecificationParser.TYPE_START_SET.clone();
    static
    {
        OF_SET.add(PascalTokenType.OF);
        OF_SET.add(PascalTokenType.SEMICOLON);
    }

    // Synchronization set to start an index type.
    private static final EnumSet<PascalTokenType> INDEX_START_SET = SimpleTypeParser.SIMPLE_TYPE_START_SET.clone();
    static
    {
        INDEX_START_SET.add(PascalTokenType.COMMA);
    }

    // Synchronization set to end an index type.
    private static final EnumSet<PascalTokenType> INDEX_END_SET = EnumSet.of(
            PascalTokenType.RIGHT_BRACKET,
            PascalTokenType.OF,
            PascalTokenType.SEMICOLON
    );
    // Synchronization set to follow an index type.
    private static final EnumSet<PascalTokenType> INDEX_FOLLOW_SET = INDEX_START_SET.clone();
    static
    {
        INDEX_FOLLOW_SET.addAll(INDEX_END_SET);
    }

    public ArrayTypeParser(PascalParserTD parent)
    {
        super(parent);
    }

    public TypeSpec parse(Token token) throws Exception
    {
        TypeSpec arrayType = TypeFactory.createType(TypeFormImpl.ARRAY);
        token = nextToken(); // consume ARRAY
        // Synchronize at the [ token.
        token = synchronize(LEFT_BRACKET_SET);
        if (token.getType() != PascalTokenType.LEFT_BRACKET)
        {
            errorHandler.flag(token, PascalErrorCode.MISSING_LEFT_BRACKET, this);
        }
        // Parse the list of index types.
        TypeSpec elementType = parseIndexTypeList(token, arrayType);
        // Synchronize at the ] token.
        token = synchronize(RIGHT_BRACKET_SET);
        if (token.getType() == PascalTokenType.RIGHT_BRACKET)
        {
            token = nextToken(); // consume [
        }
        else
        {
            errorHandler.flag(token, PascalErrorCode.MISSING_RIGHT_BRACKET, this);
        }
        // Synchronize at OF.
        token = synchronize(OF_SET);
        if (token.getType() == PascalTokenType.OF)
        {
            token = nextToken(); // consume OF
        }
        else
        {
            errorHandler.flag(token, PascalErrorCode.MISSING_OF, this);
        }
        // Parse the element type.
        elementType.setAttribute(TypeKeyImpl.ARRAY_ELEMENT_TYPE, parseElementType(token));
        return arrayType;
    }

    private TypeSpec parseIndexTypeList(Token token, TypeSpec arrayType) throws Exception
    {
        TypeSpec elementType = arrayType;
        boolean anotherIndex = false;
        token = nextToken(); // consume the [ token
        // Parse the list of index type specifications.
        do
        {
            anotherIndex = false;
            // Parse the index type.
            token = synchronize(INDEX_START_SET);
            parseIndexType(token, elementType);
            // Synchronize at the , token.
            token = synchronize(INDEX_FOLLOW_SET);
            TokenType tokenType = token.getType();
            if ((tokenType != PascalTokenType.COMMA) && (tokenType != PascalTokenType.RIGHT_BRACKET))
            {
                if (INDEX_START_SET.contains(tokenType))
                {
                    errorHandler.flag(token, PascalErrorCode.MISSING_COMMA, this);
                    anotherIndex = true;
                }
            }
            // Create an ARRAY element type object
            // for each subsequent index type.
            else if (tokenType == PascalTokenType.COMMA)
            {
                TypeSpec newElementType = TypeFactory.createType(TypeFormImpl.ARRAY);
                elementType.setAttribute(TypeKeyImpl.ARRAY_ELEMENT_TYPE, newElementType);
                elementType = newElementType;
                token = nextToken(); // consume the , token
                anotherIndex = true;
            }
        } while (anotherIndex);
        return elementType;
    }

    private void parseIndexType(Token token, TypeSpec arrayType) throws Exception
    {
        SimpleTypeParser simpleTypeParser = new SimpleTypeParser(this);
        TypeSpec indexType = simpleTypeParser.parse(token);
        arrayType.setAttribute(TypeKeyImpl.ARRAY_INDEX_TYPE, indexType);
        if (indexType == null)
        {
            return;
        }
        TypeForm form = indexType.getForm();
        int count = 0;
        // Check the index type and set the element count.
        if (form == TypeFormImpl.SUBRANGE)
        {
            Integer minValue = (Integer) indexType.getAttribute(TypeKeyImpl.SUBRANGE_MIN_VALUE);
            Integer maxValue = (Integer) indexType.getAttribute(TypeKeyImpl.SUBRANGE_MAX_VALUE);
            if ((minValue != null) && (maxValue != null))
            {
                count = maxValue - minValue + 1;
            }
        }
        else if (form == TypeFormImpl.ENUMERATION)
        {
            ArrayList<SymTabEntry> constants = (ArrayList<SymTabEntry>) indexType.getAttribute(TypeKeyImpl.ENUMERATION_CONSTANTS);
            count = constants.size();
        }
        else
        {
            errorHandler.flag(token, PascalErrorCode.INVALID_INDEX_TYPE, this);
        }
        arrayType.setAttribute(TypeKeyImpl.ARRAY_ELEMENT_COUNT, count);
    }

    private TypeSpec parseElementType(Token token) throws Exception
    {
        TypeSpecificationParser typeSpecificationParser =
                new TypeSpecificationParser(this);
        return typeSpecificationParser.parse(token);
    }
}
