using CogniPy.Collections;
using System;
using System.Collections.Generic;
using System.ComponentModel;

namespace CNL.NET
{
    public delegate void RefreshNeededEventHandler(object sender, EventArgs e);

    public abstract class TaxonomyNode : INotifyPropertyChanged, IComparable
    {
        public event PropertyChangedEventHandler PropertyChanged;
        IInvokableProvider invokableProvider;
        public int CompareTo(object obj)
        {
            if (obj is TaxonomyNode)
                return ENText.CompareTo((obj as TaxonomyNode).ENText);
            else
                return 0;
        }

        protected TaxonomyNode(IInvokableProvider invokableProvider)
        {
            this.invokableProvider = invokableProvider;
            this.children = new SortedObservableCollection<TaxonomyNode>(invokableProvider);

        }

        Guid _uniqueId = Guid.NewGuid();
        public Guid ID { get { return _uniqueId; } }

        TaxonomyNode _parentNode = null;
        public TaxonomyNode Parent { get { return _parentNode; } set { _parentNode = value; } }
        bool _isOntorionMode = false;
        public bool IsInOntorionModule
        {
            get { return _isOntorionMode; }
            set
            {
                _isOntorionMode = value;
                notifyPropertyChanged("IsInOntorionModule");

            }
        }
        bool _isExpanded = false;
        public bool isExpandedNoPropertyChanged
        {
            set { _isExpanded = value; }
        }

        public bool IsExpanded
        {
            get { return _isExpanded; }
            set
            {
                _isExpanded = value;
                notifyPropertyChanged("IsExpanded");

            }
        }

        public void SetupChild(TaxonomyNode node)
        {
            if (node.ENText != "" || node.MyType == NodeType.FictiousChild)
            {
                node.SetInvokableProvider(invokableProvider);
                Children.Add(node);
            }
        }

        void SetInvokableProvider(IInvokableProvider invokableProvider2)
        {
            this.invokableProvider = invokableProvider2;
            Children.SetInvokableProvider(invokableProvider2);
            foreach (var c in Children)
                c.SetInvokableProvider(invokableProvider2);
        }

        public void RemoveChild(TaxonomyNode node)
        {
            if (this.Children.Contains(node))
            {
                Children.Remove(node);
            }
        }

        public HashSet<string> names = new HashSet<string>();

        public void SetupName(string name)
        {
            if (names.Add(name))
            {
                purgeCache();
                notifyPropertyChanged("ENText");

            }
        }

        public void RemoveName(string name)
        {
            if (names.Remove(name))
            {
                purgeCache();
                notifyPropertyChanged("ENText");

            }
        }

        public bool ContainsName(string name)
        {
            return names.Contains(name);
        }

        public void CopyNames(TaxonomyNode node)
        {
            bool r = false;
            foreach (var n in node.names)
            {
                r |= names.Add(n);
            }
            if (r)
            {
                purgeCache();
                notifyPropertyChanged("ENText");
            }
        }

        public CogniPy.ARS.EntityKind Kind { get; set; }

        void purgeCache()
        {
            cachedENText = null;
        }
        protected string cachedENText = null;
        public abstract string ENText { get; }


        public enum NodeType { Standard, TopConcept, TopRole, TopAttribute, Nothing, FictiousChild, OntorionSynced, OntorionNotSynced }

        string _prefix = "";
        public string Prefix
        {
            protected set { _prefix = value; }
            get { return _prefix; }
        }

        public bool IsImported { get; set; }
        NodeType _myType = NodeType.Standard;
        public NodeType MyType
        {
            get { return _myType; }
            set
            {
                _myType = value;
                notifyPropertyChanged("MyType");
            }
        }

        public Object Self { get { return this; } }

        private SortedObservableCollection<TaxonomyNode> children;
        public SortedObservableCollection<TaxonomyNode> Children
        {
            get { return children; }
            set
            {
                children = value;
                notifyPropertyChanged("Children");
            }
        }

        public static bool haveCommonName(TaxonomyNode a, TaxonomyNode b)
        {
            foreach (var A in a.names)
            {
                if (!b.names.Contains(A))
                    return false;
            }
            foreach (var B in b.names)
            {
                if (!a.names.Contains(B))
                    return false;
            }
            return true;
        }

        bool _hasAnnotation = false;
        public bool HasAnnotation
        {
            get { return _hasAnnotation; }
            set
            {
                _hasAnnotation = value;
                notifyPropertyChanged("HasAnnotation");
            }
        }

        private void notifyPropertyChanged(string propName)
        {
            if (this.invokableProvider.GetInvokable() != null)
            {
                this.invokableProvider.GetInvokable().Invoke(() =>
                {

                    if (this.PropertyChanged != null)
                    {
                        this.PropertyChanged(this, new PropertyChangedEventArgs(propName));
                        this.PropertyChanged(this, new PropertyChangedEventArgs("Self"));
                    }
                });
            }
        }
    }
}
