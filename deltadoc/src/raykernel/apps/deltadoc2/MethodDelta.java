package raykernel.apps.deltadoc2;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import raykernel.apps.deltadoc2.record.MethodRecord;
import raykernel.apps.deltadoc2.record.StatementRecord;
import raykernel.lang.dom.condition.Condition;
import raykernel.lang.dom.condition.simplify.ConditionSimplifier;
import raykernel.lang.dom.naming.MethodSignature;
import raykernel.lang.dom.statement.Statement;
import raykernel.util.Tuple;

/**
 * Find out what's been added, changed, and removed
 * 
 * Added:
 *   New statement with new predicate
 * 
 * Changed: 
 *   (1) Same symbolic statement, different predicate
 *   (2) Same predicate, different statement
 * 
 * Removed:
 *   IF change reversed, statement would be added
 * 
 * @author buse
 *
 */
public class MethodDelta
{
	//If X Do Y instead of Z
	public Map<Condition, Tuple<List<StatementRecord>, List<StatementRecord>>> predToStmtMap = new HashMap<>();
	
	//If X Do Y instead of When Z
	public Map<Statement, Tuple<StatementRecord, StatementRecord>> stmtToPredMap = new HashMap<>();
	
	//Do  (added behavior)
	public List<StatementRecord> addedStmts = new LinkedList<>();
	
	//No longer  (removed behavior)
	public List<StatementRecord> removedStmts = new LinkedList<>();
	
	MethodSignature methodSig;
	
	public MethodDelta(MethodSignature methodSig)
	{
		this.methodSig = methodSig;
	}
	
	//BUG FOUND HERE
	public boolean isEmpty()
	{
		return predToStmtMap.isEmpty() && stmtToPredMap.isEmpty() && addedStmts.isEmpty() && removedStmts.isEmpty();
	}
	
	public static MethodDelta computeDelta(MethodRecord r1, MethodRecord r2)
	{
		MethodDelta ret = new MethodDelta(r1.getMethodSig());
		
		ret.compute(r1, r2);
		
		return ret;
	}
	
	private void compute(MethodRecord r1, MethodRecord r2)
	{
		// remove from consideration all matching statements (same stmt, same predicate)
		// they are the unchanged.
		//System.out.println("Comparing two versions of " + r1.getMethodSig());
		
		//System.out.println("Num Stmts: " + r1.getStatements().size());
		
		Set<StatementRecord> stmts1_in = r1.getStatements();
		Set<StatementRecord> stmts2_in = r2.getStatements();
		
		Set<StatementRecord> stmts1 = new HashSet<>();
		Set<StatementRecord> stmts2 = new HashSet<>();
		
		//simplify all the predicates
		
		for (StatementRecord rec : stmts1_in)
		{
			rec.setPredicate(ConditionSimplifier.simplify(rec.getPredicate()));
			stmts1.add(rec);
		}
		
		for (StatementRecord rec : stmts2_in)
		{
			rec.setPredicate(ConditionSimplifier.simplify(rec.getPredicate()));
			stmts2.add(rec);
		}
		
		Set<StatementRecord> stmts_only1 = new HashSet<>();
		stmts_only1.addAll(stmts1);
		stmts_only1.removeAll(stmts2);
		
		//System.out.println("\n\n");
		
		Set<StatementRecord> stmts_only2 = new HashSet<>();
		stmts_only2.addAll(stmts2);
		stmts_only2.removeAll(stmts1);
		
		///System.out.println("only1: " + stmts_only1);
		//System.out.println("only2: " + stmts_only2);
		
		// make map:  predicate -> ( [old_stmts], [new_stmts] )
		
		for (StatementRecord stmt : stmts_only1)
		{
			Tuple<List<StatementRecord>, List<StatementRecord>> recordLists = predToStmtMap.get(stmt.getPredicate());
			
			if (recordLists == null)
			{
				recordLists = makeRecordListTuple();
				predToStmtMap.put(stmt.getPredicate(), recordLists);
			}
			
			recordLists.first.add(stmt);
		}
		
		for (StatementRecord stmt : stmts_only2)
		{
			Tuple<List<StatementRecord>, List<StatementRecord>> recordLists = predToStmtMap.get(stmt.getPredicate());
			
			if (recordLists == null)
			{
				recordLists = makeRecordListTuple();
				predToStmtMap.put(stmt.getPredicate(), recordLists);
			}
			
			recordLists.second.add(stmt);
		}
		
		// for stmts where:  predicate -> ( [], [new_stmts] ) or ( [old_stmts], [] )
		
		List<StatementRecord> noCommonPred_old = new LinkedList<StatementRecord>();
		List<StatementRecord> noCommonPred_new = new LinkedList<StatementRecord>();
		
		List<Condition> predsToRemove = new LinkedList<Condition>();
		
		for (Condition pred : predToStmtMap.keySet())
		{
			Tuple<List<StatementRecord>, List<StatementRecord>> recordLists = predToStmtMap.get(pred);
			
			if (recordLists.first.isEmpty())
			{
				noCommonPred_new.addAll(recordLists.second);
				predsToRemove.add(pred);
			}
			else if (recordLists.second.isEmpty())
			{
				noCommonPred_old.addAll(recordLists.first);
				predsToRemove.add(pred);
			}
		}
		
		for (Condition pred : predsToRemove)
		{
			predToStmtMap.remove(pred);
		}
		
		// make map:  stmt -> ( old_pred, new_pred )
		
		for (StatementRecord stmt : noCommonPred_old)
		{
			Tuple<StatementRecord, StatementRecord> tuple = stmtToPredMap.get(stmt.getStatement());
			
			if (tuple == null)
			{
				tuple = new Tuple<>();
				stmtToPredMap.put(stmt.getStatement(), tuple);
			}
			
			tuple.first = stmt;
		}
		
		for (StatementRecord stmt : noCommonPred_new)
		{
			Tuple<StatementRecord, StatementRecord> tuple = stmtToPredMap.get(stmt.getStatement());
			
			if (tuple == null)
			{
				tuple = new Tuple<>();
				stmtToPredMap.put(stmt.getStatement(), tuple);
			}
			
			tuple.second = stmt;
		}
		
		// for stmts where:  stmt -> ( _ , new_pred ) or ( old_pred, _ )
		// ( _ , new_pred ) -> added_stmts
		// ( old_pred, _ ) -> removed stmts
		
		List<Statement> stmtsToRemove = new LinkedList<Statement>();
		
		for (Statement stmt : stmtToPredMap.keySet())
		{
			Tuple<StatementRecord, StatementRecord> tuple = stmtToPredMap.get(stmt);
			
			if (tuple.first == null)
			{
				addedStmts.add(tuple.second);
				stmtsToRemove.add(stmt);
			}
			else if (tuple.second == null)
			{
				removedStmts.add(tuple.first);
				stmtsToRemove.add(stmt);
			}
		}
		
		for (Statement stmt : stmtsToRemove)
		{
			stmtToPredMap.remove(stmt);
		}
	}
	
