package raykernel.apps.deltadoc2.hierarchical;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import raykernel.lang.dom.condition.AndCondition;
import raykernel.lang.dom.condition.Condition;
import raykernel.lang.dom.condition.OrCondition;
import raykernel.lang.dom.condition.simplify.ConditionSimplifier;
import raykernel.lang.dom.expression.False;
import raykernel.lang.dom.expression.True;
import raykernel.util.EnumerativeSet;

public class CombinePredicates
{
	public static void process(DocNode d)
	{
		boolean changed = true;
		int max = 7;
		int count = 0;
		
		while (changed && ++count < max)
		{
			changed = processInternal(d);
		}
		
		//System.out.println("Passes = " + count);
	}
	
	/**
	 * Returns true iff something changed
	 * @param d
	 * @return
	 */
	private static boolean processInternal(DocNode d)
	{
		boolean changed = false;
		List<PredicateNode> predicateChildren = new LinkedList<PredicateNode>();
		List<Condition> conditions = new LinkedList<Condition>();
		
		for (DocNode child : d.getChildNodes())
		{
			if (!(child instanceof PredicateNode))
			{
				changed = changed || processInternal(child);
			}
			else
			{
				predicateChildren.add((PredicateNode) child);
				conditions.add(((PredicateNode) child).getCondition());
			}
		}
		
		if (predicateChildren.isEmpty())
			return changed;
		
		//System.out.println("Processing: " + d);
		
		Condition commCond = getMostCommonSubCondition(conditions);
		
		//System.out.println("CommonCondition = " + commCond);
		
		if (commCond.equals(new True())) //they are all independant
			return changed;
		
		//if we get here, we are def changing something
		changed = true;
		
		PredicateNode commonPredNode = new PredicateNode(commCond);
		d.addChild(commonPredNode);
		
		//System.out.println("Creating new Node =" + commonPredNode);
		
		Map<Condition, PredicateNode> newPredNodes = new HashMap<>();
		
		newPredNodes.put(new True(), commonPredNode);
		
		for (PredicateNode pn : predicateChildren)
		{
			//System.out.println("  Considering child = " + pn);
			
			Condition cond = pn.getCondition();
			Condition remainder = subtract(cond, commCond);
			
			//System.out.println("  Remainder = " + remainder);
			
			if (remainder.equals(cond) || remainder.equals(new False()))
			{
				//System.out.println(" - will leave alone");
				continue; //this predicate dosent get affected
			}
			
			//moving down a level, so remove from parents children
			d.removeChild(pn);
			
			//see if we have a node for this new subcondition condition yet
			PredicateNode newpn = newPredNodes.get(remainder);
			
			if (newpn == null) //if we dont, use this node and just change the condition
			{
				newpn = pn;
				newpn.setCondition(remainder);
				commonPredNode.addChild(newpn);
				newPredNodes.put(remainder, newpn);
				//System.out.println(" Made new PN: " + newpn);
			}
			else
			//just add the statments from this node
			{
				//System.out.println(" Using existing PN: " + newpn);
				
				for (DocNode child : pn.getChildNodes())
				{
					newpn.addChild(child);
				}
			}
			
			processInternal(newpn);
			
		}
		
		return changed;
	}
	
	/**
	 * We are looking for a condition that must be true for the largest number of
	 * these input conditions to be true;
	 * @param conditions
	 * @return
	 */
	private static Condition getMostCommonSubCondition(List<Condition> conditions)
	{
		//System.out.println("Condtions: " + conditions);
		
		EnumerativeSet<Condition> eset = new EnumerativeSet<>();
		
		for (Condition c : conditions)
		{
			Set<Condition> must = mustBeTrue(c);
			eset.addAll(must);
		}
		
		//eset.printSortedCounts(10);
		
		Condition top = eset.getFirst();
		double count = eset.getCount(top);
		
		if (count > 1)
			return top;
		
		return new True();
	}
	
	/**
	 * Return a list of atomic conditions (Expressions) which must be true for this
	 * Condition to be true
	 * @param c
	 * @return
	 */
	private static Set<Condition> mustBeTrue(Condition c)
	{
		Set<Condition> ret = new HashSet<Condition>();
		
		//If and condition, and together all that must be true in each subcondition
		if (c instanceof AndCondition)
		{
			AndCondition and = (AndCondition) c;
			
			for (Condition andC : and)
			{
				Set<Condition> subConds = mustBeTrue(andC);
				ret.addAll(subConds);
			}
			
		}
		//If or condition, return all expressions that must be true in every subcondition
		else if (c instanceof OrCondition)
		{
			OrCondition or = (OrCondition) c;
			boolean first = true;
			
			for (Condition orC : or)
			{
				Set<Condition> subConds = mustBeTrue(orC);
				
				if (first)
				{
					ret.addAll(subConds);
					first = false;
				}
				else
				{
					ret.retainAll(subConds);
				}
			}
		}
		//Otherwsie this is just an atomic
		else
		{
			ret.add(c);
		}
		
		return ret;
	}
	
	/**
	 * RemThis should be an atomic condition
	 * @param cond
	 * @param remThis
	 * @return
	 */
	private static Condition subtract(Condition cond, Condition remThis)
	{
		if (!(cond instanceof AndCondition) && !(cond instanceof OrCondition))
		{
			if (cond.equals(remThis))
				return new True();
			else
				return new False();
		}
		
		return ConditionSimplifier.remove(cond, remThis);
	}
}
