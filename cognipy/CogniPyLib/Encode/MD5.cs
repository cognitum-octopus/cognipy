using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Collections;
using System.IO;

namespace Ontorion.Common
{
    internal static class MD5
    {
        /// <summary>
        /// Calculates a MD5 hash from the given string and uses the given
        /// encoding.
        /// </summary>
        /// <param name="Input">Input string</param>
        /// <param name="UseEncoding">Encoding method</param>
        /// <returns>MD5 computed string</returns>
        private static string Calculate(string Input, Encoding UseEncoding)
        {
            System.Security.Cryptography.MD5CryptoServiceProvider CryptoService;
            CryptoService = new System.Security.Cryptography.MD5CryptoServiceProvider();

            byte[] InputBytes = UseEncoding.GetBytes(Input);
            InputBytes = CryptoService.ComputeHash(InputBytes);
            return BitConverter.ToString(InputBytes).Replace("-", "");
        }

        /// <summary>
        /// Calculates a MD5 hash from the given string. 
        /// (By using the default encoding)
        /// </summary>
        /// <param name="Input">Input string</param>
        /// <returns>MD5 computed string</returns>
        public static string Calculate(string Input)
        {
            // That's just a shortcut to the base method
            return Calculate(Input, System.Text.Encoding.UTF8);
        }
    }
}
