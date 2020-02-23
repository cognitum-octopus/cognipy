using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Ontorion.CNL.DL
{
    // TODO [AnnotationForSentences]: this is a first (not optimal version) of a DL standardizer.
    // this serializer should somehow create a unique DL string for each sentence independent on clauses position.....
    class StandardizedSerializer : Serializer
    {
        public override object Visit(SwrlIVar e)
        {
            return "";
        }

        public override object Visit(SwrlDVar e)
        {
            return "";
        }

        public override object Visit(SwrlVarList e)
        {
            return "";
        }
    }
}
