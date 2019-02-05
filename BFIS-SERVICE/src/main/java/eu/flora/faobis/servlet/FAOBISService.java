package eu.flora.faobis.servlet;

import eu.flora.essi.lib.xml.XMLDocumentReader;
import eu.flora.essi.lib.xml.XMLDocumentReader.NodeResult;
import eu.flora.essi.lib.xml.XMLDocumentWriter;
import eu.flora.faobis.ExistConnector;
import eu.flora.faobis.FAOBISResponse;
import eu.flora.faobis.FAOBISResponse.Status;
import eu.flora.faobis.FullPrototypeQuery;
import eu.flora.faobis.GeonodeConnector;
import eu.flora.faobis.GeoserverConnector;
import eu.flora.faobis.LCMLClass;
import eu.flora.faobis.PythonConnector;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.io.IOException;
import java.io.InputStream;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.HttpClientBuilder;
import org.xmldb.api.base.Resource;
import eu.flora.faobis.PropertyReader;

@Api
@Path("/")
public class FAOBISService {

    // /**
    // * Basic Element list
    // */
    // @GET
    // @Produces({ MediaType.APPLICATION_JSON })
    // @ApiOperation(value = "Example", notes = "example")
    // @Path("example")
    // public Response getExample() {
    // System.out.println("Example.");
    //
    // try {
    // SemanticFilter test = new SemanticFilter();
    // Options codes1 = new Options();
    // codes1.add("code1");
    // codes1.add("code2");
    // test.getAdvancedOptions().put("originalCodes", codes1);
    // Options codes2 = new Options();
    // codes2.add("codeA");
    // codes2.add("codeA");
    // test.getAdvancedOptions().put("translatedCodes", codes2);
    // return Response.status(Response.Status.OK).entity(test).build();
    // } catch (Exception e) {
    // FAOBISResponse entity = new FAOBISResponse(Status.ERROR, "Error during test");
    // return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(entity).build();
    // }
    //
    // }

    //////////////////////////////////////////////////
    // SCHEMA
    //////////////////////////////////////////////////
    
    private PropertyReader propertyReader = PropertyReader.getInstance();

    /**
     * Basic Element list
     */
    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Basic element list", notes = "Retrieves the list of valid basic elements which are defined in the LCML xsd definition. This service can be used to get and filter the basic elements that can be used to build a template for querying the LCML database. The service can be configured to report back the elements that can be used as root nodes only or that partially match a given name (i.e. “Herbaceous). Outputs a list of possible elements, reporting the element name and the eligible_as_root flag")
    @Path("schema-basic-elements")
    public Response getBasicElementList(
	    @ApiParam(value = "String for a partial match (i.e. tree matches LC_Trees, Herbaceous matches LC_WoodyGrowthForms, LC_WoodyGrowthLeafPhenology, etc.).", required = false) @QueryParam("name") String name, //
	    @ApiParam(value = "Boolean flag for reporting back elements that can be used as root nodes only", required = false) @QueryParam("eligibleAsRoot") Boolean eligibleAsRoot//
    ) {
	System.out.println("Getting basic element list from the db.");

	try {
	    String str = "";

	    HttpClient client = HttpClientBuilder.create().build();
	    HttpGet get = new HttpGet(PythonConnector.getInstance().getListBasicElementsPath(name, eligibleAsRoot));
	    HttpClientContext context = HttpClientContext.create();

	    try {
		HttpResponse response = client.execute(get, context);
		InputStream content = response.getEntity().getContent();
		str = IOUtils.toString(content);
	    } catch (ClientProtocolException e) {
		e.printStackTrace();
	    } catch (IOException e) {
		e.printStackTrace();
	    }

	    return Response.status(Response.Status.OK).entity(str).build();
	} catch (Exception e) {
	    FAOBISResponse entity = new FAOBISResponse(Status.ERROR, "Error during basic element list");
	    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(entity).build();
	}

    }

    /**
     * Basic Element schema
     */
    @GET
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @ApiOperation(value = "Basic element schema", notes = "Retrieves the schema (including attributes) of a basic element, as defined in the LCML xsd definition. It can be used to build a template for querying the LCML database. Outputs A list of possible fields and their types, along with attributes, whenever present. The schema syntax is inspired by the cerberus library by Nicola Iarocci (http://docs.python-cerberus.org/en/stable/). Please refer to the “validation rules? section for a list of possible field related properties, in addition to name and type.")
    @Path("schema-basic-element")
    public Response getBasicElementSchema(
	    @ApiParam(value = "(String) exact element name, e.g. LC_Stratum", required = true) @QueryParam("name") String name //

    ) {
	System.out.println("Getting basic element schema from the db.");

	try {
	    String str = "";

	    HttpClient client = HttpClientBuilder.create().build();
	    HttpGet get = new HttpGet(PythonConnector.getInstance().getBasicElementSchemaPath(name));
	    HttpClientContext context = HttpClientContext.create();

	    try {
		HttpResponse response = client.execute(get, context);
		InputStream content = response.getEntity().getContent();
		str = IOUtils.toString(content);
	    } catch (ClientProtocolException e) {
		e.printStackTrace();
	    } catch (IOException e) {
		e.printStackTrace();
	    }

	    return Response.status(Response.Status.OK).entity(str).build();
	} catch (Exception e) {
	    FAOBISResponse entity = new FAOBISResponse(Status.ERROR, "Error during basic element list");
	    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(entity).build();
	}

    }

    /**
     * Derived elements list
     */
    @GET
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @ApiOperation(value = "Derived element list", notes = "Shows all the (instantiable) elements of a basic element. ")
    @Path("schema-derived-elements")
    public Response getDerivedClassesList(
	    @ApiParam(value = "(String) exact element name, e.g. LC_LandCoverElement", required = true) @QueryParam("name") String name //

    ) {
	System.out.println("Derived elements list.");

	try {
	    String str = "";

	    HttpClient client = HttpClientBuilder.create().build();
	    HttpGet get = new HttpGet(PythonConnector.getInstance().getDerivedClassesListPath(name));
	    HttpClientContext context = HttpClientContext.create();

	    try {
		HttpResponse response = client.execute(get, context);
		InputStream content = response.getEntity().getContent();
		str = IOUtils.toString(content);
	    } catch (ClientProtocolException e) {
		e.printStackTrace();
	    } catch (IOException e) {
		e.printStackTrace();
	    }

	    return Response.status(Response.Status.OK).entity(str).build();
	} catch (Exception e) {
	    FAOBISResponse entity = new FAOBISResponse(Status.ERROR, "Error during basic element list");
	    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(entity).build();
	}

    }

    //////////////////////////////////////////////////
    // LEGENDS
    //////////////////////////////////////////////////

    /**
     * Lists the legends
     */
    @GET
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @ApiOperation(value = "List legends", notes = "Lists the available legends.")
    @Path("legends")
    public Response getLegends() {
	System.out.println("Getting available legends from the db.");

	try {
	    Set<String> ret = ExistConnector.getInstance().getLegends();
	    FAOBISResponse entity = new FAOBISResponse(Status.OK, "Legends retrieved.");
	    entity.getResults().addAll(ret);
	    return Response.status(Response.Status.OK).entity(entity).build();
	} catch (Throwable e) {
	    FAOBISResponse entity = new FAOBISResponse(Status.ERROR, "Error getting the legends");
	    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(entity).build();
	}

    }

    /**
     * Gets a legend by a specific legend name
     */
    @GET
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @ApiOperation(value = "Get legend", notes = "Gets the legend identified by the given name.")
    @Path("legends/{legend}")
    public Response getLegend(@ApiParam(value = "The identifier of the legend.", required = true) @PathParam("legend") String legend) {
	System.out.println("Getting the legend from the db.");

	try {
	    Set<String> ret = ExistConnector.getInstance().getLegend(legend);
	    FAOBISResponse entity = new FAOBISResponse(Status.OK, "Legend retrieved.");
	    entity.getResults().addAll(ret);
	    return Response.status(Response.Status.OK).entity(entity).build();
	} catch (Exception e) {
	    FAOBISResponse entity = new FAOBISResponse(Status.ERROR, "Error getting the legend description: " + e.getMessage());
	    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(entity).build();
	}

    }

    /**
     * Deletes a legend with the given legend id
     */
    @DELETE
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @ApiOperation(value = "Delete legend", notes = "Deletes the legend identified by the given id.")
    @Path("legends/{legend}")
    public Response deleteLegend(@ApiParam(value = "The identifier of the legend.", required = true) @PathParam("legend") String legendId) {
	System.out.println("Deletes the legend from the db.");

	try {
	    ExistConnector.getInstance().deleteLegend(legendId);
	    FAOBISResponse entity = new FAOBISResponse(Status.OK, "Legend deleted: " + legendId);
	    return Response.status(Response.Status.OK).entity(entity).build();
	} catch (Exception e) {
	    FAOBISResponse entity = new FAOBISResponse(Status.ERROR, "Error deleting the legend description: " + e.getMessage());
	    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(entity).build();
	}

    }

