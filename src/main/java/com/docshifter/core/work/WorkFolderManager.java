package com.docshifter.core.work;

import com.docshifter.core.utils.FileUtils;
import com.docshifter.core.config.Constants;
import com.docshifter.core.config.service.GeneralConfigService;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang.StringUtils;
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
@Log4j2
public class WorkFolderManager {

	private Path workfolder;
	private Path errorfolder;

	@Autowired
	public WorkFolderManager(GeneralConfigService generalConfiguration) throws ConfigurationException {

		log.debug("Temp (Work) folder param name: {}", Constants.TEMPFOLDER);
		log.debug("Error folder param name: {}", Constants.ERRORFOLDER);
		String tempFolder = generalConfiguration.getString(Constants.TEMPFOLDER);
		String errorFolder = generalConfiguration.getString(Constants.ERRORFOLDER);
		log.debug("Temp (Work) folder: {}", tempFolder);
		log.debug("Error folder: {}", errorFolder);
		
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

		String applicationName = System.getProperty("program.name");
		log.debug("App name: {}", applicationName);
		
		if ((!StringUtils.isBlank(applicationName)) && applicationName.equalsIgnoreCase("DocShifterConsole")) {
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
		if (!folderFile.exists()) {
			log.debug("{} folder: {} does not exist. Will try to create it.", workOrError, folderPath);
			try {
				Files.createDirectories(Paths.get(folderPath));
			}
			catch (IOException ioe) {
				log.error("{} folder: {} did not exist but then could not be created.", workOrError, folderPath, ioe);
				result = false;
			}
		}
		else {
			if (Files.isRegularFile(Paths.get(folderPath))) {
				log.error("{} folder on [{}] is a regular file, not a directory!", workOrError, folderPath);
				result = false;
			}
		}

		
		if (result) {
			File tst = new File(folderPath);
			// Anyhoo, this should sort the wheat from the chaff... see if the folder is writable
			if (tst.canWrite()) {
				log.debug("{} folder on [{}] is writable. We're good to go!", workOrError, folderPath);
			}
			else {
				log.error("{} folder on [{}] is not writable!", workOrError, folderPath);
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
			log.info("ERRORFOLDER IS NULL *****");
		} else {
			deletePath(folder.getErrorFolder(), force);
		}
	}

	public void copyToErrorFolder(WorkFolder folder) throws Exception{
		FileUtils.copyFolder(folder.getFolder(), folder.getErrorFolder());
	}

	private boolean deletePath(Path dir, boolean force) {
		log.warn("Into deletePath({}, {})", dir.toString(), force);
		if (Files.isDirectory(dir)) {
			log.debug("Is directory...");
			try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir)) {

				for (Path child : ds)
					if (force) {
						if (!deletePath(child, force)) {
							return false;
						}
					} else {
						log.debug("Returning false because force is not set?");
						return false;
					}
			} catch (IOException ioe) {
				log.warn("deletePath({}) in 'for Path', caught IOException.", dir.toString(), ioe);
				return false;
			}
		}
		log.debug("Will delete [{}], if exists...", dir.toString());
		try {
			Files.deleteIfExists(dir);
		} catch (IOException ioe) {
			log.warn("deletePath({}) after 'deleteIfExists', caught IOException.", dir.toString(), ioe);
			return false;
		}
		log.debug("Returning true!");
		return true;
	}

	@Override
	public String toString() {
		StringBuilder sBuf = new StringBuilder();
		sBuf.append("WorkFolderManager: ");
		sBuf.append("Errorfolder=");
		sBuf.append(this.errorfolder);
		sBuf.append(", ");
		sBuf.append("Workfolder=");
		sBuf.append(this.workfolder);
		return sBuf.toString();
	}
}
