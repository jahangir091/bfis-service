package eu.flora.faobis;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

import org.apache.commons.io.IOUtils;

public class PostGISConnector {
    
    static Properties prop = new Properties();

    public static  String LCCS_CODE_COLUMN = prop.getProperty("classificationtable");
    public static  String SOURCE_LCCS_CODE_COLUMN;
    public static String AGGREGATION_SUFFIX; 
    private static String IDENTIFIER_COLUMN;
    private static PostGISConnector instance;

    public static PostGISConnector getInstance() {
	if (instance == null) {
	    instance = new PostGISConnector();
	}
	return instance;
    }

    private PostGISConnector() {
         LCCS_CODE_COLUMN = prop.getProperty("classificationtable");
         SOURCE_LCCS_CODE_COLUMN  = prop.getProperty("source_lccs_code");
         AGGREGATION_SUFFIX = "aggregation";
         IDENTIFIER_COLUMN = "gid";
    }

    private Connection connection;

    public void initConnection(String url, String user, String password) throws SQLException {
	/*
	 * Load the JDBC driver and establish a connection.
	 */
	try {
	    Class.forName("org.postgresql.Driver");
	} catch (ClassNotFoundException e) {
	    e.printStackTrace();
	}
	this.connection = DriverManager.getConnection(url, user, password);
    }

    public void close() throws SQLException {
	connection.close();
    }

    public static void main(String[] args) {

	// GeoserverConnector.getInstance().init("http://faobis.essi-lab.eu:8080/geoserver", "bisadmin", "Bang17");
	//
	// String table = "land_cover_2015";
	// String db = "bis";
	// String discriminantColumn = "lccs_code";
	//
	// // String table = "nyc_buildings";
	// // String db = "nyc";
	// // String discriminantColumn = "gid";
	//
	// try {
	// /*
	// * Load the JDBC driver and establish a connection.
	// */
	// String url = "jdbc:postgresql://faobis.essi-lab.eu:5433/" + db;
	//
	// PostGISConnector.getInstance().initConnection(url, "postgres", "Bang17");
	//
	// // connector.addColorColumn(table, colorColumn, discriminantColumn);
	//
	//// String styleDocument = PostGISConnector.getInstance().createStyle(table, discriminantColumn);
	//
	// // GeoserverConnector.getInstance().u
	//
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
    }

    public String createStyle(String table) throws Exception {
	return createStyle(table, LCCS_CODE_COLUMN);
    }

    public String createStyle(String table, String discriminantColumn) throws Exception {
	Statement st = connection.createStatement();

	ResultSet resultSet = st.executeQuery("SELECT DISTINCT " + discriminantColumn + " FROM " + table);

	Set<String> codes = new HashSet<String>();
	while (resultSet.next()) {
	    String lccsCode = resultSet.getString(1);
	    if (lccsCode != null) {
		codes.add(lccsCode);
	    }
	}

	InputStream stream = PostGISConnector.class.getClassLoader().getResourceAsStream("templates/sld-template.xml");
	String sldDocument = null;
	try {
	    sldDocument = IOUtils.toString(stream, "UTF-8");
	    stream.close();
	} catch (IOException e) {
	    e.printStackTrace();
	}

	String rules = "";
	for (String code : codes) {
	    String codeValue = code;
	    // int n = Integer.parseInt(code);
	    // n = n % 2;
	    // codeValue = "" + n;
	    String color = getRandomColorRGB(codeValue);
	    String rule = "<Rule>\n" + //
		    "<Title>" + code + "</Title>\n" + //
		    "<ogc:Filter>\n" + //
		    "<ogc:PropertyIsEqualTo>\n" + //
		    "<ogc:PropertyName>" + discriminantColumn + "</ogc:PropertyName>\n" + //
		    "<ogc:Literal>" + code + "</ogc:Literal>\n" + //
		    "</ogc:PropertyIsEqualTo>\n" + //
		    "</ogc:Filter>\n" + //
		    "<PolygonSymbolizer>\n" + //
		    "<Stroke>\n" + //
		    "<CssParameter name=\"stroke\">gray</CssParameter>\n" + //
		    "<CssParameter name=\"stroke-width\">0.1</CssParameter>\n" + //
		    "</Stroke>\n" + //
		    "<Fill>\n" + //
		    "<CssParameter name=\"fill\">" + color + "</CssParameter>\n" + //
		    "</Fill>\n" + //
		    "</PolygonSymbolizer>\n" + //
		    "<TextSymbolizer>\n" + //
		    "<Label>\n" + //
		    "<ogc:PropertyName>" + discriminantColumn + "</ogc:PropertyName>\n" + //
		    "</Label>\n" + //
		    "</TextSymbolizer>\n" + //
		    "</Rule>\n";
	    rules += rule + "\n";

	}
	sldDocument = sldDocument.replace("<!-- RULES -->", rules);
	System.out.println(sldDocument);
	// st.executeQuery("SELECT UPDATE land_cover_2015 SET color=");

	st.close();

	return sldDocument;
    }

