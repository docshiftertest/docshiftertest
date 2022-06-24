package com.docshifter.core.work;

import com.docshifter.core.config.Constants;
import com.docshifter.core.config.services.GeneralConfigService;
import com.docshifter.core.utils.FileUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.naming.ConfigurationException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by michiel.vandriessche@docbyte.com on 6/11/15.
 */
@Service
@Log4j2
public class WorkFolderManager {

	private Path workfolder;
	private Path errorfolder;
	private final ScheduledExecutorService scheduler;

	@Autowired
	public WorkFolderManager(GeneralConfigService generalConfiguration, ScheduledExecutorService scheduler) throws ConfigurationException {
		this.scheduler = scheduler;
		String applicationName = System.getProperty("program.name");
		log.debug("App name: {}", applicationName);
		boolean errorsAreWarnings;
		// If we're in Metrics we don't even need to check the work/error folders
		if ("DocShifterMetrics".equalsIgnoreCase(applicationName)) {
			return;
		}
		// If we're in Console we should only warn if error or work folder bad, so user has chance to fix it in Admin Console
		else if ("DocShifterConsole".equalsIgnoreCase(applicationName)) {
			errorsAreWarnings = true;
		}
		else {
			// All bets are off, we'll crash and burn on a bad work or error folder...
			errorsAreWarnings = false;
		}

		log.debug("Temp (Work) folder param name: {}", Constants.TEMPFOLDER);
		log.debug("Error folder param name: {}", Constants.ERRORFOLDER);
		String tempFolder = generalConfiguration.getString(Constants.TEMPFOLDER);
		String errorFolder = generalConfiguration.getString(Constants.ERRORFOLDER);
		log.debug("Temp (Work) folder: {}", tempFolder);
		log.debug("Error folder: {}", errorFolder);
		
		boolean workFolderResult = validateFolder("Work", tempFolder);
		boolean errorFolderResult = validateFolder("Error", errorFolder);
		if (!workFolderResult) {
			checkFatal("Work", tempFolder, errorsAreWarnings);
		}
		else {
			workfolder = Paths.get(tempFolder).toAbsolutePath();
		}
		if (!errorFolderResult) {
			checkFatal("Error", errorFolder, errorsAreWarnings);
		}
		else {
			errorfolder = Paths.get(errorFolder).toAbsolutePath();
		}
	}

	private void checkFatal(String workOrError, String folderPath, boolean errorsAreWarnings) throws ConfigurationException {
		String errorMessage = "There is an error with the configuration of the " 
				+ workOrError 
				+ " folder [" 
				+ folderPath 
				+ "]! Please check and correct";

		if (errorsAreWarnings) {
			log.warn(errorMessage);
		}
		else {
			log.error(errorMessage);
			throw new ConfigurationException(errorMessage);
		}
	}

	private boolean validateFolder(String workOrError, String folderPath) {
		boolean result = true;
		
		while (folderPath.endsWith("/") || folderPath.endsWith("\\")) {
			folderPath = folderPath.substring(0, folderPath.length() - 1);
		}
		File folderFile = new File(folderPath);
		if (folderFile.exists()) {
			// Useless if it exists but is not a folder...
			if (Files.isRegularFile(Paths.get(folderPath))) {
				log.error("{} folder on {} is a regular file, not a directory so cannot be used!", workOrError, folderPath);
				result = false;
			}
		}
		else {
			// Java may incorrectly report that the folder does not exist if it's on a network, so allow for that
			log.info("Java thinks {} folder: {} does not exist but it could be a network folder, so we'll first just try to create it.", workOrError, folderPath);
			try {
				Files.createDirectories(Paths.get(folderPath));
			}
			catch (IOException ioe) {
				log.info("{} folder: {} did not exist but then could not be created. This may be OK if it's a network folder...", workOrError, folderPath, ioe);
			}
		}
		// Either the folder didn't exist and we created it, or it did exist and java wrongly reported it didn't (network folder), or
		// it didn't exist and our creation attempt failed (permissions, bad path?)
		// Let's now see if we can create and then delete a test file in the folder...
		String tempFileName = UUID.randomUUID() + ".tmp";
		try {
			Files.createFile(Paths.get(folderPath, tempFileName));
		}
		catch (IOException ioe) {
			log.error("{} Folder: Could not create a test file {} on the folder: {} so the folder cannot be used.", workOrError, tempFileName, folderPath, ioe);
			result = false;
		}
		// We also have to be able to delete from the Work and Error folders, otherwise it'll all go hideously wrong...
		try {
			Files.deleteIfExists(Paths.get(folderPath, tempFileName));
		}
		catch (IOException ioe) {
			log.error("{} Folder: Could not delete the test file {} from the folder: {} so the folder cannot be used.", workOrError, tempFileName, folderPath, ioe);
			result = false;
		}
		return result;
	}

	public synchronized WorkFolder getNewWorkfolder(String name) throws IOException {
		return new WorkFolder(getNewPath(workfolder, name), getNewPath(errorfolder, name));
	}

	public synchronized WorkFolder getNewWorkfolder(WorkFolder root, String name) throws IOException {
		return new WorkFolder(getNewPath(root.getFolder(), name), getNewPath(root.getErrorFolder(), name), root);
	}


	private synchronized Path getNewPath(Path root, String name)  throws IOException {

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

	public void deleteWorkfolder(WorkFolder folder, boolean forceIfNotEmpty) {
		if (!folder.isRoot()) {
			deleteWorkfolder(folder.getParent(), forceIfNotEmpty);
			folder.setParent(null);
		}
		FileUtils.deletePath(scheduler, folder.getFolder(), forceIfNotEmpty);
	}

	public void deleteErrorfolder(WorkFolder folder){
		deleteErrorfolder(folder, false);
	}

	public void deleteErrorfolder(WorkFolder folder, boolean forceIfNotEmpty) {
		if (!folder.isRoot()) {
			deleteErrorfolder(folder.getParent(), forceIfNotEmpty);
			folder.setParent(null);
		}

		if (folder.getErrorFolder() == null){
			log.info("ERRORFOLDER IS NULL *****");
		} else {
			FileUtils.deletePath(scheduler, folder.getErrorFolder(), forceIfNotEmpty);
		}
	}

	public void copyToErrorFolder(WorkFolder folder) throws Exception{
		FileUtils.copyFolder(folder.getFolder(), folder.getErrorFolder());
	}

	/**
	 * Create a WorkFolder and copy the files inside another WorkFolder
	 *
	 * @param sourceWorkFolder The source workFolder
	 * @param targetWorkFolder The new workFolder to copy the source WorkFolder and all its files to
	 * @return Path the path to the new WorkFolder
	 */
	public Path copyWorkFolder(WorkFolder sourceWorkFolder, WorkFolder targetWorkFolder){
		try {
			Path newPath = targetWorkFolder.getFolder();
			org.apache.commons.io.FileUtils.copyDirectory(sourceWorkFolder.getFolder().toFile(),
					newPath.toFile());
			return newPath;
		}
		catch (IOException ioe) {
			log.error("Wasn't possible to copy the WorkFolder", ioe);
			return null;
		}
	}


	@Override
	public String toString() {
		return "WorkFolderManager: " +
				"Errorfolder=" +
				this.errorfolder +
				", " +
				"Workfolder=" +
				this.workfolder;
	}
}
