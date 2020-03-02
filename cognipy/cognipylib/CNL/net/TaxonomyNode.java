package cnl.net;

import cognipy.collections.*;
import java.util.*;

public abstract class TaxonomyNode implements INotifyPropertyChanged, java.lang.Comparable
{
	public tangible.Event<PropertyChangedEventHandler> PropertyChanged = new tangible.Event<PropertyChangedEventHandler>();
	private IInvokableProvider invokableProvider;
	public final int compareTo(Object obj)
	{
		if (obj instanceof TaxonomyNode)
		{
			return getENText().compareTo((obj instanceof TaxonomyNode ? (TaxonomyNode)obj : null).getENText());
		}
		else
		{
			return 0;
		}
	}

	protected TaxonomyNode(IInvokableProvider invokableProvider)
	{
		this.invokableProvider = invokableProvider;
		this.children = new SortedObservableCollection<TaxonomyNode>(invokableProvider);

	}

	private UUID _uniqueId = UUID.NewGuid();
	public final UUID getID()
	{
		return _uniqueId;
	}

	private TaxonomyNode _parentNode = null;
	public final TaxonomyNode getParent()
	{
		return _parentNode;
	}
	public final void setParent(TaxonomyNode value)
	{
		_parentNode = value;
	}
	private boolean _isOntorionMode = false;
	public final boolean getIsInOntorionModule()
	{
		return _isOntorionMode;
	}
	public final void setIsInOntorionModule(boolean value)
	{
		_isOntorionMode = value;
		notifyPropertyChanged("IsInOntorionModule");

	}
	private boolean _isExpanded = false;
	public final void setIsExpandedNoPropertyChanged(boolean value)
	{
		_isExpanded = value;
	}

	public final boolean getIsExpanded()
	{
		return _isExpanded;
	}
	public final void setIsExpanded(boolean value)
	{
		_isExpanded = value;
		notifyPropertyChanged("IsExpanded");

	}

	public final void SetupChild(TaxonomyNode node)
	{
		if (!node.getENText().equals("") || node.getMyType() == NodeType.FictiousChild)
		{
			node.SetInvokableProvider(invokableProvider);
			getChildren().add(node);
		}
	}

	private void SetInvokableProvider(IInvokableProvider invokableProvider2)
	{
		this.invokableProvider = invokableProvider2;
		getChildren().SetInvokableProvider(invokableProvider2);
		for (TaxonomyNode c : getChildren())
		{
			c.SetInvokableProvider(invokableProvider2);
		}
	}

	public final void RemoveChild(TaxonomyNode node)
	{
		if (this.getChildren().contains(node))
		{
			getChildren().remove(node);
		}
	}

	public HashSet<String> names = new HashSet<String>();

	public final void SetupName(String name)
	{
		if (names.add(name))
		{
			purgeCache();
			notifyPropertyChanged("ENText");

		}
	}

	public final void RemoveName(String name)
	{
		if (names.remove(name))
		{
			purgeCache();
			notifyPropertyChanged("ENText");

		}
	}

	public final boolean ContainsName(String name)
	{
		return names.contains(name);
	}

	public final void CopyNames(TaxonomyNode node)
	{
		boolean r = false;
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
		for (var n : node.names)
		{
			names.add(n);
			r |= names.size() - 1;
		}
		if (r)
		{
			purgeCache();
			notifyPropertyChanged("ENText");
		}
	}

	private cognipy.ars.EntityKind Kind = cognipy.ars.EntityKind.values()[0];
	public final cognipy.ars.EntityKind getKind()
	{
		return Kind;
	}
	public final void setKind(cognipy.ars.EntityKind value)
	{
		Kind = value;
	}

	private void purgeCache()
	{
		cachedENText = null;
	}
	protected String cachedENText = null;
	public abstract String getENText();


	public enum NodeType
	{
		Standard,
		TopConcept,
		TopRole,
		TopAttribute,
		Nothing,
		FictiousChild,
		OntorionSynced,
		OntorionNotSynced;

		public static final int SIZE = java.lang.Integer.SIZE;

		public int getValue()
		{
			return this.ordinal();
		}

		public static NodeType forValue(int value)
		{
			return values()[value];
		}
	}

	private String _prefix = "";
	protected final void setPrefix(String value)
	{
		_prefix = value;
	}
	public final String getPrefix()
	{
		return _prefix;
	}

	private boolean IsImported;
	public final boolean getIsImported()
	{
		return IsImported;
	}
	public final void setIsImported(boolean value)
	{
		IsImported = value;
	}
	private NodeType _myType = NodeType.Standard;
	public final NodeType getMyType()
	{
		return _myType;
	}
	public final void setMyType(NodeType value)
	{
		_myType = value;
		notifyPropertyChanged("MyType");
	}

	public final Object getSelf()
	{
		return this;
	}

	private SortedObservableCollection<TaxonomyNode> children;
	public final SortedObservableCollection<TaxonomyNode> getChildren()
	{
		return children;
	}
	public final void setChildren(SortedObservableCollection<TaxonomyNode> value)
	{
		children = value;
		notifyPropertyChanged("Children");
	}

	public static boolean haveCommonName(TaxonomyNode a, TaxonomyNode b)
	{
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
		for (var A : a.names)
		{
			if (!b.names.contains(A))
			{
				return false;
			}
		}
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
		for (var B : b.names)
		{
			if (!a.names.contains(B))
			{
				return false;
			}
		}
		return true;
	}

	private boolean _hasAnnotation = false;
	public final boolean getHasAnnotation()
	{
		return _hasAnnotation;
	}
	public final void setHasAnnotation(boolean value)
	{
		_hasAnnotation = value;
		notifyPropertyChanged("HasAnnotation");
	}

	private void notifyPropertyChanged(String propName)
	{
		if (this.invokableProvider.GetInvokable() != null)
		{
			this.invokableProvider.GetInvokable().Invoke(() ->
			{

					if (this.PropertyChanged != null)
					{
						for (PropertyChangedEventHandler listener : PropertyChanged.listeners())
						{
							listener.invoke(this, new PropertyChangedEventArgs(propName));
						}
						for (PropertyChangedEventHandler listener : PropertyChanged.listeners())
						{
							listener.invoke(this, new PropertyChangedEventArgs("Self"));
						}
					}
			});
		}
	}
}