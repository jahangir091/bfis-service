package eu.flora.faobis;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.exist.xmldb.EXistResource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xmldb.api.DatabaseManager;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.Database;
import org.xmldb.api.base.Resource;
import org.xmldb.api.base.ResourceIterator;
import org.xmldb.api.base.ResourceSet;
import org.xmldb.api.base.XMLDBException;
import org.xmldb.api.modules.CollectionManagementService;
import org.xmldb.api.modules.XMLResource;
import org.xmldb.api.modules.XPathQueryService;

import eu.flora.essi.lib.xml.XMLDocumentReader;
import eu.flora.essi.lib.xml.XMLDocumentReader.NodeResult;
import eu.flora.faobis.xml.XMLDocument;
import eu.flora.faobis.PropertyReader;

public class ExistConnector {
    private static PropertyReader propertyReader = PropertyReader.getInstance();
    public static String URI = propertyReader.getURI();
    public static String user = propertyReader.getUser();
    public static String password = propertyReader.getPassword();
    public static String LCML_DIRECTORY = propertyReader.getLCML_DIRECTORY();
    
    private static ExistConnector instance = null;

    public static ExistConnector getInstance() {
	if (instance == null) {
	    instance = new ExistConnector();
	}
	return instance;
    }

    private Collection collection;
    private XPathQueryService xpathService;

