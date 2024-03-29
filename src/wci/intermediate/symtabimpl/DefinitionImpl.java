package wci.intermediate.symtabimpl;

import wci.intermediate.Definition;

public enum DefinitionImpl implements Definition
{
    CONSTANT, ENUMERATION_CONSTANT("enumeration constant"),
    TYPE, VARIABLE, FIELD("record field"),
    VALUE_PARM("value parameter"), VAR_PARM("VAR parameter"),
    PROGRAM_PARM("program parameter"),
    PROGRAM, PROCEDURE, FUNCTION,
    UNDEFINED;

    private String text;

    /**
     * Constructor.
     */
    DefinitionImpl()
    {
        this.text = this.toString().toLowerCase();
    }

    /**
     * Constructor.
     * @param text the text for the definition code.
     */
    DefinitionImpl(String text)
    {
        this.text = text;
    }

    /**
     * Getter.
     * @return the text of the definition code.
     */
    public String getText()
    {
        return text;
    }
}
