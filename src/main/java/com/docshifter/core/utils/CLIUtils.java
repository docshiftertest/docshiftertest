package com.docshifter.core.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

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
	 * @throws IOException If an I/O error occurs while reading the lines from the input stream
	 */
	public static String[] getOutputLines(InputStream strm) throws IOException {
		List<String> outLines = new ArrayList<>();

		try (Reader reader = new InputStreamReader(strm);
			 BufferedReader output = new BufferedReader(reader)) {
			log.debug("Starting loop for readLine...");
			String line = output.readLine();
			while (line != null) {
				// Handle the line
				outLines.add(line);
				// Read the next line
				line = output.readLine();
			}
		}

		return outLines.toArray(new String[0]);
	}

	/**
	 * Gets the output (stdout or stderr) as a single String
	 * @param strm An input stream (normally stdout or stderr of a command)
	 * @return The (error) output, with lines concatenated using the appropriate line separator of the operating system
	 * @throws IOException If an I/O error occurs while reading the lines from the input stream
	 */
	public static String getOutputString(InputStream strm) throws IOException {
		return String.join(System.lineSeparator(), getOutputLines(strm));
	}

	/**
	 * Fetches the stdout, stderr and exit code of a {@link Process} in an orderly fashion.
	 * @param process The process to evaluate.
	 * @return A {@link ProcessResult} containing information about the process execution.
	 * @throws InterruptedException The current thread was interrupted.
	 * @throws IOException An error occurred while reading from either the stdout or stderr stream.
	 */
	public static ProcessResult getResultForProcess(Process process) throws InterruptedException, IOException {
		AtomicReference<IOException> ioe = new AtomicReference<>();

		AtomicReference<String> stdout = new AtomicReference<>();
		Thread outThread = new Thread(() -> {
			try {
				stdout.set(getOutputString(process.getInputStream()));
			} catch (IOException e) {
				// Try to interrupt the other thread by forcibly closing the other stream which will throw an
				// IOException
				if (ioe.compareAndSet(null, e)) {
					try {
						process.getErrorStream().close();
					} catch (IOException ex) {
						log.error("And an error occurred while trying to close/interrupt the error stream!");
					}
				}
			}
		});
		outThread.start();

		AtomicReference<String> stderr = new AtomicReference<>();
		Thread errThread = new Thread(() -> {
			try {
				stderr.set(getOutputString(process.getErrorStream()));
			} catch (IOException e) {
				// Try to interrupt the other thread by forcibly closing the other stream which will throw an
				// IOException
				if (ioe.compareAndSet(null, e)) {
					try {
						process.getInputStream().close();
					} catch (IOException ex) {
						log.error("And an error occurred while trying to close/interrupt the input stream!");
					}
				}
			}
		});
		errThread.start();

		// Wait for both threads to finish or fail
		outThread.join();
		errThread.join();

		// Bubble up the IOException if we got one
		if (ioe.get() != null) {
			throw ioe.get();
		}
		// And wait for the entire process to finish, so we can get the exit code
		int exitCode = process.waitFor();
		return new ProcessResult(stdout.get(), stderr.get(), exitCode);
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

	/**
	 * Holds the results of a process execution: stdout, stderr and exit code.
	 */
	@Getter
	@AllArgsConstructor
	public static class ProcessResult {
		private final String stdout;
		private final String stderr;
		private final int exitCode;
	}
}
