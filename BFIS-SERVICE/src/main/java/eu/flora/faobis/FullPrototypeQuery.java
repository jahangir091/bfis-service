package eu.flora.faobis;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Node;

import eu.flora.essi.lib.xml.XMLDocumentReader;

public class FullPrototypeQuery {

    private XMLDocumentReader legend;
    private List<LCMLClass> classes = new ArrayList<>();

    public FullPrototypeQuery(InputStream legend) {
	try {
	    this.legend = new XMLDocumentReader(legend);
	    Node[] nodes = this.legend.evaluateNodes("//*:LC_LandCoverClass[@id]");
	    for (Node node : nodes) {
		LCMLClass clazz = new LCMLClass(this.legend, node);
		classes.add(clazz);
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public void query(InputStream clazzStream) {
	try {

	    LCMLClass parsedClass = new LCMLClass(clazzStream);

	} catch (Exception e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

    }

    public List<LCMLClass> getClasses() {
	return classes;
    }

}
