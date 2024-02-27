package wci.backend.interpreter;

import java.util.ArrayList;

public interface MemoryMap
{
    /**
     * Return the memory cell with the given name.
     * @param name the name.
     * @return the cell.
     */
    public Cell getCell(String name);

    /**
     * @return the list of all the names.
     */
    public ArrayList<String> getAllNames();
}
