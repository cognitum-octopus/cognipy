using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace FluentEditorClientLib.models
{
    public class AnnotationResult
    {
        //     Subject of the annotation
        public string Subject { get; set; }

        //     type of the subject (one of Concept, Role, Instance, DataRole, Statement)
        public string SubjectType { get; set; }

        //     name of the annotation
        public string Property { get; set; }

        //     value of the annotation
        public object Value { get; set; }
        
        //     language of the annotation (can be empty or null!)
        public string Language { get; set; }
    }
}
