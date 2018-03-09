package raykernel.apps.deltadoc2;

import java.util.List;

import raykernel.apps.deltadoc2.record.ClassRecord;
import raykernel.apps.deltadoc2.record.MethodRecord;
import raykernel.apps.deltadoc2.record.RevisionRecord;
import raykernel.lang.cfg.CFG;
import raykernel.lang.cfg.CFGNode;
import raykernel.lang.cfg.FlowSet;
import raykernel.lang.cfg.StatementNode;
import raykernel.lang.cfg.Symbexe;
import raykernel.lang.dom.condition.Condition;
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
				cfg.print();
				processCFG(cfg, mr);
			}
		}
		
		return record;
	}
	
	public void processCFG(CFG cfg, MethodRecord record)
	{
		Symbexe exe = new Symbexe();
		
		exe.execute(cfg);
		
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
					
					record.put(symbStmt, pred);
					
					//System.out.println("If " + pred + "\n\t" + symbStmt + "\n\n");
				}
			}
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
