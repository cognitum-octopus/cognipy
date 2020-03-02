package cognipy.splitting;

import cognipy.cnl.dl.*;
import cognipy.*;
import java.util.*;

public final class DLToys
{
	public static String EncodeToIdentifier(String toEncode)
	{
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: byte[] toEncodeAsBytes = System.Text.Encoding.Unicode.GetBytes(toEncode);
		byte[] toEncodeAsBytes = toEncode.getBytes(java.nio.charset.StandardCharsets.UTF_16LE);
		String returnValue = System.Convert.ToBase64String(toEncodeAsBytes);
		return "\"" + returnValue.replace("/", "-").replace("+", "-") + "\"";
	}

	public static LocalityKind[] LocalityKinds = new cognipy.splitting.LocalityKind[] {LocalityKind.Bottom, LocalityKind.Top};

	private static ThreadLocal<tools.Parser> p = new ThreadLocal<tools.Parser>(() -> new cognipy.cnl.dl.dl());

	public static cognipy.cnl.dl.Paragraph ParseDL(String DL)
	{
		if (DL.trim().equals(""))
		{
			Paragraph tempVar = new Paragraph(null);
			tempVar.Statements = new ArrayList<Statement>();
			return tempVar;
		}

		tools.SYMBOL smb = p.Value.Parse(DL);
		if (smb instanceof cognipy.cnl.dl.Paragraph) // get null on syntax error
		{
			return smb instanceof cognipy.cnl.dl.Paragraph ? (cognipy.cnl.dl.Paragraph)smb : null;
		}
		else
		{
			if (smb instanceof tools.error)
			{
				throw new RuntimeException(smb.toString());
			}
			else
			{
				throw new RuntimeException("Unknown parse exception!");
			}
		}
	}

	public static String MakeScriptFromParagraph(Paragraph ast)
	{
		cognipy.cnl.dl.Serializer ser = new cognipy.cnl.dl.Serializer();
		return ser.Serialize(ast);
	}

	public static String MakeExpressionFromStatement(Statement stmt)
	{
		cognipy.cnl.dl.Serializer ser = new cognipy.cnl.dl.Serializer();
		return ser.Serialize(stmt);
	}

	public static HashSet<String> GetSignatureFromStatement(Statement stmt)
	{
		if (stmt instanceof CodeStatement)
		{
			return new HashSet<String>() {"∀"};
		}
		else
		{
			cognipy.cnl.dl.Serializer ser = new cognipy.cnl.dl.Serializer();
			ser.Serialize(stmt);
			return ser.GetSignature();
		}
	}

	public static HashSet<String> GetSignatureFromParagraph(Paragraph stmt)
	{
		cognipy.cnl.dl.Serializer ser = new cognipy.cnl.dl.Serializer();
		ser.Serialize(stmt);
		return ser.GetSignature();
	}

	/////////////////


	private static class DLLink extends CNL.DL.Node
	{
		public String I, J, R;
		public DLLink(String I, String J, String R)
		{
			super(null);
		this.I = I;
		this.J = J;
		this.R = R;
		}
	}

	private static boolean alreadyExists(ArrayList<Object> lst, Object n)
	{
		if (n instanceof Atomic)
		{
			for (Object l : lst)
			{
				if (l instanceof Atomic)
				{
					return (l instanceof Atomic ? (Atomic)l : null).id.equals((n instanceof Atomic ? (Atomic)n : null).id);
				}
			}
		}
		return false;
	}