    private ExistConnector() {
	try {
	    // initialize database driver
	    Class cl = Class.forName("org.exist.xmldb.DatabaseImpl");
	    Database database = (Database) cl.newInstance();
	    database.setProperty("create-database", "true");
	    DatabaseManager.registerDatabase(database);
	    this.collection = getOrCreateLCMLCollection();
	    this.xpathService = (XPathQueryService) collection.getService("XPathQueryService", "1.0");
	    xpathService.setProperty("indent", "yes");
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    private static Collection getOrCreateLCMLCollection() throws XMLDBException {
	return getOrCreateCollection(LCML_DIRECTORY);
    }

    private static Collection getOrCreateCollection(String collectionUri) throws XMLDBException {
	return getOrCreateCollection(collectionUri, 0);
    }

    private static Collection getOrCreateCollection(String collectionUri, int pathSegmentOffset) throws XMLDBException {

	Collection col = DatabaseManager.getCollection(URI + collectionUri, user, password);
	if (col == null) {
	    if (collectionUri.startsWith("/")) {
		collectionUri = collectionUri.substring(1);
	    }
	    String pathSegments[] = collectionUri.split("/");
	    if (pathSegments.length > 0) {
		StringBuilder path = new StringBuilder();
		for (int i = 0; i <= pathSegmentOffset; i++) {
		    path.append("/" + pathSegments[i]);
		}
		Collection start = DatabaseManager.getCollection(URI + path, user, password);
		if (start == null) {
		    // collection does not exist, so create
		    String parentPath = path.substring(0, path.lastIndexOf("/"));
		    Collection parent = DatabaseManager.getCollection(URI + parentPath, user, password);
		    CollectionManagementService mgt = (CollectionManagementService) parent.getService("CollectionManagementService", "1.0");
		    col = mgt.createCollection(pathSegments[pathSegmentOffset]);
		    col.close();
		    parent.close();
		} else {
		    start.close();
		}
	    }
	    return getOrCreateCollection(collectionUri, ++pathSegmentOffset);
	} else {
	    return col;
	}
    }

    public Resource getClassResource(String id) {
	try {

	    ResourceSet result = xpathService.query("/*:LC_LandCoverClass[map_code='" + id + "']");
	    ResourceIterator i = result.getIterator();
	    Resource res = null;
	    while (i.hasMoreResources()) {
		try {
		    res = i.nextResource();
		    return res;
		} finally {
		}
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}

	return null;
    }

    public Resource getLegendResource(String id) {
	try {

	    ResourceSet result = xpathService.query("/*:LC_Legend[@id='" + id + "']");
	    ResourceIterator i = result.getIterator();
	    Resource res = null;
	    while (i.hasMoreResources()) {
		try {
		    res = i.nextResource();
		    return res;
		} finally {
		}
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}

	return null;
    }

    public Set<String> getLegend(String id) throws Exception {
	Set<String> ret = new HashSet<String>();
	try {

	    ResourceSet result = xpathService.query("//*:LC_Legend[@id='" + id + "']");
	    ResourceIterator i = result.getIterator();
	    Resource res = null;
	    while (i.hasMoreResources()) {
		try {
		    res = i.nextResource();
		    String xml = res.getContent().toString();
		    ret.add(xml);
		} finally {
		    try {
			((EXistResource) res).freeResources();
		    } catch (XMLDBException xe) {
			xe.printStackTrace();
		    }
		}
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}

	if (ret.isEmpty()) {
	    throw new Exception("No legend found with id: " + id);
	}

	return ret;
    }

    public Set<String> getClass(String code) throws Exception {
	Set<String> ret = new HashSet<String>();
	try {

	    ResourceSet result = xpathService.query("/*:LC_LandCoverClass[map_code='" + code + "']");
	    ResourceIterator i = result.getIterator();
	    Resource res = null;
	    while (i.hasMoreResources()) {
		try {
		    res = i.nextResource();
		    String xml = res.getContent().toString();
		    ret.add(xml);
		} finally {
		    try {
			((EXistResource) res).freeResources();
		    } catch (XMLDBException xe) {
			xe.printStackTrace();
		    }
		}
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}
	if (ret.isEmpty()) {
	    throw new Exception("Class not found: " + code);
	}

	return ret;
    }

    public Set<String> getClasses() {
	Set<String> ret = new HashSet<String>();
	try {

	    ResourceSet result = xpathService.query("/*:LC_LandCoverClass/map_code/string()");
	    ResourceIterator i = result.getIterator();
	    Resource res = null;
	    while (i.hasMoreResources()) {
		try {
		    res = i.nextResource();
		    String id = res.getContent().toString();
		    ret.add(id);
		} catch (Exception e) {
		    e.printStackTrace();
		} finally {
		    try {
			((EXistResource) res).freeResources();
		    } catch (XMLDBException xe) {
			xe.printStackTrace();
		    }
		}
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}

	return ret;
    }

    public Set<String> getLegends() {
	Set<String> ret = new HashSet<String>();
	try {

	    ResourceSet result = xpathService.query("/*:LC_Legend/@id/string()");
	    ResourceIterator i = result.getIterator();
	    Resource res = null;
	    while (i.hasMoreResources()) {
		try {
		    res = i.nextResource();
		    String name = res.getContent().toString();
		    ret.add(name);
		} catch (Exception e) {
		    e.printStackTrace();
		} finally {
		    try {
			((EXistResource) res).freeResources();
		    } catch (XMLDBException xe) {
			xe.printStackTrace();
		    }
		}
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}

	return ret;

    }

    public void deleteClass(String classId) throws XMLDBException {
	Resource res = getClassResource(classId);
	if (res == null) {
	    throw new RuntimeException("Class not found");
	} else {
	    collection.removeResource(res);
	}
    }

    public void deleteLegend(String legendId) throws XMLDBException {
	Resource res = getLegendResource(legendId);
	if (res == null) {
	    throw new RuntimeException("Legend not found");
	} else {
	    collection.removeResource(res);
	}
    }

    public void putClass(String id, Object object) throws Exception {
	XMLResource res = null;
	try {

	    if (object instanceof Element) {
		Element element = (Element) object;
		XMLDocument doc = new XMLDocument(element.getOwnerDocument());
		String foundId = doc.evaluateString("//LC_LandCoverClass/map_code/string()");
		if (!id.equals(foundId)) {
		    throw new Exception("Given id mismatches with id found inside class: " + id + " " + foundId);
		}
	    } else {
		throw new Exception("Was not able to check id inside the class");
	    }

	    // create new XMLResource; an id will be assigned to the new resource
	    res = (XMLResource) collection.createResource(id, "XMLResource");

	    if (object instanceof File) {
		File file = (File) object;
		if (!file.canRead()) {
		    System.out.println("cannot read file " + file.getAbsolutePath());
		    return;
		}
		res.setContent(file);
	    } else if (object instanceof Element) {
		Element element = (Element) object;
		Document doc = element.getOwnerDocument();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		printDocument(doc, baos);
		String str = new String(baos.toByteArray());
		res.setContent(str);
	    } else {
		res.setContent(object.toString());
	    }

	    System.out.print("storing document " + res.getId() + "...");
	    collection.storeResource(res);
	    System.out.println("ok.");
	} catch (Exception e) {
	    if (res != null) {
		try {
		    ((EXistResource) res).freeResources();
		} catch (XMLDBException xe) {
		    xe.printStackTrace();
		}
	    }
	    throw e;
	} finally {
	    // dont forget to cleanup
	    if (res != null) {
		try {
		    ((EXistResource) res).freeResources();
		} catch (XMLDBException xe) {
		    xe.printStackTrace();
		}
	    }

	}
    }

    public void putLegend(String id, Object object) throws Exception {
	System.out.println("EXIST CONNECTOR PUT LEGEND " + id);
	XMLResource res = null;
	try {

	    if (object instanceof Element) {
		Element element = (Element) object;
		XMLDocument doc = new XMLDocument(element.getOwnerDocument());
		String foundId = doc.evaluateString("//LC_Legend/@id");
		if (!id.equals(foundId)) {
		    throw new Exception("Given id mismatches with id found inside legend: " + id + " != " + foundId);
		}
	    } else {
		throw new Exception("Not able to check id inside the legend: " + id);
	    }

	    // create new XMLResource; an id will be assigned to the new resource
	    res = (XMLResource) collection.createResource(id, "XMLResource");

	    if (object instanceof File) {
		File file = (File) object;
		if (!file.canRead()) {
		    System.out.println("cannot read file " + file.getAbsolutePath());
		    return;
		}
		res.setContent(file);
	    } else if (object instanceof Element) {
		Element element = (Element) object;
		Document doc = element.getOwnerDocument();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		printDocument(doc, baos);
		String str = new String(baos.toByteArray());
		res.setContent(str);
	    } else {
		res.setContent(object.toString());
	    }

	    System.out.print("storing document " + res.getId() + "...");
	    collection.storeResource(res);
	    System.out.println("ok.");
	} catch (Exception e) {
	    if (res != null) {
		try {
		    ((EXistResource) res).freeResources();
		} catch (XMLDBException xe) {
		    xe.printStackTrace();
		}
	    }
	    throw e;
	} finally {
	    // dont forget to cleanup
	    if (res != null) {
		try {
		    ((EXistResource) res).freeResources();
		} catch (XMLDBException xe) {
		    xe.printStackTrace();
		}
	    }

	}
    }

    public static void printDocument(Document doc, OutputStream out) throws IOException, TransformerException {
	TransformerFactory tf = TransformerFactory.newInstance();
	Transformer transformer = tf.newTransformer();
	transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
	transformer.setOutputProperty(OutputKeys.METHOD, "xml");
	transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
	transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

	transformer.transform(new DOMSource(doc), new StreamResult(new OutputStreamWriter(out, "UTF-8")));
    }

    public void close() {
	if (collection != null) {
	    try {
		collection.close();
	    } catch (XMLDBException xe) {
		xe.printStackTrace();
	    }
	}
    }

    public List<String> getClassesInLegend(String legend) {
	Set<String> ret = new HashSet<String>();
	try {

	    ResourceSet result = xpathService.query("/*:LC_Legend[@id='" + legend + "']/*:elements/*:LC_LandCoverClass/map_code/string()");
	    ResourceIterator i = result.getIterator();
	    Resource res = null;
	    while (i.hasMoreResources()) {
		try {
		    res = i.nextResource();
		    String id = res.getContent().toString();
		    ret.add(id);
		} catch (Exception e) {
		    e.printStackTrace();
		} finally {
		    try {
			((EXistResource) res).freeResources();
		    } catch (XMLDBException xe) {
			xe.printStackTrace();
		    }
		}
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}

	return Arrays.asList(ret.toArray(new String[] {}));
    }

    public Set<String> getBasicObjects(String legend) throws Exception {
	String xml = getLegend(legend).iterator().next();
	XMLDocumentReader reader = new XMLDocumentReader(xml);

	Set<String> ret = new TreeSet<>();
	NodeResult[] nodes = reader.evaluateNodes("//*:LC_LandCoverClass//*:LC_Stratum/*:elements/*[local-name(.)!='LC_Characteristic']");
	for (NodeResult node : nodes) {
	    String str = reader.evaluateString(node, "@*:type");
	    ret.add(str);
	}

	return ret;

    }

    public Integer getBasicObjectCount(String legend, String clazz, String obj) throws Exception {
	String xml = getLegend(legend).iterator().next();
	XMLDocumentReader reader = new XMLDocumentReader(xml);

	String objQuery = "";
	if (obj != null) {
	    objQuery = "[@*:type='" + obj + "']";
	}

	return reader.evaluateNumber("count(//*:LC_LandCoverClass[*:map_code='" + clazz + "']//*:LC_Stratum/*:elements/*" + objQuery
		+ "[local-name(.)!='LC_Characteristic'][@*:type])").intValue();

    }

    public Integer getPropertyCount(String legend, String clazz) throws Exception {
	String xml = getLegend(legend).iterator().next();
	XMLDocumentReader reader = new XMLDocumentReader(xml);
	return reader.evaluateNumber("count(//*:LC_LandCoverClass[*:map_code='" + clazz
		+ "']//*:LC_Stratum/*:elements/*//*[local-name(.)!='LC_Characteristic'][@*:type])").intValue();

    }

    public Integer getCharacteristicCount(String legend, String clazz) throws Exception {
	String xml = getLegend(legend).iterator().next();
	XMLDocumentReader reader = new XMLDocumentReader(xml);
	return reader
		.evaluateNumber("count(//*:LC_LandCoverClass[*:map_code='" + clazz + "']//*[local-name(.)='LC_Characteristic'][@*:type])")
		.intValue();

    }

}
