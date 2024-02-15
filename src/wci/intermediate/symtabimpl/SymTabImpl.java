package wci.intermediate.symtabimpl;

import wci.intermediate.SymTab;
import wci.intermediate.SymTabEntry;
import wci.intermediate.SymTabFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.TreeMap;

public class SymTabImpl extends TreeMap<String, SymTabEntry> implements SymTab
{
    private int nestingLevel;

    public SymTabImpl(int level)
    {
        this.nestingLevel = level;
    }

    @Override
    public int getNestingLevel()
    {
        return nestingLevel;
    }

    @Override
    public SymTabEntry enter(String name)
    {
        SymTabEntry entry = SymTabFactory.createSymTabEntry(name, this);
        put(name, entry);
        return entry;
    }

    @Override
    public SymTabEntry lookup(String name)
    {
        return get(name);
    }

    @Override
    public ArrayList<SymTabEntry> sortedEntries()
    {
        Collection<SymTabEntry> entries = values();
        Iterator<SymTabEntry> iter = entries.iterator();
        ArrayList<SymTabEntry> list = new ArrayList<SymTabEntry>(size());

        while (iter.hasNext())
        {
            list.add(iter.next());
        }
        return list;
    }
}
