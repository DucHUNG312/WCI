package wci.intermediate.symtabimpl;

import wci.intermediate.SymTab;
import wci.intermediate.SymTabEntry;
import wci.intermediate.SymTabFactory;
import wci.intermediate.SymTabStack;

import java.util.ArrayList;

public class SymTabStackImpl extends ArrayList<SymTab> implements SymTabStack
{
    private int currentNestingLevel;
    private SymTabEntry programId;

    public SymTabStackImpl()
    {
        this.currentNestingLevel = 0;
        add(SymTabFactory.createSymTab(currentNestingLevel));
    }

    @Override
    public int getCurrentNestingLevel()
    {
        return currentNestingLevel;
    }

    @Override
    public SymTab getLocalSymTab()
    {
        return get(currentNestingLevel);
    }

    @Override
    public SymTabEntry enterLocal(String name)
    {
        return get(currentNestingLevel).enter(name);
    }

    @Override
    public SymTabEntry lookupLocal(String name)
    {
        return get(currentNestingLevel).lookup(name);
    }

    @Override
    public SymTabEntry lookup(String name)
    {
        SymTabEntry foundEntry = null;
        for (int i = currentNestingLevel; (i >= 0) && (foundEntry == null); --i)
        {
            foundEntry = get(i).lookup(name);
        }

        return lookupLocal(name);
    }

    @Override
    public void setProgramId(SymTabEntry entry)
    {
        this.programId = programId;
    }

    @Override
    public SymTabEntry getProgramId()
    {
        return programId;
    }

    @Override
    public SymTab push()
    {
        SymTab symTab = SymTabFactory.createSymTab(++currentNestingLevel);
        add(symTab);
        return symTab;
    }

    @Override
    public SymTab push(SymTab symTab)
    {
        ++currentNestingLevel;
        add(symTab);
        return symTab;
    }

    @Override
    public SymTab pop()
    {
        SymTab symTab = get(currentNestingLevel);
        remove(currentNestingLevel--);

        return symTab;
    }
}
