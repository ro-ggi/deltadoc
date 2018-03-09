package raykernel.apps.deltadoc2;

import java.util.LinkedList;
import java.util.List;

import raykernel.apps.deltadoc2.record.ClassRecord;
import raykernel.apps.deltadoc2.record.RevisionRecord;
import raykernel.lang.dom.naming.Type;

public class DeltaDoc
{
	List<Type> addedClasses = new LinkedList<Type>();
	List<Type> removedClasses = new LinkedList<Type>();
	
	List<ClassDelta> classDeltas = new LinkedList<ClassDelta>();
	
	public static DeltaDoc computeDelta(RevisionRecord r1, RevisionRecord r2)
	{
		DeltaDoc ret = new DeltaDoc();
		
		ret.compute(r1, r2);
		
		return ret;
	}
	
	public boolean isEmpty()
	{
		return addedClasses.isEmpty() && removedClasses.isEmpty() && !hasChangedClass();
	}
	
	public boolean hasChangedClass()
	{
		for (ClassDelta cd : classDeltas)
		{
			if (!cd.isEmpty())
				return true;
		}
		return false;
	}
	
	private void compute(RevisionRecord r1, RevisionRecord r2)
	{
		List<Type> matchedTypes = new LinkedList<Type>();
		
		// pair up class records : old vs new
		for (ClassRecord cd1 : r1.getClassRecords())
		{
			Type t = cd1.getType();
			
			for (ClassRecord cd2 : r2.getClassRecords())
			{
				if (t.equals(cd2.getType()))
				{
					matchedTypes.add(t);
					classDeltas.add(ClassDelta.computeDelta(cd1, cd2));
				}
			}
		}
		
		//record unmatched classes
		for (ClassRecord cd : r1.getClassRecords())
		{
			if (!matchedTypes.contains(cd.getType()))
			{
				removedClasses.add(cd.getType());
			}
		}
		for (ClassRecord cd : r2.getClassRecords())
		{
			if (!matchedTypes.contains(cd.getType()))
			{
				addedClasses.add(cd.getType());
			}
		}
	}
	
	@Override
	public String toString()
	{
		if (isEmpty())
			return "[No Appreciable Change]";
		
		StringBuffer br = new StringBuffer();
		
		for (Type added : addedClasses)
		{
			br.append("Added class: " + added + "\n");
		}
		
		for (Type removed : removedClasses)
		{
			br.append("Removed class: " + removed + "\n");
		}
		
		for (ClassDelta cd : classDeltas)
		{
			if (!cd.isEmpty())
			{
				br.append(cd.toString());
			}
		}
		
		return br.toString();
	}
	
	public List<ClassDelta> getChangedClasses()
	{
		return classDeltas;
	}
	
}