package raykernel.apps.deltadoc2.record;

import java.util.LinkedList;
import java.util.List;

import raykernel.lang.dom.naming.Type;
import raykernel.lang.dom.statement.VariableDeclarationStatement;
import raykernel.lang.parse.ClassDeclaration;

public class ClassRecord
{
	ClassDeclaration classDeclaration;
	
	List<MethodRecord> methodRecords = new LinkedList<MethodRecord>();
	
	public ClassRecord(ClassDeclaration classDeclaration)
	{
		this.classDeclaration = classDeclaration;
	}
	
	public void addMethodRecord(MethodRecord methodRecord)
	{
		methodRecords.add(methodRecord);
	}
	
	public Type getType()
	{
		return classDeclaration.getType();
	}
	
	public List<MethodRecord> getMethodRecords()
	{
		return methodRecords;
	}
	
	public List<VariableDeclarationStatement> getFields()
	{
		return classDeclaration.getFields();
	}
}
