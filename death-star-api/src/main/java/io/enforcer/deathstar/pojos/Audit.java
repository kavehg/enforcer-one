package io.enforcer.deathstar.pojos;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by herret2 on 8/24/2015.
 *
 */
@XmlRootElement
public class Audit {

    /** ========================================================================================
     ** Variables
     ** ===================================================================================== */

    public String _id; // For use with MongoDB
    public String processId;
    public String host;
    public String mainClass;
    public String processStateChange;
    public String timeStamp;
    public String oldStatus;
    public String newStatus;
    public String movedTime;
    public String userId;

    /** ========================================================================================
     ** Constructors
     ** ===================================================================================== */

    public Audit() {} // JAX-RS requirement

    public Audit(String processId, String mainClass, String processStateChange, String host, String timeStamp,
    String oldStatus, String newStatus, String movedTime, String userId) {
        this._id = null;
        this.processId = processId;
        this.host = host;
        this.mainClass = mainClass;
        this.processStateChange = processStateChange;
        this.timeStamp = timeStamp;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.movedTime = movedTime;
        this.userId = userId;
    }

    // Takes additional _id argument for when Audit is created after database entry
    public Audit(String id, String processId, String mainClass, String processStateChange, String host, String timeStamp,
                 String oldStatus, String newStatus, String movedTime, String userId) {
        this._id = id;
        this.processId = processId;
        this.host = host;
        this.mainClass = mainClass;
        this.processStateChange = processStateChange;
        this.timeStamp = timeStamp;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.movedTime = movedTime;
        this.userId = userId;
    }

    /** ========================================================================================
     ** Accessors/Mutators
     ** ===================================================================================== */

    public String getId() { return _id; }

    public void setID(String id) { _id = id; }
}
