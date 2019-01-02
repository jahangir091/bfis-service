package eu.flora.essi.lib.xml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathFactoryConfigurationException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.UserDataHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * A utility class to parse an XML input stream/document and then execute XPaths on it
 * 
 * @author boldrini
 */
public class XMLDocumentReader {
    /**
     * The lock object is needed, as the underlying library (Saxon-HE) is not multithread-safe
     */
    final private static Object LOCK = new Object();

    private static XPathFactory factory;

    static DocumentBuilder builder;

    static {
	try {
	    synchronized (LOCK) {
		// here we chose to use Saxon HE as the library for XPath
		factory = XPathFactory.newInstance(XPathFactory.DEFAULT_OBJECT_MODEL_URI, "net.sf.saxon.xpath.XPathFactoryImpl",
			XPathFactory.class.getClassLoader());
		DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
		documentFactory.setNamespaceAware(true);
		documentFactory.setValidating(false);
		documentFactory.setIgnoringComments(false);
		documentFactory.setExpandEntityReferences(false);
		documentFactory.setIgnoringElementContentWhitespace(false);
		documentFactory.setCoalescing(false);
		builder = documentFactory.newDocumentBuilder();
	    }

	} catch (XPathFactoryConfigurationException e) {
	    // Saxon HE library not found.. this should not happen!
	    e.printStackTrace();
	    System.err.println("Saxon HE libraries not found in the classpath");
	    System.err.println("Application will exit");
	    // not possible to continue
	    System.exit(1);
	} catch (FactoryConfigurationError e) {
	    // Factory instantiation error.. this should not happen!
	    e.printStackTrace();
	    System.err.println("The default XML document builder factory could not be instantiated");
	    System.err.println("Application will exit");
	    // not possible to continue
	    System.exit(1);
	} catch (ParserConfigurationException e) {
	    // Document builder factory instantiation error.. this should not happen!
	    e.printStackTrace();
	    System.err.println("The default XML document builder could not be instantiated");
	    System.err.println("Application will exit");
	    // not possible to continue
	    System.exit(1);
	}

    }

    private XPath xpath;
    Document document;

    /**
     * Constructs a new <code>XMLDocumentReader</code> from a {@link InputStream}
     * 
     * @param stream {@link InputStream} containing the content to be parsed
     * @throws IOException If any IO errors occur
     * @throws SAXException If any parse errors occur
     * @throws IllegalArgumentException When <code>stream</code> is <code>null</code>
     */
    public XMLDocumentReader(InputStream stream) throws SAXException, IOException {
	if (stream == null) {
	    throw new IllegalArgumentException("XMLDocument: the stream containing the content to be parsed is null");
	}
	synchronized (LOCK) {
	    InputStreamReader reader = new InputStreamReader(stream, "UTF-8");
	    InputSource source = new InputSource(reader);
	    document = builder.parse(source);
	    xpath = factory.newXPath();
	}
    }

    /**
     * @param document
     * @throws SAXException
     * @throws IOException
     */
    public XMLDocumentReader(String document) throws SAXException, IOException {
	this(new ByteArrayInputStream(document.getBytes("UTF-8")));
    }

    /**
     * Creates a new <code>XMLDocumentReader</code> from the supplied <code>document</code>
     * 
     * @param document a non <code>null</code> W3C document
     * @throws SAXException
     * @throws IOException
     */
    public XMLDocumentReader(Document document) throws SAXException, IOException {

	this.document = document;
	xpath = factory.newXPath();
    }

    /**
     * Gets the namespace context used evaluating XPaths
     */
    public NamespaceContext getNamespaceContext() {
	synchronized (LOCK) {
	    return xpath.getNamespaceContext();
	}
    }

    /**
     * Sets the namespace context to be used evaluating XPaths
     * 
     * @param context
     */
    public void setNamespaceContext(NamespaceContext context) {
	synchronized (LOCK) {
	    xpath.setNamespaceContext(context);
	}
    }

    /**
     * Sets the namespaces context to be used evaluating XPaths
     * 
     * @param namespaces
     */
    public void setNamespaces(Map<String, String> namespaces) {

	setNamespaceContext(new NamespaceContextImpl(namespaces));
    }

    private enum ResultType {

	NUMBER(XPathConstants.NUMBER), //
	STRING(XPathConstants.STRING), //
	BOOLEAN(XPathConstants.BOOLEAN), //
	NODE(XPathConstants.NODE), //
	NODESET(XPathConstants.NODESET);
	private QName resultType;