    /**
     * Puts a legend with the given legend id
     */
    @PUT
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @ApiOperation(value = "Put legend", notes = "Creates the legend identified by the given name.")
    @Path("legends/{legend}")
    public Response putLegend(
	    @ApiParam(value = "The identifier (@id attribute) of the legend.", required = true) @PathParam("legend") String legend, //
	    @ApiParam(value = "The XML (LCML) representation of the legend.", required = true) XmlRequest lcml, //
	    @ApiParam(value = "The identifier of the layer that this legend is used to classify. If uploading a query legend, this is not needed.", required = false) @QueryParam("layer") String layer //
    ) {
	System.out.println("Inserting the legend in the db: " + legend);
	try {
	    ExistConnector.getInstance().putLegend(legend, lcml.getXml());
	    System.out.println("Inserted the legend in the db.");
	    if (layer != null) {
		List<String> classes = ExistConnector.getInstance().getClassesInLegend(legend);
		// String column = GeonodeConnector.getInstance().findLCCSColumn(layer, classes);
		// if (column == null) {
		// FAOBISResponse entity = new FAOBISResponse(Status.ERROR, "Unable to identify the LCML attribute
		// (column) of layer "
		// + layer + " containing the following legend classes: " + classes.toString());
		// return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(entity).build();
		// }
		// System.out.println("LCCS column identified as: " + column);
		GeoserverConnector.getInstance().publishLayer(layer, propertyReader.getLCML_CODE_COLUMN());
		GeonodeConnector.getInstance().updateLayer(layer);
		GeonodeConnector.getInstance().addClassification(layer, propertyReader.getLCML_CODE_COLUMN(), legend);

	    }
	    FAOBISResponse entity = new FAOBISResponse(Status.OK, "Legend created: " + legend);
	    return Response.status(Response.Status.OK).entity(entity).build();
        } catch (Exception e) {
            e.printStackTrace();
	    FAOBISResponse entity = new FAOBISResponse(Status.ERROR, "Error creating the legend description: " + e.getMessage());
	    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(entity).build();
	}

    }

    //////////////////////////////////////////////////
    // CLASSES
    //////////////////////////////////////////////////

    /**
     * Lists the classes
     */
    @GET
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @ApiOperation(value = "List classes", notes = "Lists the available classes.")
    @Path("classes")
    public Response getClasses() {
	System.out.println("Getting available classes from the db.");

	try {
	    Set<String> ret = ExistConnector.getInstance().getClasses();
	    FAOBISResponse entity = new FAOBISResponse(Status.OK, "Classes retrieved.");
	    entity.getResults().addAll(ret);
	    return Response.status(Response.Status.OK).entity(entity).build();
	} catch (Throwable e) {
	    FAOBISResponse entity = new FAOBISResponse(Status.ERROR, "Error getting the classes");
	    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(entity).build();
	}

    }

    /**
     * Deletes a class with the given class id
     */
    @DELETE
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @ApiOperation(value = "Delete class", notes = "Deletes the class identified by the given id.")
    @Path("classes/{class}")
    public Response deleteClass(@ApiParam(value = "The identifier of the class.", required = true) @PathParam("class") String classId) {
	System.out.println("Deletes the class from the db.");

	try {
	    ExistConnector.getInstance().deleteClass(classId);
	    FAOBISResponse entity = new FAOBISResponse(Status.OK, "Class deleted: " + classId);
	    return Response.status(Response.Status.OK).entity(entity).build();
	} catch (Exception e) {
	    FAOBISResponse entity = new FAOBISResponse(Status.ERROR, "Error deleting the class description: " + e.getMessage());
	    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(entity).build();
	}

    }

    /**
     * Gets a class LCML description by a specific LCML class code
     */
    @GET
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @ApiOperation(value = "Get class", notes = "Gets the class identified by the given LCML class code.")
    @Path("classes/{class}")
    public Response getClasses(@ApiParam(value = "The identifier of the class.", required = true) @PathParam("class") String clazz) {
	System.out.println("Getting the class from the db.");

	try {
	    Set<String> ret = ExistConnector.getInstance().getClass(clazz);
	    FAOBISResponse entity = new FAOBISResponse(Status.OK, "Class retrieved.");
	    entity.getResults().addAll(ret);
	    return Response.status(Response.Status.OK).entity(entity).build();
	} catch (Exception e) {
	    FAOBISResponse entity = new FAOBISResponse(Status.ERROR, "Error getting the class description: " + e.getMessage());
	    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(entity).build();
	}

    }

    /**
     * Puts a class with the given class code
     */
    @PUT
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @ApiOperation(value = "Put class", notes = "Creates the class identified by the given class code.")
    @Path("classes/{class}")
    public Response putClass(@ApiParam(value = "The class code.", required = true) @PathParam("class") String clazz, //
	    @ApiParam(value = "The XML (LCML) representation of the class.", required = true) XmlRequest lcml) {
	System.out.println("Puts the class in the db.");

	try {
	    ExistConnector.getInstance().putClass(clazz, lcml.getXml());
	    FAOBISResponse entity = new FAOBISResponse(Status.OK, "Class created: " + clazz);
	    return Response.status(Response.Status.OK).entity(entity).build();
	} catch (Exception e) {
	    FAOBISResponse entity = new FAOBISResponse(Status.ERROR, "Error creating the class: " + e.getMessage());
	    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(entity).build();
	}

    }

    /**
     * Lists the classes referenced by a specific layer
     */
    @GET
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @ApiOperation(value = "List classes", notes = "Lists the classes referenced by a specific layer or a subset of its features.")
    @Path("layers/{layer}/classes")
    public Response getClasses(
	    @ApiParam(value = "The identifier of the layer referencing the classes.", required = true) @PathParam("layer") String layer, //
	    @ApiParam(value = "A comma separated list of features", required = false) @QueryParam("features") List<String> features//
    ) {
	System.out.println("Getting available classes from the db.");

	try {

	    SimpleEntry<String, String> classification = GeonodeConnector.getInstance().getClassification(layer);

	    if (classification == null) {
		FAOBISResponse entity = new FAOBISResponse(Status.INVALID_ARGUMENT, "No classified layer found with identifier: " + layer);
		return Response.status(Response.Status.BAD_REQUEST).entity(entity).build();
	    }

	    String lcmlAttribute = classification.getKey();

	    Set<String> ret = GeonodeConnector.getInstance().getClasses(layer, lcmlAttribute, features);
	    FAOBISResponse entity = new FAOBISResponse(Status.OK, "Classes retrieved.");
	    entity.getResults().addAll(ret);
	    return Response.status(Response.Status.OK).entity(entity).build();
	} catch (Exception e) {
	    FAOBISResponse entity = new FAOBISResponse(Status.ERROR, "Error getting the classes");
	    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(entity).build();
	}

    }

    /**
     * Lists the classes referenced by a specific layer
     */
    @GET
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @ApiOperation(value = "Gets the legend of a given layer", notes = "Gets the legend used to classify a specific layer.")
    @Path("layers/{layer}/legend")
    public Response getLayerLegend(@ApiParam(value = "The identifier of the layer.", required = true) @PathParam("layer") String layer //

    ) {
	System.out.println("Getting the legend that classifies a layer.");

	try {

	    SimpleEntry<String, String> classification = GeonodeConnector.getInstance().getClassification(layer);

	    if (classification == null) {
		FAOBISResponse entity = new FAOBISResponse(Status.INVALID_ARGUMENT, "No classified layer found with identifier: " + layer);
		return Response.status(Response.Status.BAD_REQUEST).entity(entity).build();
	    }

	    String legend = classification.getValue();

	    FAOBISResponse entity = new FAOBISResponse(Status.OK, "Legend retrieved.");
	    entity.getResults().add(legend);
	    return Response.status(Response.Status.OK).entity(entity).build();
	} catch (Exception e) {
	    FAOBISResponse entity = new FAOBISResponse(Status.ERROR, "Error getting the legend");
	    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(entity).build();
	}

    }

