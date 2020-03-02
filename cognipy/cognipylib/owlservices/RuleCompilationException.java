package owlservices;

import cognipy.cnl.dl.*;
import java.util.*;
import java.io.*;
import java.nio.file.*;

public class RuleCompilationException extends RuntimeException
{
	public RuleCompilationException(String msg)
	{
		super(msg);
	}
}