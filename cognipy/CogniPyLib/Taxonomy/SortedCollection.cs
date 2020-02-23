using System;
using System.Collections;
using System.Collections.Generic;

namespace Ontorion.Collections
{
    /// <summary>
    /// Collections that holds elements in the specified order. The complexity and efficiency
    /// of the algorithm is comparable to the SortedList from .NET collections. In contrast 
    /// to the SortedList SortedCollection accepts redundant elements. If no comparer is 
    /// is specified the list will use the default comparer for given type.
    /// </summary>
    /// <author>consept</author>
    public class SortedCollection<TValue> : IList<TValue>
    {
        // Fields
        private const int DEFAULT_CAPACITY = 4;

        private static TValue[] emptyValues;

        private readonly IComparer<TValue> comparer;
        private TValue[] values;
        private int size;
        // for enumeration
        private int version;

        static SortedCollection()
        {
            emptyValues = new TValue[0];
        }

        // Constructors
        public SortedCollection()
        {
            this.values = emptyValues;
            this.comparer = Comparer<TValue>.Default;
        }

        public SortedCollection(IComparer<TValue> comparer)
        {
            this.values = emptyValues;
            this.comparer = comparer;
        }

        // Methods
        private void CheckCapacity(int min)
        {
            // double the capacity
            int num = this.values.Length == 0 ? DEFAULT_CAPACITY : this.values.Length * 2;
            if (min > num)
            {
                num = min;
            }
            this.Capacity = num;
        }

        public virtual void Insert(int index, TValue value)
        {
            if (value == null)
            {
                throw new ArgumentException("Value can't be null.");
            }
            if (index < 0 || index > this.size)
            {
                throw new ArgumentOutOfRangeException();
            }
            if (this.size == this.values.Length)
            {
                this.CheckCapacity(this.size + 1);
            }
            if (index < this.size)
            {
                Array.Copy(this.values, index, this.values, index + 1, this.size - index);
            }
            this.values[index] = value;
            this.size++;
            this.version++;
        }

        public void Add(TValue value)
        {
            if (value == null)
            {
                throw new ArgumentException("Value can't be null");
            }
            // check where the element should be placed
            int index = Array.BinarySearch<TValue>(values, 0, this.size, value, this.comparer);
            if (index < 0)
            {
                // xor
                index = ~index;
                Insert(index, value);
            }
        }

        public virtual void Clear()
        {
            this.version++;
            Array.Clear(this.values, 0, this.size);
            this.size = 0;
        }

        public int IndexOf(TValue value)
        {
            if (value == null)
            {
                throw new ArgumentException("Value can't be null.");
            }
            int index = Array.BinarySearch<TValue>(values, 0, this.size, value, this.comparer);
            if (index >= 0)
            {
                return index;
            }
            return -1;
        }

        public bool Contains(TValue value)
        {
            return this.IndexOf(value) >= 0;
        }

        public void CopyTo(TValue[] array, int arrayIndex)
        {
            Array.Copy(this.values, 0, array, arrayIndex, this.size);
        }

        public int Count
        {
            get { return this.size; }
        }

        public bool IsReadOnly
        {
            get { return false; }
        }

        public bool Remove(TValue value)
        {
            int index = this.IndexOf(value);
            if (index < 0)
            {
                return false;
            }
            RemoveAt(index);
            return true;
        }

        public IEnumerator<TValue> GetEnumerator()
        {
            return new SortedCollectionEnumerator(this);
        }

        IEnumerator IEnumerable.GetEnumerator()
        {
            return new SortedCollectionEnumerator(this);
        }

        // Properties
        public int Capacity
        {
            get { return this.values.Length; }
            set
            {
                if (this.values.Length != value)
                {
                    if (value < this.size)
                    {
                        throw new ArgumentException("Too small capacity.");
                    }
                    if (value > 0)
                    {
                        TValue[] tempValues = new TValue[value];
                        if (this.size > 0)
                        {
                            // copy only when size is greater than zero
                            Array.Copy(this.values, 0, tempValues, 0, this.size);
                        }
                        this.values = tempValues;
                    }
                    else
                    {
                        this.values = emptyValues;
                    }
                }
            }
        }

        public virtual void RemoveAt(int index)
        {
            if (index < 0 || index >= this.size)
            {
                throw new ArgumentOutOfRangeException();
            }
            this.size--;
            this.version++;
            Array.Copy(this.values, index + 1, this.values, index, this.size - index);
            this.values[this.size] = default(TValue);
        }

        public virtual TValue this[int index]
        {
            get
            {
                if (index < 0 || index >= this.size)
                {
                    throw new ArgumentOutOfRangeException();
                }
                return this.values[index];
            }
            set
            {
                if (index < 0 || index >= this.size)
                {
                    throw new ArgumentOutOfRangeException();
                }
                this.values[index] = value;
                this.version++;
            }
        }

        [Serializable]
        private sealed class SortedCollectionEnumerator : IEnumerator<TValue>, IDisposable, IEnumerator
        {
            // Fields
            private readonly SortedCollection<TValue> collection;
            private TValue currentValue;
            private int index;
            private int version;

            // Methods
            internal SortedCollectionEnumerator(SortedCollection<TValue> collection)
            {
                this.collection = collection;
                this.version = collection.version;
            }

            public void Dispose()
            {
                this.index = 0;
                this.currentValue = default(TValue);
            }

            public bool MoveNext()
            {
                if (this.version != this.collection.version)
                {
                    throw new ArgumentException("Collection was changed while iterating!");
                }
                if (this.index < this.collection.Count)
                {
                    this.currentValue = this.collection.values[this.index];
                    this.index++;
                    return true;
                }
                this.index = this.collection.Count + 1;
                this.currentValue = default(TValue);
                return false;
            }

            void IEnumerator.Reset()
            {
                if (this.version != this.collection.version)
                {
                    throw new ArgumentException("Collection was changed while iterating!");
                }
                this.index = 0;
                this.currentValue = default(TValue);
            }

            // Properties
            public TValue Current
            {
                get
                {
                    return this.currentValue;
                }
            }

            object IEnumerator.Current
            {
                get
                {
                    if ((this.index == 0) || (this.index == this.collection.Count + 1))
                    {
                        throw new ArgumentException("Enumerator not initilized. Call MoveNext first.");
                    }
                    return this.currentValue;
                }
            }
        }
    }
}