package raykernel.apps.deltadoc2.print;

import raykernel.apps.deltadoc2.hierarchical.DocNode;

public abstract class DocPrinter
{
	public static final String ind = " ";
	
	public abstract String print(DocNode node);
	
	protected static String indent(int amount)
	{
		StringBuffer buff = new StringBuffer();
		for (int i = 0; i < amount; i++)
		{
			buff.append(ind);
		}
		return buff.toString();
	}
}
