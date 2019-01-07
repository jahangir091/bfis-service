package eu.flora.faobis;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;

import it.geosolutions.geoserver.rest.GeoServerRESTManager;
import it.geosolutions.geoserver.rest.encoder.GSLayerEncoder;
import it.geosolutions.geoserver.rest.encoder.GSResourceEncoder;
import it.geosolutions.geoserver.rest.encoder.feature.GSFeatureTypeEncoder;
import eu.flora.faobis.PropertyReader;

public class GeoserverConnector {

    private static GeoserverConnector instance = null;
    
    private static PropertyReader propertyReader = PropertyReader.getInstance();

    // something like http://faobis.essi-lab.eu:8080/geoserver
    String endpoint = null;
    String username;
    String password;
    private GeoServerRESTManager manager = null;

    public static final String WORKSPACE = propertyReader.getWORKSPACE();
    public static final String STORE_NAME = propertyReader.getSTORE_NAME();
    public static final String STYLE_SUFFIX = "";

    public GeoServerRESTManager getManager() {
	return manager;
    }

    public String getUsername() {
	return username;
    }

    public void init(String endpoint, String username, String password) {
	this.username = username;
	this.password = password;
	this.endpoint = endpoint;

	while (manager == null) {
	    System.out.println("Initializing geoserver connector...");
	    try {
		this.manager = new GeoServerRESTManager(new URL(endpoint), username, password);
		System.out.println("Done");
	    } catch (Exception e) {
		e.printStackTrace();
		System.out.println("Error occurred: " + e.getMessage());
		try {
		    Thread.sleep(1000);
		} catch (InterruptedException e1) {
		    e1.printStackTrace();
		}
	    }	    
	}
    }

    public String getPassword() {
	return password;
    }

    public String getEndpoint() {
	return endpoint;
    }

    public static GeoserverConnector getInstance() {
	if (instance == null) {
	    instance = new GeoserverConnector();
	}
	return instance;
    }

    private GeoserverConnector() {
    }

    public boolean reset() {
	HttpClient client = HttpClientBuilder.create().build();
	UsernamePasswordCredentials creds = new UsernamePasswordCredentials(username, password);
	CredentialsProvider credsProvider = new BasicCredentialsProvider();
	credsProvider.setCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT), creds);
	HttpPut put = new HttpPut(endpoint + "/rest/reset");
	HttpClientContext context = HttpClientContext.create();
	context.setCredentialsProvider(credsProvider);
	HttpResponse response;
	try {
	    response = client.execute(put, context);
	    int code = response.getStatusLine().getStatusCode();
	    System.out.println("Reset request sent to geoserver. Response status code: " + code);
	    if (code == 200) {
		return true;
	    }
	} catch (ClientProtocolException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	}
	return false;

    }

    public void publishLayer(String layer, String column) throws Exception {

	// geoserver layers are always lower case
	String geoserverLayer = layer.toLowerCase();
	GeoserverConnector.getInstance().getManager().getPublisher().unpublishFeatureType(WORKSPACE, STORE_NAME, geoserverLayer);
	String style = geoserverLayer + GeoserverConnector.STYLE_SUFFIX;
	GeoserverConnector.getInstance().getManager().getStyleManager().removeStyle(style);

	String styleDocument = GeonodeConnector.getInstance().createStyle(geoserverLayer, column);
	GeoserverConnector.getInstance().getManager().getStyleManager().publishStyle(styleDocument, style);
	GSFeatureTypeEncoder fte = new GSFeatureTypeEncoder();
	fte.setProjectionPolicy(GSResourceEncoder.ProjectionPolicy.REPROJECT_TO_DECLARED);
	fte.addKeyword(layer);
	fte.setTitle(layer);
	fte.setName(geoserverLayer);
	fte.setSRS("EPSG:4326");
	final GSLayerEncoder layerEncoder = new GSLayerEncoder();
	layerEncoder.setDefaultStyle(style);
	GeoserverConnector.getInstance().getManager().getPublisher().publishDBLayer(WORKSPACE, STORE_NAME, fte, layerEncoder);

    }

}
