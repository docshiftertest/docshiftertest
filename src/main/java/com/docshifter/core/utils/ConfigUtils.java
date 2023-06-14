package com.docshifter.core.utils;

import lombok.Value;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Utility methods to interpret and handle configurations specified by a user.
 */
public final class ConfigUtils {
	/**
	 * Bitflag that indicates the output should be inverted.
	 * E.g. min = 1, max = 10. Input = 2,4,5,6,7. Then the output should return any integer between min and max that
	 * is not contained in the input: 1,3,8,9,10.
	 */
	public static final int INVERTED = 1;
	/**
	 * Bitflag that indicates the output should be reversed.
	 * E.g. min = 1, max = 10. Input = 2,4,5,6,7. Then the output should be returned in a reverse order: 7,6,5,4,2.
	 */
	public static final int REVERSED = 2;

	private ConfigUtils() {}

	/**
	 * Returns a positive, ordered {@link Set} of {@link Integer}s from a range {@link String}, with a global minimum
	 * bound of 1 and no global maximum bound (in practice however, the bound goes up to {@link Integer#MAX_VALUE}).
	 * @param range The range {@link String} to analyze. Such a {@link String} contains one or more entries that are
	 *                 separated by a comma. Each entry can be an individual integer, or a (closed) range of
	 *                 integers, indicated by lower and upper bounds which are separated by a hyphen, e.g. 1-3,5,7-8
	 *                 -> [1,2,3,5,7,8]
	 *              <p>
	 *              "Normal" entries
	 *                 will be combined in the final range of integers, but there is also the possibility to create a
	 *                 "negated" entry, starting with an exclamation mark, which will exclude one or more integers
	 *                 from a previously specified range. One or more integers in such an entry can be included once
	 *                 again by specifying another normal entry after that one, and this pattern can be repeated as
	 *                 many times as you want, e.g. 1-20,!5-15,8-12,!10 -> [1,2,3,4,8,9,11,12,16,17,18,19,20].
	 *              <p>
	 *              Last but not least, you can also begin a range {@link String} with a
	 *                 negated entry, in that case, you indicate that the specified range needs to be inverted, i.e.
	 *                 the output should consist of any integers between the global minimum (1) and maximum bounds that
	 *                 have NOT been negated. Here too, you can specify a normal entry to include some integers
	 *                 present within a previously negated range. E.g. !4,!6-10,8 -> [1,2,3,5,8,11,12,...]
	 * @return A {@link Set} of {@link Integer}s conforming to the range {@link String}.
	 */
	public static Set<Integer> getUnboundedRangeSet(String range) {
		return getUnboundedRangeSet(range, 0);
	}

	/**
	 * Returns a positive, ordered {@link Set} of {@link Integer}s from a range {@link String}, with a global minimum
	 * bound of 1 and no global maximum bound (in practice however, the bound goes up to {@link Integer#MAX_VALUE}).
	 * @param range The range {@link String} to analyze. Such a {@link String} contains one or more entries that are
	 *                 separated by a comma. Each entry can be an individual integer, or a (closed) range of
	 *                 integers, indicated by lower and upper bounds which are separated by a hyphen, e.g. 1-3,5,7-8
	 *                 -> [1,2,3,5,7,8]
	 *              <p>
	 *              "Normal" entries
	 *                 will be combined in the final range of integers, but there is also the possibility to create a
	 *                 "negated" entry, starting with an exclamation mark, which will exclude one or more integers
	 *                 from a previously specified range. One or more integers in such an entry can be included once
	 *                 again by specifying another normal entry after that one, and this pattern can be repeated as
	 *                 many times as you want, e.g. 1-20,!5-15,8-12,!10 -> [1,2,3,4,8,9,11,12,16,17,18,19,20].
	 *              <p>
	 *              Last but not least, you can also begin a range {@link String} with a
	 *                 negated entry, in that case, you indicate that the specified range needs to be inverted, i.e.
	 *                 the output should consist of any integers between the global minimum (1) and maximum bounds that
	 *                 have NOT been negated. Here too, you can specify a normal entry to include some integers
	 *                 present within a previously negated range. E.g. !4,!6-10,8 -> [1,2,3,5,8,11,12,...]
	 * @param flags Any bitflags to apply to the output.
	 * @return A {@link Set} of {@link Integer}s conforming to the range {@link String}.
	 */
	public static Set<Integer> getUnboundedRangeSet(String range, int flags) {
		return getRangeSet(range, Integer.MAX_VALUE, flags);
	}

