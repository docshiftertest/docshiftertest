/**
 * 
 */
package com.docshifter.security.utils;

import java.util.Map;

import org.apache.log4j.Logger;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.jasypt.salt.RandomSaltGenerator;

import com.docshifter.core.config.domain.Parameter;
import com.ulisesbocchio.jasyptspringboot.encryptor.SimplePBEByteEncryptor;
import com.ulisesbocchio.jasyptspringboot.encryptor.SimplePBEStringEncryptor;

/**
 * Utility to encrypt / decrypt messages.
 * @author Created by juan.marques on 04/12/2019.
 */
public class SecurityUtils {

	private static final Logger logger = Logger.getLogger(SecurityUtils.class);

	/**
	 * Utility to decrypt message. If no algorithm is provided will be applied the
	 * PBEWITHHMACSHA384ANDAES_256.
	 * 
	 * @param <T>              the class to be logged.
	 * 
	 * @param encryptedMessage the encrypted message.
	 * @param algorithm        the algorithm to decrypt the message.
	 * @param secret           the given secret to decrypt
	 * @param logClass         the class to be logged.
	 * @return decrypted message.
	 */
	public static <T> String decryptMessage(String encryptedMessage, String algorithm, String secret,
			Class<? extends Object> logClass) {

		SimplePBEByteEncryptor delegate = new SimplePBEByteEncryptor();

		SimplePBEStringEncryptor decrypt = new SimplePBEStringEncryptor(delegate);

		delegate.setSaltGenerator(new RandomSaltGenerator());
		delegate.setIterations(1000);

		String decryptedMessage = null;

		if (algorithm != null && !algorithm.isEmpty()) {
			delegate.setAlgorithm(algorithm);
		} else {
			logger.debug("Using the default algorithm...");
			delegate.setAlgorithm(SecurityProperties.DEFAULT_ALGORITHM.getValue());
		}

		if (secret != null && !secret.isEmpty()) {
			delegate.setPassword(secret);
		} else {
			// Get the vm given secret
			logger.debug("Getting vm argument to start the decryption...");
			delegate.setPassword(System.getProperty(SecurityProperties.JASYPT_VM_ARGUMENT.getValue()));
		}

		try {
			logger.debug("Starting decryption for " + logClass);
			decryptedMessage = decrypt.decrypt(encryptedMessage);

		} catch (Exception e) {
			if (e instanceof EncryptionOperationNotPossibleException || e instanceof IllegalArgumentException
					|| e instanceof NegativeArraySizeException) {
				logger.debug("The password is in plain text...");
				decryptedMessage = encryptedMessage;
			} else {
				logger.info(e.getStackTrace());
			}
		}
		logger.debug("Decryption completed for " + logClass);
		return decryptedMessage;
	}

	/**
	 * Utility to encrypt message. If no algorithm is provided will be applied the
	 * PBEWITHHMACSHA384ANDAES_256.
	 * 
	 * @param <T>       the class to be logged.
	 * @param algorithm the algorithm to decrypt the message.
	 * @param secret    the given secret to decrypt
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

		String encryptedMessage = null;

		if (algorithm != null && !algorithm.isEmpty()) {
			delegate.setAlgorithm(algorithm);
		} else {
			logger.debug("Using the default algorithm...");
			delegate.setAlgorithm(SecurityProperties.DEFAULT_ALGORITHM.getValue());
		}

		if (secret != null && !secret.isEmpty()) {
			delegate.setPassword(secret);
		} else {
			// Get the vm given secret
			logger.debug("Getting vm argument to start the decryption...");
			delegate.setPassword(System.getProperty(SecurityProperties.JASYPT_VM_ARGUMENT.getValue()));
		}

		try {
			logger.debug("Starting encryption for " + logClass);
			encryptedMessage = encrypt.encrypt(message);
		} catch (Exception e) {
			logger.info(e);
		}
		logger.debug("Encryption completed for " + logClass);

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
			param.entrySet().stream().filter(p -> p.getKey().getType().equalsIgnoreCase(type)).forEach((entry) -> {
				entry.setValue(encryptMessage(entry.getValue(), SecurityProperties.DEFAULT_ALGORITHM.getValue(),
						SecurityProperties.SECRET.getValue(), logClass));
			});
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
			Class<? extends Object> logClass) {

		if (param != null) {
			param.entrySet().stream().filter(p -> p.getKey().getType().equalsIgnoreCase(type)).forEach((entry) -> {
				entry.setValue(decryptMessage(entry.getValue(), SecurityProperties.DEFAULT_ALGORITHM.getValue(),
						SecurityProperties.SECRET.getValue(), logClass));
			});
		}
	}
}
