package com.docshifter.core.utils;


import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

public class ClassHelper {

	private static final Logger logger = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

	//adds every jar file inside dir and subdirs of dir to the URL[]
	//@SuppressWarnings("unchecked")
	public static URL[] findAllJars(File dir) throws IllegalArgumentException,IOException {
		//array thingy
		ArrayList<URL> urls=new ArrayList<>();
		URL[] tempURLs;
		if(!dir.isDirectory()){
			throw new IllegalArgumentException(dir.getAbsolutePath()+" is not a valid directory");
		}
		File[] children=dir.listFiles();
		if(children!=null){
			for(int i=0;i<children.length;i++){
				if(children[i].isDirectory()){
					tempURLs=findAllJars(children[i]);
					for(int j=0;j<tempURLs.length;j++)
						urls.add(tempURLs[j]);
				}else if(children[i].isFile()&&isJar(children[i])){
					urls.add(children[i].toURI().toURL());
				}
			}
		}else {throw new IOException();}
			
		//catch(MalformedURLException ex){
		//	logger.error(Logger.DS,"could not find the correct module",null,ex);
		//}
		tempURLs=new URL[0];
		try{
			tempURLs=(URL[])urls.toArray(tempURLs);
		}catch(ArrayStoreException ex){
			tempURLs=null;
			logger.error("Error while loading jar's",ex);
		}
		return tempURLs;
	}
	public static boolean isJar(File file){
		String filePath=file.getAbsolutePath();
		logger.trace("Jarname found = " + filePath.toString(), null);

		int lastIndex = filePath.lastIndexOf('.');
		if (lastIndex < 0) {
			return false;
		} else {
			return ".jar".equalsIgnoreCase(filePath.substring(filePath.lastIndexOf('.')));
		}
	}
}
