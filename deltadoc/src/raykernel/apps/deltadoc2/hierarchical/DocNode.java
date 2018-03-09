package raykernel.apps.deltadoc2.hierarchical;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public abstract class DocNode
{
	LinkedList<DocNode> children = new LinkedList<>();
	
	public List<DocNode> getChildNodes()
	{
		return children;
	}
	
	public void addChild(DocNode n)
	{
		children.add(n);
	}
	
	public void removeChild(DocNode n)
	{
		children.remove(n);
	}
	
	public int getMinCharIndex()
	{
		int min = Integer.MAX_VALUE;
		
		for (DocNode child : children)
		{
			int childMin = child.getMinCharIndex();
			if (childMin < min)
			{
				min = childMin;
			}
		}
		
		//System.out.println("Min char index for " + this + " = " + min);
		
		return min;
	}
	
	public List<DocNode> getSortedChildNodes()
	{
		Collections.sort(children, new Comparator<DocNode>() {
			
			@Override
			public int compare(DocNode n1, DocNode n2)
			{
				return n1.getMinCharIndex() - n2.getMinCharIndex();
			}
		});
		
		return children;
	}
	
}
