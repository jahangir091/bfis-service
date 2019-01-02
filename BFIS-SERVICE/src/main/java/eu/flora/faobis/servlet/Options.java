package eu.flora.faobis.servlet;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement
public class Options {

    @javax.xml.bind.annotation.XmlElement(name = "option")
    private List<String> options = new ArrayList<String>();

    @XmlTransient
    public List<String> getOptions() {
	return options;
    }

    public boolean add(String e) {
	return options.add(e);
    }

}
