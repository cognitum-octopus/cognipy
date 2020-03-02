package cognipy.cnl.dl;

import cognipy.*;
import cognipy.cnl.*;
import java.util.*;

public interface Populator
{
	java.lang.Iterable<Map.Entry<String, String>> Populate(String sentenceBeginning, String str, ArrayList<String> forms, int max);
}