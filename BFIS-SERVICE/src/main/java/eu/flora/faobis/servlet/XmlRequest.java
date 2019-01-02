package eu.flora.faobis.servlet;

import java.io.InputStream;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

@XmlRootElement(name = "XmlParameter")
public class XmlRequest {

    @XmlAnyElement
    private Object xml;

    public XmlRequest() {

    }

    public XmlRequest(InputStream stream) throws Exception {
	super();
	DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	xml = dBuilder.parse(stream).getDocumentElement();
	
    }

    @XmlTransient
    public Object getXml() {
	return xml;
    }

    public void setXml(Object xml) {
	this.xml = xml;
    }
}
