package raykernel.apps.deltadoc2;

import raykernel.lang.dom.expression.Expression;
import raykernel.lang.dom.expression.InvocationExpression;
import raykernel.lang.dom.statement.ExpressionStatement;
import raykernel.lang.dom.statement.ReturnStatement;
import raykernel.lang.dom.statement.Statement;
import raykernel.lang.dom.statement.ThrowStatement;

public class Policy
{
	/**
	 * Which statements should be documented?
	 */
	public static boolean mustDoc(Statement s)
	{
		if (s instanceof ReturnStatement)
			return true;
		
		if (s instanceof ThrowStatement)
			return true;
		
		if (s instanceof ExpressionStatement)
		{
			Expression e = ((ExpressionStatement) s).getExpression();
			
			if (e instanceof InvocationExpression)
				return true;
		}
		
		return false;
	}
	
}
