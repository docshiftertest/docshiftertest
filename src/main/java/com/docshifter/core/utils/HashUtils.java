package com.docshifter.core.utils;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Provides common methods for hashing (for integrity checking).
 */
@Log4j2
public final class HashUtils {
	private static final Pattern DIGEST_PATTERN = Pattern.compile("[\\s\\-]");
	private static final Map<String, String> digestMap = fillDigestMap("MD5", "SHA-1", "SHA-256", "SHA-512");

	private HashUtils() {}

	private static Map<String, String> fillDigestMap(String... digests) {
		return Arrays.stream(digests).collect(
				Collectors.toMap(digest -> DIGEST_PATTERN.matcher(digest).replaceAll("").toLowerCase(),
				digest -> digest));
	}

	/**
	 * Calculates the hash of a file located at a Path.
	 * @param inFilePath The Path to check.
	 * @param digestMethod The digest method to use. MD5 and SHA variants are supported.
	 * @return The calculated hash for the provided file. Null if the file couldn't be read or if the digest method is
	 * invalid.
	 */
	public static String calculateHash(Path inFilePath, String digestMethod) {

		String digestMethodToUse;

		if (StringUtils.isBlank(digestMethod)) {
			log.error("digestMethod cannot be NULL or empty! Returning NULL hash...");
			return null;
		}

		digestMethod = DIGEST_PATTERN.matcher(digestMethod).replaceAll("").toLowerCase();
		digestMethodToUse = digestMap.get(digestMethod);
		if (digestMethodToUse == null) {
			log.error("digestMethod is not available: {}", digestMethod);
			return null;
		}

		try {
			MessageDigest digest = MessageDigest.getInstance(digestMethodToUse);
			return getFileChecksum(digest, inFilePath.toFile());
		}
		catch (IOException | NoSuchAlgorithmException e) {
			log.error(e.getClass().getSimpleName() + ": " + digestMethod, e);
		}

		return null;
	}

	private static String getFileChecksum(MessageDigest digest, File file) throws IOException {
		//Get file input stream for reading the file content
		try (FileInputStream fis = new FileInputStream(file)) {
			//Create byte array to read data in chunks
			byte[] byteArray = new byte[1024];
			int bytesCount;

			//Read file data and update in message digest
			while ((bytesCount = fis.read(byteArray)) != -1) {
				digest.update(byteArray, 0, bytesCount);
			}
		}

		//Get the hash's bytes
		byte[] bytes = digest.digest();

		//This bytes[] has bytes in decimal format;
		//Convert it to hexadecimal format
		StringBuilder sb = new StringBuilder();
		for (byte aByte : bytes) {
			sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
		}

		//return complete hash
		return sb.toString();
	}
}
