package eu.flora.faobis;

import java.util.ArrayList;
import java.util.List;

public class LCMLElement {

    private String xmlName;
    private String name;
    private String type;
    private String description;

    public String getXmlName() {
	return xmlName;
    }

    public void setXmlName(String xmlName) {
	this.xmlName = xmlName;
    }

    public String getName() {
	return name;
    }

    public void setName(String name) {
	this.name = name;
    }

    public String getType() {
	return type;
    }

    public void setType(String type) {
	this.type = type;
    }

    public String getDescription() {
	return description;
    }

    public void setDescription(String description) {
	this.description = description;
    }

    public LCMLElement(String xmlName, String name, String type, String description) {
	this.xmlName = xmlName;
	this.name = name;
	this.type = type;
	this.description = description;
    }

    private List<LCMLElement> children = new ArrayList<>();

    public List<LCMLElement> getChildren() {
	return children;
    }

    public void setChildren(List<LCMLElement> children) {
	this.children = children;
    }

    @Override
    public String toString() {
	return xmlName + " (" + type + ")";
    }

    public void print(String prefix) {
	System.out.println(prefix + xmlName + " (" + type + ")");
	for (LCMLElement child : children) {
	    child.print(prefix + "*");
	}
    }

    public boolean fullMatch(LCMLElement element, boolean considerCharacteristics) {
	if (!considerCharacteristics && element.getXmlName().equals("LC_Characteristic")) {
	    return true;
	}
	if (!element.getXmlName().equals(getXmlName()) || !element.getType().equals(getType())) {
//	    System.out.println("Considering " + getName() + " & " + element.getName() + ": false");
	    return false;
	}
	for (LCMLElement lcmlElement : element.getChildren()) {
	    String xmlName = lcmlElement.getXmlName();
	    if (!considerCharacteristics && xmlName.equals("LC_Characteristic")) {
		continue;
	    }
	    boolean fullMatchChildren = false;
	    innerCycle: for (LCMLElement original : children) {
		boolean result = original.fullMatch(lcmlElement, considerCharacteristics);
//		System.out.println("Considering " + original.getName() + " & " + lcmlElement.getName() + ": " + result);
		if (result) {
		    fullMatchChildren = true;
		    break innerCycle;
		}
	    }
	    if (!fullMatchChildren) {
		return false;
	    }
	}
	return true;

    }

}
