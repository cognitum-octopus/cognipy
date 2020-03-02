package cognipyunittests;

import CogniPy.*;
import NUnit.Framework.*;
import java.util.*;
import java.io.*;

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [TestFixture] public class ModularisationTests
public class ModularisationTests
{
	public static String getAssemblyDirectory()
	{
		String codeBase = Assembly.GetExecutingAssembly().CodeBase;
		UriBuilder uri = new UriBuilder(codeBase);
		String path = Uri.UnescapeDataString(uri.Path);
		return (new File(path)).getParent();
	}

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [Test] public void SR14BUG()
	public final void SR14BUG()
	{
		CogniPySvr reasoner = new CogniPySvr();
		reasoner.LoadCnl(Path.Combine(getAssemblyDirectory(), "TestFiles", "TestTBox.encnl"), true, false, false);
		HashMap<String, CogniPy.models.InstanceDescription> desc = reasoner.DescribeInstances("Nikolai");
		Assert.IsEmpty(desc);
	}

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [Test] public void SR11BUG()
	public final void SR11BUG()
	{
		String[] toinstert = new String[]{"Deal-Criteria-2 is a reputational-risk-network-deal-criteria.", "Deal-Criteria-2 is a reputational-risk-network-component.", "Deal-Criteria-2 has-sql-name equal-to ''.", "Deal-Criteria-2 has-availability equal-to 'Data-Available'.", "Deal-Criteria-2 has-unit equal-to ''.", "Deal-Criteria-2 has-aggregation equal-to 'None'.", "Annotations:\r\n_Deal-Criteria-2 Instance: node-label 'Importance'@en\r\n_Deal-Criteria-2 Instance: node-description 'Importance'@en\r\n."};
		CogniPySvr reasoner = new CogniPySvr();
		reasoner.LoadCnl(Path.Combine(getAssemblyDirectory(), "TestFiles", "ont2.encnl"), true, true, false);
		for (String l : toinstert)
		{
			reasoner.KnowledgeInsert(l, true, true);
		}
		ArrayList<String> sups = reasoner.GetSuperConceptsOf("Deal-Criteria-2", false);
		assert sups.size() == 4;

		HashMap<String, CogniPy.models.InstanceDescription> descr = reasoner.DescribeInstances("Deal-Criteria-2");
		assert descr.size() == 1;
	}

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [Test] public void StrageBug2a2BUG()
	public final void StrageBug2a2BUG()
	{
		CogniPySvr reasoner = new CogniPySvr();
		reasoner.LoadCnlFromString("Bubu influences-with-weight-of-1 Dudu.", true, true, false);
		String toDel = "Bubu influence-with-weight-of-1 Dudu.";
		reasoner.KnowledgeDelete(toDel, true);
		String cnl = reasoner.ToCNL(false);
	}

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [Test] public void SR15BUG()
	public final void SR15BUG()
	{
		CogniPySvr reasoner = new CogniPySvr();
		String ontologyPath = Path.Combine(getAssemblyDirectory(), "TestFiles", "TestOntology.encnl");
		reasoner.LoadCnl(ontologyPath, true, true, false);

		final String concept = "reputational-risk-network-component";

		ArrayList<String> instances = reasoner.GetInstancesOf(concept, false);

		java.lang.Iterable<CogniPy.models.InstanceDescription> descriptionsBeforeDeletion = reasoner.DescribeInstancesByName(instances);

		ArrayList<String> toDel = new ArrayList<String>(Arrays.asList("Deal-Criteria-14 is a deal-criteria.", "Deal-Criteria-11 is a deal-criteria.", "Deal-Criteria-26 is a deal-criteria.", "Deal-Criteria-8 is a deal-criteria.", "Deal-Criteria-2 is a deal-criteria.", "Deal-Criteria-20 is a deal-criteria.", "Deal-Criteria-23 is a deal-criteria.", "Deal-Criteria-5 is a deal-criteria.", "Deal-Criteria-17 is a deal-criteria."));
		reasoner.KnowledgeDelete(tangible.StringHelper.join("\r\n", toDel), true);

		java.lang.Iterable<CogniPy.models.InstanceDescription> descriptionsAfterDeletion = reasoner.DescribeInstancesByName(instances);
	}
}