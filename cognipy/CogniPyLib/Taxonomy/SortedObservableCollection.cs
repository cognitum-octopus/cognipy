using System.Collections.Generic;
using System.Collections.Specialized;
using System.ComponentModel;
using System;

namespace Ontorion.Collections
{
    /// <summary>
    /// SortedCollection which implements INotifyCollectionChanged interface and so can be used
    /// in WPF applications as the source of the binding.
    /// </summary>
    /// <author>consept</author>
    public interface IInvokable
    {
        void Invoke(Action act);
    }

    public interface IInvokableProvider
    {
        IInvokable GetInvokable();
    }

    public class SortedObservableCollection<TValue> : SortedCollection<TValue>, INotifyPropertyChanged, INotifyCollectionChanged
    {
        IInvokableProvider invokableprovider;
        public SortedObservableCollection(IInvokableProvider invokableprovider) : base() { this.invokableprovider = invokableprovider; }

        public SortedObservableCollection(IComparer<TValue> comparer, IInvokableProvider invokableprovider) : base(comparer) { this.invokableprovider = invokableprovider; }

        object guard = new object();

        // Events
        public event NotifyCollectionChangedEventHandler CollectionChanged;

        public event PropertyChangedEventHandler PropertyChanged;

        private void Invoke(Action act)
        {
            if (invokableprovider.GetInvokable() != null)
            {
                invokableprovider.GetInvokable().Invoke(() => {
                    try
                    {
                        lock(guard)
                            act();
                    }
                    catch(Exception)
                    {
                        if (this.CollectionChanged != null)
                            this.CollectionChanged(this, new NotifyCollectionChangedEventArgs(NotifyCollectionChangedAction.Reset));
                    }
                });
            }
        }

        private void OnCollectionChanged(NotifyCollectionChangedAction action, object item, int index)
        {
            Invoke(() =>
                {
                    if (this.CollectionChanged != null)
                        this.CollectionChanged(this, new NotifyCollectionChangedEventArgs(action, item, index));
                });
        }

        private void OnCollectionChanged(NotifyCollectionChangedAction action, object oldItem, object newItem, int index)
        {
            Invoke(() =>
                {
                    if (this.CollectionChanged != null)
                        this.CollectionChanged(this, new NotifyCollectionChangedEventArgs(action, newItem, oldItem, index));
                });
        }

        private void OnCollectionChanged(NotifyCollectionChangedAction action, object item, int index, int oldIndex)
        {
            Invoke(() =>
                {
                    if (this.CollectionChanged != null)
                        this.CollectionChanged(this, new NotifyCollectionChangedEventArgs(action, item, index, oldIndex));
                });
        }


        public void OnPropertyChanged(string propertyName)
        {
           Invoke(() =>
                {
                    if (this.PropertyChanged != null)
                        this.PropertyChanged(this, new PropertyChangedEventArgs(propertyName));
                });
        }

        public override void Insert(int index, TValue value)
        {
            lock (guard)
            {
                base.Insert(index, value);
                Invoke(() =>
                    {
                        if (this.PropertyChanged != null)
                        {
                            this.PropertyChanged(this, new PropertyChangedEventArgs("Count"));
                            this.PropertyChanged(this, new PropertyChangedEventArgs("Item[]"));
                        }
                        if (this.CollectionChanged != null)
                            this.CollectionChanged(this, new NotifyCollectionChangedEventArgs(NotifyCollectionChangedAction.Add, value, index));
                    });
            }
        }

        public override void RemoveAt(int index)
        {
            lock (guard)
            {
                var item = this[index];
                base.RemoveAt(index);
                Invoke(() =>
                {
                    if (this.PropertyChanged != null)
                    {
                        this.PropertyChanged(this, new PropertyChangedEventArgs("Count"));
                        this.PropertyChanged(this, new PropertyChangedEventArgs("Item[]"));
                    }
                    if (this.CollectionChanged != null)
                        this.CollectionChanged(this, new NotifyCollectionChangedEventArgs(NotifyCollectionChangedAction.Remove, item, index));
                });
            }
        }

        public override TValue this[int index]
        {
            get
            {
                lock (guard)
                    return base[index];
            }
            set
            {
                lock (guard)
                {
                    var oldItem = base[index];
                    base[index] = value;
                    Invoke(() =>
                    {
                        if (this.PropertyChanged != null)
                            this.PropertyChanged(this, new PropertyChangedEventArgs("Item[]"));
                        if (this.CollectionChanged != null)
                            this.CollectionChanged(this, new NotifyCollectionChangedEventArgs(NotifyCollectionChangedAction.Replace, oldItem, value, index));
                    });
                }
            }
        }

        public override void Clear()
        {
            lock (guard)
            {
                base.Clear();
                Invoke(() =>
                {
                    if (this.CollectionChanged != null)
                        this.CollectionChanged(this, new NotifyCollectionChangedEventArgs(NotifyCollectionChangedAction.Reset));
                });
            }
        }

        internal void SetInvokableProvider(IInvokableProvider invokable)
        {
            lock (guard)
                invokableprovider = invokable;
        }
    }
}
