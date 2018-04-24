package raykernel.apps.deltadoc2.record;

import java.util.HashSet;
import java.util.Set;

import raykernel.lang.dom.condition.Condition;
import raykernel.lang.dom.naming.MethodSignature;
import raykernel.lang.dom.statement.Statement;

public class MethodRecord
{
	MethodSignature sig;
	
	Set<StatementRecord> stmtRecords = new HashSet<>();
	
	public MethodRecord(MethodSignature sig)
	{
		this.sig = sig;
	}
	
	public void put(Statement symbStmt, Condition pred)
	{
		StatementRecord record = new StatementRecord(pred, symbStmt);
		stmtRecords.add(record);
	}
	
	public MethodSignature getMethodSig()
	{
		return sig;
	}
	
	public Set<StatementRecord> getStatements()
	{
		return stmtRecords;
	}
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((sig == null) ? 0 : sig.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MethodRecord other = (MethodRecord) obj;
		if (sig == null)
		{
			if (other.sig != null)
				return false;
		}
		else if (!sig.equals(other.sig))
			return false;
		return true;
	}
	
}