    // /**
    // * Lists the available feature sets in the db
    // */
    // @GET
    // @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    // @ApiOperation(value = "List feature sets", notes = "Lists the available feature sets from the db")
    // @Path("feature-sets")
    // public Response getFeatureSets() {
    // System.out.println("Getting available feature sets from the db.");
    //
    // try {
    // Set<String> tables = PostGISConnector.getInstance().getTables();
    // FAOBISResponse entity = new FAOBISResponse(Status.OK, "Feature sets retrieved.");
    // entity.getResults().addAll(tables);
    // return Response.status(Response.Status.OK).entity(entity).build();
    // } catch (Exception e) {
    // FAOBISResponse entity = new FAOBISResponse(Status.ERROR, "Error getting the feature sets");
    // return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(entity).build();
    // }
    //
    // }
    //
    // /**
    // * Removes a specific feature set from the db
    // */
    // @DELETE
    // @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    // @ApiOperation(value = "Remove feature set", notes = "Removes a specific feature set from the db.")
    // @Path("feature-sets/{featureSet}")
    // public Response removeTable(
    // @ApiParam(value = "The name of the feature set to delete.", required = true) @PathParam("featureSet") String
    // featureSet) {
    // System.out.println("Removing feature set: " + featureSet);
    //
    // try {
    // PostGISConnector.getInstance().removeTable(featureSet);
    // } catch (Exception e) {
    // FAOBISResponse entity = new FAOBISResponse(Status.ERROR, "Error removing feature set: " + featureSet);
    // return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(entity).build();
    // }
    // FAOBISResponse entity = new FAOBISResponse(Status.OK, "Feature set removed: " + featureSet);
    // return Response.status(Response.Status.OK).entity(entity).build();
    // }

    // /**
    // * Lists the available aggregated feature sets in the PosGIS db
    // */
    // @GET
    // @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    // @ApiOperation(value = "List aggregated feature sets", notes = "Lists the aggregated feature sets from the db")
    // @Path("aggregated-feature-sets")
    // public Response getAggregations() {
    // System.out.println("Getting available aggregated feature sets from the db.");
    //
    // try {
    // Set<String> aggregations = PostGISConnector.getInstance().getAggregations();
    // FAOBISResponse entity = new FAOBISResponse(Status.OK, "Aggregated feature sets retrieved.");
    // entity.getResults().addAll(aggregations);
    // return Response.status(Response.Status.OK).entity(entity).build();
    // } catch (Exception e) {
    // FAOBISResponse entity = new FAOBISResponse(Status.ERROR, "Error getting the aggregated feature sets");
    // return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(entity).build();
    // }
    //
    // }

    // /**
    // * Removes a specific aggregated feature set in the PostGIS db
    // */
    // @DELETE
    // @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    // @ApiOperation(value = "Remove aggregated feature set", notes = "Removes a specific aggregated feature set from
    // the db.")
    // @Path("aggregated-feature-sets/{aggregatedFeatureSet}")
    // public Response removeAggregatedFeatureSet(
    // @ApiParam(value = "The name of the aggregated feature set to delete.", required = true)
    // @PathParam("aggregatedFeatureSet") String aggregatedFeatureSet) {
    // removeLayer(aggregatedFeatureSet);
    // System.out.println("Removing aggregated feature set: " + aggregatedFeatureSet);
    //
    // try {
    // PostGISConnector.getInstance().removeAggregation(aggregatedFeatureSet);
    // } catch (Exception e) {
    // FAOBISResponse entity = new FAOBISResponse(Status.ERROR, "Error removing aggregated feature set: " +
    // aggregatedFeatureSet);
    // return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(entity).build();
    // }
    //
    // FAOBISResponse entity = new FAOBISResponse(Status.OK, "Aggregated feature set removed: " + aggregatedFeatureSet);
    // return Response.status(Response.Status.OK).entity(entity).build();
    // }

    //////////////////////////////////////////////////
    // QUERIES
    //////////////////////////////////////////////////

    /**
     * Query the reference classes using a semantic filter
     */
    @POST
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @ApiOperation(value = "Select classes", notes = "Apply a semantic filter to the classes of a layer (full prototype compliant or similarity based). The code to be included is the map_code identifier of a previously created LCML class.")
    @Path("layers/{layer}/query-by-class")
    public Response queryByClass( //
	    SemanticFilter filter, //
	    @ApiParam(value = "The identifier of the layer containing the classes to be queried.", required = true) @PathParam("layer") String layer //
    ) {
	System.out.println("Applying the semantic filter.");

	if (filter == null) {
	    FAOBISResponse entity = new FAOBISResponse(Status.INVALID_ARGUMENT, "No filter provided.");
	    return Response.status(Response.Status.BAD_REQUEST).entity(entity).build();
	}

	String classCode = filter.getCode();

	if (classCode == null) {
	    FAOBISResponse entity = new FAOBISResponse(Status.INVALID_ARGUMENT, "A valid query class map_code should be provided.");
	    return Response.status(Response.Status.BAD_REQUEST).entity(entity).build();
	}

	Resource classResource = ExistConnector.getInstance().getClassResource(classCode);

	if (classResource == null) {
	    FAOBISResponse entity = new FAOBISResponse(Status.INVALID_ARGUMENT, "No query class found with map_code: " + classCode);
	    return Response.status(Response.Status.BAD_REQUEST).entity(entity).build();
	}

	SimpleEntry<String, String> classification = GeonodeConnector.getInstance().getClassification(layer);

	if (classification == null) {
	    FAOBISResponse entity = new FAOBISResponse(Status.INVALID_ARGUMENT, "No classified layer found with identifier: " + layer);
	    return Response.status(Response.Status.BAD_REQUEST).entity(entity).build();
	}

	String lcmlAttribute = classification.getKey();
	String legend = classification.getValue();

	Resource legendResource = ExistConnector.getInstance().getLegendResource(legend);

	if (legendResource == null) {
	    FAOBISResponse entity = new FAOBISResponse(Status.INVALID_ARGUMENT, "No legend found with identifier: " + legend);
	    return Response.status(Response.Status.BAD_REQUEST).entity(entity).build();
	}

	// String class

	// List<String> features = new ArrayList<>();
	// Set<String> ret = GeonodeConnector.getInstance().getClasses(layer, lcmlAttribute, features);

	try {
	    FAOBISResponse entity = new FAOBISResponse(Status.OK, "Classes retrieved.");
	    // if (!ret.isEmpty()) {

	    String legendString = legendResource.getContent().toString();
	    String queryClassString = classResource.getContent().toString();

	    InputStream legendStream = IOUtils.toInputStream(legendString);
	    InputStream clazz = IOUtils.toInputStream(queryClassString);

	    LCMLClass lcmlClazz = new LCMLClass(clazz);

	    FullPrototypeQuery ftq = new FullPrototypeQuery(legendStream);
	    List<LCMLClass> legendClasses = ftq.getClasses();
	    for (LCMLClass legendClass : legendClasses) {
		System.out.println("Legend class id " + legendClass.getId() + " map code: " + legendClass.getMapCode());
		boolean match = lcmlClazz.fullMatch(legendClass, true);
		System.out.println("Result: " + match);
		if (match) {
		    entity.getResults().add(legendClass.getMapCode());
		}
	    }

	    // }
	    return Response.status(Response.Status.OK).entity(entity).build();
	} catch (Exception e) {
	    FAOBISResponse entity = new FAOBISResponse(Status.ERROR, "Error getting the classes");
	    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(entity).build();
	}

    }

    /**
     * Query the classes using a semantic filter
     */
    @POST
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @ApiOperation(value = "Thematize layer", notes = "Apply a semantic aggregation to the given layer (full prototype compliant or similarity based). The code to be included is the identifier of a previously created LCML legend.")
    @Path("layers/{layer}/query-by-legend")
    public Response queryByLegendNoLayer( //
	    SemanticFilter filter, //
	    @ApiParam(value = "The identifier of the layer to thematize.", required = true) @PathParam("layer") String layer //
    ) {
	return queryByLegend(filter, layer, null);
    }