    public void addColorColumn(String table, String colorColumn, String discriminantColumn) {
	Statement st;
	try {
	    st = connection.createStatement();

	    ResultSet resultSet = st.executeQuery("SELECT column_name from information_schema.columns WHERE table_name='" + table
		    + "' and column_name='" + colorColumn + "'");

	    if (!resultSet.next()) {
		st.executeUpdate("ALTER TABLE " + table + " ADD " + colorColumn + " char(25)");
	    }

	    resultSet = st.executeQuery("SELECT DISTINCT " + discriminantColumn + " FROM " + table);

	    Set<String> codes = new HashSet<String>();
	    while (resultSet.next()) {
		String lccsCode = resultSet.getString(1);
		if (lccsCode != null) {
		    codes.add(lccsCode);
		}
	    }
	    for (String code : codes) {
		String codeValue = code;
		int n = Integer.parseInt(code);
		n = n % 2;
		codeValue = "" + n;
		String color = getRandomColorRGB(codeValue);
		String query = "UPDATE " + table + " SET " + colorColumn + "='" + color + "' WHERE " + discriminantColumn + "='" + code
			+ "'";
		System.out.println(query);
		st.executeUpdate(query);
	    }

	    // st.executeQuery("SELECT UPDATE land_cover_2015 SET color=");

	    st.close();

	} catch (SQLException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    private static String getRandomColorRGB(String code) {
	Random random = new Random(code.hashCode());
	int R = (int) (random.nextDouble() * 256);
	int G = (int) (random.nextDouble() * 256);
	int B = (int) (random.nextDouble() * 256);
	Color color = new Color(R, G, B); // random color, but can be bright or dull

	// to get rainbow, pastel colors
	final float hue = random.nextFloat();
	final float saturation = 0.9f;// 1.0 for brilliant, 0.0 for dull
	final float luminance = 1.0f; // 1.0 for brighter, 0.0 for black
	color = Color.getHSBColor(hue, saturation, luminance);
	R = color.getRed();
	G = color.getGreen();
	B = color.getBlue();
	String hex = String.format("#%02X%02X%02X", R, G, B);

	System.out.println("Color: " + code + " -> " + hex);

	return hex;
    }

    public void createAggregationTable(String table, String aggregation, List<String> originalCodes, List<String> translatedCodes) {
	try {
	    Statement st = connection.createStatement();
	    String aggregationTable = aggregation + AGGREGATION_SUFFIX;
	    System.out.println("Creating aggregated table " + aggregationTable);
	    st.executeUpdate("DROP VIEW IF EXISTS " + aggregation);
	    st.executeUpdate("DROP TABLE IF EXISTS " + aggregationTable);
	    st.executeUpdate("CREATE TABLE " + aggregationTable + " (" + SOURCE_LCCS_CODE_COLUMN + " varchar(20)," + LCCS_CODE_COLUMN
		    + " varchar(20))");
	    String values = "";
	    Iterator<?> originalIterator = originalCodes.iterator();
	    Iterator<?> translatedIterator = translatedCodes.iterator();

	    while (originalIterator.hasNext()) {
		String originalCode = (String) originalIterator.next();
		String translatedCode = (String) translatedIterator.next();
		values += "('" + originalCode + "','" + translatedCode + "'),";
	    }
	    // removes the last comma
	    values = values.substring(0, values.length() - 1);
	    System.out.println("Populating aggregated table " + aggregationTable);

	    st.executeUpdate(
		    "INSERT INTO " + aggregationTable + " (" + SOURCE_LCCS_CODE_COLUMN + "," + LCCS_CODE_COLUMN + ") VALUES " + values);
	    System.out.println("Populated.");

	    System.out.println("Creating view from table " + table + " and aggregation table " + aggregationTable);

	    ResultSet resultSet = st.executeQuery("SELECT column_name from information_schema.columns WHERE table_name='" + table + "'");

	    String columns = "";
	    while (resultSet.next()) {
		String column = resultSet.getString(1);
		if (!column.equals(LCCS_CODE_COLUMN)) {
		    columns += column + ",";
		}
	    }
	    columns = columns + aggregation + AGGREGATION_SUFFIX + "." + LCCS_CODE_COLUMN;

	    String query = "CREATE VIEW " + aggregation + " AS SELECT " + columns + " from " + table + " INNER JOIN " + aggregation
		    + AGGREGATION_SUFFIX + " ON " + table + "." + LCCS_CODE_COLUMN + "=" + aggregation + AGGREGATION_SUFFIX + "."
		    + SOURCE_LCCS_CODE_COLUMN;
	    System.out.println(query);
	    st.executeUpdate(query);

	    System.out.println("View created.");

	    st.close();
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public Set<String> getTables() {

	Statement st;
	try {
	    st = connection.createStatement();

	    ResultSet resultSet = st
		    .executeQuery("select table_name from information_schema.columns where column_name='" + LCCS_CODE_COLUMN + "';");

	    Set<String> tables = new HashSet<String>();
	    while (resultSet.next()) {
		String table = resultSet.getString(1);
		if (table != null) {
		    tables.add(table);
		}
	    }

	    // removing the aggregation tables
	    resultSet = st
		    .executeQuery("select table_name from information_schema.columns where column_name='" + SOURCE_LCCS_CODE_COLUMN + "';");

	    while (resultSet.next()) {
		String table = resultSet.getString(1);
		if (table != null) {
		    tables.remove(table);
		}
	    }

	    // removes also the views
	    Set<String> aggregations = getAggregations();
	    tables.removeAll(aggregations);

	    st.close();

	    return tables;

	    // st.executeQuery("SELECT UPDATE land_cover_2015 SET color=");

	} catch (

	SQLException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	return null;

    }

    public void removeTable(String table) throws SQLException {
	Statement st = connection.createStatement();

	st.executeUpdate("DROP TABLE IF EXISTS " + table);

    }

    public Set<String> getAggregations() {

	Statement st;
	try {
	    st = connection.createStatement();

	    ResultSet resultSet = st.executeQuery("select information_schema.views.table_name from information_schema.columns "
		    + "INNER JOIN information_schema.views ON information_schema.columns.table_name=information_schema.views.table_name "
		    + "where column_name='" + LCCS_CODE_COLUMN + "';");

	    Set<String> tables = new HashSet<String>();
	    while (resultSet.next()) {
		String table = resultSet.getString(1);
		if (table != null) {
		    tables.add(table);
		}
	    }

	    st.close();

	    return tables;

	    // st.executeQuery("SELECT UPDATE land_cover_2015 SET color=");

	} catch (

	SQLException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	return null;

    }

    public void removeAggregation(String view) throws SQLException {
	Statement st = connection.createStatement();

	st.executeUpdate("DROP VIEW IF EXISTS " + view);
	st.executeUpdate("DROP TABLE IF EXISTS " + view + AGGREGATION_SUFFIX);

    }

    public List<String> getFeature(String layer, String feature) {

	List<String> attributes = new ArrayList<String>();
	Statement st;
	try {
	    st = connection.createStatement();

	    ResultSet resultSet;
	    if (feature == null || feature.equals("")) {
		resultSet = st.executeQuery("select column_name from information_schema.columns WHERE table_name='" + layer + "'");
	    } else {
		resultSet = st.executeQuery("select * from " + layer + " where " + IDENTIFIER_COLUMN + "='" + feature + "';");
	    }

	    while (resultSet.next()) {
		int count = resultSet.getMetaData().getColumnCount();
		for (int i = 0; i < count; i++) {
		    String res = resultSet.getString(i + 1);
		    if (res != null) {
			attributes.add(res);
		    }
		}

	    }

	} catch (SQLException e) {
	    e.printStackTrace();
	}

	return attributes;
    }

    public Set<String> getFeatures(String layer, String bbox, String polygon, List<String> attributesNames,
	    List<String> attributesValues) {

	Set<String> features = new HashSet<String>();
	Statement st;
	try {
	    st = connection.createStatement();

	    String query = "";

	    if (bbox != null && bbox.contains(",")) {
		String[] split = bbox.split(",");
		query = " geom && ST_MakeEnvelope(" + split[1] + "," + split[2] + "," + split[3] + "," + split[0] + ",4326)";
	    }

	    if (polygon != null && polygon.contains(",")) {
		String[] split = polygon.split(",");
		if (!query.isEmpty()) {
		    query = query + " AND";
		}
		String points = "";
		for (int i = 0; i < split.length; i = i + 2) {
		    points += split[i + 1] + " " + split[i] + ",";
		}
		points = points.substring(0, points.length() - 1);
		query = " geom && ST_MakePolygon(ST_GeomFromText('LINESTRING(" + points + ")'))";
	    }

	    if (!attributesNames.isEmpty()) {
		for (int i = 0; i < attributesNames.size(); i++) {
		    String attributeName = attributesNames.get(i);
		    String attributeValue = attributesValues.get(i);
		    String prefix = " AND ";
		    if (i == 0 && query.isEmpty()) {
			prefix = "";
		    }
		    query += prefix + attributeName + "='" + attributeValue + "'";
		}
	    }

	    if (!query.isEmpty()) {
		query = " WHERE " + query;
	    }

	    ResultSet resultSet = st.executeQuery("select " + IDENTIFIER_COLUMN + " from " + layer + query + ";");

	    while (resultSet.next()) {
		String feature = resultSet.getString(1);
		if (feature != null) {
		    features.add(feature);
		}
	    }

	} catch (SQLException e) {
	    e.printStackTrace();
	}

	return features;
    }

    public Set<String> getClasses(String featureSet, List<String> features) {

	Set<String> classes = new HashSet<String>();
	Statement st;
	try {
	    st = connection.createStatement();

	    String query = "";

	    if (features != null && !features.isEmpty()) {

		query = " WHERE ";
		for (String feature : features) {
		    query += IDENTIFIER_COLUMN + "=" + feature + " OR";
		}
		query = query.substring(0, query.length() - 2);
	    }

	    ResultSet resultSet = st.executeQuery("select " + LCCS_CODE_COLUMN + " from " + featureSet + query + ";");

	    while (resultSet.next()) {
		String clazz = resultSet.getString(1);
		if (clazz != null) {
		    classes.add(clazz);
		}
	    }

	} catch (SQLException e) {
	    e.printStackTrace();
	}

	return classes;
    }

}
