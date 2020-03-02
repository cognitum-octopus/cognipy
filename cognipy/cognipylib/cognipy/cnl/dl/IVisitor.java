package cognipy.cnl.dl;

import cognipy.*;
import cognipy.cnl.*;

public interface IVisitor
{
	Object Visit(Paragraph p);
	Object Visit(DLAnnotationAxiom dLAnnotation);
	Object Visit(Annotation a);
	Object Visit(Subsumption e);
	Object Visit(Equivalence e);
	Object Visit(Disjoint e);
	Object Visit(DisjointUnion e);
	Object Visit(DataTypeDefinition e);
	Object Visit(RoleInclusion e);
	Object Visit(ComplexRoleInclusion e);
	Object Visit(RoleEquivalence e);
	Object Visit(RoleDisjoint e);
	Object Visit(DataRoleInclusion e);
	Object Visit(DataRoleEquivalence e);
	Object Visit(DataRoleDisjoint e);
	Object Visit(InstanceOf e);
	Object Visit(RelatedInstances e);
	Object Visit(InstanceValue e);
	Object Visit(SameInstances e);
	Object Visit(DifferentInstances e);
	Object Visit(HasKey e);
	Object Visit(Number e);
	Object Visit(String e);
	Object Visit(Float e);
	Object Visit(Bool e);
	Object Visit(DateTimeVal e);
	Object Visit(Duration e);
	Object Visit(Facet e);
	Object Visit(FacetList e);
	Object Visit(BoundFacets e);
	Object Visit(BoundOr e);
	Object Visit(BoundAnd e);
	Object Visit(BoundNot e);
	Object Visit(BoundVal e);
	Object Visit(ValueSet e);
	Object Visit(TotalBound e);
	Object Visit(DTBound e);
	Object Visit(TopBound e);
	Object Visit(Atomic e);
	Object Visit(NamedInstance e);
	Object Visit(UnnamedInstance e);
	Object Visit(Top e);
	Object Visit(Bottom e);
	Object Visit(RoleInversion e);
	Object Visit(InstanceSet e);
	Object Visit(ConceptOr e);
	Object Visit(ConceptAnd e);
	Object Visit(ConceptNot e);
	Object Visit(OnlyRestriction e);
	Object Visit(SomeRestriction e);
	Object Visit(OnlyValueRestriction e);
	Object Visit(SomeValueRestriction e);
	Object Visit(SelfReference e);
	Object Visit(NumberRestriction e);
	Object Visit(NumberValueRestriction e);

	Object Visit(SwrlStatement e);
	Object Visit(SwrlItemList e);
	Object Visit(SwrlInstance e);
	Object Visit(SwrlRole e);
	Object Visit(SwrlSameAs e);
	Object Visit(SwrlDifferentFrom e);
	Object Visit(SwrlDataProperty e);
	Object Visit(SwrlDataRange e);
	Object Visit(SwrlBuiltIn e);
	Object Visit(SwrlDVal e);
	Object Visit(SwrlDVar e);
	Object Visit(SwrlIVal e);
	Object Visit(SwrlIVar e);

	Object Visit(SwrlIterate e);
	Object Visit(ExeStatement e);
	Object Visit(SwrlVarList e);

	Object Visit(CodeStatement e);
}