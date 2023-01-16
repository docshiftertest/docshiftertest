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

public final class ConfigUtils {
	public static final int INVERTED = 1;
	public static final int REVERSED = 2;

	private ConfigUtils() {}

	/**
	 * Returns an ordered {@link Set} of possible {@link Integer}s from a range {@link String}.
	 * @param range The range {@link String} to analyze.
	 * @return A {@link Set} of {@link Integer}s conforming to the range {@link String}.
	 */
	public static Set<Integer> getUnboundedRangeSet(String range) {
		return getUnboundedRangeSet(range, 0);
	}

	public static Set<Integer> getUnboundedRangeSet(String range, int flags) {
		return getRangeSet(range, Integer.MAX_VALUE, flags);
	}

	public static Set<Integer> getRangeSet(String range, int max) {
		return getRangeSet(range, max, 0);
	}

	public static Set<Integer> getRangeSet(String range, int max, int flags) {
		return getRangeStream(range, max, flags)
				.boxed()
				.collect(Collectors.toCollection(LinkedHashSet::new));
	}

	public static IntStream getUnboundedRangeStream(String range) {
		return getUnboundedRangeStream(range, 0);
	}

	public static IntStream getUnboundedRangeStream(String range, int flags) {
		return getRangeStream(range, Integer.MAX_VALUE, flags);
	}

	public static IntStream getRangeStream(String range, int max) {
		return getRangeStream(range, max, 0);
	}

	public static IntStream getRangeStream(String range, int max, int flags) {
		return getRangeStream(range, 1, max, (flags & INVERTED) == INVERTED, (flags & REVERSED) == REVERSED);
	}

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
		if (entries[0].startsWith("!")) {
			inverted = !inverted;
		}

		for (String entry : entries) {
			boolean negate = entry.startsWith("!");
			if (negate) {
				entry = entry.substring(1);
			}

			String[] rangeArr = entry.split("-", 2);
			if (rangeArr.length > 2) {
				throw new IllegalArgumentException();
			}
			int lower = tryParseInt(rangeArr[0], min, max);
			int upper = lower;
			if (rangeArr.length == 2) {
				upper = tryParseInt(rangeArr[1], lower, max);
				lower = tryParseInt(rangeArr[0], min, upper);
			}

			if (reversed) {
				int tmpLower = lower;
				lower = upper;
				upper = tmpLower;
			}
			rangeMarkers.addAll(Arrays.asList(RangeMarker.createRange(negate == inverted, lower, upper)));
		}

		int realMin = reversed ? max : min;
		int realMax = reversed ? min : max;
		if (inverted) {
			rangeMarkers.addAll(Arrays.asList(RangeMarker.createRange(true, realMin, realMax)));
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

	@Value
	private static class RangeMarker {
		private final RangeMarker otherSide;
		private final boolean isStart;
		private final boolean isInclusion;
		private final int position;

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
			return start == start.otherSide ? new RangeMarker[]{start} : new RangeMarker[]{start, start.otherSide};
		}

		public boolean isSingleValueMarker() {
			return this == otherSide;
		}
	}

	private static int tryParseInt(String entry, int min, int max) {
		if (!entry.startsWith("(") || !entry.endsWith(")")) {
			if ("last".equalsIgnoreCase(entry)) {
				return checkReasonableMax(max);
			}
			int parsed = Integer.parseInt(entry);
			if (parsed < min) {
				throw new IllegalArgumentException();
			}
			if (parsed > max) {
				throw new IllegalArgumentException();
			}
			return parsed;
		}

		entry = entry.substring(1, entry.length() - 1);
		String[] operands = entry.split("[*/+-]", 2);
		char operator;
		if (operands.length == 1) {
			operator = '+';
		} else if (operands.length == 2) {
			operator = entry.charAt(operands[0].length());
		} else {
			throw new IllegalArgumentException();
		}
		BinaryOperator<Float> operatorFn = switch (operator) {
			case '+' -> Float::sum;
			case '-' -> (op1, op2) -> op1 - op2;
			case '*' -> (op1, op2) -> op1 * op2;
			case '/' -> (op1, op2) -> op1 / op2;
			default -> throw new IllegalArgumentException("Unexpected value: " + operator);
		};
		return Arrays.stream(operands)
				.map(op -> "last".equalsIgnoreCase(op) ? checkReasonableMax(max) : Float.parseFloat(op))
				.reduce(operatorFn)
				.map(Integer.class::cast)
				.map(val -> Math.max(min, Math.min(max, val)))
				.orElseThrow(() -> new IllegalStateException());
	}

	private static int checkReasonableMax(Integer max) {
		if (max == Integer.MAX_VALUE) {
			throw new IllegalArgumentException();
		}
		return max;
	}
}
