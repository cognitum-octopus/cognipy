using CogniPy.Collections;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.ComponentModel;
using System.Linq;
using System.Text;

namespace CNL.NET
{
    public interface TaxonomyInterface : INotifyPropertyChanged, IInvokableProvider
    {
        /// <summary>
        /// All the childrens of the current taxonomy
        /// </summary>
        ObservableCollection<TaxonomyNode> Children { get; set; }

        /// <summary>
        /// When the taxonomy is reasoned, this boolean is used to choose between the (fast) structural reasoner and the normal reasoner
        /// </summary>
        bool IsStructural { get; set; }

        

        /// <summary>
        /// set the invokable element that will be connected to this taxonomy
        /// </summary>
        /// <param name="invokable"></param>
        void SetInvokable(IInvokable invokable);

        /// <summary>
        /// Add a DL paragraph to the current taxonomy
        /// </summary>
        /// <param name="p"></param>
        void Add(CogniPy.CNL.DL.Paragraph p);

        /// <summary>
        /// 
        /// </summary>
        /// <param name="structural"></param>
        void Merge();
        
        /// <summary>
        /// Merges the taxonomy contained in newTaxo into the current taxonomy
        /// If generate is true we are generating a new taxonomy
        /// </summary>
        /// <param name="newTaxo">taxonomy from which to merge</param>
        /// <param name="generate">Decides wether we are generating a new taxonomy or merging with an old one</param>
        void Merge(TaxonomyInterface newTaxo,bool generate);

        void Refresh();

        /// <summary>
        /// Creates the taxonomy
        /// </summary>
        /// <param name="structural"></param>
        void Attach();

        void Dispose();

        /// <summary>
        /// Appends the annotations contained in annotMan to the taxonomyNodes
        /// </summary>
        /// <param name="annotMan">Annotation manager containing the nodes that have annotations</param>
        /// <returns></returns>
        void updateAnnotationPropertyForNodes(CogniPy.CNL.AnnotationManager annotMan);
    }
}
