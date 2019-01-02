/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.flora.faobis;

import eu.flora.faobis.servlet.ServletListener;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 *
 * @author root
 */
public class PropertyReader {

    /**
     * @return the webserverurl
     */
    public String getWebserverurl() {
        return webserverurl;
    }

    /**
     * @return the geoserverurl
     */
    public String getGeoserverurl() {
        return geoserverurl;
    }

    /**
     * @return the tomcatip
     */
    public String getTomcatip() {
        return tomcatip;
    }

    /**
     * @return the tomcatport
     */
    public String getTomcatport() {
        return tomcatport;
    }

    /**
     * @return the geoserveruser
     */
    public String getGeoserveruser() {
        return geoserveruser;
    }

    /**
     * @return the geoserverpassword
     */
    public String getGeoserverpassword() {
        return geoserverpassword;
    }

    /**
     * @return the postgresip
     */
    public String getPostgresip() {
        return postgresip;
    }

    /**
     * @return the postgresport
     */
    public String getPostgresport() {
        return postgresport;
    }

    /**
     * @return the postgresuser
     */
    public String getPostgresuser() {
        return postgresuser;
    }

    /**
     * @return the postgrespassword
     */
    public String getPostgrespassword() {
        return postgrespassword;
    }

    /**
     * @return the postgresmetadatadb
     */
    public String getPostgresmetadatadb() {
        return postgresmetadatadb;
    }

    /**
     * @return the postgresdatadb
     */
    public String getPostgresdatadb() {
        return postgresdatadb;
    }

    /**
     * @return the existdburl
     */
    public String getExistdburl() {
        return existdburl;
    }

    /**
     * @return the existip
     */
    public String getExistip() {
        return existip;
    }

    /**
     * @return the existport
     */
    public String getExistport() {
        return existport;
    }

    /**
     * @return the existuser
     */
    public String getExistuser() {
        return existuser;
    }

    /**
     * @return the existpassword
     */
    public String getExistpassword() {
        return existpassword;
    }

    /**
     * @return the classificationtable
     */
    public String getClassificationtable() {
        return classificationtable;
    }

    
    private String webserverurl = "http://localhost";
    private String geoserverurl = "http://localhost:8080/geoserver";
    private String tomcatip = "127.0.0.1";
    private String tomcatport = "8080";
    private String geoserveruser = "admin";
    private String geoserverpassword = "geoserver";

    private String postgresip = "127.0.0.1";
    private String postgresport = "5432";
    private String postgresuser = "geodash_dev";
    private String postgrespassword = "";
    private String postgresmetadatadb = "geodash_dev_backup";
    private String postgresdatadb = "geodash_dev_backup-imports";

    private String existdburl = "http://localhost:8080/exist";
    private String existip = "127.0.0.1";
    private String existport = "8081";
    private String existuser = "admin";
    private String existpassword = "Ge0Dash123";
    private String classificationtable = "classification_CqhvqTL6";
    
    private static PropertyReader instance;
    
    public static PropertyReader getInstance() {
        if (instance == null) {
            instance = new PropertyReader();
        }
        return instance;
    }
    
    public void loadProperties(InputStream input) {
        try {

	    input = ServletListener.class.getClassLoader().getResourceAsStream("config.properties");
            Properties prop = new Properties();
	    prop.load(input);
//	    hostname = prop.getProperty("hostname");
            webserverurl = prop.getProperty("webserverurl");
            geoserverurl = prop.getProperty("geoserverurl");
            tomcatip = prop.getProperty("tomcatip");
            tomcatport = prop.getProperty("tomcatport");
            geoserveruser = prop.getProperty("geoserveruser");
            geoserverpassword = prop.getProperty("geoserverpassword");

            postgresip = prop.getProperty("postgresip");
	    postgresport = prop.getProperty("postgresport");
	    postgresuser = prop.getProperty("postgresuser");
	    postgrespassword = prop.getProperty("postgrespassword");
	    postgresmetadatadb = prop.getProperty("postgresmetadatadb");
	    postgresdatadb = prop.getProperty("postgresdatadb");
            classificationtable = prop.getProperty("classificationtable");

            existip = prop.getProperty("existip");
	    existport = prop.getProperty("existport");
	    existuser = prop.getProperty("existuser");
            existpassword = prop.getProperty("existpassword");
        } catch (IOException ex) {
	    ex.printStackTrace();
	} finally {
	    if (input != null) {
		try {
		    input.close();
		} catch (IOException e) {
		    e.printStackTrace();
		}
	    }

	}

    }
}
