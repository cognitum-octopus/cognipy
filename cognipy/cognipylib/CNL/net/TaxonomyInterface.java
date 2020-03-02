package cnl.net;

import cognipy.collections.*;

public interface TaxonomyInterface extends INotifyPropertyChanged, IInvokableProvider
{
	/** 
	 All the childrens of the current taxonomy
	*/
	ObservableCollection<TaxonomyNode> getChildren();
	void setChildren(ObservableCollection<TaxonomyNode> value);

	/** 
	 When the taxonomy is reasoned, this boolean is used to choose between the (fast) structural reasoner and the normal reasoner
	*/
	boolean getIsStructural();
	void setIsStructural(boolean value);



	/** 
	 set the invokable element that will be connected to this taxonomy
	 
	 @param invokable
	*/
	void SetInvokable(IInvokable invokable);

	/** 
	 Add a DL paragraph to the current taxonomy
	 
	 @param p
	*/
	void Add(cognipy.cnl.dl.Paragraph p);

	/** 
	 
	 
	 @param structural
	*/
	void Merge();

	/** 
	 Merges the taxonomy contained in newTaxo into the current taxonomy
	 If generate is true we are generating a new taxonomy
	 
	 @param newTaxo taxonomy from which to merge
	 @param generate Decides wether we are generating a new taxonomy or merging with an old one
	*/
	void Merge(TaxonomyInterface newTaxo, boolean generate);

	void Refresh();

	/** 
	 Creates the taxonomy
	 
	 @param structural
	*/
	void Attach();

	void Dispose();

	/** 
	 Appends the annotations contained in annotMan to the taxonomyNodes
	 
	 @param annotMan Annotation manager containing the nodes that have annotations
	 @return 
	*/
	void updateAnnotationPropertyForNodes(cognipy.cnl.AnnotationManager annotMan);
}