	/**
	 * Returns a positive, ordered {@link Set} of {@link Integer}s from a range {@link String}, with a global minimum
	 * bound of 1 and a specified global maximum bound.
	 * @param range The range {@link String} to analyze. Such a {@link String} contains one or more entries that are
	 *                 separated by a comma. Each entry can be an individual integer, or a (closed) range of
	 *                 integers, indicated by lower and upper bounds which are separated by a hyphen, e.g. 1-3,5,7-8
	 *                 -> [1,2,3,5,7,8]
	 *              <p>
	 *              The
	 *                 individual integers or lower or upper bounds can also be one of the special sequences
	 *                 {@code FIRST} or {@code LAST} (case-insensitive), which then dynamically refers to respectively
	 *                 the first/lowest or last/highest possible integer in the sequence (so this depends on the global
	 *                 minimum/maximum bounds). Furthermore, any of the 4 basic arithmetic operations (+, -, /, *) may
	 *                 take place on the {@code FIRST}/{@code LAST} sequences, given that this operation is specified
	 *                 between parentheses, e.g. {@code (LAST-1)} or {@code (LAST/2)}.
	 *              <p>
	 *              "Normal" entries
	 *                 will be combined in the final range of integers, but there is also the possibility to create a
	 *                 "negated" entry, starting with an exclamation mark, which will exclude one or more integers
	 *                 from a previously specified range. One or more integers in such an entry can be included once
	 *                 again by specifying another normal entry after that one, and this pattern can be repeated as
	 *                 many times as you want, e.g. 1-20,!5-15,8-12,!10 -> [1,2,3,4,8,9,11,12,16,17,18,19,20].
	 *              <p>
	 *              Last but not least, you can also begin a range {@link String} with a
	 *                 negated entry, in that case, you indicate that the specified range needs to be inverted, i.e.
	 *                 the output should consist of any integers between the global minimum (1) and maximum bounds that
	 *                 have NOT been negated. Here too, you can specify a normal entry to include some integers
	 *                 present within a previously negated range. E.g. for max 10: !4,!6-10,8 -> [1,2,3,5,8]
	 * @param max The global maximum bound.
	 * @return A {@link Set} of {@link Integer}s conforming to the range {@link String}.
	 */
	public static Set<Integer> getRangeSet(String range, int max) {
		return getRangeSet(range, max, 0);
	}

