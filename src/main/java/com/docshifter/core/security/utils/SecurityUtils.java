package com.docshifter.core.security.utils;

import com.docshifter.core.config.entities.Parameter;
import com.ulisesbocchio.jasyptspringboot.encryptor.SimplePBEByteEncryptor;
import com.ulisesbocchio.jasyptspringboot.encryptor.SimplePBEStringEncryptor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang.StringUtils;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.jasypt.salt.RandomSaltGenerator;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * Utility to encrypt / decrypt messages.
 * 
 * @author Created by juan.marques on 04/12/2019.
 */
@Log4j2
public class SecurityUtils {
	/**
	 * Some known {@link IOException} messages that hint to us that the message is already in plaintext when trying to decrypt.
	 */
	private static final Set<String> PLAINTEXT_EX_MSG_INDICATORS = Set.of("Invalid lenByte", "Too short");

	/**
	 * Utility to decrypt message. If no algorithm is provided will be applied the
	 * PBEWITHHMACSHA384ANDAES_256.
	 * 
	 * @param <T>              the class to be logged.
	 * 
	 * @param encryptedMessage the encrypted message.
	 * @param algorithm        the algorithm to decrypt the message.
	 * @param secret           the given secret to decrypt or pass null to use the
	 *                         VM provided secret
	 * @param logClass         the class to be logged.
	 * @return decrypted message.
	 */
	public static <T> String decryptMessage(String encryptedMessage, String algorithm, String secret, T logClass) {

		SimplePBEByteEncryptor delegate = new SimplePBEByteEncryptor();

		SimplePBEStringEncryptor decrypt = new SimplePBEStringEncryptor(delegate);

		String decryptedMessage;
		// Early exit if there's nothing to decrypt
		if (StringUtils.isEmpty(encryptedMessage)) {
			return encryptedMessage;
		}

		if (StringUtils.isNotEmpty(algorithm)) {
			delegate.setAlgorithm(algorithm);
		} else {
			log.debug("Using the default algorithm...");
			delegate.setAlgorithm(SecurityProperties.DEFAULT_ALGORITHM.getValue());
		}

		if (StringUtils.isNotEmpty(secret)) {
			delegate.setPassword(secret);
		} else {
			// Get the vm given secret
			log.debug("Getting vm argument to start the decryption...");
			delegate.setPassword(System.getProperty(SecurityProperties.JASYPT_VM_ARGUMENT.getValue()));
		}

		try {
			log.debug("Starting decryption for {} ", logClass);
			decryptedMessage = decrypt.decrypt(encryptedMessage);
		} catch (EncryptionOperationNotPossibleException | IllegalArgumentException | NegativeArraySizeException e) {
			log.debug("The password is in plain text...: ", e);
			decryptedMessage = encryptedMessage;
		} catch (Exception e) {
			if (e instanceof IOException && PLAINTEXT_EX_MSG_INDICATORS.contains(e.getMessage())) {
				log.debug("The password is in plain text...: ", e);
				decryptedMessage = encryptedMessage;
			} else {
				EncryptionOperationNotPossibleException eonpe = new EncryptionOperationNotPossibleException("Occurred an error trying to decrypt " + logClass);
				eonpe.initCause(e);
				throw eonpe;
			}
		}
		log.debug("Decryption completed for {} " , logClass);
		return decryptedMessage;
	}

	/**
	 * Utility to encrypt message. If no algorithm is provided will be applied the
	 * PBEWITHHMACSHA384ANDAES_256.
	 * 
	 * @param <T>       the class to be logged.
	 * @param algorithm the algorithm to encrypt the message.
	 * @param secret    the given secret to encrypt or pass null to use the VM
	 *                  provided secret
	 * @param message   the encrypted message.
	 * @param logClass  the class to be logged.
	 * 
	 * @return encrypted message.
	 */
	public static <T> String encryptMessage(String message, String algorithm, String secret, T logClass) {

		SimplePBEByteEncryptor delegate = new SimplePBEByteEncryptor();
		SimplePBEStringEncryptor encrypt = new SimplePBEStringEncryptor(delegate);
		delegate.setSaltGenerator(new RandomSaltGenerator());
		delegate.setIterations(1000);

		boolean isAlreadyEncrypted;
		try {
			isAlreadyEncrypted = !decryptMessage(message, algorithm, secret, logClass).equals(message);
		} catch (Exception ex) {
			// If this exception occurs, check the exception handling in the decryptMessage method above!
			// If the supplied message is plaintext, then there might be a case/exception we haven't taken into account yet.
			// We wrap and rethrow an exception here because we prefer giving the user an error message over the potential to
			// double encrypt messages/passwords...
			EncryptionOperationNotPossibleException eonpe = new EncryptionOperationNotPossibleException("Ran into an error while trying to check if password is already encrypted");
			eonpe.initCause(ex);
			throw eonpe;
		}

		String encryptedMessage;

		if (StringUtils.isNotEmpty(algorithm)) {
			delegate.setAlgorithm(algorithm);
		} else {
			log.debug("Using the default algorithm...");
			delegate.setAlgorithm(SecurityProperties.DEFAULT_ALGORITHM.getValue());
		}

		if (StringUtils.isNotEmpty(secret)) {
			delegate.setPassword(secret);
		} else {
			// Get the vm given secret
			log.debug("Getting vm argument to start the decryption...");
			delegate.setPassword(System.getProperty(SecurityProperties.JASYPT_VM_ARGUMENT.getValue()));
		}

		try {
			log.debug("Starting encryption for {} ",logClass);
			if (!isAlreadyEncrypted) {
				encryptedMessage = encrypt.encrypt(message);
			}
			else {
				log.debug("Message is already encrypted...");
				encryptedMessage = message;
			}
		} catch (Exception e) {
			log.error(e);
			throw new EncryptionOperationNotPossibleException("Occurred an error trying to encrypt " + logClass);
		}

		log.debug("Encryption completed for {} ", logClass);

		return encryptedMessage;

	}

	/**
	 * Encrypt the parameters.
	 * 
	 * @param <T>      the class to be logged.
	 * 
	 * @param param    the Map with {@link Parameter} as Key.
	 * @param type     the type of the parameter than you want to encrypt.
	 * @param logClass the class to be logged.
	 */
	public static <T> void readParametersThenEncrypt(Map<Parameter, String> param, String type, T logClass) {

		if (param != null) {
			param.entrySet().stream().filter(p -> p.getKey().getType().equalsIgnoreCase(type)).forEach(entry -> entry.setValue(encryptMessage(entry.getValue(), SecurityProperties.DEFAULT_ALGORITHM.getValue(),
					SecurityProperties.SECRET.getValue(), logClass)));
		}
	}

	/**
	 * Decrypt the parameters.
	 * 
	 * @param <T>      the class to be logged.
	 * 
	 * @param param    the Map with {@link Parameter} as Key.
	 * @param type     the type of the parameter than you want to decrypt.
	 * @param logClass the class to be logged.
	 */
	public static <T> void readParametersThenDecrypt(Map<Parameter, String> param, String type,
			Class<?> logClass) {

		if (param != null) {
			param.entrySet().stream().filter(p -> p.getKey().getType().equalsIgnoreCase(type)).forEach(entry -> entry.setValue(decryptMessage(entry.getValue(), SecurityProperties.DEFAULT_ALGORITHM.getValue(),
					SecurityProperties.SECRET.getValue(), logClass)));
		}
	}
}
