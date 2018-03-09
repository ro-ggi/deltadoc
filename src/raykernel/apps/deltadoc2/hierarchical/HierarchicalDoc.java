package raykernel.apps.deltadoc2.hierarchical;

import java.util.List;

import raykernel.apps.deltadoc2.ClassDelta;
import raykernel.apps.deltadoc2.DeltaDoc;
import raykernel.apps.deltadoc2.MethodDelta;
import raykernel.apps.deltadoc2.record.StatementRecord;
import raykernel.lang.dom.condition.Condition;
import raykernel.lang.dom.naming.MethodSignature;
import raykernel.lang.dom.statement.Statement;
import raykernel.lang.dom.statement.VariableDeclarationStatement;
import raykernel.util.Tuple;

public class HierarchicalDoc
{
	public static DocNode makeDoc(DeltaDoc doc)
	{
		//TODO added / removed classes
		return makeDoc(doc.getChangedClasses());
	}
	
	public static DocNode makeDoc(List<ClassDelta> classDeltas)
	{
		RootNode ret = new RootNode();
		
		for (ClassDelta cd : classDeltas)
		{
			if (!cd.isEmpty())
			{
				ClassNode cn = new ClassNode(cd.getType().toString());
				ret.addChild(cn);
				
				//Added Fields
				for (VariableDeclarationStatement d : cd.addedFields)
				{
					FieldNode fn = new FieldNode(d, true);
					cn.addChild(fn);
				}
				
				//Removed Fields
				for (VariableDeclarationStatement d : cd.removedFields)
				{
					FieldNode fn = new FieldNode(d, false);
					cn.addChild(fn);
				}
				
				//Added Methods
				for (MethodSignature d : cd.addedMethods)
				{
					MethodNameNode fn = new MethodNameNode(d, true);
					cn.addChild(fn);
				}
				
				//Removed Methods
				for (MethodSignature d : cd.removedMethods)
				{
					MethodNameNode fn = new MethodNameNode(d, false);
					cn.addChild(fn);
				}
				
				//Changed Methods
				for (MethodDelta md : cd.methodDeltas)
				{
					if (!md.isEmpty())
					{
						MethodNode mn = makeDoc(md);
						cn.addChild(mn);
					}
				}
				
			}
		}
		
		if (ret.getChildNodes().isEmpty())
		{
			ret.addChild(new EmptyNode());
		}
		
		return ret;
	}
	
	public static MethodNode makeDoc(MethodDelta md)
	{
		MethodNode ret = new MethodNode(md.getMethodSig().toString());
		
		if (!md.removedStmts.isEmpty())
		{
			NoLongerNode nl = new NoLongerNode();
			
			for (StatementRecord stmt : md.removedStmts)
			{
				PredicateNode pn = new PredicateNode(stmt.getPredicate());
				StatementNode sn = new StatementNode(stmt.getStatement());
				pn.addChild(sn);
				nl.addChild(sn);
			}
			
			ret.addChild(nl);
		}
		
		for (StatementRecord stmt : md.addedStmts)
		{
			PredicateNode pn = new PredicateNode(stmt.getPredicate());
			StatementNode sn = new StatementNode(stmt.getStatement());
			pn.addChild(sn);
			ret.addChild(pn);
		}
		
		for (Condition pred : md.predToStmtMap.keySet())
		{
			PredicateNode pn = new PredicateNode(pred);
			
			DoNode dn = new DoNode();
			InsteadOfNode in = new InsteadOfNode();
			
			//All of these have the same predicate
			Tuple<List<StatementRecord>, List<StatementRecord>> tuple = md.predToStmtMap.get(pred);
			
			//current stmts
			for (StatementRecord sr : tuple.second)
			{
				StatementNode sn = new StatementNode(sr.getStatement());
				dn.addChild(sn);
			}
			
			//prev stmts
			for (StatementRecord sr : tuple.first)
			{
				StatementNode sn = new StatementNode(sr.getStatement());
				in.addChild(sn);
			}
			
			pn.addChild(dn);
			pn.addChild(in);
			ret.addChild(pn);
		}
		
		for (Statement s : md.stmtToPredMap.keySet())
		{
			Tuple<StatementRecord, StatementRecord> tuple = md.stmtToPredMap.get(s);
			
			//these are the same stmt with different predicate
			StatementRecord oldstmt = tuple.first;
			StatementRecord newstmt = tuple.second;
			
			PredicateNode pn = new PredicateNode(newstmt.predicate);
			StatementNode sn = new StatementNode(newstmt.statement);
			sn.setAltCondition(oldstmt.predicate);
			
			pn.addChild(sn);
			ret.addChild(pn);
		}
		
		return ret;
	}
	
}