	public QName getResultType() {
	    return resultType;
	}

	ResultType(QName resultType) {
	    this.resultType = resultType;
	}
    }

    /**
     * Evaluates the given XPath expression on the document to get a {@link Number} result.
     * 
     * @param xpathExpression
     * @return a {@link Number} result
     * @throws XPathExpressionException If expression cannot be evaluated.
     */
    public Number evaluateNumber(String xpathExpression) throws XPathExpressionException {
	return evaluateNumber(null, xpathExpression);
    }

    /**
     * Evaluates the given XPath expression on the given target to get a {@link Number} result.
     * 
     * @param target a {@link Node} that is the result of a previous evaluateNode operation. If null, evaluation
     *        will be performed on the entire document.
     * @param xpathExpression
     * @return a {@link Number} result
     * @throws XPathExpressionException If expression cannot be evaluated.
     */
    public Number evaluateNumber(Node target, String xpathExpression) throws XPathExpressionException {
	return (Number) this.evaluate(xpathExpression, target, ResultType.NUMBER);
    }

    /**
     * Evaluates the given XPath expression on the document to get a {@link Boolean} result.
     * 
     * @param xpathExpression
     * @return a {@link Boolean} result
     * @throws XPathExpressionException If expression cannot be evaluated.
     */
    public Boolean evaluateBoolean(String xpathExpression) throws XPathExpressionException {
	return evaluateBoolean(null, xpathExpression);
    }

    /**
     * Evaluates the given XPath expression on the given target to get a {@link Boolean} result.
     * 
     * @param target a {@link Node} that is the result of a previous evaluateNode operation. If null, evaluation
     *        will be performed on the entire document.
     * @param xpathExpression
     * @return a {@link Boolean} result
     * @throws XPathExpressionException If expression cannot be evaluated.
     */
    public Boolean evaluateBoolean(Node target, String xpathExpression) throws XPathExpressionException {
	return (Boolean) this.evaluate(xpathExpression, target, ResultType.BOOLEAN);
    }

    /**
     * Evaluates the given XPath expression on the document to get a {@link String} result.
     * 
     * @param xpathExpression
     * @return a String result
     * @throws XPathExpressionException If expression cannot be evaluated.
     */
    public String evaluateString(String xpathExpression) throws XPathExpressionException {
	return evaluateString(null, xpathExpression);
    }

    /**
     * Evaluates the given XPath expression on the given target to get a {@link String} result.
     * 
     * @param target a {@link Node} that is the result of a previous evaluateNode operation. If null, evaluation
     *        will be performed on the entire document.
     * @param xpathExpression
     * @return a String result
     * @throws XPathExpressionException If expression cannot be evaluated.
     */
    public String evaluateString(Node target, String xpathExpression) throws XPathExpressionException {
	return (String) this.evaluate(xpathExpression, target, ResultType.STRING);
    }

    /**
     * Evaluates the given XPath expression on the document to get a {@link Node} result.
     * 
     * @param xpathExpression
     * @return a {@link NodeResult} result or null if the expression evaluated to null
     * @throws XPathExpressionException If expression cannot be evaluated.
     */
    public NodeResult evaluateNode(String xpathExpression) throws XPathExpressionException {
	return evaluateNode(null, xpathExpression);
    }

    /**
     * Evaluates the given XPath expression on the given target to get a {@link Node} result.
     * 
     * @param target a {@link Node} that is the result of a previous evaluateNode operation. If null, evaluation
     *        will be performed on the entire document.
     * @param xpathExpression
     * @return a {@link Node} result or null if the expression evaluated to null
     * @throws XPathExpressionException If expression cannot be evaluated.
     */
    public NodeResult evaluateNode(Node target, String xpathExpression) throws XPathExpressionException {
	Object ret = this.evaluate(xpathExpression, target, ResultType.NODE);
	if (ret == null) {
	    return null;
	}
	return new NodeResult((Node) ret);
    }

    /**
     * Evaluates the given XPath expression on the document to get an array {@link NodeResult} result.
     * 
     * @param xpathExpression
     * @return an array of {@link NodeResult} result
     * @throws XPathExpressionException If expression cannot be evaluated.
     */
    public NodeResult[] evaluateNodes(String xpathExpression) throws XPathExpressionException {
	return evaluateNodes(null, xpathExpression);
    }