	private static Tuple<String, Object> getSimpleDLFormOfSwrlItem(SwrlItem item)
	{
		{
			SwrlInstance conc = item instanceof SwrlInstance ? (SwrlInstance)item : null;
			if (conc != null)
			{
				SwrlIVar V = conc.I instanceof SwrlIVar ? (SwrlIVar)conc.I : null;
				if (V != null)
				{
					return Tuple.Create(V.VAR, (Object)conc.C);
				}
			}
		}
		{
			SwrlRole conc = item instanceof SwrlRole ? (SwrlRole)item : null;
			if (conc != null)
			{
				SwrlIVar V1 = conc.I instanceof SwrlIVar ? (SwrlIVar)conc.I : null;
				SwrlIVar V2 = conc.J instanceof SwrlIVar ? (SwrlIVar)conc.J : null;
				if (V1 != null)
				{
					if (V2 != null)
					{
						return Tuple.Create((String)null, (Object)new DLLink(V1.VAR, V2.VAR, conc.R));
					}
					else
					{
						SwrlIVal L2 = conc.J instanceof SwrlIVal ? (SwrlIVal)conc.J : null;
						if (L2 != null)
						{
							CNL.DL.Atomic tempVar = new CNL.DL.Atomic(null);
							tempVar.id = conc.R;
							CNL.DL.InstanceSet tempVar2 = new CNL.DL.InstanceSet(null);
							CNL.DL.NamedInstance tempVar3 = new CNL.DL.NamedInstance(null);
							tempVar3.name = L2.I;
							tempVar2.Instances = new ArrayList<Instance>(Arrays.asList(tempVar3));
							return Tuple.Create(V1.VAR, (Object)new CNL.DL.SomeRestriction(null, tempVar, tempVar2));
						}
					}
				}
				if (V2 != null)
				{
					SwrlIVal L1 = conc.I instanceof SwrlIVal ? (SwrlIVal)conc.I : null;
					if (L1 != null)
					{
						CNL.DL.Atomic tempVar5 = new CNL.DL.Atomic(null);
						CNL.DL.NamedInstance tempVar6 = new CNL.DL.NamedInstance(null);
						tempVar6.name = L1.I;
						tempVar4.Instances = new ArrayList<Instance>(Arrays.asList(tempVar6));
						return Tuple.Create(V2.VAR, (Object)new CNL.DL.SomeRestriction(null, tempVar4));
						tempVar5.id = conc.R;
						CNL.DL.Atomic tempVar7 = new CNL.DL.Atomic(null);
						tempVar7.id = conc.R;
						CNL.DL.RoleInversion(null, tempVar7), new CNL.DL.InstanceSet tempVar4 = new CNL.DL.RoleInversion(null, tempVar5), new CNL.DL.InstanceSet(null);
					}
				}
			}
		}
		{
			SwrlDataProperty conc = item instanceof SwrlDataProperty ? (SwrlDataProperty)item : null;
			if (conc != null)
			{
				SwrlIVar V = conc.IO instanceof SwrlIVar ? (SwrlIVar)conc.IO : null;
				SwrlDVal D = conc.DO instanceof SwrlDVal ? (SwrlDVal)conc.DO : null;
				if (V != null && D != null)
				{
					CNL.DL.Atomic tempVar8 = new CNL.DL.Atomic(null);
					tempVar8.id = conc.R;
					CNL.DL.ValueSet tempVar9 = new CNL.DL.ValueSet(null);
					tempVar9.Values = new ArrayList<Value>(Arrays.asList(D.Val));
					return Tuple.Create(V.VAR, (Object)new CNL.DL.SomeValueRestriction(null, tempVar8, tempVar9));
				}
				SwrlDVar V2 = conc.DO instanceof SwrlDVar ? (SwrlDVar)conc.DO : null;
				if (V != null && V2 != null)
				{
					return Tuple.Create((String)null, (Object)new DLLink(V.VAR, V2.VAR, conc.R));
				}
			}
		}
		{
			SwrlDataRange conc = item instanceof SwrlDataRange ? (SwrlDataRange)item : null;
			if (conc != null)
			{
				SwrlDVar V = conc.DO instanceof SwrlDVar ? (SwrlDVar)conc.DO : null;
				if (V != null)
				{
					return Tuple.Create(V.VAR, (Object)conc.B);
				}
			}
		}

		return null;
	}


