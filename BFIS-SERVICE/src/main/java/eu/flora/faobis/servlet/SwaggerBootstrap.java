package eu.flora.faobis.servlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import io.swagger.jaxrs.config.BeanConfig;

public class SwaggerBootstrap extends HttpServlet {

    /**
     * 
     */
    private static final long serialVersionUID = 8816155255713103127L;

    public static String HOST_NAME = null;
    public static String PORT = null;
    public static String BASE_PATH = null;

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
