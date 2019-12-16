/**
 * 
 */
package com.docshifter.security.utils;

import java.lang.reflect.Field;

import org.springframework.core.annotation.AnnotationUtils;

import com.docshifter.security.Encrypted;

/**
 * Utility that takes all the fields which are annotated with @Encrypted and
 * then gets the field value from the state parameter and writes it back to the
 * state array.
 * 
 * @author Created by juan.marques on 09/12/2019.
 */
public class EncryptionUtils {

	public static boolean isFieldEncrypted(Field field) {
		return AnnotationUtils.findAnnotation(field, Encrypted.class) != null;
	}

	public static int getPropertyIndex(String name, String[] properties) {
		for (int i = 0; i < properties.length; i++) {
			if (name.equals(properties[i])) {
				return i;
			}
		}
		throw new IllegalArgumentException("No property was found for name " + name);
	}
}
