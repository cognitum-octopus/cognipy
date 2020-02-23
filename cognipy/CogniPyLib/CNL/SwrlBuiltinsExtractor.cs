using Ontorion.CNL.DL;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Ontorion.CNL
{
    internal class SwrlBuiltinsExtractor : GenericVisitor
    {
        public Tuple<Paragraph, Paragraph> Split(Paragraph e)
        {
            return e.accept(this) as Tuple<Paragraph, Paragraph>;
        }

        bool builtinFound = false;
        public override object Visit(Paragraph e)
        {
            List<Statement> swrlBuiltinsStatements = new List<Statement>();
            List<Statement> normalStatements = new List<Statement>();
            foreach (var x in e.Statements)
            {
                builtinFound = false;
                x.accept(this);
                if (builtinFound || x.modality!= Statement.Modality.IS)
                    swrlBuiltinsStatements.Add(x);
                else
                    normalStatements.Add(x);
            }
            return Tuple.Create(new Paragraph(null) { Statements = normalStatements }, new Paragraph(null) { Statements = swrlBuiltinsStatements });
        }

        public override object Visit(SwrlStatement e)
        {
            builtinFound = true;
            return null;
        }

        public override object Visit(SwrlBuiltIn e)
        {
            builtinFound = true;
            return null;
        }

        public override object Visit(ExeStatement e)
        {
            builtinFound = true;
            return null;
        }

        public override object Visit(SwrlIterate e)
        {
            builtinFound = true;
            return null;
        }

        public override object Visit(CodeStatement e)
        {
            builtinFound = true;
            return null;
        }
    }
}