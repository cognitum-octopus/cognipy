using Ontorion.ARS;
using org.semanticweb.owlapi.vocab;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Text;
using System.Text.RegularExpressions;
using System.Threading;

namespace Ontorion.CNL.DL
{
    internal class CalcDynamicDepths : GenericVisitor
    {
        public HashSet<int> IntersectionDepth = new HashSet<int>();
        public HashSet<int> UnionDepth = new HashSet<int>();
        public HashSet<int> HasKeyDepth = new HashSet<int>();

        public override object Visit(ConceptAnd e)
        {
            IntersectionDepth.Add(e.Exprs.Count);
            return base.Visit(e);
        }

        public override object Visit(ConceptOr e)
        {
            UnionDepth.Add(e.Exprs.Count);
            return base.Visit(e);
        }
        public override object Visit(HasKey e)
        {
            UnionDepth.Add(e.DataRoles.Count + e.Roles.Count);
            return base.Visit(e);
        }
    }
}