	/**
	 * Returns a positive, ordered {@link Set} of {@link Integer}s from a range {@link String}, with a global minimum
	 * bound of 1 and a specified global maximum bound.
	 * @param range The range {@link String} to analyze. Such a {@link String} contains one or more entries that are
	 *                 separated by a comma. Each entry can be an individual integer, or a (closed) range of
	 *                 integers, indicated by lower and upper bounds which are separated by a hyphen, e.g. 1-3,5,7-8
	 *                 -> [1,2,3,5,7,8]
	 *              <p>
	 *              The
	 *                 individual integers or lower or upper bounds can also be one of the special sequences
	 *                 {@code FIRST} or {@code LAST} (case-insensitive), which then dynamically refers to respectively
	 *                 the first/lowest or last/highest possible integer in the sequence (so this depends on the global
	 *                 minimum/maximum bounds). Furthermore, any of the 4 basic arithmetic operations (+, -, /, *) may
	 *                 take place on the {@code FIRST}/{@code LAST} sequences, given that this operation is specified
	 *                 between parentheses, e.g. {@code (LAST-1)} or {@code (LAST/2)}.
	 *              <p>
	 *              "Normal" entries
	 *                 will be combined in the final range of integers, but there is also the possibility to create a
	 *                 "negated" entry, starting with an exclamation mark, which will exclude one or more integers
	 *                 from a previously specified range. One or more integers in such an entry can be included once
	 *                 again by specifying another normal entry after that one, and this pattern can be repeated as
	 *                 many times as you want, e.g. 1-20,!5-15,8-12,!10 -> [1,2,3,4,8,9,11,12,16,17,18,19,20].
	 *              <p>
	 *              Last but not least, you can also begin a range {@link String} with a
	 *                 negated entry, in that case, you indicate that the specified range needs to be inverted, i.e.
	 *                 the output should consist of any integers between the global minimum (1) and maximum bounds that
	 *                 have NOT been negated. Here too, you can specify a normal entry to include some integers
	 *                 present within a previously negated range. E.g. for max 10: !4,!6-10,8 -> [1,2,3,5,8]
	 * @param max The global maximum bound.
	 * @param flags Any bitflags to apply to the output.
	 * @return A {@link Set} of {@link Integer}s conforming to the range {@link String}.
	 */
	public static Set<Integer> getRangeSet(String range, int max, int flags) {
		return getRangeStream(range, max, flags)
				.boxed()
				.collect(Collectors.toCollection(LinkedHashSet::new));
	}

	/**
	 * Returns a positive, ordered {@link IntStream} from a range {@link String}, with a global minimum bound of 1
	 * and no global maximum bound (in practice however, the bound goes up to {@link Integer#MAX_VALUE}).
	 * @param range The range {@link String} to analyze. Such a {@link String} contains one or more entries that are
	 *                 separated by a comma. Each entry can be an individual integer, or a (closed) range of
	 *                 integers, indicated by lower and upper bounds which are separated by a hyphen, e.g. 1-3,5,7-8
	 *                 -> [1,2,3,5,7,8]
	 *              <p>
	 *              "Normal" entries
	 *                 will be combined in the final range of integers, but there is also the possibility to create a
	 *                 "negated" entry, starting with an exclamation mark, which will exclude one or more integers
	 *                 from a previously specified range. One or more integers in such an entry can be included once
	 *                 again by specifying another normal entry after that one, and this pattern can be repeated as
	 *                 many times as you want, e.g. 1-20,!5-15,8-12,!10 -> [1,2,3,4,8,9,11,12,16,17,18,19,20].
	 *              <p>
	 *              Last but not least, you can also begin a range {@link String} with a
	 *                 negated entry, in that case, you indicate that the specified range needs to be inverted, i.e.
	 *                 the output should consist of any integers between the global minimum (1) and maximum bounds that
	 *                 have NOT been negated. Here too, you can specify a normal entry to include some integers
	 *                 present within a previously negated range. E.g. !4,!6-10,8 -> [1,2,3,5,8,11,12,...]
	 * @return An {@link IntStream} conforming to the range {@link String}.
	 */
	public static IntStream getUnboundedRangeStream(String range) {
		return getUnboundedRangeStream(range, 0);
	}

