package cognipy.common;

import cognipy.*;

public final class MD5
{
	/** 
	 Calculates a MD5 hash from the given string and uses the given
	 encoding.
	 
	 @param Input Input string
	 @param UseEncoding Encoding method
	 @return MD5 computed string
	*/
	private static String Calculate(String Input, Encoding UseEncoding)
	{
		System.Security.Cryptography.MD5CryptoServiceProvider CryptoService;
		CryptoService = new System.Security.Cryptography.MD5CryptoServiceProvider();

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: byte[] InputBytes = UseEncoding.GetBytes(Input);
		byte[] InputBytes = UseEncoding.GetBytes(Input);
		InputBytes = CryptoService.ComputeHash(InputBytes);
		return BitConverter.toString(InputBytes).replace("-", "");
	}

	/** 
	 Calculates a MD5 hash from the given string. 
	 (By using the default encoding)
	 
	 @param Input Input string
	 @return MD5 computed string
	*/
	public static String Calculate(String Input)
	{
		// That's just a shortcut to the base method
		return Calculate(Input, System.Text.Encoding.UTF8);
	}
}