    /**
     * Query the classes using a semantic filter
     */
    @POST
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @ApiOperation(value = "Thematize layer", notes = "Apply a semantic aggregation to the given layer (full prototype compliant or similarity based). The code to be included is the identifier of a previously created LCML legend. Additionally, it creates a new aggregated layer")
    @Path("layers/{layer}/query-by-legend/{aggregatedLayer}")
    public Response queryByLegend( //
	    SemanticFilter filter, //
	    @ApiParam(value = "The identifier of the layer to thematize.", required = true) @PathParam("layer") String layer, //
	    @ApiParam(value = "The identifier of the aggregated layer to produce as a result.", required = false) @PathParam("aggregatedLayer") String aggregatedLayer //
    ) {
	System.out.println("Applying the thematization.");

	if (filter == null) {
	    FAOBISResponse entity = new FAOBISResponse(Status.INVALID_ARGUMENT, "No filter provided.");
	    return Response.status(Response.Status.BAD_REQUEST).entity(entity).build();
	}

	String queryLegendCode = filter.getCode();
	if (queryLegendCode == null) {
	    FAOBISResponse entity = new FAOBISResponse(Status.INVALID_ARGUMENT,
		    "No legend code provided. The identifier of a previously created legend is expected");
	    return Response.status(Response.Status.BAD_REQUEST).entity(entity).build();
	}

	Resource queryLegendResource = ExistConnector.getInstance().getLegendResource(queryLegendCode);

	if (queryLegendResource == null) {
	    FAOBISResponse entity = new FAOBISResponse(Status.INVALID_ARGUMENT,
		    "No query legend found with identifier: " + queryLegendCode);
	    return Response.status(Response.Status.BAD_REQUEST).entity(entity).build();
	}

	try {
	    SimpleEntry<String, String> classification = GeonodeConnector.getInstance().getClassification(layer);

	    if (classification == null) {
		FAOBISResponse entity = new FAOBISResponse(Status.INVALID_ARGUMENT,
			"No classified parent layer found with identifier: " + layer);
		return Response.status(Response.Status.BAD_REQUEST).entity(entity).build();
	    }

	    String lcmlAttribute = classification.getKey();
	    String legendCode = classification.getValue();

	    Resource sourceLegendResource = ExistConnector.getInstance().getLegendResource(legendCode);

	    if (sourceLegendResource == null) {
		FAOBISResponse entity = new FAOBISResponse(Status.INVALID_ARGUMENT,
			"No source legend found with identifier: " + legendCode);
		return Response.status(Response.Status.BAD_REQUEST).entity(entity).build();
	    }

	    XMLDocumentReader inLegendReader;
	    XMLDocumentReader outLegendReader;
	    XMLDocumentWriter outLegendWriter;
	    try {
		String sourceLegendString = sourceLegendResource.getContent().toString();
		InputStream sourceLegendStream = IOUtils.toInputStream(sourceLegendString);
		inLegendReader = new XMLDocumentReader(sourceLegendStream);

		String queryLegendString = queryLegendResource.getContent().toString();
		InputStream queryLegendStream = IOUtils.toInputStream(queryLegendString);
		outLegendReader = new XMLDocumentReader(queryLegendStream);
		outLegendWriter = new XMLDocumentWriter(outLegendReader);
	    } catch (Exception e1) {
		e1.printStackTrace();
		FAOBISResponse entity = new FAOBISResponse(Status.INVALID_ARGUMENT,
			"Error reading query legend with identifier: " + queryLegendCode);
		return Response.status(Response.Status.BAD_REQUEST).entity(entity).build();

	    }
	    List<String> listSourceLegendClasses = ExistConnector.getInstance().getClassesInLegend(legendCode);
	    HashSet<String> sourceLegendClasses = new HashSet<>(listSourceLegendClasses);

	    boolean layerCreation = true;
	    if (aggregatedLayer == null) {
		aggregatedLayer = layer + "fake";
		layerCreation = false;
	    }

	    // ADVANCED OPTIONS PARSING
	    Map<String, Options> options = filter.getAdvancedOptions();
	    Options fakeResponseOption = options.get("fake-response");
	    boolean fakeResponse = false;
	    if (fakeResponseOption != null && fakeResponseOption.getOptions().get(0).toString().equals("true")) {
		fakeResponse = true;
	    }
	    Options layerCreationOption = options.get("layer-creation");

	    if (layerCreationOption != null && layerCreationOption.getOptions().get(0).toString().equals("false")) {
		fakeResponse = false;
	    }

	    // END ADVANCED OPTIONS PARSING

	    HashMap<String, String> mapping = new HashMap<>();

	    String str;

	    String msg = "Aggregated layer (of " + layer + ") created: " + aggregatedLayer;

	    List<String> querylegendClasses = ExistConnector.getInstance().getClassesInLegend(queryLegendCode);

	    if (fakeResponse) {

		//////////////////// START FAKE RESPONSE

		String scores = "";



		for (int i = 0; i < listSourceLegendClasses.size(); i++) {

		    String sourceLegendClass = listSourceLegendClasses.get(i);

		    scores += "\"" + sourceLegendClass + "\": {";

		    String innerScores = "";
		    boolean added = false;
		    for (int queryClass = 0; queryClass < querylegendClasses.size(); queryClass++) {

			String queryLegendClass = querylegendClasses.get(queryClass);
			String score = "";

			if (i == queryClass) {
			    score = "90.0";
			    mapping.put(sourceLegendClass, queryLegendClass);
			    added = true;
			} else {
			    score = "30.0";
			}
			scores += "\"" + queryLegendClass + "\": " + score + ",";
		    }

		    if (!added) {
			if (querylegendClasses.isEmpty()) {
			    mapping.put(sourceLegendClass, sourceLegendClass);
			} else {
			    mapping.put(sourceLegendClass, querylegendClasses.get(0));
			}
		    }

		    scores = scores.substring(0, scores.length() - 1);

		    scores += innerScores + "},";
		}

		scores = scores.substring(0, scores.length() - 1);

		String realScores = "\"scores\": {" + scores + "}";

		str = "{\"status\": \"OK\",\"message\": \"" + msg + "\"," + realScores + "}";

		if (layerCreation) {
		    Set<String> classes = GeonodeConnector.getInstance().getClasses(layer, lcmlAttribute, null);

		    for (String clazz : classes) {
			if (!sourceLegendClasses.contains(clazz)) {
			    mapping.put(clazz, clazz);
			}
		    }
		}

		//////////////////// END FAKE RESPONSE

	    } else {

		//////////////////// TRUE ALGORITHM

		String result = PythonConnector.getInstance().postSimilarityQuery(legendCode, queryLegendCode, options);

		String realScores = ""; // \"scores\": {" + scores + "}

		Double threshold = filter.getSimilarityThreshold();

		ScoreParserOriginalToQuery parser = new ScoreParserOriginalToQuery(result, filter.getType(), threshold);

		result = parser.getJSON().toString();

		realScores = result.substring(result.indexOf("{") + 1, result.lastIndexOf("}"));

		str = "{\"status\": \"OK\",\"message\": \"" + msg + "\"," + realScores + "}";

		if (layerCreation) {
		    Set<String> classes = GeonodeConnector.getInstance().getClasses(layer, lcmlAttribute, null);
		    mapping.clear();

		    for (String clazz : classes) {
			String translation = parser.map.get(clazz);
			if (translation != null) {
			    mapping.put(clazz, translation);
			    sourceLegendClasses.add(clazz);
			} else {
			    mapping.put(clazz, clazz);
			}
		    }

		    try {
			List<String> myOriginalCodes = filter.getAdvancedOptions().get("originalCodes").getOptions();
			List<String> myTranslatedCodes = filter.getAdvancedOptions().get("translatedCodes").getOptions();

			if (myOriginalCodes != null && myTranslatedCodes != null) {
			    if (myOriginalCodes.size() != myTranslatedCodes.size()) {
				FAOBISResponse entity = new FAOBISResponse(Status.ERROR,
					"Custom mapping incomplete, check size of operands: ");
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(entity).build();
			    } else {
				for (int i = 0; i < myOriginalCodes.size(); i++) {
				    String originalCode = myOriginalCodes.get(i);
				    String translation = myTranslatedCodes.get(i);
				    mapping.put(originalCode, translation);
				}
			    }
			}
		    } catch (NullPointerException e) {

		    }

		}

		//////////////////// END TRUE ALGORITHM
	    }

	    for (String sourceClass : sourceLegendClasses) {
		String translation = mapping.get(sourceClass);
		if (translation == null) {
		    translation = sourceClass;
		}
		if (!querylegendClasses.contains(translation)) {
		    // it must be added from source legend
		    NodeResult missingClazz = inLegendReader.evaluateNode("//*:LC_LandCoverClass[*:map_code='" + translation + "']");
		    System.out.println("Adding class: " + translation);
		    outLegendWriter.addNode("//*:LC_Legend/*:elements", missingClazz);
		}
	    }

	    System.out.println("Putting this legend:");
	    // System.out.println(outLegendReader.asString());

	    if (layerCreation) {

		String outLegendId = aggregatedLayer + "_legend";
		outLegendWriter.setText("/*:LC_Legend/@id", outLegendId);
		outLegendWriter.setText("/*:LC_Legend/*:name", outLegendId);

		ExistConnector.getInstance().putLegend(outLegendId, outLegendReader.getDocument().getDocumentElement());
		GeonodeConnector.getInstance().addClassification(aggregatedLayer, lcmlAttribute, outLegendId);

		Response ret = createAggregation(aggregatedLayer, layer, mapping);
		if (ret.getStatus() != Response.Status.OK.getStatusCode()) {
		    return ret;
		}
	    }

	    return Response.status(Response.Status.OK).entity(str).build();

	} catch (Exception e) {
	    FAOBISResponse entity = new FAOBISResponse(Status.ERROR, "Error thematizing the layer: " + e.getMessage());
	    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(entity).build();
	}

    }

    //////////////////////////////////////////////////
    // AGGREGATIONS
    //////////////////////////////////////////////////

