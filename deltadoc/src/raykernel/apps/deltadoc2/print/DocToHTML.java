package raykernel.apps.deltadoc2.print;

import raykernel.apps.deltadoc2.hierarchical.DocNode;

public class DocToHTML extends DocPrinter
{
	
	/**
	 * Prints corresponding node with HTML list tags
	 * @return
	 */
	@Override
	public String print(DocNode node)
	{
		return "<ul class=\"collapsibleList\">\n" + nodeToString(node, 1) + "</ul>\n";
	}
	
	/**
	 * Prints corresponding node with indenting
	 * @return
	 */
	private String nodeToString(DocNode node, int level)
	{
		StringBuffer buf = new StringBuffer();
		
		buf.append(indent(level) + "<li>" + node + "\n");
		
		if (!node.getChildNodes().isEmpty())
		{
			buf.append(indent(level + 1) + "<ul>\n");
			
			for (DocNode child : node.getSortedChildNodes())
			{
				buf.append(nodeToString(child, level + 2));
			}
			
			buf.append(indent(level + 1) + "</ul>\n");
		}
		buf.append(indent(level) + "</li>\n");
		
		return buf.toString();
	}
	
}
