package io.enforcer.deathstar.rest;

import io.enforcer.deathstar.DeathStar;
import io.enforcer.deathstar.pojos.*;
import io.enforcer.deathstar.services.PersistenceService;
import io.enforcer.deathstar.services.ReportService;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import static java.util.Arrays.asList;

/**
 * Created by herret2 on 8/24/2015.
 */
@Path("persistence")
public class PersistenceResource {
    /**
     * Logger
     */
    private static final Logger logger = Logger.getLogger(PersistenceResource.class.getName());

    /**
     * Reference to the report store
     */
    private PersistenceService persistenceService;

    /**
     * MongoDB
     */
    //private MongoClient mongoClient;
    //private MongoDatabase db;

    /**
     * Instantiate REST resource
     */
    public PersistenceResource() {
        this.persistenceService = DeathStar.getPersistenceService();
        logger.log(Level.FINE, "persistence service instantiated: {0}", this);

        // Connect to MongoDB
        //mongoClient = new MongoClient("localhost", 27017);
        //db = mongoClient.getDatabase("test");
    }

    /** ========================================================================================
     **  User API
     **  ==================================================================================== */

    /**
     * Retrieves a User with a given userId
     */
    @GET
    @Path("/user/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public User getUser(@PathParam("userId") String userId) {

        logger.log(Level.INFO, "Retrieving User via API with userId: " + userId);

        // Example Return, will change when db implemented
        return new User(Integer.parseInt(userId), "HERRET2", "Welcome01", 3);
    }

    /**
     *  Overwrites a User with a given userId and user object
     */
    @PUT
    @Path("/user/{userId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public User putUser(String userString, @PathParam("userId") String userId) {

        JsonReader jsonReader = Json.createReader(new StringReader(userString));
        JsonObject user = jsonReader.readObject();
        logger.log(Level.INFO, "Received a User via API PUT: ", userId);
        jsonReader.close();

        // Example Return, will change when db implemented
        return new User(Integer.parseInt(userId), "HERRET2", "Welcome01", 3);
    }

    /** ========================================================================================
     **  Report API
     **  ==================================================================================== */

    /*
     * Retrieves a Report with a given reportId
     */
    @GET
    @Path("/report/{reportId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Report getReport(@PathParam("reportId") String reportId) {

        logger.log(Level.INFO, "Retrieving Report via API with reportId: " + reportId);

        // Example Return, will change when db implemented
        return new Report(Integer.parseInt(reportId), "MainClass", "STARTED", "host1", "2015-02-05T13:09:32");
    }

    /**
     * Retrieves all available Reports
     */
    @GET
    @Path("/report")
    @Produces(MediaType.APPLICATION_JSON)
    public Report getReports() {

        logger.log(Level.INFO, "Retrieving All Reports via API");
        Report[] reportArray = new Report[10];
        // TODO: return array
        // Example Return, will change when db implemented
        return new Report(999, "MainClass", "STARTED", "host1", "2015-02-05T13:09:32");
    }

    /**
     * Overwrites a Report with a given reportId and report object
     */
    @PUT
    @Path("/report/{reportId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Report putReport(String reportString, @PathParam("reportId") String reportId) {

        JsonReader jsonReader = Json.createReader(new StringReader(reportString));
        JsonObject report = jsonReader.readObject();
        logger.log(Level.INFO, "Received a Report via API PUT: ", reportId);
        jsonReader.close();
        // Example Return, will change when db implemented
        return new Report(report.getInt("processId")+1000, report.getString("host"), report.getString("mainClass"), report.getString("processStateChange"), report.getString("timeStamp"));
    }

    /**
     * Creates a new Report with a given reportId and report Object
     */
    @POST
    @Path("/report/{reportId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Report postReport(String reportString, @PathParam("reportId") String reportId) {

        JsonReader jsonReader = Json.createReader(new StringReader(reportString));
        JsonObject report = jsonReader.readObject();
        logger.log(Level.INFO, "Received a Report via API POST: ", reportId);
        jsonReader.close();
        // Example Return, will change when db implemented
        return new Report(report.getInt("processId")+1000, report.getString("host"), report.getString("mainClass"), report.getString("processStateChange"), report.getString("timeStamp"));
    }

    /** ========================================================================================
     **  Audit API
     **  ==================================================================================== */

    /**
     * Retrieves an Audit with a given auditId
     */
    @GET
    @Path("/audit/{auditId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Audit getAudit(@PathParam("auditId") String auditId) {

        logger.log(Level.INFO, "Retrieving Audit via API with auditId: " + auditId);
        // Example Return, will change when db implemented
        return new Audit(Integer.parseInt(auditId), "MainClass", "STARTED", "host1", "2015-02-05T13:09:32", "New", "Acknowledged", "HERRET2", "2015-08-24T13:09:32");
    }

    /**
     * Creates a new Audit with a given auditId and Audit Object
     */
    @POST
    @Path("/audit/{auditId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Audit postAudit(String auditString, @PathParam("auditId") String auditId) {

        JsonReader jsonReader = Json.createReader(new StringReader(auditString));
        JsonObject audit = jsonReader.readObject();
        logger.log(Level.INFO, "Received an Audit via API POST: ", auditId);
        jsonReader.close();

        // Example Return, will change when db implemented
        try {
            Audit a = new Audit(Integer.parseInt(auditId), audit.getString("host"), audit.getString("mainClass"), audit.getString("processStateChange"), audit.getString("timeStamp"),
                    audit.getString("oldStatus"), audit.getString("newStatus"), audit.getString("movedTime"),audit.getString("userId"));
            return a;
        } catch(Exception e)
        {
            logger.log(Level.SEVERE, "Error Posting to Audit: " + e.getMessage());
        }


        return null;
    }
}

