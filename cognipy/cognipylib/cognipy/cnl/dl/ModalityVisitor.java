package cognipy.cnl.dl;

import cognipy.*;
import cognipy.cnl.*;

public class ModalityVisitor extends cognipy.cnl.dl.GenericVisitor
{
	private cognipy.cnl.CNLTools tools = null;

	private tangible.Func1Param<String, String> ns2pfx;


	public ModalityVisitor(cognipy.cnl.CNLTools _tools)
	{
		this(_tools, null);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public ModalityVisitor(CogniPy.CNL.CNLTools _tools, Func<string, string> ns2pfx = null)
	public ModalityVisitor(cognipy.cnl.CNLTools _tools, tangible.Func1Param<String, String> ns2pfx)
	{
		tools = _tools;
		this.ns2pfx = (String arg) -> ns2pfx.invoke(arg);
	}

	private String relation;
	public final String getRelation()
	{
		return relation;
	}

	private String relatedConcept;
	public final String getRelatedConcept()
	{
		return relatedConcept;
	}

	private CNL.DL.Statement.Modality modality = CNL.DL.Statement.Modality.values()[0];
	public final CNL.DL.Statement.Modality getModality()
	{
		return modality;
	}

	private String concept;
	public final String getConcept()
	{
		return concept;
	}

	@Override
	public Object Visit(cognipy.cnl.dl.Subsumption e)
	{
		modality = e.modality;
		//string a, b;
		//if (e.C is Ontorion.CNL.DL.Atomic && (e.C as Ontorion.CNL.DL.Atomic).id.Equals(leftSide.id))
		if (e.C instanceof cognipy.cnl.dl.Atomic)
		{
			concept = (e.C instanceof cognipy.cnl.dl.Atomic ? (cognipy.cnl.dl.Atomic)e.C : null).id;
			if (e.D instanceof cognipy.cnl.dl.Atomic)
			{
				cognipy.cnl.dl.Atomic atom = e.D instanceof cognipy.cnl.dl.Atomic ? (cognipy.cnl.dl.Atomic)e.D : null;
				relation = "be";
				relatedConcept = atom.id;
			}
			else if (e.D instanceof cognipy.cnl.dl.Restriction)
			{
				String restriction = ((e.D instanceof cognipy.cnl.dl.Restriction ? (cognipy.cnl.dl.Restriction)e.D : null).R instanceof cognipy.cnl.dl.Atomic ? (cognipy.cnl.dl.Atomic)(e.D instanceof cognipy.cnl.dl.Restriction ? (cognipy.cnl.dl.Restriction)e.D : null).R : null).id;
				cognipy.cnl.dl.IAccept node = null;
				if (e.D instanceof cognipy.cnl.dl.OnlyRestriction)
				{
					node = (e.D instanceof cognipy.cnl.dl.OnlyRestriction ? (cognipy.cnl.dl.OnlyRestriction)e.D : null).C;
				}
				else if (e.D instanceof cognipy.cnl.dl.SomeRestriction)
				{
					node = (e.D instanceof cognipy.cnl.dl.SomeRestriction ? (cognipy.cnl.dl.SomeRestriction)e.D : null).C;
				}
				else if (e.D instanceof cognipy.cnl.dl.OnlyValueRestriction)
				{
					node = (e.D instanceof cognipy.cnl.dl.OnlyValueRestriction ? (cognipy.cnl.dl.OnlyValueRestriction)e.D : null).B;
				}
				else if (e.D instanceof cognipy.cnl.dl.SomeValueRestriction)
				{
					node = (e.D instanceof cognipy.cnl.dl.SomeValueRestriction ? (cognipy.cnl.dl.SomeValueRestriction)e.D : null).B;
				}
				else if (e.D instanceof cognipy.cnl.dl.NumberRestriction)
				{
					//string str = tools.GetENDLFromAst(e.D);
					node = (e.D instanceof cognipy.cnl.dl.NumberRestriction ? (cognipy.cnl.dl.NumberRestriction)e.D : null).C;
				}
				else if (e.D instanceof cognipy.cnl.dl.NumberValueRestriction)
				{
					node = (e.D instanceof cognipy.cnl.dl.NumberValueRestriction ? (cognipy.cnl.dl.NumberValueRestriction)e.D : null).B;
				}

				relation = restriction;
				String restrVal = tools.GetENDLFromAst(node, false, ns2pfx);
				relatedConcept = restrVal.replace("is ", "");
			}
			else
			{
				relation = null;
				relatedConcept = tools.GetENDLFromAst(e.D, false, ns2pfx);
			}

			//var instSet = new List<Ontorion.CNL.DL.Instance>();
			//instSet.Add(new Ontorion.CNL.DL.NamedInstance(null) { name = "_Tom" });
			//a = tools.GetENDLFromAst(new Ontorion.CNL.DL.Subsumption(null) { C = new Ontorion.CNL.DL.InstanceSet(null) { Instances = instSet }, D = e.D });
			//a = tools.GetENDLFromAst(e.D);
			//string[] spl = a.Split(' ');
			//modalities[e.modality].Add(new KeyValuePair<string, string>(spl[1], string.Join(" ", spl, 2, spl.Length - 2)));
		}
		return super.Visit(e);
	}
}