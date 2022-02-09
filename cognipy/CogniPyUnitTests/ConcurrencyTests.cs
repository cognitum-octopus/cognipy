using CogniPy;
using NUnit.Framework;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Linq;
using System.Threading;

namespace CogniPyUnitTests
{
    class ConcurrencyTests
    {

        [Test]
        public void NewJenaBug()
        {
            var client = InitializeClient("IntegrationTestsTBox.encnl");

            var tt = client.DescribeInstances("a thing");
            Assert.IsNotEmpty(tt);
        }

        [Test]
        public void NewJenaBug2()
        {
            var client = InitializeClient("TestTBox.encnl");

            var inst0 = client.GetInstancesOf("field-1-1-1", false); // No results
            var all = client.GetInstancesOf("thing", false); // OK
            var inst1 = client.GetInstancesOf("field-1-1-1", false); // OK
            Assert.IsNotEmpty(inst0);
            Assert.IsNotEmpty(all);
            CollectionAssert.AreEquivalent(inst0, inst1);
        }

        [Test]
        public void NewJenaBug3()
        {
            var client = InitializeClient("TestTBox.encnl");

            //            var inst0 = client.GetInstancesOf("field-1-1-1", false); // No results

            client.KnowledgeInsert(@"Element-1-Form-D-13-09-2018-T-16-59-42[<http://www.sfo.cognitum.eu/Survey/c3a9ab53-4423-4354-885d-2776ba98505d#>] is a element-1-form.
Survey[<http://www.sfo.cognitum.eu/Survey/c3a9ab53-4423-4354-885d-2776ba98505d#>] is a subject[sfo].
Element-1-Form-D-13-09-2018-T-16-59-42[<http://www.sfo.cognitum.eu/Survey/c3a9ab53-4423-4354-885d-2776ba98505d#>] concern[sfo] Survey[<http://www.sfo.cognitum.eu/Survey/c3a9ab53-4423-4354-885d-2776ba98505d#>].
Survey[<http://www.sfo.cognitum.eu/Survey/c3a9ab53-4423-4354-885d-2776ba98505d#>] is-concerned-by[sfo] Element-1-Form-D-13-09-2018-T-16-59-42[<http://www.sfo.cognitum.eu/Survey/c3a9ab53-4423-4354-885d-2776ba98505d#>].
Element-1-1-Section-D-13-09-2018-T-16-59-42[<http://www.sfo.cognitum.eu/Survey/c3a9ab53-4423-4354-885d-2776ba98505d#>] is a element-1-1-section.
Element-1-Form-D-13-09-2018-T-16-59-42[<http://www.sfo.cognitum.eu/Survey/c3a9ab53-4423-4354-885d-2776ba98505d#>] concern[sfo] Element-1-1-Section-D-13-09-2018-T-16-59-42[<http://www.sfo.cognitum.eu/Survey/c3a9ab53-4423-4354-885d-2776ba98505d#>].", true, true);

            client.KnowledgeInsert(@"Element-1-1-Section-D-13-09-2018-T-16-59-42[<http://www.sfo.cognitum.eu/Survey/c3a9ab53-4423-4354-885d-2776ba98505d#>] have-field-1-1-1 equal-to 'Krzysztof'.
Element-1-1-Section-D-13-09-2018-T-16-59-42[<http://www.sfo.cognitum.eu/Survey/c3a9ab53-4423-4354-885d-2776ba98505d#>] have-field-1-1-2 equal-to 'sdsdsd'.
Element-1-1-Section-D-13-09-2018-T-16-59-42[<http://www.sfo.cognitum.eu/Survey/c3a9ab53-4423-4354-885d-2776ba98505d#>] have-field-1-1-3 equal-to 'k.cieslinski@cognitum.eu'.
Element-1-1-Section-D-13-09-2018-T-16-59-42[<http://www.sfo.cognitum.eu/Survey/c3a9ab53-4423-4354-885d-2776ba98505d#>] have-field-1-1-4 equal-to '384753443544'.
Element-1-1-Section-D-13-09-2018-T-16-59-42[<http://www.sfo.cognitum.eu/Survey/c3a9ab53-4423-4354-885d-2776ba98505d#>] have-field-1-1-5 equal-to 'Cognitum'.
Element-1-1-Section-D-13-09-2018-T-16-59-42[<http://www.sfo.cognitum.eu/Survey/c3a9ab53-4423-4354-885d-2776ba98505d#>] have-field-1-1-6 equal-to 'sdsds'.
Element-1-1-Section-D-13-09-2018-T-16-59-42[<http://www.sfo.cognitum.eu/Survey/c3a9ab53-4423-4354-885d-2776ba98505d#>] have-field-1-1-7 Answer-1-1-7-4.
Element-1-1-Section-D-13-09-2018-T-16-59-42[<http://www.sfo.cognitum.eu/Survey/c3a9ab53-4423-4354-885d-2776ba98505d#>] have-field-1-1-8 Answer-1-1-8-2.
Element-1-1-Section-D-13-09-2018-T-16-59-42[<http://www.sfo.cognitum.eu/Survey/c3a9ab53-4423-4354-885d-2776ba98505d#>] have-field-1-1-9 Answer-1-1-9-3.
Element-1-1-Section-D-13-09-2018-T-16-59-42[<http://www.sfo.cognitum.eu/Survey/c3a9ab53-4423-4354-885d-2776ba98505d#>] have-field-1-1-10 Answer-1-1-10-2.
Element-1-Form-D-13-09-2018-T-16-59-42[<http://www.sfo.cognitum.eu/Survey/c3a9ab53-4423-4354-885d-2776ba98505d#>] have-status[sfo] Submitted[sfo].
Survey[<http://www.sfo.cognitum.eu/Survey/c3a9ab53-4423-4354-885d-2776ba98505d#>] is a submitted-patient.", true, true);

            client.KnowledgeInsert(@"
Element-2-Form-D-13-09-2018-T-17-0-30[<http://www.sfo.cognitum.eu/Survey/c3a9ab53-4423-4354-885d-2776ba98505d#>] is a element-2-form.
Element-2-Form-D-13-09-2018-T-17-0-30[<http://www.sfo.cognitum.eu/Survey/c3a9ab53-4423-4354-885d-2776ba98505d#>] concern[sfo] Survey[<http://www.sfo.cognitum.eu/Survey/c3a9ab53-4423-4354-885d-2776ba98505d#>].
Survey[<http://www.sfo.cognitum.eu/Survey/c3a9ab53-4423-4354-885d-2776ba98505d#>] is-concerned-by[sfo] Element-2-Form-D-13-09-2018-T-17-0-30[<http://www.sfo.cognitum.eu/Survey/c3a9ab53-4423-4354-885d-2776ba98505d#>].
Element-2-1-Section-D-13-09-2018-T-17-0-30[<http://www.sfo.cognitum.eu/Survey/c3a9ab53-4423-4354-885d-2776ba98505d#>] is a element-2-1-section.
Element-2-Form-D-13-09-2018-T-17-0-30[<http://www.sfo.cognitum.eu/Survey/c3a9ab53-4423-4354-885d-2776ba98505d#>] concern[sfo] Element-2-1-Section-D-13-09-2018-T-17-0-30[<http://www.sfo.cognitum.eu/Survey/c3a9ab53-4423-4354-885d-2776ba98505d#>].
Element-2-Form-D-13-09-2018-T-17-0-30[<http://www.sfo.cognitum.eu/Survey/c3a9ab53-4423-4354-885d-2776ba98505d#>] follows[sfo] Element-1-Form-D-13-09-2018-T-16-59-42[<http://www.sfo.cognitum.eu/Survey/c3a9ab53-4423-4354-885d-2776ba98505d#>].", true, true);

            client.KnowledgeDelete(@"Element-2-1-Section-D-13-09-2018-T-17-0-30[<http://www.sfo.cognitum.eu/Survey/c3a9ab53-4423-4354-885d-2776ba98505d#>] have-field-2-1-3 Answer-2-1-3-1.
Element-2-1-Section-D-13-09-2018-T-17-0-30[<http://www.sfo.cognitum.eu/Survey/c3a9ab53-4423-4354-885d-2776ba98505d#>] have-field-2-1-3 Answer-2-1-3-2.
Element-2-1-Section-D-13-09-2018-T-17-0-30[<http://www.sfo.cognitum.eu/Survey/c3a9ab53-4423-4354-885d-2776ba98505d#>] have-field-2-1-3 Answer-2-1-3-4.
Element-2-1-Section-D-13-09-2018-T-17-0-30[<http://www.sfo.cognitum.eu/Survey/c3a9ab53-4423-4354-885d-2776ba98505d#>] have-field-2-1-3 Answer-2-1-3-5.", true);

            client.KnowledgeInsert(@"Element-2-1-Section-D-13-09-2018-T-17-0-30[<http://www.sfo.cognitum.eu/Survey/c3a9ab53-4423-4354-885d-2776ba98505d#>] have-field-2-1-3 Answer-2-1-3-3.", true, true);

            client.KnowledgeDelete(@"Element-2-1-Section-D-13-09-2018-T-17-0-30[<http://www.sfo.cognitum.eu/Survey/c3a9ab53-4423-4354-885d-2776ba98505d#>] have-field-2-1-3 Answer-2-1-3-1.
Element-2-1-Section-D-13-09-2018-T-17-0-30[<http://www.sfo.cognitum.eu/Survey/c3a9ab53-4423-4354-885d-2776ba98505d#>] have-field-2-1-3 Answer-2-1-3-2.
Element-2-1-Section-D-13-09-2018-T-17-0-30[<http://www.sfo.cognitum.eu/Survey/c3a9ab53-4423-4354-885d-2776ba98505d#>] have-field-2-1-3 Answer-2-1-3-3.
Element-2-1-Section-D-13-09-2018-T-17-0-30[<http://www.sfo.cognitum.eu/Survey/c3a9ab53-4423-4354-885d-2776ba98505d#>] have-field-2-1-3 Answer-2-1-3-4.
"
, true);
            var desc = client.DescribeInstances("Survey[<http://www.sfo.cognitum.eu/Survey/c3a9ab53-4423-4354-885d-2776ba98505d#>]");

            var all = client.GetInstancesOf("thing", false); // OK
            var inst1 = client.GetInstancesOf("field-1-1-1", false); // OK
                                                                     //            Assert.IsNotEmpty(inst0);
            Assert.IsNotEmpty(all);
            //            CollectionAssert.AreEquivalent(inst0, inst1);
        }



        [Test]
        public void CloneReasonerInternalCacheTest()
        {
            var proto = InitializeClient("TestOnto2.encnl");

            Stopwatch sw = new Stopwatch();
            sw.Start();
            proto.GetSubConceptsOf("starting-form[sfo]", false);
            sw.Stop();

            var timeForCacheInitializationProto = sw.ElapsedMilliseconds;

            sw.Restart();
            var clone1 = proto.CloneForAboxChangesOnly();
            sw.Stop();

            var clone1CreationTime = sw.ElapsedMilliseconds;

            sw.Restart();
            var results = clone1.GetSubConceptsOf("starting-form[sfo]", false);
            sw.Stop();

            var timeForCacheInitializationClone1 = sw.ElapsedMilliseconds;


            sw.Restart();
            var clone2 = proto.CloneForAboxChangesOnly();
            sw.Stop();

            var clone2CreationTime = sw.ElapsedMilliseconds;

            sw.Restart();
            var results2 = clone2.GetSubConceptsOf("starting-form[sfo]", false);
            sw.Stop();

            var timeForCacheInitializationClone2 = sw.ElapsedMilliseconds;

            Assert.Less(clone2CreationTime, clone1CreationTime);
            Assert.Less(timeForCacheInitializationClone2, timeForCacheInitializationClone1);
            Assert.Less(timeForCacheInitializationClone1, timeForCacheInitializationProto);
        }

        [Test]
        public void ConcurrentReasonerClonnigTest()
        {
            var numberOfThreads = 20;
            var clonesPerThread = 200;
            var tasks = new Thread[numberOfThreads];

            var proto = InitializeClient("TestOnto.encnl");
            for (int i = 0; i < numberOfThreads; i++)
                tasks[i] = new Thread(() =>
                {

                    for (int j = 0; j < clonesPerThread; j++)
                    {
                        var clone = proto.CloneForAboxChangesOnly();
                        clone.KnowledgeInsert("Kaka is a maka.", false, true);
                        var inst = clone.GetInstancesOf("maka", false);
                        CollectionAssert.Contains(inst, "Kaka");
                    }

                });


            foreach (var t in tasks)
                t.Start();

            foreach (var t in tasks)
                t.Join();
        }

        [Test]
        public void ModalitiesConcurrencyTest()
        {
            var numberOfThreads = 12;
            var proto = InitializeClient("TestOnto2.encnl");
            Func<Guid, string> toAddString = (guid) =>
             {
                 var uri = "http://www.sfo.cognitum.eu/Survey/" + guid.ToString();
                 var str = String.Join("\r\n", new List<string>() {
            $"Element-1-Form-D-24-01-2019-T-15-42-57[<{uri}>] is a element-1-form.",
            $"Survey[<{uri}>] is a subject[sfo].",
            $"Element-1-Form-D-24-01-2019-T-15-42-57[<{uri}>] concern[sfo] Survey[<{uri}>].",
            $"Survey[<{uri}>] is-concerned-by[sfo] Element-1-Form-D-24-01-2019-T-15-42-57[<{uri}>].",
            $"Element-1-1-Section-D-24-01-2019-T-15-42-57[<{uri}>] is a element-1-1-section.",
            $"Element-1-Form-D-24-01-2019-T-15-42-57[<{uri}>] concern[sfo] Element-1-1-Section-D-24-01-2019-T-15-42-57[<{uri}>]."
             });
                 return str;
             };


            var countdownEvent = new CountdownEvent(numberOfThreads);
            var tasks = new Thread[numberOfThreads];
            for (int i = 0; i < numberOfThreads; i++)
                tasks[i] = new Thread(() =>
                {
                    countdownEvent.Signal();
                    countdownEvent.Wait();


                    var clone = proto.CloneForAboxChangesOnly();
                    clone.KnowledgeInsert(toAddString(Guid.NewGuid()), false, true);


                });


            foreach (var t in tasks)
                t.Start();

            foreach (var t in tasks)
                t.Join();
        }

        [Test]
        //  [Ignore("this concurrency test takes too much time.")]
        [TestCase(true, true)]
        [TestCase(true, false)]
        [TestCase(false, false)]
        [TestCase(false, true)]
        public void MixedLoadConcurrencyTest(bool direct, bool clone)
        {
            const int n = 20;
            const int m = 200;
            const int k = 30;

            var reaonsers = new List<CogniPySvr>();

            if (clone)
            {
                var proto = InitializeClient("Ont.encnl");
                for (var i = 0; i < n; ++i)
                {
                    reaonsers.Add(proto.CloneForAboxChangesOnly());
                }
            }
            else
                for (var i = 0; i < n; ++i)
                {
                    reaonsers.Add(InitializeClient("Ont.encnl"));
                }


            var countdownEvent = new CountdownEvent(n);
            int taskCnt = 0;
            var tasks = reaonsers.Select(reasoner => new Thread(() =>
            {
                Random rnd = new Random();
                countdownEvent.Signal();
                countdownEvent.Wait();
                int taskid = Interlocked.Increment(ref taskCnt);
                string instance = "";
                for (var j = 0; j < m; ++j)
                {
                    if (j % 20 == 0)
                        instance = "Survey[<http://www.sfo.cognitum.eu/Survey/7cdd5fc7-8d39-4756-912c-9f400d9ea44d#>]";

                    reasoner.DescribeInstances(direct ? "a subject[sfo] that is  " + instance : instance);
                    var suc = reasoner.GetSuperConceptsOf(instance, direct);
                    string concept = null;
                    if (suc.Count > 0)
                    {
                        concept = suc[rnd.Next(suc.Count)];
                    }
                    if (concept != null)
                    {
                        var ins = reasoner.GetInstancesOf(concept, direct);
                        if (ins.Count > 0)
                            instance = ins[0];
                        reasoner.GetSubConceptsOf(concept, direct);
                    }
                    reasoner.KnowledgeInsert("Lion-" + taskid.ToString() + "-" + j.ToString() + " is-our King.", false, true);
                    var qq = reasoner.GetInstancesOf("a thing that is-our King", false);
                    if (j < k)
                        Assert.AreEqual(qq.Count, j + 1);
                    else
                    {
                        Assert.AreEqual(qq.Count, k + 1);
                        reasoner.KnowledgeDelete(qq[0] + " is-our King.", true);
                    }
                }

            })).ToArray();

            foreach (var t in tasks)
                t.Start();

            foreach (var t in tasks)
                t.Join();
        }

        private CogniPySvr InitializeClient(string ontologyFile, bool materialize = true)
        {
            var ontologyDirectory = AppDomain.CurrentDomain.SetupInformation.ApplicationBase;
            var ontologyPath = Path.Combine(ontologyDirectory, "TestFiles", ontologyFile);

            var client = new CogniPySvr();
            client.LoadCnl(ontologyPath, true, materialize);

            return client;
        }


        [Test]
        public void LongAnnotationLoadingTimeBug()
        {
            var reasoner = InitializeClient("TestOnto2.encnl", false);
            var result = reasoner.GetSubConceptsOf("a starting-form[sfo]", true);
        }
    }
}
