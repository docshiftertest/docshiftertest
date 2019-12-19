/**
 * 
 */
package com.docshifter.core.security.utils;

import java.lang.reflect.Field;

import org.springframework.core.annotation.AnnotationUtils;

import com.docshifter.core.security.Encrypted;

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
		for (int idx  = 0; idx  < properties.length; idx  ++) {
			if (name.equals(properties[idx ])) {
				return idx ;
			}
		}
		throw new IllegalArgumentException("No property was found for name " + name);
	}
}