	public static Statement TransformSwrlToDL(SwrlStatement e)
	{
		if (e.slc.list.size() == 1)
		{
			Tuple<String, Object> conc = getSimpleDLFormOfSwrlItem(e.slc.list.get(0));
			if (conc != null && conc.Item1 != null && conc.Item2 instanceof Node)
			{
				HashMap<String, ArrayList<Object>> nodes = new HashMap<String, ArrayList<Object>>();
				HashMap<String, HashMap<String, HashSet<String>>> links = new HashMap<String, HashMap<String, HashSet<String>>>();
				HashMap<String, HashMap<String, HashSet<String>>> invlinks = new HashMap<String, HashMap<String, HashSet<String>>>();
				HashSet<Tuple<String, String, String>> pendinglinks = new HashSet<Tuple<String, String, String>>();
				for (SwrlItem p : e.slp.list)
				{
					Tuple<String, Object> pred = getSimpleDLFormOfSwrlItem(p);
					if (pred == null)
					{
						return e;
					}
					if (pred.Item2 instanceof DLLink)
					{
						T2 tempVar = pred.Item2;
						DLLink l = tempVar instanceof DLLink ? (DLLink)tempVar : null;
						if (!links.containsKey(l.I))
						{
							links.put(l.I, new HashMap<String, HashSet<String>>());
						}
						if (!links.get(l.I).containsKey(l.J))
						{
							links.get(l.I).put(l.J, new HashSet<String>());
						}
						links.get(l.I).get(l.J).add(l.R);
						pendinglinks.add(Tuple.Create(l.I, l.J, l.R));
						if (!invlinks.containsKey(l.J))
						{
							invlinks.put(l.J, new HashMap<String, HashSet<String>>());
						}
						if (!invlinks.get(l.J).containsKey(l.I))
						{
							invlinks.get(l.J).put(l.I, new HashSet<String>());
						}
						invlinks.get(l.J).get(l.I).add(l.R);
					}
					else
					{
						if (!nodes.containsKey(pred.Item1))
						{
							nodes.put(pred.Item1, new ArrayList<Object>());
						}
						if (!alreadyExists(nodes.get(pred.Item1), pred.Item2))
						{
							nodes.get(pred.Item1).add(pred.Item2);
						}
					}
				}

				tangible.Func1Param<String, ArrayList<Node>> act = (String arg) -> null.invoke(arg);

				HashSet<String> alreadyDone = new HashSet<String>();

				act = (String arg) ->
				{
						alreadyDone.add(xname);
						ArrayList<Node> torkn = new ArrayList<Node>();

						for (Map.Entry<String, ArrayList<Object>> kv : nodes.entrySet())
						{
							if (kv.getKey() == xname)
							{
								ArrayList<Node> r = cognipy.splitting.DLToys.<Node>getAllAs(kv.getValue());
								if (r == null)
								{
									return null;
								}
								torkn.addAll(r);
							}
							else
							{
								if (links.containsKey(xname) && links.get(xname).containsKey(kv.getKey()))
								{
									if (links.get(xname).get(kv.getKey()).size() > 1)
									{
										return null;
									}
									var R = links.get(xname).get(kv.getKey()).First();
									pendinglinks.remove(Tuple.Create(xname, kv.getKey(), R));
									if (nodes.get(kv.getKey()).get(0) instanceof Node)
									{
										ArrayList<Node> r = cognipy.splitting.DLToys.<Node>getAllAs(nodes.get(kv.getKey()));
										if (r == null)
										{
											return null;
										}
										if (!alreadyDone.contains(kv.getKey()))
										{
											var cc = act.invoke(kv.getKey());
											if (cc == null)
											{
												return null;
											}
											r.addAll(cc);
										}
										CNL.DL.Atomic tempVar2 = new CNL.DL.Atomic(null);
										tempVar2.id = R;
										CNL.DL.ConceptAnd tempVar3 = new CNL.DL.ConceptAnd(null);
										tempVar3.Exprs = r;
										torkn.add(new CNL.DL.SomeRestriction(null, tempVar2, tempVar3));
									}
									else
									{
										ArrayList<AbstractBound> r = cognipy.splitting.DLToys.<AbstractBound>getAllAs(nodes.get(kv.getKey()));
										if (r == null)
										{
											return null;
										}
										CNL.DL.Atomic tempVar4 = new CNL.DL.Atomic(null);
										tempVar4.id = R;
										CNL.DL.BoundAnd tempVar5 = new CNL.DL.BoundAnd(null);
										tempVar5.List = r;
										torkn.add(new CNL.DL.SomeValueRestriction(null, tempVar4, tempVar5));
									}
								}
								else if (invlinks.containsKey(xname) && invlinks.get(xname).containsKey(kv.getKey()))
								{
									if (invlinks.get(xname).get(kv.getKey()).size() > 1)
									{
										return null;
									}
									var R = invlinks.get(xname).get(kv.getKey()).First();
									pendinglinks.remove(Tuple.Create(xname, kv.getKey(), R));
									if (nodes.get(kv.getKey()).get(0) instanceof Node)
									{
										ArrayList<Node> r = cognipy.splitting.DLToys.<Node>getAllAs(nodes.get(kv.getKey()));
										if (r == null)
										{
											return null;
										}
										if (!alreadyDone.contains(kv.getKey()))
										{
											var cc = act.invoke(kv.getKey());
											if (cc == null)
											{
												return null;
											}
											r.addAll(cc);
										}
										CNL.DL.Atomic tempVar7 = new CNL.DL.Atomic(null);
										tempVar6.Exprs = r;
										torkn.add(new CNL.DL.SomeRestriction(null, tempVar6));
										tempVar7.id = R;
										CNL.DL.Atomic tempVar8 = new CNL.DL.Atomic(null);
										tempVar8.id = R;
										CNL.DL.RoleInversion(null, tempVar8), new CNL.DL.ConceptAnd tempVar6 = new CNL.DL.RoleInversion(null, tempVar7), new CNL.DL.ConceptAnd(null);
									}
									else
									{
										return null;
									}
								}
								else
								{
									return null;
								}
							}
						}

						return torkn;
				};

//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
				var rr = act.invoke(conc.Item1);
				if (rr == null)
				{
					return e;
				}

				if (!pendinglinks.isEmpty())
				{
					return e;
				}

				CNL.DL.ConceptAnd tempVar9 = new CNL.DL.ConceptAnd(null);
				tempVar9.Exprs = rr;
				return new CNL.DL.Subsumption(null, tempVar9, (Node)conc.Item2, Statement.Modality.IS);
			}
		}
		return e;
	}
	private static <T> ArrayList<T> getAllAs(ArrayList<Object> lst)
	{
		ArrayList<T> ret = new ArrayList<T>();
		for (Object o : lst)
		{
			if (o instanceof T)
			{
				ret.add((T)o);
			}
			else
			{
				return null;
			}
		}
		return ret;
	}


