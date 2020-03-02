package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

public interface ISwrlObject
{
	Object accept(IVisitor v);
}