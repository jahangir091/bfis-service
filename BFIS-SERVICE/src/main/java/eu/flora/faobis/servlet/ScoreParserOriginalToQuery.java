package eu.flora.faobis.servlet;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.json.JSONObject;

import eu.flora.faobis.servlet.SemanticFilter.Type;

public class ScoreParserOriginalToQuery {
    HashMap<String, String> map = new HashMap<>();
    private JSONObject json;

    public JSONObject getJSON() {
	return json;
    }

    public ScoreParserOriginalToQuery(String jsonString, Type type, Double threshold) {
	if (type.equals(Type.FULL_PROTOTYPE)) {
	    threshold = 100.0;
	}
	this.json = new JSONObject(jsonString);
	JSONObject scores = this.json.getJSONObject("scores");
	Set<String> originalClasses = scores.keySet();
	for (String originalClass : originalClasses) {
	    JSONObject originalClassScore = scores.getJSONObject(originalClass);
	    Iterator<String> queryClasses = originalClassScore.keys();
	    double max = 0;
	    String outClass = originalClass;
	    while (queryClasses.hasNext()) {
		String queryClass = (String) queryClasses.next();
		double queryScore = originalClassScore.getDouble(queryClass);
		if (type.equals(Type.FULL_PROTOTYPE)) {
		    if (queryScore < 99.999999) {
			queryScore = 0;
		    } else {
			queryScore = 100.0;
		    }
		    originalClassScore.put(queryClass, queryScore);
		}
		if (queryScore > max && queryScore >= threshold) {
		    max = queryScore;
		    outClass = queryClass;
		}
	    }
	    map.put(originalClass, outClass);
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
		"'lcc8': 100.0," + //
		"'lcc9': 70.0}}}";
	ScoreParserOriginalToQuery sp = new ScoreParserOriginalToQuery(json, Type.SIMILARITY_BASED, 99.);
	System.out.println(sp);
	System.out.println(sp.getJSON());
	sp = new ScoreParserOriginalToQuery(json, Type.FULL_PROTOTYPE, 99.);
	System.out.println(sp);
	System.out.println(sp.getJSON());

    }
}
