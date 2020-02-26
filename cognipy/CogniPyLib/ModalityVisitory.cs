using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace CogniPy.CNL.DL
{
    public class ModalityVisitor : CogniPy.CNL.DL.GenericVisitor
    {
        private CogniPy.CNL.CNLTools tools = null;

        Func<string, string> ns2pfx;

        public ModalityVisitor(CogniPy.CNL.CNLTools _tools,Func<string,string> ns2pfx=null)
        {
            tools = _tools; 
            this.ns2pfx = ns2pfx;
        }

        private string relation;
        public string Relation
        {
            get
            {
                return relation;
            }
        }

        private string relatedConcept;
        public string RelatedConcept
        {
            get
            {
                return relatedConcept;
            }
        }

        private CNL.DL.Statement.Modality modality;
        public CNL.DL.Statement.Modality Modality
        {
            get
            {
                return modality;
            }
        }

        private string concept;
        public string Concept
        {
            get
            {
                return concept;
            }
        }

        public override object Visit(CogniPy.CNL.DL.Subsumption e)
        {
            modality = e.modality;
            //string a, b;
            //if (e.C is Ontorion.CNL.DL.Atomic && (e.C as Ontorion.CNL.DL.Atomic).id.Equals(leftSide.id))
            if (e.C is CogniPy.CNL.DL.Atomic)
            {
                concept = (e.C as CogniPy.CNL.DL.Atomic).id;
                if (e.D is CogniPy.CNL.DL.Atomic)
                {
                    CogniPy.CNL.DL.Atomic atom = e.D as CogniPy.CNL.DL.Atomic;
                    relation = "be";
                    relatedConcept = atom.id;
                }
                else if (e.D is CogniPy.CNL.DL.Restriction)
                {
                    string restriction = ((e.D as CogniPy.CNL.DL.Restriction).R as CogniPy.CNL.DL.Atomic).id;
                    CogniPy.CNL.DL.IAccept node = null;
                    if (e.D is CogniPy.CNL.DL.OnlyRestriction)
                    {
                        node = (e.D as CogniPy.CNL.DL.OnlyRestriction).C;
                    }
                    else if (e.D is CogniPy.CNL.DL.SomeRestriction)
                    {
                        node = (e.D as CogniPy.CNL.DL.SomeRestriction).C;
                    }
                    else if (e.D is CogniPy.CNL.DL.OnlyValueRestriction)
                    {
                        node = (e.D as CogniPy.CNL.DL.OnlyValueRestriction).B;
                    }
                    else if (e.D is CogniPy.CNL.DL.SomeValueRestriction)
                    {
                        node = (e.D as CogniPy.CNL.DL.SomeValueRestriction).B;
                    }
                    else if (e.D is CogniPy.CNL.DL.NumberRestriction)
                    {
                        //string str = tools.GetENDLFromAst(e.D);
                        node = (e.D as CogniPy.CNL.DL.NumberRestriction).C;
                    }
                    else if (e.D is CogniPy.CNL.DL.NumberValueRestriction)
                    {
                        node = (e.D as CogniPy.CNL.DL.NumberValueRestriction).B;
                    }

                    relation = restriction;
                    string restrVal = tools.GetENDLFromAst(node,false,ns2pfx);
                    relatedConcept = restrVal.Replace("is ", ""); 
                }
                else
                {
                    relation = null;
                    relatedConcept = tools.GetENDLFromAst(e.D,false,ns2pfx);
                }

                //var instSet = new List<Ontorion.CNL.DL.Instance>();
                //instSet.Add(new Ontorion.CNL.DL.NamedInstance(null) { name = "_Tom" });
                //a = tools.GetENDLFromAst(new Ontorion.CNL.DL.Subsumption(null) { C = new Ontorion.CNL.DL.InstanceSet(null) { Instances = instSet }, D = e.D });
                //a = tools.GetENDLFromAst(e.D);
                //string[] spl = a.Split(' ');
                //modalities[e.modality].Add(new KeyValuePair<string, string>(spl[1], string.Join(" ", spl, 2, spl.Length - 2)));
            }
            return base.Visit(e);
        }
    }
}
