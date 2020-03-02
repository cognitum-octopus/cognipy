package cognipy.cnl;

import cognipy.*;
import java.util.*;
import java.time.*;

@FunctionalInterface
public interface NewAnnotationSubjectHandler
{
	void invoke(Object sender);
}