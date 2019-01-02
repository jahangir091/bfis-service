package eu.flora.faobis;

import java.io.InputStream;

import org.w3c.dom.Node;

import eu.flora.essi.lib.xml.XMLDocumentReader;

public class LCMLClass {

    private LCMLElement root;

    public LCMLElement getRoot() {
	return root;
    }

    public void setRoot(LCMLElement root) {
	this.root = root;
    }

    private XMLDocumentReader clazzReader;
    private String mapCode;
    public String getMapCode() {
        return mapCode;
    }

    public void setMapCode(String mapCode) {
        this.mapCode = mapCode;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    private String id;

    public LCMLClass(InputStream clazzStream) {
	try {
	    this.clazzReader = new XMLDocumentReader(clazzStream);
	    org.w3c.dom.Node[] firstElements = clazzReader.evaluateNodes("*/*:elements/*");
	    String xmlName = clazzReader.evaluateString("local-name(*[1])");
	    String name = clazzReader.evaluateString("*[1]/*:name");
	    String type = clazzReader.evaluateString("*[1]/@*:type");
	    String description = clazzReader.evaluateString("*[1]/*:description");
	    this.id = clazzReader.evaluateString("*[1]/@id");
	    this.mapCode = clazzReader.evaluateString("*[1]/*:map_code");
	    root = new LCMLElement(xmlName, name, type, description);
	    attachNodes(root, firstElements);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public LCMLClass(XMLDocumentReader legend, Node node) {
	try {
	    this.clazzReader = legend;
	    org.w3c.dom.Node[] firstElements = clazzReader.evaluateNodes(node, "*:elements/*");
	    String xmlName = clazzReader.evaluateString(node, "local-name(.)");
	    String name = clazzReader.evaluateString(node, "*:name");
	    String type = clazzReader.evaluateString(node, "@*:type");
	    String description = clazzReader.evaluateString(node, "*:description");
	    this.id = clazzReader.evaluateString(node, "@id");
	    this.mapCode = clazzReader.evaluateString(node, "*:map_code");
	    root = new LCMLElement(xmlName, name, type, description);
	    attachNodes(root, firstElements);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    private void attachNodes(LCMLElement father, org.w3c.dom.Node[] nodes) {
	for (org.w3c.dom.Node node : nodes) {
	    try {
		String xmlName = clazzReader.evaluateString(node, "local-name(.)");
		String name = clazzReader.evaluateString(node, "*:name");
		String type = clazzReader.evaluateString(node, "@*:type");
		String description = clazzReader.evaluateString(node, "*:description");
		LCMLElement element = new LCMLElement(xmlName, name, type, description);
		father.getChildren().add(element);
		org.w3c.dom.Node[] childNodes = clazzReader.evaluateNodes(node, "*:elements/*");
		attachNodes(element, childNodes);
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	}
    }

    public boolean fullMatch(LCMLClass clazz, boolean characteristicsOn) {
	return root.fullMatch(clazz.getRoot(), characteristicsOn);
    }

    public void print() {
	System.out.println("Id: " + id);
	System.out.println("Map Code: " + mapCode);
	root.print("");
    }



}
