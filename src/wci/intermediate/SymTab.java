package wci.intermediate;

import java.util.ArrayList;

public interface SymTab
{
    public int getNestingLevel();
    public SymTabEntry enter(String name);
    public SymTabEntry lookup(String name);
    ArrayList<SymTabEntry> sortedEntries();
}
