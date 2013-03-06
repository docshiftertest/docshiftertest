package com.docbyte.docshifter.util;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

/**
 * @author Dieter Verlinde - Docbyte
 *
 */
public class HSQLDBShutdown {
	private static String DBPORT="9001";
	private static String DBNAME="docshifter";
	private static String DBUSER="DS_USER";
	private static String DBPASSWORD="DS_USER";

	public static void main(String []args){
		System.out.println("Shutting down database...");
		File cfgFile=new File("./data/server.properties");
		if(cfgFile.canRead()){
			try {
				PropertiesConfiguration cfg=new PropertiesConfiguration(cfgFile);
				if(cfg.containsKey("server.port")){
					DBPORT=cfg.getString("server.port");
				}
				if(cfg.containsKey("server.database.0")){
					DBNAME=cfg.getString("server.database.0");
				}
				if(cfg.containsKey("server.urlid.0")){
					String temp=cfg.getString("server.urlid.0");
					String t[]=temp.split("\\.");
					if(t.length==2)
						DBUSER=t[0];
						DBPASSWORD=t[1];
				}
			} catch (ConfigurationException e) {
				System.out.println("Bad server.properties file, using default settings.");
				e.printStackTrace(System.err);
			}
			
		}else {
			System.out.println("Could not read "+cfgFile.getAbsolutePath()+", using default settings.");
		}
		Connection conn=null;
		try {
			Class.forName("org.hsqldb.jdbcDriver");

			conn = DriverManager.getConnection("jdbc:hsqldb:hsql://localhost:"+ DBPORT+"/"+DBNAME,DBUSER,DBPASSWORD);
			Statement st = conn.createStatement();
			st.execute("SHUTDOWN");
			conn.close();    // if there are no other open connection
			System.out.println("Clean Shutdown completed.");
		} catch (Exception ex) {
			ex.printStackTrace(System.err);
		} finally {
			if(conn!=null)
				try {
					conn.close();
				} catch (Exception e) {
					e.printStackTrace(System.err);
				}
		}
	}
}
