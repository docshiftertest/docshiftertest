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
	 * @param zipFile the file to unzip
	 * @param directory the directory to unzip in
	 * @param charset the charset to use for the entry file names
	 * @return list of unzipped files
	 * @throws IOException
	 */
	public static List<File> unzip(File zipFile, File directory, Charset charset) throws IOException {
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
				} else {
					newFile = Files.createFile(Paths.get(directory.getAbsolutePath(), File.separator, FileUtils.removeIllegalFilesystemCharacters(entry.getName())));
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
					} catch (IOException e) {
						log.info("Unzip failed", e);
					}
				}
			}
		} catch (IOException e) {
			log.info("Unzip failed", e);
		} catch (IllegalArgumentException e) {
			log.info("Unzip failed, most likely due to character encoding", e);
		}

		return unzippedFiles;
	}
}
