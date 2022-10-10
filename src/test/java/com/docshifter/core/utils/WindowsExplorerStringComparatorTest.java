package com.docshifter.core.utils;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Log4j2
public class WindowsExplorerStringComparatorTest {
	private WindowsExplorerStringComparator sut;

	@BeforeEach
	public void beforeEach() {
		sut = new WindowsExplorerStringComparator();
	}

	@Test
	public void orderingSetWithNumberAtStart() {

		log.debug("orderingSetWithNumberAtStart test:");

		List<String> expectedOrder = Arrays.asList(
				"1. Test", "8. Test", "9. Test", "10. Test", "11. Test", "111. Test"
		);
		log.debug("expectedOrder: {}", expectedOrder);

		Set<String> allStrings = new TreeSet<>(sut);
		allStrings.add("9. Test");
		allStrings.add("10. Test");
		allStrings.add("8. Test");
		allStrings.add("111. Test");
		allStrings.add("1. Test");
		allStrings.add("11. Test");

		List<String> resultOrder = allStrings.stream().toList();
		log.debug("resultOrder: {}", resultOrder);

		assertEquals(6, allStrings.size(), "The size should match.");
		for (int index = 0; index < allStrings.size(); index++) {
			assertEquals(expectedOrder.get(index), resultOrder.get(index), "The result in index " + index +" should matches.");
		}
	}

	@Test
	public void orderingSetWithLettersAtStart() {

		log.debug("orderingSetWithLettersAtStart test:");

		List<String> expectedOrder = Arrays.asList(
				"Diego", "Johnno", "Juan", "Julian", "Raphael"
		);
		log.debug("expectedOrder: {}", expectedOrder);

		Set<String> allStrings = new TreeSet<>(sut);
		allStrings.add("Juan");
		allStrings.add("Diego");
		allStrings.add("Julian");
		allStrings.add("Raphael");
		allStrings.add("Johnno");

		List<String> resultOrder = allStrings.stream().toList();
		log.debug("resultOrder: {}", resultOrder);

		assertEquals(5, allStrings.size(), "The size should match.");
		for (int index = 0; index < allStrings.size(); index++) {
			assertEquals(expectedOrder.get(index), resultOrder.get(index), "The result in index " + index +" should matches.");
		}
	}

	@Test
	public void compareWithNumberAtStartTest() {
		log.debug("compareWithNumberAtStartTest: ");

		int resultForEqual = sut.compare("1001. Bookmark", "1001. Bookmark");
		int resultForLessThan = sut.compare("100. Bookmark", "1000. Bookmark");
		int resultForGreaterThan = sut.compare("1000. Bookmark", "100. Bookmark");

		assertEquals(0, resultForEqual, "The strings are equal.");
		assertEquals(-1, resultForLessThan, "The first string is less than the second.");
		assertEquals(1, resultForGreaterThan, "The first string is greater than the second.");
	}

	@Test
	public void compareWithLettersAtStartTest() {
		log.debug("compareWithLettersAtStartTest: ");

		int resultForEqual = sut.compare("Some text here", "Some text here");
		int resultForLessThan = sut.compare("Bookmark", "This is not a text");
		int resultForGreaterThan = sut.compare("This is not a text for sure", "This is not a text as well");

		assertEquals(0, resultForEqual, "The strings are equal.");
		assertEquals(-18, resultForLessThan, "Difference between B and T (-18).");
		assertEquals(5, resultForGreaterThan, "Difference between f and a (5).");
	}

	@Test
	public void orderingSetWithLetters() {

		log.debug("orderingSetWithNumberAtStart test:");

		List<String> expectedOrder = Arrays.asList(
				"Failed-High", "FAILED-Test", "FAILED_High", "Failed_Test"
		);
		log.debug("expectedOrder: {}", expectedOrder);

		Set<String> allStrings = new TreeSet<>(sut);
		allStrings.add("Failed-High");
		allStrings.add("Failed_Test");
		allStrings.add("FAILED_High");
		allStrings.add("FAILED-Test");

		List<String> resultOrder = allStrings.stream().toList();
		log.debug("resultOrder: {}", resultOrder);

		assertEquals(4, allStrings.size(), "The size should match.");
		for (int index = 0; index < allStrings.size(); index++) {
			assertEquals(expectedOrder.get(index), resultOrder.get(index), "The result in index " + index +" should matches.");
		}
	}
}