	/////////////////////


	/**Locality
	*/
	private static class LXComporer implements Comparator<ArrayList<String>>
	{
		public final int compare(ArrayList<String> lx, ArrayList<String> ly)
		{
			int ms = lx.size() < ly.size() ? lx.size() : ly.size();
			Iterator<String> lxEn = lx.iterator();
			Iterator<String> lyEn = ly.iterator();
//C# TO JAVA CONVERTER TODO TASK: .NET iterators are only converted within the context of 'while' and 'for' loops:
			lxEn.MoveNext();
//C# TO JAVA CONVERTER TODO TASK: .NET iterators are only converted within the context of 'while' and 'for' loops:
			lyEn.MoveNext();
			for (int i = 0; i < ms; i++)
			{
//C# TO JAVA CONVERTER TODO TASK: .NET iterators are only converted within the context of 'while' and 'for' loops:
				if (lxEn.Current.CompareTo(lyEn.Current) < 0)
				{
					return -1;
				}
//C# TO JAVA CONVERTER TODO TASK: .NET iterators are only converted within the context of 'while' and 'for' loops:
				else if (lxEn.Current.CompareTo(lyEn.Current) > 0)
				{
					return 1;
				}
//C# TO JAVA CONVERTER TODO TASK: .NET iterators are only converted within the context of 'while' and 'for' loops:
				lxEn.MoveNext();
//C# TO JAVA CONVERTER TODO TASK: .NET iterators are only converted within the context of 'while' and 'for' loops:
				lyEn.MoveNext();
			}
			if (lx.size() < ly.size())
			{
				return -1;
			}
			else if (lx.size() > ly.size())
			{
				return 1;
			}
			else
			{
				return 0;
			}
		}
	}

	private static ArrayList<ArrayList<String>> CreateSimple(String s)
	{
		ArrayList<ArrayList<String>> ret = new ArrayList<ArrayList<String>>();
		ret.add(new ArrayList<String>());
		ret.get(0).add(s);
		return ret;
	}

	private static ArrayList<ArrayList<String>> CreateNull()
	{
		ArrayList<ArrayList<String>> ret = new ArrayList<ArrayList<String>>();
		return ret;
	}

	public static boolean IsAny(ArrayList<ArrayList<String>> s)
	{
		if (s.size() == 1)
		{
			if (s.get(0).size() == 1)
			{
				return s.get(0).get(0).equals("∀");
			}
		}
		return false;
	}

	private static ArrayList<ArrayList<String>> Cumulate(ArrayList<ArrayList<String>> a, ArrayList<ArrayList<String>> b)
	{
		if (IsAny(a))
		{
			return a;
		}
		if (IsAny(b))
		{
			return b;
		}
		LXComporer cmp = new LXComporer();
		ArrayList<ArrayList<String>> ret = new ArrayList<ArrayList<String>>();
		for (ArrayList<String> l : b)
		{
			if (a.BinarySearch(l, cmp) < 0)
			{
				ret.add(l);
			}
		}
		ret.addAll(a);
		Collections.sort(ret, cmp);
		return ret;
	}

	private static ArrayList<ArrayList<String>> Intersect(ArrayList<ArrayList<String>> a, ArrayList<ArrayList<String>> b)
	{
		if (IsAny(a))
		{
			return b;
		}
		if (IsAny(b))
		{
			return a;
		}
		LXComporer cmp = new LXComporer();
		ArrayList<ArrayList<String>> ret = new ArrayList<ArrayList<String>>();
		for (ArrayList<String> k : a)
		{
			for (ArrayList<String> l : b)
			{
				ArrayList<String> inter = new ArrayList<String>();
				for (String x : l)
				{
					if (k.BinarySearch(x) < 0)
					{
						inter.add(x);
					}
				}
				inter.addAll(k);
//C# TO JAVA CONVERTER TODO TASK: This version of the List.Sort method is not converted to Java:
				inter.Sort();
				if (ret.BinarySearch(inter, cmp) < 0)
				{
					ret.add(inter);
				}
			}
		}
		Collections.sort(ret, cmp);
		return ret;
	}

	private static boolean isLocalInstanceName(String name)
	{
		return name.startsWith("_");
	}

