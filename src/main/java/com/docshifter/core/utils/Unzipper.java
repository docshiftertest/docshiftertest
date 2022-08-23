package com.docshifter.core.utils;

import lombok.extern.log4j.Log4j2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Log4j2
public class Unzipper {

	/**
	 *
	 * @see #unzip(File, File, Charset) Charset 'CP437' will be used
	 *
	 * @param zipFile the file to unzip
	 * @param directory the directory to unzip in
	 * @return list of unzipped files
	 * @throws IOException
	 */
	public static List<File> unzip(File zipFile, File directory) throws IOException {
		return unzip(zipFile, directory, Charset.forName("CP437"));
	}

	/**
	 *
	 * @see #unzip(File, File, Charset) Charset 'CP437' will be used
	 *
	 * @param zipFile the file to unzip
	 * @param directory the directory to unzip in
	 * @param retainZipFolderStructure whether to keep the folder structure as we unzip
	 * @return list of unzipped files
	 * @throws IOException
	 */
	public static List<File> unzip(File zipFile, File directory, boolean retainZipFolderStructure) throws IOException {
		return unzip(zipFile, directory, Charset.forName("CP437"), retainZipFolderStructure);
	}


	/**
	 *
	 * @param zipFile the file to unzip
	 * @param directory the directory to unzip in
	 * @param charset the charset to use for the entry file names
	 * @return list of unzipped files
	 * @throws IOException
	 */
	public static List<File> unzip(File zipFile, File directory, Charset charset) throws IOException {
		return unzip(zipFile, directory, charset, false);
	}

	/**
	 *
	 * @param zipFile the file to unzip
	 * @param directory the directory to unzip in
	 * @param charset the charset to use for the entry file names
	 * @param retainZipFolderStructure whether to keep the folder structure as we unzip
	 * @return list of unzipped files
	 * @throws IOException
	 */
	public static List<File> unzip(File zipFile, File directory, Charset charset, boolean retainZipFolderStructure) throws IOException {
		byte[] buffer = new byte[1024];
		List<File> unzippedFiles = new ArrayList<>();

		if (!directory.exists() && !directory.isDirectory()) {
			log.info("directory.exists(): {} and directory.isDirectory(): {} so will createDirectories for: {}",
					directory.exists(),
					directory.isDirectory(),
					directory.toPath());
			Files.createDirectories(directory.toPath());
		}
		for (File file : Objects.requireNonNull(directory.listFiles())) {
			boolean success = file.delete();
			if (!success) {
				log.warn("Couldn't delete file {}", file);
			}
		}

		//DS-438 use more broad char encoding to prevent problems with 'special characters'
		try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile), charset)) {
			ZipEntry entry;

			while ((entry = zis.getNextEntry()) != null) {
				Path newFile;
				if (entry.isDirectory()) {
					newFile = Files.createDirectories(Paths.get(directory.getAbsolutePath(), File.separator, entry.getName()));
				}
				else {
					if (retainZipFolderStructure) {
						String name = entry.getName();
						String[] entryParts;
						String filePath = "";
						String fileName = name;
						/*
						4.4.17.1 The name of the file, with optional relative path.
						The path stored MUST not contain a drive or device letter, or a leading slash.
						All slashes MUST be forward slashes '/' as opposed to backwards slashes '\'
						for compatibility with Amiga and UNIX file systems etc.
						If input came from standard input, there is no file name field.
						 */
						// In other words, this code is only dealing with COMPLIANT zips!
						if (name.contains("/")) {
							entryParts = name.split("/");
							for (int idx = 0; idx < entryParts.length; idx++) {
								// File path is all entryParts except the last one, which is the file name
								if (idx < entryParts.length - 1) {
									filePath += FileUtils.removeIllegalFilesystemCharacters(entryParts[idx]);
								}
								// Add a file separator between but stop when we reach the last entryPart
								// which is part of the file path
								if (idx < entryParts.length - 2) {
									filePath += File.separator;
								}
								// The last entryParts entry is (hopefully) the file name
								if (idx == entryParts.length - 1) {
									fileName = FileUtils.removeIllegalFilesystemCharacters(entryParts[idx]);
								}
							}
						}
						Files.createDirectories(Paths.get(directory.getAbsolutePath(), filePath));
						newFile = Files.createFile(
								Paths.get(directory.getAbsolutePath(), filePath, fileName));
					}
					else {
						newFile = Files.createFile(
								Paths.get(directory.getAbsolutePath(), FileUtils.removeIllegalFilesystemCharacters(entry.getName())));
					}
					unzippedFiles.add(newFile.toFile());
				}

				if (!Files.isDirectory(newFile)) {
					try (FileOutputStream output = new FileOutputStream(newFile.toFile().getAbsolutePath())) {
						int len;

						while ((len = zis.read(buffer)) > 0) {
							output.write(buffer, 0, len);
						}
						// Close output early?
						output.close();
						// Drop the reference to newFile?
						newFile = null;
					}
					catch (IOException ioe) {
						log.info("Unzip failed", ioe);
					}
				}
			}
		}
		catch (IOException ioe) {
			log.info("Unzip failed", ioe);
		}
		catch (IllegalArgumentException illy) {
			log.info("Unzip failed, most likely due to character encoding", illy);
		}
		return unzippedFiles;
	}
}
