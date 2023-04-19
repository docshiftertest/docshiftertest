package com.docshifter.core.utils;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Objects;
import java.util.PrimitiveIterator;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Log4j2
public class ConfigUtilsTest {
	private void compareStreams(IntStream actual, IntStream... expected) {
		IntStream combinedExpected = Arrays.stream(expected).flatMapToInt(s -> s);
		PrimitiveIterator.OfInt actualIt = actual.iterator(), expectedIt = combinedExpected.iterator();
		StringBuilder actualSb = new StringBuilder("["), expectedSb = new StringBuilder("[");
		int actualSize = 0, expectedSize = 0;
		boolean equals = true;
		while (actualIt.hasNext() || expectedIt.hasNext()) {
			Integer nextActual = null, nextExpected = null;
			if (actualIt.hasNext()) {
				nextActual = actualIt.next();
				actualSize++;
				actualSb.append(nextActual).append(", ");
			}
			if (expectedIt.hasNext()) {
				nextExpected = expectedIt.next();
				expectedSize++;
				expectedSb.append(nextExpected).append(", ");
			}
			if (!Objects.equals(nextActual, nextExpected)) {
				equals = false;
			}
		}
		if (actualSb.length() > 3) {
			actualSb.setLength(actualSb.length() - 2);
		}
		actualSb.append(']');
		if (expectedSb.length() > 3) {
			expectedSb.setLength(expectedSb.length() - 2);
		}
		expectedSb.append(']');
		assertTrue(equals, """
				Streams are mismatched. Expected size = %d, actual size = %d.
				Expected: %s
				Actual:   %s""".formatted(expectedSize, actualSize, expectedSb, actualSb));
	}

	private IntStream reverseRangeClosed(int from, int to) {
		return IntStream.rangeClosed(from, to)
				.map(i -> to - i + from);
	}

	@Test
	void test() {
		compareStreams(ConfigUtils.getRangeStream("5-10,15-90,!40-60,45-46,50-55,!51", 100),
				IntStream.rangeClosed(5, 10),
				IntStream.rangeClosed(15, 39),
				IntStream.rangeClosed(45, 46),
				IntStream.of(50),
				IntStream.rangeClosed(52, 55),
				IntStream.rangeClosed(61, 90));
	}

	@Test
	void test_reversed() {
		compareStreams(ConfigUtils.getRangeStream("5-10,15-90,!40-60,45-46,50-55,!51", 100, ConfigUtils.REVERSED),
				reverseRangeClosed(61, 90),
				reverseRangeClosed(52, 55),
				IntStream.of(50),
				reverseRangeClosed(45, 46),
				reverseRangeClosed(15, 39),
				reverseRangeClosed(5, 10));
	}

	@Test
	void test_inverted() {
		compareStreams(ConfigUtils.getRangeStream("5-10,15-90,!40-60,45-46,50-55,!51", 100, ConfigUtils.INVERTED),
				IntStream.rangeClosed(1, 39),
				IntStream.rangeClosed(45, 46),
				IntStream.of(50),
				IntStream.rangeClosed(52, 55),
				IntStream.rangeClosed(61, 100));
	}

	@Test
	void test_inverted_minimum() {
		compareStreams(ConfigUtils.getRangeStream("!1", 5),
				IntStream.rangeClosed(2, 5));
	}

	@Test
	void test_inverted_empty() {
		compareStreams(ConfigUtils.getRangeStream("!1,!2,!3", 3));
	}
}
