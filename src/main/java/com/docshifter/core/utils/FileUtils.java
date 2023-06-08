package com.docshifter.core.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.Writer;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
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
            return Files.isRegularFile(path);
        }
        return false;
    }

    public static boolean checkFileExist(String filePath){

        Path path = Paths.get(filePath);
        if (Files.exists(path)) {
            return Files.isRegularFile(path);
        }
        return false;
    }

    public static boolean checkFileinClasspath(String path){

        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        URL destination = classloader.getResource(path);
        return destination != null;
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
			}
        }
        else{
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

    /**
     * Gets the name minus the path from a full fileName.
     * This method will handle a file in either Unix or Windows format. The text after the last forward or backslash is returned.
     *   a/b/c.txt --> c.txt
     *   a.txt     --> a.txt
     *   a/b/c     --> c
     *   a/b/c/    --> ""
     * The output will be the same irrespective of the machine that the code is running on.
     * @param fileName the fileName to query, null returns null
     * @return The file's name without the path, or an empty string if none exists.
     *         Null bytes inside string will be removed
     */
    public static String getFilename(String fileName){
        return FilenameUtils.getName(fileName);
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

        // Create the byte array to hold the data
        byte[] bytes = new byte[(int) length];

        // Read in the bytes
        int offset = 0;
        int numRead;
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

    /**
     * Shortens a filename to default 256 chars
     * @param filename The filename to be shortened, if needed
     * @return The (possibly shortened) filename
     */
    public static String shortenFileName(String filename){
        return shortenFileName(filename, 256);
    }

    /**
     * Shortens a filename to default 256 chars
     * @param filename The filename to be shortened, if needed
     * @param length The length to which to shorten the filename
     * @return The (possibly shortened) filename
     */
    public static String shortenFileName(String filename, int length){
        String newfilename = filename;
        if (filename.length() > length) {
            newfilename = filename.substring(0, length);
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
        //Removing extra spaces as this was causing issues on windows
        output = output.trim();
		output = output.replaceAll("(\\.{2,})","_");

		//replace all control characters (DS-318)
        output = output.replaceAll("\\p{Cntrl}", "_");

        // https://stackoverflow.com/a/31976060
		if (SystemUtils.IS_OS_WINDOWS) {
            output = switch (output) {
                case "CON", "PRN", "AUX", "NUL",
                        "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9",
                        "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9" ->
                        output.concat("_");
                default -> output;
            };
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
        Path path = Paths.get(infilePath);
        String fileName = path.getFileName().toString();
        String fileNameWOExt = getNameWithoutExtension(fileName);
        String newFileName;
        Path newUniquePath = path;

        if (Files.exists(path)) {
            //Check whether it has an extension
            if (fileName.contains(".")) {
                //If it has an extension
                String extension = getExtension(infilePath);
                newFileName = shortenFileName(fileNameWOExt) + System.currentTimeMillis() + "."+ extension;
                newUniquePath = path.getParent().resolve(newFileName);
                if (Files.exists(newUniquePath)) {
                    newUniquePath = Paths.get(getUniquePath(newUniquePath.toString()));
                }
            } else {
                //If no extension
                newFileName = shortenFileName(fileNameWOExt) + System.currentTimeMillis();
                newUniquePath = path.getParent().resolve(newFileName);
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

    public static boolean checkFolderExist(String folder){
        Path path = Paths.get(folder);
        if (!Files.exists(path)) {
			return Files.isDirectory(path);
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

    /**
     * Create the folder structure
     * @deprecated
     * This method should not be used because of the typo in the name
     * <p> Use {@link FileUtils#createFolderStructure(String, String)} instead
     *
     * @param rootFolderPath Where to begin
     * @param folderStructurePath The folder structure to add on
     * @return The absolute path of the target folder if existing or created, else the root folder path
     */
    @Deprecated
    public static String createFolderStucture(String rootFolderPath,
                                               String folderStructurePath) {
        return createFolderStructure(rootFolderPath, folderStructurePath);
    }

    /**
     * Create the folder structure
     * @param rootFolderPath Where to begin
     * @param folderStructurePath The folder structure to add on
     * @return The absolute path of the target folder if existing or created, else the root folder path
     */
    public static String createFolderStructure(String rootFolderPath,
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

    /**
     * Supposed to fix crlf in a file but looks a bit weird
     * @deprecated Logic looks a bit ropey and is not scalable for large files
     * @param file The file whose cf/lfs we want to fix
     * @throws IOException If something I/O like goes wrong
     */
    @Deprecated
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
    }

    /**
     * Another crlf fixer?
     * @deprecated We should probably fix these up by making one good cr/lf fixer
     * @param file The file to 'convert' from dos linefeeds to unix linefeeds
     * @throws IOException If something I/O like goes wrong
     */
    @Deprecated
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
	 * @param closable The closeable thingy to close
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
	 * @return The contents of the file, as a String. Beware of scalability here with big files
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
	 * @return The contents of the file, as a String. Beware of scalability here with big files
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
      * Attempts to delete a file.
     * @deprecated Should not be used. It's a useless copy of deletePath and
     * does not even use the Scheduler that's passed in
	 * @param scheduler Used to schedule retries after a failure.
	 * @param dir The directory or file to delete.
     */
    @Deprecated
    public static void deleteFile(ScheduledExecutorService scheduler, Path dir) {
        log.debug("Will try to delete a file... {}", dir);
        if (!Files.isDirectory(dir) && Files.exists(dir)) {
            try {
                log.info("Deleting.. {}", dir);
                Files.delete(dir);
            } catch (IOException e) {
                log.error("An error occurred when trying to delete the file: {}", dir);
            }

        }
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
     * Writes a new json file using the object passed and the complete file path.
     *
     * @param objToBeWritten The object to be written into the json file
     * @param filePathName   The file name with the path
     */
    public static boolean writeJsonFile(Object objToBeWritten, String filePathName) {
        try (Writer writer = new FileWriter(filePathName, StandardCharsets.UTF_8)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(objToBeWritten, writer);
            return true;

        } catch (IOException ioe) {
            log.error("Could not create the file for the " + filePathName, ioe);
            return false;
        }
    }

	/**
	 * Deletes one or more lines in a file matching some content exactly. If the resulting file would be empty
	 * (ignoring empty lines) after deleting the offending content, then the entire file is deleted altogether.
	 * @param file The {@link Path} to the file to work with.
	 * @param lineToDelete The content to delete in the file.
	 * @return {@code true} if any of the specified content was found (and therefore appropriate action was taken),
	 * {@code false} otherwise. Note that even if {@code false} was returned, it could also mean that the file has
	 * now been deleted (in case the file was already empty before)!
	 * @throws IOException The file does not exist, is not a file (but likely a directory), it could not be opened for
	 * reading, or it could not be deleted (if applicable).
	 */
	public static boolean deleteLineOrFileIfEmpty(Path file, String lineToDelete) throws IOException {
		if (!Files.isRegularFile(file)) {
			throw new IOException(file + " does not exist or is not a file.");
		}

		Map<Boolean, List<String>> partitionedLines;
		try (Stream<String> lineStream = Files.lines(file)) {
				partitionedLines = lineStream.filter(StringUtils::isNotEmpty)
					.collect(Collectors.partitioningBy(line -> line.equals(lineToDelete)));
		}
		List<String> matchingLines = partitionedLines.get(true);
		List<String> nonMatchingLines = partitionedLines.get(false);

		if (nonMatchingLines.isEmpty()) {
			Files.deleteIfExists(file);
		} else if (!matchingLines.isEmpty()) {
			Files.write(file, nonMatchingLines, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
		}
		// Nothing to do if no lines had to be deleted and the file wasn't already empty

		return !matchingLines.isEmpty();
	}
}
