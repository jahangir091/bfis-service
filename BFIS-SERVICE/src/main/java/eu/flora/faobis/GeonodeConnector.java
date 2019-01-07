package eu.flora.faobis;

import com.google.common.collect.Lists;
import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.apache.commons.io.IOUtils;

public class GeonodeConnector {
    private static PropertyReader propertyReader = PropertyReader.getInstance();
    public static final String LCML_CODE_COLUMN = propertyReader.getLCML_CODE_COLUMN();
    public static final String SOURCE_LCML_CODE_COLUMN = propertyReader.getSOURCE_LCML_CODE_COLUMN();
    public static final String AGGREGATION_SUFFIX = propertyReader.getAGGREGATION_SUFFIX();
    private static final String IDENTIFIER_COLUMN = propertyReader.getIDENTIFIER_COLUMN();
    private static String CLASSIFICATION_TABLE;
    private static final String LAYER_COLUMN = propertyReader.getLAYER_COLUMN();
    private static final String LCCS_ATTRIBUTE_COLUMN = propertyReader.getLCCS_ATTRIBUTE_COLUMN();
    private static final String LCCS_LEGEND_COLUMN = propertyReader.getLCCS_LEGEND_COLUMN();
    private static final String GEOMETRY = propertyReader.getGEOMETRY();
    private static GeonodeConnector instance;

    public static GeonodeConnector getInstance() {
        if (instance == null) {
            instance = new GeonodeConnector();
        }
        return instance;
    }

    private GeonodeConnector() {
    }

    private Connection metadataConnection = null;
    private Connection dataConnection = null;
//    private String hostname;
    private String geoserverurl;
    private String webserverurl;

