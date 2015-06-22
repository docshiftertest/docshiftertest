package com.docbyte.docshifter.work;

import com.docbyte.docshifter.config.ConfigurationServer;
import com.docbyte.docshifter.config.Constants;
import com.docbyte.docshifter.config.GeneralConfigurationBean;
import com.docbyte.docshifter.util.FileUtils;

import javax.naming.ConfigurationException;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by michiel.vandriessche@docbyte.com on 6/11/15.
 */
public class WorkFolderManager {

	private static WorkFolderManager instance = null;

	private Path workfolder;



	public static WorkFolderManager getInstance() throws ConfigurationException {
		if (instance == null) {
			instance = new WorkFolderManager();
		}
		return instance;
	}


	public WorkFolderManager() throws ConfigurationException {
		GeneralConfigurationBean cfg = ConfigurationServer.getGeneralConfiguration();
		workfolder = Paths.get(cfg.getString(Constants.TEMPFOLDER)).toAbsolutePath();

		if (!Files.isDirectory(workfolder)) {
			throw new ConfigurationException("Workfolder is badly configured");
		}

	}


	public synchronized WorkFolder getNewWorkfolder(String name) throws IOException {

		return new WorkFolder(getNewPath(workfolder, name));
	}

	public synchronized WorkFolder getNewWorkfolder(WorkFolder root, String name) throws IOException {


		return new WorkFolder(getNewPath(root.getFolder(), name), root);
	}



	private Path getNewPath(Path root, String name)  throws IOException {

		Files.createDirectories(workfolder);

		name = FileUtils.removeIllegalFilesystemCharacters(name);

		Path specificWorkFolder = workfolder.resolve(name);

		while (Files.exists(specificWorkFolder)) {
			specificWorkFolder = workfolder.resolve(name + System.currentTimeMillis());
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

	private boolean deletePath(Path dir, boolean force) {
		if (Files.isDirectory(dir)) {
			try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir)) {

				for (Path child : ds)
					if (force) {
						if (!deletePath(child, force)) {
							return false;
						}
					} else {
						return false;
					}
			} catch (IOException ex) {
				return false;
			}
		}
		try {
			Files.deleteIfExists(dir);
		} catch (IOException ex) {
			return false;
		}
		return true;
	}
}
