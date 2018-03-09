package raykernel.apps.deltadoc2.print;

import raykernel.apps.deltadoc2.hierarchical.DocNode;

public class DocToPlainText extends DocPrinter
{
	
	/**
	 * Prints corresponding node with indenting
	 * @return
	 */
	@Override
	public String print(DocNode node)
	{
		return nodeToString(node, 0);
	}
	
	/**
	 * Prints corresponding node with indenting
	 * @return
	 */
	private String nodeToString(DocNode node, int level)
	{
		StringBuffer buf = new StringBuffer();
		
		buf.append(indent(level) + node + "\n");
		
		for (DocNode child : node.getSortedChildNodes())
		{
			buf.append(nodeToString(child, level + 1));
		}
		
		return buf.toString();
	}
	
}
