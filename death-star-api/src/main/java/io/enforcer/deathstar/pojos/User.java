package io.enforcer.deathstar.pojos;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by herret2 on 8/24/2015.
 * A user navigates the app and creates REST calls for Reports and Audits
 */
@XmlRootElement
public class User {

    /** ========================================================================================
     ** Variables
     ** ===================================================================================== */

    public String _id; // For use with MongoDB
    public String acf2Id;
    public String password;
    public String accessLevel; //Todo: Make Enum?
    public String[] logins;
    public String[] logoffs;

    /** ========================================================================================
     ** Constructors
     ** ===================================================================================== */

    public User() {} // JAX-RS requirement

    public User(String acf2Id, String password, String accessLevel) {
        this._id = null;
        this.acf2Id = acf2Id;
        this.password = password;
        this.accessLevel = accessLevel;
    }

    // Takes additional _id argument for when Audit is created after database entry
    public User(String id, String acf2Id, String password, String accessLevel) {
        this._id = id;
        this.acf2Id = acf2Id;
        this.password = password;
        this.accessLevel = accessLevel;
    }

    /** ========================================================================================
     ** Accessors/Mutators
     ** ===================================================================================== */

    public String getId() { return _id; }

    public void setID(String id) { _id = id; }
}


