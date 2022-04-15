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
	private Path folder;
	private WorkFolder parent;
	private Path errorFolder;
	private List<String> errormessageList;

	public WorkFolder() {

	}

	public WorkFolder(Path workfolder, Path errorFolder, WorkFolder parent) {
		this.parent = parent;
		this.errorFolder = errorFolder;
		this.folder = workfolder;
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
		this.errorFolder = errorFolder;
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
		this.folder = folder;
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
			newPath = Paths.get(folder.toString(), filename + "." + extension);
		}
		else {
			newPath = Paths.get(folder.toString(), filename);
		}
		String now;
		while (Files.exists(newPath)) {
			now = Objects.toString(System.currentTimeMillis());
			newPath = Paths.get(folder.toString(), now);
			try {
				Files.createDirectories(newPath);
			} catch (IOException e) {
				log.error("Could not create directory: {}", newPath);
				return null;
			}
			if (StringUtils.isNotBlank(extension)) {
				newPath = Paths.get(newPath.toString(), filename + "." + extension);
			}
			else {
				newPath = Paths.get(newPath.toString(), filename);
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

		Path newPath = Paths.get(folder.toString(), folderName);
		while (Files.exists(newPath))
		{
			newPath = Paths.get(folder.toString(), folderName + "_" + Objects.toString(System.currentTimeMillis()));
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
