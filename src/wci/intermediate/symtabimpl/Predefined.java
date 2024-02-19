package wci.intermediate.symtabimpl;

import wci.intermediate.SymTabEntry;
import wci.intermediate.SymTabStack;
import wci.intermediate.TypeFactory;
import wci.intermediate.TypeSpec;
import wci.intermediate.typeimpl.TypeFormImpl;
import wci.intermediate.typeimpl.TypeKeyImpl;

import java.util.ArrayList;

public class Predefined
{
    // Predefined types.
    public static TypeSpec integerType;
    public static TypeSpec realType;
    public static TypeSpec booleanType;
    public static TypeSpec charType;
    public static TypeSpec undefinedType;

    // Predefined identifiers.
    public static SymTabEntry integerId;
    public static SymTabEntry realId;
    public static SymTabEntry booleanId;
    public static SymTabEntry charId;
    public static SymTabEntry falseId;
    public static SymTabEntry trueId;

    public static void initialize(SymTabStack symTabStack)
    {
        initializeTypes(symTabStack);
        initializeConstants(symTabStack);
    }

    private static void initializeConstants(SymTabStack symTabStack)
    {
        // integer
        integerId = symTabStack.enterLocal("integer");
        integerType = TypeFactory.createType(TypeFormImpl.SCALAR);
        integerType.setIdentifier(integerId);
        integerId.setDefinition(DefinitionImpl.TYPE);
        integerId.setTypeSpec(integerType);

        // real
        realId = symTabStack.enterLocal("real");
        realType = TypeFactory.createType(TypeFormImpl.SCALAR);
        realType.setIdentifier(realId);
        realId.setDefinition(DefinitionImpl.TYPE);
        realId.setTypeSpec(realType);

        // char
        charId = symTabStack.enterLocal("char");
        charType = TypeFactory.createType(TypeFormImpl.SCALAR);
        charType.setIdentifier(charId);
        charId.setDefinition(DefinitionImpl.TYPE);
        charId.setTypeSpec(charType);

        // boolean
        booleanId = symTabStack.enterLocal("boolean");
        booleanType = TypeFactory.createType(TypeFormImpl.ENUMERATION);
        booleanType.setIdentifier(booleanId);
        booleanId.setDefinition(DefinitionImpl.TYPE);
        booleanId.setTypeSpec(booleanType);

        // undefined type
        undefinedType = TypeFactory.createType(TypeFormImpl.SCALAR);
    }

    private static void initializeTypes(SymTabStack symTabStack)
    {
        // false constant
        falseId = symTabStack.enterLocal("false");
        falseId.setDefinition(DefinitionImpl.ENUMERATION_CONSTANT);
        falseId.setTypeSpec(booleanType);
        falseId.setAttribute(SymTabKeyImpl.CONSTANT_VALUE, Integer.valueOf(0));

        // true constant
        trueId = symTabStack.enterLocal("true");
        trueId.setDefinition(DefinitionImpl.ENUMERATION_CONSTANT);
        trueId.setTypeSpec(booleanType);
        trueId.setAttribute(SymTabKeyImpl.CONSTANT_VALUE, Integer.valueOf(1));

        // Add false and true to the boolean enumeration type
        ArrayList<SymTabEntry> constants = new ArrayList<SymTabEntry>();
        constants.add(falseId);
        constants.add(trueId);
        booleanType.setAttribute(TypeKeyImpl.ENUMERATION_CONSTANTS, constants);
    }
}
