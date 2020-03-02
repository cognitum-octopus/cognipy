package cognipy.ars;

import cognipy.cnl.dl.*;
import cognipy.cnl.en.*;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.*;
import org.semanticweb.owlapi.vocab.*;
import cognipy.*;
import java.util.*;

public class DifferenceToItsef extends IncorectOWLStatementException
{
	@Override
	public String getMessage()
	{
		return "Entity cannot be different from itself.";
	}
}