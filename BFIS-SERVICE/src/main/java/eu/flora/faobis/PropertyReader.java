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
    
    public String getURI() {
        return URI;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public String getLCML_DIRECTORY() {
        return LCML_DIRECTORY;
    }
    
    
    public String getSWAGGER_BOOTSTRAP_PORT() {
        return SWAGGER_BOOTSTRAP_PORT;
    }

    public String getSWAGGER_BOOTSTRAP_BASE_PATH() {
        return SWAGGER_BOOTSTRAP_BASE_PATH;
    }

    public String getSWAGGER_BOOTSTRAP_HOST_NAME() {
        return SWAGGER_BOOTSTRAP_HOST_NAME;
    }
    
    public String getLCCS_CODE_COLUMN() {
        return LCCS_CODE_COLUMN;
    }

    public String getSOURCE_LCCS_CODE_COLUMN() {
        return SOURCE_LCCS_CODE_COLUMN;
    }

    public String getPOSTGIS_AGGREGATION_SUFFIX() {
        return POSTGIS_AGGREGATION_SUFFIX;
    }

    public String getPOSTGIS_IDENTIFIER_COLUMN() {
        return POSTGIS_IDENTIFIER_COLUMN;
    }
    
    public String getWORKSPACE() {
        return WORKSPACE;
    }

    public String getSTORE_NAME() {
        return STORE_NAME;
    }
    
        public String getLCML_CODE_COLUMN() {
        return LCML_CODE_COLUMN;
    }

    public String getSOURCE_LCML_CODE_COLUMN() {
        return SOURCE_LCML_CODE_COLUMN;
    }

    public String getAGGREGATION_SUFFIX() {
        return AGGREGATION_SUFFIX;
    }

    public String getIDENTIFIER_COLUMN() {
        return IDENTIFIER_COLUMN;
    }

    public String getLAYER_COLUMN() {
        return LAYER_COLUMN;
    }

    public String getLCCS_ATTRIBUTE_COLUMN() {
        return LCCS_ATTRIBUTE_COLUMN;
    }

    public String getLCCS_LEGEND_COLUMN() {
        return LCCS_LEGEND_COLUMN;
    }


    public String getGEOMETRY() {
        return GEOMETRY;
    }
    
//    tomcat configuration
    private String tomcatip = "127.0.0.1";
    private String tomcatport = "8080";
    
//    webserver configurations
    private String webserverurl = "http://localhost";
    private String geoserverurl = "http://localhost:8080/geoserver";
    
//    geoserver configuration
    private String geoserveruser = "admin";
    private String geoserverpassword = "geoserver";
    
//    postgres configuration
    private String postgresip = "127.0.0.1";
    private String postgresport = "5432";
    private String postgresuser = "geodash_dev";
    private String postgrespassword = "";
    private String postgresmetadatadb = "geodash_dev_backup";
    private String postgresdatadb = "geodash_dev_backup-imports";
    
//    exist db configuration
    private String existdburl = "http://localhost:8080/exist";
    private String existip = "127.0.0.1";
    private String existport = "8081";
    private String existuser = "admin";
    private String existpassword = "Ge0Dash123";
    private String classificationtable = "classification_CqhvqTL6";
    
//   exist connector
    private String URI = "xmldb:exist://faobis.essi-lab.eu:8899/exist/xmlrpc";
    private String user = "admin";
    private String password = "Bang17";
    private String LCML_DIRECTORY = "/db/lcml";
    
//    geonode connector
    private String LCML_CODE_COLUMN = "LCCS_code";
    private String SOURCE_LCML_CODE_COLUMN = "SOURCE_LCCS_code";
    private String AGGREGATION_SUFFIX = "aggregation";
    private String IDENTIFIER_COLUMN = "fid";
    private String LAYER_COLUMN = "layer";
    private String LCCS_ATTRIBUTE_COLUMN = "lccs_attribute";
    private String LCCS_LEGEND_COLUMN = "lccs_legend";
    private String GEOMETRY = "the_geom";
    
//    swagger bootstrap
    private String SWAGGER_BOOTSTRAP_PORT = null;
    private String SWAGGER_BOOTSTRAP_BASE_PATH = null;
    private String SWAGGER_BOOTSTRAP_HOST_NAME = null;
    
    //    postgis connector
    private String LCCS_CODE_COLUMN = "lccs_code";
    private String SOURCE_LCCS_CODE_COLUMN = "source_lccs_code";
    private String POSTGIS_AGGREGATION_SUFFIX = "aggregation";
    private String POSTGIS_IDENTIFIER_COLUMN = "gid";
    
//    geoserver connector
    private String WORKSPACE = "geonode";
    private String STORE_NAME = "datastore";
    
    
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
            
//             assign config values to exist connector
            URI = prop.getProperty("existconnectoruser");
            user = prop.getProperty("existconnectoruser");
            password = prop.getProperty("existconnectorpassword");
            LCML_DIRECTORY = prop.getProperty("existconnectorlcmldirectory");
            
//            assign config values to geonode connector
            LCML_CODE_COLUMN = prop.getProperty("geonodeconnector_lcmlcodecolumn");
            SOURCE_LCML_CODE_COLUMN = prop.getProperty("geonodeconnector_sourcelcmlcodecolumn");
            AGGREGATION_SUFFIX = prop.getProperty("geonodeconnector_aggregationsuffix");
            IDENTIFIER_COLUMN = prop.getProperty("geonodeconnector_identifiercolumn");
            LAYER_COLUMN = prop.getProperty("geonodeconnector_layercolumn");
            LCCS_ATTRIBUTE_COLUMN = prop.getProperty("geonodeconnector_lccsattributecolumn");
            LCCS_LEGEND_COLUMN = prop.getProperty("geonodeconnector_lccslegendcolumn");
            GEOMETRY = prop.getProperty("geonodeconnector_geometry");
            
//            swagger property reader
            SWAGGER_BOOTSTRAP_HOST_NAME = prop.getProperty("swaggerbootstrap_hostname");
            SWAGGER_BOOTSTRAP_PORT = prop.getProperty("swaggerbootstrap_port");
            SWAGGER_BOOTSTRAP_BASE_PATH = prop.getProperty("swaggerbootstrap_basepath");
            
//            postgis connector
            LCCS_CODE_COLUMN = prop.getProperty("lccscodecolumn");
            SOURCE_LCCS_CODE_COLUMN = prop.getProperty("sourcelccscodecolumn");
            POSTGIS_AGGREGATION_SUFFIX = prop.getProperty("aggregationsuffix");
            POSTGIS_IDENTIFIER_COLUMN = prop.getProperty("identifiedcolumn");
            
//            geoserver connector 
            WORKSPACE = prop.getProperty("geoserverworkspace");
            STORE_NAME = prop.getProperty("geoserverstorename");
            
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