    public Response createAggregation(String aggregatedLayer, //
	    String layer, //
	    HashMap<String, String> mapping) {
	List<String> originalCodes = new ArrayList<>();
	List<String> translatedCodes = new ArrayList<>();
	for (String key : mapping.keySet()) {
	    originalCodes.add(key);
	    translatedCodes.add(mapping.get(key));
	}
	return createAggregation(aggregatedLayer, layer, originalCodes, translatedCodes);
    }

    /**
     * Creates an aggregated layer
     */
    @PUT
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @ApiOperation(value = "Create aggregated layer", notes = "Creates an aggregated layer from an existing (parent) layer and a given set of original codes and a set of correspondent translated codes.")
    @Path("layers/{layer}")
    public Response createAggregation(
	    @ApiParam(value = "The identifier of the aggregated layer to create.", required = true) @PathParam("layer") String aggregatedLayer, //
	    @ApiParam(value = "The identifier of the source (parent) layer to use for creating the aggregated layer.", required = true) @QueryParam("parentLayer") String layer, //
	    // @ApiParam(value = "The name of the attribute containing the LCML code.", required = true)
	    // @QueryParam("lcmlAttribute") String lcmlAttribute, //
	    @ApiParam(value = "The original codes of the source layer.", required = true) @QueryParam("originalCodes") List<String> originalCodes, //
	    @ApiParam(value = "The correspondent translated codes of the aggregated layer.", required = true) @QueryParam("translatedCodes") List<String> translatedCodes) {

	System.out.println("Creating aggregated layer " + aggregatedLayer + " of layer " + layer);

	if (aggregatedLayer.equals(layer)) {
	    FAOBISResponse entity = new FAOBISResponse(Status.INVALID_ARGUMENT,
		    "Aggregated layers can't have the same name as the input layer: " + aggregatedLayer + "==" + layer);
	    return Response.status(Response.Status.BAD_REQUEST).entity(entity).build();
	}

	if (originalCodes.size() != translatedCodes.size()) {
	    FAOBISResponse entity = new FAOBISResponse(Status.INVALID_ARGUMENT,
		    "Original classes number mismatches with translated classes number: " + originalCodes.size() + "!="
			    + translatedCodes.size());
	    return Response.status(Response.Status.BAD_REQUEST).entity(entity).build();
	}

	try {

	    SimpleEntry<String, String> classification = GeonodeConnector.getInstance().getClassification(layer);

	    if (classification == null) {
		FAOBISResponse entity = new FAOBISResponse(Status.INVALID_ARGUMENT, "No classified layer found with identifier: " + layer);
		return Response.status(Response.Status.BAD_REQUEST).entity(entity).build();
	    }

	    String lcmlAttribute = classification.getKey();

	    GeonodeConnector.getInstance().createAggregationTable(lcmlAttribute, layer, aggregatedLayer, originalCodes, translatedCodes);

	    GeonodeConnector.getInstance().addLayer(layer, aggregatedLayer);

	    GeoserverConnector.getInstance().publishLayer(aggregatedLayer, lcmlAttribute);
	} catch (Exception e) {
	    e.printStackTrace();

	    FAOBISResponse entity = new FAOBISResponse(Status.ERROR, "Error creating aggregated layer: " + e.getMessage());
	    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(entity).build();

	}

	FAOBISResponse entity = new FAOBISResponse(Status.OK, "Aggregated layer (of " + layer + ") created: " + aggregatedLayer);
	return Response.status(Response.Status.OK).entity(entity).build();
    }

    //////////////////////////////////////////////////
    // LAYERS
    //////////////////////////////////////////////////

    /**
     * Lists the available layer in GeoServer/GeoNode
     */
    @GET
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @ApiOperation(value = "List layers", notes = "Lists the layers from GeoServer/GeoNode")
    @Path("layers")
    public Response getLayers() {
	System.out.println("Getting available map layers.");

	try {
	    List<String> layers = GeoserverConnector.getInstance().getManager().getReader().getLayers().getNames();
	    FAOBISResponse entity = new FAOBISResponse(Status.OK, "Layers retrieved.");
	    entity.getResults().addAll(layers);
	    return Response.status(Response.Status.OK).entity(entity).build();
	} catch (Exception e) {
	    FAOBISResponse entity = new FAOBISResponse(Status.ERROR, "Error getting the layers");
	    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(entity).build();
	}

    }

    /**
     * Removes a GeoServer/GeoNode layer
     */
    @DELETE
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @ApiOperation(value = "Delete layer", notes = "Deletes a map layer.")
    @Path("layers/{layer}")
    public Response removeLayer(
	    @ApiParam(value = "The identifier of the layer to delete.", required = true) @PathParam("layer") String layer) {
	System.out.println("Removing Geoserver/Geonode layer: " + layer);

	try {
	    GeoserverConnector.getInstance().getManager().getPublisher().unpublishFeatureType(GeoserverConnector.WORKSPACE,
		    GeoserverConnector.STORE_NAME, layer);
	    GeoserverConnector.getInstance().getManager().getStyleManager().removeStyle(layer + GeoserverConnector.STYLE_SUFFIX);
	    GeonodeConnector.getInstance().deleteLayer(layer);
	} catch (Exception e) {
	    FAOBISResponse entity = new FAOBISResponse(Status.ERROR, "Error removing layer: " + layer);
	    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(entity).build();
	}
	FAOBISResponse entity = new FAOBISResponse(Status.OK, "Layer removed: " + layer);
	return Response.status(Response.Status.OK).entity(entity).build();
    }

    /**
     * Creates a GeoServer/GeoNode layer from a feature set in the db
     */
    // @PUT
    // @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    // @ApiOperation(value = "Creates a map layer", notes = "Creates a map layer from an existing feature set or
    // aggregated feature set.")
    // @Path("layers/{layer}")
    // public Response createLayer(
    // @ApiParam(value = "The identifier of the layer to create. This must be equal to an existing feature set or
    // aggregated
    // feature set name.", required = true) @PathParam("layer") String layer, //
    // @ApiParam(value = "The name of the legend to be applied.", required = false) @PathParam("legend") String legend)
    // {
    // System.out.println("Creating Geoserver/GeoNode layer: " + layer);
    //
    // String styleDocument;
    // try {
    // styleDocument = PostGISConnector.getInstance().createStyle(layer);
    // } catch (Exception e) {
    // e.printStackTrace();
    // FAOBISResponse entity = new FAOBISResponse(Status.ERROR,
    // "Error creating style for layer: " + layer + " (" + e.getMessage() + ")");
    // return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(entity).build();
    // }
    // try {
    // // GeoserverConnector.getInstance().getManager().getPublisher().removeLayer(GeoserverConnector.WORKSPACE,
    // // layer);
    // GeoserverConnector.getInstance().getManager().getPublisher().unpublishFeatureType(GeoserverConnector.WORKSPACE,
    // GeoserverConnector.STORE_NAME, layer);
    // // GeoserverConnector.getInstance().getManager().getPublisher().removeLayer(GeoserverConnector.WORKSPACE,
    // // layer);
    // GeoserverConnector.getInstance().getManager().getStyleManager().removeStyleInWorkspace(GeoserverConnector.WORKSPACE,
    // layer + GeoserverConnector.STYLE_SUFFIX);
    // GeoserverConnector.getInstance().getManager().getStyleManager().publishStyleInWorkspace(GeoserverConnector.WORKSPACE,
    // styleDocument, layer + GeoserverConnector.STYLE_SUFFIX);
    // GSFeatureTypeEncoder fte = new GSFeatureTypeEncoder();
    // fte.setProjectionPolicy(GSResourceEncoder.ProjectionPolicy.REPROJECT_TO_DECLARED);
    // fte.addKeyword(layer);
    // fte.setTitle(layer);
    // fte.setName(layer);
    // fte.setSRS("EPSG:4326");
    // final GSLayerEncoder layerEncoder = new GSLayerEncoder();
    // layerEncoder.setDefaultStyle(layer + GeoserverConnector.STYLE_SUFFIX);
    // GeoserverConnector.getInstance().getManager().getPublisher().publishDBLayer(GeoserverConnector.WORKSPACE,
    // GeoserverConnector.STORE_NAME, fte, layerEncoder);
    // // GeoserverConnector.getInstance().getManager().getPublisher().configureLayer(GeoserverConnector.WORKSPACE,
    // // layer, layerEncoder);
    // } catch (Exception e) {
    // FAOBISResponse entity = new FAOBISResponse(Status.ERROR, "Error publishing layer: " + layer);
    // return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(entity).build();
    // }
    // FAOBISResponse entity = new FAOBISResponse(Status.OK, "Layer published: " + layer);
    // return Response.status(Response.Status.OK).entity(entity).build();
    // }

