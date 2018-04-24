package raykernel.lang.dom.expression;

import raykernel.lang.dom.condition.Condition;

public class ParenthesizedExpression extends OneOpExpressionCondition
{
	
	public ParenthesizedExpression(Expression exp)
	{
		this.exp = exp;
	}
	
	@Override
	public String toString()
	{
		return "(" + exp + ")";
	}
	
	@Override
	public Expression clone()
	{
		return new ParenthesizedExpression(exp.clone());
	}
	
	@Override
	public Condition negated()
	{
		return new PrefixExpression(this, "!");
	}
}
