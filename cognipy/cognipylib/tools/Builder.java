package tools;

import java.util.*;
import java.io.*;

@FunctionalInterface
public interface Builder
{
	void invoke(Transition t);
}