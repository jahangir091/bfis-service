package eu.flora.faobis.servlet;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

@XmlRootElement(name = "SemanticFilter")
public class SemanticFilter {

    public enum Type {
	FULL_PROTOTYPE, SIMILARITY_BASED
    }

    @XmlElement
    private Type type;
    @XmlElement
    private Double similarityThreshold;
    @XmlElementWrapper(name = "advancedOptions")
    private Map<String, Options> advancedOptions = new HashMap<String, Options>();

    @XmlElement
    private String code;

    @XmlTransient
    public String getCode() {
	return code;
    }

    public void setCode(String code) {
	this.code = code;
    }

    @XmlTransient
    public Map<String, Options> getAdvancedOptions() {
	return advancedOptions;
    }

    @XmlTransient
    public Double getSimilarityThreshold() {
	return similarityThreshold;
    }

    public void setSimilarityThreshold(Double similarityThreshold) {
	this.similarityThreshold = similarityThreshold;
    }

    @XmlTransient
    public Type getType() {
	return type;
    }

    public void setType(Type type) {
	this.type = type;
    }

    public static void main(String[] args) throws Exception {
	SemanticFilter test = new SemanticFilter();
	test.setType(Type.FULL_PROTOTYPE);
	test.setSimilarityThreshold(34.0);
	DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
	DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

	// root elements
	String code = "BOs";
	test.setCode(code);

	Options list1 = new Options();
	list1.add("aaa a");
	list1.add("bbb b");
	test.getAdvancedOptions().put("option1", list1);
	Options list2 = new Options();
	list2.add("ccc c");
	test.getAdvancedOptions().put("option2", list2);

	Marshaller marshaller = JAXBContext.newInstance(SemanticFilter.class).createMarshaller();

	marshaller.marshal(test, System.out);


    }

}
