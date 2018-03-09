package raykernel.apps.deltadoc2.record;

import java.util.LinkedList;
import java.util.List;

public class RevisionRecord
{
	List<ClassRecord> classRecords = new LinkedList<ClassRecord>();
	
	public RevisionRecord()
	{
		
	}
	
	public void addClassRecord(ClassRecord cr)
	{
		classRecords.add(cr);
	}
	
	public List<ClassRecord> getClassRecords()
	{
		return classRecords;
	}
}
