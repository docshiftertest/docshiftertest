package com.docshifter.core.work;

import com.docbyte.utils.FileUtils;
import com.docshifter.core.config.Constants;
import com.docshifter.core.config.service.GeneralConfigService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.naming.ConfigurationException;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by michiel.vandriessche@docbyte.com on 6/11/15.
 */
@Service
public class WorkFolderManager {

	private static final Logger logger = Logger.getLogger(WorkFolderManager.class);

	private Path workfolder;
	private Path errorfolder;

	@Autowired
	public WorkFolderManager(GeneralConfigService generalConfiguration) throws ConfigurationException {

		logger.debug("Temp (Work) folder param name: " + Constants.TEMPFOLDER);
		logger.debug("Error folder param name: " + Constants.ERRORFOLDER);
		String tempFolder = generalConfiguration.getString(Constants.TEMPFOLDER);
		String errorFolder = generalConfiguration.getString(Constants.ERRORFOLDER);
		logger.debug("Temp (Work) folder: " + tempFolder);
		logger.debug("Error folder: " + errorFolder);
		
		boolean workFolderResult = validateFolder("Work", tempFolder);
		boolean errorFolderResult = validateFolder("Error", errorFolder);
		if (!workFolderResult) {
			checkFatal("Work", tempFolder);
		}
		else {
			workfolder = Paths.get(tempFolder).toAbsolutePath();
		}
		if (!errorFolderResult) {
			checkFatal("Error", errorFolder);
		}
		else {
			errorfolder = Paths.get(errorFolder).toAbsolutePath();
		}
	}

	private void checkFatal(String workOrError, String folderPath) throws ConfigurationException {
		String errorMessage = "There is an error with the configuration of the " 
				+ workOrError 
				+ " folder [" 
				+ folderPath 
				+ "]! Please check and correct";
		Exception exc = new Exception();
		exc.fillInStackTrace();
		StackTraceElement[] traces = exc.getStackTrace();
		StackTraceElement trace = traces[traces.length - 1];
		logger.debug(trace.toString());
		if (trace.toString().startsWith("com.docshifter.console.DocShifterConsole")) {
			logger.warn(errorMessage);
		}
		else {
			logger.error(errorMessage);
			throw new ConfigurationException(errorMessage);
		}
	}

	private boolean validateFolder(String workOrError, String folderPath) {
		boolean result = true;
		
		while (folderPath.endsWith("/") || folderPath.endsWith("\\")) {
			folderPath = folderPath.substring(0, folderPath.length() - 1);
		}
		File folderFile = new File(folderPath);
		if (!folderFile.exists()) {
			logger.debug(workOrError + "folder: " + folderPath + " does not exist. Will try to create it.");
			try {
				Files.createDirectories(Paths.get(folderPath));
			}
			catch (IOException ioe) {
				logger.error(workOrError + "folder: " + folderPath + " did not exist but then could not be created. " +
						"IOException was: " + ioe);
				result = false;
			}
		}
		else {
			if (Files.isRegularFile(Paths.get(folderPath))) {
				logger.error(workOrError + "folder on [" + folderPath + "] is a regular file, not a directory!");
				result = false;
			}
		}

		
		if (result) {
			File tst = new File(folderPath);
			// Anyhoo, this should sort the wheat from the chaff... see if the folder is writable
			if (tst.canWrite()) {
				logger.debug(workOrError + "folder on [" + folderPath + "] is writable. We're good to go!");
			}
			else {
				logger.error(workOrError + "folder on [" + folderPath + "] is not writable!");
				result = false;
			}
		}
		return result;
	}

	public synchronized WorkFolder getNewWorkfolder(String name) throws IOException {
		return new WorkFolder(getNewPath(workfolder, name), getNewPath(errorfolder, name));
	}

	public synchronized WorkFolder getNewWorkfolder(WorkFolder root, String name) throws IOException {
		return new WorkFolder(getNewPath(root.getFolder(), name), getNewPath(root.getErrorFolder(), name), root);
	}


	private Path getNewPath(Path root, String name)  throws IOException {

		name = FileUtils.removeIllegalFilesystemCharacters(name);

		Path specificWorkFolder = root.resolve(name);

		while (Files.exists(specificWorkFolder)) {
			specificWorkFolder = root.resolve(name + System.currentTimeMillis());
		}

		Files.createDirectory(specificWorkFolder);
		return specificWorkFolder;
	}

	public void deleteWorkfolder(WorkFolder folder) {
		deleteWorkfolder(folder, false);
	}

	public void deleteWorkfolder(WorkFolder folder, boolean force) {
		if (!folder.isRoot()) {
			deleteWorkfolder(folder.getParent(), force);
			folder.setParent(null);
		}
		deletePath(folder.getFolder(), force);
	}

	public void deleteErrorfolder(WorkFolder folder){
		deleteErrorfolder(folder, false);
	}

	public void deleteErrorfolder(WorkFolder folder, boolean force) {
		if (!folder.isRoot()) {
			deleteErrorfolder(folder.getParent(), force);
			folder.setParent(null);
		}

		if (folder.getErrorFolder() == null){
			logger.info("ERRORFOLDER IS NULL *****", null);
		} else {
			deletePath(folder.getErrorFolder(), force);
		}
	}

	public void copyToErrorFolder(WorkFolder folder) throws Exception{
		FileUtils.copyFolder(folder.getFolder(), folder.getErrorFolder());
	}

	private boolean deletePath(Path dir, boolean force) {
		logger.warn("Into deletePath(" + dir.toString() + ", " + force +")");
		if (Files.isDirectory(dir)) {
			logger.debug("Is directory...");
			try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir)) {

				for (Path child : ds)
					if (force) {
						if (!deletePath(child, force)) {
							return false;
						}
					} else {
						logger.debug("Returning false because force is not set?");
						return false;
					}
			} catch (IOException ioe) {
				logger.warn("deletePath(" + dir.toString() + ") in 'for Path', caught IOException: " + ioe);
				return false;
			}
		}
		logger.debug("Will delete [" + dir.toString() + "], if exists...");
		try {
			Files.deleteIfExists(dir);
		} catch (IOException ioe) {
			logger.warn("deletePath(" + dir.toString() + ") after 'deleteIfExists', caught IOException: " + ioe);
			return false;
		}
		logger.debug("Returning true!");
		return true;
	}
}
