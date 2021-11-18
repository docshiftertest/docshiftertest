package com.docshifter.core.utils;

import lombok.extern.log4j.Log4j2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Log4j2
public final class CLIUtils {
	private CLIUtils() {}

	/**
	 * Parses a command line string and tokenizes it into an array of arguments. Takes into account backslashes,
	 * spaces and double quotes as special characters. Double quotes group characters together in a single
	 * argument, spaces split arguments (unless contained between double quotes) and backslashes escape double quotes
	 * and other backslashes.
	 * @param input The command line string to tokenize.
	 * @return A tokenized command line string, i.e. split into ordered arguments.
	 */
	public static String[] parseArgs(String input) {
		if (input == null) {
			return null;
		}

		final List<String> args = new ArrayList<>();
		final StringBuilder sb = new StringBuilder();
		boolean groupTogether = false;
		boolean escape = false;

		for (char inputChar : input.toCharArray()) {
			switch (inputChar) {
				case '\\':
					// Add a literal backslash if we're in escape mode
					if (escape) {
						sb.append(inputChar);
						escape = false;
					} else {
						escape = true;
					}
					break;
				case '"':
					// Add a literal double quote if we're in escape mode
					if (escape) {
						sb.append(inputChar);
						escape = false;
					} else {
						groupTogether = !groupTogether;
					}
					break;
				case ' ':
					// If we're in escape mode: a space shouldn't be escaped, so add a literal backslash as we
					// didn't escape anything
					if (escape) {
						sb.append('\\');
						escape = false;
					}
					if (!groupTogether) {
						args.add(sb.toString());
						sb.setLength(0);
					} else {
						sb.append(inputChar);
					}
					break;
				default:
					// If we're in escape mode: this character shouldn't be escaped, so add a literal backslash as
					// we didn't escape anything
					if (escape) {
						sb.append('\\');
						escape = false;
					}
					sb.append(inputChar);
			}
		}
		// After the last character/iteration: make sure to add trailing backslash if escape mode was activated, also
		// add the last token as an argument as there is probably no space at the end of the string
		if (escape) {
			sb.append('\\');
		}
		if (sb.length() > 0) {
			args.add(sb.toString());
		}

		return args.toArray(new String[0]);
	}

	/**
	 * Gets the output (stdout or stderr) as a List of Strings
	 * @param strm An input stream (normally stdout or stderr of a command)
	 * @return List<String> The lines of (error) output
	 * @throws IOException
	 */
	public static String[] getOutputLines(InputStream strm) throws IOException {
		BufferedReader output = new BufferedReader( new InputStreamReader( strm ) );
		List<String> outLines = new ArrayList<>();
		log.debug("Starting loop for readLine...");
		String line = output.readLine();
		while( line != null) {
			// Handle the line
			outLines.add(line);
			// Read the next line
			line = output.readLine();
		}
		return outLines.toArray(new String[0]);
	}

	/**
	 * Gets the output (stdout or stderr) as a single String
	 * @param strm An input stream (normally stdout or stderr of a command)
	 * @return The (error) output, with lines concatenated using the appropriate line separator of the operating system
	 * @throws IOException
	 */
	public static String getOutputString(InputStream strm) throws IOException {
		return String.join(System.lineSeparator(), getOutputLines(strm));
	}

	/**
	 * Overrides an option in the list of options, or adds it if it doesn't exist yet or if it is incomplete.
	 * @param options The list of options to update
	 * @param option The option to add/replace
	 * @param value The value for the option
	 */
	public static void addOrReplaceOption(List<String> options, String option, String value) {
		int index = options.indexOf(option);
		if (index == -1) {
			options.add(option);
			index = options.size() - 1;
		}

		if (index == options.size() - 1) {
			options.add(value);
		} else {
			options.set(index + 1, value);
		}
	}
}
