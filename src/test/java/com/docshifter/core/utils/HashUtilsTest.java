package com.docshifter.core.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

public class HashUtilsTest {
	@Test
	public void calculateHash_md5() {
		String hash = HashUtils.calculateHash(Paths.get("target/test-classes/attachment.txt"), "md5");
		Assertions.assertEquals("4d4d5e12d2e5d2394543d18653b6ccc7", hash);
	}

	@Test
	public void calculateHash_sha1() {
		String hash = HashUtils.calculateHash(Paths.get("target/test-classes/attachment.txt"), "sha 1");
		Assertions.assertEquals("9db729bede8d46beb96945b11269f4849e1f96a9", hash);
	}

	@Test
	public void calculateHash_sha256() {
		String hash = HashUtils.calculateHash(Paths.get("target/test-classes/attachment.txt"), "sHa256");
		Assertions.assertEquals("8634a7c49a935d848e3b912e9dcdf22ec3ce67797ab4b6dda1f1368eeb8f6181", hash);
	}

	@Test
	public void calculateHash_sha512() {
		String hash = HashUtils.calculateHash(Paths.get("target/test-classes/attachment.txt"), "shA- 512");
		Assertions.assertEquals("8882e3133701e3c818714f625066e8320acf0edc561736ef008d13f5d4ae37b1ac152fede929793bf448b7aff223faa322f5ccfd0171dca13618380bc04a05c7", hash);
	}

	@Test
	public void calculateHash_invalidPath() {
		String hash = HashUtils.calculateHash(Paths.get("target/test-classes/idonot.exist"), "md5");
		Assertions.assertNull(hash);
	}

	@Test
	public void calculateHash_invalidDigestMethod() {
		String hash = HashUtils.calculateHash(Paths.get("target/test-classes/attachment.txt"), "whatever");
		Assertions.assertNull(hash);
	}
}
