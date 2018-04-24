package raykernel.apps.deltadoc2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import raykernel.apps.deltadoc2.record.ClassRecord;
import raykernel.apps.deltadoc2.record.MethodRecord;
import raykernel.apps.deltadoc2.record.RevisionRecord;
import raykernel.apps.deltadoc2.record.StatementRecord;
import raykernel.lang.cfg.CFG;
import raykernel.lang.cfg.CFGNode;
import raykernel.lang.cfg.FlowSet;
import raykernel.lang.cfg.StatementNode;
import raykernel.lang.cfg.Symbexe;
import raykernel.lang.dom.condition.AndCondition;
import raykernel.lang.dom.condition.Condition;
import raykernel.lang.dom.condition.OrCondition;
import raykernel.lang.dom.expression.True;
import raykernel.lang.dom.naming.MethodSignature;
import raykernel.lang.dom.statement.Statement;
import raykernel.lang.parse.ClassDeclaration;

/**
 * Input: Any java file
 * Output: a map from (symbolic) statements to path predicates
 * @author buse
 *
 */
public class PreProcess
{
	
	public RevisionRecord process(List<ClassDeclaration> classes)
	{
		RevisionRecord record = new RevisionRecord();
		
		for (ClassDeclaration cd : classes)
		{
			//handle fields and stuff
			ClassRecord cr = new ClassRecord(cd);
			record.addClassRecord(cr);
			
			for (MethodSignature meth : cd.getMethods())
			{
				MethodRecord mr = new MethodRecord(meth);
				cr.addMethodRecord(mr);
				
				CFG cfg = meth.getCFG();
//				cfg.print();
				processCFG(cfg, mr);
			}
		}
		
		return record;
	}
	
	public void processCFG(CFG cfg, MethodRecord record)
	{
		Symbexe exe = new Symbexe();
		
		exe.execute(cfg);
		
		Map<Statement, List<Condition>> statmentToConditionsMap = new HashMap<>();
		
		for (CFGNode node : cfg.getNodes())
		{
			if (node instanceof StatementNode)
			{
				Statement s = ((StatementNode) node).getStatement();
				
				if (!Policy.mustDoc(s))
				{
					continue;
				}
				
				//System.out.println("Processing Statement: " + s);

				List<FlowSet> flowSets = exe.getFlowSetsFor(node);

				for (FlowSet flowSet : flowSets)
				{
					Statement symbStmt = flowSet.computeSymbolicStmt(s);
					Condition pred = flowSet.computeSymbolicPredicate();
					pred = (Condition) pred.clone();
					List<Condition> conditions = statmentToConditionsMap.get(symbStmt);

					// havent seen this stmt before
					if (conditions == null)
					{
						conditions = new ArrayList<>();
						conditions.add(pred);
						statmentToConditionsMap.put(symbStmt, conditions);
					}
					// or add this condition
					else
					{
						// Remove if, else conditions after the paths flow again together: if (c) ...,
						// else ... results in path predicate if (c & !c) which is equivalent to if
						// true.
						if (!conditions.isEmpty() && !(conditions.get(0) instanceof True) && !(pred instanceof True))
						{
							Iterator<Condition> i = conditions.iterator();
							while (i.hasNext())
							{
								Condition c1 = i.next();
								Iterator<Condition> ii = ((AndCondition) c1).iterator();
								while (ii.hasNext())
								{
									Condition c2 = ii.next();
									Iterator<Condition> iii = ((AndCondition) pred).iterator();
									while (iii.hasNext())
									{
										Condition c3 = iii.next();
										// Do not merge sub conditions with mergeSubConditions(), next check depends on
										// original form.
										if (c2.equals(c3.negated()))
										{
											ii.remove();
											iii.remove();
											break;
										}
									}
								}
							}
						}

						if (pred instanceof True || ((AndCondition) pred).getConditions().isEmpty())
						{
							conditions = new ArrayList<>();
							conditions.add(new True());
							statmentToConditionsMap.put(symbStmt, conditions);
						} else
						{
							conditions.add(pred);
						}
					}
				}
			}
		}
		
		for (Statement symbStmt : statmentToConditionsMap.keySet())
		{
			Condition[] conditions = statmentToConditionsMap.get(symbStmt).toArray(new Condition[0]);
			Condition pred;
			if (conditions[0] instanceof True)
				pred = new True();
			else
				pred = new OrCondition(conditions);
			record.put(symbStmt, pred);
		}
	}
	
	void dispSymbexeResults(CFG cfg)
	{
		Symbexe exe = new Symbexe();
		
		exe.execute(cfg);
		
		for (CFGNode node : cfg.getNodes())
		{
			if (node instanceof StatementNode)
			{
				Statement s = ((StatementNode) node).getStatement();
				
				//System.out.println("Processing Statement: " + s);
				
				List<FlowSet> flowSets = exe.getFlowSetsFor(node);
				
				for (FlowSet flowSet : flowSets)
				{
					Statement symbStmt = flowSet.computeSymbolicStmt(s);
					Condition pred = flowSet.computeSymbolicPredicate();
					
					//System.out.println("If " + pred + "\n\t" + symbStmt + "\n\n");
				}
			}
		}
	}
	
}