    /**
     * Evaluates the given XPath expression on the given target to get an array {@link Node} result.
     * 
     * @param target a {@link Node} that is the result of a previous evaluateNode operation. If null, evaluation
     *        will be performed on the entire document.
     * @param xpathExpression
     * @return an array of {@link Node} result
     * @throws XPathExpressionException If expression cannot be evaluated.
     */
    public NodeResult[] evaluateNodes(Node target, String xpathExpression) throws XPathExpressionException {
	Object ret = this.evaluate(xpathExpression, target, ResultType.NODESET);
	if (ret instanceof NodeList) {
	    NodeList nodes = (NodeList) ret;
	    NodeResult[] result = new NodeResult[nodes.getLength()];
	    for (int i = 0; i < nodes.getLength(); i++) {
		Node item = nodes.item(i);
		if (item == null) {
		    result[i] = null;
		} else {
		    result[i] = new NodeResult(item);
		}
	    }
	    return result;

	}
	// it should never happen!
	return null;
    }

    /**
     * @param xpathExpression
     * @return
     * @throws XPathExpressionException
     */
    public List<Node> evaluateOriginalNodesList(String xpathExpression) throws XPathExpressionException {

	ArrayList<Node> out = new ArrayList<>();

	Object ret = this.evaluate(xpathExpression, null, ResultType.NODESET);
	if (ret instanceof NodeList) {
	    NodeList nodes = (NodeList) ret;

	    for (int i = 0; i < nodes.getLength(); i++) {
		Node item = nodes.item(i);
		out.add(item);
	    }
	}
	return out;
    }

    private Object evaluate(String xpathExpression, Node target, ResultType resultType) throws XPathExpressionException {
	Object item = document;
	if (target != null) {
	    item = target;
	}
	Object result;
	synchronized (LOCK) {
	    result = xpath.evaluate(xpathExpression, item, resultType.getResultType());
	}
	return result;
    }

    /**
     * A wrapper to a {@link Node} that is result of the evaluateNode method of {@link XMLDocumentReader}. This wrapper
     * class
     * should be used only in this
     * context, the
     * private constructor ensures that.
     * 
     * @author boldrini
     */
    public class NodeResult implements Node {
	private Node node;

	/**
	 * Private constructor used only inside the {@link XMLDocumentReader} class
	 * 
	 * @param node
	 */
	private NodeResult(Node node) {
	    this.node = node;
	}

	@Override
	public String getNodeName() {
	    return node.getNodeName();
	}

	@Override
	public String getNodeValue() throws DOMException {
	    return node.getNodeValue();
	}

	@Override
	public void setNodeValue(String nodeValue) throws DOMException {
	    node.setNodeValue(nodeValue);
	}

	@Override
	public short getNodeType() {
	    return node.getNodeType();
	}

	@Override
	public Node getParentNode() {
	    return node.getParentNode();
	}

	@Override
	public NodeList getChildNodes() {
	    return node.getChildNodes();
	}

	@Override
	public Node getFirstChild() {
	    return node.getFirstChild();
	}

	@Override
	public Node getLastChild() {
	    return node.getLastChild();
	}

	@Override
	public Node getPreviousSibling() {
	    return node.getPreviousSibling();
	}

	@Override
	public Node getNextSibling() {
	    return node.getNextSibling();
	}

	@Override
	public NamedNodeMap getAttributes() {
	    return node.getAttributes();
	}

	@Override
	public Document getOwnerDocument() {
	    return node.getOwnerDocument();
	}

	@Override
	public Node insertBefore(Node newChild, Node refChild) throws DOMException {
	    return node.insertBefore(newChild, refChild);
	}

	@Override
	public Node replaceChild(Node newChild, Node oldChild) throws DOMException {
	    return node.replaceChild(newChild, oldChild);
	}

	@Override
	public Node removeChild(Node oldChild) throws DOMException {
	    return node.removeChild(oldChild);
	}

	@Override
	public Node appendChild(Node newChild) throws DOMException {
	    return node.appendChild(newChild);
	}

	@Override
	public boolean hasChildNodes() {
	    return node.hasChildNodes();
	}

	@Override
	public Node cloneNode(boolean deep) {
	    return node.cloneNode(deep);
	}

	@Override
	public void normalize() {
	    node.normalize();

	}

