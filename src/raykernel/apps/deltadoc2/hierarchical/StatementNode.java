package raykernel.apps.deltadoc2.hierarchical;

import raykernel.lang.dom.condition.Condition;
import raykernel.lang.dom.statement.Statement;

public class StatementNode extends DocNode
{
	Statement stmt;
	Condition altCondition;
	
	public Statement getStmt()
	{
		return stmt;
	}
	
	public void setAltCondition(Condition altCondition)
	{
		this.altCondition = altCondition;
	}
	
	public StatementNode(Statement statement)
	{
		stmt = statement;
	}
	
	@Override
	public String toString()
	{
		if (altCondition != null)
			return stmt + " instead of when " + altCondition;
		return stmt.toString();
	}
	
	@Override
	public int getMinCharIndex()
	{
		return stmt.getCharIndex();
	}
	
}
