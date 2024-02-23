package wci.frontend.pascal.parsers;

import wci.frontend.Token;
import wci.frontend.TokenType;
import wci.frontend.pascal.PascalErrorCode;
import wci.frontend.pascal.PascalParserTD;
import wci.frontend.pascal.PascalTokenType;
import wci.intermediate.*;
import wci.intermediate.icodeimpl.ICodeKeyImpl;
import wci.intermediate.icodeimpl.ICodeNodeTypeImpl;
import wci.intermediate.symtabimpl.DefinitionImpl;
import wci.intermediate.symtabimpl.Predefined;
import wci.intermediate.typeimpl.TypeChecker;
import wci.intermediate.typeimpl.TypeFormImpl;
import wci.intermediate.typeimpl.TypeKeyImpl;

import java.util.EnumSet;

public class VariableParser extends PascalParserTD
{
    private static final EnumSet<PascalTokenType> SUBSCRIPT_FIELD_START_SET = EnumSet.of(
            PascalTokenType.LEFT_BRACKET,
            PascalTokenType.DOT
    );
    // Synchronization set for the ] token.
    private static final EnumSet<PascalTokenType> RIGHT_BRACKET_SET = EnumSet.of(
            PascalTokenType.RIGHT_BRACKET,
            PascalTokenType.EQUALS,
            PascalTokenType.SEMICOLON
    );
    public VariableParser(PascalParserTD parent)
    {
        super(parent);
    }

    public ICodeNode parse(Token token) throws Exception
    {
        String name = token.getText().toLowerCase();
        SymTabEntry variableId = symTabStack.lookup(name);

        if(variableId == null)
        {
            errorHandler.flag(token, PascalErrorCode.IDENTIFIER_UNDEFINED, this);
            variableId = symTabStack.enterLocal(name);
            variableId.setDefinition(DefinitionImpl.UNDEFINED);
            variableId.setTypeSpec(Predefined.undefinedType);
        }

        return parse(token, variableId);
    }

    public ICodeNode parse(Token token, SymTabEntry variableId) throws Exception
    {
        Definition defnCode = variableId.getDefinition();
        if((defnCode != DefinitionImpl.VARIABLE) && (defnCode != DefinitionImpl.VALUE_PARM) && ((defnCode != DefinitionImpl.VAR_PARM)))
        {
            errorHandler.flag(token, PascalErrorCode.INVALID_IDENTIFIER_USAGE, this);
        }

        variableId.appendLineNumber(token.getLineNumber());

        ICodeNode variableNode = ICodeFactory.createICodeNode(ICodeNodeTypeImpl.VARIABLE);
        variableNode.setAttribute(ICodeKeyImpl.ID, variableId);

        token = nextToken();

        TypeSpec variableType = variableId.getTypeSpec();
        while (SUBSCRIPT_FIELD_START_SET.contains(token.getType()))
        {
            ICodeNode subFldNode = token.getType() == PascalTokenType.LEFT_BRACKET
                    ? parseSubscripts(variableType)
                    : parseField(variableType);
            token = currentToken();
            // Update the variable&apos;s type.
            // The variable node adopts the SUBSCRIPTS or FIELD node.
            variableType = subFldNode.getTypeSpec();
            variableNode.addChild(subFldNode);
        }
        variableNode.setTypeSpec(variableType);
        return variableNode;
    }

    private ICodeNode parseSubscripts(TypeSpec variableType)
            throws Exception
    {
        Token token;
        ExpressionParser expressionParser = new ExpressionParser(this);
        // Create a SUBSCRIPTS node.
        ICodeNode subscriptsNode = ICodeFactory.createICodeNode(ICodeNodeTypeImpl.SUBSCRIPTS);
        do {
            token = nextToken(); // consume the [ or , token
            // The current variable is an array.
            if (variableType.getForm() == TypeFormImpl.ARRAY) {
                // Parse the subscript expression.
                ICodeNode exprNode = expressionParser.parse(token);
                TypeSpec exprType = exprNode != null ? exprNode.getTypeSpec()
                        : Predefined.undefinedType;
                // The subscript expression type must be assignment
                // compatible with the array index type.
                TypeSpec indexType =
                        (TypeSpec) variableType.getAttribute(TypeKeyImpl.ARRAY_INDEX_TYPE);
                if (!TypeChecker.areAssignmentCompatible(indexType, exprType)) {
                    errorHandler.flag(token, PascalErrorCode.INCOMPATIBLE_TYPES, this);
                }
                // The SUBSCRIPTS node adopts the subscript expression tree.
                subscriptsNode.addChild(exprNode);
                // Update the variable's type.
                variableType = (TypeSpec) variableType.getAttribute(TypeKeyImpl.ARRAY_ELEMENT_TYPE);
            }
            // Not an array type, so too many subscripts.
            else {
                errorHandler.flag(token, PascalErrorCode.TOO_MANY_SUBSCRIPTS, this);
                expressionParser.parse(token);
            }
            token = currentToken();
        } while (token.getType() == PascalTokenType.COMMA);
        // Synchronize at the ] token.
        token = synchronize(RIGHT_BRACKET_SET);
        if (token.getType() == PascalTokenType.RIGHT_BRACKET) {
            token = nextToken(); // consume the ] token
        }
        else {
            errorHandler.flag(token, PascalErrorCode.MISSING_RIGHT_BRACKET, this);
        }
        subscriptsNode.setTypeSpec(variableType);
        return subscriptsNode;
    }

    private ICodeNode parseField(TypeSpec variableType)
            throws Exception
    {
        // Create a FIELD node.
        ICodeNode fieldNode = ICodeFactory.createICodeNode(ICodeNodeTypeImpl.FIELD);
        Token token = nextToken(); // consume the . token
        TokenType tokenType = token.getType();
        TypeForm variableForm = variableType.getForm();
        if ((tokenType == PascalTokenType.IDENTIFIER) && (variableForm == TypeFormImpl.RECORD)) {
            SymTab symTab = (SymTab) variableType.getAttribute(TypeKeyImpl.RECORD_SYMTAB);
            String fieldName = token.getText().toLowerCase();
            SymTabEntry fieldId = symTab.lookup(fieldName);
            if (fieldId != null) {
                variableType = fieldId.getTypeSpec();
                fieldId.appendLineNumber(token.getLineNumber());
                // Set the field identifier&apos;s name.
                fieldNode.setAttribute(ICodeKeyImpl.ID, fieldId);
            }
            else {
                errorHandler.flag(token, PascalErrorCode.INVALID_FIELD, this);
            }
        }
        else {
            errorHandler.flag(token, PascalErrorCode.INVALID_FIELD, this);
        }
        token = nextToken(); // consume the field identifier
        fieldNode.setTypeSpec(variableType);
        return fieldNode;
    }
}
