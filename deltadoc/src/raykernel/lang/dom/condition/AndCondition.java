package raykernel.lang.dom.condition;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;

import raykernel.lang.dom.expression.Expression;
import raykernel.util.Tools;

public class AndCondition extends Condition implements Iterable<Condition>
{
	//The things anded-together
	Set<Condition> conditions = new LinkedHashSet<Condition>();
	
	public AndCondition()
	{
		
	}
	
	public AndCondition(Condition... conditions)
	{
		for (Condition c : conditions)
		{
			this.conditions.add(c);
		}
	}
	
	public void addCondition(Condition c)
	{
		conditions.add(c);
	}
	
	// Do not merge sub conditions before PreProcess.processCFG(), it depends on original form.
	public void mergeSubConditions()
	{
		Set<Condition> correctedConditions = new LinkedHashSet<Condition>();
		for (Condition c : conditions)
		{
			if (c instanceof AndCondition)
			{
				for (Condition c2 : ((AndCondition) c).getConditions())
					correctedConditions.add(c2);
			} else if (c instanceof OrCondition && ((OrCondition) c).conditions.size() == 1)
			{
				correctedConditions.addAll(((OrCondition) c).conditions);
			} else
				correctedConditions.add(c);
		}
		conditions = correctedConditions;
	}
	
	@Override
	public Condition and(Condition c)
	{
		AndCondition ret = (AndCondition) clone();
		ret.addCondition(c);
		return ret;
	}
	
	public Collection<Condition> getConditions()
	{
		return conditions;
	}
	
	@Override
	public Collection<Expression> getSubExpressions()
	{
		Collection<Expression> expressions = new LinkedList<Expression>();
		expressions.addAll(conditions);
		return expressions;
	}
	
	@Override
	public void substitute(Expression oldExp, Expression newExp)
	{
		if (conditions.contains(oldExp))
		{
			conditions.remove(oldExp);
			conditions.add((Condition) newExp);
		}
		
		for (Condition c : this)
		{
			c.substitute(oldExp, newExp);
		}
	}
	
	@Override
	public Expression clone()
	{
		AndCondition ret = new AndCondition();
		
		for (Condition c : this)
		{
			ret.addCondition(c);
		}
		
		return ret;
	}
	
	/**
	 * Returns an Or condition with all the conditions negated
	 */
	@Override
	public Condition negated()
	{
		OrCondition ret = new OrCondition();
		
		for (Condition c : this)
		{
			ret.addCondition(c.negated());
		}
		
		return ret;
	}
	
	@Override
	public Iterator<Condition> iterator()
	{
		return conditions.iterator();
	}
	
	@Override
	public String toString()
	{
		return "(" + Tools.stringifyList(conditions, "&&") + ")";
	}
	
	@Override
	public boolean equals(Object o)
	{
		if (o instanceof AndCondition)
			return conditions.equals(((AndCondition) o).conditions);
		return false;
	}
	
	@Override
	public int hashCode()
	{
		return conditions.hashCode() + 2;
	}
}
