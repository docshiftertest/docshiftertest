package com.docbyte.docshifter.util;


import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Last Modification Date: $Date$
 * 
 * @author $Author$
 * @version $Rev$
 */
public class FileUtils {
	/*
	 * public static void main(String args[]){ FileOutputStream stream=null; try
	 * { PdfReader reader= new
	 * PdfReader("C:/docshifter/destination2/Beoordeling algemeen Frans.pdf");
	 * //Document document = new Document(); stream=new
	 * FileOutputStream("C:/docshifter/temp/pdfaPLUS.pdf"); //PdfWriter writer =
	 * PdfWriter.getInstance(document,stream ); //document.open(); int n =
	 * reader.getNumberOfPages(); BaseFont bf =
	 * BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI,
	 * BaseFont.EMBEDDED); PdfStamper stamper = new PdfStamper(reader, new
	 * FileOutputStream("C:/docshifter/temp/pdfaPLUS.pdf"));
	 * stamper.insertPage(1, PageSize.A4); PdfContentByte cb =
	 * stamper.getOverContent(1); cb.beginText(); cb.setFontAndSize(bf, 18);
	 * cb.setTextMatrix(36, 770); cb.showText("Inserted this Page");
	 * cb.endText(); stamper.close(); reader.close();
	 * 
	 * } catch (Exception e) { 
	 * e.printStackTrace(); } finally{ if (stream!=null) try { stream.close(); }
	 * catch (IOException e) { 
	 * e.printStackTrace(); } } }
	 */
	public static String replaceFileExtension(String filePath, String fileFormat) {
		int dot = filePath.lastIndexOf(".");
		String result = filePath;
		if (dot != -1) {
			result = filePath.substring(0, dot + 1) + fileFormat;
		}
		return result;
	}

	/**
	 * Tests if file extension is the same as given extension
	 * 
	 * @param filePath
	 * @param extension
	 *            file-extension without .!
	 * @return true if file-extension is the same
	 */
	public static boolean testFileExtension(String filePath, String extension) {
		if (filePath == null || extension == null || filePath.length() == 0
				|| extension.length() == 0)
			return false;
		int dot = filePath.lastIndexOf(".");

		if (dot != -1) {
			return filePath.substring(dot + 1).equalsIgnoreCase(extension);
		} else
			return false;
	}

	public static String getFileExtension(String filePath) {
		if (filePath == null || filePath.length() == 0)
			return null;
		int dot = filePath.lastIndexOf(".");

		if (dot != -1) {
			return filePath.substring(dot + 1);
		} else
			return null;
	}

	public static byte[] getBytesFromFile(File file) throws IOException {
		InputStream is = new FileInputStream(file);

		// Get the size of the file
		long length = file.length();

		if (length > Integer.MAX_VALUE) {
			// File is too large
		}

		// Create the byte array to hold the data
		byte[] bytes = new byte[(int) length];

		// Read in the bytes
		int offset = 0;
		int numRead = 0;
		while (offset < bytes.length
				&& (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
			offset += numRead;
		}

		// Ensure all the bytes have been read in
		if (offset < bytes.length) {
			is.close();
			throw new IOException("Could not completely read file "
					+ file.getName());
		}

		// Close the input stream and return bytes
		is.close();
		return bytes;
	}

	public static String shortenFileName(String filename){
		String newfilename = filename;
		if (filename.length() > 40) {
			newfilename = filename.substring(0, 40);
		}
		return newfilename;
	}


	public static void dos2unix(File file) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		FileReader frdr = new FileReader(file);
		BufferedReader buff = new BufferedReader(frdr);
		int bt = buff.read();
		char c = (char) bt;
		while (bt > -1) {
			if (bt != 13)
				out.write(c);
			bt = buff.read();
			c = (char) bt;
		}
		out.write(c);
		if (c != '\n')
			out.write('\n');

		byte[] changed = out.toByteArray();
		out.close();
		buff.close();

		RandomAccessFile f = new RandomAccessFile(file, "rw");
		f.write(changed);
		f.setLength(changed.length);
		f.close();
		changed = null;
	}

	/**
	 * Returns a list of all the files located in a given folder
	 * 
	 * @param folderpath
	 *            aboslute path of the folder
	 * @return a list of files
	 */
	//@SuppressWarnings("unchecked")
	public static List getFiles(String folderpath) {
		List fileList = new ArrayList();
		File file = new File(folderpath);
		visitAllFiles(file, fileList);
		return fileList;
	}

