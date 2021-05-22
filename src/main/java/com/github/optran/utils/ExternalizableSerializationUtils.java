package com.github.optran.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.github.optran.utils.exceptions.RuntimeIOException;

public class ExternalizableSerializationUtils {
	public static final byte[] serializeExternalizable(Externalizable object) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			object.writeExternal(oos);
			oos.close();
			return baos.toByteArray();
		} catch (IOException e) {
			throw new RuntimeIOException(e);
		}
	}

	public static final void deserializeExternalizable(byte[]data, Externalizable deserializedObject) {
		try {
			ByteArrayInputStream bais = new ByteArrayInputStream(data);
			ObjectInputStream ois = new ObjectInputStream(bais);
			deserializedObject.readExternal(ois);
			ois.close();
		} catch (IOException e) {
			throw new RuntimeIOException(e);
		} catch (ClassNotFoundException e) {
			throw new RuntimeIOException(e);
		}
	}
}
