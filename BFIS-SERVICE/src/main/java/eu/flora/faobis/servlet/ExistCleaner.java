package eu.flora.faobis.servlet;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.xmldb.api.base.XMLDBException;

import eu.flora.faobis.ExistConnector;
import eu.flora.faobis.GeonodeConnector;
import eu.flora.faobis.GeoserverConnector;

public class ExistCleaner {
    /**
     * Removes from the repository all legends that are not used by layers
     */
    public static void cleanRepository() {
	List<String> layers = GeoserverConnector.getInstance().getManager().getReader().getLayers().getNames();
	List<String> usedLegends = new ArrayList<String>();
	for (String layer : layers) {

	    SimpleEntry<String, String> classification = GeonodeConnector.getInstance().getClassification(layer);

	    if (classification == null) {
		System.out.println("Classification for layer: " + layer + " -> NOT FOUND!");
	    } else {
		String lcmlAttribute = classification.getKey();
		String legend = classification.getValue();
		usedLegends.add(legend);
		System.out.println("Classification for layer: " + layer + " -> " + legend);
	    }
	}

	Set<String> legends = ExistConnector.getInstance().getLegends();
	for (String legend : legends) {
	    if (usedLegends.contains(legend)) {
		System.out.println("Legend in use: " + legend);
	    } else {
		System.out.println("Legend NOT in use, deleting: " + legend);
		try {
		    ExistConnector.getInstance().deleteLegend(legend);
		} catch (XMLDBException e) {
		    e.printStackTrace();
		}
	    }
	}

    }
}
