package raykernel.apps.deltadoc2.record;

import raykernel.lang.dom.condition.Condition;
import raykernel.lang.dom.statement.Statement;

public class StatementRecord
{
	private Condition predicate;
	private Statement statement;
	
	public StatementRecord(Condition predicate, Statement statement)
	{
		this.predicate = predicate;
		this.statement = statement;
	}
	
	public Condition getPredicate()
	{
		return predicate;
	}
	
	public Statement getStatement()
	{
		return statement;
	}
	
	@Override
	public int hashCode()
	{
		return predicate.hashCode() + statement.hashCode() + 3;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		boolean result = false;
		
		if (obj instanceof StatementRecord)
		{
			if (((StatementRecord) obj).predicate.equals(predicate)
					&& ((StatementRecord) obj).statement.equals(statement))
			{
				result = true;
			}
		}
		
		return result;
	}
	
	public StatementRecord setPredicate(Condition pred)
	{
		return new StatementRecord(pred, statement);
	}
	
	@Override
	public String toString()
	{
		return "If " + predicate + " Do " + statement;
	}
	
}
