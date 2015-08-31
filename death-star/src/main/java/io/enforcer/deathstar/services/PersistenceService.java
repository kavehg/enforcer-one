package io.enforcer.deathstar.services;

import io.enforcer.deathstar.pojos.Action;
import io.enforcer.deathstar.pojos.Audit;
import io.enforcer.deathstar.pojos.Report;
import io.enforcer.deathstar.pojos.User;
import io.enforcer.deathstar.ws.WebSocketBroadcastThread;
import org.bson.Document;
import org.bson.types.ObjectId;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.io.StringReader;
import java.util.Set;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by herret2 on 8/24/2015.
 */
public class PersistenceService {

    /**  =======================================================================================
     **  Variables
     **  ==================================================================================== */

    private static final Logger logger = Logger.getLogger(PersistenceService.class.getName());

    /**  =======================================================================================
     **  Constructor
     **  ==================================================================================== */
    public PersistenceService() {
        logger.log(Level.FINE, "ReportService instantiated: {0}", this);
    }

    /**  =======================================================================================
     **  Methods
     **  ==================================================================================== */

    /**
     * Builds a Report object from a given string
     */
    public Report buildReport(String reportString) {

        JsonReader jsonReader = Json.createReader(new StringReader(reportString));
        JsonObject report = jsonReader.readObject();
        jsonReader.close();

        Report newReport;

        // Depending on whether reportString includes _id prooperty, use appropriate Report constructor
        if (report.getString("_id") != "") {
             newReport = new Report(report.getString("_id"), report.getString("processId"), report.getString("mainClass"),
                    report.getString("processStateChange"), report.getString("host"), report.getString("timeStamp"), report.getString("status"));
        }
        else {
            newReport = new Report(report.getString("processId"), report.getString("mainClass"),
                    report.getString("processStateChange"), report.getString("host"), report.getString("timeStamp"), report.getString("status"));
        }

        return newReport;
    }

    /**
     * Builds an Audit object from a given string
     */
    public Audit buildAudit(String auditString) {

        JsonReader jsonReader = Json.createReader(new StringReader(auditString));
        JsonObject audit = jsonReader.readObject();
        jsonReader.close();

        Audit newAudit;

        // Depending on whether reportString includes _id prooperty, use appropriate Report constructor
        if (audit.getString("_id") == "") {
            newAudit = new Audit(audit.getString("processId"), audit.getString("host"), audit.getString("mainClass"), audit.getString("processStateChange"), audit.getString("timeStamp"),
                    audit.getString("oldStatus"), audit.getString("newStatus"), audit.getString("movedTime"),audit.getString("userAcf2Id"));
        }
        else {
            newAudit = new Audit(audit.getString("_id"), audit.getString("processId"), audit.getString("host"), audit.getString("mainClass"), audit.getString("processStateChange"), audit.getString("timeStamp"),
                    audit.getString("oldStatus"), audit.getString("newStatus"), audit.getString("movedTime"),audit.getString("userAcf2Id"));
        }

        return newAudit;
    }

    /**
     * Builds a mongo-style Document from a given Report
     */
    public Document reportToDocument(Report report) {
        Document document = new Document();

        // If _id is included in report, add it to document
        if (!report._id.equals("")) {
            document.append("_id", new ObjectId(report._id));
        }

        document.append("processId", report.processId);
        document.append("host", report.host);
        document.append("mainClass", report.mainClass);
        document.append("processStateChange", report.processStateChange);
        document.append("status", report.status);
        document.append("timeStamp", report.timeStamp);

        return document;
    }

    /**
     * Builds a mongo-style Document from a given Audit
     */
    public Document auditToDocument(Audit audit) {
        Document document = new Document();

        // If _id is included in audit, add it to document
        if (!audit._id.equals("")) {
            document.append("_id", new ObjectId(audit._id));
        }

        document.append("processId", audit.processId);
        document.append("host", audit.host);
        document.append("mainClass", audit.mainClass);
        document.append("processStateChange", audit.processStateChange);
        document.append("oldStatus", audit.oldStatus);
        document.append("newStatus", audit.newStatus);
        document.append("timeStamp", audit.timeStamp);
        document.append("movedTime", audit.movedTime);
        document.append("userAcf2Id", audit.userId);

        return document;
    }

    /**
     * Builds a mongo-style Document from a given User
     */
    public Document userToDocument(User user) {
        Document document = new Document();

        //document.append("_id", new ObjectId(user._id));
        document.append("acf2Id", user.acf2Id);
        document.append("password", user.password);
        document.append("access", user.accessLevel);

        return document;
    }

    /**
     * Builds a Report from a given mongo-style Document
     */
    public Report documentToReport(Document document) {
        Report report = new Report();

        report._id = ((ObjectId)document.get("_id")).toHexString();
        report.processId = document.getString("processId");
        report.mainClass = document.getString("mainClass");
        report.host = document.getString("host");
        report.processStateChange = document.getString("processStateChange");
        report.status = document.getString("status");
        report.timeStamp = document.getString("timeStamp");

        return report;
    }

    /**
     * Builds an Audit from a given mongo-style Document
     */
    public Audit documentToAudit(Document document) {
        Audit audit = new Audit();

        audit._id = ((ObjectId)document.get("_id")).toHexString();
        audit.processId = document.getString("processId");
        audit.host = document.getString("host");
        audit.mainClass = document.getString("mainClass");
        audit.processStateChange = document.getString("processStateChange");
        audit.timeStamp = document.getString("timeStamp");
        audit.oldStatus = document.getString("oldStatus");
        audit.newStatus = document.getString("newStatus");
        audit.movedTime = document.getString("movedTime");
        audit.userId = document.getString("userAcf2Id");

        return audit;
    }

    /**
     * Builds Response from given status code and data
     */
    public Response responseBuilder(int code, String data, String allowedMethods) {

        return Response.status(code)
                .entity(data)
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "GET,PUT,POST")
                .header("Access-Control-Allow-Credentials", "true")
                .allow("OPTIONS").build();
    }


}
