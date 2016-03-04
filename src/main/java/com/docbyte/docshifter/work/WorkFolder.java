package com.docbyte.docshifter.work;

import com.docbyte.docshifter.util.FileUtils;

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


	public WorkFolder(Path workfolder, WorkFolder parent) {
		this.parent = parent;
		this.folder = workfolder;
	}

	public WorkFolder(Path workfolder) {
		this(workfolder, null);
	}

	public boolean isRoot() {
		if (parent == null) {
			return true;
		}
		return false;
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


	public Path getFilePath(String filename) {
		FileUtils.removeIllegalFilesystemCharacters(filename);
		return folder.resolve(filename);
	}

	public Path getFilePath(String filename, String extention) {
		return this.getFilePath(filename + "." + extention);
	}

	public Path getNewFilePath(String filename, String extension) {

		//create path filename+extention
		//check if exists

		filename = FileUtils.shortenFileName(filename);

		Path newPath = Paths.get(folder.toString(), filename + "." + extension);
		if (Files.exists(newPath))
		{
			newPath = Paths.get(folder.toString(), filename + "_" + Objects.toString(System.currentTimeMillis()) + "." + extension);
		}

		//THIS IS TEMPORARY SINCE OVERRIDING STILL HAPPENS IN RELEASE
		newPath = Paths.get(folder.toString(), filename + "_" + Objects.toString(System.currentTimeMillis()) + "." + extension);
		return newPath;
	}


	private void writeObject(ObjectOutputStream oos)
			throws IOException {
		// default serialization
//		oos.defaultWriteObject();
		// write the object
		List ser = new ArrayList();
		ser.add(this.folder.toString());
		ser.add(this.parent);
		oos.writeObject(ser);
	}

	private void readObject(ObjectInputStream ois)
			throws ClassNotFoundException, IOException {
		// default deserialization
		//	ois.defaultReadObject();
		List ser = (List) ois.readObject();
		this.parent = (WorkFolder) ser.get(1);
		this.folder = Paths.get((String) ser.get(0));

	}
}
