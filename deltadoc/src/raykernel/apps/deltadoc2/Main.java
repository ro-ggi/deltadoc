package raykernel.apps.deltadoc2;

import java.io.File;
import java.util.List;

import raykernel.apps.deltadoc2.hierarchical.CombinePredicates;
import raykernel.apps.deltadoc2.hierarchical.DocNode;
import raykernel.apps.deltadoc2.hierarchical.HierarchicalDoc;
import raykernel.apps.deltadoc2.print.DocPrinter;
import raykernel.apps.deltadoc2.print.DocToDtree;
import raykernel.apps.deltadoc2.print.DocToHTML;
import raykernel.apps.deltadoc2.print.DocToPlainText;
import raykernel.apps.deltadoc2.record.RevisionRecord;
import raykernel.io.FileReader;
import raykernel.lang.parse.ClassDeclaration;
import raykernel.lang.parse.EclipseCFGParser;

public class Main
{
	public static void main(String[] args)
	{
		DocPrinter print = new DocToPlainText();
		int firstArg = 0;
		
		if (args[0].equals("-h"))
		{
			print = new DocToHTML();
			firstArg = 1;
		}
		else if (args[0].equals("-d"))
		{
			print = new DocToDtree();
			firstArg = 1;
		}
		else if (args[0].equals("-p"))
		{
			//keep defaullt
			firstArg = 1;
		}
		
		try
		{
			//*** read in files ***
			File f1 = new File(args[firstArg]);
			File f2 = new File(args[firstArg + 1]);
			
			String source1 = FileReader.readFile(f1);
			String source2 = FileReader.readFile(f2);
			
			//*** parse them, create CFGs ***
			EclipseCFGParser parser = new EclipseCFGParser();
			List<ClassDeclaration> classes1 = parser.parse(source1);
			List<ClassDeclaration> classes2 = parser.parse(source2);
			
			//*** enumerate paths, do symbolic execution, make change records ***
			PreProcess pp = new PreProcess();
			RevisionRecord r1 = pp.process(classes1);
			RevisionRecord r2 = pp.process(classes2);
			
			//*** Discover what changed and needs to be documented ***
			DeltaDoc doc = DeltaDoc.computeDelta(r1, r2);
			
			//*** format / distill hierarchical documentation ***
			DocNode output = HierarchicalDoc.makeDoc(doc);
			CombinePredicates.process(output);
			
			//*** print it ***
			System.out.println(print.print(output));
		}
		catch (Exception e)
		{
			System.out.println("!Error: " + e);
			e.printStackTrace();
		}
	}
}