    /**
     * Gets the layer attributes
     */
    @GET
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @ApiOperation(value = "Get layer attributes", notes = "Gets the layer attributes of the layer associated with the given identifier.")
    @Path("layers/{layer}/attributes")
    public Response getAttributes( //
	    @ApiParam(value = "The identifier of the layer.", required = true) @PathParam("layer") String layer) {
	System.out.println("Geting the attributes from the db.");

	try {
	    List<String> features = GeonodeConnector.getInstance().getAttributes(layer, null);
	    FAOBISResponse entity = new FAOBISResponse(Status.OK, "Attributes retrieved.");
	    entity.getResults().addAll(features);
	    return Response.status(Response.Status.OK).entity(entity).build();
	} catch (Exception e) {
	    FAOBISResponse entity = new FAOBISResponse(Status.ERROR, "Error getting the attributes");
	    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(entity).build();
	}

    }

    /**
     * Gets the layer attribute values
     */
    @GET
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @ApiOperation(value = "Get layer attribute values", notes = "Gets the layer attributes values of the layer associated with the given identifier.")
    @Path("layers/{layer}/attribute/{attribute}/values")
    public Response getAttributeValues( //
	    @ApiParam(value = "The identifier of the layer.", required = true) @PathParam("layer") String layer, //
	    @ApiParam(value = "The identifier of the attribute.", required = true) @PathParam("attribute") String attribute) {
	System.out.println("Geting the attributes values from the db.");

	try {
	    List<String> values = GeonodeConnector.getInstance().getAttributeValues(layer, attribute);
	    FAOBISResponse entity = new FAOBISResponse(Status.OK, "Attributes values retrieved.");
	    entity.getResults().addAll(values);
	    return Response.status(Response.Status.OK).entity(entity).build();
	} catch (Exception e) {
	    FAOBISResponse entity = new FAOBISResponse(Status.ERROR, "Error getting the attribute values");
	    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(entity).build();
	}

    }

    /**
     * Gets the feature with the given name from a given layer
     */
    @GET
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @ApiOperation(value = "Get feature", notes = "Gets the feature in the given layer that is associated with the given identifier.")
    @Path("layers/{layer}/feature/{feature}")
    public Response getFeature( //
	    @ApiParam(value = "The identifier of the layer.", required = true) @PathParam("layer") String layer, //
	    @ApiParam(value = "The feature identifier.", required = true) @PathParam("feature") String feature) {
	System.out.println("Geting the feature from the db.");

	try {
	    List<String> features = GeonodeConnector.getInstance().getFeature(layer, feature);
	    FAOBISResponse entity = new FAOBISResponse(Status.OK, "Features retrieved.");
	    entity.getResults().addAll(features);
	    return Response.status(Response.Status.OK).entity(entity).build();
	} catch (Exception e) {
	    FAOBISResponse entity = new FAOBISResponse(Status.ERROR, "Error getting the feature");
	    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(entity).build();
	}

    }

    /**
     * Lists the available features in a layer or in a rectangular region
     */
    @GET
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @ApiOperation(value = "List features", notes = "Lists the available features from a given layer optionally satisfying the given attributes constraints. Attributes constraints include a rectangular region (bounding box) or polygonal region (bounding polygon) constraining (intersecting) the desired features and a list of name-value attribute pairs.")
    @Path("layers/{layer}/features")
    public Response getFeatures(
	    @ApiParam(value = "The identifier of the layer containing the desired features.", required = true) @PathParam("layer") String layer, //
	    @ApiParam(value = "The bounding box constraint selects the features intersecting the given rectangular region, expressed as minLat,minLon,maxLat,MaxLon coordinates in decimal degrees", required = false) @QueryParam("bbox") String bbox, //
	    @ApiParam(value = "The bounding polygon constraint selects the features intersecting the given polygonal region, expressed as a comma separated list of lat,lon pairs in decimal degrees (i.e. lat1,lon1,lat2,lon2,...,latN,lonN) such as lat1,lon1=latN,lonN", required = false) @QueryParam("polygon") String polygon, //
	    @ApiParam(value = "A comma separated set of attribute names", required = false) @QueryParam("attributesNames") List<String> attributesNames, //
	    @ApiParam(value = "A comma separated set of attribute values", required = false) @QueryParam("attributesValues") List<String> attributesValues//
    ) {
	System.out.println("Getting available features from the db.");

	if (attributesNames != null && attributesValues != null && attributesNames.size() != attributesValues.size()) {
	    FAOBISResponse entity = new FAOBISResponse(Status.INVALID_ARGUMENT,
		    "Attribute name-value lists must have the same size: " + attributesNames.size() + "!=" + attributesValues.size());
	    return Response.status(Response.Status.BAD_REQUEST).entity(entity).build();
	}

	try {
	    Set<String> features = GeonodeConnector.getInstance().getFeatures(layer, bbox, polygon, attributesNames, attributesValues);
	    FAOBISResponse entity = new FAOBISResponse(Status.OK, "Features retrieved.");
	    entity.getResults().addAll(features);
	    return Response.status(Response.Status.OK).entity(entity).build();
	} catch (Exception e) {
	    FAOBISResponse entity = new FAOBISResponse(Status.ERROR, "Error getting the features");
	    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(entity).build();
	}

    }

    /**
     * Counts the available features in a layer or in a rectangular region
     */
    @GET
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @ApiOperation(value = "Count features", notes = "Counts the available features from a given layer optionally satisfying the given attributes constraints. Attributes constraints include a rectangular region (bounding box) or polygonal region (bounding polygon) constraining (intersecting) the desired features and a list of name-value attribute pairs.")
    @Path("layers/{layer}/features/count")
    public Response getFeaturesCount(
	    @ApiParam(value = "The identifier of the layer containing the desired features.", required = true) @PathParam("layer") String layer, //
	    @ApiParam(value = "The bounding box constraint selects the features intersecting the given rectangular region, expressed as minLat,minLon,maxLat,MaxLon coordinates in decimal degrees", required = false) @QueryParam("bbox") String bbox, //
	    @ApiParam(value = "The bounding polygon constraint selects the features intersecting the given polygonal region, expressed as a comma separated list of lat,lon pairs in decimal degrees (i.e. lat1,lon1,lat2,lon2,...,latN,lonN) such as lat1,lon1=latN,lonN", required = false) @QueryParam("polygon") String polygon, //
	    @ApiParam(value = "A comma separated set of attribute names", required = false) @QueryParam("attributesNames") List<String> attributesNames, //
	    @ApiParam(value = "A comma separated set of attribute values", required = false) @QueryParam("attributesValues") List<String> attributesValues//
    ) {
	System.out.println("Counting available features from the db.");

	if (attributesNames != null && attributesValues != null && attributesNames.size() != attributesValues.size()) {
	    FAOBISResponse entity = new FAOBISResponse(Status.INVALID_ARGUMENT,
		    "Attribute name-value lists must have the same size: " + attributesNames.size() + "!=" + attributesValues.size());
	    return Response.status(Response.Status.BAD_REQUEST).entity(entity).build();
	}

	try {
	    Set<String> features = GeonodeConnector.getInstance().getFeatures(layer, bbox, polygon, attributesNames, attributesValues);
	    FAOBISResponse entity = new FAOBISResponse(Status.OK, "Features retrieved.");
	    entity.getResults().add("" + features.size());
	    return Response.status(Response.Status.OK).entity(entity).build();
	} catch (Exception e) {
	    FAOBISResponse entity = new FAOBISResponse(Status.ERROR, "Error getting the features");
	    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(entity).build();
	}

    }

    /**
     * Gets the area covered by the available features in a layer or in a rectangular region
     */
    @GET
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @ApiOperation(value = "Get features area", notes = "Gets the area of the available features from a given layer optionally satisfying the given attributes constraints. Attributes constraints include a rectangular region (bounding box) or polygonal region (bounding polygon) constraining (intersecting) the desired features and a list of name-value attribute pairs.")
    @Path("layers/{layer}/features/area")
    public Response getFeaturesArea(
	    @ApiParam(value = "The identifier of the layer containing the desired features.", required = true) @PathParam("layer") String layer, //
	    @ApiParam(value = "The bounding box constraint selects the features intersecting the given rectangular region, expressed as minLat,minLon,maxLat,MaxLon coordinates in decimal degrees", required = false) @QueryParam("bbox") String bbox, //
	    @ApiParam(value = "The bounding polygon constraint selects the features intersecting the given polygonal region, expressed as a comma separated list of lat,lon pairs in decimal degrees (i.e. lat1,lon1,lat2,lon2,...,latN,lonN) such as lat1,lon1=latN,lonN", required = false) @QueryParam("polygon") String polygon, //
	    @ApiParam(value = "A comma separated set of attribute names", required = false) @QueryParam("attributesNames") List<String> attributesNames, //
	    @ApiParam(value = "A comma separated set of attribute values", required = false) @QueryParam("attributesValues") List<String> attributesValues//
    ) {
	System.out.println("Getting the area of the available features from the db.");

	if (attributesNames != null && attributesValues != null && attributesNames.size() != attributesValues.size()) {
	    FAOBISResponse entity = new FAOBISResponse(Status.INVALID_ARGUMENT,
		    "Attribute name-value lists must have the same size: " + attributesNames.size() + "!=" + attributesValues.size());
	    return Response.status(Response.Status.BAD_REQUEST).entity(entity).build();
	}

	try {

	    String area = GeonodeConnector.getInstance().getArea(layer, bbox, polygon, attributesNames, attributesValues);
	    FAOBISResponse entity = new FAOBISResponse(Status.OK, "Features area retrieved (square meters).");
	    entity.getResults().add(area);
	    return Response.status(Response.Status.OK).entity(entity).build();
	} catch (Exception e) {
	    FAOBISResponse entity = new FAOBISResponse(Status.ERROR, "Error getting the features");
	    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(entity).build();
	}

    }

