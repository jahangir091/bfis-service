package eu.flora.faobis.servlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import eu.flora.faobis.PropertyReader;

import io.swagger.jaxrs.config.BeanConfig;

public class SwaggerBootstrap extends HttpServlet {

    /**
     * 
     */
    private static final long serialVersionUID = 8816155255713103127L;
    
    private static PropertyReader propertyReader = PropertyReader.getInstance();

    public static String HOST_NAME = propertyReader.getSWAGGER_BOOTSTRAP_HOST_NAME();
    public static String PORT = propertyReader.getSWAGGER_BOOTSTRAP_PORT();
    public static String BASE_PATH = propertyReader.getSWAGGER_BOOTSTRAP_BASE_PATH();

    @Override
    public void init(ServletConfig config) throws ServletException {
	super.init(config);

	BeanConfig swagger = new BeanConfig();
	swagger.setTitle("BFIS Service API");
	swagger.setVersion("1.0.0");
	swagger.setSchemes(new String[] { "http" });
	swagger.setHost(HOST_NAME + ":" + PORT);
	swagger.setBasePath(BASE_PATH);
	swagger.setResourcePackage("eu.flora.faobis.servlet");
	swagger.setScan(true);
    }

}
