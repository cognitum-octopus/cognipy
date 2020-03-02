package tools;

import java.util.*;
import java.io.*;

// Support for runtime object creation

@FunctionalInterface
public interface TCreator
{
	Object invoke(Lexer yyl);
}