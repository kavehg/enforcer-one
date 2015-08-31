package io.enforcer.deathstar.rest;

import com.google.gson.Gson;
import com.mongodb.*;
import io.enforcer.deathstar.DeathStar;
import io.enforcer.deathstar.pojos.*;
import io.enforcer.deathstar.services.PersistenceService;
import io.enforcer.deathstar.services.ReportService;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.mongodb.client.*;
import org.bson.BsonSerializationException;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import static com.mongodb.client.model.Filters.*;
import static java.util.Arrays.asList;

import com.twilio.sdk.*;
import com.twilio.sdk.resource.factory.*;
import com.twilio.sdk.resource.instance.*;
import com.twilio.sdk.resource.list.*;


/**
 * Created by herret2 on 8/24/2015.
 */
@Path("persistence")
public class PersistenceResource {

    /**  =======================================================================================
     **  Variables
     **  ==================================================================================== */

    // Logger
    private static final Logger logger = Logger.getLogger(PersistenceResource.class.getName());

    // Reference to Persistence Store
    private PersistenceService persistenceService;

    // MongoDB
    private MongoClient mongoClient;
    private MongoDatabase db;
    private MongoCollection users;
    private MongoCollection reports;
    private MongoCollection audits;

    // Twilio
    private static final String ACCOUNT_SID = "ACf067231a98cdf1ad8c03c9eb26aa8024";
    private static final String AUTH_TOKEN = "906572fca60f7e5446afec7e0963001f";

    /**  =======================================================================================
     **  Constructor
     **  ==================================================================================== */

    /**
     * Instantiate REST resource
     */
    public PersistenceResource() {
        this.persistenceService = DeathStar.getPersistenceService();
        logger.log(Level.FINE, "persistence service instantiated: {0}", this);

        // Attempt Connect to MongoDB
        try {
            mongoClient = new MongoClient("localhost", 27017);
            db = mongoClient.getDatabase("enforcer-one");
            users = db.getCollection("users");
            reports = db.getCollection("reports");
            audits = db.getCollection("audits");

            logger.log(Level.FINE, "Connected to MongoDB!");
        } catch(Exception e)
        {
            logger.log(Level.SEVERE, "Error connecting to MongoDB: ", e.getMessage());
        }
    }

    /**  =======================================================================================
     **  User API
     **  ==================================================================================== */

    /**
     * Retrieves a User with a given acf2Id
     */
    @GET
    @Path("/users/{acf2Id}")
    @Produces(MediaType.APPLICATION_JSON)
    public User getUser(@PathParam("acf2Id") String acf2Id) {

        logger.log(Level.INFO, "Retrieving User via API with userId: " + acf2Id);

        // Example Return, will change when db implemented
        return new User(acf2Id, "Welcome01", "3");
    }

    /**
     *  Overwrites a User with a given userId and user object
     */
    @PUT
    @Path("/users/{acf2Id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public User putUser(String userString, @PathParam("acf2Id") String acf2Id) {

        JsonReader jsonReader = Json.createReader(new StringReader(userString));
        JsonObject user = jsonReader.readObject();
        logger.log(Level.INFO, "Received a User via API PUT: ", acf2Id);
        jsonReader.close();

        // Example Return, will change when db implemented
        return new User(acf2Id, "Welcome01", "3");
    }

    /**  =======================================================================================
     **  Report API
     **  ==================================================================================== */

    /**
     * Retrieves a Report with a given reportId
     */
    @GET
    @Path("/reports/{reportId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getReport(@PathParam("reportId") String reportId) {

        ArrayList<Report> reportList = new ArrayList<Report>();

        try {
            logger.info("Received REST API GET request: getReport(" + reportId + ")");

            FindIterable<Document> returnedReports = reports.find(new Document("_id", new ObjectId(reportId)));

            returnedReports.forEach(new Block<Document>() {
                @Override
                public void apply(final Document document) {
                    Report r = persistenceService.documentToReport(document);
                    reportList.add(r);
                }
            });

            Report newReport = reportList.get(0);

            String jsonResult = new Gson().toJson(newReport);
            logger.info("> Report Object: " + jsonResult);

            logger.info("> Received response for getReport(reportId). Sending to requesting client...");

            return persistenceService.responseBuilder(200, jsonResult, "GET");

        } catch (MongoException me) {
            logger.log(Level.SEVERE, "MongoDB Exception when inserting Report: ", me.getMessage());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception: ", e.getMessage());
        }

        return persistenceService.responseBuilder(500, "Error with Request", "GET");
    }

    /**
     * Retrieves all available Reports
     */
    @GET
    @Path("/reports")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getReports() {

        List<Report> reportList = new ArrayList<Report>();

        try {
            logger.info("Received REST API GET request: getReports()");

            FindIterable<Document> allReports = reports.find();

            allReports.forEach(new Block<Document>() {
                @Override
                public void apply(final Document document) {
                    Report r = persistenceService.documentToReport(document);
                    reportList.add(r);
                }
            });

            String jsonResult = new Gson().toJson(reportList);

            logger.info("> Received response for getReport(reportId). Sending to requesting client...");

            return persistenceService.responseBuilder(200, jsonResult, "GET");

        } catch (MongoException me) {
            logger.log(Level.SEVERE, "MongoDB Exception when inserting Report: ", me.getMessage());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception: ", e.getMessage());
        }

        return persistenceService.responseBuilder(500, "Error with Request..","GET");
    }

