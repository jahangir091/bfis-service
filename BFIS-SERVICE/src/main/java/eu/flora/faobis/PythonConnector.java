package eu.flora.faobis;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.Map;

import eu.flora.faobis.servlet.Options;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.HttpClientBuilder;

public class PythonConnector {
    //private String hostname = "http://192.168.56.102"; //149.139.19.8
    public static String hostname = null;

    public String getHostname() {
	return hostname;
    }

    public void setHostname(String hostname) {
	this.hostname = hostname;
    }

    private static PythonConnector instance = null;

    public static PythonConnector getInstance() {
	if (instance == null) {
	    instance = new PythonConnector();
	}
	return instance;
    }

    private PythonConnector() {

    }

    public String getListBasicElementsPath(String name, Boolean eligibleAsRoot) {
	String ret = hostname + "/lcmlutils/services/list-basic-elements";

	String separator = "?";

	if (name != null) {
	    ret = ret + separator + "name=" + name;
	    separator = "&";
	}

	if (eligibleAsRoot != null) {
	    ret = ret + separator + "eligibleAsRoot=" + eligibleAsRoot;
	    separator = "&";
	}

	return ret;
    }

    public String getBasicElementSchemaPath(String name) {
	String ret = hostname + "/lcmlutils/services/basic-element-schema/" + name;

	return ret;
    }

    public String getDerivedClassesListPath(String name) {
	String ret = hostname + "/lcmlutils/services/derived-classes-list/" + name;

	return ret;
    }

    public String postSimilarityQuery(String originalLegendCode, String queryLegendCode, Map<String, Options> queryoptions) throws Exception {
	String endpoint = hostname + "/lcmlutils/services/similarity-assessment";

	HttpClient client = HttpClientBuilder.create().build();
	HttpPost post = new HttpPost(endpoint);
	ByteArrayOutputStream baos = new ByteArrayOutputStream();

	String req = "{\n" + //
		"\"working_legend_name\":\"WLN\",\n" + //
		"\"lcml_class\":\"LCML_CLASS\"\n" + //
		// "\"advanced_options\":\"{}\"\n" + //
		"}";

	req = req.replace("WLN", originalLegendCode);

	String legend = ExistConnector.getInstance().getLegend(queryLegendCode).iterator().next();
	legend = legend.replace("\"", "'").replace("\n", "");

	req = req.replace("LCML_CLASS", legend);

	//req = req.replace("{}", queryoptions);

	InputStream stream = new ByteArrayInputStream(req.getBytes("UTF-8"));
	IOUtils.copy(stream, baos);
	byte[] bytes = baos.toByteArray();
	ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
	int length = bytes.length;
	InputStreamEntity ent = new InputStreamEntity(bais, length);
	ent.setChunked(false);
	post.setEntity(ent);
	HttpClientContext context = HttpClientContext.create();

	try {
	    HttpResponse response = client.execute(post, context);
	    InputStream content = response.getEntity().getContent();
	    String str = IOUtils.toString(content);
	    return str;
	} catch (ClientProtocolException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	}

	return endpoint;
    }

}