    public void initConnection(String url, String user, String password, String metadataDb, String dataDb,
            String webserverurl, String geoserverurl, String classificationtable)
            throws SQLException {
        /*
	 * Load the JDBC driver and establish a connection.
         */
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        while (dataConnection == null) {
            try {
                this.metadataConnection = DriverManager.getConnection(url + metadataDb, user, password);
                this.dataConnection = DriverManager.getConnection(url + dataDb, user, password);
            } catch (Exception e) {
                System.out.println("Error occurred: " + e.getMessage());
                System.out.println("Trying to connect again to PostGIS...");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        }
//	this.hostname = hostname;
        this.geoserverurl = geoserverurl;
        this.webserverurl = webserverurl;
        GeonodeConnector.CLASSIFICATION_TABLE = classificationtable;
    }

    public void close() throws SQLException {
        metadataConnection.close();
        dataConnection.close();
    }

    public static void main(String[] args) {

        System.out.println("CREATE TABLE " + CLASSIFICATION_TABLE + " ("
                + //
                LAYER_COLUMN + " varchar(256)," + LCCS_ATTRIBUTE_COLUMN + " varchar(256),"
                + //
                LCCS_LEGEND_COLUMN + " varchar(256)"//
                + ")");
        // GeonodeConnector.getInstance().createClassificationTable();

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

    public String createStyle(String table, String discriminantColumn) throws Exception {
        Statement st = dataConnection.createStatement();

        ResultSet resultSet = st.executeQuery("SELECT DISTINCT \"" + discriminantColumn + "\" FROM " + table);

        Set<String> codes = new HashSet<String>();
        while (resultSet.next()) {
            String lccsCode = resultSet.getString(1);
            if (lccsCode != null) {
                codes.add(lccsCode);
            }
        }

        InputStream stream = GeonodeConnector.class.getClassLoader().getResourceAsStream("templates/sld-template.xml");
        String sldDocument = null;
        try {
            sldDocument = IOUtils.toString(stream, "UTF-8");
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String rules = "";
        ArrayList<String> codeList = Lists.newArrayList(codes.iterator());
        for (int i = 0; i < codeList.size(); i++) {
            String code = codeList.get(i);
            String codeValue = code;
            // int n = Integer.parseInt(code);
            // n = n % 2;
            // codeValue = "" + n;
            String color = getRandomColorRGB(i, codeList.size(), codeValue);
            String rule = "<Rule>\n"
                    + //
                    "<Title>" + code + "</Title>\n"
                    + //
                    "<ogc:Filter>\n"
                    + //
                    "<ogc:PropertyIsEqualTo>\n"
                    + //
                    "<ogc:PropertyName>" + discriminantColumn + "</ogc:PropertyName>\n"
                    + //
                    "<ogc:Literal>" + code + "</ogc:Literal>\n"
                    + //
                    "</ogc:PropertyIsEqualTo>\n"
                    + //
                    "</ogc:Filter>\n"
                    + //
                    "<PolygonSymbolizer>\n"
                    + //
                    "<Stroke>\n"
                    + //
                    "<CssParameter name=\"stroke\">gray</CssParameter>\n"
                    + //
                    "<CssParameter name=\"stroke-width\">0.1</CssParameter>\n"
                    + //
                    "</Stroke>\n"
                    + //
                    "<Fill>\n"
                    + //
                    "<CssParameter name=\"fill\">" + color + "</CssParameter>\n"
                    + //
                    "</Fill>\n"
                    + //
                    "</PolygonSymbolizer>\n"
                    + //
                    "<TextSymbolizer>\n"
                    + //
                    "<Label>\n"
                    + //
                    "<ogc:PropertyName>" + discriminantColumn + "</ogc:PropertyName>\n"
                    + //
                    "</Label>\n"
                    + //
                    "</TextSymbolizer>\n"
                    + //
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
            st = metadataConnection.createStatement();

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
            ArrayList<String> codeList = Lists.newArrayList(codes.iterator());
            for (int i = 0; i < codeList.size(); i++) {
                String code = codeList.get(i);
                String codeValue = code;
                int n = Integer.parseInt(code);
                n = n % 2;
                codeValue = "" + n;
                String color = getRandomColorRGB(i, codeList.size(), codeValue);
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

    private static String getRandomColorRGB(int numColor, int numColors, String code) {
        Random random = new Random(code.hashCode());
        // int R = (int) (random.nextDouble() * 256);
        // int G = (int) (random.nextDouble() * 256);
        // int B = (int) (random.nextDouble() * 256);
        // Color color = new Color(R, G, B); // random color, but can be bright or dull
        //
        // // to get rainbow, pastel colors
        // final float hue = random.nextFloat();
        // final float saturation = 0.9f;// 1.0 for brilliant, 0.0 for dull
        // final float luminance = 1.0f; // 1.0 for brighter, 0.0 for black

        Color color = null;
        // for (int i = 0; i < 360; i += 360 / 100.0) {
        float hue = random.nextInt(360);
        float saturation = 90 + random.nextFloat() * 10;
        float lightness = 50 + random.nextFloat() * 10;
        color = Color.getHSBColor(hue, saturation, lightness);
        // }

        int R = color.getRed();
        int G = color.getGreen();
        int B = color.getBlue();
        String hex = String.format("#%02X%02X%02X", R, G, B);

        System.out.println("Color: " + code + " -> " + hex);

        return hex;
    }

    public Set<String> getTables() {

        Statement st;
        try {
            st = metadataConnection.createStatement();

            ResultSet resultSet = st
                    .executeQuery("select table_name from information_schema.columns where column_name='" + LCML_CODE_COLUMN + "';");

            Set<String> tables = new HashSet<String>();
            while (resultSet.next()) {
                String table = resultSet.getString(1);
                if (table != null) {
                    tables.add(table);
                }
            }

            // removing the aggregation tables
            resultSet = st
                    .executeQuery("select table_name from information_schema.columns where column_name='" + SOURCE_LCML_CODE_COLUMN + "';");

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
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;

    }

    public void removeTable(String table) throws SQLException {
        Statement st = metadataConnection.createStatement();

        st.executeUpdate("DROP TABLE IF EXISTS " + table);

    }

    public Set<String> getAggregations() {

        Statement st;
        try {
            st = metadataConnection.createStatement();

            ResultSet resultSet = st.executeQuery("select information_schema.views.table_name from information_schema.columns "
                    + "INNER JOIN information_schema.views ON information_schema.columns.table_name=information_schema.views.table_name "
                    + "where column_name='" + LCML_CODE_COLUMN + "';");

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
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;

    }

    public void removeAggregation(String view) throws SQLException {
        Statement st = metadataConnection.createStatement();

        st.executeUpdate("DROP VIEW IF EXISTS " + view);
        st.executeUpdate("DROP TABLE IF EXISTS " + view + AGGREGATION_SUFFIX);

    }

    public List<String> getAttributeValues(String layer, String attribute) {
        List<String> values = new ArrayList<String>();
        Statement st;
        try {
            st = dataConnection.createStatement();

            ResultSet resultSet = st.executeQuery("select DISTINCT \"" + attribute + "\" from " + layer + ";");

            while (resultSet.next()) {
                String value = resultSet.getString(1);
                if (value != null) {
                    values.add(value);
                }

            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return values;
    }

    public List<String> getAttributes(String layer, String feature) {

        List<String> attributes = new ArrayList<String>();
        Statement st;
        try {
            st = dataConnection.createStatement();

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

    public List<String> getFeature(String layer, String feature) {

        List<String> attributes = new ArrayList<String>();
        Statement st;
        try {
            st = dataConnection.createStatement();

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

    private String getConstrainedQuery(String bbox, String polygon, List<String> attributesNames, List<String> attributesValues) {
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

        if (attributesNames != null && !attributesNames.isEmpty()) {
            for (int i = 0; i < attributesNames.size(); i++) {
                String attributeName = attributesNames.get(i);
                String attributeValue = attributesValues.get(i);
                String prefix = " AND ";
                if (i == 0 && query.isEmpty()) {
                    prefix = "";
                }
                query += prefix + "\"" + attributeName + "\"='" + attributeValue + "'";
            }
        }

        if (!query.isEmpty()) {
            query = " WHERE " + query;
        }
        return query;
    }

    public String getArea(String layer, String bbox, String polygon, List<String> attributesNames, List<String> attributesValues) {
        Statement st;
        try {
            st = dataConnection.createStatement();

            String query = getConstrainedQuery(bbox, polygon, attributesNames, attributesValues);

            ResultSet resultSet = st.executeQuery("select SUM(st_area(" + GEOMETRY + "::geography)) from " + layer + query + ";");

            while (resultSet.next()) {
                String area = resultSet.getString(1);
                if (area != null) {
                    return area;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public HashMap<String, String> getAreasByClass(String layer, String bbox, String polygon, List<String> attributesNames,
            List<String> attributesValues) {

        HashMap<String, String> areas = new HashMap<String, String>();
        Statement st;
        try {
            st = dataConnection.createStatement();

            String query = getConstrainedQuery(bbox, polygon, attributesNames, attributesValues);

            ResultSet resultSet = st.executeQuery("select \"" + LCML_CODE_COLUMN + "\", SUM(st_area(" + GEOMETRY + "::geography)) from "
                    + layer + query + " group by \"" + LCML_CODE_COLUMN + "\";");

            while (resultSet.next()) {
                String code = resultSet.getString(1);
                String area = resultSet.getString(2);
                if (code != null && area != null) {
                    areas.put(code, area);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return areas;
    }

    public HashMap<String, Integer> getCountsByClass(String layer, String bbox, String polygon, List<String> attributesNames,
            List<String> attributesValues) {

        HashMap<String, Integer> counts = new HashMap<String, Integer>();
        Statement st;
        try {
            st = dataConnection.createStatement();

            String query = getConstrainedQuery(bbox, polygon, attributesNames, attributesValues);

            ResultSet resultSet = st.executeQuery("select \"" + LCML_CODE_COLUMN + "\", COUNT(st_area(" + GEOMETRY + "::geography)) from "
                    + layer + query + " group by \"" + LCML_CODE_COLUMN + "\";");

            while (resultSet.next()) {
                String code = resultSet.getString(1);
                int count = resultSet.getInt(2);
                if (code != null) {
                    counts.put(code, count);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return counts;
    }

    public Set<String> getFeatures(String layer, String bbox, String polygon, List<String> attributesNames, List<String> attributesValues) {

        Set<String> features = new HashSet<String>();
        Statement st;
        try {
            st = dataConnection.createStatement();

            String query = getConstrainedQuery(bbox, polygon, attributesNames, attributesValues);

            ResultSet resultSet = st.executeQuery("select \"" + IDENTIFIER_COLUMN + "\" from " + layer + query + ";");

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

    public Set<String> getClasses(String layer, String lcmlAttribute, List<String> features) {

        Set<String> classes = new HashSet<String>();
        Statement st;
        try {
            st = dataConnection.createStatement();

            String query = "";

            if (features != null && !features.isEmpty()) {

                query = " WHERE ";
                for (String feature : features) {
                    query += IDENTIFIER_COLUMN + "=" + feature + " OR";
                }
                query = query.substring(0, query.length() - 2);
            }

            ResultSet resultSet = st.executeQuery("select \"" + lcmlAttribute + "\" from " + layer + query + ";");

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

    /////////////////////////////
    // NEW IMPLEMENTATION TO GEODASH
    ///////////////////////////
    public Set<String> getLayers() {

        Statement st;
        try {
            st = dataConnection.createStatement();

            ResultSet resultSet = st.executeQuery(
                    "select table_name from information_schema.tables where table_schema='public' and table_type='BASE TABLE' and table_catalog='geodash_dev_backup-imports';");

            Set<String> tables = new HashSet<String>();
            while (resultSet.next()) {
                String table = resultSet.getString(1);
                if (table != null && !table.endsWith(AGGREGATION_SUFFIX)) {
                    tables.add(table);
                }
            }

            st.close();

            return tables;

            // st.executeQuery("SELECT UPDATE land_cover_2015 SET color=");
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;

    }

    public void createAggregationTable(String lcmlColumn, String table, String aggregation, List<String> originalCodes,
            List<String> translatedCodes) {

        String originalLcmlColumn = "original_" + lcmlColumn;
        try {
            Statement st = dataConnection.createStatement();
            String aggregationTable = aggregation + AGGREGATION_SUFFIX;
            System.out.println("Creating aggregated table " + aggregationTable);
            st.executeUpdate("DROP VIEW IF EXISTS " + aggregation);
            st.executeUpdate("DROP TABLE IF EXISTS " + aggregationTable);
            st.executeUpdate("CREATE TABLE " + aggregationTable + " (\"" + originalLcmlColumn + "\" varchar(100),\"" + lcmlColumn
                    + "\" varchar(100))");
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
                    "INSERT INTO " + aggregationTable + " (\"" + originalLcmlColumn + "\",\"" + lcmlColumn + "\") VALUES " + values);
            System.out.println("Populated.");

            System.out.println("Creating view from table " + table + " and aggregation table " + aggregationTable);

            ResultSet resultSet = st.executeQuery("SELECT column_name from information_schema.columns WHERE table_name='" + table + "'");

            String columns = "";
            while (resultSet.next()) {
                String column = resultSet.getString(1);
                if (!column.equals(lcmlColumn)) {
                    columns += "\"" + column + "\",";
                }
            }
            columns = columns + aggregation + AGGREGATION_SUFFIX + ".\"" + lcmlColumn + "\"";

            String query = "CREATE VIEW " + aggregation + " AS SELECT " + columns + " from " + table + " INNER JOIN " + aggregation
                    + AGGREGATION_SUFFIX + " ON (" + table + ".\"" + lcmlColumn + "\"::text=" + aggregation + AGGREGATION_SUFFIX + ".\""
                    + originalLcmlColumn + "\"::text)";
            System.out.println(query);
            st.executeUpdate(query);

            System.out.println("View created.");

            st.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteLayer(String table) {
        try {

            Statement dataSt = dataConnection.createStatement();

            ResultSet resultSet;

            resultSet = dataSt.executeQuery("select table_name from information_schema.views where table_name='" + table + "';");

            boolean hasNext = resultSet.next();

            if (hasNext) {
                dataSt.executeUpdate("DROP VIEW IF EXISTS " + table);
                dataSt.executeUpdate("DROP TABLE IF EXISTS " + table + AGGREGATION_SUFFIX);
            } else {
                dataSt.executeUpdate("DROP TABLE IF EXISTS " + table);
            }

            removeClassification(table);

            Statement st = metadataConnection.createStatement();

            resultSet = st.executeQuery("SELECT resourcebase_ptr_id from layers_layer WHERE name='" + table + "'");
            hasNext = resultSet.next();
            if (hasNext) {
                String resourceBasePointer = resultSet.getString(1);

                // String newResourceBasePointer = "" + resourceBasePointer.hashCode();
                // create a new line in resource base
                st.executeUpdate("DELETE from layers_attribute WHERE \"layer_id\"='" + resourceBasePointer + "'");
                st.executeUpdate("DELETE from layers_layer_styles WHERE \"layer_id\"='" + resourceBasePointer + "'");
                st.executeUpdate("DELETE from layers_layer WHERE \"name\"='" + table + "'");
                st.executeUpdate("DELETE from base_link WHERE \"resource_id\"='" + resourceBasePointer + "'");
                st.executeUpdate("DELETE from base_contactrole WHERE \"resource_id\"='" + resourceBasePointer + "'");
                st.executeUpdate("DELETE from base_resourcebase WHERE \"id\"='" + resourceBasePointer + "'");
                // st.executeUpdate("DROP TABLE IF EXISTS " + table);
            }

            st.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addLayer(String table, String aggregation) {
        try {

            Statement st = metadataConnection.createStatement();

            // String resourceBaseResultSet resultSet = st.executeQuery("SELECT resourcebase_ptr_id from layers_layer
            // where name='" + table + "'");
            //
            // String columns = "";
            // while (resultSet.next()) {
            // String column = resultSet.getString(1);
            // if (!column.equals(LCML_CODE_COLUMN)) {
            //
            // }
            // }
            ResultSet resultSet;

            resultSet = st.executeQuery("SELECT resourcebase_ptr_id from layers_layer WHERE name='" + table + "'");
            resultSet.next();
            String resourceBasePointer = resultSet.getString(1);

            String newResourceBasePointer = "" + Math.abs((resourceBasePointer + aggregation).hashCode());

            // create a new line in resource base
            st.executeUpdate("DELETE from layers_attribute WHERE \"layer_id\"='" + newResourceBasePointer + "'");
            st.executeUpdate("DELETE from layers_layer WHERE \"name\"='" + aggregation + "'");
            st.executeUpdate("DELETE from layers_layer WHERE \"resourcebase_ptr_id\"='" + newResourceBasePointer + "'");
            st.executeUpdate("DELETE from base_link WHERE \"resource_id\"='" + newResourceBasePointer + "'");
            st.executeUpdate("DELETE from base_resourcebase WHERE \"id\"='" + newResourceBasePointer + "'");

            resultSet = st
                    .executeQuery("SELECT bbox_x0,bbox_x1,bbox_y0,bbox_y1 from base_resourcebase WHERE id='" + resourceBasePointer + "'");
            resultSet.next();
            String x0 = resultSet.getString(1);
            String x1 = resultSet.getString(2);
            String y0 = resultSet.getString(3);
            String y1 = resultSet.getString(4);

            double w = Double.parseDouble(x0);
            double e = Double.parseDouble(x1);
            double s = Double.parseDouble(y0);
            double n = Double.parseDouble(y1);
            double xExtent = e - w;
            double yExtent = n - s;

            double xyRatio = xExtent / yExtent;

            if (xExtent > yExtent) {
                xExtent = 400;
                yExtent = xExtent / xyRatio;
            } else {
                yExtent = 400;
                xExtent = yExtent * xyRatio;
            }

//	    String thumbnailUrl = hostname + "/geoserver/geonode/wms?service=WMS&version=1.1.0&request=GetMap&layers=geonode:"
            String thumbnailUrl = geoserverurl + "/geonode/wms?service=WMS&version=1.1.0&request=GetMap&layers=geonode:"
                    + aggregation.toLowerCase() + "&styles=&bbox=" + x0 + "," + y0 + "," + x1 + "," + y1 + "&width=" + ((int) xExtent)
                    + "&height=" + ((int) yExtent) + "&srs=EPSG:4326&format=image/png";

            List<String> exceptionColumns = new ArrayList<String>();
            List<String> exceptionValues = new ArrayList<String>();

            exceptionColumns = new ArrayList<String>();
            exceptionValues = new ArrayList<String>();

            exceptionColumns.add("id");
            exceptionValues.add(newResourceBasePointer);

            exceptionColumns.add("detail_url");
            exceptionValues.add("/layers/geonode:" + aggregation.toLowerCase());

            exceptionColumns.add("thumbnail_url");
            exceptionValues.add(thumbnailUrl);

            copyRowsInTable(st, "base_resourcebase", "id", resourceBasePointer, exceptionColumns, exceptionValues);

            // exceptionColumns = new ArrayList<String>();
            // exceptionValues = new ArrayList<String>();
            //
            // exceptionColumns.add("id");
            // exceptionValues.add(null);
            //
            // exceptionColumns.add("resource_id");
            // exceptionValues.add(newResourceBasePointer);
            // copyRowsInTable(st, "base_link", "resource_id", resourceBasePointer, exceptionColumns, exceptionValues);
            List<String> whereColumns;
            List<String> whereValues;

            // LEGEND
            whereColumns = new ArrayList<String>();
            whereValues = new ArrayList<String>();

            exceptionColumns = new ArrayList<String>();
            exceptionValues = new ArrayList<String>();

            whereColumns.add("resource_id");
            whereValues.add(resourceBasePointer);

            whereColumns.add("name");
            whereValues.add("Legend");

            exceptionColumns.add("id");
            exceptionValues.add(null);

            exceptionColumns.add("resource_id");
            exceptionValues.add(newResourceBasePointer);

            exceptionColumns.add("url");
//	    exceptionValues.add(hostname + "/geoserver/wms?request=GetLegendGraphic&format=image/png&WIDTH=20&HEIGHT=20&LAYER=geonode:"
            exceptionValues.add(geoserverurl + "/wms?request=GetLegendGraphic&format=image/png&WIDTH=20&HEIGHT=20&LAYER=geonode:"
                    + aggregation.toLowerCase() + "&legend_options=fontAntiAliasing:true;fontSize:12;forceLabels:on");

            copyRowsInTable(st, "base_link", whereColumns, whereValues, exceptionColumns, exceptionValues);

            // html
            whereColumns = new ArrayList<String>();
            whereValues = new ArrayList<String>();

            exceptionColumns = new ArrayList<String>();
            exceptionValues = new ArrayList<String>();

            whereColumns.add("resource_id");
            whereValues.add(resourceBasePointer);

            whereColumns.add("name");
            whereValues.add("geonode:" + table.toLowerCase());

            exceptionColumns.add("id");
            exceptionValues.add(null);

            exceptionColumns.add("resource_id");
            exceptionValues.add(newResourceBasePointer);

            exceptionColumns.add("url");
//	    exceptionValues.add(hostname + "/layers/geonode:" + aggregation.toLowerCase());
            exceptionValues.add(webserverurl + "/layers/geonode:" + aggregation.toLowerCase());

            exceptionColumns.add("name");
            exceptionValues.add("geonode:" + aggregation.toLowerCase());

            copyRowsInTable(st, "base_link", whereColumns, whereValues, exceptionColumns, exceptionValues);

            // thumbnail
            whereColumns = new ArrayList<String>();
            whereValues = new ArrayList<String>();

            exceptionColumns = new ArrayList<String>();
            exceptionValues = new ArrayList<String>();

            whereColumns.add("resource_id");
            whereValues.add(resourceBasePointer);

            whereColumns.add("name");
            whereValues.add("Thumbnail");

            exceptionColumns.add("id");
            exceptionValues.add(null);

            exceptionColumns.add("resource_id");
            exceptionValues.add(newResourceBasePointer);

            exceptionColumns.add("url");
            exceptionValues.add(thumbnailUrl);

            copyRowsInTable(st, "base_link", whereColumns, whereValues, exceptionColumns, exceptionValues);

            // whereColumns = new ArrayList<String>();
            // whereValues = new ArrayList<String>();
            //
            // whereColumns.add("resource_id");
            // whereValues.add(newResourceBasePointer);
            //
            // whereColumns.add("name");
            // whereValues.add("Legend");
            //
            // updateRowsInTable(st, "base_link", whereColumns, whereValues, "url",
            // geoserverUrl + "/wms?request=GetLegendGraphic&format=image/png&WIDTH=20&HEIGHT=20&LAYER=geonode:" +
            // aggregation
            // + "&legend_options=fontAntiAliasing:true;fontSize:12;forceLabels:on");
            exceptionColumns = new ArrayList<String>();
            exceptionValues = new ArrayList<String>();

            exceptionColumns.add("title_en");
            exceptionValues.add(aggregation);

            exceptionColumns.add("name");
            exceptionValues.add(aggregation);

            exceptionColumns.add("typename");
            exceptionValues.add("geonode:" + aggregation.toLowerCase());

            exceptionColumns.add("upload_session_id");
            exceptionValues.add(null);

            exceptionColumns.add("resourcebase_ptr_id");
            exceptionValues.add(newResourceBasePointer);

            copyRowsInTable(st, "layers_layer", "name", table, exceptionColumns, exceptionValues);

            exceptionColumns = new ArrayList<String>();
            exceptionValues = new ArrayList<String>();

            exceptionColumns.add("layer_id");
            exceptionValues.add(newResourceBasePointer);

            exceptionColumns.add("id");
            exceptionValues.add(null);

            copyRowsInTable(st, "layers_attribute", "layer_id", resourceBasePointer, exceptionColumns, exceptionValues);

            // permissions
            exceptionColumns = new ArrayList<String>();
            exceptionValues = new ArrayList<String>();

            exceptionColumns.add("object_pk");
            exceptionValues.add(newResourceBasePointer);

            exceptionColumns.add("id");
            exceptionValues.add(null);

            copyRowsInTable(st, "guardian_userobjectpermission", "object_pk", resourceBasePointer, exceptionColumns, exceptionValues);

            st.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateLayer(String table) {
        try {

            // geonode tables are always lowercase
            table = table.toLowerCase();

            Statement st = metadataConnection.createStatement();

            ResultSet resultSet;

            resultSet = st.executeQuery("SELECT resourcebase_ptr_id from layers_layer WHERE name='" + table + "'");
            resultSet.next();
            String resourceBasePointer = resultSet.getString(1);

            // create a new line in resource base
            resultSet = st
                    .executeQuery("SELECT bbox_x0,bbox_x1,bbox_y0,bbox_y1 from base_resourcebase WHERE id='" + resourceBasePointer + "'");
            resultSet.next();
            String x0 = resultSet.getString(1);
            String x1 = resultSet.getString(2);
            String y0 = resultSet.getString(3);
            String y1 = resultSet.getString(4);

            double w = Double.parseDouble(x0);
            double e = Double.parseDouble(x1);
            double s = Double.parseDouble(y0);
            double n = Double.parseDouble(y1);
            double xExtent = e - w;
            double yExtent = n - s;

            double xyRatio = xExtent / yExtent;

            if (xExtent > yExtent) {
                xExtent = 400;
                yExtent = xExtent / xyRatio;
            } else {
                yExtent = 400;
                xExtent = yExtent * xyRatio;
            }

//	    String thumbnailUrl = hostname + "/geoserver/geonode/wms?service=WMS&version=1.1.0&request=GetMap&layers=geonode:" + table
            String thumbnailUrl = geoserverurl + "/geonode/wms?service=WMS&version=1.1.0&request=GetMap&layers=geonode:" + table
                    + "&styles=&bbox=" + x0 + "," + y0 + "," + x1 + "," + y1 + "&width=" + ((int) xExtent) + "&height=" + ((int) yExtent)
                    + "&srs=EPSG:4326&format=image/png";

            List<String> exceptionColumns = new ArrayList<String>();
            List<String> exceptionValues = new ArrayList<String>();

            exceptionColumns = new ArrayList<String>();
            exceptionValues = new ArrayList<String>();

            exceptionColumns.add("detail_url");
            exceptionValues.add("/layers/geonode:" + table);

            exceptionColumns.add("thumbnail_url");
            exceptionValues.add(thumbnailUrl);

            List<String> whereColumns = Arrays.asList(new String[]{"id"});
            List<String> whereValues = Arrays.asList(new String[]{resourceBasePointer});
            ;
            // updateRowsInTable(st, "base_resourcebase", whereColumns, whereValues, "detail_url", "/layers/geonode:" +
            // table);
            updateRowsInTable(st, "base_resourcebase", whereColumns, whereValues, "thumbnail_url", thumbnailUrl);

            st.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void copyRowsInTable(Statement st, String table, String whereColumn, String whereValue, List<String> exceptionColumns,
            List<String> exceptionValues) throws SQLException {

        List<String> whereColumns = new ArrayList<String>();
        whereColumns.add(whereColumn);
        List<String> whereValues = new ArrayList<String>();
        whereValues.add(whereValue);
        copyRowsInTable(st, table, whereColumns, whereValues, exceptionColumns, exceptionValues);
    }

    private void copyRowsInTable(Statement st, String table, List<String> whereColumns, List<String> whereValues,
            List<String> exceptionColumns, List<String> exceptionValues) throws SQLException {

        ResultSet resultSet = st.executeQuery("SELECT column_name from information_schema.columns WHERE table_name='" + table + "'");

        List<String> initialColumns = new ArrayList<String>();

        while (resultSet.next()) {
            String column = resultSet.getString(1);
            initialColumns.add(column);
        }

        String otherClauses = "";
        for (int i = 1; i < whereColumns.size(); i++) {
            otherClauses += " AND \"" + whereColumns.get(i) + "\"='" + whereValues.get(i) + "'";
        }
        resultSet = st.executeQuery(
                "SELECT * from " + table + " WHERE \"" + whereColumns.get(0) + "\"='" + whereValues.get(0) + "'" + otherClauses);

        Statement insertStatement = metadataConnection.createStatement();

        while (resultSet.next()) {

            Iterator<String> columnNamesIterator = initialColumns.iterator();

            List<String> theColumns = new ArrayList<String>();
            List<String> theValues = new ArrayList<String>();

            int columnCount = resultSet.getMetaData().getColumnCount();
            for (int i = 1; i < columnCount + 1; i++) {
                String columnName = columnNamesIterator.next();
                String columnValue = resultSet.getString(i);
                boolean exception = false;
                for (int j = 0; j < exceptionColumns.size(); j++) {
                    String exceptionColumn = exceptionColumns.get(j);
                    String exceptionValue = exceptionValues.get(j);
                    if (columnName.equals(exceptionColumn)) {
                        if (exceptionValue != null) {
                            theColumns.add(columnName);
                            theValues.add(exceptionValue);
                        }
                        exception = true;
                        break;
                    }
                }
                if (!exception) {
                    if (columnValue != null) {
                        theColumns.add(columnName);
                        theValues.add(columnValue);
                    }
                }
            }

            String columns = getColumns(theColumns);
            String values = getValues(theValues);

            String update = "INSERT INTO " + table + " (" + columns + ") VALUES (" + values + ")";
            System.out.println(update);
            insertStatement.executeUpdate(update);
            System.out.println("Updated.");
        }

        insertStatement.close();
    }

    private void updateRowsInTable(Statement st, String table, List<String> whereColumns, List<String> whereValues, String columnName,
            String columnValue) throws SQLException {

        ResultSet resultSet = st.executeQuery("SELECT column_name from information_schema.columns WHERE table_name='" + table + "'");

        List<String> initialColumns = new ArrayList<String>();

        while (resultSet.next()) {
            String column = resultSet.getString(1);
            initialColumns.add(column);
        }

        String otherClauses = "";
        for (int i = 1; i < whereColumns.size(); i++) {
            otherClauses += " AND \"" + whereColumns.get(i) + "\"='" + whereValues.get(i) + "'";
        }

        String update = "UPDATE " + table + " SET \"" + columnName + "\"='" + columnValue + "' WHERE \"" + whereColumns.get(0) + "\"='"
                + whereValues.get(0) + "'" + otherClauses;
        System.out.println(update);
        st.executeUpdate(update);
        System.out.println("Updated.");
    }

    private String getColumns(List<String> theColumns) {
        String columns = "";
        for (String column : theColumns) {
            columns += "\"" + column + "\",";
        }
        columns = columns.substring(0, columns.length() - 1);
        return columns;

    }

    private String getValues(List<String> theValues) {
        String columns = "";
        for (String column : theValues) {
            String v;
            if (column == null) {
                v = null;
            } else {
                v = column.replace("'", "''");
            }
            columns += "'" + v + "',";
        }
        columns = columns.substring(0, columns.length() - 1);
        return columns;

    }

    /////////////////////////////
    // CLASSIFICATIONS
    ///////////////////////////
    public void createClassificationTable() {
        try {
            Statement st = dataConnection.createStatement();
            st.executeUpdate("DROP TABLE IF EXISTS " + CLASSIFICATION_TABLE);
            st.executeUpdate("CREATE TABLE " + CLASSIFICATION_TABLE + " ("
                    + //
                    LAYER_COLUMN + " varchar(256)," + LCCS_ATTRIBUTE_COLUMN + " varchar(256),"
                    + //
                    LCCS_LEGEND_COLUMN + " varchar(256)"//
                    + ")");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Set<String> getClassifications() {

        Statement st;
        try {
            st = dataConnection.createStatement();

            ResultSet resultSet = st.executeQuery("select " + LAYER_COLUMN + " from " + CLASSIFICATION_TABLE + ";");

            Set<String> ret = new HashSet<String>();
            while (resultSet.next()) {
                String classification = resultSet.getString(1);
                if (classification != null) {
                    ret.add(classification);
                }
            }

            st.close();

            return ret;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void removeClassification(String classification) throws SQLException {
        Statement st = dataConnection.createStatement();

        st.executeUpdate("DELETE from " + CLASSIFICATION_TABLE + " WHERE \"" + LAYER_COLUMN + "\"='" + classification + "';");

    }

    public void addClassification(String layer, String column, String legend) {
        Statement st;
        try {
            st = dataConnection.createStatement();

            String values = "('" + layer + "','" + column + "','" + legend + "')";

            st.executeUpdate("INSERT INTO " + CLASSIFICATION_TABLE + " (\"" + LAYER_COLUMN + "\",\"" + LCCS_ATTRIBUTE_COLUMN + "\",\""
                    + LCCS_LEGEND_COLUMN + "\") VALUES " + values);

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public SimpleEntry<String, String> getClassification(String layer) {

        Statement st;
        try {
            st = dataConnection.createStatement();

            ResultSet resultSet = st
                    .executeQuery("select * from " + CLASSIFICATION_TABLE + " WHERE \"" + LAYER_COLUMN + "\"='" + layer + "';");

            while (resultSet.next()) {
                String attribute = resultSet.getString(2);
                String legend = resultSet.getString(3);
                return new SimpleEntry<String, String>(attribute, legend);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public String findLCCSColumn(String layer, List<String> classes) throws Exception {

        layer = layer.toLowerCase(); // because GeoDash creates underlying table using lower cases only

        String lccsColumn = null;

        try {
            Statement st = dataConnection.createStatement();

            ResultSet resultSet;

            resultSet = st.executeQuery("select column_name from information_schema.columns WHERE table_name='" + layer + "'");

            List<String> attributes = new ArrayList<>();

            while (resultSet.next()) {
                int count = resultSet.getMetaData().getColumnCount();
                for (int i = 0; i < count; i++) {
                    String res = resultSet.getString(i + 1);
                    if (res != null) {
                        attributes.add(res);
                    }
                }

            }

            if (!attributes.contains(LCML_CODE_COLUMN)) {
                throw new Exception("Unable to find attribute " + LCML_CODE_COLUMN + " in layer " + layer);
            }

            return LCML_CODE_COLUMN;

            // THIS CODE USED TO FIND FOR A COLUMN THAT CONTAINED THE LCML MAP_CODE CLASSES
            // nextAttribute: for (String attribute : attributes) {
            //
            // resultSet = st.executeQuery("select distinct " + layer + ".\"" + attribute + "\" from " + layer);
            //
            // while (resultSet.next()) {
            // int count = resultSet.getMetaData().getColumnCount();
            // for (int i = 0; i < count; i++) {
            // String clazz = resultSet.getString(i + 1);
            // if (!classes.contains(clazz)) {
            // continue nextAttribute;
            // } else {
            // lccsColumn = attribute;
            // }
            // }
            // if (count > 0) {
            // return attribute;
            // }
            //
            // }
            // }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return lccsColumn;
    }

}
