package cognipy.ars;

import cognipy.cnl.dl.*;
import cognipy.cnl.en.*;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.*;
import org.semanticweb.owlapi.vocab.*;
import cognipy.*;
import java.util.*;

public class NoInversionsForDataRolesException extends IncorectOWLStatementException
{
	public String RoleName;
	public NoInversionsForDataRolesException(String role)
	{
		RoleName = role;
	}

	@Override
	public String getMessage()
	{
		return "Inversion on attributes are prohibited.\r\nYou tryied to do it with '" + RoleName + "' attribute.";
	}
}