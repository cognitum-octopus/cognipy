package cognipy.collections;

import cognipy.*;
import java.util.*;

public class SortedObservableCollection<TValue> extends SortedCollection<TValue> implements INotifyPropertyChanged, INotifyCollectionChanged
{
	private IInvokableProvider invokableprovider;
	public SortedObservableCollection(IInvokableProvider invokableprovider)
	{
		super();
	this.invokableprovider = invokableprovider;
	}

	public SortedObservableCollection(Comparator<TValue> comparer, IInvokableProvider invokableprovider)
	{
		super(comparer);
	this.invokableprovider = invokableprovider;
	}

	private Object guard = new Object();

	// Events
	public tangible.Event<NotifyCollectionChangedEventHandler> CollectionChanged = new tangible.Event<NotifyCollectionChangedEventHandler>();

	public tangible.Event<PropertyChangedEventHandler> PropertyChanged = new tangible.Event<PropertyChangedEventHandler>();

	private void Invoke(tangible.Action0Param act)
	{
		if (invokableprovider.GetInvokable() != null)
		{
			invokableprovider.GetInvokable().Invoke(() ->
			{
					try
					{
						synchronized (guard)
						{
							act.invoke();
						}
					}
					catch (RuntimeException e)
					{
						if (this.CollectionChanged != null)
						{
							for (NotifyCollectionChangedEventHandler listener : CollectionChanged.listeners())
							{
								listener.invoke(this, new NotifyCollectionChangedEventArgs(NotifyCollectionChangedAction.Reset));
							}
						}
					}
			});
		}
	}

	private void OnCollectionChanged(NotifyCollectionChangedAction action, Object item, int index)
	{
		Invoke(() ->
		{
					if (this.CollectionChanged != null)
					{
						for (NotifyCollectionChangedEventHandler listener : CollectionChanged.listeners())
						{
							listener.invoke(this, new NotifyCollectionChangedEventArgs(action, item, index));
						}
					}
		});
	}

	private void OnCollectionChanged(NotifyCollectionChangedAction action, Object oldItem, Object newItem, int index)
	{
		Invoke(() ->
		{
					if (this.CollectionChanged != null)
					{
						for (NotifyCollectionChangedEventHandler listener : CollectionChanged.listeners())
						{
							listener.invoke(this, new NotifyCollectionChangedEventArgs(action, newItem, oldItem, index));
						}
					}
		});
	}

	private void OnCollectionChanged(NotifyCollectionChangedAction action, Object item, int index, int oldIndex)
	{
		Invoke(() ->
		{
					if (this.CollectionChanged != null)
					{
						for (NotifyCollectionChangedEventHandler listener : CollectionChanged.listeners())
						{
							listener.invoke(this, new NotifyCollectionChangedEventArgs(action, item, index, oldIndex));
						}
					}
		});
	}


	public final void OnPropertyChanged(String propertyName)
	{
		Invoke(() ->
		{
					 if (this.PropertyChanged != null)
					 {
						 for (PropertyChangedEventHandler listener : PropertyChanged.listeners())
						 {
							 listener.invoke(this, new PropertyChangedEventArgs(propertyName));
						 }
					 }
		});
	}

	@Override
	public void add(int index, TValue value)
	{
		synchronized (guard)
		{
			super.Insert(index, value);
			Invoke(() ->
			{
						if (this.PropertyChanged != null)
						{
							for (PropertyChangedEventHandler listener : PropertyChanged.listeners())
							{
								listener.invoke(this, new PropertyChangedEventArgs("Count"));
							}
							for (PropertyChangedEventHandler listener : PropertyChanged.listeners())
							{
								listener.invoke(this, new PropertyChangedEventArgs("Item[]"));
							}
						}
						if (this.CollectionChanged != null)
						{
							for (NotifyCollectionChangedEventHandler listener : CollectionChanged.listeners())
							{
								listener.invoke(this, new NotifyCollectionChangedEventArgs(NotifyCollectionChangedAction.Add, value, index));
							}
						}
			});
		}
	}

	@Override
	public void remove(int index)
	{
		synchronized (guard)
		{
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
			var item = this.get(index);
			super.RemoveAt(index);
			Invoke(() ->
			{
					if (this.PropertyChanged != null)
					{
						for (PropertyChangedEventHandler listener : PropertyChanged.listeners())
						{
							listener.invoke(this, new PropertyChangedEventArgs("Count"));
						}
						for (PropertyChangedEventHandler listener : PropertyChanged.listeners())
						{
							listener.invoke(this, new PropertyChangedEventArgs("Item[]"));
						}
					}
					if (this.CollectionChanged != null)
					{
						for (NotifyCollectionChangedEventHandler listener : CollectionChanged.listeners())
						{
							listener.invoke(this, new NotifyCollectionChangedEventArgs(NotifyCollectionChangedAction.Remove, item, index));
						}
					}
			});
		}
	}

	@Override
	public TValue get(int index)
	{
		synchronized (guard)
		{
			return super[index];
		}
	}
	@Override
	public void set(int index, TValue value)
	{
		synchronized (guard)
		{
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
			var oldItem = super[index];
			super[index] = value;
			Invoke(() ->
			{
					if (this.PropertyChanged != null)
					{
						for (PropertyChangedEventHandler listener : PropertyChanged.listeners())
						{
							listener.invoke(this, new PropertyChangedEventArgs("Item[]"));
						}
					}
					if (this.CollectionChanged != null)
					{
						for (NotifyCollectionChangedEventHandler listener : CollectionChanged.listeners())
						{
							listener.invoke(this, new NotifyCollectionChangedEventArgs(NotifyCollectionChangedAction.Replace, oldItem, value, index));
						}
					}
			});
		}
	}

	@Override
	public void clear()
	{
		synchronized (guard)
		{
			super.Clear();
			Invoke(() ->
			{
					if (this.CollectionChanged != null)
					{
						for (NotifyCollectionChangedEventHandler listener : CollectionChanged.listeners())
						{
							listener.invoke(this, new NotifyCollectionChangedEventArgs(NotifyCollectionChangedAction.Reset));
						}
					}
			});
		}
	}

	public final void SetInvokableProvider(IInvokableProvider invokable)
	{
		synchronized (guard)
		{
			invokableprovider = invokable;
		}
	}
}