package raykernel.apps.deltadoc2.print;

import raykernel.apps.deltadoc2.hierarchical.ClassNode;
import raykernel.apps.deltadoc2.hierarchical.DoNode;
import raykernel.apps.deltadoc2.hierarchical.DocNode;
import raykernel.apps.deltadoc2.hierarchical.InsteadOfNode;
import raykernel.apps.deltadoc2.hierarchical.MethodNameNode;
import raykernel.apps.deltadoc2.hierarchical.MethodNode;
import raykernel.apps.deltadoc2.hierarchical.NoLongerNode;
import raykernel.apps.deltadoc2.hierarchical.PredicateNode;
import raykernel.apps.deltadoc2.hierarchical.RootNode;

/**
 * makes a dtree <http://destroydrop.com/javascripts/tree> version of the deltadoc
 * @author buse
 *
 *
 *
 */
public class DocToDtree extends DocPrinter
{
	int counter = 0;
	
	int getNextInt()
	{
		return ++counter;
	}
	
	/**
	 * Prints corresponding node with HTML list tags
	 * @return
	 */
	@Override
	public String print(DocNode node)
	{
		return "<!--\n\nd = new dTree('d');\n\n" +
		
		makeAddString(node, -1) +
		
		"document.write(d);\n\n//-->\n";
	}
	
	public String makeAddString(DocNode node, int parentID)
	{
		int nodeID = getNextInt();
		
		StringBuffer br = new StringBuffer();
		
		br.append("d.add(" + nodeID + "," + parentID + ",'" + node + "','http://google.com','hovertext','_self','"
				+ getIcon(node) + "','" + getOpenIcon(node) + "');\n");
		
		for (DocNode child : node.getSortedChildNodes())
		{
			br.append(makeAddString(child, nodeID));
		}
		
		return br.toString();
	}
	
	String imgdir = "img2/";
	
	public String getIcon(DocNode node)
	{
		if (node instanceof RootNode)
			return imgdir + "change_obj.gif";
		if (node instanceof ClassNode)
			return imgdir + "newclass_wiz.gif";
		if (node instanceof MethodNode)
			return imgdir + "jmeth.gif";
		if (node instanceof MethodNameNode)
			return imgdir + "jmeth_obj.gif";
		if (node instanceof DoNode)
			return imgdir + "restart_task.gif";
		if (node instanceof InsteadOfNode)
			return imgdir + "term_restart.gif";
		if (node instanceof PredicateNode)
			return imgdir + "processinginst.gif";
		if (node instanceof NoLongerNode)
			return imgdir + "terminate_all_co.gif";
		
		//else
		return imgdir + "brkpi_obj.gif";
		
	}
	
	public String getOpenIcon(DocNode node)
	{
		if (node instanceof RootNode)
			return imgdir + "change_obj.gif";
		if (node instanceof ClassNode)
			return imgdir + "classes.gif";
		if (node instanceof MethodNode)
			return imgdir + "jmeth_obj.gif";
		if (node instanceof MethodNameNode)
			return imgdir + "jmeth_obj.gif";
		if (node instanceof DoNode)
			return imgdir + "nav_go.gif";
		if (node instanceof InsteadOfNode)
			return imgdir + "nav_stop.gif";
		if (node instanceof PredicateNode)
			return imgdir + "watch_exp.gif";
		if (node instanceof NoLongerNode)
			return imgdir + "terminate_rem_co.gif";
		
		//else
		return imgdir + "brkpi_obj.gif";
		
	}
}