	private static ArrayList<ArrayList<String>> AnalizeLocality(tools.SYMBOL stmt, LocalityKind lockind, tangible.OutObject<Boolean> ret)
	{
		ret.argValue = true;
		if (stmt instanceof Subsumption)
		{
			if ((stmt instanceof Subsumption ? (Subsumption)stmt : null).modality == Statement.Modality.IS)
			{
				//Subsumption of concepts
				if (lockind == LocalityKind.Bottom)
				{
					//functional role
					if ((stmt instanceof Subsumption ? (Subsumption)stmt : null).C instanceof Top)
					{
						if ((stmt instanceof Subsumption ? (Subsumption)stmt : null).D instanceof NumberRestriction)
						{
							NumberRestriction restr = (stmt instanceof Subsumption ? (Subsumption)stmt : null).D instanceof NumberRestriction ? (NumberRestriction)(stmt instanceof Subsumption ? (Subsumption)stmt : null).D : null;
							if ((restr.C instanceof Top) && ((restr.Kind.equals("≤") && Integer.parseInt(restr.N) == 1) || (restr.Kind.equals("<") && Integer.parseInt(restr.N) == 2)))
							{
								return AnalizeConBottom(restr.R, lockind, "R");
							}
						}
					}
				}

				return Cumulate(AnalizeConBottom((stmt instanceof Subsumption ? (Subsumption)stmt : null).C, lockind, "C"), AnalizeConTop((stmt instanceof Subsumption ? (Subsumption)stmt : null).D, lockind, "C"));
			}
		}
		else if (stmt instanceof RoleInclusion)
		{
			if (lockind == LocalityKind.Bottom)
			{
				return AnalizeConBottom((stmt instanceof RoleInclusion ? (RoleInclusion)stmt : null).C, lockind, "R");
			}
			else
			{
				return AnalizeConTop((stmt instanceof RoleInclusion ? (RoleInclusion)stmt : null).D, lockind, "R");
			}
		}
		else if (stmt instanceof ComplexRoleInclusion)
		{
			//ComplexRoleInclusion
			if (lockind == LocalityKind.Bottom)
			{
				ArrayList<ArrayList<String>> r = CreateNull();
				for (Node S : (stmt instanceof ComplexRoleInclusion ? (ComplexRoleInclusion)stmt : null).RoleChain)
				{
					r = Cumulate(r, AnalizeConBottom(S, lockind, "R"));
				}
				return r;
			}
			else
			{
				return AnalizeConTop((stmt instanceof ComplexRoleInclusion ? (ComplexRoleInclusion)stmt : null).R, lockind, "R");
			}
		}
		else if (stmt instanceof Equivalence)
		{
			if ((stmt instanceof Equivalence ? (Equivalence)stmt : null).modality == Statement.Modality.IS)
			{
				//Equivalence of concepts
				ArrayList<ArrayList<String>> frst = null;
				Node X = (stmt instanceof Equivalence ? (Equivalence)stmt : null).Equivalents.get(0);
				for (int i = 1; i < (stmt instanceof Equivalence ? (Equivalence)stmt : null).Equivalents.size(); i++)
				{
					Node Y = (stmt instanceof Equivalence ? (Equivalence)stmt : null).Equivalents.get(i);
					ArrayList<ArrayList<String>> n = Cumulate(AnalizeConBottom(X, lockind, "C"), AnalizeConTop(Y, lockind, "C"));
					ArrayList<ArrayList<String>> m = Cumulate(AnalizeConBottom(Y, lockind, "C"), AnalizeConTop(X, lockind, "C"));
					frst = frst == null ? Intersect(n, m) : Intersect(frst, Intersect(n, m));
				}
				return frst;
			}
		}
		else if (stmt instanceof RoleEquivalence)
		{
			//Equivalence of roles
			if (lockind == LocalityKind.Bottom)
			{
				ArrayList<ArrayList<String>> frst = null;
				Node X = (stmt instanceof RoleEquivalence ? (RoleEquivalence)stmt : null).Equivalents.get(0);
				for (int i = 1; i < (stmt instanceof RoleEquivalence ? (RoleEquivalence)stmt : null).Equivalents.size(); i++)
				{
					Node Y = (stmt instanceof RoleEquivalence ? (RoleEquivalence)stmt : null).Equivalents.get(i);
					ArrayList<ArrayList<String>> n = Intersect(AnalizeConBottom(X, lockind, "R"), AnalizeConBottom(Y, lockind, "R"));
					frst = frst == null ? n : Intersect(frst, n);
				}
				return frst;
			}
			else
			{
				ArrayList<ArrayList<String>> frst = null;
				Node X = (stmt instanceof RoleEquivalence ? (RoleEquivalence)stmt : null).Equivalents.get(0);
				for (int i = 1; i < (stmt instanceof RoleEquivalence ? (RoleEquivalence)stmt : null).Equivalents.size(); i++)
				{
					Node Y = (stmt instanceof RoleEquivalence ? (RoleEquivalence)stmt : null).Equivalents.get(i);
					ArrayList<ArrayList<String>> n = Intersect(AnalizeConTop(X, lockind, "R"), AnalizeConTop(Y, lockind, "R"));
					frst = frst == null ? n : Intersect(frst, n);
				}
				return frst;
			}
		}
		else if (stmt instanceof InstanceOf)
		{
			if ((stmt instanceof InstanceOf ? (InstanceOf)stmt : null).modality == Statement.Modality.IS && ((stmt instanceof InstanceOf ? (InstanceOf)stmt : null).I instanceof NamedInstance))
			{
				//InstanceOf
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
				var n = ((stmt instanceof InstanceOf ? (InstanceOf)stmt : null).I instanceof NamedInstance ? (NamedInstance)(stmt instanceof InstanceOf ? (InstanceOf)stmt : null).I : null).name;
				if (isLocalInstanceName(n))
				{
					return Cumulate(CreateSimple("I" + ":" + n), AnalizeConTop((stmt instanceof InstanceOf ? (InstanceOf)stmt : null).C, lockind, "C"));
				}
				else
				{
					return AnalizeConTop((stmt instanceof InstanceOf ? (InstanceOf)stmt : null).C, lockind, "C");
				}
			}
		}
		else if (stmt instanceof InstanceValue)
		{
			if ((stmt instanceof InstanceValue ? (InstanceValue)stmt : null).modality == Statement.Modality.IS && ((stmt instanceof InstanceValue ? (InstanceValue)stmt : null).I instanceof NamedInstance))
			{
				//InstanceValue
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
				var n = ((stmt instanceof InstanceValue ? (InstanceValue)stmt : null).I instanceof NamedInstance ? (NamedInstance)(stmt instanceof InstanceValue ? (InstanceValue)stmt : null).I : null).name;
				if (isLocalInstanceName(n))
				{
					ArrayList<ArrayList<String>> inter = lockind == LocalityKind.Top ? AnalizeConTop((stmt instanceof InstanceValue ? (InstanceValue)stmt : null).R, lockind, "D") : CreateNull();
					return Cumulate(CreateSimple("I" + ":" + n), inter);
				}
				else
				{
					if (lockind == LocalityKind.Top)
					{
						return AnalizeConTop((stmt instanceof InstanceValue ? (InstanceValue)stmt : null).R, lockind, "D");
					}
				}
			}
		}
		else if (stmt instanceof RelatedInstances)
		{
			if ((stmt instanceof RelatedInstances ? (RelatedInstances)stmt : null).modality == Statement.Modality.IS && ((stmt instanceof RelatedInstances ? (RelatedInstances)stmt : null).I instanceof NamedInstance) && ((stmt instanceof RelatedInstances ? (RelatedInstances)stmt : null).J instanceof NamedInstance))
			{
				//Related Instances
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
				var n = ((stmt instanceof RelatedInstances ? (RelatedInstances)stmt : null).I instanceof NamedInstance ? (NamedInstance)(stmt instanceof RelatedInstances ? (RelatedInstances)stmt : null).I : null).name;
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
				var m = ((stmt instanceof RelatedInstances ? (RelatedInstances)stmt : null).J instanceof NamedInstance ? (NamedInstance)(stmt instanceof RelatedInstances ? (RelatedInstances)stmt : null).J : null).name;
				if (isLocalInstanceName(n) && isLocalInstanceName(m))
				{
					ArrayList<ArrayList<String>> A;
					ArrayList<ArrayList<String>> B;
					{
						ArrayList<ArrayList<String>> inter = lockind == LocalityKind.Top ? Intersect(AnalizeConTop((stmt instanceof RelatedInstances ? (RelatedInstances)stmt : null).R, lockind, "R"), CreateSimple("I" + ":" + m)) : CreateNull();
						A = Cumulate(CreateSimple("I" + ":" + n), inter);
					}
					{
						ArrayList<ArrayList<String>> inter = lockind == LocalityKind.Top ? Intersect(AnalizeConTop((stmt instanceof RelatedInstances ? (RelatedInstances)stmt : null).R, lockind, "R"), CreateSimple("I" + ":" + n)) : CreateNull();
						B = Cumulate(CreateSimple("I" + ":" + m), inter);
					}
					return Intersect(A, B);
				}
				else
				{
					if (lockind == LocalityKind.Top)
					{
						return AnalizeConTop((stmt instanceof RelatedInstances ? (RelatedInstances)stmt : null).R, lockind, "R");
					}
				}
			}
		}
		ret.argValue = false;
		return CreateNull();
	}

