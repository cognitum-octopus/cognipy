package cognipyunittests;

import CogniPy.*;
import NUnit.Framework.*;
import java.util.*;

public class ConcurrencyTests
{

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [Test] public void NewJenaBug()
	public final void NewJenaBug()
	{
		CogniPySvr client = InitializeClient("IntegrationTestsTBox.encnl");

		HashMap<String, CogniPy.models.InstanceDescription> tt = client.DescribeInstances("a thing");
		Assert.IsNotEmpty(tt);
	}

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [Test] public void NewJenaBug2()
	public final void NewJenaBug2()
	{
		CogniPySvr client = InitializeClient("TestTBox.encnl");

		ArrayList<String> inst0 = client.GetInstancesOf("field-1-1-1", false); // No results
		ArrayList<String> all = client.GetInstancesOf("thing", false); // OK
		ArrayList<String> inst1 = client.GetInstancesOf("field-1-1-1", false); // OK
		Assert.IsNotEmpty(inst0);
		Assert.IsNotEmpty(all);
		CollectionAssert.AreEquivalent(inst0, inst1);
	}

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [Test] public void NewJenaBug3()
	public final void NewJenaBug3()
	{
		CogniPySvr client = InitializeClient("TestTBox.encnl");

		//            var inst0 = client.GetInstancesOf("field-1-1-1", false); // No results

		client.KnowledgeInsert("Element-1-Form-D-13-09-2018-T-16-59-42[<http://www.sfo.cognitum.eu/Survey/c3a9ab53-4423-4354-885d-2776ba98505d#>] is a element-1-form." + "\r\n" +
"Survey[<http://www.sfo.cognitum.eu/Survey/c3a9ab53-4423-4354-885d-2776ba98505d#>] is a subject[sfo]." + "\r\n" +
"Element-1-Form-D-13-09-2018-T-16-59-42[<http://www.sfo.cognitum.eu/Survey/c3a9ab53-4423-4354-885d-2776ba98505d#>] concern[sfo] Survey[<http://www.sfo.cognitum.eu/Survey/c3a9ab53-4423-4354-885d-2776ba98505d#>]." + "\r\n" +
"Survey[<http://www.sfo.cognitum.eu/Survey/c3a9ab53-4423-4354-885d-2776ba98505d#>] is-concerned-by[sfo] Element-1-Form-D-13-09-2018-T-16-59-42[<http://www.sfo.cognitum.eu/Survey/c3a9ab53-4423-4354-885d-2776ba98505d#>]." + "\r\n" +
"Element-1-1-Section-D-13-09-2018-T-16-59-42[<http://www.sfo.cognitum.eu/Survey/c3a9ab53-4423-4354-885d-2776ba98505d#>] is a element-1-1-section." + "\r\n" +
"Element-1-Form-D-13-09-2018-T-16-59-42[<http://www.sfo.cognitum.eu/Survey/c3a9ab53-4423-4354-885d-2776ba98505d#>] concern[sfo] Element-1-1-Section-D-13-09-2018-T-16-59-42[<http://www.sfo.cognitum.eu/Survey/c3a9ab53-4423-4354-885d-2776ba98505d#>].", true, true);

		client.KnowledgeInsert("Element-1-1-Section-D-13-09-2018-T-16-59-42[<http://www.sfo.cognitum.eu/Survey/c3a9ab53-4423-4354-885d-2776ba98505d#>] have-field-1-1-1 equal-to 'Krzysztof'." + "\r\n" +
"Element-1-1-Section-D-13-09-2018-T-16-59-42[<http://www.sfo.cognitum.eu/Survey/c3a9ab53-4423-4354-885d-2776ba98505d#>] have-field-1-1-2 equal-to 'sdsdsd'." + "\r\n" +
"Element-1-1-Section-D-13-09-2018-T-16-59-42[<http://www.sfo.cognitum.eu/Survey/c3a9ab53-4423-4354-885d-2776ba98505d#>] have-field-1-1-3 equal-to 'k.cieslinski@cognitum.eu'." + "\r\n" +
"Element-1-1-Section-D-13-09-2018-T-16-59-42[<http://www.sfo.cognitum.eu/Survey/c3a9ab53-4423-4354-885d-2776ba98505d#>] have-field-1-1-4 equal-to '384753443544'." + "\r\n" +
"Element-1-1-Section-D-13-09-2018-T-16-59-42[<http://www.sfo.cognitum.eu/Survey/c3a9ab53-4423-4354-885d-2776ba98505d#>] have-field-1-1-5 equal-to 'Cognitum'." + "\r\n" +
"Element-1-1-Section-D-13-09-2018-T-16-59-42[<http://www.sfo.cognitum.eu/Survey/c3a9ab53-4423-4354-885d-2776ba98505d#>] have-field-1-1-6 equal-to 'sdsds'." + "\r\n" +
"Element-1-1-Section-D-13-09-2018-T-16-59-42[<http://www.sfo.cognitum.eu/Survey/c3a9ab53-4423-4354-885d-2776ba98505d#>] have-field-1-1-7 Answer-1-1-7-4." + "\r\n" +
"Element-1-1-Section-D-13-09-2018-T-16-59-42[<http://www.sfo.cognitum.eu/Survey/c3a9ab53-4423-4354-885d-2776ba98505d#>] have-field-1-1-8 Answer-1-1-8-2." + "\r\n" +
"Element-1-1-Section-D-13-09-2018-T-16-59-42[<http://www.sfo.cognitum.eu/Survey/c3a9ab53-4423-4354-885d-2776ba98505d#>] have-field-1-1-9 Answer-1-1-9-3." + "\r\n" +
"Element-1-1-Section-D-13-09-2018-T-16-59-42[<http://www.sfo.cognitum.eu/Survey/c3a9ab53-4423-4354-885d-2776ba98505d#>] have-field-1-1-10 Answer-1-1-10-2." + "\r\n" +
"Element-1-Form-D-13-09-2018-T-16-59-42[<http://www.sfo.cognitum.eu/Survey/c3a9ab53-4423-4354-885d-2776ba98505d#>] have-status[sfo] Submitted[sfo]." + "\r\n" +
"Survey[<http://www.sfo.cognitum.eu/Survey/c3a9ab53-4423-4354-885d-2776ba98505d#>] is a submitted-patient.", true, true);

		client.KnowledgeInsert("" + "\r\n" +
"Element-2-Form-D-13-09-2018-T-17-0-30[<http://www.sfo.cognitum.eu/Survey/c3a9ab53-4423-4354-885d-2776ba98505d#>] is a element-2-form." + "\r\n" +
"Element-2-Form-D-13-09-2018-T-17-0-30[<http://www.sfo.cognitum.eu/Survey/c3a9ab53-4423-4354-885d-2776ba98505d#>] concern[sfo] Survey[<http://www.sfo.cognitum.eu/Survey/c3a9ab53-4423-4354-885d-2776ba98505d#>]." + "\r\n" +
"Survey[<http://www.sfo.cognitum.eu/Survey/c3a9ab53-4423-4354-885d-2776ba98505d#>] is-concerned-by[sfo] Element-2-Form-D-13-09-2018-T-17-0-30[<http://www.sfo.cognitum.eu/Survey/c3a9ab53-4423-4354-885d-2776ba98505d#>]." + "\r\n" +
"Element-2-1-Section-D-13-09-2018-T-17-0-30[<http://www.sfo.cognitum.eu/Survey/c3a9ab53-4423-4354-885d-2776ba98505d#>] is a element-2-1-section." + "\r\n" +
"Element-2-Form-D-13-09-2018-T-17-0-30[<http://www.sfo.cognitum.eu/Survey/c3a9ab53-4423-4354-885d-2776ba98505d#>] concern[sfo] Element-2-1-Section-D-13-09-2018-T-17-0-30[<http://www.sfo.cognitum.eu/Survey/c3a9ab53-4423-4354-885d-2776ba98505d#>]." + "\r\n" +
"Element-2-Form-D-13-09-2018-T-17-0-30[<http://www.sfo.cognitum.eu/Survey/c3a9ab53-4423-4354-885d-2776ba98505d#>] follows[sfo] Element-1-Form-D-13-09-2018-T-16-59-42[<http://www.sfo.cognitum.eu/Survey/c3a9ab53-4423-4354-885d-2776ba98505d#>].", true, true);

		client.KnowledgeDelete("Element-2-1-Section-D-13-09-2018-T-17-0-30[<http://www.sfo.cognitum.eu/Survey/c3a9ab53-4423-4354-885d-2776ba98505d#>] have-field-2-1-3 Answer-2-1-3-1." + "\r\n" +
"Element-2-1-Section-D-13-09-2018-T-17-0-30[<http://www.sfo.cognitum.eu/Survey/c3a9ab53-4423-4354-885d-2776ba98505d#>] have-field-2-1-3 Answer-2-1-3-2." + "\r\n" +
"Element-2-1-Section-D-13-09-2018-T-17-0-30[<http://www.sfo.cognitum.eu/Survey/c3a9ab53-4423-4354-885d-2776ba98505d#>] have-field-2-1-3 Answer-2-1-3-4." + "\r\n" +
"Element-2-1-Section-D-13-09-2018-T-17-0-30[<http://www.sfo.cognitum.eu/Survey/c3a9ab53-4423-4354-885d-2776ba98505d#>] have-field-2-1-3 Answer-2-1-3-5.", true);

		client.KnowledgeInsert("Element-2-1-Section-D-13-09-2018-T-17-0-30[<http://www.sfo.cognitum.eu/Survey/c3a9ab53-4423-4354-885d-2776ba98505d#>] have-field-2-1-3 Answer-2-1-3-3.", true, true);

		client.KnowledgeDelete("Element-2-1-Section-D-13-09-2018-T-17-0-30[<http://www.sfo.cognitum.eu/Survey/c3a9ab53-4423-4354-885d-2776ba98505d#>] have-field-2-1-3 Answer-2-1-3-1." + "\r\n" + "Element-2-1-Section-D-13-09-2018-T-17-0-30[<http://www.sfo.cognitum.eu/Survey/c3a9ab53-4423-4354-885d-2776ba98505d#>] have-field-2-1-3 Answer-2-1-3-2." + "\r\n" + "Element-2-1-Section-D-13-09-2018-T-17-0-30[<http://www.sfo.cognitum.eu/Survey/c3a9ab53-4423-4354-885d-2776ba98505d#>] have-field-2-1-3 Answer-2-1-3-3." + "\r\n" + "Element-2-1-Section-D-13-09-2018-T-17-0-30[<http://www.sfo.cognitum.eu/Survey/c3a9ab53-4423-4354-885d-2776ba98505d#>] have-field-2-1-3 Answer-2-1-3-4." + "\r\n" + "", true);
		HashMap<String, CogniPy.models.InstanceDescription> desc = client.DescribeInstances("Survey[<http://www.sfo.cognitum.eu/Survey/c3a9ab53-4423-4354-885d-2776ba98505d#>]");

		ArrayList<String> all = client.GetInstancesOf("thing", false); // OK
		ArrayList<String> inst1 = client.GetInstancesOf("field-1-1-1", false); // OK
																 //            Assert.IsNotEmpty(inst0);
		Assert.IsNotEmpty(all);
		//            CollectionAssert.AreEquivalent(inst0, inst1);
	}



//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [Test] public void CloneReasonerInternalCacheTest()
	public final void CloneReasonerInternalCacheTest()
	{
		CogniPySvr proto = InitializeClient("RODO.encnl");

		Stopwatch sw = new Stopwatch();
		sw.Start();
		proto.GetSubConceptsOf("starting-form[sfo]", false);
		sw.Stop();

		long timeForCacheInitializationProto = sw.ElapsedMilliseconds;

		sw.Restart();
		CogniPy.CogniPySvr clone1 = proto.CloneForAboxChangesOnly();
		sw.Stop();

		long clone1CreationTime = sw.ElapsedMilliseconds;

		sw.Restart();
		ArrayList<String> results = clone1.GetSubConceptsOf("starting-form[sfo]", false);
		sw.Stop();

		long timeForCacheInitializationClone1 = sw.ElapsedMilliseconds;


		sw.Restart();
		CogniPy.CogniPySvr clone2 = proto.CloneForAboxChangesOnly();
		sw.Stop();

		long clone2CreationTime = sw.ElapsedMilliseconds;

		sw.Restart();
		ArrayList<String> results2 = clone2.GetSubConceptsOf("starting-form[sfo]", false);
		sw.Stop();

		long timeForCacheInitializationClone2 = sw.ElapsedMilliseconds;

		Assert.Less(clone2CreationTime, clone1CreationTime);
		Assert.Less(timeForCacheInitializationClone2, timeForCacheInitializationClone1);
		Assert.Less(timeForCacheInitializationClone1, timeForCacheInitializationProto);
	}

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [Test] public void ConcurrentReasonerClonnigTest()
	public final void ConcurrentReasonerClonnigTest()
	{
		int numberOfThreads = 20;
		int clonesPerThread = 200;
		Thread[] tasks = new Thread[numberOfThreads];

		CogniPySvr proto = InitializeClient("WingsTelstraModelWithGraph.encnl");
		for (int i = 0; i < numberOfThreads; i++)
		{
			tasks[i] = new Thread()
			{
			void run()
			{
    
					for (int j = 0; j < clonesPerThread; j++)
					{
						CogniPy.CogniPySvr clone = proto.CloneForAboxChangesOnly();
						clone.KnowledgeInsert("Kaka is a maka.", false, true);
						ArrayList<String> inst = clone.GetInstancesOf("maka", false);
						CollectionAssert.Contains(inst, "Kaka");
					}
    
			}
			};
		}


		for (Thread t : tasks)
		{
			t.start();
		}

		for (Thread t : tasks)
		{
			t.join();
		}
	}

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [Test] public void ModalitiesConcurrencyTest()
	public final void ModalitiesConcurrencyTest()
	{
		int numberOfThreads = 12;
		CogniPySvr proto = InitializeClient("RODO.encnl");
		tangible.Func1Param<UUID, String> toAddString = (UUID guid) ->
		{
				 String uri = "http://www.sfo.cognitum.eu/Survey/" + guid.toString();
				 String str = tangible.StringHelper.join("\r\n", new ArrayList<String>(Arrays.asList(String.format("Element-1-Form-D-24-01-2019-T-15-42-57[<%1$s>] is a element-1-form.", uri), String.format("Survey[<%1$s>] is a subject[sfo].", uri), String.format("Element-1-Form-D-24-01-2019-T-15-42-57[<%1$s>] concern[sfo] Survey[<%2$s>].", uri, uri), String.format("Survey[<%1$s>] is-concerned-by[sfo] Element-1-Form-D-24-01-2019-T-15-42-57[<%2$s>].", uri, uri), String.format("Element-1-1-Section-D-24-01-2019-T-15-42-57[<%1$s>] is a element-1-1-section.", uri), String.format("Element-1-Form-D-24-01-2019-T-15-42-57[<%1$s>] concern[sfo] Element-1-1-Section-D-24-01-2019-T-15-42-57[<%2$s>].", uri, uri))));
				 return str;
		};


		java.util.concurrent.CountDownLatch countdownEvent = new java.util.concurrent.CountDownLatch(numberOfThreads);
		Thread[] tasks = new Thread[numberOfThreads];
		for (int i = 0; i < numberOfThreads; i++)
		{
			tasks[i] = new Thread()
			{
			void run()
			{
					countdownEvent.countDown();
					countdownEvent.Wait();
    
    
					CogniPy.CogniPySvr clone = proto.CloneForAboxChangesOnly();
					clone.KnowledgeInsert(toAddString.invoke(UUID.NewGuid()), false, true);
    
    
			}
			};
		}


		for (Thread t : tasks)
		{
			t.start();
		}

		for (Thread t : tasks)
		{
			t.join();
		}
	}

