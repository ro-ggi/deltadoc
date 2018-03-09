package raykernel.apps.deltadoc2.hierarchical;

public class ClassNode extends DocNode
{
	
	private final String className;
	
	public ClassNode(String className)
	{
		this.className = className;
	}
	
	@Override
	public String toString()
	{
		return className;
	}
	
}
