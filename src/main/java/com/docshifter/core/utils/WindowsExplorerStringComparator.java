package com.docshifter.core.utils;

import java.util.Comparator;

/**
 * <p>A {@link String} {@link Comparator} that orders in a Windows Explorer-like (when sorting by names), friendly file
 * name fashion. E.g. the String "1. First file" is directly followed by "2. Second file" and not "11. Eleventh file".</p>
 *
 * <p><b>This class is <i>NOT</i> thread safe, so do not share a single instance across multiple threads but
 * initialize a new one each time instead!</b></p>
 * @see <a href="https://stackoverflow.com/a/3066778">This StackOverflow answer, which this code is based on.</a>
 */
public class WindowsExplorerStringComparator implements Comparator<String> {
	private String string1;
	private String string2;
	private int position1;
	private int position2;
	private int length1;
	private int length2;

	/**
	 * Compares its two arguments for order.
	 * Returns a negative integer, zero, or a positive integer as the first argument is less than, equal to,
	 * or greater than the second.
	 * @param first the first String to be compared
	 * @param second the second String to be compared
	 */
	@Override
	public int compare(String first, String second) {

		string1 = first;
		string2 = second;
		length1 = first.length();
		length2 = second.length();
		position1 = 0;
		position2 = 0;

		int result = 0;
		while (result == 0 && position1 < length1 && position2 < length2) {
			char ch1 = string1.charAt(position1);
			char ch2 = string2.charAt(position2);

			if (Character.isDigit(ch1)) {
				if (Character.isDigit(ch2)) {
					result = compareNumbers();
				}
				else {
					result = -1;
				}
			}
			else if (Character.isLetter(ch1))  {
				if (Character.isLetter(ch2)) {
					result = compareOther(true);
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
					result = compareOther(false);
				}
			}

			position1++;
			position2++;
		}

		if (result == 0) {
			result = length1 - length2;
		}

		return result;
	}

	private int compareNumbers() {

		int end1 = getEnd(position1 + 1, length1, string1);

		int fullLength1 = end1 - position1;
		while (position1 < end1 && string2.charAt(position1) == '0') {
			position1++;
		}

		int end2 = getEnd(position2 + 1, length2, string2);

		int fullLength2 = end2 - position2;
		while (position2 < end2 && string2.charAt(position2) == '0') {
			position2++;
		}

		int delta = (end1 - position1) - (end2 - position2);
		if (delta != 0) {
			return delta;
		}

		while (position1 < end1 && position2 < end2) {
			delta = string1.charAt(position1++) - string2.charAt(position2++);

			if (delta != 0) {
				return delta;
			}
		}

		position1--;
		position2--;

		return fullLength2 - fullLength1;
	}

	/**
	 * Gets the last character that is a digit while comparing numbers
	 * @param end the characters to start checking
	 * @param length the total length of the string
	 * @param string the string to check
	 * @return the int representing the position of the last digit
	 */
	private int getEnd(int end, int length, String string) {

		while (end < length && Character.isDigit(string.charAt(end))) {
			end++;
		}

		return end;
	}

	private int compareOther(boolean isLetters) {

		char ch1 = string1.charAt(position1);
		char ch2 = string2.charAt(position2);

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