	/**
	 * Returns a positive, ordered {@link IntStream} from a range {@link String}, with a global minimum bound of 1
	 * and no global maximum bound (in practice however, the bound goes up to {@link Integer#MAX_VALUE}).
	 * @param range The range {@link String} to analyze. Such a {@link String} contains one or more entries that are
	 *                 separated by a comma. Each entry can be an individual integer, or a (closed) range of
	 *                 integers, indicated by lower and upper bounds which are separated by a hyphen, e.g. 1-3,5,7-8
	 *                 -> [1,2,3,5,7,8]
	 *              <p>
	 *              "Normal" entries
	 *                 will be combined in the final range of integers, but there is also the possibility to create a
	 *                 "negated" entry, starting with an exclamation mark, which will exclude one or more integers
	 *                 from a previously specified range. One or more integers in such an entry can be included once
	 *                 again by specifying another normal entry after that one, and this pattern can be repeated as
	 *                 many times as you want, e.g. 1-20,!5-15,8-12,!10 -> [1,2,3,4,8,9,11,12,16,17,18,19,20].
	 *              <p>
	 *              Last but not least, you can also begin a range {@link String} with a
	 *                 negated entry, in that case, you indicate that the specified range needs to be inverted, i.e.
	 *                 the output should consist of any integers between the global minimum (1) and maximum bounds that
	 *                 have NOT been negated. Here too, you can specify a normal entry to include some integers
	 *                 present within a previously negated range. E.g. !4,!6-10,8 -> [1,2,3,5,8,11,12,...]
	 * @param flags Any bitflags to apply to the output.
	 * @return An {@link IntStream} conforming to the range {@link String}.
	 */
	public static IntStream getUnboundedRangeStream(String range, int flags) {
		return getRangeStream(range, Integer.MAX_VALUE, flags);
	}

	/**
	 * Returns a positive, ordered {@link IntStream} from a range {@link String}, with a global minimum bound of 1
	 * and a specified global maximum bound.
	 * @param range The range {@link String} to analyze. Such a {@link String} contains one or more entries that are
	 *                 separated by a comma. Each entry can be an individual integer, or a (closed) range of
	 *                 integers, indicated by lower and upper bounds which are separated by a hyphen, e.g. 1-3,5,7-8
	 *                 -> [1,2,3,5,7,8]
	 *              <p>
	 *              The
	 *                 individual integers or lower or upper bounds can also be one of the special sequences
	 *                 {@code FIRST} or {@code LAST} (case-insensitive), which then dynamically refers to respectively
	 *                 the first/lowest or last/highest possible integer in the sequence (so this depends on the global
	 *                 minimum/maximum bounds). Furthermore, any of the 4 basic arithmetic operations (+, -, /, *) may
	 *                 take place on the {@code FIRST}/{@code LAST} sequences, given that this operation is specified
	 *                 between parentheses, e.g. {@code (LAST-1)} or {@code (LAST/2)}.
	 *              <p>
	 *              "Normal" entries
	 *                 will be combined in the final range of integers, but there is also the possibility to create a
	 *                 "negated" entry, starting with an exclamation mark, which will exclude one or more integers
	 *                 from a previously specified range. One or more integers in such an entry can be included once
	 *                 again by specifying another normal entry after that one, and this pattern can be repeated as
	 *                 many times as you want, e.g. 1-20,!5-15,8-12,!10 -> [1,2,3,4,8,9,11,12,16,17,18,19,20].
	 *              <p>
	 *              Last but not least, you can also begin a range {@link String} with a
	 *                 negated entry, in that case, you indicate that the specified range needs to be inverted, i.e.
	 *                 the output should consist of any integers between the global minimum (1) and maximum bounds that
	 *                 have NOT been negated. Here too, you can specify a normal entry to include some integers
	 *                 present within a previously negated range. E.g. for max 10: !4,!6-10,8 -> [1,2,3,5,8]
	 * @param max The global maximum bound.
	 * @return An {@link IntStream} conforming to the range {@link String}.
	 */
	public static IntStream getRangeStream(String range, int max) {
		return getRangeStream(range, max, 0);
	}

