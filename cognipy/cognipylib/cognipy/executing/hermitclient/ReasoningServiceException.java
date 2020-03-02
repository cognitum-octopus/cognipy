package cognipy.executing.hermitclient;

import cognipy.ars.*;
import cognipy.cnl.*;
import cognipy.cnl.dl.*;
import cognipy.configuration.*;
import cognipy.executing.hermit.*;
import cognipy.models.*;
import com.clarkparsia.owlapi.explanation.*;
import org.semanticweb.owlapi.apibinding.*;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.profiles.*;
import org.semanticweb.owlapi.reasoner.*;
import org.semanticweb.owlapi.reasoner.impl.*;
import org.semanticweb.owlapi.reasoner.structural.*;
import org.semanticweb.owlapi.util.*;
import cognipy.*;
import java.io.*;
import java.util.*;

public class ReasoningServiceException extends RuntimeException
{
	public ReasoningServiceException(String message)
	{
		super(message);
	}
}