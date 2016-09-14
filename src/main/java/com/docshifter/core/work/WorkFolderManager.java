package com.docshifter.core.work;

import com.docbyte.docshifter.util.Logger;
import com.docbyte.utils.FileUtils;
import com.docshifter.core.config.Constants;
import com.docshifter.core.config.service.GeneralConfigService;
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

	private static WorkFolderManager instance = null;

	private Path workfolder;
	private Path errorfolder;


	@Autowired
	public WorkFolderManager(GeneralConfigService generalConfiguration) throws ConfigurationException {

		System.out.println(generalConfiguration);
		System.out.println(Constants.TEMPFOLDER);
		System.out.println(generalConfiguration.getString(Constants.TEMPFOLDER));
		System.out.println(Paths.get(generalConfiguration.getString(Constants.TEMPFOLDER)));
		System.out.println(Paths.get(generalConfiguration.getString(Constants.TEMPFOLDER)).toAbsolutePath());
		System.out.println(generalConfiguration);
		System.out.println(Constants.ERRORFOLDER);
		System.out.println(generalConfiguration.getString(Constants.ERRORFOLDER));
		System.out.println(Paths.get(generalConfiguration.getString(Constants.ERRORFOLDER)));
		System.out.println(Paths.get(generalConfiguration.getString(Constants.ERRORFOLDER)).toAbsolutePath());



		workfolder = Paths.get(generalConfiguration.getString(Constants.TEMPFOLDER)).toAbsolutePath();
		errorfolder = Paths.get(generalConfiguration.getString(Constants.ERRORFOLDER)).toAbsolutePath();

		if (!Files.isDirectory(workfolder)) {
			throw new ConfigurationException("Workfolder is badly configured");
		}

		if (!Files.isDirectory(errorfolder)) {
			throw new ConfigurationException("Errorfolder is badly configured");
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
			Logger.info("ERRORFOLDER IS NULL *****", null);
		} else {
			deletePath(folder.getErrorFolder(), force);
		}


	}

	public void copyToErrorFolder(WorkFolder folder) throws Exception{
		FileUtils.copyFolder(folder.getFolder(), folder.getErrorFolder());
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
