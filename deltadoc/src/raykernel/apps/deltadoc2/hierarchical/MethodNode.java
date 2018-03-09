package raykernel.apps.deltadoc2.hierarchical;

public class MethodNode extends DocNode
{
	String method;
	
	public MethodNode(String methodSig)
	{
		method = methodSig;
	}
	
	@Override
	public String toString()
	{
		return "when calling " + method;
	}
	
}
