package raykernel.apps.deltadoc2;

import java.util.LinkedList;
import java.util.List;

import raykernel.apps.deltadoc2.record.ClassRecord;
import raykernel.apps.deltadoc2.record.MethodRecord;
import raykernel.lang.dom.naming.MethodSignature;
import raykernel.lang.dom.naming.Type;
import raykernel.lang.dom.statement.VariableDeclarationStatement;

public class ClassDelta
{
	Type type;
	
	public List<VariableDeclarationStatement> addedFields = new LinkedList<>();
	public List<VariableDeclarationStatement> removedFields = new LinkedList<>();
	
	public List<MethodSignature> addedMethods = new LinkedList<MethodSignature>();
	public List<MethodSignature> removedMethods = new LinkedList<MethodSignature>();
	
	public List<MethodDelta> methodDeltas = new LinkedList<MethodDelta>();
	
	public ClassDelta(Type type)
	{
		this.type = type;
	}
	
	public static ClassDelta computeDelta(ClassRecord r1, ClassRecord r2)
	{
		ClassDelta ret = new ClassDelta(r1.getType());
		
		ret.compute(r1, r2);
		
		return ret;
	}
	
	public boolean isEmpty()
	{
		return addedFields.isEmpty() && removedFields.isEmpty() && addedMethods.isEmpty() && removedMethods.isEmpty()
				&& !hasChangedMethod();
	}
	
	public boolean hasChangedMethod()
	{
		for (MethodDelta md : methodDeltas)
		{
			if (!md.isEmpty())
				return true;
		}
		return false;
	}
	
	private void compute(ClassRecord r1, ClassRecord r2)
	{
		List<MethodSignature> matchedMethods = new LinkedList<MethodSignature>();
		
		// pair up method records : old vs new
		for (MethodRecord cd1 : r1.getMethodRecords())
		{
			MethodSignature t = cd1.getMethodSig();
			
			for (MethodRecord cd2 : r2.getMethodRecords())
			{
				if (t.equals(cd2.getMethodSig()))
				{
					matchedMethods.add(t);
					methodDeltas.add(MethodDelta.computeDelta(cd1, cd2));
				}
			}
		}
		
		//record unmatched methods
		for (MethodRecord cd : r1.getMethodRecords())
		{
			if (!matchedMethods.contains(cd.getMethodSig()))
			{
				removedMethods.add(cd.getMethodSig());
			}
		}
		for (MethodRecord cd : r2.getMethodRecords())
		{
			if (!matchedMethods.contains(cd.getMethodSig()))
			{
				addedMethods.add(cd.getMethodSig());
			}
		}
		
		// pair up fields
		List<VariableDeclarationStatement> matchedFields = new LinkedList<VariableDeclarationStatement>();
		
		// pair up method records : old vs new
		for (VariableDeclarationStatement vd1 : r1.getFields())
		{
			for (VariableDeclarationStatement vd2 : r2.getFields())
			{
				if (vd1.equals(vd2))
				{
					matchedFields.add(vd1);
				}
			}
		}
		
		//record unmatched methods
		for (VariableDeclarationStatement vd : r1.getFields())
		{
			if (!matchedFields.contains(vd))
			{
				removedFields.add(vd);
			}
		}
		for (VariableDeclarationStatement vd : r2.getFields())
		{
			if (!matchedFields.contains(vd))
			{
				addedFields.add(vd);
			}
		}
		
	}
	
	@Override
	public String toString()
	{
		if (isEmpty())
			return "[No Appreciable Change]";
		
		StringBuffer br = new StringBuffer();
		
		br.append("Changes to " + type + "\n----------------------------\n");
		
		//TODO added/removed Fields
		
		for (MethodSignature meth : removedMethods)
		{
			System.out.println("Removed Method: " + meth);
		}
		
		for (MethodSignature meth : addedMethods)
		{
			System.out.println("Added Method: " + meth);
		}
		
		for (MethodDelta md : methodDeltas)
		{
			if (!md.isEmpty())
			{
				br.append(md.toString() + "\n");
			}
		}
		
		return br.toString();
	}
	
	public Type getType()
	{
		return type;
	}
}