    /**
     * Overwrites a Report with a given reportId and report object
     */
    @PUT
    @Path("/reports/{reportId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response putReport(String reportString, @PathParam("reportId") String reportId) {

        try {
            logger.info("Received REST API POST request: putReport(" + reportId + ")");

            Report newReport = persistenceService.buildReport(reportString);
            Document document = persistenceService.reportToDocument(newReport);

            reports.findOneAndReplace(new Document("_id", new ObjectId(reportId)), document);
            logger.info("> Success putReport(reportId). Sending to requesting client...");

            String jsonResult = new Gson().toJson(newReport);

            return persistenceService.responseBuilder(200, jsonResult, "PUT");

        } catch (MongoException me) {
            logger.log(Level.SEVERE, "MongoDB Exception when inserting Report: ", me.getMessage());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception: ", e.getMessage());
        }

        return persistenceService.responseBuilder(500, "Error with Request..", "PUT");
    }

    /**
     * Creates a new Report with a given reportId and report Object
     */
    @POST
    @Path("/reports/{reportId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response postReport(@Context HttpHeaders headers, String reportString, @PathParam("reportId") String reportId) {

        try {
            logger.info("Received REST API POST request: postReport(" + reportId + ")");

            Report newReport = persistenceService.buildReport(reportString);
            Document document = persistenceService.reportToDocument(newReport);
            reports.insertOne(document);

            logger.info("> Received response for postReport(reportId). Sending to requesting client...");

            String jsonResult = new Gson().toJson(newReport);

            return persistenceService.responseBuilder(201, jsonResult, "POST");

        } catch (MongoException me) {
            logger.log(Level.SEVERE, "MongoDB Exception when inserting Report: ", me.getMessage());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception: ", e.getMessage());
        }

        return persistenceService.responseBuilder(500, "Error with Request..", "POST");
    }

    /**  =======================================================================================
     **  Audit API
     **  ==================================================================================== */

    /**
     * Retrieves an Audit with a given auditId
     */
    @GET
    @Path("/audits/{auditId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAudit(@PathParam("auditId") String auditId) {

        ArrayList<Audit> auditList = new ArrayList<Audit>();

        try {
            logger.info("Received REST API GET request: getAudit(" + auditId + ")");

            FindIterable<Document> returnedAudits = audits.find(new Document("_id", new ObjectId(auditId)));

            returnedAudits.forEach(new Block<Document>() {
                @Override
                public void apply(final Document document) {
                    Audit r = persistenceService.documentToAudit(document);
                    auditList.add(r);
                }
            });

            Audit newAudit = auditList.get(0);

            String jsonResult = new Gson().toJson(newAudit);
            logger.info("> Report Object: " + jsonResult);

            logger.info("> Received response for getAudit(auditId). Sending to requesting client...");

            return persistenceService.responseBuilder(200, jsonResult, "GET");

        } catch (MongoException me) {
            logger.log(Level.SEVERE, "MongoDB Exception when inserting Report: ", me.getMessage());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception: ", e.getMessage());
        }

        return persistenceService.responseBuilder(500, "Error with Request", "GET");
    }

    /**
     * Retrieves all Audits
     */
    @GET
    @Path("/audits")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAudits() {

        List<Audit> auditList = new ArrayList<Audit>();

        try {
            logger.info("Received REST API GET request: getAudits()");

            FindIterable<Document> allAudits = audits.find();

            allAudits.forEach(new Block<Document>() {
                @Override
                public void apply(final Document document) {
                    Audit a = persistenceService.documentToAudit(document);
                    auditList.add(a);
                }
            });

            String jsonResult = new Gson().toJson(auditList);

            logger.info("> Received response for getAudits(). Sending to requesting client...");

            return persistenceService.responseBuilder(200, jsonResult, "GET");

        } catch (MongoException me) {
            logger.log(Level.SEVERE, "MongoDB Exception when inserting Report: ", me.getMessage());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception: ", e.getMessage());
        }

        return persistenceService.responseBuilder(500, "Error with Request..","GET");
    }

    /**
     * Creates a new Audit with a given auditId and Audit Object
     */
    @POST
    @Path("/audits/{auditId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response postAudit(String auditString, @PathParam("auditId") String auditId) {

        try {
            logger.info("Received REST API POST request: postAudit(" + auditId + ")");

            Audit newAudit = persistenceService.buildAudit(auditString);
            Document document = persistenceService.auditToDocument(newAudit);
            audits.insertOne(document);

            logger.info("> Received response for postAudit(auditId). Sending to requesting client...");

            String jsonResult = new Gson().toJson(newAudit);

            return persistenceService.responseBuilder(201, jsonResult, "POST");

        } catch (MongoException me) {
            logger.log(Level.SEVERE, "MongoDB Exception when inserting Report: ", me.getMessage());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception: ", e.getMessage());
        }

        return persistenceService.responseBuilder(500, "Error with Request..", "POST");
    }

    /**  =======================================================================================
     **  Helper Functions -> All moved to PersistenceService.java
     **  ==================================================================================== */

}

