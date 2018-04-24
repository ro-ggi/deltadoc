package raykernel.lang.dom.expression;

import raykernel.lang.dom.condition.Condition;

public class PrefixExpression extends OneOpExpressionCondition
{
	
	String op;
	
	PrefixExpression(Expression exp, String op)
	{
		this.exp = exp;
		this.op = op;
	}
	
	@Override
	public String toString()
	{
		return op + exp;
	}
	
	@Override
	public Expression clone()
	{
		return new PrefixExpression(exp.clone(), op);
	}
	
	@Override
	public Condition negated()
	{
		if (op.equals("!") && exp instanceof Condition)
		{
			return (Condition) exp;
		}
		return super.negated();
	}
}
