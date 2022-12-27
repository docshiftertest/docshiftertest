/**
 * 
 */
package com.docshifter.core.security;

import java.lang.reflect.Field;
import java.util.Map;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import com.docshifter.core.config.entities.Parameter;
import com.docshifter.core.security.utils.EncryptionUtils;
import com.docshifter.core.security.utils.SecurityProperties;
import com.docshifter.core.security.utils.SecurityUtils;

/**
 * Utility to decrypt the fields annotated with {@link Encrypted}.
 * 
 * @author Created by juan.marques on 09/12/2019.
 */
@Component
@Log4j2
public class FieldDecrypter {

	public void decrypt(Object entity) {
		ReflectionUtils.doWithFields(entity.getClass(), field -> decryptField(field, entity),
				EncryptionUtils::isFieldEncrypted);
	}

	@SuppressWarnings("unchecked")
	private void decryptField(Field field, Object entity) {

		field.setAccessible(true);

		Object encryptedMessage = ReflectionUtils.getField(field, entity);

		// No need to decrypt null values
		if (encryptedMessage == null) {
			log.debug("Not decrypting NULL value for field {}", field.getName());
			return;
		}

		if (encryptedMessage instanceof Map) {
			if (field.getName().equalsIgnoreCase(SecurityProperties.MODULE_PARAMETER_VALUES.getValue())) {
				SecurityUtils.readParametersThenDecrypt((Map<Parameter, String>) encryptedMessage,
						SecurityProperties.TYPE_PASSWORD.getValue(), entity.getClass());
			}
		} else {
			if (!(encryptedMessage instanceof String)) {
				throw new IllegalStateException("Encrypted annotation was used on a non-String field");
			}
			ReflectionUtils.setField(field, entity,
					SecurityUtils.decryptMessage(encryptedMessage.toString(),
							SecurityProperties.DEFAULT_ALGORITHM.getValue(), SecurityProperties.SECRET.getValue(),
							entity.getClass()));
		}

	}
}