    /**
     * Gets the areas by covered by the available features in a layer or in a rectangular region
     */
    @GET
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @ApiOperation(value = "Get feature areas by class", notes = "Gets the areas by class of the available features from a given layer optionally satisfying the given attributes constraints. Attributes constraints include a rectangular region (bounding box) or polygonal region (bounding polygon) constraining (intersecting) the desired features and a list of name-value attribute pairs.")
    @Path("layers/{layer}/features/areasByClass")
    public Response getFeatureAreasByClass(
	    @ApiParam(value = "The identifier of the layer containing the desired features.", required = true) @PathParam("layer") String layer, //
	    @ApiParam(value = "The bounding box constraint selects the features intersecting the given rectangular region, expressed as minLat,minLon,maxLat,MaxLon coordinates in decimal degrees", required = false) @QueryParam("bbox") String bbox, //
	    @ApiParam(value = "The bounding polygon constraint selects the features intersecting the given polygonal region, expressed as a comma separated list of lat,lon pairs in decimal degrees (i.e. lat1,lon1,lat2,lon2,...,latN,lonN) such as lat1,lon1=latN,lonN", required = false) @QueryParam("polygon") String polygon, //
	    @ApiParam(value = "A comma separated set of attribute names", required = false) @QueryParam("attributesNames") List<String> attributesNames, //
	    @ApiParam(value = "A comma separated set of attribute values", required = false) @QueryParam("attributesValues") List<String> attributesValues//
    ) {
	System.out.println("Getting the areas by class of the available features from the db.");

	if (attributesNames != null && attributesValues != null && attributesNames.size() != attributesValues.size()) {
	    FAOBISResponse entity = new FAOBISResponse(Status.INVALID_ARGUMENT,
		    "Attribute name-value lists must have the same size: " + attributesNames.size() + "!=" + attributesValues.size());
	    return Response.status(Response.Status.BAD_REQUEST).entity(entity).build();
	}

	try {

	    HashMap<String, String> areas = GeonodeConnector.getInstance().getAreasByClass(layer, bbox, polygon, attributesNames,
		    attributesValues);
	    FAOBISResponse entity = new FAOBISResponse(Status.OK, "Features areas by class retrieved (square meters).");
	    for (String clazz : areas.keySet()) {
		String area = areas.get(clazz);
		entity.getResults().add(clazz);
		entity.getResults().add(area);
	    }

	    return Response.status(Response.Status.OK).entity(entity).build();
	} catch (Exception e) {
	    FAOBISResponse entity = new FAOBISResponse(Status.ERROR, "Error getting the features");
	    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(entity).build();
	}

    }

    /**
     * Gets the class counts for the available features in a layer or in a rectangular region
     */
    @GET
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @ApiOperation(value = "Get feature counts by class", notes = "Gets the counts by class of the available features from a given layer optionally satisfying the given attributes constraints. Attributes constraints include a rectangular region (bounding box) or polygonal region (bounding polygon) constraining (intersecting) the desired features and a list of name-value attribute pairs.")
    @Path("layers/{layer}/features/countsByClass")
    public Response getFeatureCountsByClass(
	    @ApiParam(value = "The identifier of the layer containing the desired features.", required = true) @PathParam("layer") String layer, //
	    @ApiParam(value = "The bounding box constraint selects the features intersecting the given rectangular region, expressed as minLat,minLon,maxLat,MaxLon coordinates in decimal degrees", required = false) @QueryParam("bbox") String bbox, //
	    @ApiParam(value = "The bounding polygon constraint selects the features intersecting the given polygonal region, expressed as a comma separated list of lat,lon pairs in decimal degrees (i.e. lat1,lon1,lat2,lon2,...,latN,lonN) such as lat1,lon1=latN,lonN", required = false) @QueryParam("polygon") String polygon, //
	    @ApiParam(value = "A comma separated set of attribute names", required = false) @QueryParam("attributesNames") List<String> attributesNames, //
	    @ApiParam(value = "A comma separated set of attribute values", required = false) @QueryParam("attributesValues") List<String> attributesValues//
    ) {
	System.out.println("Getting the counts by class of the available features from the db.");

	if (attributesNames != null && attributesValues != null && attributesNames.size() != attributesValues.size()) {
	    FAOBISResponse entity = new FAOBISResponse(Status.INVALID_ARGUMENT,
		    "Attribute name-value lists must have the same size: " + attributesNames.size() + "!=" + attributesValues.size());
	    return Response.status(Response.Status.BAD_REQUEST).entity(entity).build();
	}

	try {

	    HashMap<String, Integer> areas = GeonodeConnector.getInstance().getCountsByClass(layer, bbox, polygon, attributesNames,
		    attributesValues);
	    FAOBISResponse entity = new FAOBISResponse(Status.OK, "Features counts by class retrieved.");
	    for (String clazz : areas.keySet()) {
		String counts = "" + areas.get(clazz);
		entity.getResults().add(clazz);
		entity.getResults().add(counts);
	    }

	    return Response.status(Response.Status.OK).entity(entity).build();
	} catch (Exception e) {
	    FAOBISResponse entity = new FAOBISResponse(Status.ERROR, "Error getting the features");
	    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(entity).build();
	}

    }

    @GET
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @ApiOperation(value = "Get count of basic objects by class", notes = "Gets the basic object counts by class of a given layer.")
    @Path("layers/{layer}/statistics/basicObjectsByClass")
    public Response getBasicObjectsByClass(
	    @ApiParam(value = "The identifier of the layer.", required = true) @PathParam("layer") String layer) {
	System.out.println("Getting the basic object counts by class.");

	try {

	    SimpleEntry<String, String> classification = GeonodeConnector.getInstance().getClassification(layer);
	    if (classification == null) {
		FAOBISResponse entity = new FAOBISResponse(Status.INVALID_ARGUMENT, "No classified layer found with identifier: " + layer);
		return Response.status(Response.Status.BAD_REQUEST).entity(entity).build();
	    }
	    String lcmlAttribute = classification.getKey();
	    String legend = classification.getValue();

	    Set<String> classes = GeonodeConnector.getInstance().getClasses(layer, lcmlAttribute, null);
            
            ExistConnector existConnectorInstance = ExistConnector.getInstance();
            String xml = existConnectorInstance.getLegend(legend).iterator().next();

	    FAOBISResponse entity = new FAOBISResponse(Status.OK, "Basic object counts by class.");
	    for (String clazz : classes) {
		String count = "" + existConnectorInstance.getBasicObjectCount(legend, clazz, null, xml);
		entity.getResults().add(clazz);
		entity.getResults().add(count);
	    }
	    return Response.status(Response.Status.OK).entity(entity).build();
	} catch (Exception e) {
	    FAOBISResponse entity = new FAOBISResponse(Status.ERROR, "Error getting the features");
	    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(entity).build();
	}

    }

    @GET
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @ApiOperation(value = "Get count of properties by class", notes = "Gets the property counts by class of a given layer.")
    @Path("layers/{layer}/statistics/propertiesByClass")
    public Response getPropertiesByClass(
	    @ApiParam(value = "The identifier of the layer.", required = true) @PathParam("layer") String layer) {
	System.out.println("Getting the property counts by class.");

	try {

	    SimpleEntry<String, String> classification = GeonodeConnector.getInstance().getClassification(layer);
	    if (classification == null) {
		FAOBISResponse entity = new FAOBISResponse(Status.INVALID_ARGUMENT, "No classified layer found with identifier: " + layer);
		return Response.status(Response.Status.BAD_REQUEST).entity(entity).build();
	    }
	    String lcmlAttribute = classification.getKey();
	    String legend = classification.getValue();

	    Set<String> classes = GeonodeConnector.getInstance().getClasses(layer, lcmlAttribute, null);
            ExistConnector existConnectorInstance = ExistConnector.getInstance();
            String xml = existConnectorInstance.getLegend(legend).iterator().next();

	    FAOBISResponse entity = new FAOBISResponse(Status.OK, "Property counts by class.");
	    for (String clazz : classes) {
		String count = "" + existConnectorInstance.getPropertyCount(legend, clazz, xml);
		entity.getResults().add(clazz);
		entity.getResults().add(count);
	    }
	    return Response.status(Response.Status.OK).entity(entity).build();
	} catch (Exception e) {
	    FAOBISResponse entity = new FAOBISResponse(Status.ERROR, "Error getting the features");
	    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(entity).build();
	}

    }

