package cognipy.ars;

import cognipy.cnl.dl.*;
import cognipy.cnl.en.*;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.*;
import org.semanticweb.owlapi.vocab.*;
import cognipy.*;
import java.util.*;

public class Transform implements cognipy.cnl.dl.IVisitor
{
	private DLToOWLNameConv owlNC = new DLToOWLNameConv();

	public final HashMap<String, String> getInvUriMappings()
	{
		return owlNC.InvUriMappings;
	}
	public final void setInvUriMappings(HashMap<String, String> value)
	{
		owlNC.InvUriMappings = value;
	}
	private boolean forReasoning = false;

	public final void setOWLDataFactory(boolean forReasoning, String defaultNS, OWLDataFactory factory, PrefixOWLOntologyFormat namespaceManager, cognipy.cnl.en.endict lex)
	{
		this.owlNC.setOWLFormat(defaultNS, namespaceManager, lex);
		this.factory = factory;
		this.forReasoning = forReasoning;
	}

	public static Set GetJavaAxiomSet(java.lang.Iterable<AxiomOrComment> axioms)
	{
		LinkedHashSet ret = new LinkedHashSet();
		for (AxiomOrComment axiom : axioms)
		{
			if (axiom.axiom != null)
			{
				ret.add(axiom.axiom);
			}
		}
		return ret;
	}

	public static Set GetJavaAxiomSet(java.lang.Iterable<OWLAxiom> axioms)
	{
		LinkedHashSet ret = new LinkedHashSet();
		for (OWLAxiom axiom : axioms)
		{
			ret.add(axiom);
		}
		return ret;
	}

	private OWLDataFactory factory;
	private OWLReasoner resolvingReasoner = null;



	private ArrayList<OWLAxiom> additionalAxioms = null;
	private ArrayList<OWLAxiom> additionalHotfixDeclarations = null;

	public final void setReasoner(OWLReasoner reasoner)
	{
		this.resolvingReasoner = reasoner;
	}


	public static class AxiomOrComment
	{
		public OWLAxiom axiom;
		public String comment;
	}

	public static class Axioms
	{
		public ArrayList<AxiomOrComment> axioms = new ArrayList<AxiomOrComment>();
		public HashSet<OWLAxiom> additions = new HashSet<OWLAxiom>();
		public HashSet<OWLAxiom> hotfixes = new HashSet<OWLAxiom>();
	}

	private boolean hasAnnotationsForStatement = false;
	private HashMap<String, ArrayList<DLAnnotationAxiom>> annotationsBySubject = null;


