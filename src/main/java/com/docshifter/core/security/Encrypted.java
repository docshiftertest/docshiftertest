/**
 * 
 */
package com.docshifter.core.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.docshifter.core.security.utils.SecurityUtils;

/**
 * Indicates that this property will be a candidate for encryption.
 *
 * <p>
 * The property must be {@link String} or a {@link Map} note that for use with
 * map you should look {@link EncryptionListener}
 * {@code onPreUpdateCollection()} for further example to implementation.
 * 
 * @see {@link EncryptionListener} , {@link FieldEncrypter} ,
 *      {@link FieldDecrypter} , {@link SecurityUtils}
 * @author Created by juan.marques on 09/12/2019.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Encrypted {
}
