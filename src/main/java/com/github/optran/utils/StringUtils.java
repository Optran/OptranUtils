/*
 * MIT License
 * 
 * Copyright (c) 2021 Ashutosh Wad
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
*/
package com.github.optran.utils;

/**
 * This class provides methods to make string manipulation easier.
 * 
 * @author Ashutosh Wad
 *
 */
public final class StringUtils {
	/**
	 * This is a null guarded trim. If the input string is null, null is returned
	 * else the result of the inbuilt trim function is returned.
	 * 
	 * @param str The string to be trimmed.
	 * @return The trimmed string value.
	 */
	public static final String trim(String str) {
		if (null == str) {
			return null;
		}
		return str.trim();
	}

	/**
	 * This is a null guarded length function that returns 0 if the input is null,
	 * and the actual length of the string if a valid string is supplied.
	 * 
	 * @param str The string whose length needs to be retrieved.
	 * @return The length of the input string.
	 */
	public static final int length(String str) {
		if (str == null) {
			return 0;
		}
		return str.length();
	}

	/**
	 * This function returns true if the trimmed length of the string provided is
	 * equal to 0.
	 * 
	 * @param str The string that needs to be checked.
	 * @return True if the string is blank, false otherwise.
	 */
	public static final boolean isBlank(String str) {
		return 0 == length(trim(str));
	}

	/**
	 * This function returns true if the trimmed length of the string provided is
	 * greater than 0.
	 * 
	 * @param str The string that needs to be checked.
	 * @return True if the string is not blank, false otherwise.
	 */
	public static final boolean isNotBlank(String str) {
		return length(trim(str)) > 0;
	}
}
