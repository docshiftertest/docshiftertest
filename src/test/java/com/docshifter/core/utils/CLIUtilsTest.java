package com.docshifter.core.utils;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CLIUtilsTest {
	@Test
	public void parseArgs_basicTest() {
		Assert.assertArrayEquals(new String[] {"some_command", "1", "other-arg", "1.5"},
				CLIUtils.parseArgs("some_command 1 other-arg 1.5"));
	}

	@Test
	public void parseArgs_nullReturnsNull() {
		Assert.assertNull(CLIUtils.parseArgs(null));
	}

	@Test
	public void parseArgs_nothingReturnsEmpty() {
		Assert.assertArrayEquals(new String[0], CLIUtils.parseArgs(""));
	}

	@Test
	public void parseArgs_doubleQuotesGroupTogether() {
		Assert.assertArrayEquals(new String[] {"some_command", "group me", "do", "not", "group"},
				CLIUtils.parseArgs("some_command \"group me\" do not group"));
	}

	@Test
	public void parseArgs_escapedDoubleQuotes() {
		Assert.assertArrayEquals(new String[] {"some_command", "\"do", "not", "group\""},
				CLIUtils.parseArgs("some_command \\\"do not group\\\""));
	}

	@Test
	public void parseArgs_backslashNoSpecialChar() {
		Assert.assertArrayEquals(new String[] {"some_command", "C:\\Users\\Johnno"},
				CLIUtils.parseArgs("some_command C:\\Users\\Johnno"));
	}

	@Test
	public void parseArgs_escapedBackslash() {
		Assert.assertArrayEquals(new String[] {"some_command", "\\escaped slashes"},
				CLIUtils.parseArgs("some_command \\\\\"escaped slashes\""));
	}

	@Test
	public void parseArgs_trailingBackslash() {
		Assert.assertArrayEquals(new String[] {"some_command", "slash", "at", "end\\"},
				CLIUtils.parseArgs("some_command slash at end\\"));
	}

	@Test
	public void parseArgs_trailingQuote() {
		Assert.assertArrayEquals(new String[] {"some_command", "quote", "at", "end"},
				CLIUtils.parseArgs("some_command quote at end\""));
	}

	@Test
	public void parseArgs_multipleSpaces() {
		Assert.assertArrayEquals(new String[] {"some_command", "do", "not", "group"},
				CLIUtils.parseArgs("   some_command do   not group   "));
	}

	@Test
	public void addOrReplaceOption_nonExisting() {
		List<String> options = new ArrayList<>(Arrays.asList("ffmpeg", "-i", "input.avi", "output.mov"));
		CLIUtils.addOrReplaceOption(options, "-c:v", "libx264");
		Assert.assertArrayEquals(new String[] {"ffmpeg", "-i", "input.avi", "output.mov", "-c:v", "libx264"}, options.toArray());
	}

	@Test
	public void addOrReplaceOption_incomplete() {
		List<String> options = new ArrayList<>(Arrays.asList("ffmpeg", "-i", "input.avi", "output.mov", "-c:v"));
		CLIUtils.addOrReplaceOption(options, "-c:v", "libx264");
		Assert.assertArrayEquals(new String[] {"ffmpeg", "-i", "input.avi", "output.mov", "-c:v", "libx264"}, options.toArray());
	}

	@Test
	public void addOrReplaceOption_existingAtEnd() {
		List<String> options = new ArrayList<>(Arrays.asList("ffmpeg", "-i", "input.avi", "output.mov", "-c:v", "libx265"));
		CLIUtils.addOrReplaceOption(options, "-c:v", "libx264");
		Assert.assertArrayEquals(new String[] {"ffmpeg", "-i", "input.avi", "output.mov", "-c:v", "libx264"}, options.toArray());
	}

	@Test
	public void addOrReplaceOption_existingInMiddle() {
		List<String> options = new ArrayList<>(Arrays.asList("ffmpeg", "-i", "input.avi", "output.mov", "-c:v",
				"libx265", "-c:a", "aac"));
		CLIUtils.addOrReplaceOption(options, "-c:v", "libx264");
		Assert.assertArrayEquals(new String[] {"ffmpeg", "-i", "input.avi", "output.mov", "-c:v", "libx264", "-c:a", "aac"},
				options.toArray());
	}
}
