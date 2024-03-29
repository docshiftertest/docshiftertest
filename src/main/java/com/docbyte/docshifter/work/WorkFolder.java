package com.docbyte.docshifter.work;

import com.docbyte.docshifter.util.FileUtils;
import com.docbyte.utils.Logger;

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

/**
 * Created by michiel.vandriessche@docbyte.com on 6/11/15.
 */
public class WorkFolder implements Serializable {


	private static final long serialVersionUID = 7938321829497848697L;
	private Path folder;
	private WorkFolder parent;
	private Path errorFolder;
	private List<String> errormessageList;

	public WorkFolder(Path workfolder, Path errorFolder, WorkFolder parent) {
		this.parent = parent;
		this.errorFolder = errorFolder;
		this.folder = workfolder;
	}

	public WorkFolder(Path workfolder, Path errorFolder) {
		this(workfolder, errorFolder, null);
	}

	public boolean isRoot() {
		if (parent == null) {
			return true;
		}
		return false;
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

	public List<String> getErrormessageList() {
		return errormessageList;
	}

	public void addErrorMessage(String message){
		if (errormessageList == null){
			this.errormessageList = new ArrayList<>();
		}
		this.errormessageList.add(message);
	}

	public Path getFilePath(String filename) {
		FileUtils.removeIllegalFilesystemCharacters(filename);
		return folder.resolve(filename);
	}

	public Path getFilePath(String filename, String extention) {
		return this.getNewFilePath(filename, extention);
	}

	public Path getNewFilePath(String filename, String extension) {

		//create path filename+extention
		//check if exists

		filename = FileUtils.shortenFileName(filename);
		filename = FileUtils.removeIllegalFilesystemCharacters(filename);

		Path newPath = Paths.get(folder.toString(), filename + "." + extension);
		if (Files.exists(newPath))
		{
			newPath = Paths.get(folder.toString(), filename + "_" + Objects.toString(System.currentTimeMillis()) + "." + extension);
		}

		return newPath;
	}

	public Path getNewFolderPath(String foldername) {

		foldername = FileUtils.removeIllegalFilesystemCharacters(foldername);

		Path newPath = Paths.get(folder.toString(), foldername);
		if (Files.exists(newPath))
		{
			newPath = Paths.get(folder.toString(), foldername + "_" + Objects.toString(System.currentTimeMillis()));
		}

		try {
			Files.createDirectories(newPath);
			return newPath;
		} catch (IOException e) {
			Logger.error("Could not create directory: " + newPath, null);
			return null;
		}

	}


	private void writeObject(ObjectOutputStream oos)
			throws IOException {
		// default serialization
//		oos.defaultWriteObject();
		// write the object
		List ser = new ArrayList();
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