	//@SuppressWarnings("unchecked")
	private static void visitAllFiles(File dir, List fileList) {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				visitAllFiles(new File(dir, children[i]), fileList);
			}
		} else {
			fileList.add(dir);
		}
	}

	public static void moveFile(File srcFile, File destFile) throws IOException {
		copyFile(srcFile, destFile);
		if (!srcFile.delete()) {
			throw new IOException("Unable to delete file "
					+ srcFile.getAbsolutePath());
		}
	}

	public static void copyFile(File srcFile, File destFile) throws IOException {
		if (!srcFile.isDirectory()) {
			Logger.info("copyfile-isfile", null);
			Logger.info("srcfile= " + srcFile + " destfile= " + destFile, null);
			FileInputStream inStream = null;
			FileOutputStream outStream = null;
			FileChannel inChannel = null;
			FileChannel outChannel = null;
			try {
				inStream = new FileInputStream(srcFile);
				inChannel = inStream.getChannel();
				outStream = new FileOutputStream(destFile);
				outChannel = outStream.getChannel();

				outChannel.transferFrom(inChannel, 0, inChannel.size());

			} catch (IOException e) {
				throw e;
			} finally {
				try {
					if (inChannel != null)
						inChannel.close();
					if (outChannel != null)
						outChannel.close();
					if (inStream != null)
						inStream.close();
					if (outStream != null)
						outStream.close();
				} catch (Exception ex) {
				}
			}
		}else{
			Logger.info("copyfile-isfolder--->copyfolder", null);
			copyFolder(srcFile, destFile);
		}
	}
	
	public static void copyFolder(File src, File dest) throws IOException {
		if (src.isDirectory()) {
			Logger.info("copyfolder-isdir", null);
			// if directory not exists, create it
			if (!dest.exists()) {
				dest.mkdir();
				Logger.info("Directory copied from " + src + "  to " + dest,
						null);
			}

			// list all the directory contents
			String files[] = src.list();

			for (String file : files) {
				// construct the src and dest file structure
				File srcFile = new File(src, file);
				File destFile = new File(dest, file);
				// recursive copy
				if (!srcFile.isDirectory()) {
					copyFile(srcFile, destFile);
				} else {
					copyFolder(srcFile, destFile);
				}
			}
		}
	}

	public static String createFolderStucture(String rootFolderPath,
			String folderStructurePath) {
		if (folderStructurePath == null)
			folderStructurePath = "";
		File targetFolder = new File(rootFolderPath + folderStructurePath);
		if (targetFolder.exists()) {
			return targetFolder.getAbsolutePath();
		} else if (targetFolder.mkdirs()) {
			return targetFolder.getAbsolutePath();
		} else
			return rootFolderPath;
	}

	public static boolean deleteDirectory(File dirPath) {
		if (dirPath.exists()) {
			File[] files = dirPath.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					deleteDirectory(files[i]);
				} else {
					files[i].delete();
				}
			}
		}
		return (dirPath.delete());
	}


	public static void renameFile(String filePath, String newFilePath) {
		File fileToRename = new File(filePath);
		fileToRename.renameTo(new File(newFilePath));
	}

	public static void deleteFile(String filePath) {
		File f = new File(filePath);
		if (f.exists() && !f.isDirectory()) {
			f.delete();
		}
	}

	public static String removeIllegalFilesystemCharacters(String input){
		String output = input;
		output = output.replace('/', '_');
		output = output.replace('\\', '_');
		output = output.replace('*', '_');
		output = output.replace('?', '_');
		output = output.replace(':', '_');
		output = output.replace('"', '_');
		output = output.replace('<', '_');
		output = output.replace('>', '_');
		output = output.replace('|', '_');
		output = output.replace(' ', '_');
		output = output.replace("\t", "_");
		return output;
	}

	public static void checkOrCreateFolder(Path folder) throws IOException {
		if (!Files.exists(folder)) {
			try {
				Files.createDirectories(folder);
			} catch (Exception ex) {
				throw new IOException("Folder does not exist and failed to create it", ex);
			}
		} else if (!Files.isDirectory(folder)) {
			throw new IOException("The specified floder is not a folder");
		}
	}

}