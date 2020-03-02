package cognipy.cnl.en;

import cognipy.cnl.dl.*;
import cognipy.*;
import cognipy.cnl.*;
import java.util.*;

public class ENSerializeException extends RuntimeException
{

	public ENSerializeException(tools.SYMBOL node, String message)
	{
		super(message);
		//    base(message/* + (node!=null?" in :"+node.toString():"")*/);
		//    this.node = node;
	}
}