package com.docshifter.core.work;

import com.docshifter.core.utils.FileUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang.StringUtils;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Created by michiel.vandriessche@docbyte.com on 6/11/15.
 */
@Log4j2
public class WorkFolder implements Serializable {
	private static final long serialVersionUID = 7938321829497848697L;
	/**
	 * Directories present in the work folder which names' are prefixed by this String hold a special meaning to
	 * DocShifter, so users should not be allowed to specify a group name with this prefix and modules shouldn't create
	 * subdirectories in the work folder with this prefix unless they know what they're doing...
	 */
	public static final String SPECIAL_DIRECTORY_PREFIX = "__DS__";
	/**
	 * Prefix denoting a special branching directory in a work folder. All files/subdirectories that need to follow a
	 * specific branch path will be grouped together in such a directory.
	 */
	public static final String BRANCH_DIRECTORY_PREFIX = SPECIAL_DIRECTORY_PREFIX + "branch-";
	private Path folder;
	private WorkFolder parent;
	private Path errorFolder;

	public WorkFolder() {

	}

	public WorkFolder(Path workfolder, Path errorFolder, WorkFolder parent) {
		this.parent = parent;
		// Enforce the folders to an absolute path because relative paths might give us issues when modules get a new
		// file/folder path in this WF and then call out to other modules with this generated path. Especially in
		// tests, where each module has their own separate target/test-classes folder...
		this.errorFolder = errorFolder.toAbsolutePath();
		this.folder = workfolder.toAbsolutePath();
	}

	public WorkFolder(Path workfolder, Path errorFolder) {
		this(workfolder, errorFolder, null);
	}

	@JsonIgnore
	public boolean isRoot() {
        return parent == null;
    }


	public Path getErrorFolder() {
		return errorFolder;
	}

	public void setErrorFolder(Path errorFolder) {
		this.errorFolder = errorFolder.toAbsolutePath();
	}

	public WorkFolder getParent() {
		return parent;
	}

	public void setParent(WorkFolder parent) {
		this.parent = parent;
	}

	public Path getFolder() {
		return folder;
	}

	public void setFolder(Path folder) {
		this.folder = folder.toAbsolutePath();
	}

	public String toString() {
		return folder.toString();
	}

	public Path getNewFilePath(String filename, String extension) {

		return getNewFilePath(filename,extension, true);
	}

	public Path getNewFilePath(String filename, String extension, boolean shortenFileName) {

		if (StringUtils.isBlank(filename)) {
			filename = UUID.randomUUID().toString();
		}
		if (shortenFileName) {
			filename = FileUtils.shortenFileName(filename);
		}
		filename = FileUtils.removeIllegalFilesystemCharacters(filename);

		Path newPath;
		if (StringUtils.isNotBlank(extension)) {
			newPath = folder.resolve(filename + "." + extension);
		}
		else {
			newPath = folder.resolve(filename);
		}
		String now;
		while (Files.exists(newPath)) {
			now = Objects.toString(System.currentTimeMillis());
			newPath = folder.resolve(now);
			try {
				Files.createDirectories(newPath);
			} catch (IOException e) {
				log.error("Could not create directory: {}", newPath);
				return null;
			}
			if (StringUtils.isNotBlank(extension)) {
				newPath = newPath.resolve(filename + "." + extension);
			}
			else {
				newPath = newPath.resolve(filename);
			}
		}
		return newPath;
	}

	public Path getNewFolderPath(String folderName) {

		if (StringUtils.isBlank(folderName)) {
			folderName = UUID.randomUUID().toString();
		}
		folderName = FileUtils.shortenFileName(folderName);
		folderName = FileUtils.removeIllegalFilesystemCharacters(folderName);

		Path newPath = folder.resolve(folderName);
		while (Files.exists(newPath))
		{
			newPath = folder.resolve(folderName + "_" + Objects.toString(System.currentTimeMillis()));
		}

		try {
			Files.createDirectories(newPath);
		} catch (IOException e) {
			log.error("Could not create directory: {}", newPath);
			return null;
		}

		return newPath;
	}

	public Path getNewFolderPath() {
		return getNewFolderPath(null);
	}

	private void writeObject(ObjectOutputStream oos)
			throws IOException {
		// default serialization
//		oos.defaultWriteObject();
		// write the object
		List<Serializable> ser = new ArrayList<>();
		ser.add(this.folder.toString());
		ser.add(this.parent);
		ser.add(this.errorFolder.toString());
		oos.writeObject(ser);
	}

	private void readObject(ObjectInputStream ois)
			throws ClassNotFoundException, IOException {
		// default deserialization
		//	ois.defaultReadObject();
		List ser = (List) ois.readObject();
		this.parent = (WorkFolder) ser.get(1);
		this.folder = Paths.get((String) ser.get(0));
		this.errorFolder = Paths.get((String) ser.get(2));
	}
}