	/**
	 * Returns a positive, ordered {@link IntStream} from a range {@link String}, with a global minimum bound of 1
	 * and a specified global maximum bound.
	 * @param range The range {@link String} to analyze. Such a {@link String} contains one or more entries that are
	 *                 separated by a comma. Each entry can be an individual integer, or a (closed) range of
	 *                 integers, indicated by lower and upper bounds which are separated by a hyphen, e.g. 1-3,5,7-8
	 *                 -> [1,2,3,5,7,8]
	 *              <p>
	 *              The
	 *                 individual integers or lower or upper bounds can also be one of the special sequences
	 *                 {@code FIRST} or {@code LAST} (case-insensitive), which then dynamically refers to respectively
	 *                 the first/lowest or last/highest possible integer in the sequence (so this depends on the global
	 *                 minimum/maximum bounds). Furthermore, any of the 4 basic arithmetic operations (+, -, /, *) may
	 *                 take place on the {@code FIRST}/{@code LAST} sequences, given that this operation is specified
	 *                 between parentheses, e.g. {@code (LAST-1)} or {@code (LAST/2)}.
	 *              <p>
	 *              "Normal" entries
	 *                 will be combined in the final range of integers, but there is also the possibility to create a
	 *                 "negated" entry, starting with an exclamation mark, which will exclude one or more integers
	 *                 from a previously specified range. One or more integers in such an entry can be included once
	 *                 again by specifying another normal entry after that one, and this pattern can be repeated as
	 *                 many times as you want, e.g. 1-20,!5-15,8-12,!10 -> [1,2,3,4,8,9,11,12,16,17,18,19,20].
	 *              <p>
	 *              Last but not least, you can also begin a range {@link String} with a
	 *                 negated entry, in that case, you indicate that the specified range needs to be inverted, i.e.
	 *                 the output should consist of any integers between the global minimum (1) and maximum bounds that
	 *                 have NOT been negated. Here too, you can specify a normal entry to include some integers
	 *                 present within a previously negated range. E.g. for max 10: !4,!6-10,8 -> [1,2,3,5,8]
	 * @param max The global maximum bound.
	 * @param flags Any bitflags to apply to the output.
	 * @return An {@link IntStream} conforming to the range {@link String}.
	 */
	public static IntStream getRangeStream(String range, int max, int flags) {
		return getRangeStream(range, 1, max, (flags & INVERTED) == INVERTED, (flags & REVERSED) == REVERSED);
	}