	private Tuple<List<StatementRecord>, List<StatementRecord>> makeRecordListTuple()
	{
		Tuple<List<StatementRecord>, List<StatementRecord>> recordLists = new Tuple<>();
		
		List<StatementRecord> oldList = new LinkedList<StatementRecord>();
		List<StatementRecord> newList = new LinkedList<StatementRecord>();
		
		recordLists.first = oldList;
		recordLists.second = newList;
		
		return recordLists;
	}
	
	@Override
	public String toString()
	{
		if (isEmpty())
			return "[No Appreciable Change]";
		
		StringBuffer br = new StringBuffer();
		
		br.append("When calling " + getMethodSig() + "\n");
		
		if (!removedStmts.isEmpty())
		{
			br.append("No Longer\n");
			
			for (StatementRecord stmt : removedStmts)
			{
				br.append("If " + stmt.predicate + "\n");
				br.append("   " + stmt.statement + "\n");
			}
		}
		
		if (!addedStmts.isEmpty())
		{
			br.append("\n");
			
			for (StatementRecord stmt : addedStmts)
			{
				br.append("If " + stmt.predicate + "\n");
				br.append("   " + stmt.statement + "\n");
			}
		}
		
		if (!predToStmtMap.isEmpty())
		{
			for (Condition pred : predToStmtMap.keySet())
			{
				br.append("If " + pred + "\n");
				
				//All of these have the same predicate
				Tuple<List<StatementRecord>, List<StatementRecord>> tuple = predToStmtMap.get(pred);
				
				for (StatementRecord sr : tuple.second)
				{
					br.append("   " + sr.statement + "\n");
				}
				
				br.append("  Instead of \n");
				
				for (StatementRecord sr : tuple.first)
				{
					br.append("   " + sr.statement + "\n");
				}
			}
		}
		
		if (!stmtToPredMap.isEmpty())
		{
			for (Statement s : stmtToPredMap.keySet())
			{
				Tuple<StatementRecord, StatementRecord> tuple = stmtToPredMap.get(s);
				
				//these are the same stmt with different predicate
				StatementRecord oldstmt = tuple.first;
				StatementRecord newstmt = tuple.second;
				
				br.append("If " + newstmt.predicate + "\n");
				br.append("   " + newstmt.statement + " instead of when " + oldstmt.predicate + "\n");
			}
		}
		
		return br.toString();
	}
	
	public MethodSignature getMethodSig()
	{
		return methodSig;
	}
}
