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
 * Utility to encrypt the fields annotated with {@link Encrypted}.
 * 
 * @author Created by juan.marques on 09/12/2019.
 */
@Component
@Log4j2
public class FieldEncrypter {

	public void encrypt(Object[] state, String[] propertyNames, Object entity) {
		ReflectionUtils.doWithFields(entity.getClass(), field -> encryptField(field, state, propertyNames, entity),
				EncryptionUtils::isFieldEncrypted);
	}

	@SuppressWarnings("unchecked")
	private void encryptField(Field field, Object[] state, String[] propertyNames, Object entity) {

		int propertyIndex = EncryptionUtils.getPropertyIndex(field.getName(), propertyNames);
		Object currentValue = state[propertyIndex];

		// No need to encrypt null values
		if (currentValue == null) {
			log.debug("Not encrypting NULL value for field {}", field.getName());
			return;
		}

		if (currentValue instanceof Map) {
			if (field.getName().equalsIgnoreCase(SecurityProperties.MODULE_PARAMETER_VALUES.getValue())) {
				SecurityUtils.readParametersThenEncrypt((Map<Parameter, String>) currentValue,
						SecurityProperties.TYPE_PASSWORD.getValue(), entity.getClass());
			}
		} else {

			if (!(currentValue instanceof String)) {
				throw new IllegalStateException("Encrypted annotation was used on a non-String field");
			}
			state[propertyIndex] = SecurityUtils.encryptMessage(currentValue.toString(),
					SecurityProperties.DEFAULT_ALGORITHM.getValue(), SecurityProperties.SECRET.getValue(),
					entity.getClass());

		}
	}
}
