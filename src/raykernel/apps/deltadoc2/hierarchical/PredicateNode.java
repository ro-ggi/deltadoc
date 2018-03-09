package raykernel.apps.deltadoc2.hierarchical;

import raykernel.lang.dom.condition.Condition;

public class PredicateNode extends DocNode
{
	Condition predicate;
	
	public PredicateNode(Condition predicate)
	{
		this.predicate = predicate;
	}
	
	@Override
	public String toString()
	{
		return "if " + predicate.toString();
	}
	
	public Condition getCondition()
	{
		return predicate;
	}
	
	public void setCondition(Condition predicate)
	{
		this.predicate = predicate;
	}
	
}
