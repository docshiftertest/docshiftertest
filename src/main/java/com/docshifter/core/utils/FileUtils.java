package com.docshifter.core.utils;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.SystemUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 *
 * Created by samnang.nop on 27/01/2016.
 *
 */
@Log4j2
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
            log.info("copyfile-isfile");
            log.info("srcfile= {} destfile= {}", srcFile, destFile);
			try (FileInputStream inStream = new FileInputStream(srcFile);
				 FileChannel inChannel = inStream.getChannel();
				 FileOutputStream outStream = new FileOutputStream(destFile);
				 FileChannel outChannel = outStream.getChannel()) {

				outChannel.transferFrom(inChannel, 0, inChannel.size());

			} catch (IOException e) {
				throw e;
			}
        }else{
            log.info("copyfile-isfolder--->copyfolder");
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
        if (filename.length() > 256) {
            newfilename = filename.substring(0, 256);
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
			return Files.isDirectory(path);
		}
        return false;
    }

    public static boolean checkFolderExist(String path){
        if (!Files.exists(Paths.get(path))) {
			return Files.isDirectory(Paths.get(path));
		}
        return false;
    }

    public static void copyFolder(Path src, Path dest) throws IOException{
		copyFolder(src.toAbsolutePath().toFile(), dest.toAbsolutePath().toFile());
	}

    public static void copyFolder(File src, File dest) throws IOException {
        if (src.isDirectory()) {
            log.info("copyfolder-isdir");
            // if directory does not exist, create it
            if (!dest.exists()) {
                boolean success = dest.mkdir();
                if (success) {
					log.info("Directory copied from {}  to {}", src, dest);
				} else {
                	log.warn("Couldn't create directory at {}", dest);
				}
            }
            // list all the directory contents
            String[] files = src.list();
            if (files == null) {
            	log.warn("Call to src.list() returned null for src: {}", src.getPath());
            }
            else {
                for (String file : files) {
                	if (file == null) {
                		log.warn("A file entry was null for src: {}", src.getPath());
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
            if (files == null) {
            	log.warn("dirPath.listFiles() returned NULL! So {} cannot be deleted.", dirPath);
            	return false;
			}
			for (File file : files) {
				if (file.isDirectory()) {
					deleteDirectory(file);
				} else {
					boolean success = file.delete();
					if (!success) {
						log.warn("Couldn't delete file {}", file);
					}
				}
			}
        }
        return dirPath.delete();
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
		for (byte datum : data) {
			char ch = (char) (datum & 0xff);
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
		File[] files = dir.listFiles();
		if (files == null) {
			log.warn("dirPath.listFiles() returned NULL! So {} cannot be cleaned.", dir);
			return;
		}
		for (File file : files) {
			if (!file.getName().equalsIgnoreCase(ignoreFolder) || !file.getName().equalsIgnoreCase(ignoreFolder)) {
				boolean success = file.delete();
				if (!success) {
					log.warn("Couldn't delete file {}", file);
				}
			}

		}
	}
	
    public static String addExtensionToFilename(String fileName, String extension) {
        Pattern p = Pattern.compile("\\." + extension);
        Matcher o = p.matcher(fileName);
        boolean fileNameContainsExtension = o.find();
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
		return fileToString(fileName, null, sizeLimit);
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
		if (logMessagePrefix == null) {
			logMessagePrefix = "";
		}
		StringBuilder sBuf = new StringBuilder();
		try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(fileName))) {
			int bytesRead;
			int totalBytesRead = 0;
			StringBuilder sInp = new StringBuilder(); 
			byte[] buffer = new byte[4096];
			while ((bytesRead = bis.read(buffer)) != -1 && totalBytesRead <= sizeLimit) {
				totalBytesRead += bytesRead;
				sInp.append(new String(buffer, 0, bytesRead));
			}
			sBuf.append(logMessagePrefix)
					.append(" File content follows: ")
					.append(NEWLINE)
					.append(sInp)
					.append(NEWLINE);
		}
		catch (IOException ioe) {
			sBuf.append(logMessagePrefix)
					.append("But then we got an IO Exception trying to read the file: [")
					.append(fileName)
					.append("]!")
					.append(NEWLINE)
					.append(ioe);
		}
		return sBuf.toString();
	}

	/**
	 * Attempts to delete a file or directory, taking into account the possibility of shenanigans happening related
	 * to calling the Java I/O API on unconventional storage types (such as NAS). For example: File.exists()
	 * or Files.isDirectory(...) being unreliable and returning false negatives.
	 * @param scheduler Used to schedule retries after a failure.
	 * @param dir The directory or file to delete.
	 * @param forceIfNotEmpty Should a directory be deleted if it is not empty?
	 * @return Whether the file/directory could be deleted immediately, without any errors. If an error did occur, it
	 * might be possible that after the backoff period the item will have been deleted successfully anyway.
	 */
	public static boolean deletePath(ScheduledExecutorService scheduler, Path dir, boolean forceIfNotEmpty) {
		return deletePath(scheduler, dir, forceIfNotEmpty, 3);
	}

	/**
	 * Attempts to delete a file or directory, taking into account the possibility of shenanigans happening related
	 * to calling the Java I/O API on unconventional storage types (such as NAS). For example: File.exists()
	 * or Files.isDirectory(...) being unreliable and returning false negatives.
	 * @param scheduler Used to schedule retries after a failure.
	 * @param dir The directory or file to delete.
	 * @param forceIfNotEmpty Should a directory be deleted if it is not empty?
	 * @param retryCounter The number of retries to allow for in case a file/directory could not be deleted. Backoff
	 *                        time is 1 minute.
	 * @return Whether the file/directory could be deleted immediately, without any errors. If an error did occur, it
	 * might be possible that after the backoff period the item will have been deleted successfully anyway.
	 */
	public static boolean deletePath(ScheduledExecutorService scheduler, Path dir, boolean forceIfNotEmpty,
									 int retryCounter) {
		return deletePath(scheduler, dir, dir, forceIfNotEmpty, retryCounter, new HashSet<>());
	}

	/**
	 * Attempts to delete a file or directory, taking into account the possibility of shenanigans happening related
	 * to calling the Java I/O API on unconventional storage types (such as NAS). For example: File.exists()
	 * or Files.isDirectory(...) being unreliable and returning false negatives.
	 * @param scheduler Used to schedule retries after a failure.
	 * @param currDir The current directory or file to delete.
	 * @param rootDir The root directory, used for tracking purposes in a recursive call.
	 * @param forceIfNotEmpty Should a directory be deleted if it is not empty?
	 * @param retryCounter The number of retries to allow for in case a file/directory could not be deleted. Backoff
	 *                        time is 1 minute.
	 * @param bypassDirs Known paths where the Files.isDirectory check needs to be bypassed and automatically assume
	 *                      it's a directory, used for tracking purposes in a recursive call.
	 * @return Whether the file/directory could be deleted immediately, without any errors. If an error did occur, it
	 * might be possible that after the backoff period the item will have been deleted successfully anyway.
	 */
	private static boolean deletePath(ScheduledExecutorService scheduler, Path currDir, Path rootDir,
									  boolean forceIfNotEmpty, int retryCounter, Set<Path> bypassDirs) {
		IOException encounteredIOE = null;
		log.info("Into deletePath({}, {}, {}, {}, bypassDirs)", currDir, rootDir, forceIfNotEmpty, retryCounter);
		// If it's a directory, try to recursively delete all files and subdirectories inside of it
		if (Files.isDirectory(currDir) || bypassDirs.contains(currDir)) {
			log.debug("Is directory...");
			try (Stream<Path> stream = Files.list(currDir)) {
				boolean result = stream.allMatch(child -> {
					if (!forceIfNotEmpty) {
						log.info("Returning false because the directory contains files and forceIfNotEmpty is false?");
						return false;
					}
					return deletePath(scheduler, child, rootDir, true, retryCounter, bypassDirs);
				});
				if (!result) {
					return false;
				}
			} catch (NotDirectoryException ndex) {
				// False positive? Move on and try to delete the file anyways
				log.warn("{} was somehow not a directory?", currDir, ndex);
			} catch (IOException ioe) {
				// This is a bit more serious... Might be fixable though by trying later as it might be a temporary
				// problem with the underlying storage
				log.warn("deletePath({}) in 'for Path', caught IOException.", currDir, ioe);
				encounteredIOE = ioe;
			}
		}
		// Now try to delete the file/directory itself (given that we didn't run into the serious error before)
		log.debug("Will delete [{}], if exists...", currDir);
		if (encounteredIOE == null) {
			try {
				Files.deleteIfExists(currDir);
			} catch (IOException ioe) {
				log.warn("deletePath({}) after 'deleteIfExists', caught IOException.", currDir, ioe);
				encounteredIOE = ioe;
			}
		}

		// Everything still OK? Great, we successfully deleted it!
		if (encounteredIOE == null) {
			bypassDirs.remove(currDir);
			log.debug("Returning true!");
			return true;
		}

		// We've reached the "uh-oh, stuff's not going so great" part of this method...
		final int finalRetryCounter;
		// If we tried to delete a directory that somehow was not empty, it must mean that the Files.isDirectory(currDir)
		// check lied to us and returned a false negative!
		if (encounteredIOE instanceof DirectoryNotEmptyException) {
			// But if we're already bypassing the check this time, then something is seriously off, so let's not retry
			if (bypassDirs.contains(currDir)) {
				log.error("Getting DirectoryNotEmptyException for this directory even after bypassing the " +
						"isDirectory check! This should absolutely not happen! Returning false...");
				return false;
			}

			log.warn("And it was a DirectoryNotEmptyException! This should not happen! Will circumvent the " +
					"directory check next time.");
			bypassDirs.add(currDir);
			finalRetryCounter = retryCounter;
		} else {
			// Any other errors: subtract 1 from the retryCounter and hope that it works out next time
			finalRetryCounter = retryCounter - 1;
		}

		if (retryCounter > 0) {
			log.warn("Will retry deletion in 1 minute...");
			scheduler.schedule(() ->
				// Retry deletion all the way from the beginning/root again
				deletePath(scheduler, rootDir, rootDir, forceIfNotEmpty, finalRetryCounter, bypassDirs),
					1, TimeUnit.MINUTES);
		} else {
			log.error("There are no more retries left! {} failed to delete!", currDir);
		}
		return false;
	}

	/**
	 * Gets the output (stdout or stderr) as a List of Strings
	 * @param strm An input stream (normally stdout or stderr of a command)
	 * @return List<String> The lines of (error) output
	 * @throws IOException
	 */
	public static List<String> getOutputLines(InputStream strm) throws IOException {
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
		return outLines;
	}
}
