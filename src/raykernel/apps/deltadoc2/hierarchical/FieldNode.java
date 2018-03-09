package raykernel.apps.deltadoc2.hierarchical;

import raykernel.lang.dom.statement.VariableDeclarationStatement;

public class FieldNode extends DocNode
{
	VariableDeclarationStatement dec;
	boolean added;
	
	public FieldNode(VariableDeclarationStatement d, boolean added)
	{
		dec = d;
		this.added = added;
	}
	
	@Override
	public String toString()
	{
		if (added)
			return "added field : " + dec.toString();
		return "removed field : " + dec.toString();
	}
	
}
