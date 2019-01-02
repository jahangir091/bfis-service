package eu.flora.faobis.servlet;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.json.JSONObject;

public class ScoreParserQueryToOriginal {
    HashMap<String, String> map = new HashMap<>();

    public ScoreParserQueryToOriginal(String json, Double threshold) {
	JSONObject jObject = new JSONObject(json);
	JSONObject scores = jObject.getJSONObject("scores");
	Set<String> qClasses = scores.keySet();
	// originalClass -> queryClass -> Double
	HashMap<String, HashMap<String, Double>> map = new HashMap<String, HashMap<String, Double>>();
	for (String qClass : qClasses) {
	    JSONObject qClassScore = scores.getJSONObject(qClass);
	    Iterator<String> originalClasses = qClassScore.keys();
	    double max = 0;
	    String outClass = qClass;
	    while (originalClasses.hasNext()) {
		String originalClass = (String) originalClasses.next();
		double queryScore = qClassScore.getDouble(originalClass);
		HashMap<String, Double> originalClassScores = map.get(originalClass);
		if (originalClassScores == null) {
		    originalClassScores = new HashMap<>();
		}
		originalClassScores.put(qClass, queryScore);
		map.put(originalClass, originalClassScores);
	    }
	}

	for (String oClass : map.keySet()) {

	    HashMap<String, Double> oClassScore = map.get(oClass);
	    double max = 0;
	    String outClass = oClass;
	    for (String qClass : qClasses) {
		Double queryScore = oClassScore.get(qClass);
		if (queryScore > max && queryScore > threshold) {
		    max = queryScore;
		    outClass = qClass;
		}
	    }
	    this.map.put(oClass, outClass);
	}

    }

    @Override
    public String toString() {
	String ret = "";
	for (String key : map.keySet()) {
	    ret += key + "->" + map.get(key) + ";";
	}
	return ret;
    }

    public static void main(String[] args) {
	String json = "{'scores': {'lcc1': {'12b': 70.0," + //
		"'14a': 60.0," + //
		"'lcc8': 90.0," + //
		"'lcc9': 70.0}}}";
	ScoreParserQueryToOriginal sp = new ScoreParserQueryToOriginal(json, 70.);
	System.out.println(sp);

    }
}
