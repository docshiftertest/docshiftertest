package com.docshifter.core.work;

import com.docbyte.utils.FileUtils;
import com.docshifter.core.config.Constants;
import com.docshifter.core.config.service.GeneralConfigService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.naming.ConfigurationException;
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

		logger.info(generalConfiguration);
		logger.info(Constants.TEMPFOLDER);
		logger.info(generalConfiguration.getString(Constants.TEMPFOLDER));
		logger.info(Paths.get(generalConfiguration.getString(Constants.TEMPFOLDER)));
		logger.info(Paths.get(generalConfiguration.getString(Constants.TEMPFOLDER)).toAbsolutePath());
		logger.info(generalConfiguration);
		logger.info(Constants.ERRORFOLDER);
		logger.info(generalConfiguration.getString(Constants.ERRORFOLDER));
		logger.info(Paths.get(generalConfiguration.getString(Constants.ERRORFOLDER)));
		logger.info(Paths.get(generalConfiguration.getString(Constants.ERRORFOLDER)).toAbsolutePath());

		workfolder = Paths.get(generalConfiguration.getString(Constants.TEMPFOLDER)).toAbsolutePath();
		errorfolder = Paths.get(generalConfiguration.getString(Constants.ERRORFOLDER)).toAbsolutePath();

		if (!Files.isDirectory(workfolder)) {
			try {
				Files.createDirectories(workfolder);
			}
			catch (IOException ioe) {
				logger.error("Trying to create workfolder at [" + workfolder + "] got IOException: " + ioe);
				throw new ConfigurationException("Workfolder is badly configured: " + workfolder);
			}
		}

		if (!Files.isDirectory(errorfolder)) {
			try {
				Files.createDirectories(errorfolder);
			}
			catch (IOException ioe) {
				logger.error("Trying to create errorfolder at [" + errorfolder + "] got IOException: " + ioe);
				throw new ConfigurationException("Errorfolder is badly configured: " + errorfolder);
			}
		}
	}

	public synchronized WorkFolder getNewWorkfolder(String name) throws IOException {
		return new WorkFolder(getNewPath(workfolder, name), getNewPath(errorfolder, name));
	}

	public synchronized WorkFolder getNewWorkfolder(WorkFolder root, String name) throws IOException {
		return new WorkFolder(getNewPath(root.getFolder(), name), getNewPath(root.getErrorFolder(), name), root);
	}


	private Path getNewPath(Path root, String name)  throws IOException {

		//Files.createDirectories(root);

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
