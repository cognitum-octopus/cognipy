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

public class MathBinary extends org.apache.jena.reasoner.rulesys.builtins.BaseBuiltin
{
	@Override
	public String getName()
	{
		return "mathBinary";
	}

	@Override
	public int getArgLength()
	{
		return 4;
	}

	@Override
	public boolean bodyCall(org.apache.jena.graph.Node[] args, int length, RuleContext context)
	{
		checkArgs(length, context);

		String kind = getArg(0, args, context).getLiteral().toString();

		org.apache.jena.graph.Node n0 = getArg(1, args, context);
		if (kind.equals("subtract"))
		{
			org.apache.jena.graph.Node n1 = getArg(2, args, context);
			org.apache.jena.graph.Node n2 = getArg(3, args, context);

			Object p1 = RuleExtensions.getValFromJenaLiteral(n1.getLiteralValue());
			if (!RuleExtensions.isInteger(p1) && !RuleExtensions.isDouble(p1))
			{
				return false;
			}

			Object p2 = RuleExtensions.getValFromJenaLiteral(n2.getLiteralValue());
			if (!RuleExtensions.isInteger(p2) && !RuleExtensions.isDouble(p2))
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
				if (RuleExtensions.isInteger(p0) && RuleExtensions.isInteger(p1) && RuleExtensions.isInteger(p2))
				{
					return (Long)p0 == (Long)p1 - (Long)p2;
				}
				else
				{
					return (Double)p0 == (Double)p1 - (Double)p2;
				}
			}
			else
			{
				if (RuleExtensions.isInteger(p1))
				{
					return context.getEnv().bind(n0, Util.makeLongNode((Long)p1 - (Long)p2));
				}
				else
				{
					return context.getEnv().bind(n0, Util.makeDoubleNode((Double)p1 - (Double)p2));
				}
			}
		}
		else if (kind.equals("divide"))
		{
			org.apache.jena.graph.Node n1 = getArg(2, args, context);
			org.apache.jena.graph.Node n2 = getArg(3, args, context);

			Object p1 = RuleExtensions.getValFromJenaLiteral(n1.getLiteralValue());
			if (!RuleExtensions.isInteger(p1) && !RuleExtensions.isDouble(p1))
			{
				return false;
			}

			Object p2 = RuleExtensions.getValFromJenaLiteral(n2.getLiteralValue());
			if (!RuleExtensions.isInteger(p2) && !RuleExtensions.isDouble(p2))
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
				return (Double)p0 == (Double)p1 / (Double)p2;
			}
			else
			{
				return context.getEnv().bind(n0, Util.makeDoubleNode((Double)p1 / (Double)p2));
			}
		}
		else if (kind.equals("power"))
		{
			org.apache.jena.graph.Node n1 = getArg(2, args, context);
			org.apache.jena.graph.Node n2 = getArg(3, args, context);

			Object p1 = RuleExtensions.getValFromJenaLiteral(n1.getLiteralValue());
			if (!RuleExtensions.isInteger(p1) && !RuleExtensions.isDouble(p1))
			{
				return false;
			}

			Object p2 = RuleExtensions.getValFromJenaLiteral(n2.getLiteralValue());
			if (!RuleExtensions.isInteger(p2) && !RuleExtensions.isDouble(p2))
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
				return (Double)p0 == Math.pow((Double)p1, (Double)p2);
			}
			else
			{
				return context.getEnv().bind(n0, Util.makeDoubleNode(Math.pow((Double)p1, (Double)p2)));
			}
		}
		else if (kind.equals("int-divide"))
		{
			org.apache.jena.graph.Node n1 = getArg(2, args, context);
			org.apache.jena.graph.Node n2 = getArg(3, args, context);

			Object p1 = RuleExtensions.getValFromJenaLiteral(n1.getLiteralValue());
			if (!RuleExtensions.isInteger(p1))
			{
				return false;
			}

			Object p2 = RuleExtensions.getValFromJenaLiteral(n2.getLiteralValue());
			if (!RuleExtensions.isInteger(p2))
			{
				return false;
			}

			if (n0.isLiteral())
			{
				Object p0 = RuleExtensions.getValFromJenaLiteral(n0.getLiteralValue());
				if (!RuleExtensions.isInteger(p0))
				{
					return false;
				}
				return (Long)p0 == (Long)p1 / (Long)p2;
			}
			else
			{
				return context.getEnv().bind(n0, Util.makeLongNode((Long)p1 / (Long)p2));
			}
		}
		else if (kind.equals("modulo"))
		{
			org.apache.jena.graph.Node n1 = getArg(2, args, context);
			org.apache.jena.graph.Node n2 = getArg(3, args, context);

			Object p1 = RuleExtensions.getValFromJenaLiteral(n1.getLiteralValue());
			if (!RuleExtensions.isInteger(p1))
			{
				return false;
			}

			Object p2 = RuleExtensions.getValFromJenaLiteral(n2.getLiteralValue());
			if (!RuleExtensions.isInteger(p2))
			{
				return false;
			}

			if (n0.isLiteral())
			{
				Object p0 = RuleExtensions.getValFromJenaLiteral(n0.getLiteralValue());
				if (!RuleExtensions.isInteger(p0))
				{
					return false;
				}
				return (Long)p0 == (Long)p1 % (Long)p2;
			}
			else
			{
				return context.getEnv().bind(n0, Util.makeLongNode((Long)p1 % (Long)p2));
			}
		}
		else if (kind.equals("round-half-to-even"))
		{
			org.apache.jena.graph.Node n1 = getArg(2, args, context);
			org.apache.jena.graph.Node n2 = getArg(3, args, context);

			Object p1 = RuleExtensions.getValFromJenaLiteral(n1.getLiteralValue());
			if (!RuleExtensions.isDouble(p1) && !RuleExtensions.isInteger(p1))
			{
				return false;
			}

			Object p2 = RuleExtensions.getValFromJenaLiteral(n2.getLiteralValue());
			if (!RuleExtensions.isInteger(p2))
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
					return (Long)p0 == FloatingPointToInteger.ToInt64(RoundHalfToEven((Double)p1, (Integer)p2));
				}
				else
				{
					return (Double)p0 == RoundHalfToEven((Double)p1, (Integer)p2);
				}
			}
			else
			{
				if (RuleExtensions.isInteger(p1))
				{
					return context.getEnv().bind(n0, Util.makeLongNode(FloatingPointToInteger.ToInt64(RoundHalfToEven((Double)p1, (Integer)p2))));
				}
				else
				{
					return context.getEnv().bind(n0, Util.makeDoubleNode(RoundHalfToEven((Double)p1, (Integer)p2)));
				}
			}
		}
		else
		{
			throw new BuiltinException(this, context, "Unimplemented kind of BuiltIn " + getName() + ":" + kind);
		}
	}

	private static double RoundHalfToEven(double x, int precision)
	{
		if (precision >= 0)
		{
			return Math.Round(x, precision, MidpointRounding.ToEven);
		}
		else
		{
			double fac = Math.pow(10.0, -precision);
			return fac * Math.Round(x / fac, 0, MidpointRounding.ToEven);
		}
	}
}