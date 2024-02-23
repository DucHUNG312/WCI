package wci.intermediate.icodeimpl;

import wci.intermediate.*;

import java.util.*;

public class ICodeNodeImpl extends HashMap<ICodeKey, Object> implements ICodeNode
{
    private ICodeNode parent;
    private ArrayList<ICodeNode> children;
    private ICodeNodeType type;
    private TypeSpec typeSpec;

    public ICodeNodeImpl(ICodeNodeType type)
    {
        this.type = type;
        this.parent = null;
        this.children = new ArrayList<ICodeNode>();
    }

    @Override
    public ICodeNodeType getType()
    {
        return type;
    }

    @Override
    public ICodeNode getParent()
    {
        return parent;
    }

    @Override
    public ICodeNode addChild(ICodeNode node)
    {
        if(node != null)
        {
            children.add(node);
            ((ICodeNodeImpl)node).parent = this;
        }
        return node;
    }

    @Override
    public ArrayList<ICodeNode> getChildren()
    {
        return children;
    }

    @Override
    public void setAttribute(ICodeKey key, Object value)
    {
        put(key, value);
    }

    @Override
    public Object getAttribute(ICodeKey key)
    {
        return get(key);
    }

    // copies all of the attributes to the new node
    @Override
    public ICodeNode copy()
    {
        ICodeNodeImpl copy = (ICodeNodeImpl) ICodeFactory.createICodeNode(type);
        Set<Map.Entry<ICodeKey, Object>> attributes = entrySet();
        Iterator<Map.Entry<ICodeKey, Object>> it = attributes.iterator();

        while (it.hasNext())
        {
            Map.Entry<ICodeKey, Object> attribute = it.next();
            copy.put(attribute.getKey(), attribute.getValue());
        }

        return copy;
    }

    @Override
    public void setTypeSpec(TypeSpec typeSpec)
    {
        this.typeSpec = typeSpec;
    }

    @Override
    public TypeSpec getTypeSpec()
    {
        return typeSpec;
    }

    public String toString()
    {
        return type.toString();
    }
}