	/**
	 * Returns an ordered {@link IntStream} from a range {@link String}.
	 * @param range The range {@link String} to analyze. Such a {@link String} contains one or more entries that are
	 *                 separated by a comma. Each entry can be an individual integer, or a (closed) range of
	 *                 integers, indicated by lower and upper bounds which are separated by a hyphen, e.g. 1-3,5,7-8
	 *                 -> [1,2,3,5,7,8]
	 *              <p>
	 *              The
	 *                 individual integers or lower or upper bounds can also be one of the special sequences
	 *                 {@code FIRST} or {@code LAST} (case-insensitive), which then dynamically refers to respectively
	 *                 the first/lowest or last/highest possible integer in the sequence (so this depends on the global
	 *                 minimum/maximum bounds). Furthermore, any of the 4 basic arithmetic operations (+, -, /, *) may
	 *                 take place on the {@code FIRST}/{@code LAST} sequences, given that this operation is specified
	 *                 between parentheses, e.g. {@code (LAST-1)} or {@code (LAST/2)}.
	 *              <p>
	 *              "Normal" entries
	 *                 will be combined in the final range of integers, but there is also the possibility to create a
	 *                 "negated" entry, starting with an exclamation mark, which will exclude one or more integers
	 *                 from a previously specified range. One or more integers in such an entry can be included once
	 *                 again by specifying another normal entry after that one, and this pattern can be repeated as
	 *                 many times as you want, e.g. 1-20,!5-15,8-12,!10 -> [1,2,3,4,8,9,11,12,16,17,18,19,20].
	 *              <p>
	 *              Last but not least, you can also begin a range {@link String} with a
	 *                 negated entry, in that case, you indicate that the specified range needs to be inverted, i.e.
	 *                 the output should consist of any integers between the global minimum and maximum bounds that
	 *                 have NOT been negated. Here too, you can specify a normal entry to include some integers
	 *                 present within a previously negated range. E.g. for min 1 and max 10: !4,!6-10,8 -> [1,2,3,5,8]
	 * @param min The global minimum bound.
	 * @param max The global maximum bound.
	 * @param inverted Whether to invert the output. E.g. min = 1, max = 10. Input = 2,4,5-7. If {@code true} then the
	 *                    output should return any integer between min and max that is not contained in the input:
	 *                    1,3,8,9,10. NOTE: The input can also be inverted by starting the range {@link String} with
	 *                    a negated entry (as mentioned in the {@code range} parameter explanation)! So if this is
	 *                    set to {@code true}, and the {@code range} starts with a negated entry (!), then the
	 *                    inversion flag will be flipped and the output will be returned in a non-inverted fashion.
	 * @param reversed Whether to reverse the output. E.g. min = 1, max = 10. Input = 2,4,5-7. If {@code true} then the
	 *                    output should be returned in a reverse order: 7,6,5,4,2.
	 * @return An {@link IntStream} conforming to the range {@link String}.
	 */
	private static IntStream getRangeStream(String range, int min, int max, boolean inverted, boolean reversed) {
		String cleanedRange = StringUtils.deleteWhitespace(range);
		if (StringUtils.isEmpty(cleanedRange)) {
			if (!inverted) {
				return IntStream.empty();
			}
			IntStream all = IntStream.rangeClosed(min, max);
			if (reversed) {
				all = all.map(i -> max - i + min);
			}
			return all;
		}
		String[] entries = cleanedRange.split(",");

		Comparator<RangeMarker> positionComparator = Comparator.comparingInt(RangeMarker::getPosition);
		if (reversed) {
			positionComparator = positionComparator.reversed();
		}
		Set<RangeMarker> rangeMarkers =
				new TreeSet<>(positionComparator
						.thenComparing(RangeMarker::isSingleValueMarker, Comparator.reverseOrder())
						.thenComparing(RangeMarker::isInclusion, Comparator.reverseOrder())
						.thenComparing(RangeMarker::isStart)
				);
		// Per the Javadocs, if the first entry is a negation instead of a normal one, then we flip the inversion flag
		if (entries[0].startsWith("!")) {
			inverted = !inverted;
		}

		for (String entry : entries) {
			boolean negate = entry.startsWith("!");
			if (negate) {
				entry = entry.substring(1);
			}
			// Split on all hyphens that are not contained within parentheses. Limit on 3, so we can detect an invalid
			// entry with more elements than expected.
			String[] rangeArr = entry.split("-(?![^(]*\\))", 3);
			if (rangeArr.length > 2) {
				throw new IllegalArgumentException("A range entry cannot contain more than 2 elements (lower and " +
						"upper bound)! Evaluated range entry: " + entry);
			}
			int lower = parseDynamicInt(rangeArr[0], min, max);
			int upper = lower;
			if (rangeArr.length == 2) {
				upper = parseDynamicInt(rangeArr[1], lower, max);
				// Revalidate against upper now that we know it
				lower = parseDynamicInt(rangeArr[0], min, upper);
			}

			if (reversed) {
				int tmpLower = lower;
				lower = upper;
				upper = tmpLower;
			}
			rangeMarkers.addAll(Arrays.asList(RangeMarker.createRange(!negate, lower, upper)));
		}

		final int realMin = reversed ? max : min;
		final int realMax = reversed ? min : max;
		if (inverted) {
			// If we're in inverted mode, we should include every number from min to max by default
			// Per the rangeMarkers (TreeSet) Comparator: when the positions are the same, single value markers take
			// priority, then inclusions, then end markers
			// Due to this ordering, a slight problem can occur where an exclusion single value marker for the
			// minimum value gets processed first, and then we process the global "include everything from min to max
			// by default" range, causing the former one to get ignored. To tackle this issue, we can reduce the
			// global range to start at the minimum, excluding any consecutive single value exclusion range markers
			// that are equal to (or follow) it.
			int globalRangeStart = realMin;
			for (RangeMarker curr : rangeMarkers) {
				if (curr.getPosition() == globalRangeStart && curr.isSingleValueMarker() && !curr.isInclusion()) {
					if (reversed) {
						globalRangeStart--;
					} else {
						globalRangeStart++;
					}
				} else {
					break;
				}
			}
			// No point adding a global inclusion range if we ended up excluding all the individual numbers (from min
			// to max) in the first place...
			if (reversed ? globalRangeStart >= realMax : globalRangeStart <= realMax) {
				rangeMarkers.addAll(Arrays.asList(RangeMarker.createRange(true, globalRangeStart, realMax)));
			}
		}

		return combineRanges(rangeMarkers.iterator(), realMin, reversed);
	}

