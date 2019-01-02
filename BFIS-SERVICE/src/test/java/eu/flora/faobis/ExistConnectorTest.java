//package eu.flora.faobis;
//
//import java.io.InputStream;
//import java.util.List;
//
//import javax.ws.rs.core.Response;
//import javax.xml.bind.JAXBContext;
//
//import org.junit.Before;
//import org.junit.Test;
//
//import eu.flora.faobis.servlet.FAOBISService;
//import eu.flora.faobis.servlet.ServletListener;
//import eu.flora.faobis.servlet.XmlRequest;
//
//public class ExistConnectorTest {
//
//    private FAOBISService service;
//
//    @Before
//    public void init() {
//	ServletListener listener = new ServletListener();
//	listener.contextInitialized(null);
//	this.service = new FAOBISService();
//    }
//
//    @Test
//    public void test() throws Exception {
//	InputStream stream = ExistConnectorTest.class.getClassLoader().getResourceAsStream("land_cover_2015.xml");
//	XmlRequest xmlRequest = new XmlRequest(stream);
//	JAXBContext context = JAXBContext.newInstance(XmlRequest.class);
//	context.createMarshaller().marshal(xmlRequest, System.out);
//	service.putLegend("land_cover_2015", xmlRequest,null);
//
//	Response legends = service.getLegends();
//	FAOBISResponse entity = getResponse(legends.getEntity());
//	System.out.println(entity.getMessage());
//	List<String> results = entity.getResults();
//	for (String result : results) {
//	    System.out.println(result);
//	}
//
//    }
//
//    private FAOBISResponse getResponse(Object entity) {
//	if (entity instanceof FAOBISResponse) {
//	    FAOBISResponse ret = (FAOBISResponse) entity;
//	    return ret;
//	}
//	return null;
//    }
//
//}
