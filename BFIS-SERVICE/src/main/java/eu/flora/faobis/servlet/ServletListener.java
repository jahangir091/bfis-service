package eu.flora.faobis.servlet;

import eu.flora.faobis.ExistConnector;
import eu.flora.faobis.GeonodeConnector;
import eu.flora.faobis.GeoserverConnector;
import eu.flora.faobis.PropertyReader;
import eu.flora.faobis.PythonConnector;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Properties;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

public class ServletListener implements ServletContextListener {

    private PropertyReader propertyReader = PropertyReader.getInstance();
    public void contextDestroyed(ServletContextEvent arg0) {
	System.out.println("Server shut down");
	// close connection to db
	try {
//	    PostGISConnector.getInstance().close();
	    ExistConnector.getInstance().close();
        } catch (Exception e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

    }

//    private String hostname = "103.48.16.254";  //"192.168.56.102"; // "faobis.essi-lab.eu";
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
    private String classificationtable = "classification_cqhvqtl6";

    @Override
    public void contextInitialized(ServletContextEvent arg0) {

//	Properties prop = new Properties();
	InputStream input = null;

//	try {

	    input = ServletListener.class.getClassLoader().getResourceAsStream("config.properties");
            PropertyReader.getInstance().loadProperties(input);
//	    prop.load(input);
//	    hostname = prop.getProperty("hostname");
            webserverurl = propertyReader.getWebserverurl();
            geoserverurl = propertyReader.getGeoserverurl();
            tomcatip = propertyReader.getTomcatip();
            tomcatport = propertyReader.getTomcatport();
            geoserveruser = propertyReader.getGeoserveruser();
            geoserverpassword = propertyReader.getGeoserverpassword();

            postgresip = propertyReader.getPostgresip();
	    postgresport = propertyReader.getPostgresport();
	    postgresuser = propertyReader.getPostgresuser();
	    postgrespassword = propertyReader.getPostgrespassword();
	    postgresmetadatadb = propertyReader.getPostgresmetadatadb();
	    postgresdatadb = propertyReader.getPostgresdatadb();
            classificationtable = propertyReader.getClassificationtable();

            existip = propertyReader.getExistip();
	    existport = propertyReader.getExistport();
	    existuser = propertyReader.getExistuser();
            existpassword = propertyReader.getExistpassword();


//        } catch (IOException ex) {
//	    ex.printStackTrace();
//	} finally {
//	    if (input != null) {
//		try {
//		    input.close();
//		} catch (IOException e) {
//		    e.printStackTrace();
//		}
//	    }
//
//	}

        System.out.println("BFIS service is starting up on hosts = Tomcat: " + propertyReader.getTomcatip() + 
                ", eXist: " + propertyReader.getExistip() + ", DB: " + propertyReader.getPostgresip());
	Thread thread = new Thread() {
	    @Override
	    public void run() {
		init();
	    }
	};
	thread.start();

    }

    private void init() {
//    	Properties prop = new Properties();
//		InputStream input = null;
//        try {
//            input = ServletListener.class.getClassLoader().getResourceAsStream("config.properties");
//            prop.load(input);
//            hostname = prop.getProperty("hostname");
//            System.out.println("loading hostname for eXist = " + hostname);
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        } finally {
//            if (input != null) {
//                try {
//                    input.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//	    	}
//		}
        PythonConnector.getInstance().setHostname("http://" + propertyReader.getExistip());

	if (new File("/home/boldrini").exists()) {
	    // this checks if the service is running on a development machine (boldrini)
	    SwaggerBootstrap.HOST_NAME = "boldrini.essi-lab.eu";
	    SwaggerBootstrap.PORT = "9090";
	    System.out.println("we are on boldrini");
	} else if (new File("/tmp/asclepio").exists()) {
	    // this checks if the service is running on a development machine (asclepio)
	    SwaggerBootstrap.HOST_NAME = "asclepio.essi-lab.eu";
	    SwaggerBootstrap.PORT = "80";
	    System.out.println("we are on asclepio");
	} else {
	    // else we are in production service
	    SwaggerBootstrap.HOST_NAME = propertyReader.getTomcatip();
	    SwaggerBootstrap.PORT = propertyReader.getTomcatport();
	}

	SwaggerBootstrap.BASE_PATH = "/bfis-service/rest";

	// exist configuration
	ExistConnector.URI = "xmldb:exist://" + existip + ":" + existport + "/exist/xmlrpc";
	ExistConnector.user = existuser;
	ExistConnector.password = existpassword;

        String existURL = "http://" + existip + ":" + existport + "/exist";
	while (checkResponseCode(existURL) != 200) {
	    System.out.println("Waiting for eXist to come up: " + existURL);
	    try {
		Thread.sleep(1000);
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	}
	// // geoserver connector configuration
	// GeoserverConnector.getInstance().init("http://" + hostname + ":8080/geoserver", "bisadmin", "Bang17");
	// // postgis connector configuration
	// String url = "jdbc:postgresql://" + hostname + ":5433/bis";
	// String user = "postgres";
	// String password = "Bang17";
	// try {
	// PostGISConnector.getInstance().initConnection(url, user, password);
	// } catch (SQLException e) {
	// e.printStackTrace();
	// }

        String postgresUrl = "jdbc:postgresql://" + postgresip + ":" + postgresport + "/";

	try {
            GeonodeConnector.getInstance().initConnection(postgresUrl, postgresuser, postgrespassword, postgresmetadatadb, postgresdatadb,
                    webserverurl, geoserverurl, classificationtable);
//                    "http://" + hostname);
	    // the following line deletes and recreates the classification table
	    // GeonodeConnector.getInstance().createClassificationTable();
	} catch (SQLException e) {
	    e.printStackTrace();
	}
//        String geoserverURL = "http://" + tomcatip + ":" + tomcatport + "/geoserver";
        GeoserverConnector.getInstance().init(geoserverurl, geoserveruser, geoserverpassword);
	System.out.println("BFIS service started up");
	// GeonodeConnector.getInstance().createClassificationTable();

	// to clean the exist repository
	// ExistCleaner.cleanRepository();

    }

    public int checkResponseCode(String url) {
	System.out.println("Checking response code for: " + url);
	HttpClient client = HttpClientBuilder.create().build();
	HttpGet put = new HttpGet(url);
	HttpResponse response;
	try {
	    response = client.execute(put);
	    int code = response.getStatusLine().getStatusCode();
	    System.out.println("Obtained code: " + code);
	    return code;
	} catch (ClientProtocolException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	}
	return 0;

    }

}
