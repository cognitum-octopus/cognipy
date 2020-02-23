using System;
using System.Collections.Generic;
using System.Text;

namespace Ontorion.CNL.DL
{
    public interface IVisitor
    {
        object Visit(Paragraph p);
        object Visit(DLAnnotationAxiom dLAnnotation);
        object Visit(Annotation a);
        object Visit(Subsumption e);
        object Visit(Equivalence e);
        object Visit(Disjoint e);
        object Visit(DisjointUnion e);
        object Visit(DataTypeDefinition e);
        object Visit(RoleInclusion e);
        object Visit(ComplexRoleInclusion e);
        object Visit(RoleEquivalence e);
        object Visit(RoleDisjoint e);
        object Visit(DataRoleInclusion e);
        object Visit(DataRoleEquivalence e);
        object Visit(DataRoleDisjoint e);
        object Visit(InstanceOf e);
        object Visit(RelatedInstances e);
        object Visit(InstanceValue e);
        object Visit(SameInstances e);
        object Visit(DifferentInstances e);
        object Visit(HasKey e);
        object Visit(Number e);
        object Visit(String e);
        object Visit(Float e);
        object Visit(Bool e);
        object Visit(DateTimeVal e);
        object Visit(Duration e);
        object Visit(Facet e);
        object Visit(FacetList e);
        object Visit(BoundFacets e);
        object Visit(BoundOr e);
        object Visit(BoundAnd e);
        object Visit(BoundNot e);
        object Visit(BoundVal e);
        object Visit(ValueSet e);
        object Visit(TotalBound e);
        object Visit(DTBound e);
        object Visit(TopBound e);
        object Visit(Atomic e);
        object Visit(NamedInstance e);
        object Visit(UnnamedInstance e);
        object Visit(Top e);
        object Visit(Bottom e);
        object Visit(RoleInversion e);
        object Visit(InstanceSet e);
        object Visit(ConceptOr e);
        object Visit(ConceptAnd e);
        object Visit(ConceptNot e);
        object Visit(OnlyRestriction e);
        object Visit(SomeRestriction e);
        object Visit(OnlyValueRestriction e);
        object Visit(SomeValueRestriction e);
        object Visit(SelfReference e);
        object Visit(NumberRestriction e);
        object Visit(NumberValueRestriction e);

        object Visit(SwrlStatement e);
        object Visit(SwrlItemList e);
        object Visit(SwrlInstance e);
        object Visit(SwrlRole e);
        object Visit(SwrlSameAs e);
        object Visit(SwrlDifferentFrom e);
        object Visit(SwrlDataProperty e);
        object Visit(SwrlDataRange e);
        object Visit(SwrlBuiltIn e);
        object Visit(SwrlDVal e);
        object Visit(SwrlDVar e);
        object Visit(SwrlIVal e);
        object Visit(SwrlIVar e);

        object Visit(SwrlIterate e);
        object Visit(ExeStatement e);
        object Visit(SwrlVarList e);

        object Visit(CodeStatement e);
    }


}