	//  [Ignore("this concurrency test takes too much time.")]
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [Test][TestCase(true, true)][TestCase(true, false)][TestCase(false, false)][TestCase(false, true)] public void MixedLoadConcurrencyTest(bool direct, bool clone)
	public final void MixedLoadConcurrencyTest(boolean direct, boolean clone)
	{
		final int n = 20;
		final int m = 200;
		final int k = 30;

		ArrayList<CogniPySvr> reaonsers = new ArrayList<CogniPySvr>();

		if (clone)
		{
			CogniPySvr proto = InitializeClient("Ont.encnl");
			for (int i = 0; i < n; ++i)
			{
				reaonsers.add(proto.CloneForAboxChangesOnly());
			}
		}
		else
		{
			for (int i = 0; i < n; ++i)
			{
				reaonsers.add(InitializeClient("Ont.encnl"));
			}
		}


		java.util.concurrent.CountDownLatch countdownEvent = new java.util.concurrent.CountDownLatch(n);
		int taskCnt = 0;
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
		var tasks = reaonsers.Select(reasoner -> new Thread()
		{
		void run()
		{
				Random rnd = new Random();
				countdownEvent.countDown();
				countdownEvent.Wait();
				tangible.RefObject<Integer> tempRef_taskCnt = new tangible.RefObject<Integer>(taskCnt);
				int taskid = Interlocked.Increment(tempRef_taskCnt);
			taskCnt = tempRef_taskCnt.argValue;
				String instance = "";
				for (int j = 0; j < m; ++j)
				{
					if (j % 20 == 0)
					{
						instance = "Survey[<http://www.sfo.cognitum.eu/Survey/7cdd5fc7-8d39-4756-912c-9f400d9ea44d#>]";
					}
    
					reasoner.DescribeInstances(direct ? "a subject[sfo] that is  " + instance : instance);
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
					var suc = reasoner.GetSuperConceptsOf(instance, direct);
					String concept = null;
					if (suc.Count > 0)
					{
						concept = suc[rnd.nextInt(suc.Count)];
					}
					if (concept != null)
					{
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
						var ins = reasoner.GetInstancesOf(concept, direct);
						if (ins.Count > 0)
						{
							instance = ins[0];
						}
						reasoner.GetSubConceptsOf(concept, direct);
					}
					reasoner.KnowledgeInsert("Duda-" + String.valueOf(taskid) + "-" + String.valueOf(j) + " to-nasz Pan.", false, true);
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
					var qq = reasoner.GetInstancesOf("a thing that to-nasz Pan", false);
					if (j < k)
					{
						assert qq.Count == j + 1;
					}
					else
					{
						assert qq.Count == k + 1;
						reasoner.KnowledgeDelete(qq[0] + " to-nasz Pan.", true);
					}
				}
    
		}
		}).ToArray();

//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
		for (var t : tasks)
		{
			t.Start();
		}

//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
		for (var t : tasks)
		{
			t.Join();
		}
	}

//C# TO JAVA CONVERTER TODO TASK: Local functions are not converted by C# to Java Converter:
//	private CogniPySvr InitializeClient(string ontologyFile, bool materialize = true)
//		{
//			var ontologyDirectory = AppDomain.CurrentDomain.SetupInformation.ApplicationBase;
//			var ontologyPath = Path.Combine(ontologyDirectory, "TestFiles", ontologyFile);
//
//			var client = new CogniPySvr();
//			client.LoadCnl(ontologyPath, true, materialize);
//
//			return client;
//		}


//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [Test] public void LongAnnotationLoadingTimeBug()
//C# TO JAVA CONVERTER TODO TASK: Local functions are not converted by C# to Java Converter:
//	public void LongAnnotationLoadingTimeBug()
//		{
//			var reasoner = InitializeClient("RODO.encnl", false);
//			var result = reasoner.GetSubConceptsOf("a starting-form[sfo]", true);
//		}
}
}
