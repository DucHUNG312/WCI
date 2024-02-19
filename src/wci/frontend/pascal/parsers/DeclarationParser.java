package wci.frontend.pascal.parsers;

import wci.frontend.Token;
import wci.frontend.pascal.PascalParserTD;
import wci.frontend.pascal.tokens.PascalTokenType;
import wci.intermediate.symtabimpl.DefinitionImpl;
import wci.intermediate.symtabimpl.SymTabKeyImpl;

import java.util.EnumSet;

public class DeclarationParser extends PascalParserTD
{
    static final EnumSet<PascalTokenType> DECLARATION_START_SET = EnumSet.of(
            PascalTokenType.CONST,
            PascalTokenType.TYPE,
            PascalTokenType.VAR,
            PascalTokenType.PROCEDURE,
            PascalTokenType.FUNCTION,
            PascalTokenType.BEGIN
    );

    static final EnumSet<PascalTokenType> TYPE_START_SET = DECLARATION_START_SET.clone();
    static
    {
        TYPE_START_SET.remove(PascalTokenType.CONST);
    }
    static final EnumSet<PascalTokenType> VAR_START_SET = TYPE_START_SET.clone();
    static
    {
        VAR_START_SET.remove(PascalTokenType.TYPE);
    }
    static final EnumSet<PascalTokenType> ROUTINE_START_SET = VAR_START_SET.clone();
    static
    {
        ROUTINE_START_SET.remove(PascalTokenType.VAR);
    }

    public DeclarationParser(PascalParserTD parent)
    {
        super(parent);
    }

    public void parse(Token token) throws Exception
    {
        token = synchronize(DECLARATION_START_SET);

        if(token.getType() == PascalTokenType.CONST)
        {
            token = nextToken(); // consume CONST
            ConstantDefinitionParser constantDefinitionParser = new ConstantDefinitionParser(this);
            constantDefinitionParser.parse(token);
        }

        token = synchronize(TYPE_START_SET);

        token = synchronize(TYPE_START_SET);
        if (token.getType() == PascalTokenType.TYPE)
        {
            token = nextToken(); // consume TYPE
            TypeDefinitionsParser typeDefinitionsParser = new TypeDefinitionsParser(this);
            typeDefinitionsParser.parse(token);
        }
        token = synchronize(VAR_START_SET);
        if (token.getType() == PascalTokenType.VAR)
        {
            token = nextToken(); // consume VAR
            VariableDeclarationsParser variableDeclarationsParser = new VariableDeclarationsParser(this);
            variableDeclarationsParser.setDefinition(DefinitionImpl.VARIABLE);
            variableDeclarationsParser.parse(token);
        }
        token = synchronize(ROUTINE_START_SET);
    }
}