	private static ArrayList<ArrayList<String>> AnalizeConBottom(tools.SYMBOL C, LocalityKind lockind, String kind)
	{
		if (C instanceof Bottom)
		{
			return CreateSimple("∀");
		}
		else if (C instanceof InstanceSet)
		{
			if ((C instanceof InstanceSet ? (InstanceSet)C : null).Instances.isEmpty())
			{
				return CreateSimple("∀");
			}
		}
		else if (C instanceof Atomic)
		{
			if (lockind == LocalityKind.Bottom)
			{
				return CreateSimple(kind + ":" + (C instanceof Atomic ? (Atomic)C : null).id);
			}
		}
		else if (C instanceof RoleInversion)
		{
			return AnalizeConBottom((C instanceof RoleInversion ? (RoleInversion)C : null).R, lockind, "R");
		}
		else if (C instanceof ConceptNot)
		{
			return AnalizeConTop((C instanceof ConceptNot ? (ConceptNot)C : null).C, lockind, "C");
		}
		else if (C instanceof ConceptAnd)
		{
			ArrayList<ArrayList<String>> frst = null;
			for (Node X : (C instanceof ConceptAnd ? (ConceptAnd)C : null).Exprs)
			{
				frst = frst == null ? AnalizeConBottom(X, lockind, "C") : Cumulate(frst, AnalizeConBottom(X, lockind, "C"));
			}
			return frst;
		}
		else if (C instanceof ConceptOr)
		{
			ArrayList<ArrayList<String>> frst = null;
			for (Node X : (C instanceof ConceptOr ? (ConceptOr)C : null).Exprs)
			{
				frst = frst == null ? AnalizeConBottom(X, lockind, "C") : Intersect(frst, AnalizeConBottom(X, lockind, "C"));
			}
			return frst;
		}
		else if (C instanceof SomeRestriction)
		{
			if (lockind == LocalityKind.Bottom)
			{
				return Cumulate(AnalizeConBottom((C instanceof SomeRestriction ? (SomeRestriction)C : null).R, lockind, "R"), AnalizeConBottom((C instanceof SomeRestriction ? (SomeRestriction)C : null).C, lockind, "C"));
			}
			else
			{
				return AnalizeConBottom((C instanceof SomeRestriction ? (SomeRestriction)C : null).C, lockind, "C");
			}
		}
		else if (C instanceof OnlyRestriction)
		{
			if (lockind == LocalityKind.Top)
			{
				return Intersect(AnalizeConBottom((C instanceof OnlyRestriction ? (OnlyRestriction)C : null).R, lockind, "R"), AnalizeConTop((C instanceof OnlyRestriction ? (OnlyRestriction)C : null).C, lockind, "C"));
			}
		}
		else if (C instanceof SelfReference)
		{
			if (lockind == LocalityKind.Bottom)
			{
				return AnalizeConBottom((C instanceof SelfReference ? (SelfReference)C : null).R, lockind, "R");
			}
		}
		else if (C instanceof NumberRestriction)
		{
			if (lockind == LocalityKind.Bottom)
			{
				if ((C instanceof NumberRestriction ? (NumberRestriction)C : null).Kind.equals("≥") || (C instanceof NumberRestriction ? (NumberRestriction)C : null).Kind.equals(">"))
				{
					return Cumulate(AnalizeConBottom((C instanceof NumberRestriction ? (NumberRestriction)C : null).R, lockind, "R"), AnalizeConBottom((C instanceof NumberRestriction ? (NumberRestriction)C : null).C, lockind, "C"));
				}
			}
			else
			{
				if ((C instanceof NumberRestriction ? (NumberRestriction)C : null).Kind.equals("≥") || (C instanceof NumberRestriction ? (NumberRestriction)C : null).Kind.equals(">"))
				{
					return AnalizeConBottom((C instanceof NumberRestriction ? (NumberRestriction)C : null).C, lockind, "C");
				}
				else if ((C instanceof NumberRestriction ? (NumberRestriction)C : null).Kind.equals("≤") || (C instanceof NumberRestriction ? (NumberRestriction)C : null).Kind.equals("<"))
				{
					return Intersect(AnalizeConTop((C instanceof NumberRestriction ? (NumberRestriction)C : null).R, lockind, "R"), AnalizeConTop((C instanceof NumberRestriction ? (NumberRestriction)C : null).C, lockind, "C"));
				}
				else
				{
					return Intersect(AnalizeConBottom((C instanceof NumberRestriction ? (NumberRestriction)C : null).C, lockind, "C"), Intersect(AnalizeConTop((C instanceof NumberRestriction ? (NumberRestriction)C : null).R, lockind, "R"), AnalizeConTop((C instanceof NumberRestriction ? (NumberRestriction)C : null).C, lockind, "C")));
				}
			}
		}
		return CreateNull();
	}