	private static IntStream combineRanges(Iterator<RangeMarker> it, int initialValue, boolean reversed) {
		IntStream combined = IntStream.empty();
		Deque<RangeMarker> openMarkers = new ArrayDeque<>();
		RangeMarker lastMarker = new RangeMarker(false, initialValue);
		while (it.hasNext()) {
			RangeMarker currMarker = it.next();

			if (currMarker.isSingleValueMarker()) {
				IntStream result = handleLastMarker(openMarkers, currMarker, lastMarker, reversed, false);
				if (result != null) {
					combined = IntStream.concat(combined, result);
				}
				if (lastMarker.position != currMarker.position && currMarker.isInclusion) {
					combined = IntStream.concat(combined, IntStream.of(currMarker.position));
				}
				lastMarker = currMarker;
				continue;
			}

			if (currMarker.isStart) {
				IntStream result = handleLastMarker(openMarkers, currMarker, lastMarker, reversed, false);
				if (result != null) {
					combined = IntStream.concat(combined, result);
				}

				openMarkers.push(currMarker);
				lastMarker = currMarker;
				continue;
			}

			IntStream result = handleLastMarker(openMarkers, currMarker, lastMarker, reversed, true);
			if (result != null) {
				combined = IntStream.concat(combined, result);
			}
			openMarkers.remove(currMarker.otherSide);
			lastMarker = currMarker;
		}
		return combined;
	}

	private static IntStream handleLastMarker(Deque<RangeMarker> openMarkers, RangeMarker currMarker,
											  RangeMarker lastMarker, boolean reversed, boolean closedRange) {
		if (lastMarker.position == currMarker.position) {
			return null;
		}
		if (openMarkers.peek() != null && openMarkers.peek().isInclusion) {
			int from = lastMarker.position;
			if (!lastMarker.isInclusion || !lastMarker.isStart) {
				if (reversed) {
					from--;
				} else {
					from++;
				}
			}
			int realFrom = reversed ? currMarker.position : from;
			int realTo = reversed ? from : currMarker.position;
			if (!closedRange) {
				realTo--;
			}
			IntStream range = IntStream.rangeClosed(realFrom, realTo);
			if (reversed) {
				final int finalFrom = from;
				range = range.map(i -> currMarker.position - i + finalFrom);
			}
			return range;
		}
		return null;
	}

	/**
	 * Represents a single range marker/boundary.
	 */
	@Value
	private static class RangeMarker {
		RangeMarker otherSide;
		boolean isStart;
		boolean isInclusion;
		int position;

		private RangeMarker(RangeMarker otherSide, int endPosition) {
			this.isInclusion = otherSide.isInclusion;
			this.isStart = false;
			this.position = endPosition;
			this.otherSide = otherSide;
		}

		private RangeMarker(boolean isInclusion, int startPosition, int endPosition) {
			this.isInclusion = isInclusion;
			this.isStart = startPosition != endPosition;
			this.position = startPosition;
			this.otherSide = startPosition == endPosition ? this : new RangeMarker(this, endPosition);
		}

		public RangeMarker(boolean isInclusion, int position) {
			this(isInclusion, position, position);
		}

		public static RangeMarker[] createRange(boolean isInclusion, int startPosition, int endPosition) {
			RangeMarker start = new RangeMarker(isInclusion, startPosition, endPosition);
			return start.isSingleValueMarker() ? new RangeMarker[]{start} : new RangeMarker[]{start, start.otherSide};
		}