	@Override
	public boolean isSupported(String feature, String version) {
	    return node.isSupported(feature, version);
	}

	@Override
	public String getNamespaceURI() {
	    return node.getNamespaceURI();
	}

	@Override
	public String getPrefix() {
	    return node.getPrefix();
	}

	@Override
	public void setPrefix(String prefix) throws DOMException {
	    node.setPrefix(prefix);
	}

	@Override
	public String getLocalName() {
	    return node.getLocalName();
	}

	@Override
	public boolean hasAttributes() {
	    return node.hasAttributes();
	}

	@Override
	public String getBaseURI() {
	    return node.getBaseURI();
	}

	@Override
	public short compareDocumentPosition(Node other) throws DOMException {
	    return node.compareDocumentPosition(other);
	}

	@Override
	public String getTextContent() throws DOMException {
	    return node.getTextContent();
	}

	@Override
	public void setTextContent(String textContent) throws DOMException {
	    node.setTextContent(textContent);
	}

	@Override
	public boolean isSameNode(Node other) {
	    return node.isSameNode(other);
	}

	@Override
	public String lookupPrefix(String namespaceURI) {
	    return node.lookupPrefix(namespaceURI);
	}

	@Override
	public boolean isDefaultNamespace(String namespaceURI) {
	    return node.isDefaultNamespace(namespaceURI);
	}

	@Override
	public String lookupNamespaceURI(String prefix) {
	    return node.lookupNamespaceURI(prefix);
	}

	@Override
	public boolean isEqualNode(Node arg) {
	    return node.isEqualNode(arg);
	}

	@Override
	public Object getFeature(String feature, String version) {
	    return node.getFeature(feature, version);
	}

	@Override
	public Object setUserData(String key, Object data, UserDataHandler handler) {
	    return node.setUserData(key, data, handler);
	}

	@Override
	public Object getUserData(String key) {
	    return node.getUserData(key);
	}

	public Node getInternalNode() {

	    return node;
	}
    }

    public void print() {
	try {
	    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	    DOMSource xmlSource = new DOMSource(document);
	    Result outputTarget = new StreamResult(outputStream);
	    TransformerFactory factory = TransformerFactory.newInstance();
	    Transformer transformer = factory.newTransformer();
	    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
	    transformer.transform(xmlSource, outputTarget);
	    System.out.println(outputStream.toString());

	} catch (Exception ex) {
	    ex.printStackTrace();
	    // it should not happen
	}
    }

    /**
     * @param node
     * @return
     * @throws TransformerException
     * @throws UnsupportedEncodingException
     */
    public static String asString(Node node) throws TransformerException, UnsupportedEncodingException {

	return asOutputStream(node).toString("UTF-8");
    }

    private static class NamespaceContextImpl implements NamespaceContext {

	private Map<String, String> namespaces;

	NamespaceContextImpl(Map<String, String> namespaces) {
	    this.namespaces = namespaces;
	}

	public String getNamespaceURI(String prefix) {
	    return namespaces.get(prefix);
	}

	public String getPrefix(String namespace) {
	    for (Map.Entry<String, String> entry : namespaces.entrySet()) {
		if (entry.getValue().equals(namespace)) {
		    return entry.getKey();
		}
	    }
	    return null;
	}

	public Iterator<?> getPrefixes(String namespace) {
	    String prefix = namespaces.get(namespace);
	    if (prefix == null) {
		return null;
	    }
	    return Collections.singletonList(prefix).iterator();
	}
    }

    /**
     * @return
     */
    public Document getDocument() {
	return document;
    }

    /**
     * @return
     * @throws TransformerException
     * @throws UnsupportedEncodingException
     */
    public String asString() throws TransformerException, UnsupportedEncodingException {

	return asString(document);
    }

    /**
     * @return
     * @throws TransformerException
     */
    public ByteArrayInputStream asStream() throws TransformerException {

	ByteArrayOutputStream outputStream = asOutputStream(document);
	return new ByteArrayInputStream(outputStream.toByteArray());
    }

    private static ByteArrayOutputStream asOutputStream(Node node) throws TransformerException {

	ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	DOMSource xmlSource = new DOMSource(node);
	Result outputTarget = new StreamResult(outputStream);
	TransformerFactory factory = TransformerFactory.newInstance();
	Transformer transformer = factory.newTransformer();
	transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
	transformer.transform(xmlSource, outputTarget);
	return outputStream;
    }

}