	private static ArrayList<ArrayList<String>> AnalizeConTop(tools.SYMBOL C, LocalityKind lockind, String kind)
	{
		if (C instanceof Top)
		{
			return CreateSimple("∀");
		}
		else if (C instanceof Atomic)
		{
			if (lockind == LocalityKind.Top)
			{
				return CreateSimple(kind + ":" + (C instanceof Atomic ? (Atomic)C : null).id);
			}
		}
		else if (C instanceof ConceptNot)
		{
			return AnalizeConBottom((C instanceof ConceptNot ? (ConceptNot)C : null).C, lockind, "C");
		}
		else if (C instanceof ConceptAnd)
		{
			ArrayList<ArrayList<String>> frst = null;
			for (Node X : (C instanceof ConceptAnd ? (ConceptAnd)C : null).Exprs)
			{
				frst = frst == null ? AnalizeConTop(X, lockind, "C") : Intersect(frst, AnalizeConTop(X, lockind, "C"));
			}
			return frst;
		}
		else if (C instanceof ConceptOr)
		{
			ArrayList<ArrayList<String>> frst = null;
			for (Node X : (C instanceof ConceptOr ? (ConceptOr)C : null).Exprs)
			{
				frst = frst == null ? AnalizeConTop(X, lockind, "C") : Cumulate(frst, AnalizeConTop(X, lockind, "C"));
			}
			return frst;
		}
		else if (C instanceof SomeRestriction)
		{
			if (lockind == LocalityKind.Top)
			{
				if ((C instanceof SomeRestriction ? (SomeRestriction)C : null).C instanceof InstanceSet)
				{
					if (((C instanceof SomeRestriction ? (SomeRestriction)C : null).C instanceof InstanceSet ? (InstanceSet)(C instanceof SomeRestriction ? (SomeRestriction)C : null).C : null).Instances.Count() > 0)
					{
						return AnalizeConTop((C instanceof SomeRestriction ? (SomeRestriction)C : null).R, lockind, "R");
					}
				}
				else
				{
					return Intersect(AnalizeConTop((C instanceof SomeRestriction ? (SomeRestriction)C : null).R, lockind, "R"), AnalizeConTop((C instanceof SomeRestriction ? (SomeRestriction)C : null).C, lockind, "C"));
				}
			}
		}
		else if (C instanceof SelfReference)
		{
			if (lockind == LocalityKind.Top)
			{
				return AnalizeConTop((C instanceof SelfReference ? (SelfReference)C : null).R, lockind, "R");
			}
		}
		else if (C instanceof OnlyRestriction)
		{
			if (lockind == LocalityKind.Bottom)
			{
				return Cumulate(AnalizeConBottom((C instanceof OnlyRestriction ? (OnlyRestriction)C : null).R, lockind, "R"), AnalizeConTop((C instanceof OnlyRestriction ? (OnlyRestriction)C : null).C, lockind, "C"));
			}
			else
			{
				return AnalizeConTop((C instanceof OnlyRestriction ? (OnlyRestriction)C : null).C, lockind, "C");
			}
		}
		else if (C instanceof NumberRestriction)
		{
			if (lockind == LocalityKind.Bottom)
			{
				if ((C instanceof NumberRestriction ? (NumberRestriction)C : null).Kind.equals("≤") || (C instanceof NumberRestriction ? (NumberRestriction)C : null).Kind.equals("<"))
				{
					return Cumulate(AnalizeConBottom((C instanceof NumberRestriction ? (NumberRestriction)C : null).R, lockind, "R"), AnalizeConBottom((C instanceof NumberRestriction ? (NumberRestriction)C : null).C, lockind, "C"));
				}
			}
			else
			{
				if ((C instanceof NumberRestriction ? (NumberRestriction)C : null).Kind.equals("≤") || (C instanceof NumberRestriction ? (NumberRestriction)C : null).Kind.equals("<"))
				{
					return AnalizeConBottom((C instanceof NumberRestriction ? (NumberRestriction)C : null).C, lockind, "C");
				}
				else if ((C instanceof NumberRestriction ? (NumberRestriction)C : null).Kind.equals("≥") || (C instanceof NumberRestriction ? (NumberRestriction)C : null).Kind.equals(">"))
				{
					return Intersect(AnalizeConTop((C instanceof NumberRestriction ? (NumberRestriction)C : null).R, lockind, "R"), AnalizeConTop((C instanceof NumberRestriction ? (NumberRestriction)C : null).C, lockind, "C"));
				}
				else
				{
					return Cumulate(AnalizeConBottom((C instanceof NumberRestriction ? (NumberRestriction)C : null).C, lockind, "C"), Intersect(AnalizeConTop((C instanceof NumberRestriction ? (NumberRestriction)C : null).R, lockind, "R"), AnalizeConTop((C instanceof NumberRestriction ? (NumberRestriction)C : null).C, lockind, "C")));
				}
			}
		}
		return CreateNull();
	}

}