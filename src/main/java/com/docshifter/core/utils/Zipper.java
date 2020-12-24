package com.docshifter.core.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Zipper {

	public static File zipFiles(List<File> files, File zipFilePath) {

		Logger.debug("zip filename: " + zipFilePath.toString(), null);
		int length;
		byte[] buffer = new byte[1024];

		try (FileOutputStream fos = new FileOutputStream(zipFilePath);
		     ZipOutputStream zos = new ZipOutputStream(fos)) {

			if (files != null) {
				for (File f : files) {
					if (f.isFile()) {
						Logger.debug("****************** ZIPPING FILE=" + f.getName(), null);
						try (FileInputStream in = new FileInputStream(f)) {
							zos.putNextEntry(new ZipEntry(f.getName()));
							while ((length = in.read(buffer)) > 0) {
								zos.write(buffer, 0, length);
							}
							zos.closeEntry();
						}
					}
				}
			}

		} catch (Exception e) {
			Logger.error("Error creating zip", e);
		}

		return zipFilePath;
	}

	public static File zipFolder(File sourceFolder, File zipFilePath) {
		List<File> filesToZip = Arrays.asList(Objects.requireNonNull(sourceFolder.listFiles(), "passed sourceFolder is not a folder or does not contain any files"));
		return zipFiles(filesToZip, zipFilePath);
	}
}
