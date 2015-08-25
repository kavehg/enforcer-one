package io.enforcer.deathstar.pojos;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by herret2 on 8/24/2015.
 * A user navigates the app and creates REST calls for Reports and Audits
 */
@XmlRootElement
public class User {

    public Integer userId;
    public String acf2Id;
    public String password;
    public Integer accessLevel;
    //public String logins;
    //public String logoffs;

    public User() {} // JAX-RS requirement

    public User(Integer userId, String acf2Id, String password, Integer accessLevel) {
        this.userId = userId;
        this.acf2Id = acf2Id;
        this.password = password;
        this.accessLevel = accessLevel;
        //this.logins = logins;
        //this.logoffs = logoffs;
    }

    /*@Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("User{");
        sb.append("userId=").append(processId);
        sb.append(", mainClass='").append(mainClass).append('\'');
        sb.append(", processStateChange='").append(processStateChange).append('\'');
        sb.append(", host='").append(host).append('\'');
        sb.append(", timeStamp='").append(timeStamp).append('\'');
        sb.append('}');
        return sb.toString();
    }*/

}