    @GET
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @ApiOperation(value = "Get count of characteristics by class", notes = "Gets the characteristic counts by class of a given layer.")
    @Path("layers/{layer}/statistics/characteristicsByClass")
    public Response getCharacteristicsByClass(
	    @ApiParam(value = "The identifier of the layer.", required = true) @PathParam("layer") String layer) {
	System.out.println("Getting the characteristic counts by class.");

	try {

	    SimpleEntry<String, String> classification = GeonodeConnector.getInstance().getClassification(layer);
	    if (classification == null) {
		FAOBISResponse entity = new FAOBISResponse(Status.INVALID_ARGUMENT, "No classified layer found with identifier: " + layer);
		return Response.status(Response.Status.BAD_REQUEST).entity(entity).build();
	    }
	    String lcmlAttribute = classification.getKey();
	    String legend = classification.getValue();

	    Set<String> classes = GeonodeConnector.getInstance().getClasses(layer, lcmlAttribute, null);
            
            ExistConnector existConnectorInstance = ExistConnector.getInstance();
            String xml = existConnectorInstance.getLegend(legend).iterator().next();

	    FAOBISResponse entity = new FAOBISResponse(Status.OK, "Characteristic counts by class.");
	    for (String clazz : classes) {
		String count = "" + existConnectorInstance.getCharacteristicCount(legend, clazz, xml);
		entity.getResults().add(clazz);
		entity.getResults().add(count);
	    }
	    return Response.status(Response.Status.OK).entity(entity).build();
	} catch (Exception e) {
	    FAOBISResponse entity = new FAOBISResponse(Status.ERROR, "Error getting the features");
	    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(entity).build();
	}

    }

    @GET
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @ApiOperation(value = "Get basic object counts", notes = "Gets the counts by basic object.")
    @Path("layers/{layer}/statistics/countsByBasicObject")
    public Response getCountsByBasicObject(
	    @ApiParam(value = "The identifier of the layer.", required = true) @PathParam("layer") String layer) {
	System.out.println("Getting the counts by basic object.");

	try {

	    SimpleEntry<String, String> classification = GeonodeConnector.getInstance().getClassification(layer);
	    if (classification == null) {
		FAOBISResponse entity = new FAOBISResponse(Status.INVALID_ARGUMENT, "No classified layer found with identifier: " + layer);
		return Response.status(Response.Status.BAD_REQUEST).entity(entity).build();
	    }
	    String lcmlAttribute = classification.getKey();
	    String legend = classification.getValue();

	    Set<String> classes = GeonodeConnector.getInstance().getClasses(layer, lcmlAttribute, null);

	    FAOBISResponse entity = new FAOBISResponse(Status.OK, "Counts by basic object.");
            
            ExistConnector existConnectorInstance = ExistConnector.getInstance();
            String xml = existConnectorInstance.getLegend(legend).iterator().next();
            Set<String> objects = existConnectorInstance.getBasicObjects(legend);

	    for (String obj : objects) {
		Integer count = 0;
		for (String clazz : classes) {

		    count += existConnectorInstance.getBasicObjectCount(legend, clazz, obj, xml);

		}
		if (count > 0) {
		    entity.getResults().add(obj);
		    entity.getResults().add("" + count);
		}
	    }
	    return Response.status(Response.Status.OK).entity(entity).build();
	} catch (Exception e) {
	    FAOBISResponse entity = new FAOBISResponse(Status.ERROR, "Error getting the features");
	    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(entity).build();
	}

    }

    @GET
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @ApiOperation(value = "Get counts by instantiated basic object", notes = "Gets counts by instantiated basic object of a given layer.")
    @Path("layers/{layer}/statistics/countsByInstantiatedBasicObject")
    public Response getCountsByInstantiatedBasicObject(
	    @ApiParam(value = "The identifier of the layer.", required = true) @PathParam("layer") String layer) {
	System.out.println("Getting the counts by basic object.");

	try {

	    SimpleEntry<String, String> classification = GeonodeConnector.getInstance().getClassification(layer);
	    if (classification == null) {
		FAOBISResponse entity = new FAOBISResponse(Status.INVALID_ARGUMENT, "No classified layer found with identifier: " + layer);
		return Response.status(Response.Status.BAD_REQUEST).entity(entity).build();
	    }
	    String lcmlAttribute = classification.getKey();
	    String legend = classification.getValue();

	    Set<String> classes = GeonodeConnector.getInstance().getClasses(layer, lcmlAttribute, null);

	    FAOBISResponse entity = new FAOBISResponse(Status.OK, "Counts by basic object.");
            
            
            ExistConnector existConnectorInstance = ExistConnector.getInstance();
            String xml = existConnectorInstance.getLegend(legend).iterator().next();

	    Set<String> objects = existConnectorInstance.getBasicObjects(legend);

	    HashMap<String, Integer> featureCounts = GeonodeConnector.getInstance().getCountsByClass(layer, null, null, null, null);
            Integer cc = 0;
	    for (String obj : objects) {
		Integer count = 0;
		for (String clazz : classes) {

		    Integer multiplier = featureCounts.get(clazz);

		    if (multiplier != 0) {
			Integer sub = multiplier * existConnectorInstance.getBasicObjectCount(legend, clazz, obj, xml);
			count += sub;
		    }
                    cc++;
                  
                            
		}
		if (count > 0) {
		    entity.getResults().add(obj);
		    entity.getResults().add("" + count);
		}
	    }
	    return Response.status(Response.Status.OK).entity(entity).build();
	} catch (Exception e) {
	    FAOBISResponse entity = new FAOBISResponse(Status.ERROR, "Error getting the features");
	    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(entity).build();
	}

    }

    //////////////////////////////////////////////////
    // CLASSIFICATION
    //////////////////////////////////////////////////

    // /**
    // * Lists the classifications
    // */
    // @GET
    // @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    // @ApiOperation(value = "List classifications", notes = "Lists the existing classifications.")
    // @Path("classifications")
    // public Response getClassifications() {
    // System.out.println("Getting existing classifications from the db.");
    //
    // try {
    // Set<String> ret = GeonodeConnector.getInstance().getClassifications();
    // FAOBISResponse entity = new FAOBISResponse(Status.OK, "Classifications retrieved.");
    // entity.getResults().addAll(ret);
    // return Response.status(Response.Status.OK).entity(entity).build();
    // } catch (Throwable e) {
    // FAOBISResponse entity = new FAOBISResponse(Status.ERROR, "Error getting the classifications");
    // return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(entity).build();
    // }
    //
    // }
    //
    // /**
    // * Deletes a classifications with the given classification id
    // */
    // @DELETE
    // @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    // @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    // @ApiOperation(value = "Delete classification", notes = "Deletes the classification identified by the given
    // (layer) id.")
    // @Path("classifications/{classification}")
    // public Response deleteClassification(
    // @ApiParam(value = "The identifier of the classification.", required = true) @PathParam("classification") String
    // classificationId) {
    // System.out.println("Deletes the classification from the db.");
    //
    // try {
    // GeonodeConnector.getInstance().removeClassification(classificationId);
    // FAOBISResponse entity = new FAOBISResponse(Status.OK, "Classification deleted: " + classificationId);
    // return Response.status(Response.Status.OK).entity(entity).build();
    // } catch (Exception e) {
    // FAOBISResponse entity = new FAOBISResponse(Status.ERROR, "Error deleting the classification: " + e.getMessage());
    // return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(entity).build();
    // }
    //
    // }
    //
    // /**
    // * Gets a classification by a specific classification (layer) code
    // */
    // @GET
    // @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    // @ApiOperation(value = "Get classification", notes = "Gets the classification identified by the given
    // classification code.")
    // @Path("classifications/{classification}")
    // public Response getClassification(
    // @ApiParam(value = "The classification (layer) id.", required = true) @PathParam("classification") String
    // classification) {
    // System.out.println("Getting the class from the db.");
    //
    // try {
    // Set<String> ret = GeonodeConnector.getInstance().getClassification(classification);
    // FAOBISResponse entity = new FAOBISResponse(Status.OK, "Classification retrieved.");
    // entity.getResults().addAll(ret);
    // return Response.status(Response.Status.OK).entity(entity).build();
    // } catch (Exception e) {
    // FAOBISResponse entity = new FAOBISResponse(Status.ERROR, "Error getting the classification: " + e.getMessage());
    // return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(entity).build();
    // }
    //
    // }

    public static void main(String[] args) {

    }
}
