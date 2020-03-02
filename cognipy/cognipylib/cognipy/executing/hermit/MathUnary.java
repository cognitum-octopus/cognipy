package cognipy.executing.hermit;

import cognipy.cnl.dl.*;
import cognipy.configuration.*;
import org.apache.jena.graph.*;
import org.apache.jena.graph.impl.*;
import org.apache.jena.reasoner.rulesys.*;
import org.apache.jena.util.*;
import cognipy.*;
import java.util.*;
import java.io.*;

public class MathUnary extends org.apache.jena.reasoner.rulesys.builtins.BaseBuiltin
{
	@Override
	public String getName()
	{
		return "mathUnary";
	}

	@Override
	public int getArgLength()
	{
		return 3;
	}
	@Override
	public boolean bodyCall(org.apache.jena.graph.Node[] args, int length, RuleContext context)
	{
		checkArgs(length, context);

		String kind = getArg(0, args, context).getLiteral().toString();

		org.apache.jena.graph.Node n0 = getArg(1, args, context);
		if (kind.equals("minus"))
		{
			org.apache.jena.graph.Node n1 = getArg(2, args, context);

			Object p1 = RuleExtensions.getValFromJenaLiteral(n1.getLiteralValue());
			if (!RuleExtensions.isInteger(p1) && !RuleExtensions.isDouble(p1))
			{
				return false;
			}

			if (n0.isLiteral())
			{
				Object p0 = RuleExtensions.getValFromJenaLiteral(n0.getLiteralValue());
				if (!RuleExtensions.isInteger(p0) && !RuleExtensions.isDouble(p0))
				{
					return false;
				}
				if (RuleExtensions.isInteger(p0) && RuleExtensions.isInteger(p1))
				{
					return (Long)p0 == -(Long)p1;
				}
				else
				{
					return (Double)p0 == -(Double)p1;
				}
			}
			else
			{
				if (RuleExtensions.isInteger(p1))
				{
					return context.getEnv().bind(n0, Util.makeLongNode(-(Long)p1));
				}
				else
				{
					return context.getEnv().bind(n0, Util.makeDoubleNode(-(Double)p1));
				}
			}
		}
		else if (kind.equals("absolute"))
		{
			org.apache.jena.graph.Node n1 = getArg(2, args, context);

			Object p1 = RuleExtensions.getValFromJenaLiteral(n1.getLiteralValue());
			if (!RuleExtensions.isInteger(p1) && !RuleExtensions.isDouble(p1))
			{
				return false;
			}

			if (n0.isLiteral())
			{
				Object p0 = RuleExtensions.getValFromJenaLiteral(n0.getLiteralValue());
				if (!RuleExtensions.isInteger(p0) && !RuleExtensions.isDouble(p0))
				{
					return false;
				}
				if (RuleExtensions.isInteger(p0) && RuleExtensions.isInteger(p1))
				{
					return (Long)p0 == Math.abs((Long)p1);
				}
				else
				{
					return (Double)p0 == Math.abs((Double)p1);
				}
			}
			else
			{
				if (RuleExtensions.isInteger(p1))
				{
					return context.getEnv().bind(n0, Util.makeLongNode(Math.abs((Long)p1)));
				}
				else
				{
					return context.getEnv().bind(n0, Util.makeDoubleNode(Math.abs((Double)p1)));
				}
			}
		}
		else if (kind.equals("ceiling"))
		{
			org.apache.jena.graph.Node n1 = getArg(2, args, context);

			Object p1 = RuleExtensions.getValFromJenaLiteral(n1.getLiteralValue());
			if (!RuleExtensions.isInteger(p1) && !RuleExtensions.isDouble(p1))
			{
				return false;
			}

			if (n0.isLiteral())
			{
				Object p0 = RuleExtensions.getValFromJenaLiteral(n0.getLiteralValue());
				if (!RuleExtensions.isInteger(p0) && !RuleExtensions.isDouble(p0))
				{
					return false;
				}
				if (RuleExtensions.isInteger(p0) && RuleExtensions.isInteger(p1))
				{
					return (Long)p0 == (Long)p1;
				}
				else
				{
					return (Double)p0 == Math.ceil((Double)p1);
				}
			}
			else
			{
				if (RuleExtensions.isInteger(p1))
				{
					return context.getEnv().bind(n0, Util.makeLongNode((Long)p1));
				}
				else
				{
					return context.getEnv().bind(n0, Util.makeDoubleNode(Math.ceil((Double)p1)));
				}
			}
		}
		else if (kind.equals("floor"))
		{
			org.apache.jena.graph.Node n1 = getArg(2, args, context);

			Object p1 = RuleExtensions.getValFromJenaLiteral(n1.getLiteralValue());
			if (!RuleExtensions.isInteger(p1) && !RuleExtensions.isDouble(p1))
			{
				return false;
			}

			if (n0.isLiteral())
			{
				Object p0 = RuleExtensions.getValFromJenaLiteral(n0.getLiteralValue());
				if (!RuleExtensions.isInteger(p0) && !RuleExtensions.isDouble(p0))
				{
					return false;
				}
				if (RuleExtensions.isInteger(p0) && RuleExtensions.isInteger(p1))
				{
					return (Long)p0 == (Long)p1;
				}
				else
				{
					return (Double)p0 == Math.floor((Double)p1);
				}
			}
			else
			{
				if (RuleExtensions.isInteger(p1))
				{
					return context.getEnv().bind(n0, Util.makeLongNode((Long)p1));
				}
				else
				{
					return context.getEnv().bind(n0, Util.makeDoubleNode(Math.floor((Double)p1)));
				}
			}
		}
		else if (kind.equals("round"))
		{
			org.apache.jena.graph.Node n1 = getArg(2, args, context);

			Object p1 = RuleExtensions.getValFromJenaLiteral(n1.getLiteralValue());
			if (!RuleExtensions.isInteger(p1) && !RuleExtensions.isDouble(p1))
			{
				return false;
			}

			if (n0.isLiteral())
			{
				Object p0 = RuleExtensions.getValFromJenaLiteral(n0.getLiteralValue());
				if (!RuleExtensions.isInteger(p0) && !RuleExtensions.isDouble(p0))
				{
					return false;
				}
				if (RuleExtensions.isInteger(p0) && RuleExtensions.isInteger(p1))
				{
					return (Long)p0 == (Long)p1;
				}
				else
				{
					return (Double)p0 == Math.rint((Double)p1);
				}
			}
			else
			{
				if (RuleExtensions.isInteger(p1))
				{
					return context.getEnv().bind(n0, Util.makeLongNode((Long)p1));
				}
				else
				{
					return context.getEnv().bind(n0, Util.makeDoubleNode(Math.rint((Double)p1)));
				}
			}
		}
		else if (kind.equals("sine"))
		{
			org.apache.jena.graph.Node n1 = getArg(2, args, context);

			Object p1 = RuleExtensions.getValFromJenaLiteral(n1.getLiteralValue());
			if (!RuleExtensions.isInteger(p1) && !RuleExtensions.isDouble(p1))
			{
				return false;
			}

			if (n0.isLiteral())
			{
				Object p0 = RuleExtensions.getValFromJenaLiteral(n0.getLiteralValue());
				if (!RuleExtensions.isInteger(p0) && !RuleExtensions.isDouble(p0))
				{
					return false;
				}
				return (Double)p0 == Math.sin((Double)p1);
			}
			else
			{
				return context.getEnv().bind(n0, Util.makeDoubleNode(Math.sin((Double)p1)));
			}
		}
		else if (kind.equals("cosine"))
		{
			org.apache.jena.graph.Node n1 = getArg(2, args, context);

			Object p1 = RuleExtensions.getValFromJenaLiteral(n1.getLiteralValue());
			if (!RuleExtensions.isInteger(p1) && !RuleExtensions.isDouble(p1))
			{
				return false;
			}

			if (n0.isLiteral())
			{
				Object p0 = RuleExtensions.getValFromJenaLiteral(n0.getLiteralValue());
				if (!RuleExtensions.isInteger(p0) && !RuleExtensions.isDouble(p0))
				{
					return false;
				}
				return (Double)p0 == Math.cos((Double)p1);
			}
			else
			{
				return context.getEnv().bind(n0, Util.makeDoubleNode(Math.cos((Double)p1)));
			}
		}
		else if (kind.equals("tangent"))
		{
			org.apache.jena.graph.Node n1 = getArg(2, args, context);

			Object p1 = RuleExtensions.getValFromJenaLiteral(n1.getLiteralValue());
			if (!RuleExtensions.isInteger(p1) && !RuleExtensions.isDouble(p1))
			{
				return false;
			}

			if (n0.isLiteral())
			{
				Object p0 = RuleExtensions.getValFromJenaLiteral(n0.getLiteralValue());
				if (!RuleExtensions.isInteger(p0) && !RuleExtensions.isDouble(p0))
				{
					return false;
				}
				return (Double)p0 == Math.tan((Double)p1);
			}
			else
			{
				return context.getEnv().bind(n0, Util.makeDoubleNode(Math.tan((Double)p1)));
			}
		}
		else
		{
			throw new BuiltinException(this, context, "Unimplemented kind of BuiltIn " + getName() + ":" + kind);
		}
	}
}