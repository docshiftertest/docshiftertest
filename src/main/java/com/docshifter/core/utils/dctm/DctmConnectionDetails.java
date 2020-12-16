package com.docshifter.core.utils.dctm;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Setter
@Getter
@AllArgsConstructor
public class DctmConnectionDetails {
	
	private String repository;
	private String username;
	private String password;
	
	public static DctmConnectionDetails fromProperties(String file) throws IOException {
		
		Properties prop = new Properties();
		
		InputStream is = DctmConnectionDetails.class.getClassLoader().getResourceAsStream(file);
		if (is == null) {
			throw new IllegalArgumentException("File not found in classpath");
		}
		
		prop.load(is);
		is.close();
		
		return new DctmConnectionDetails(
				prop.getProperty("repository"),
				prop.getProperty("username"),
				prop.getProperty("password")
		);
		
		
	}
}
