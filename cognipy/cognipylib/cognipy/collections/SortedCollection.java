package cognipy.collections;

import cognipy.*;
import java.util.*;
import java.io.*;

/** 
 Collections that holds elements in the specified order. The complexity and efficiency
 of the algorithm is comparable to the SortedList from .NET collections. In contrast 
 to the SortedList SortedCollection accepts redundant elements. If no comparer is 
 is specified the list will use the default comparer for given type.
 
 <author>consept</author>
*/
//C# TO JAVA CONVERTER TODO TASK: The interface type was changed to the closest equivalent Java type, but the methods implemented will need adjustment:
public class SortedCollection<TValue> implements List<TValue>
{
	// Fields
	private static final int DEFAULT_CAPACITY = 4;

	private static TValue[] emptyValues;

	private Comparator<TValue> comparer;
	private TValue[] values;
	private int size;
	// for enumeration
	private int version;

	static
	{
//C# TO JAVA CONVERTER WARNING: Java does not allow direct instantiation of arrays of generic type parameters:
//ORIGINAL LINE: emptyValues = new TValue[0];
		emptyValues = (TValue[])new Object[0];
	}

	// Constructors
	public SortedCollection()
	{
		this.values = emptyValues;
		this.comparer = Comparer<TValue>.Default;
	}

	public SortedCollection(Comparator<TValue> comparer)
	{
		this.values = emptyValues;
		this.comparer = comparer;
	}

	// Methods
	private void CheckCapacity(int min)
	{
		// double the capacity
		int num = this.values.length == 0 ? DEFAULT_CAPACITY :this.values.length * 2;
		if (min > num)
		{
			num = min;
		}
		this.setCapacity(num);
	}

	public void add(int index, TValue value)
	{
		if (value == null)
		{
			throw new IllegalArgumentException("Value can't be null.");
		}
		if (index < 0 || index > this.size)
		{
			throw new IndexOutOfBoundsException();
		}
		if (this.size == this.values.length)
		{
			this.CheckCapacity(this.size + 1);
		}
		if (index < this.size)
		{
			System.arraycopy(this.values, index, this.values, index + 1, this.size - index);
		}
		this.values[index] = value;
		this.size++;
		this.version++;
	}

	public final void Add(TValue value)
	{
		if (value == null)
		{
			throw new IllegalArgumentException("Value can't be null");
		}
		// check where the element should be placed
		int index = Arrays.<TValue>binarySearch(values, 0, this.size, value, this.comparer);
		if (index < 0)
		{
			// xor
			index = ~index;
			Insert(index, value);
		}
	}

	public void clear()
	{
		this.version++;
		Array.Clear(this.values, 0, this.size);
		this.size = 0;
	}

	public final int indexOf(Object objectValue)
	{
		TValue value = (TValue)objectValue;
		if (value == null)
		{
			throw new IllegalArgumentException("Value can't be null.");
		}
		int index = Arrays.<TValue>binarySearch(values, 0, this.size, value, this.comparer);
		if (index >= 0)
		{
			return index;
		}
		return -1;
	}

	public final boolean contains(Object objectValue)
	{
		TValue value = (TValue)objectValue;
		return this.IndexOf(value) >= 0;
	}

	public final void CopyTo(TValue[] array, int arrayIndex)
	{
		System.arraycopy(this.values, 0, array, arrayIndex, this.size);
	}

	public final int size()
	{
		return this.size;
	}

	public final boolean getIsReadOnly()
	{
		return false;
	}

	public final boolean remove(Object objectValue)
	{
		TValue value = (TValue)objectValue;
		int index = this.IndexOf(value);
		if (index < 0)
		{
			return false;
		}
		RemoveAt(index);
		return true;
	}

	public final Iterator<TValue> iterator()
	{
		return new SortedCollectionEnumerator(this);
	}

	public final Iterator GetEnumerator()
	{
		return new SortedCollectionEnumerator(this);
	}

	// Properties
	public final int getCapacity()
	{
		return this.values.length;
	}
	public final void setCapacity(int value)
	{
		if (this.values.length != value)
		{
			if (value < this.size)
			{
				throw new IllegalArgumentException("Too small capacity.");
			}
			if (value > 0)
			{
//C# TO JAVA CONVERTER WARNING: Java does not allow direct instantiation of arrays of generic type parameters:
//ORIGINAL LINE: TValue[] tempValues = new TValue[value];
				TValue[] tempValues = (TValue[])new Object[value];
				if (this.size > 0)
				{
						// copy only when size is greater than zero
					System.arraycopy(this.values, 0, tempValues, 0, this.size);
				}
				this.values = tempValues;
			}
			else
			{
				this.values = emptyValues;
			}
		}
	}

	public void remove(int index)
	{
		if (index < 0 || index >= this.size)
		{
			throw new IndexOutOfBoundsException();
		}
		this.size--;
		this.version++;
		System.arraycopy(this.values, index + 1, this.values, index, this.size - index);
		this.values[this.size] = null;
	}

	public TValue get(int index)
	{
		if (index < 0 || index >= this.size)
		{
			throw new IndexOutOfBoundsException();
		}
		return this.values[index];
	}
	public void set(int index, TValue value)
	{
		if (index < 0 || index >= this.size)
		{
			throw new IndexOutOfBoundsException();
		}
		this.values[index] = value;
		this.version++;
	}

	private final static class SortedCollectionEnumerator implements Iterator<TValue>, Closeable, Iterator, Serializable
	{
		// Fields
		private SortedCollection<TValue> collection;
		private TValue currentValue;
		private int index;
		private int version;

		// Methods
		public SortedCollectionEnumerator(SortedCollection<TValue> collection)
		{
			this.collection = collection;
			this.version = collection.version;
		}

		public void close() throws IOException
		{
			this.index = 0;
			this.currentValue = null;
		}

		public boolean MoveNext()
		{
			if (this.version != this.collection.version)
			{
				throw new IllegalArgumentException("Collection was changed while iterating!");
			}
			if (this.index < this.collection.size())
			{
				this.currentValue = this.collection.values[this.index];
				this.index++;
				return true;
			}
			this.index = this.collection.size() + 1;
			this.currentValue = null;
			return false;
		}

		public void Reset()
		{
			if (this.version != this.collection.version)
			{
				throw new IllegalArgumentException("Collection was changed while iterating!");
			}
			this.index = 0;
			this.currentValue = null;
		}

		// Properties
		public TValue getCurrent()
		{
			return this.currentValue;
		}

		public Object getCurrent()
		{
			if ((this.index == 0) || (this.index == this.collection.size() + 1))
			{
				throw new IllegalArgumentException("Enumerator not initilized. Call MoveNext first.");
			}
			return this.currentValue;
		}
	}
}