		public boolean isSingleValueMarker() {
			return this == otherSide;
		}
	}

	/**
	 * Parses an {@code int} and makes sure that its value sits between a specified min/max range, but with a twist!
	 * Instead of normal, boring ints, this method also accepts what we call "dynamic" ints, meaning special keywords
	 * such as {@code FIRST} and {@code LAST} which refer to the provided minimum and maximum values respectively.
	 * Additionally, an entry can also contain one of the four basic arithmetic operations: +, -, *, /. The consequence
	 * of that is that it is possible to perform calculations using the special keywords, such as LAST-1 or LAST/2.
	 * Finally, by default this method will enforce the min/max bounds strictly, meaning that if the final result is outside
	 * the requested range, an {@link IllegalArgumentException} will be thrown. This strict mode can be turned off by putting
	 * the entry between parentheses however, such as (3) or (LAST-5). In that case the result will always be clamped between
	 * the min/max bounds.
	 * @param entry The dynamic {@code int} entry to parse.
	 * @param min The minimum value that should be allowed for the integer.
	 * @param max The maximum value that should be allowed for the integer.
	 * @return The parsed {@code int}.
	 * @throws IllegalArgumentException The {@code entry} could not be parsed, either because it is supplied in a bad format
	 * or because the min/max constraints are not met (and we are enforcing those bounds strictly).
	 */
	public static int parseDynamicInt(String entry, int min, int max) {
		final boolean strictMode;
		if (entry.startsWith("(") && entry.endsWith(")")) {
			entry = entry.substring(1, entry.length() - 1);
			strictMode = false;
		} else {
			strictMode = true;
		}

		String[] operands = entry.split("[*/+-]", 3);
		char operator;
		if (operands.length == 1) {
			operator = '+';
		} else if (operands.length == 2) {
			operator = entry.charAt(operands[0].length());
		} else {
			throw new IllegalArgumentException("Only 1 or 2 operands are allowed, found more (or none) while " +
					"evaluating: " + entry);
		}
		BinaryOperator<Float> operatorFn = switch (operator) {
			case '+' -> Float::sum;
			case '-' -> (op1, op2) -> op1 - op2;
			case '*' -> (op1, op2) -> op1 * op2;
			case '/' -> (op1, op2) -> op1 / op2;
			default -> throw new IllegalArgumentException("Unexpected operator value: " + operator);
		};
		return Arrays.stream(operands)
				.map(op -> parseOperand(op, min, max))
				.map(Float.class::cast)
				.reduce(operatorFn)
				.map(Integer.class::cast)
				.map(val -> clampResult(val, min, max, strictMode))
				.orElseThrow(() -> new IllegalStateException("Found no operands, this should not happen as we " +
						"have checked it before!"));
	}

	private static int parseOperand(String operand, int min, int max) {
		if ("first".equalsIgnoreCase(operand)) {
			if (min == Integer.MIN_VALUE) {
				throw new IllegalArgumentException("It doesn't make sense to use FIRST in an unbounded range!");
			}
			return min;
		}

		if ("last".equalsIgnoreCase(operand)) {
			if (max == Integer.MAX_VALUE) {
				throw new IllegalArgumentException("It doesn't make sense to use LAST in an unbounded range!");
			}
			return max;
		}

		return Integer.parseInt(operand);
	}

	private static int clampResult(int result, int min, int max, boolean strictMode) {
		if (!strictMode) {
			return Math.max(min, Math.min(max, result));
		}

		if (result < min) {
			throw new IllegalArgumentException("The value " + result + " is located below the global minimum (" + min + ")!" +
					" If you are unsure of the minimum, you can let the value get interpreted safely by putting " +
					"it between parentheses.");
		}
		if (result > max) {
			throw new IllegalArgumentException("The value " + result + " is located above the global maximum (" + max + ")!" +
					" If you are unsure of the maximum, you can let the value get interpreted safely by putting " +
					"it between parentheses.");
		}
		return result;
	}
}
