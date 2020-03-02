package cognipy.ars;

import cognipy.cnl.dl.*;
import org.semanticweb.owlapi.model.*;
import cognipy.*;
import java.util.*;

public interface IOwlNameingConvention
{
	cognipy.cnl.dl.DlName ToDL(OwlName owlname, CNL.EN.endict lex, tangible.Func1Param<String, String> ns2pfx, EntityKind madeFor);
	OwlName FromDL(cognipy.cnl.dl.DlName dl, CNL.EN.endict lex, tangible.Func1Param<String, String> pfx2ns, EntityKind madeFor);
}