	public final Axioms Convert(cognipy.cnl.dl.Paragraph p)
	{
		return Convert(p, null);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public Axioms Convert(CogniPy.CNL.DL.Paragraph p, CogniPy.CNL.DL.Paragraph paraFromAnnotStatements = null)
	public final Axioms Convert(cognipy.cnl.dl.Paragraph p, cognipy.cnl.dl.Paragraph paraFromAnnotStatements)
	{
		if (paraFromAnnotStatements != null && !paraFromAnnotStatements.Statements.isEmpty())
		{
			annotationsBySubject = new HashMap<String, ArrayList<DLAnnotationAxiom>>();
			int nannot = 0;
			for (Statement stmtAnn : paraFromAnnotStatements.Statements)
			{
				if (stmtAnn instanceof DLAnnotationAxiom)
				{
					nannot++;
					DLAnnotationAxiom dlannotAx = stmtAnn instanceof DLAnnotationAxiom ? (DLAnnotationAxiom)stmtAnn : null;
					if (!annotationsBySubject.containsKey(dlannotAx.getSubject()))
					{
						annotationsBySubject.put(dlannotAx.getSubject(), new ArrayList<DLAnnotationAxiom>());
					}
					annotationsBySubject.get(dlannotAx.getSubject()).add(dlannotAx);
				}
			}
			if (nannot > 0)
			{
				hasAnnotationsForStatement = true;
			}
		}

		additionalAxioms = new ArrayList<OWLAxiom>();
		additionalHotfixDeclarations = new ArrayList<OWLAxiom>();
		Object tempVar = p.accept(this);
		ArrayList<AxiomOrComment> axioms = tempVar instanceof ArrayList<AxiomOrComment> ? (ArrayList<AxiomOrComment>)tempVar : null;

		Axioms tempVar2 = new Axioms();
		tempVar2.axioms = axioms;
		tempVar2.additions = new HashSet<OWLAxiom>(additionalAxioms);
		tempVar2.hotfixes = new HashSet<OWLAxiom>(additionalHotfixDeclarations);
		return tempVar2;
	}

	public final Map.Entry<OWLClassExpression, HashSet<OWLAxiom>> Convert(cognipy.cnl.dl.Node e)
	{
		additionalAxioms = new ArrayList<OWLAxiom>();
		additionalHotfixDeclarations = new ArrayList<OWLAxiom>();
		Object tempVar = e.accept(this);
		OWLClassExpression cls = tempVar instanceof OWLClassExpression ? (OWLClassExpression)tempVar : null;
		return new Map.Entry<OWLClassExpression, HashSet<OWLAxiom>>(cls, new HashSet<OWLAxiom>(additionalAxioms));
	}

	public final OWLNamedIndividual GetNamedIndividual(String I)
	{
		return factory.getOWLNamedIndividual(owlNC.getIRIFromId(I, EntityKind.Instance));
	}

	public final IRI getIRIFromDL(String I, EntityKind kind)
	{
		return owlNC.getIRIFromId(I, kind);
	}

	public final OWLObjectPropertyExpression GetObjectProperty(cognipy.cnl.dl.Node r)
	{
		try (context.set(VisitingContext.ObjectRole))
		{
			Object tempVar = r.accept(this);
			return tempVar instanceof OWLObjectPropertyExpression ? (OWLObjectPropertyExpression)tempVar : null;
		}
	}
	public final OWLDataProperty GetDataProperty(cognipy.cnl.dl.Node r)
	{
		try (context.set(VisitingContext.DataRole))
		{
			Object tempVar = r.accept(this);
			return tempVar instanceof OWLDataProperty ? (OWLDataProperty)tempVar : null;
		}
	}
	public final Object Visit(cognipy.cnl.dl.Paragraph e)
	{
		ArrayList<AxiomOrComment> axioms = new ArrayList<AxiomOrComment>();
		for (Statement stmt : e.Statements)
		{
			//                if (!isDisabledStatement(stmt))
			AxiomOrComment aoc = new AxiomOrComment();
			Object n = stmt.accept(this);
			if (n == null)
			{
				continue;
			}
			if (n instanceof OWLAxiom)
			{
				aoc.axiom = n instanceof OWLAxiom ? (OWLAxiom)n : null;
			}
			else
			{
				aoc.comment = n.toString();
			}
			axioms.add(aoc);
		}
		return axioms;
	}

	private Set getOWLAnnotationForStatement(cognipy.cnl.dl.Statement e)
	{
		Set annotationsForStatement = new LinkedHashSet();
		if (hasAnnotationsForStatement)
		{
			cognipy.cnl.dl.Serializer ser = new cognipy.cnl.dl.Serializer(false);
			CNL.DL.Paragraph tempVar = new CNL.DL.Paragraph(null);
			tempVar.Statements = new ArrayList<CNL.DL.Statement>(Arrays.asList(e));
			String stmtSer = ser.Serialize(tempVar).replace("\r\n", "");
			if (annotationsBySubject.containsKey(stmtSer))
			{
				for (DLAnnotationAxiom ann : annotationsBySubject.get(stmtSer))
				{
					Object tempVar2 = ann.accept(this);
					annotationsForStatement.add(tempVar2 instanceof OWLAnnotation ? (OWLAnnotation)tempVar2 : null);
				}

			}
		}
		return annotationsForStatement;
	}

	//        Dictionary<string, List<Ontorion.CNL.DL.Statement>> namedStatements4Instances = new Dictionary<string, List<Ontorion.CNL.DL.Statement>>();
	private HashMap<cognipy.cnl.dl.UnnamedInstance, ArrayList<cognipy.cnl.dl.Statement>> unnamedStatements4Instances = new HashMap<cognipy.cnl.dl.UnnamedInstance, ArrayList<cognipy.cnl.dl.Statement>>();
	//        Ontorion.CNL.DL.Statement currentStatement = null;
	private void setCurrentStatement(cognipy.cnl.dl.Statement stmt)
	{
		//            if (InconsistencyDebugMode) return;

		//            currentStatement = stmt;
	}

	//public List<Ontorion.CNL.DL.Instance> GetInstances()
	//{
	//    List<Ontorion.CNL.DL.Instance> ret = new List<Ontorion.CNL.DL.Instance>();
	//    foreach (string ni in namedStatements4Instances.Keys)
	//        ret.Add(new Ontorion.CNL.DL.NamedInstance(null) { name = ni });
	//    foreach(var k in unnamedStatements4Instances.Keys)
	//        ret.Add(k);
	//    return ret;
	//}


	//        public bool InconsistencyDebugMode = false;


	//public List<Ontorion.CNL.DL.Statement> GetDisabledStatements()
	//{
	//    List<Ontorion.CNL.DL.Statement> ret = new List<Ontorion.CNL.DL.Statement>();
	//    foreach (var stmt in disabledStatements)
	//    {
	//        if (stmt.Value)
	//            ret.Add(stmt.Key);
	//    }
	//    return ret;
	//}

	//Dictionary<Ontorion.CNL.DL.Statement, bool> disabledStatements = new Dictionary<Ontorion.CNL.DL.Statement, bool>();
	//public java.util.Set DisableEnableInstance(Ontorion.CNL.DL.Instance inst, bool val)
	//{
	//    if (!InconsistencyDebugMode) return null;

	//    java.util.Set ret = new java.util.HashSet();

	//    if (inst is Ontorion.CNL.DL.NamedInstance)
	//    {
	//        if (namedStatements4Instances.ContainsKey((inst as Ontorion.CNL.DL.NamedInstance).name))
	//        {
	//            foreach (var stmt in namedStatements4Instances[(inst as Ontorion.CNL.DL.NamedInstance).name])
	//            {
	//                disabledStatements[stmt] = val;
	//                ret.addAll(Convert(stmt));
	//            }
	//        }
	//    }
	//    else
	//    {
	//        if (unnamedStatements4Instances.ContainsKey(inst as Ontorion.CNL.DL.UnnamedInstance))
	//        {
	//            foreach (var stmt in unnamedStatements4Instances[inst as Ontorion.CNL.DL.UnnamedInstance])
	//            {
	//                disabledStatements[stmt] = val;
	//                ret.addAll(Convert(stmt));
	//            }
	//        }
	//    }

	//    return ret;
	//}

	//public void DisableAllInstances()
	//{
	//    var insts = GetInstances();
	//    foreach (var inst in insts)
	//    {
	//        if (inst is Ontorion.CNL.DL.NamedInstance)
	//        {
	//            if (namedStatements4Instances.ContainsKey((inst as Ontorion.CNL.DL.NamedInstance).name))
	//            {
	//                foreach (var stmt in namedStatements4Instances[(inst as Ontorion.CNL.DL.NamedInstance).name])
	//                {
	//                    disabledStatements[stmt] = true;
	//                }
	//            }
	//        }
	//        else
	//        {
	//            if (unnamedStatements4Instances.ContainsKey(inst as Ontorion.CNL.DL.UnnamedInstance))
	//            {
	//                foreach (var stmt in unnamedStatements4Instances[inst as Ontorion.CNL.DL.UnnamedInstance])
	//                {
	//                    disabledStatements[stmt] = true;
	//                }
	//            }
	//        }
	//    }
	//}
	//public java.util.Set DisableEnableStatement(Ontorion.CNL.DL.Statement stmt, bool val)
	//{
	//    if (!InconsistencyDebugMode) return null;
	//    disabledStatements[stmt] = val;
	//    return Convert(stmt);
	//}

	//bool isDisabledStatement(Ontorion.CNL.DL.Statement stmt)
	//{
	//    if (!InconsistencyDebugMode) return false;

	//    if (disabledStatements.ContainsKey(stmt))
	//    {
	//        return disabledStatements[stmt];
	//    }
	//    else
	//        return false;
	//}

	//        void setNamedStatement4Instance(Ontorion.CNL.DL.NamedInstance inst)
	//        {
	////            if (!InconsistencyDebugMode)
	////            {
	//                if (!namedStatements4Instances.ContainsKey(inst.name))
	//                    namedStatements4Instances[inst.name] = new List<Ontorion.CNL.DL.Statement>();
	//                namedStatements4Instances[inst.name].Add(currentStatement);
	////            }
	//        }
	//        void setUnnamedStatement4Instance(Ontorion.CNL.DL.UnnamedInstance inst)
	//        {
	////           if (!InconsistencyDebugMode)
	////            {
	//                if (!unnamedStatements4Instances.ContainsKey(inst))
	//                    unnamedStatements4Instances[inst] = new List<Ontorion.CNL.DL.Statement>();
	//                unnamedStatements4Instances[inst].Add(currentStatement);
	////            }
	//        }

	private cognipy.cnl.dl.NamedInstance getSingleNamgedInstance(cognipy.cnl.dl.Node C)
	{
		if (C instanceof cognipy.cnl.dl.InstanceSet)
		{
			if ((C instanceof cognipy.cnl.dl.InstanceSet ? (cognipy.cnl.dl.InstanceSet)C : null).Instances.size() == 1)
			{
				if ((C instanceof cognipy.cnl.dl.InstanceSet ? (cognipy.cnl.dl.InstanceSet)C : null).Instances.get(0) instanceof cognipy.cnl.dl.NamedInstance)
				{
					return (C instanceof cognipy.cnl.dl.InstanceSet ? (cognipy.cnl.dl.InstanceSet)C : null).Instances.get(0) instanceof cognipy.cnl.dl.NamedInstance ? (cognipy.cnl.dl.NamedInstance)(C instanceof cognipy.cnl.dl.InstanceSet ? (cognipy.cnl.dl.InstanceSet)C : null).Instances.get(0) : null;
				}
			}
		}
		return null;
	}

	private cognipy.cnl.dl.Value getSingleEqualValue(cognipy.cnl.dl.AbstractBound C)
	{
		if (C instanceof cognipy.cnl.dl.ValueSet)
		{
			if ((C instanceof cognipy.cnl.dl.ValueSet ? (cognipy.cnl.dl.ValueSet)C : null).Values.size() == 1)
			{
				return (C instanceof cognipy.cnl.dl.ValueSet ? (cognipy.cnl.dl.ValueSet)C : null).Values.get(0) instanceof cognipy.cnl.dl.Value ? (cognipy.cnl.dl.Value)(C instanceof cognipy.cnl.dl.ValueSet ? (cognipy.cnl.dl.ValueSet)C : null).Values.get(0) : null;
			}
		}
		else if (C instanceof cognipy.cnl.dl.BoundVal)
		{
			if ((C instanceof cognipy.cnl.dl.BoundVal ? (cognipy.cnl.dl.BoundVal)C : null).Kind.equals("="))
			{
				return (C instanceof cognipy.cnl.dl.BoundVal ? (cognipy.cnl.dl.BoundVal)C : null).V;
			}
		}
		else if (C instanceof cognipy.cnl.dl.BoundFacets)
		{
			if ((C instanceof cognipy.cnl.dl.BoundFacets ? (cognipy.cnl.dl.BoundFacets)C : null).FL.List.size() == 1 && (C instanceof cognipy.cnl.dl.BoundFacets ? (cognipy.cnl.dl.BoundFacets)C : null).FL.List.get(0).Kind.equals("="))
			{
				return (C instanceof cognipy.cnl.dl.BoundFacets ? (cognipy.cnl.dl.BoundFacets)C : null).FL.List.get(0).V;
			}
		}
		return null;
	}

	public final Object Visit(CNL.DL.Annotation a)
	{
		return a.txt;
	}

	public final Object Visit(CNL.DL.DLAnnotationAxiom a)
	{
		org.semanticweb.owlapi.model.OWLAnnotationProperty annotProp = factory.getOWLAnnotationProperty(owlNC.getIRIFromId(a.annotName, cognipy.ars.EntityKind.Role));
		OWLLiteral annotLit;
		if (!tangible.StringHelper.isNullOrEmpty(a.language))
		{
			annotLit = factory.getOWLLiteral(a.value, a.language);
		}
		else
		{
			annotLit = factory.getOWLLiteral(a.value);
		}

		org.semanticweb.owlapi.model.OWLAnnotation annotEl = factory.getOWLAnnotation(annotProp, annotLit, getOWLAnnotationForStatement(a));

		cognipy.ars.EntityKind result = cognipy.cnl.AnnotationManager.ParseSubjectKind(a.getSubjKind());

		if (result != EntityKind.Statement)
		{
			IRI tempVar = owlNC.getIRIFromId(a.getSubject(), result);
			OWLAnnotationSubject owlAnnotSubj = tempVar instanceof OWLAnnotationSubject ? (OWLAnnotationSubject)tempVar : null;
			return factory.getOWLAnnotationAssertionAxiom(owlAnnotSubj, annotEl);
		}
		else
		{
			return annotEl;
		}
	}

	public final Object Visit(cognipy.cnl.dl.Subsumption e)
	{
		try (context.set(VisitingContext.Concept))
		{
			setCurrentStatement(e);
			cognipy.cnl.dl.NamedInstance iC = getSingleNamgedInstance(e.C);
			cognipy.cnl.dl.NamedInstance iD = getSingleNamgedInstance(e.D);
			if (iC != null)
			{
				if (iD != null)
				{
					HashSet indivs = new HashSet();
					Object tempVar = iC.accept(this);
					indivs.add(tempVar instanceof OWLNamedIndividual ? (OWLNamedIndividual)tempVar : null);
					Object tempVar2 = iD.accept(this);
					indivs.add(tempVar2 instanceof OWLNamedIndividual ? (OWLNamedIndividual)tempVar2 : null);
					if (indivs.size() < 2)
					{
						throw new NoTautology();
					}
					else
					{
						return factory.getOWLSameIndividualAxiom(indivs, getOWLAnnotationForStatement(e));
					}
				}
				else
				{
					if (e.D instanceof SomeRestriction)
					{
						cognipy.cnl.dl.NamedInstance iS = getSingleNamgedInstance((e.D instanceof SomeRestriction ? (SomeRestriction)e.D : null).C);
						if (iS != null)
						{
							OWLObjectPropertyExpression r;
							try (context.set(VisitingContext.ObjectRole))
							{
								Object tempVar3 = (e.D instanceof SomeRestriction ? (SomeRestriction)e.D : null).R.accept(this);
								r = tempVar3 instanceof OWLObjectPropertyExpression ? (OWLObjectPropertyExpression)tempVar3 : null;
							}
							Object tempVar4 = iC.accept(this);
							Object tempVar5 = iS.accept(this);
							return factory.getOWLObjectPropertyAssertionAxiom(r, tempVar4 instanceof OWLNamedIndividual ? (OWLNamedIndividual)tempVar4 : null, tempVar5 instanceof OWLNamedIndividual ? (OWLNamedIndividual)tempVar5 : null, getOWLAnnotationForStatement(e));
						}
					}
					else if (e.D instanceof SomeValueRestriction)
					{
						cognipy.cnl.dl.Value iV = getSingleEqualValue((e.D instanceof SomeValueRestriction ? (SomeValueRestriction)e.D : null).B);
						if (iV != null)
						{
							OWLDataPropertyExpression r;
							try (context.set(VisitingContext.DataRole))
							{
								Object tempVar6 = (e.D instanceof SomeValueRestriction ? (SomeValueRestriction)e.D : null).R.accept(this);
								r = tempVar6 instanceof OWLDataPropertyExpression ? (OWLDataPropertyExpression)tempVar6 : null;
							}
							Object tempVar7 = iC.accept(this);
							Object tempVar8 = iV.accept(this);
							return factory.getOWLDataPropertyAssertionAxiom(r, tempVar7 instanceof OWLNamedIndividual ? (OWLNamedIndividual)tempVar7 : null, tempVar8 instanceof OWLLiteral ? (OWLLiteral)tempVar8 : null, getOWLAnnotationForStatement(e));
						}
					}
					Object tempVar9 = e.D.accept(this);
					Object tempVar10 = iC.accept(this);
					return factory.getOWLClassAssertionAxiom(tempVar9 instanceof OWLClassExpression ? (OWLClassExpression)tempVar9 : null, tempVar10 instanceof OWLNamedIndividual ? (OWLNamedIndividual)tempVar10 : null, getOWLAnnotationForStatement(e));
				}
			}
			if (e.C instanceof CNL.DL.Top)
			{
				if (e.D instanceof CNL.DL.NumberRestriction) // object functional
				{
					if (((e.D instanceof CNL.DL.NumberRestriction ? (CNL.DL.NumberRestriction)e.D : null).Kind.equals("≤") && Integer.parseInt((e.D instanceof CNL.DL.NumberRestriction ? (CNL.DL.NumberRestriction)e.D : null).N) == 1) || ((e.D instanceof CNL.DL.NumberRestriction ? (CNL.DL.NumberRestriction)e.D : null).Kind.equals("<") && Integer.parseInt((e.D instanceof CNL.DL.NumberRestriction ? (CNL.DL.NumberRestriction)e.D : null).N) == 2))
					{
						if ((e.D instanceof CNL.DL.NumberRestriction ? (CNL.DL.NumberRestriction)e.D : null).C instanceof CNL.DL.Top)
						{
							if ((e.D instanceof CNL.DL.NumberRestriction ? (CNL.DL.NumberRestriction)e.D : null).R instanceof Atomic)
							{
								OWLObjectPropertyExpression r;
								try (context.set(VisitingContext.ObjectRole))
								{
									Object tempVar11 = (e.D instanceof CNL.DL.NumberRestriction ? (CNL.DL.NumberRestriction)e.D : null).R.accept(this);
									r = tempVar11 instanceof OWLObjectPropertyExpression ? (OWLObjectPropertyExpression)tempVar11 : null;
								}
								return factory.getOWLFunctionalObjectPropertyAxiom(r, getOWLAnnotationForStatement(e));
							}
							else if ((e.D instanceof CNL.DL.NumberRestriction ? (CNL.DL.NumberRestriction)e.D : null).R instanceof RoleInversion)
							{
								if (((e.D instanceof CNL.DL.NumberRestriction ? (CNL.DL.NumberRestriction)e.D : null).R instanceof RoleInversion ? (RoleInversion)(e.D instanceof CNL.DL.NumberRestriction ? (CNL.DL.NumberRestriction)e.D : null).R : null).R instanceof Atomic)
								{
									OWLObjectPropertyExpression r;
									try (context.set(VisitingContext.ObjectRole))
									{
										Object tempVar12 = ((e.D instanceof CNL.DL.NumberRestriction ? (CNL.DL.NumberRestriction)e.D : null).R instanceof RoleInversion ? (RoleInversion)(e.D instanceof CNL.DL.NumberRestriction ? (CNL.DL.NumberRestriction)e.D : null).R : null).R.accept(this);
										r = tempVar12 instanceof OWLObjectPropertyExpression ? (OWLObjectPropertyExpression)tempVar12 : null;
									}
									return factory.getOWLInverseFunctionalObjectPropertyAxiom(r, getOWLAnnotationForStatement(e));
								}
							}
						}
					}
				}
				else if (e.D instanceof CNL.DL.NumberValueRestriction) //functional data
				{
					if (((e.D instanceof CNL.DL.NumberValueRestriction ? (CNL.DL.NumberValueRestriction)e.D : null).Kind.equals("≤") && Integer.parseInt((e.D instanceof CNL.DL.NumberValueRestriction ? (CNL.DL.NumberValueRestriction)e.D : null).N) == 1) || ((e.D instanceof CNL.DL.NumberValueRestriction ? (CNL.DL.NumberValueRestriction)e.D : null).Kind.equals("<") && Integer.parseInt((e.D instanceof CNL.DL.NumberValueRestriction ? (CNL.DL.NumberValueRestriction)e.D : null).N) == 2))
					{
						if ((e.D instanceof CNL.DL.NumberValueRestriction ? (CNL.DL.NumberValueRestriction)e.D : null).B instanceof CNL.DL.TopBound)
						{
							OWLDataPropertyExpression r;
							try (context.set(VisitingContext.DataRole))
							{
								Object tempVar13 = (e.D instanceof CNL.DL.NumberValueRestriction ? (CNL.DL.NumberValueRestriction)e.D : null).R.accept(this);
								r = tempVar13 instanceof OWLDataPropertyExpression ? (OWLDataPropertyExpression)tempVar13 : null;
							}
							return factory.getOWLFunctionalDataPropertyAxiom(r, getOWLAnnotationForStatement(e));
						}
					}
				}
				else if (e.D instanceof CNL.DL.OnlyRestriction) // object range
				{
					if ((e.D instanceof CNL.DL.OnlyRestriction ? (CNL.DL.OnlyRestriction)e.D : null).R instanceof Atomic)
					{
						OWLObjectPropertyExpression r;
						try (context.set(VisitingContext.ObjectRole))
						{
							Object tempVar14 = (e.D instanceof CNL.DL.OnlyRestriction ? (CNL.DL.OnlyRestriction)e.D : null).R.accept(this);
							r = tempVar14 instanceof OWLObjectPropertyExpression ? (OWLObjectPropertyExpression)tempVar14 : null;
						}

						Object tempVar15 = (e.D instanceof CNL.DL.OnlyRestriction ? (CNL.DL.OnlyRestriction)e.D : null).C.accept(this);
						return factory.getOWLObjectPropertyRangeAxiom(r, tempVar15 instanceof OWLClassExpression ? (OWLClassExpression)tempVar15 : null, getOWLAnnotationForStatement(e));
					}
				}
				else if (e.D instanceof CNL.DL.OnlyValueRestriction) // data range
				{

					if ((e.D instanceof CNL.DL.OnlyValueRestriction ? (CNL.DL.OnlyValueRestriction)e.D : null).R instanceof Atomic)
					{
						OWLDataPropertyExpression r;
						try (context.set(VisitingContext.DataRole))
						{
							Object tempVar16 = (e.D instanceof CNL.DL.OnlyValueRestriction ? (CNL.DL.OnlyValueRestriction)e.D : null).R.accept(this);
							r = tempVar16 instanceof OWLDataPropertyExpression ? (OWLDataPropertyExpression)tempVar16 : null;
						}

						Object tempVar17 = (e.D instanceof CNL.DL.OnlyValueRestriction ? (CNL.DL.OnlyValueRestriction)e.D : null).B.accept(this);
						return factory.getOWLDataPropertyRangeAxiom(r, tempVar17 instanceof OWLDataRange ? (OWLDataRange)tempVar17 : null, getOWLAnnotationForStatement(e));
					}
				}
				else if (e.D instanceof CNL.DL.SelfReference) // reflexive
				{
					OWLObjectPropertyExpression r;
					try (context.set(VisitingContext.ObjectRole))
					{
						Object tempVar18 = (e.D instanceof CNL.DL.SelfReference ? (CNL.DL.SelfReference)e.D : null).R.accept(this);
						r = tempVar18 instanceof OWLObjectPropertyExpression ? (OWLObjectPropertyExpression)tempVar18 : null;
					}

					return factory.getOWLReflexiveObjectPropertyAxiom(r, getOWLAnnotationForStatement(e));
				}
			}
			else if (e.C instanceof SomeRestriction)
			{
				if ((e.C instanceof SomeRestriction ? (SomeRestriction)e.C : null).C instanceof CNL.DL.Top) // object domain
				{
					if ((e.C instanceof CNL.DL.SomeRestriction ? (CNL.DL.SomeRestriction)e.C : null).R instanceof Atomic)
					{
						OWLObjectPropertyExpression r;
						try (context.set(VisitingContext.ObjectRole))
						{
							Object tempVar19 = (e.C instanceof CNL.DL.SomeRestriction ? (CNL.DL.SomeRestriction)e.C : null).R.accept(this);
							r = tempVar19 instanceof OWLObjectPropertyExpression ? (OWLObjectPropertyExpression)tempVar19 : null;
						}

						Object tempVar20 = e.D.accept(this);
						return factory.getOWLObjectPropertyDomainAxiom(r, tempVar20 instanceof OWLClassExpression ? (OWLClassExpression)tempVar20 : null, getOWLAnnotationForStatement(e));
					}
				}
			}
			else if (e.C instanceof SomeValueRestriction)
			{
				if ((e.C instanceof SomeValueRestriction ? (SomeValueRestriction)e.C : null).B instanceof CNL.DL.TopBound) // data domain
				{
					if ((e.C instanceof CNL.DL.SomeValueRestriction ? (CNL.DL.SomeValueRestriction)e.C : null).R instanceof Atomic)
					{
						OWLDataPropertyExpression r;
						try (context.set(VisitingContext.DataRole))
						{
							Object tempVar21 = (e.C instanceof CNL.DL.SomeValueRestriction ? (CNL.DL.SomeValueRestriction)e.C : null).R.accept(this);
							r = tempVar21 instanceof OWLDataPropertyExpression ? (OWLDataPropertyExpression)tempVar21 : null;
						}

						Object tempVar22 = e.D.accept(this);
						return factory.getOWLDataPropertyDomainAxiom(r, tempVar22 instanceof OWLClassExpression ? (OWLClassExpression)tempVar22 : null, getOWLAnnotationForStatement(e));
					}
				}
			}

			if (e.D instanceof Bottom)
			{
				if (e.C instanceof CNL.DL.SelfReference) // irreflexive
				{
					OWLObjectPropertyExpression r;
					try (context.set(VisitingContext.ObjectRole))
					{
						Object tempVar23 = (e.C instanceof CNL.DL.SelfReference ? (CNL.DL.SelfReference)e.C : null).R.accept(this);
						r = tempVar23 instanceof OWLObjectPropertyExpression ? (OWLObjectPropertyExpression)tempVar23 : null;
					}

					return factory.getOWLIrreflexiveObjectPropertyAxiom(r, getOWLAnnotationForStatement(e));
				}
			}
			Object tempVar24 = e.C.accept(this);
			Object tempVar25 = e.D.accept(this);
			return factory.getOWLSubClassOfAxiom(tempVar24 instanceof OWLClassExpression ? (OWLClassExpression)tempVar24 : null, tempVar25 instanceof OWLClassExpression ? (OWLClassExpression)tempVar25 : null, getOWLAnnotationForStatement(e));
		}
	}

	public final Object Visit(cognipy.cnl.dl.Equivalence e)
	{
		try (context.set(VisitingContext.Concept))
		{
			setCurrentStatement(e);
			boolean allIndivs = true;
			for (Node x : e.Equivalents)
			{
				cognipy.cnl.dl.NamedInstance ic = getSingleNamgedInstance(x);
				if (ic == null)
				{
					allIndivs = false;
					break;
				}
			}
			Set s = new HashSet();
			if (allIndivs)
			{
				for (Node x : e.Equivalents)
				{
					cognipy.cnl.dl.NamedInstance ic = getSingleNamgedInstance(x);
					s.add(ic.accept(this));
				}
				if (s.size() < 2)
				{
					throw new NoTautology();
				}
				else
				{
					return factory.getOWLSameIndividualAxiom(s, getOWLAnnotationForStatement(e));
				}
			}
			else
			{
				for (Node x : e.Equivalents)
				{
					s.add(x.accept(this));
				}
				if (s.size() < 2)
				{
					throw new NoTautology();
				}
				else
				{
					return factory.getOWLEquivalentClassesAxiom(s, getOWLAnnotationForStatement(e));
				}
			}
		}
	}

	public final Object Visit(cognipy.cnl.dl.Disjoint e)
	{
		try (context.set(VisitingContext.Concept))
		{
			setCurrentStatement(e);
			Set s = new HashSet();
			for (Node x : e.Disjoints)
			{
				s.add(x.accept(this));
			}
			if (s.size() < 2)
			{
				throw new DifferenceToItsef();
			}
			else
			{
				return factory.getOWLDisjointClassesAxiom(s, getOWLAnnotationForStatement(e));
			}
		}
	}

	public final Object Visit(cognipy.cnl.dl.DisjointUnion e)
	{
		try (context.set(VisitingContext.Concept))
		{
			setCurrentStatement(e);
			Set s = new HashSet();
			for (Node x : e.Union)
			{
				s.add(x.accept(this));
			}
			if (s.size() < 2)
			{
				throw new DifferenceToItsef();
			}
			else
			{
				return factory.getOWLDisjointUnionAxiom(factory.getOWLClass(owlNC.getIRIFromId(e.name, EntityKind.Concept)), s, getOWLAnnotationForStatement(e));
			}
		}
	}

	public final Object Visit(DataTypeDefinition e)
	{
		try (context.set(VisitingContext.Concept))
		{
			setCurrentStatement(e);
			Object tempVar = e.B.accept(this);
			return factory.getOWLDatatypeDefinitionAxiom(factory.getOWLDatatype(owlNC.getIRIFromId(e.name, EntityKind.Concept)), tempVar instanceof OWLDataRange ? (OWLDataRange)tempVar : null, getOWLAnnotationForStatement(e));
		}
	}

	public final Object Visit(DTBound e)
	{
		return factory.getOWLDatatype(owlNC.getIRIFromId(e.name, EntityKind.Concept));
	}

	public final Object Visit(cognipy.cnl.dl.RoleInclusion e)
	{
		try (context.set(VisitingContext.ObjectRole))
		{
			setCurrentStatement(e);
			if ((e.C instanceof RoleInversion) && (e.C instanceof RoleInversion ? (RoleInversion)e.C : null).R instanceof Atomic && e.D instanceof Atomic)
			{
				if ((e.D instanceof Atomic ? (Atomic)e.D : null).id.equals(((e.C instanceof RoleInversion ? (RoleInversion)e.C : null).R instanceof Atomic ? (Atomic)(e.C instanceof RoleInversion ? (RoleInversion)e.C : null).R : null).id))
				{ //symmetric
					Object tempVar = e.D.accept(this);
					return factory.getOWLSymmetricObjectPropertyAxiom(tempVar instanceof OWLObjectPropertyExpression ? (OWLObjectPropertyExpression)tempVar : null, getOWLAnnotationForStatement(e));
				}
				else
				{ // role inversion
					Object tempVar2 = (e.C instanceof RoleInversion ? (RoleInversion)e.C : null).R.accept(this);
					Object tempVar3 = e.D.accept(this);
					return factory.getOWLInverseObjectPropertiesAxiom(tempVar2 instanceof OWLObjectPropertyExpression ? (OWLObjectPropertyExpression)tempVar2 : null, tempVar3 instanceof OWLObjectPropertyExpression ? (OWLObjectPropertyExpression)tempVar3 : null, getOWLAnnotationForStatement(e));
				}
			}
			Object tempVar4 = e.C.accept(this);
			Object tempVar5 = e.D.accept(this);
			return factory.getOWLSubObjectPropertyOfAxiom(tempVar4 instanceof OWLObjectPropertyExpression ? (OWLObjectPropertyExpression)tempVar4 : null, tempVar5 instanceof OWLObjectPropertyExpression ? (OWLObjectPropertyExpression)tempVar5 : null, getOWLAnnotationForStatement(e));
		}
	}

	public final Object Visit(cognipy.cnl.dl.RoleEquivalence e)
	{
		try (context.set(VisitingContext.ObjectRole))
		{
			setCurrentStatement(e);

			Set s = new HashSet();
			for (Node x : e.Equivalents)
			{
				s.add(x.accept(this));
			}
			//if (s.size() < 2)
			//    throw new NoTautology();
			//else
			return factory.getOWLEquivalentObjectPropertiesAxiom(s, getOWLAnnotationForStatement(e));
		}
	}

	public final Object Visit(cognipy.cnl.dl.RoleDisjoint e)
	{
		try (context.set(VisitingContext.ObjectRole))
		{
			setCurrentStatement(e);

			if (e.Disjoints.size() == 2)
			{
				if ((e.Disjoints.get(0) instanceof RoleInversion) && (e.Disjoints.get(0) instanceof RoleInversion ? (RoleInversion)e.Disjoints.get(0) : null).R instanceof Atomic && e.Disjoints.get(1) instanceof Atomic)
				{
					if ((e.Disjoints.get(1) instanceof Atomic ? (Atomic)e.Disjoints.get(1) : null).id.equals(((e.Disjoints.get(0) instanceof RoleInversion ? (RoleInversion)e.Disjoints.get(0) : null).R instanceof Atomic ? (Atomic)(e.Disjoints.get(0) instanceof RoleInversion ? (RoleInversion)e.Disjoints.get(0) : null).R : null).id))
					{ //asymmetric
						Object tempVar = e.Disjoints.get(1).accept(this);
						return factory.getOWLAsymmetricObjectPropertyAxiom(tempVar instanceof OWLObjectPropertyExpression ? (OWLObjectPropertyExpression)tempVar : null, getOWLAnnotationForStatement(e));
					}
				}
				else if ((e.Disjoints.get(1) instanceof RoleInversion) && (e.Disjoints.get(1) instanceof RoleInversion ? (RoleInversion)e.Disjoints.get(1) : null).R instanceof Atomic && e.Disjoints.get(0) instanceof Atomic)
				{
					if ((e.Disjoints.get(0) instanceof Atomic ? (Atomic)e.Disjoints.get(0) : null).id.equals(((e.Disjoints.get(1) instanceof RoleInversion ? (RoleInversion)e.Disjoints.get(1) : null).R instanceof Atomic ? (Atomic)(e.Disjoints.get(1) instanceof RoleInversion ? (RoleInversion)e.Disjoints.get(1) : null).R : null).id))
					{ //asymmetric
						Object tempVar2 = e.Disjoints.get(0).accept(this);
						return factory.getOWLAsymmetricObjectPropertyAxiom(tempVar2 instanceof OWLObjectPropertyExpression ? (OWLObjectPropertyExpression)tempVar2 : null, getOWLAnnotationForStatement(e));
					}
				}
			}

			Set s = new HashSet();
			for (Node x : e.Disjoints)
			{
				s.add(x.accept(this));
			}
			if (s.size() < 2)
			{
				throw new DifferenceToItsef();
			}
			else
			{
				return factory.getOWLDisjointObjectPropertiesAxiom(s, getOWLAnnotationForStatement(e));
			}
		}
	}

	public final Object Visit(cognipy.cnl.dl.ComplexRoleInclusion e)
	{
		try (context.set(VisitingContext.ObjectRole))
		{
			setCurrentStatement(e);

			if (e.R instanceof Atomic && e.RoleChain.size() == 2 && e.RoleChain.get(0) instanceof Atomic && e.RoleChain.get(1) instanceof Atomic)
			{
				if ((e.R instanceof Atomic ? (Atomic)e.R : null).id.equals((e.RoleChain.get(0) instanceof Atomic ? (Atomic)e.RoleChain.get(0) : null).id) && (e.R instanceof Atomic ? (Atomic)e.R : null).id.equals((e.RoleChain.get(1) instanceof Atomic ? (Atomic)e.RoleChain.get(1) : null).id))
				{
					//transitive 
					Object tempVar = e.R.accept(this);
					return factory.getOWLTransitiveObjectPropertyAxiom(tempVar instanceof OWLObjectPropertyExpression ? (OWLObjectPropertyExpression)tempVar : null, getOWLAnnotationForStatement(e));
				}
			}

			List chain = new ArrayList();
			for (Node r : e.RoleChain)
			{
				Object tempVar2 = r.accept(this);
				chain.add(tempVar2 instanceof OWLObjectPropertyExpression ? (OWLObjectPropertyExpression)tempVar2 : null);
			}

			Object tempVar3 = e.R.accept(this);
			return factory.getOWLSubPropertyChainOfAxiom(chain, tempVar3 instanceof OWLObjectPropertyExpression ? (OWLObjectPropertyExpression)tempVar3 : null, getOWLAnnotationForStatement(e));
		}
	}

	public final Object Visit(cognipy.cnl.dl.DataRoleInclusion e)
	{
		try (context.set(VisitingContext.DataRole))
		{
			setCurrentStatement(e);
			Object tempVar = e.C.accept(this);
			Object tempVar2 = e.D.accept(this);
			return factory.getOWLSubDataPropertyOfAxiom(tempVar instanceof OWLDataPropertyExpression ? (OWLDataPropertyExpression)tempVar : null, tempVar2 instanceof OWLDataPropertyExpression ? (OWLDataPropertyExpression)tempVar2 : null, getOWLAnnotationForStatement(e));
		}
	}

	public final Object Visit(cognipy.cnl.dl.DataRoleEquivalence e)
	{
		try (context.set(VisitingContext.DataRole))
		{
			setCurrentStatement(e);
			Set s = new HashSet();
			for (Node x : e.Equivalents)
			{
				s.add(x.accept(this));
			}
			if (s.size() < 2)
			{
				throw new NoTautology();
			}
			else
			{
				return factory.getOWLEquivalentDataPropertiesAxiom(s, getOWLAnnotationForStatement(e));
			}
		}
	}

	public final Object Visit(cognipy.cnl.dl.DataRoleDisjoint e)
	{
		try (context.set(VisitingContext.DataRole))
		{
			setCurrentStatement(e);
			Set s = new HashSet();
			for (Node x : e.Disjoints)
			{
				s.add(x.accept(this));
			}
			if (s.size() < 2)
			{
				throw new DifferenceToItsef();
			}
			else
			{
				return factory.getOWLDisjointDataPropertiesAxiom(s, getOWLAnnotationForStatement(e));
			}
		}
	}

	public final Object Visit(cognipy.cnl.dl.InstanceOf e)
	{
		setCurrentStatement(e);
		Object tempVar = e.C.accept(this);
		Object tempVar2 = e.I.accept(this);
		return factory.getOWLClassAssertionAxiom(tempVar instanceof OWLClassExpression ? (OWLClassExpression)tempVar : null, tempVar2 instanceof OWLIndividual ? (OWLIndividual)tempVar2 : null, getOWLAnnotationForStatement(e));
	}

	public final Object Visit(cognipy.cnl.dl.RelatedInstances e)
	{
		setCurrentStatement(e);
		OWLObjectPropertyExpression prop;
		try (context.set(VisitingContext.ObjectRole))
		{
			Object tempVar = e.R.accept(this);
			prop = tempVar instanceof OWLObjectPropertyExpression ? (OWLObjectPropertyExpression)tempVar : null;
		}
		Object tempVar2 = e.I.accept(this);
		Object tempVar3 = e.J.accept(this);
		return factory.getOWLObjectPropertyAssertionAxiom(prop, tempVar2 instanceof OWLIndividual ? (OWLIndividual)tempVar2 : null, tempVar3 instanceof OWLIndividual ? (OWLIndividual)tempVar3 : null, getOWLAnnotationForStatement(e));
	}

	public final Object Visit(cognipy.cnl.dl.InstanceValue e)
	{
		setCurrentStatement(e);
		OWLDataPropertyExpression prop;
		try (context.set(VisitingContext.DataRole))
		{
			Object tempVar = e.R.accept(this);
			prop = tempVar instanceof OWLDataPropertyExpression ? (OWLDataPropertyExpression)tempVar : null;
		}
		Object tempVar2 = e.I.accept(this);
		Object tempVar3 = e.V.accept(this);
		return factory.getOWLDataPropertyAssertionAxiom(prop, tempVar2 instanceof OWLIndividual ? (OWLIndividual)tempVar2 : null, tempVar3 instanceof OWLLiteral ? (OWLLiteral)tempVar3 : null, getOWLAnnotationForStatement(e));
	}

	public final Object Visit(cognipy.cnl.dl.SameInstances e)
	{
		setCurrentStatement(e);
		HashSet indivs = new HashSet();
		for (Instance x : e.Instances)
		{
			Assert(x instanceof CNL.DL.NamedInstance);
			indivs.add(factory.getOWLNamedIndividual(owlNC.getIRIFromId((x instanceof CNL.DL.NamedInstance ? (CNL.DL.NamedInstance)x : null).name, EntityKind.Instance)));
		}
		if (indivs.size() < 2)
		{
			throw new NoTautology();
		}
		else
		{
			return factory.getOWLSameIndividualAxiom(indivs, getOWLAnnotationForStatement(e));
		}
	}

	public final Object Visit(cognipy.cnl.dl.DifferentInstances e)
	{
		setCurrentStatement(e);
		HashSet indivs = new HashSet();
		for (Instance x : e.Instances)
		{
			Assert(x instanceof CNL.DL.NamedInstance);
			indivs.add(factory.getOWLNamedIndividual(owlNC.getIRIFromId((x instanceof CNL.DL.NamedInstance ? (CNL.DL.NamedInstance)x : null).name, EntityKind.Instance)));
		}
		if (indivs.size() < 2)
		{
			throw new DifferenceToItsef();
		}
		else
		{
			return factory.getOWLDifferentIndividualsAxiom(indivs, getOWLAnnotationForStatement(e));
		}
	}

	public final Object Visit(cognipy.cnl.dl.HasKey e)
	{
		setCurrentStatement(e);
		Set roleSet = new HashSet();
		try (context.set(VisitingContext.ObjectRole))
		{
			for (Node x : e.Roles)
			{
				roleSet.add(x.accept(this));
			}
		}
		try (context.set(VisitingContext.DataRole))
		{
			for (Node x : e.DataRoles)
			{
				roleSet.add(x.accept(this));
			}
		}
		Object tempVar = e.C.accept(this);
		return factory.getOWLHasKeyAxiom(tempVar instanceof OWLClassExpression ? (OWLClassExpression)tempVar : null, roleSet, getOWLAnnotationForStatement(e));
	}

	public final Object Visit(cognipy.cnl.dl.NamedInstance e)
	{
		//            setNamedStatement4Instance(e);
		return factory.getOWLNamedIndividual(owlNC.getIRIFromId(e.name, EntityKind.Instance));
	}


	public final Object Visit(cognipy.cnl.dl.UnnamedInstance e)
	{
		//            setUnnamedStatement4Instance(e);
		OWLIndividual ni;
		if (forReasoning)
		{
			cognipy.cnl.dl.Serializer dlserializer = new cognipy.cnl.dl.Serializer(false);
			String name = "\"" + dlserializer.Serialize(e.C).replace("\"", "\"\"") + "." + UUID.NewGuid().toString("N") + "_uUu_" + "\"";
			ni = factory.getOWLNamedIndividual(owlNC.getIRIFromId(name, EntityKind.Instance));
		}
		else
		{
			ni = factory.getOWLAnonymousIndividual();
		}

		if (e.Only)
		{
			Object tempVar = e.C.accept(this);
			OWLClassExpression c = tempVar instanceof OWLClassExpression ? (OWLClassExpression)tempVar : null;
			if (!c.isTopEntity())
			{
				Set inds = new HashSet();
				inds.add(ni);
				additionalAxioms.add(factory.getOWLEquivalentClassesAxiom(c, factory.getOWLObjectOneOf(inds)));
			}
		}
		else
		{
			Object tempVar2 = e.C.accept(this);
			OWLClassExpression c = tempVar2 instanceof OWLClassExpression ? (OWLClassExpression)tempVar2 : null;
			if (!c.isTopEntity())
			{
				additionalAxioms.add(factory.getOWLClassAssertionAxiom(c, ni));
			}
		}
		return ni;
	}

	public final Object Visit(cognipy.cnl.dl.Number e)
	{
		//return factory.getOWLLiteral(int.Parse(e.val));
		return factory.getOWLLiteral(e.ToInt());
	}
	public final Object Visit(cognipy.cnl.dl.Bool e)
	{
		//object tt = factory.getOWLLiteral(bool.Parse(e.ToBool().ToString()));
		//return factory.getOWLLiteral(bool.Parse(e.val));
		return factory.getOWLLiteral(e.ToBool());
	}


	private OWLLiteral getLiteralVal(Value v)
	{
		if (v instanceof CNL.DL.Bool)
		{
			return factory.getOWLLiteral(v.ToBool());
		}
		else if (v instanceof CNL.DL.String)
		{
			return factory.getOWLLiteral(v.toString(), OWL2Datatype.XSD_STRING);
		}
		else if (v instanceof CNL.DL.Float)
		{
			return factory.getOWLLiteral(v.toString(), OWL2Datatype.XSD_DOUBLE);
		}
		else if (v instanceof CNL.DL.Number)
		{
			return factory.getOWLLiteral(v.ToInt());
		}
		else if (v instanceof CNL.DL.DateTimeVal)
		{
			return factory.getOWLLiteral(completeDTMVal(v.ToStringExact()), OWL2Datatype.XSD_DATE_TIME);
		}
		else if (v instanceof CNL.DL.Duration)
		{
			return factory.getOWLLiteral(v.ToStringExact() + "^^http://www.w3.org/2001/XMLSchema#dayTimeDuration", factory.getRDFPlainLiteral());
		}
		else
		{
			return factory.getOWLLiteral(v.toString()); //TODO xsd:date i inne typy
		}
	}

	private static Regex DtmRg = new Regex("(?<date>([1-9][0-9]{3}-[0-1][0-9]-[0-3][0-9]))(?<time>(T[0-2][0-9]:[0-5][0-9](:[0-5][0-9](.[0-9]+)?)?)(Z|((\\+|\\-)[0-2][0-9]:[0-5][0-9]))?)?", RegexOptions.Compiled);
	private static String completeDTMVal(String val)
	{
		System.Text.RegularExpressions.Match m = DtmRg.Match(val);
		String dta = m.Groups["date"].Value;
		String tm = m.Groups["time"].Value;
		StringBuilder sb = new StringBuilder();
		sb.append(dta);
		if (tangible.StringHelper.isNullOrEmpty(tm))
		{
			sb.append("T00:00:00");
		}
		else
		{
			sb.append(tm);
		}
		if (tm.length() == "T00:00".length())
		{
			sb.append(":00");
		}
		return sb.toString();
	}


	public final Object Visit(cognipy.cnl.dl.DateTimeVal e)
	{
		return getLiteralVal(e);
	}

	public final Object Visit(cognipy.cnl.dl.Duration e)
	{
		return getLiteralVal(e);
	}

	public final Object Visit(cognipy.cnl.dl.String e)
	{
		return getLiteralVal(e);
	}
	public final Object Visit(cognipy.cnl.dl.Float e)
	{
		return getLiteralVal(e);
	}

	private static CultureInfo en_cult = new CultureInfo("en-US");
	private OWLDatatype forcedDatatype = null;
	public final Object Visit(Facet e)
	{
		OWLLiteral val = getLiteralVal(e.V);
		if (e.Kind.equals("≤"))
		{
			return factory.getOWLFacetRestriction(OWLFacet.MAX_INCLUSIVE, val);
		}
		else if (e.Kind.equals("<"))
		{
			return factory.getOWLFacetRestriction(OWLFacet.MAX_EXCLUSIVE, val);
		}
		else if (e.Kind.equals("≥"))
		{
			return factory.getOWLFacetRestriction(OWLFacet.MIN_INCLUSIVE, val);
		}
		else if (e.Kind.equals(">"))
		{
			return factory.getOWLFacetRestriction(OWLFacet.MIN_EXCLUSIVE, val);
		}
		else if (e.Kind.equals("#"))
		{
			forcedDatatype = factory.getOWLDatatype(XSDVocabulary.STRING.getIRI());
			return factory.getOWLFacetRestriction(OWLFacet.PATTERN, val);
		}
		else if (e.Kind.equals("<->"))
		{
			forcedDatatype = factory.getOWLDatatype(XSDVocabulary.STRING.getIRI());
			return factory.getOWLFacetRestriction(OWLFacet.LENGTH, val);
		}
		else if (e.Kind.equals("<-> ≥"))
		{
			forcedDatatype = factory.getOWLDatatype(XSDVocabulary.STRING.getIRI());
			return factory.getOWLFacetRestriction(OWLFacet.MIN_LENGTH, val);
		}
		else if (e.Kind.equals("<-> ≤"))
		{
			forcedDatatype = factory.getOWLDatatype(XSDVocabulary.STRING.getIRI());
			return factory.getOWLFacetRestriction(OWLFacet.MAX_LENGTH, val);
		}

		Assert(false);
		return null;
	}

	public final Object Visit(FacetList e)
	{
		HashSet ret = new HashSet();
		for (Facet f : e.List)
		{
			ret.add(f.accept(this));
		}
		return ret;
	}

	public final Object Visit(BoundFacets e)
	{
		org.semanticweb.owlapi.model.OWLDatatype dtp = getLiteralVal(e.FL.List.get(0).V).getDatatype();
		forcedDatatype = null;
		Object tempVar = e.FL.accept(this);
		Set set = tempVar instanceof Set ? (Set)tempVar : null;

		return factory.getOWLDatatypeRestriction(forcedDatatype == null ? dtp : forcedDatatype, set);
	}

	public final Object Visit(BoundOr e)
	{
		Set s = new HashSet();
		for (AbstractBound i : e.List)
		{
			s.add(i.accept(this));
		}
		return factory.getOWLDataUnionOf(s);
	}

	public final Object Visit(BoundAnd e)
	{
		Set s = new HashSet();
		for (AbstractBound i : e.List)
		{
			s.add(i.accept(this));
		}
		return factory.getOWLDataIntersectionOf(s);
	}

	public final Object Visit(BoundNot e)
	{
		Object tempVar = e.B.accept(this);
		return factory.getOWLDataComplementOf(tempVar instanceof OWLDataRange ? (OWLDataRange)tempVar : null);
	}

	public final Object Visit(BoundVal e)
	{
		OWLLiteral val = getLiteralVal(e.V);
		if (e.Kind.equals("="))
		{
			return factory.getOWLDataOneOf(val);
		}
		else // if (e.Kind == "≠")
		{
			return factory.getOWLDataComplementOf(factory.getOWLDataOneOf(val));
		}
	}

	public final Object Visit(cognipy.cnl.dl.TotalBound e)
	{
		org.semanticweb.owlapi.model.OWLDatatype dt = getLiteralVal(e.V).getDatatype();
		if (forReasoning && (dt.toString().equals("http://www.w3.org/2001/XMLSchema#double")))
		{
			return factory.getOWLDataUnionOf(factory.getOWLDatatypeMinInclusiveRestriction(0.0), factory.getOWLDatatypeMaxInclusiveRestriction(0.0));
		}
		else
		{
			return dt;
		}
	}

	public final Object Visit(cognipy.cnl.dl.TopBound e)
	{
		return factory.getTopDatatype();
	}

	public final Object Visit(cognipy.cnl.dl.ValueSet e)
	{
		Set vals = new HashSet();
		for (Value val : e.Values)
		{
			vals.add(val.accept(this));
		}
		return factory.getOWLDataOneOf(vals);
	}

	private enum VisitingContext
	{
		Concept,
		ObjectRole,
		DataRole;

		public static final int SIZE = java.lang.Integer.SIZE;

		public int getValue()
		{
			return this.ordinal();
		}

		public static VisitingContext forValue(int value)
		{
			return values()[value];
		}
	}
	private cognipy.cnl.dl.VisitingParam<VisitingContext> context = new cognipy.cnl.dl.VisitingParam<VisitingContext>(VisitingContext.Concept);

	public final Object Visit(cognipy.cnl.dl.Atomic e)
	{
		switch (context.get())
		{
			case VisitingContext.Concept:
			{
					org.semanticweb.owlapi.model.OWLClass cls = factory.getOWLClass(owlNC.getIRIFromId(e.id, EntityKind.Concept));
					if (resolvingReasoner != null)
					{
						Set set = resolvingReasoner.getInstances(cls, false).getFlattened();
						org.semanticweb.owlapi.model.OWLObjectOneOf oneOf = factory.getOWLObjectOneOf(set);
						org.semanticweb.owlapi.model.OWLObjectIntersectionOf inters = factory.getOWLObjectIntersectionOf(cls, oneOf);
						return inters;
					}
					else
					{
						return cls;
					}
			}
			case VisitingContext.ObjectRole:
				return factory.getOWLObjectProperty(owlNC.getIRIFromId(e.id, EntityKind.Role));
			case VisitingContext.DataRole:
				return factory.getOWLDataProperty(owlNC.getIRIFromId(e.id, EntityKind.Role));
		}
		Assert(false);
		return null;
	}
	public final Object Visit(cognipy.cnl.dl.Top e)
	{
		switch (context.get())
		{
			case VisitingContext.Concept:
				return factory.getOWLThing();
			case VisitingContext.ObjectRole:
				return factory.getOWLTopObjectProperty();
			case VisitingContext.DataRole:
				return factory.getOWLTopDataProperty();
		}
		Assert(false);
		return null;
	}
	public final Object Visit(cognipy.cnl.dl.Bottom e)
	{
		switch (context.get())
		{
			case VisitingContext.Concept:
				return factory.getOWLNothing();
			case VisitingContext.ObjectRole:
				return factory.getOWLBottomObjectProperty();
			case VisitingContext.DataRole:
				return factory.getOWLBottomDataProperty();
		}
		Assert(false);
		return null;
	}
	public final Object Visit(cognipy.cnl.dl.RoleInversion e)
	{
		if (context.get() == VisitingContext.DataRole)
		{
			throw new NoInversionsForDataRolesException((e.R instanceof cognipy.cnl.dl.Atomic ? (cognipy.cnl.dl.Atomic)e.R : null).id);
		}
		Assert(context.get() == VisitingContext.ObjectRole);
		Object o = e.R.accept(this);
		Assert(o instanceof OWLObjectPropertyExpression);
		return factory.getOWLObjectInverseOf(o instanceof OWLObjectPropertyExpression ? (OWLObjectPropertyExpression)o : null);
	}
	public final Object Visit(cognipy.cnl.dl.InstanceSet e)
	{
		Set inds = new HashSet();
		for (Instance c : e.Instances)
		{
			inds.add(c.accept(this));
		}
		return factory.getOWLObjectOneOf(inds);
	}
	public final Object Visit(cognipy.cnl.dl.ConceptOr e)
	{
		Set clss = new HashSet();
		for (Node c : e.Exprs)
		{
			clss.add(c.accept(this));
		}
		return factory.getOWLObjectUnionOf(clss);
	}
	public final Object Visit(cognipy.cnl.dl.ConceptAnd e)
	{
		Set clss = new HashSet();
		for (Node c : e.Exprs)
		{
			clss.add(c.accept(this));
		}
		return factory.getOWLObjectIntersectionOf(clss);
	}
	public final Object Visit(cognipy.cnl.dl.ConceptNot e)
	{
		Object tempVar = e.C.accept(this);
		return factory.getOWLObjectComplementOf(tempVar instanceof OWLClassExpression ? (OWLClassExpression)tempVar : null);
	}
	public final Object Visit(cognipy.cnl.dl.OnlyRestriction e)
	{
		Object owlpe = null;
		try (context.set(VisitingContext.ObjectRole))
		{
			owlpe = e.R.accept(this);
			Assert(owlpe != null && owlpe instanceof OWLObjectPropertyExpression);
		}
		Object owlce = null;
		try (context.set(VisitingContext.Concept))
		{
			owlce = e.C.accept(this);
			Assert(owlce != null && owlce instanceof OWLClassExpression);
		}
		return factory.getOWLObjectAllValuesFrom(owlpe instanceof OWLObjectPropertyExpression ? (OWLObjectPropertyExpression)owlpe : null, owlce instanceof OWLClassExpression ? (OWLClassExpression)owlce : null);
	}
	public final Object Visit(cognipy.cnl.dl.SomeRestriction e)
	{
		Object owlpe = null;
		try (context.set(VisitingContext.ObjectRole))
		{
			owlpe = e.R.accept(this);
			Assert(owlpe != null && owlpe instanceof OWLObjectPropertyExpression);
		}
		Object owlce = null;
		try (context.set(VisitingContext.Concept))
		{
			owlce = e.C.accept(this);
			Assert(owlce != null && owlce instanceof OWLClassExpression);
		}
		return factory.getOWLObjectSomeValuesFrom(owlpe instanceof OWLObjectPropertyExpression ? (OWLObjectPropertyExpression)owlpe : null, owlce instanceof OWLClassExpression ? (OWLClassExpression)owlce : null);
	}
	public final Object Visit(cognipy.cnl.dl.OnlyValueRestriction e)
	{
		Object owlpe = null;
		try (context.set(VisitingContext.DataRole))
		{
			owlpe = e.R.accept(this);
			Assert(owlpe != null && owlpe instanceof OWLDataPropertyExpression);
			Object owlb = null;
			owlb = e.B.accept(this);
			Assert(owlb != null);
			return factory.getOWLDataAllValuesFrom(owlpe instanceof OWLDataPropertyExpression ? (OWLDataPropertyExpression)owlpe : null, owlb instanceof OWLDataRange ? (OWLDataRange)owlb : null);
		}
	}
	public final Object Visit(cognipy.cnl.dl.SomeValueRestriction e)
	{
		Object owlpe = null;
		try (context.set(VisitingContext.DataRole))
		{
			owlpe = e.R.accept(this);
			Assert(owlpe != null && owlpe instanceof OWLDataPropertyExpression);
			Object owlb = null;
			owlb = e.B.accept(this);
			Assert(owlb != null);
			return factory.getOWLDataSomeValuesFrom(owlpe instanceof OWLDataPropertyExpression ? (OWLDataPropertyExpression)owlpe : null, owlb instanceof OWLDataRange ? (OWLDataRange)owlb : null);
		}
	}
	public final Object Visit(cognipy.cnl.dl.SelfReference e)
	{
		Object owlpe = null;
		try (context.set(VisitingContext.ObjectRole))
		{
			owlpe = e.R.accept(this);
			Assert(owlpe != null && owlpe instanceof OWLObjectPropertyExpression);
		}
		return factory.getOWLObjectHasSelf(owlpe instanceof OWLObjectPropertyExpression ? (OWLObjectPropertyExpression)owlpe : null);
	}
	public final Object Visit(cognipy.cnl.dl.NumberRestriction e)
	{
		Object owlpe = null;
		try (context.set(VisitingContext.ObjectRole))
		{
			owlpe = e.R.accept(this);
			Assert(owlpe != null && owlpe instanceof OWLObjectPropertyExpression);
		}
		Object owlce = null;
		try (context.set(VisitingContext.Concept))
		{
			owlce = e.C.accept(this);
			Assert(owlce != null && owlce instanceof OWLClassExpression);
		}
		if (e.Kind.equals("="))
		{
			return factory.getOWLObjectExactCardinality(Integer.parseInt(e.N), owlpe instanceof OWLObjectPropertyExpression ? (OWLObjectPropertyExpression)owlpe : null, owlce instanceof OWLClassExpression ? (OWLClassExpression)owlce : null);
		}
		else if (e.Kind.equals("≤") || e.Kind.equals("<"))
		{
			return factory.getOWLObjectMaxCardinality(e.Kind.equals("<") ? Integer.parseInt(e.N) - 1 : Integer.parseInt(e.N), owlpe instanceof OWLObjectPropertyExpression ? (OWLObjectPropertyExpression)owlpe : null, owlce instanceof OWLClassExpression ? (OWLClassExpression)owlce : null);
		}
		else if (e.Kind.equals("≥") || e.Kind.equals(">"))
		{
			return factory.getOWLObjectMinCardinality(e.Kind.equals(">") ? Integer.parseInt(e.N) + 1 : Integer.parseInt(e.N), owlpe instanceof OWLObjectPropertyExpression ? (OWLObjectPropertyExpression)owlpe : null, owlce instanceof OWLClassExpression ? (OWLClassExpression)owlce : null);
		}
		else // different
		{
			return factory.getOWLObjectUnionOf(factory.getOWLObjectMaxCardinality(Integer.parseInt(e.N) - 1, owlpe instanceof OWLObjectPropertyExpression ? (OWLObjectPropertyExpression)owlpe : null, owlce instanceof OWLClassExpression ? (OWLClassExpression)owlce : null), factory.getOWLObjectMinCardinality(Integer.parseInt(e.N) + 1, owlpe instanceof OWLObjectPropertyExpression ? (OWLObjectPropertyExpression)owlpe : null, owlce instanceof OWLClassExpression ? (OWLClassExpression)owlce : null));
		}
	}
	public final Object Visit(cognipy.cnl.dl.NumberValueRestriction e)
	{
		Object owlpe = null;
		try (context.set(VisitingContext.DataRole))
		{
			owlpe = e.R.accept(this);
			Assert(owlpe != null && owlpe instanceof OWLDataPropertyExpression);
			Object owlb = null;
			owlb = e.B.accept(this);
			Assert(owlb != null);
			if (e.Kind.equals("="))
			{
				return factory.getOWLDataExactCardinality(Integer.parseInt(e.N), owlpe instanceof OWLDataPropertyExpression ? (OWLDataPropertyExpression)owlpe : null, owlb instanceof OWLDataRange ? (OWLDataRange)owlb : null);
			}
			else if (e.Kind.equals("≤") || e.Kind.equals("<"))
			{
				return factory.getOWLDataMaxCardinality(e.Kind.equals("<") ? Integer.parseInt(e.N) - 1 : Integer.parseInt(e.N), owlpe instanceof OWLDataPropertyExpression ? (OWLDataPropertyExpression)owlpe : null, owlb instanceof OWLDataRange ? (OWLDataRange)owlb : null);
			}
			else if (e.Kind.equals("≥") || e.Kind.equals(">"))
			{
				return factory.getOWLDataMinCardinality(e.Kind.equals(">") ? Integer.parseInt(e.N) + 1 : Integer.parseInt(e.N), owlpe instanceof OWLDataPropertyExpression ? (OWLDataPropertyExpression)owlpe : null, owlb instanceof OWLDataRange ? (OWLDataRange)owlb : null);
			}
			else // different
			{
				return factory.getOWLObjectUnionOf(factory.getOWLDataMaxCardinality(Integer.parseInt(e.N) - 1, owlpe instanceof OWLDataPropertyExpression ? (OWLDataPropertyExpression)owlpe : null, owlpe instanceof OWLDataRange ? (OWLDataRange)owlpe : null), factory.getOWLDataMinCardinality(Integer.parseInt(e.N) + 1, owlpe instanceof OWLDataPropertyExpression ? (OWLDataPropertyExpression)owlpe : null, owlpe instanceof OWLDataRange ? (OWLDataRange)owlpe : null));
			}
		}
	}
	public static void Assert(boolean b)
	{
		if (!b)
		{
//C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
//#if DEBUG
			System.Diagnostics.Debugger.Break();
//#endif
			throw new RuntimeException("Conversion Assertion Failed dl->owlapi.");
		}
	}

	///////////////// SWRL ///////////////////////////////////////////
	public final Object Visit(CNL.DL.SwrlStatement e)
	{
		Object tempVar = e.slp.accept(this);
		Object tempVar2 = e.slc.accept(this);
		return factory.getSWRLRule(tempVar instanceof Set ? (Set)tempVar : null, tempVar2 instanceof Set ? (Set)tempVar2 : null, getOWLAnnotationForStatement(e));
	}

	public final Object Visit(CNL.DL.SwrlItemList e)
	{
		Set atoms = new LinkedHashSet();
		for (int i = 0; i < e.list.size(); i++)
		{
			Object x = e.list.get(i).accept(this);
			if (x instanceof SWRLAtom)
			{
				atoms.add(x);
			}
			else if (x instanceof ArrayList<SWRLAtom>)
			{
				for (ArrayList<SWRLAtom> y : x instanceof ArrayList<SWRLAtom> ? (ArrayList<SWRLAtom>)x : null)
				{
					atoms.add(y);
				}
			}
		}

		return atoms;
	}

	public final Object Visit(CNL.DL.SwrlInstance e)
	{
		Object obj = null;
		try (context.set(VisitingContext.Concept))
		{
			if (e.C instanceof CNL.DL.Node)
			{
				obj = (e.C instanceof CNL.DL.Node ? (CNL.DL.Node)e.C : null).accept(this);
				Assert(obj instanceof OWLClassExpression);
				if (obj instanceof OWLClass)
				{
					additionalHotfixDeclarations.add(factory.getOWLSubClassOfAxiom(obj instanceof OWLClass ? (OWLClass)obj : null, factory.getOWLThing()));
				}
			}
		}
		Object arg = e.I.accept(this);
		Assert(arg instanceof SWRLIArgument);
		return factory.getSWRLClassAtom(obj instanceof OWLClassExpression ? (OWLClassExpression)obj : null, arg instanceof SWRLIArgument ? (SWRLIArgument)arg : null);
	}

	public final Object Visit(CNL.DL.SwrlRole e)
	{
		org.semanticweb.owlapi.model.OWLObjectProperty R = factory.getOWLObjectProperty(owlNC.getIRIFromId(e.R, EntityKind.Role));

		Object tempVar = e.I.accept(this);
		SWRLIArgument arg1 = tempVar instanceof SWRLIArgument ? (SWRLIArgument)tempVar : null;
		Object tempVar2 = e.J.accept(this);
		SWRLIArgument arg2 = tempVar2 instanceof SWRLIArgument ? (SWRLIArgument)tempVar2 : null;

		return factory.getSWRLObjectPropertyAtom(R, arg1, arg2);
	}

	public final Object Visit(CNL.DL.SwrlSameAs e)
	{
		Object tempVar = e.I.accept(this);
		SWRLIArgument arg1 = tempVar instanceof SWRLIArgument ? (SWRLIArgument)tempVar : null;
		Object tempVar2 = e.J.accept(this);
		SWRLIArgument arg2 = tempVar2 instanceof SWRLIArgument ? (SWRLIArgument)tempVar2 : null;

		return factory.getSWRLSameIndividualAtom(arg1, arg2);
	}


	public final Object Visit(CNL.DL.SwrlDifferentFrom e)
	{
		Object tempVar = e.I.accept(this);
		SWRLIArgument arg1 = tempVar instanceof SWRLIArgument ? (SWRLIArgument)tempVar : null;
		Object tempVar2 = e.J.accept(this);
		SWRLIArgument arg2 = tempVar2 instanceof SWRLIArgument ? (SWRLIArgument)tempVar2 : null;

		return factory.getSWRLDifferentIndividualsAtom(arg1, arg2);
	}

	public final Object Visit(CNL.DL.SwrlDataProperty e)
	{
		org.semanticweb.owlapi.model.OWLDataProperty R = factory.getOWLDataProperty(owlNC.getIRIFromId(e.R, EntityKind.Role));

		Object tempVar = e.IO.accept(this);
		SWRLIArgument arg1 = tempVar instanceof SWRLIArgument ? (SWRLIArgument)tempVar : null;
		Object tempVar2 = e.DO.accept(this);
		SWRLDArgument arg2 = tempVar2 instanceof SWRLDArgument ? (SWRLDArgument)tempVar2 : null;

		return factory.getSWRLDataPropertyAtom(R, arg1, arg2);
	}

	private void AppendComparator(ArrayList<SWRLAtom> ret, String comparator, ISwrlObject A, ISwrlObject B)
	{
		SWRLBuiltInsVocabulary buitIn = null;
		switch (comparator)
		{
			case "":
				buitIn = SWRLBuiltInsVocabulary.EQUAL;
				break;
			case "=":
				buitIn = SWRLBuiltInsVocabulary.EQUAL;
				break;
			case "≤":
				buitIn = SWRLBuiltInsVocabulary.LESS_THAN_OR_EQUAL;
				break;
			case "≥":
				buitIn = SWRLBuiltInsVocabulary.GREATER_THAN_OR_EQUAL;
				break;
			case "<":
				buitIn = SWRLBuiltInsVocabulary.LESS_THAN;
				break;
			case ">":
				buitIn = SWRLBuiltInsVocabulary.GREATER_THAN;
				break;
			case "≠":
				buitIn = SWRLBuiltInsVocabulary.NOT_EQUAL;
				break;
			default:
				throw new IllegalStateException();
		}
		ArrayList args = new ArrayList();
		Object tempVar = B.accept(this);
		args.add(tempVar instanceof SWRLDArgument ? (SWRLDArgument)tempVar : null);
		Object tempVar2 = A.accept(this);
		args.add(tempVar2 instanceof SWRLDArgument ? (SWRLDArgument)tempVar2 : null);
		ret.add(factory.getSWRLBuiltInAtom(buitIn.getIRI(), args));
	}

	private String mapCode(String code)
	{
		switch (code)
		{
			case "≤":
				return "<=";
			case "≥":
				return ">=";
			case "≠":
				return "<>";
			default:
				return code;
		}
	}

	public final Object Visit(SwrlBuiltIn e)
	{
		String builtInName = e.builtInName;
		String btag = KeyWords.Me.GetTag(mapCode(builtInName));
		ArrayList<SWRLAtom> ret = new ArrayList<SWRLAtom>();
		if (btag.equals("CMP") || btag.equals("EQ"))
		{

			ISwrlObject A = e.Values.get(0);
			ISwrlObject B = e.Values.get(1);
			AppendComparator(ret, e.builtInName, A, B);
		}
		else
		{
			IRI buitIn = null;

			ArrayList lst = new ArrayList();
			lst.add(e.Values.get(e.Values.size() - 1).accept(this));

			for (int i = 0; i < e.Values.size() - 1; i++)
			{
				lst.add(e.Values.get(i).accept(this));
			}

			if (builtInName.equals("plus") || builtInName.equals("times") || builtInName.equals("followed-by"))
			{
				if (builtInName.equals("followed-by"))
				{
					buitIn = SWRLBuiltInsVocabulary.STRING_CONCAT.getIRI();
				}
				else if (builtInName.equals("plus"))
				{
					buitIn = SWRLBuiltInsVocabulary.ADD.getIRI();
				}
				else if (builtInName.equals("times"))
				{
					buitIn = SWRLBuiltInsVocabulary.MULTIPLY.getIRI();
				}
			}
			else if (builtInName.equals("datetime") || builtInName.equals("duration"))
			{
				if (builtInName.equals("datetime"))
				{
					buitIn = SWRLBuiltInsVocabulary.DATE_TIME.getIRI();
				}
				else if (builtInName.equals("duration"))
				{
					buitIn = SWRLBuiltInsVocabulary.DAY_TIME_DURATION.getIRI();
				}
			}
			else if (e.builtInName.equals("alpha-representation-of"))
			{
				buitIn = IRI.create("http://ontorion.com/swrlb#" + "alphaRepresentationOf");
			}
			else if (e.builtInName.equals("annotation"))
			{
				buitIn = IRI.create("http://ontorion.com/swrlb#" + "annotation");
			}
			else if (e.builtInName.equals("execute"))
			{
				buitIn = IRI.create("http://ontorion.com/swrlb#" + "executeExternalFunction");
			}
			else if (builtInName.equals("translated") || builtInName.equals("replaced"))
			{
				if (builtInName.equals("translated"))
				{
					buitIn = SWRLBuiltInsVocabulary.TRANSLATE.getIRI();
				}
				else if (builtInName.equals("replaced"))
				{
					buitIn = SWRLBuiltInsVocabulary.REPLACE.getIRI();
				}
			}
			else if (builtInName.equals("from") || builtInName.equals("before") || builtInName.equals("after"))
			{
				if (e.builtInName.equals("from"))
				{
					buitIn = SWRLBuiltInsVocabulary.SUBSTRING.getIRI();
				}
				else if (e.builtInName.equals("before"))
				{
					buitIn = SWRLBuiltInsVocabulary.SUBSTRING_BEFORE.getIRI();
				}
				else if (e.builtInName.equals("after"))
				{
					buitIn = SWRLBuiltInsVocabulary.SUBSTRING_AFTER.getIRI();
				}
			}
			else if (e.Values.size() == 3)
			{
				if (e.builtInName.equals("minus"))
				{
					buitIn = SWRLBuiltInsVocabulary.SUBTRACT.getIRI();
				}
				else if (e.builtInName.equals("divided-by"))
				{
					buitIn = SWRLBuiltInsVocabulary.DIVIDE.getIRI();
				}
				else if (e.builtInName.equals("integer-divided-by"))
				{
					buitIn = SWRLBuiltInsVocabulary.INTEGER_DIVIDE.getIRI();
				}
				else if (e.builtInName.equals("modulo"))
				{
					buitIn = SWRLBuiltInsVocabulary.MOD.getIRI();
				}
				else if (e.builtInName.equals("raised-to-the-power-of"))
				{
					buitIn = SWRLBuiltInsVocabulary.POW.getIRI();
				}
				else if (e.builtInName.equals("rounded-with-the-precision-of"))
				{
					buitIn = SWRLBuiltInsVocabulary.ROUND_HALF_TO_EVEN.getIRI();
				}
			}
			else if (e.Values.size() == 2)
			{
				if (e.builtInName.equals("not"))
				{
					buitIn = SWRLBuiltInsVocabulary.BOOLEAN_NOT.getIRI();
				}
				else if (e.builtInName.equals("minus"))
				{
					buitIn = SWRLBuiltInsVocabulary.UNARY_MINUS.getIRI();
				}
				else if (e.builtInName.equals("absolute-value-of"))
				{
					buitIn = SWRLBuiltInsVocabulary.ABS.getIRI();
				}
				else if (e.builtInName.equals("ceiling-of"))
				{
					buitIn = SWRLBuiltInsVocabulary.CEILING.getIRI();
				}
				else if (e.builtInName.equals("floor-of"))
				{
					buitIn = SWRLBuiltInsVocabulary.FLOOR.getIRI();
				}
				else if (e.builtInName.equals("round-of"))
				{
					buitIn = SWRLBuiltInsVocabulary.ROUND.getIRI();
				}
				else if (e.builtInName.equals("sine-of"))
				{
					buitIn = SWRLBuiltInsVocabulary.SIN.getIRI();
				}
				else if (e.builtInName.equals("cosine-of"))
				{
					buitIn = SWRLBuiltInsVocabulary.COS.getIRI();
				}
				else if (e.builtInName.equals("tangent-of"))
				{
					buitIn = SWRLBuiltInsVocabulary.TAN.getIRI();
				}
				else if (e.builtInName.equals("case-ignored"))
				{
					buitIn = SWRLBuiltInsVocabulary.STRING_EQUALS_IGNORE_CASE.getIRI();
				}
				else if (e.builtInName.equals("length-of"))
				{
					buitIn = SWRLBuiltInsVocabulary.STRING_LENGTH.getIRI();
				}
				else if (e.builtInName.equals("space-normalized"))
				{
					buitIn = SWRLBuiltInsVocabulary.NORMALIZE_SPACE.getIRI();
				}
				else if (e.builtInName.equals("upper-cased"))
				{
					buitIn = SWRLBuiltInsVocabulary.UPPER_CASE.getIRI();
				}
				else if (e.builtInName.equals("lower-cased"))
				{
					buitIn = SWRLBuiltInsVocabulary.LOWER_CASE.getIRI();
				}
				else if (e.builtInName.equals("contains-string"))
				{
					buitIn = SWRLBuiltInsVocabulary.CONTAINS.getIRI();
				}
				else if (e.builtInName.equals("starts-with-string"))
				{
					buitIn = SWRLBuiltInsVocabulary.STARTS_WITH.getIRI();
				}
				else if (e.builtInName.equals("ends-with-string"))
				{
					buitIn = SWRLBuiltInsVocabulary.ENDS_WITH.getIRI();
				}
				else if (e.builtInName.equals("matches-string"))
				{
					buitIn = SWRLBuiltInsVocabulary.MATCHES.getIRI();
				}
				else if (e.builtInName.equals("contains-case-ignored-string"))
				{
					buitIn = SWRLBuiltInsVocabulary.CONTAINS_IGNORE_CASE.getIRI();
				}
				else if (e.builtInName.equals("sounds-like-string"))
				{
					buitIn = IRI.create("http://ontorion.com/swrlb#" + "soundsLike");
				}
			}

			ret.add(factory.getSWRLBuiltInAtom(buitIn, lst));
		}
		return ret;
	}

	public final Object Visit(CNL.DL.SwrlDataRange e)
	{
		Object tempVar = e.B.accept(this);
		OWLDataRange dr = tempVar instanceof OWLDataRange ? (OWLDataRange)tempVar : null;
		Object tempVar2 = e.DO.accept(this);
		SWRLDArgument arg2 = tempVar2 instanceof SWRLDArgument ? (SWRLDArgument)tempVar2 : null;
		return factory.getSWRLDataRangeAtom(dr, arg2);
	}

	////////// SWRL //////////////////////////////////////////

	public final Object Visit(SwrlIterate e)
	{
		return null;
	}

	public final Object Visit(ExeStatement e)
	{
		return null;
	}

	public final Object Visit(SwrlVarList e)
	{
		Debugger.Break();
		throw new UnsupportedOperationException();
	}


	public final Object Visit(SwrlDVal e)
	{
		return factory.getSWRLLiteralArgument(getLiteralVal(e.Val));
	}

	public final Object Visit(SwrlDVar e)
	{
		return factory.getSWRLVariable(owlNC.getIRIFromId(e.VAR, EntityKind.SWRLVariable));
	}

	public final Object Visit(SwrlIVal e)
	{
		return factory.getSWRLIndividualArgument(GetNamedIndividual(e.I));
	}

	public final Object Visit(SwrlIVar e)
	{
		return factory.getSWRLVariable(owlNC.getIRIFromId(e.VAR, EntityKind.SWRLVariable));
	}


	public final Object Visit(CodeStatement e)
	{
		return null;
	}
}