package cognipy.cnl;

import cognipy.*;
import java.util.*;
import java.time.*;

public class W3CAnnotation implements IEquatable<W3CAnnotation>, IEqualityComparer<W3CAnnotation>, INotifyPropertyChanged
{
	public W3CAnnotation(boolean isDL)
	{
		this.setIsDL(isDL);
	}

	private String _type;
	public final String getType()
	{
		return _type;
	}
	public final void setType(String value)
	{
		_type = value;
	}

	private Object _value;
	public final Object getFormattedValue()
	{
		if (_value instanceof String)
		{
			return System.Net.WebUtility.HtmlDecode(_value.toString());
		}
		else
		{
			return _value;
		}
	}
	public final Object getValue()
	{
		return _value;
	}
	public final void setValue(Object value)
	{
		_value = value;
		parseIfString();
	}
	// TODO ALESSANDRO this is not solving completely the problem.... probably to solve it completely we will need to change the way in which the value is taken from OWL and when a new annotation is entered... is it enough to deal
	// with it in the appendAnnotation method? Probably not....
	private void parseIfString()
	{
		if (_value instanceof String)
		{
			String val = (String)_value;
			_value = val.replace("\n", "");
			if (val.startsWith("'") && val.endsWith("'"))
			{
				_value = val.substring(1).substring(0, val.length() - (1 + 1)); //remove starting and trailing single quotes.
			}
		}
	}
	private String _language = null;
	public final String getLanguage()
	{
		return _language;
	}
	public final void setLanguage(String value)
	{
		_language = value;
	}

	private boolean _external = false;
	public final boolean getExternal()
	{
		return _external;
	}
	public final void setExternal(boolean value)
	{
		_external = value;
	}
	@Override
	public String toString()
	{
		String ret = getType() + " ";
		if (getValue() instanceof Double)
		{
			ret += (Double)getValue();
		}
		else if (getValue() instanceof Long)
		{
			ret += (Long)getValue();
		}
		else if (getValue() instanceof LocalDateTime)
		{
			ret += (LocalDateTime)getValue();
		}
		else if (getValue() instanceof String)
		{
			String val = (String)getValue();
			if (!tangible.StringHelper.isNullOrWhiteSpace(val))
			{
				if (!val.startsWith("'"))
				{
					val = "'" + val;
				}
				if (!val.endsWith("'"))
				{
					val += "'";
				}
			}
			else
			{
				val += "''";
			}
			ret += val;
		}

		if (!tangible.StringHelper.isNullOrEmpty(getLanguage()))
		{
			ret += "@" + getLanguage();
		}
		return ret;
	}

	private boolean isDL;
	public final boolean getIsDL()
	{
		return isDL;
	}
	private void setIsDL(boolean value)
	{
		isDL = value;
	}

	public final boolean equals(W3CAnnotation other)
	{
		String otVal = other.getValue().toString().replace("'", "");
		String thisVal = this.getValue().toString().replace("'", "");

		if (this.getLanguage().equals(other.getLanguage()) && this.getType().equals(other.getType()) && otVal.equals(thisVal))
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	public final boolean equals(W3CAnnotation x, W3CAnnotation y)
	{
		if (x.equals(y))
		{
			return true;
		}

		return false;
	}

	public final int hashCode(W3CAnnotation obj)
	{
		//int hash = obj.Type.GetHashCode() + obj.Value.GetHashCode();
		//if (!String.IsNullOrEmpty(obj.Language))
		//    hash += obj.Language.GetHashCode();

		return 0;
	}

	// ESCHOI TEST
	public tangible.Event<PropertyChangedEventHandler> PropertyChanged = new tangible.Event<PropertyChangedEventHandler>();
	// Create the OnPropertyChanged method to raise the event
	protected final void OnPropertyChanged(String name)
	{
		PropertyChangedEventHandler handler = (Object sender, System.ComponentModel.PropertyChangedEventArgs e) -> PropertyChanged.invoke(sender, e);
		if (handler != null)
		{
			handler.invoke(this, new PropertyChangedEventArgs(name));
		}
	}
}