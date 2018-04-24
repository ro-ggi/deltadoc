package raykernel.apps.deltadoc2.hierarchical;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
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
		/* if c1 or c2
		 *  ... instead of when c2 or c3
		 * -->
		 * if c2
		 *  if c1
		 *  ... instead of when c3
		 */
		extractCommonAltConditionParts(d);
		
		/* if c1
		 *  if c2
		 *   ...
		 * -->
		 * if c1 and c2
		 *  ...
		 */
		concatenateConditionsSeries(d);
		
		// Only atomic most common condition is processed, so repetition is needed
		boolean changed = true;
		int max = 7;
		int count = 0;
		
		while (changed && ++count <= max)
		{
			/* if c1 or c2
			 *  stmt1
			 * if c1 or c3
			 *  stmt2
			 * -->
			 * if c1
			 *  if c2
			 *   stmt1
			 *  if c3
			 *   stmt2
			 */
			changed = extractCommonPredicates(d);
		}
		
		//System.out.println("Passes = " + count);
		
		/* if (c1 and c2) or (c1 and c3)
		 * -->
		 * if c1
		 *  if c2 or c3
		 */
		simplifyOrPredicates(d);
		
		concatenateConditionsSeries(d);
		
		removeTrueNodes(d);
		
		// Combine same predicates for statement with altCondition.
		combineSamePredicates(d);
	}
	
	private static void extractCommonAltConditionParts(DocNode parent)
	{
		for (DocNode child : parent.getChildNodes().toArray(new DocNode[0]))
		{
			if (child instanceof PredicateNode)
			{
				PredicateNode predNode = (PredicateNode) child;

				for (DocNode grandchild : predNode.getChildNodes().toArray(new DocNode[0]))
				{
					if (grandchild instanceof StatementNode && ((StatementNode) grandchild).altCondition != null)
					{
						StatementNode stmtNode = (StatementNode) grandchild;

						predNode.removeChild(stmtNode);
						if (predNode.getChildNodes().isEmpty())
							parent.removeChild(predNode);

						List<Condition> conditions = new ArrayList<>();
						conditions.add(stmtNode.altCondition);
						conditions.add(predNode.getCondition());
						Condition commCond = getMostCommonSubCondition(conditions);

						if ((!commCond.equals(new True())))
						{
							PredicateNode commPredNode = new PredicateNode(commCond);
							PredicateNode reminderPredNode = new PredicateNode(
									subtract(predNode.getCondition(), commCond));
							stmtNode.setAltCondition(subtract(stmtNode.altCondition, commCond));

							parent.addChild(commPredNode);
							commPredNode.addChild(reminderPredNode);
							reminderPredNode.addChild(stmtNode);

							extractCommonAltConditionParts(commPredNode);
						} else
						{
							// Guard uncommon part with "if TRUE" predicate
							PredicateNode guardPredNode = new PredicateNode(new True());
							// Uncommon part predicate
							PredicateNode uncommPredNode = new PredicateNode(predNode.getCondition());

							parent.addChild(guardPredNode);
							guardPredNode.addChild(uncommPredNode);
							uncommPredNode.addChild(stmtNode);
						}
					}
				}
			} else
			{
				extractCommonAltConditionParts(child);
			}
		}
	}
	
	/**
	 * Returns true iff something changed
	 * @param d
	 * @return
	 */
	private static boolean extractCommonPredicates(DocNode d)
	{
		boolean changed = false;
		List<PredicateNode> predicateChildren = new LinkedList<PredicateNode>();
		List<Condition> conditions = new LinkedList<Condition>();
		
		for (DocNode child : d.getChildNodes())
		{
			if (child instanceof PredicateNode)
			{
				predicateChildren.add((PredicateNode) child);
				conditions.add(((PredicateNode) child).getCondition());
			}
		}
		
		//System.out.println("Processing: " + d);
		
		Condition commCond = getMostCommonSubCondition(conditions);
		
		//System.out.println("CommonCondition = " + commCond);
		
		if (!commCond.equals(new True())) { // they are not independent

			// if we get here, we are def changing something
			changed = true;

			PredicateNode commonPredNode = new PredicateNode(commCond);
			d.addChild(commonPredNode);

			// System.out.println("Creating new Node =" + commonPredNode);

			Map<Condition, PredicateNode> newPredNodes = new HashMap<>();

			newPredNodes.put(new True(), commonPredNode);

			for (PredicateNode pn : predicateChildren) {
				// System.out.println(" Considering child = " + pn);

				Condition cond = pn.getCondition();
				Condition remainder = subtract(cond, commCond);

				// System.out.println(" Remainder = " + remainder);

				if (remainder.equals(cond) || remainder.equals(new False())) {
					// System.out.println(" - will leave alone");
					continue; // this predicate dosent get affected
				}

				// moving down a level, so remove from parents children
				d.removeChild(pn);

				// see if we have a node for this new subcondition condition yet
				PredicateNode newpn = newPredNodes.get(remainder);

				if (newpn == null) // if we dont, use this node and just change the condition
				{
					newpn = pn;
					newpn.setCondition(remainder);
					commonPredNode.addChild(newpn);
					newPredNodes.put(remainder, newpn);
					// System.out.println(" Made new PN: " + newpn);
				} else
				// just add the statments from this node
				{
					// System.out.println(" Using existing PN: " + newpn);

					for (DocNode child : pn.getChildNodes()) {
						newpn.addChild(child);
					}
				}
			}
		}

		for (DocNode child : d.getChildNodes()) {
			changed = changed | extractCommonPredicates(child);
		}

		return changed;
	}
		
	private static void simplifyOrPredicates(DocNode parent)
	{
		for (DocNode child : parent.getChildNodes())
		{
			if (child instanceof PredicateNode && ((PredicateNode) child).getCondition() instanceof OrCondition)
			{
				OrCondition cond = (OrCondition) ((PredicateNode) child).getCondition();
				List<Condition> conditions = new LinkedList<Condition>();
				conditions.addAll(cond.getConditions());

				Condition commCond = getMostCommonSubCondition(conditions);

				if (!commCond.equals(new True()))
				{
					Condition reminder = subtract(cond, commCond);
					((OrCondition) reminder).mergeSubConditions();
					PredicateNode reminderPredNode = new PredicateNode(reminder);

					for (DocNode grandchild : child.getChildNodes().toArray(new DocNode[0]))
					{
						reminderPredNode.addChild(grandchild);
						child.removeChild(grandchild);
					}
					child.addChild(reminderPredNode);
					((PredicateNode) child).setCondition(commCond);
				}
			}
		}

		for (DocNode child : parent.getChildNodes())
		{
			simplifyOrPredicates(child);
		}
	}
	
	
	
	private static void concatenateConditionsSeries(DocNode parent)
	{
		for (DocNode child : parent.getChildNodes().toArray(new DocNode[0]))
		{
			concatenateConditionsSeries(child);

			if (child instanceof PredicateNode)
			{
				if (child.getChildNodes().size() == 1)
				{
					DocNode grandchild = child.getChildNodes().get(0);

					if (grandchild instanceof PredicateNode)
					{
						Condition childCond = ((PredicateNode) child).getCondition();
						Condition grandchildCond = ((PredicateNode) grandchild).getCondition();
						if (!childCond.equals(new True()) && !grandchildCond.equals(new True()))
						{
							childCond = new AndCondition(childCond, grandchildCond);
							((AndCondition) childCond).mergeSubConditions();
							((PredicateNode) grandchild).setCondition(childCond);
							
							parent.removeChild(child);
							parent.addChild(grandchild);
						}
					}
				}
			}
		}
	}
	
	private static void removeTrueNodes(DocNode parent)
	{
		for (DocNode child : parent.getChildNodes().toArray(new DocNode[0]))
		{
			removeTrueNodes(child);

			if (child instanceof PredicateNode && ((PredicateNode) child).getCondition().equals(new True()))
			{
				for (DocNode grandchild : child.getChildNodes())
				{							
					parent.addChild(grandchild);
				}
				parent.removeChild(child);
			}
		}
	}
	
	private static void combineSamePredicates(DocNode parent)
	{
		List<PredicateNode> predicateChildren = new LinkedList<PredicateNode>();
		List<Condition> conditions = new LinkedList<Condition>();

		for (DocNode child : parent.getChildNodes())
		{
			if (child instanceof PredicateNode)
			{
				predicateChildren.add((PredicateNode) child);
				conditions.add(((PredicateNode) child).getCondition());
			}
		}

		ListIterator<Condition> conditionsIterator = conditions.listIterator(conditions.size());
		while (conditionsIterator.hasPrevious())
		{
			int conditionsIteratorIndex = conditionsIterator.previousIndex();
			Condition cond = conditionsIterator.previous();
			int firstIndex = conditions.indexOf(cond);
			if (conditionsIteratorIndex != firstIndex)
			{
				PredicateNode commonPredNode = predicateChildren.get(firstIndex);
				for (DocNode grandchild : predicateChildren.get(conditionsIteratorIndex).getChildNodes())
				{
					commonPredNode.addChild(grandchild);
				}

				parent.removeChild(predicateChildren.get(conditionsIteratorIndex));
				conditionsIterator.remove();
				predicateChildren.remove(conditionsIteratorIndex);
			}
		}
		
		for (DocNode child : parent.getChildNodes())
				combineSamePredicates(child);
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
