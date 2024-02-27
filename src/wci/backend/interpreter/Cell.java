package wci.backend.interpreter;

public interface Cell
{
    /**
     * Set a new value into the cell.
     * @param newValue the new value.
     */
    public void setValue(Object newValue);

    /**
     * @return the value in the cell.
     */
    public Object getValue();
}
