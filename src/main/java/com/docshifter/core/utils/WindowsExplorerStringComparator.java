package com.docshifter.core.utils;

import java.util.Comparator;

/**
 * <p>A {@link String} {@link Comparator} that orders in a Windows Explorer-like (when sorting by names), friendly file
 * name fashion. E.g. the String "1. First file" is directly followed by "2. Second file" and not "11. Eleventh file".</p>
 * @see <a href="https://stackoverflow.com/a/3066778">This StackOverflow answer, which this code is based on.</a>
 */
public class WindowsExplorerStringComparator implements Comparator<String> {
	/**
	 * Compares its two arguments for order.
	 * Returns a negative integer, zero, or a positive integer as the first argument is less than, equal to,
	 * or greater than the second.
	 * @param first the first String to be compared
	 * @param second the second String to be compared
	 */
	@Override
	public int compare(String first, String second) {

		String[] strings = {first, second};
		int[] positions = {0, 0};

		int result = 0;
		while (result == 0 && positions[0] < strings[0].length() && positions[1] < strings[1].length()) {
			char ch1 = strings[0].charAt(positions[0]);
			char ch2 = strings[1].charAt(positions[1]);

			if (Character.isDigit(ch1)) {
				if (Character.isDigit(ch2)) {
					result = compareNumbers(strings, positions);
				}
				else {
					result = -1;
				}
			}
			else if (Character.isLetter(ch1))  {
				if (Character.isLetter(ch2)) {
					result = compareOther(true, strings, positions);
				}
				else {
					result = 1;
				}
			}
			else {
				if (Character.isDigit(ch2)) {
					result = 1;
				}
				else if (Character.isLetter(ch2)) {
					result = -1;
				}
				else {
					result = compareOther(false, strings, positions);
				}
			}

			positions[0]++;
			positions[1]++;
		}

		if (result == 0) {
			result = strings[0].length() - strings[1].length();
		}

		return result;
	}

	private static int compareNumbers(String[] strings, int[] positions) {

		int end1 = getEnd(positions[0] + 1, strings[0]);

		int fullLength1 = end1 - positions[0];
		while (positions[0] < end1 && strings[1].charAt(positions[0]) == '0') {
			positions[0]++;
		}

		int end2 = getEnd(positions[1] + 1, strings[1]);

		int fullLength2 = end2 - positions[1];
		while (positions[1] < end2 && strings[1].charAt(positions[1]) == '0') {
			positions[1]++;
		}

		int delta = (end1 - positions[0]) - (end2 - positions[1]);
		if (delta != 0) {
			return delta;
		}

		while (positions[0] < end1 && positions[1] < end2) {
			delta = strings[0].charAt(positions[0]++) - strings[1].charAt(positions[1]++);

			if (delta != 0) {
				return delta;
			}
		}

		positions[0]--;
		positions[1]--;

		return fullLength2 - fullLength1;
	}

	/**
	 * Gets the last character that is a digit while comparing numbers
	 * @param end the characters to start checking
	 * @param string the string to check
	 * @return the int representing the position of the last digit
	 */
	private static int getEnd(int end, String string) {

		while (end < string.length() && Character.isDigit(string.charAt(end))) {
			end++;
		}

		return end;
	}

	private static int compareOther(boolean isLetters, String[] strings, int[] positions) {

		char ch1 = strings[0].charAt(positions[0]);
		char ch2 = strings[1].charAt(positions[1]);

		if (ch1 == ch2) {
			return 0;
		}

		if (isLetters) {
			ch1 = Character.toUpperCase(ch1);
			ch2 = Character.toUpperCase(ch2);

			if (ch1 != ch2) {
				ch1 = Character.toLowerCase(ch1);
				ch2 = Character.toLowerCase(ch2);
			}

		}

		return ch1 - ch2;
	}
}
