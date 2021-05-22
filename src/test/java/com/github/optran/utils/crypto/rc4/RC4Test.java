package com.github.optran.utils.crypto.rc4;

import static com.github.optran.utils.StringUtils.byteArrToHex;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.util.Iterator;
import java.util.Random;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class RC4Test {
	@Test
	public void rc4Test() {
		// Following key's and keystreams are from the Wiki page
		testRc4KeyStream("Key", "EB9F7781B734CA72A719");
		testRc4KeyStream("Wiki", "6044DB6D41B7");
		testRc4KeyStream("Secret", "04D46B053CA87B59");
		
		//Following are the border conditions
		testRc4KeyStream((String)null, "0205070D0D171F28");
		testRc4KeyStream("", "0205070D0D171F28");
		
		testRc4KeyStream((byte[])null, "0205070D0D171F28");
		testRc4KeyStream("".getBytes(), "0205070D0D171F28");
		
		byte[]bigKey = new byte[300];
		for (int i = 0; i < bigKey.length; i++) {
			bigKey[i] = (byte)(i%256);
		}
		testRc4KeyStream(bigKey, "7D6EE463FAAF29B3");
	}

	public void testRc4KeyStream(String key, String keyStream) {
		RC4 rc4 = new RC4();
		rc4.seed(key);
		byte[] data = new byte[keyStream.length() / 2];
		for (int i = 0; i < data.length; i++) {
			data[i] = rc4.next();
		}
		assertEquals(keyStream, byteArrToHex(data));
	}
	
	public void testRc4KeyStream(byte[]key, String keyStream) {
		RC4 rc4 = new RC4();
		rc4.seed(key);
		byte[] data = new byte[keyStream.length() / 2];
		for (int i = 0; i < data.length; i++) {
			data[i] = rc4.next();
		}
		assertEquals(keyStream, byteArrToHex(data));
	}
}
