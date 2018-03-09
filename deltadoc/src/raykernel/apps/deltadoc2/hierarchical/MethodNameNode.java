package raykernel.apps.deltadoc2.hierarchical;

import raykernel.lang.dom.naming.MethodSignature;

public class MethodNameNode extends DocNode
{
	String dec;
	boolean added;
	
	public MethodNameNode(MethodSignature dec, boolean added)
	{
		this.dec = dec.toString();
		this.added = added;
	}
	
	@Override
	public String toString()
	{
		if (added)
			return "added method : " + dec.toString();
		return "removed method : " + dec.toString();
	}
}
