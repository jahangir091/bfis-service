package eu.flora.essi.lib.xml;

import java.io.IOException;
import java.util.List;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import eu.flora.essi.lib.xml.XMLDocumentReader.NodeResult;

/**
 * Extends {@link XMLDocumentReader} by adding writing functionalities
 * 
 * @author Fabrizio
 */
public class XMLDocumentWriter {

    private XMLDocumentReader xmlDocReader;

    /**
     * Creates a new <code>XMLDocumentWriter</code> from the supplied {@link XMLDocumentReader}
     * 
     * @param xmlDocReader
     * @throws SAXException
     * @throws IOException
     */
    public XMLDocumentWriter(XMLDocumentReader xmlDocReader) throws SAXException, IOException {

	this.xmlDocReader = xmlDocReader;
    }

    /**
     * Removes the nodes resulting from the supplied <code>xPath</code>
     * 
     * @param xPath
     * @return <code>true</code> if the the nodes list resulting from the supplied <code>xPath</code> is not empty,
     *         <code>false</code> otherwise
     * @throws XPathExpressionException
     */
    public boolean remove(String xPath) throws XPathExpressionException {

	List<Node> list = this.xmlDocReader.evaluateOriginalNodesList(xPath);
	if (list.isEmpty()) {
	    return false;
	}
	for (Node node : list) {
	    node.getParentNode().removeChild(node);
	}
	return true;
    }

    /**
     * Renames the nodes resulting from the supplied <code>xPath</code> with the given name
     * 
     * @param xPath
     * @param qualifiedName the new qualified name, such as gco:CharacterString or CharacterString in case of no
     *        namespace is provided
     * @throws XPathExpressionException
     */
    public void rename(String xPath, String qualifiedName) throws XPathExpressionException {

	List<Node> list = this.xmlDocReader.evaluateOriginalNodesList(xPath);
	for (Node node : list) {
	    if (node instanceof Element) {
		Element element = (Element) node;
		if (qualifiedName.contains(":")) {
		    String[] split = qualifiedName.split(":");
		    String prefix = split[0];
		    NamespaceContext namespaceContext = this.xmlDocReader.getNamespaceContext();
		    if (namespaceContext != null) {
			String namespace = namespaceContext.getNamespaceURI(prefix);
			if (namespace != null) {
			    element.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:" + prefix, namespace);
			    this.xmlDocReader.document.renameNode(element, namespace, qualifiedName);
			}
		    }
		} else {
		    this.xmlDocReader.document.renameNode(element, element.getNamespaceURI(), qualifiedName);
		}
	    }
	}
    }

    /**
     * Adds the attributes specified by the given name value pairs to the nodes resulting from the supplied
     * <code>xPath</code>
     * 
     * @param xPath
     * @param nameValuePairs
     * @throws XPathExpressionException
     */
    public void addAttributes(String xPath, String... nameValuePairs) throws XPathExpressionException {
	List<Node> list = this.xmlDocReader.evaluateOriginalNodesList(xPath);
	for (Node node : list) {
	    if (node instanceof Element) {
		Element element = (Element) node;
		for (int i = 0; i < nameValuePairs.length; i = i + 2) {
		    element.setAttribute(nameValuePairs[i], nameValuePairs[i + 1]);
		}
	    }
	}
    }

    /**
     * Sets the specified text to the elements resulting from the supplied <code>xPath</code>
     * 
     * @param xPath
     * @param text
     * @throws XPathExpressionException
     */
    public void setText(String xPath, String text) throws XPathExpressionException {
	List<Node> list = this.xmlDocReader.evaluateOriginalNodesList(xPath);
	for (Node node : list) {
	    if (node instanceof Element) {
		Element element = (Element) node;
		element.setTextContent(text);
	    } else if (node instanceof Attr) {
		Attr attr = (Attr) node;
		attr.setTextContent(text);
	    }
	}
    }

    /**
     * Adds the specified node to the nodes resulting from the supplied
     * <code>xPath</code>
     * 
     * @param xPath
     * @param node
     * @throws XPathExpressionException
     */
    public void addNode(String xPath, NodeResult node) throws XPathExpressionException {
	List<Node> list = this.xmlDocReader.evaluateOriginalNodesList(xPath);
	for (Node originalNode : list) {
	    Node importedNode = originalNode.getOwnerDocument().importNode(node, true);
	    originalNode.appendChild(importedNode);
	}
    }
}
