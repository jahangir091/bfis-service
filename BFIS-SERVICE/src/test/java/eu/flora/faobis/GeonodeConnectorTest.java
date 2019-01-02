//package eu.flora.faobis;
//
//import java.sql.SQLException;
//import java.util.Arrays;
//import java.util.List;
//import java.util.Set;
//
//import org.junit.Before;
//import org.junit.Test;
//
//import it.geosolutions.geoserver.rest.encoder.GSLayerEncoder;
//import it.geosolutions.geoserver.rest.encoder.GSResourceEncoder;
//import it.geosolutions.geoserver.rest.encoder.feature.GSFeatureTypeEncoder;
//
//public class GeonodeConnectorTest {
//
//    @Before
//    public void init() throws SQLException {
//	String url = "jdbc:postgresql://149.139.19.111:5432/";
//	String user = "geodash_dev";
//	String password = "geodash_dev";//"";
//	String metadataDb = "geodash_dev_backup";
//	String dataDb = "geodash_dev_backup-imports";
//	GeonodeConnector.getInstance().initConnection(url, user, password, metadataDb, dataDb, "http://149.139.19.111");
//	GeoserverConnector.getInstance().init("http://149.139.19.111:8080/geoserver", "admin", "geoserver");
//    }
//
//    @Test
//    public void testLayers() throws Exception {
//	Set<String> layers = GeonodeConnector.getInstance().getLayers();
//	String table = "srdi_2004";
//	System.out.println("Found " + table);
//
//	// FD 2015
//	// List<String> originalCodes = Arrays
//	// .asList(new String[] { "PCm", "PCs", "FWa", "RS", "L", "Po", "Br", "FDp", "BNl", "OT", "BH", "FP", "R" });
//	// List<String> translatedCodes = Arrays.asList(new String[] { "A", "A", "A", "A", "A", "A", "B", "B", "B", "B",
//	// "B", "B", "B" });
//
//	// srdi_2004
//	List<String> originalCodes = Arrays.asList(new String[] { "21", "16", "13", "9" });
//	List<String> translatedCodes = Arrays.asList(new String[] { "A", "A", "B", "B" });
//
//	String agg = "agg1";
//
//	GeonodeConnector.getInstance().createAggregationTable("LCCS_code", table, agg, originalCodes, translatedCodes);
//
//	GeonodeConnector.getInstance().addLayer(table, agg);
//
//	List<String> geoserverLayers = GeoserverConnector.getInstance().getManager().getReader().getLayers().getNames();
//	for (String geoserverLayer : geoserverLayers) {
//	    System.out.println(geoserverLayer);
//	}
//
//	String workspace = "geonode";
//	String datastore = "datastore";
//	GeoserverConnector.getInstance().getManager().getPublisher().unpublishFeatureType(workspace, datastore, agg);
//	GeoserverConnector.getInstance().getManager().getStyleManager().removeStyleInWorkspace(workspace,
//		agg + GeoserverConnector.STYLE_SUFFIX);
//
//	String styleDocument = GeonodeConnector.getInstance().createStyle(agg, "LCCS_code");
//	GeoserverConnector.getInstance().getManager().getStyleManager().publishStyleInWorkspace(workspace, styleDocument,
//		agg + GeoserverConnector.STYLE_SUFFIX);
//	GSFeatureTypeEncoder fte = new GSFeatureTypeEncoder();
//	fte.setProjectionPolicy(GSResourceEncoder.ProjectionPolicy.REPROJECT_TO_DECLARED);
//	fte.addKeyword(agg);
//	fte.setTitle(agg);
//	fte.setName(agg);
//	fte.setSRS("EPSG:4326");
//	final GSLayerEncoder layerEncoder = new GSLayerEncoder();
//	layerEncoder.setDefaultStyle(agg + GeoserverConnector.STYLE_SUFFIX);
//	GeoserverConnector.getInstance().getManager().getPublisher().publishDBLayer(workspace, datastore, fte, layerEncoder);
//    }
//
//}
