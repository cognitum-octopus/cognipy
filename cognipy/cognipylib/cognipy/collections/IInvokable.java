package cognipy.collections;

import cognipy.*;
import java.util.*;

/** 
 SortedCollection which implements INotifyCollectionChanged interface and so can be used
 in WPF applications as the source of the binding.
 
 <author>consept</author>
*/
public interface IInvokable
{
	void Invoke(tangible.Action0Param act);
}