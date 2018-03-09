package raykernel.lang.dom.statement;

import java.io.Serializable;
import java.util.Collection;

import raykernel.lang.dom.expression.Expression;

public abstract class Statement implements Serializable
{
	int charIndex = -1; //useful for ordering later
	
	public abstract Collection<Expression> getSubExpressions();
	
	public abstract void substitute(Expression oldExp, Expression newExp);
	
	@Override
	public abstract Statement clone();
	
	@Override
	public boolean equals(Object o)
	{
		boolean result = false;
		
		result = toString().equals(o.toString());
		
		//System.out.println(this + " ?= " + o + " : " + result);
		
		return result;
	}
	
	@Override
	public int hashCode()
	{
		System.out.println(this + " hash= " + toString().hashCode());
		
		return toString().hashCode();
	}
	
	public void setCharIndex(int charIndex)
	{
		this.charIndex = charIndex;
	}
	
	public int getCharIndex()
	{
		return charIndex;
	}
}
