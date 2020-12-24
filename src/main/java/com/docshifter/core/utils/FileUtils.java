package com.docshifter.core.utils;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.SystemUtils;

import java.io.*;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * Created by samnang.nop on 27/01/2016.
 *
 */
public final class FileUtils {
	
	private static final String NEWLINE = System.getProperty("line.separator");

    // FILE UTILS
    public static boolean checkFileExist(Path path){

        if (Files.exists(path)) {
            if (Files.isRegularFile(path)) {
                return true;
            }
            return false;
        }
        return false;
    }

    public static boolean checkFileExist(String path){

        if (Files.exists(Paths.get(path))) {
            if (Files.isRegularFile(Paths.get(path))) {
                return true;
            }
            return false;
        }
        return false;
    }

    public static boolean checkFileinClasspath(String path){

        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        URL destination = classloader.getResource(path);
        if(destination == null) {
            return false;
        }
        return true;
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

    public static void moveFile(File srcFile, File destFile) throws IOException {
        copyFile(srcFile, destFile);
        if (!srcFile.delete()) {
            throw new IOException("Unable to delete file "
                    + srcFile.getAbsolutePath());
        }
    }

    public static String getExtension(String fileName){
        return FilenameUtils.getExtension(fileName);
    }

    public static String getExtension(Path fileName){
        return FilenameUtils.getExtension(fileName.toString());
    }

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

    public static String replaceFileExtension(String filePath, String fileFormat) {
        int dot = filePath.lastIndexOf(".");
        String result = filePath;
        if (dot != -1) {
            result = filePath.substring(0, dot + 1) + fileFormat;
        }
        return result;
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

    public static String getNameWithoutExtension(String filename){
        if (filename.contains(".")) {
            return filename.substring(0, filename.lastIndexOf("."));
        }
        return filename;
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
		output = output.replaceAll("(\\.{2,})","_");

		//replace all control characters (DS-318)
        output = output.replaceAll("\\p{Cntrl}", "_");

        // https://stackoverflow.com/a/31976060
		if (SystemUtils.IS_OS_WINDOWS) {
			switch (output) {
				case "CON":
				case "PRN":
				case "AUX":
				case "NUL":
				case "COM1":
				case "COM2":
				case "COM3":
				case "COM4":
				case "COM5":
				case "COM6":
				case "COM7":
				case "COM8":
				case "COM9":
				case "LPT1":
				case "LPT2":
				case "LPT3":
				case "LPT4":
				case "LPT5":
				case "LPT6":
				case "LPT7":
				case "LPT8":
				case "LPT9":
					output = output.concat("_");
					break;
			}
		}

		if (output.equals(".")) {
			output = "(.)";
		}
		if (output.equals("..")) {
			output = "(..)";
		}

		return output;
	}

    public static String getUniquePath(String infilePath){
        String fileName = Paths.get(infilePath).getFileName().toString();
        String fileNameWOExt = getNameWithoutExtension(fileName);
        String newFileName;
        Path newUniquePath = Paths.get(infilePath);

        if (Files.exists(Paths.get(infilePath))) {
            //Check whether it has an extension
            if (fileName.contains(".")) {
                //If it has an extension
                String extension = getExtension(infilePath);
                newFileName = shortenFileName(fileNameWOExt) + System.currentTimeMillis() + "."+ extension;
                newUniquePath = Paths.get(infilePath).getParent().resolve(newFileName);
                if (Files.exists(newUniquePath)) {
                    newUniquePath = Paths.get(getUniquePath(newUniquePath.toString()));
                }
            } else {
                //If no extension
                newFileName = shortenFileName(fileNameWOExt) + System.currentTimeMillis();
                newUniquePath = Paths.get(infilePath).getParent().resolve(newFileName);
                if (Files.exists(newUniquePath)) {
                    newUniquePath = Paths.get(getUniquePath(newUniquePath.toString()));
                }
            }
        }

        return newUniquePath.toString();
    }

    //FOLDER UTILS
    public static boolean checkFolderExist(Path path){

        if (!Files.exists(path)) {
            if (Files.isDirectory(path)) {
                return true;
            }
            return false;
        }
        return false;
    }

    public static boolean checkFolderExist(String path){

        if (!Files.exists(Paths.get(path))) {
            if (Files.isDirectory(Paths.get(path))) {
                return true;
            }
            return false;
        }
        return false;
    }

    public static void copyFolder(Path src, Path dest) throws IOException{
		copyFolder(src.toAbsolutePath().toFile(), dest.toAbsolutePath().toFile());
	}

    public static void copyFolder(File src, File dest) throws IOException {
        if (src.isDirectory()) {
            Logger.info("copyfolder-isdir", null);
            // if directory does not exist, create it
            if (!dest.exists()) {
                dest.mkdir();
                Logger.info("Directory copied from " + src + "  to " + dest,
                        null);
            }
            // list all the directory contents
            String files[] = src.list();
            if (files == null) {
            	Logger.warn("Call to src.list() returned null for src: " + src.getPath(), null);
            }
            else {
                for (String file : files) {
                	if (file == null) {
                		Logger.warn("A file entry was null for src: " + src.getPath(), null);
                	}
                	else {
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
        }
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

    public static void fixCrLf(File file) throws IOException {
        RandomAccessFile in = new RandomAccessFile(file, "r");
        byte[] data = new byte[(int) file.length()];
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        in.readFully(data);
        in.close();

        // int line = 1;
        for (int i = 0; i < data.length; i++) {
            char ch = (char) (data[i] & 0xff);
            if (ch > 127) {
                return;
            } else if (ch < 32) {
                if (ch == '\n') {
                    out.write('\r');
                    out.write(ch);
                    // line++;
                }
            } else {
                out.write(ch);
            }
        }
        byte[] changed = out.toByteArray();
        if (ByteUtils.compareNotNull(data, changed) != 0) {
            RandomAccessFile f = new RandomAccessFile(file, "rw");
            f.write(changed);
            f.setLength(changed.length);
            f.close();
        }
        changed = null;
        data = null;
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
    
	public static void cleanDirectory(File dir, String ignoreFolder) {
		for (File file : dir.listFiles()) {
			if (!file.getName().equalsIgnoreCase(ignoreFolder) || !file.getName().equalsIgnoreCase(ignoreFolder)) {
				file.delete();
			}

		}
	}
	
    public static String addExtensionToFilename(String fileName, String extension) {
        Pattern p = Pattern.compile(new StringBuffer().append("\\.").append(extension).toString());
        Matcher o = p.matcher(fileName);
        Boolean fileNameContainsExtension = o.find();
        if (!fileNameContainsExtension) {
            fileName = fileName.concat(".").concat(extension);
        }
        return fileName;
    }
    
    /**
	 * Uses BufferedStream to copy a file
	 * @param source A Path object representing the input
	 * @param target A Path object representing the output
	 */
    public static void bufferedStreamsCopy(Path source, Path target) {
		bufferedStreamsCopy(new File(source.toString()), new File(target.toString()));
	}
	/**
	 * Uses BufferedStream to copy a file
	 * @param source A File object representing the input
	 * @param target A Path object representing the output
	 */
    public static void bufferedStreamsCopy(File source, Path target) {
		bufferedStreamsCopy(source, new File(target.toString()));
	}
	/**
	 * Uses BufferedStream to copy a file
	 * @param source A Path object representing the input
	 * @param target A File object representing the output
	 */
	public static void bufferedStreamsCopy(Path source, File target) {
		bufferedStreamsCopy(new File(source.toString()), target);
	}
	/**
	 * Uses BufferedStream to copy a file, this is the method that does the work
	 * Should be a pretty efficient copy
	 * TODO: compare with and benchmark the copyFile() method?
	 * @param source A File object representing the input
	 * @param target A File object representing the output
	 */
	public static void bufferedStreamsCopy(File source, File target) {
		InputStream fin = null;
		OutputStream fout = null;
		try {
			fin = new BufferedInputStream(new FileInputStream(source.toString()));
			fout = new BufferedOutputStream(new FileOutputStream(target));
			int data;
			while ((data = fin.read()) != -1) {
				fout.write(data);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close(fin);
			close(fout);
		}
	}
	/**
	 * Internally used method to close anything that's closable
	 * @param closable
	 */
	private static void close(Closeable closable) {
		if (closable != null) {
			try {
				closable.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	/**
	 * Converts a file to a String with no message prefixed. Be careful that you don't send any really
	 * large files to this method.
	 * @param fileName The file to 'convert' to a String
	 * @param sizeLimit give a limit so we don't read all of a HUGE file into a String
	 * @return
	 */
	public static String fileToString(String fileName, int sizeLimit) {
		return fileToString(fileName, sizeLimit);
	}

	/**
	 * Converts a file to a String with an optional message. Be careful that you don't send any really
	 * large files to this method.
	 * @param fileName The file to 'convert' to a String
	 * @param logMessagePrefix An optional prefix to the content
	 * @param sizeLimit give a limit so we don't read all of a HUGE file into a String
	 * @return
	 */
	public static String fileToString(String fileName, String logMessagePrefix, int sizeLimit) {
		StringBuilder sBuf = new StringBuilder();
		try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(new File(fileName)))) {
			int bytesRead;
			int totalBytesRead = 0;
			StringBuilder sInp = new StringBuilder(); 
			byte[] buffer = new byte[4096];
			while ((bytesRead = bis.read(buffer)) != -1 && totalBytesRead <= sizeLimit) {
				totalBytesRead += bytesRead;
				sInp.append(new String(buffer, 0, bytesRead));
			}
			if (logMessagePrefix != null) {
				sBuf.append(logMessagePrefix);
			}
			sBuf.append(" File content follows: ");
			sBuf.append(NEWLINE);
			sBuf.append(sInp);
			sBuf.append(NEWLINE);
		}
		catch (IOException ioe) {
			sBuf.append(logMessagePrefix);
			sBuf.append("But then we got an IO Exception trying to read the file: [" + fileName + "]!");
			sBuf.append(NEWLINE);
			sBuf.append(ioe);
		}
		return sBuf.toString();
	}
}
