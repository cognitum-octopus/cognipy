package cnl.net;

import cognipy.collections.*;
import java.util.*;

@FunctionalInterface
public interface RefreshNeededEventHandler
{
	void invoke(Object sender, EventArgs e);
}