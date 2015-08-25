package io.enforcer.deathstar.pojos;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by herret2 on 8/24/2015.
 *
 */
@XmlRootElement
public class Audit {

    public Integer processId;
    public String mainClass;
    public String processStateChange;
    public String host;
    public String timeStamp;
    public String oldStatus;
    public String newStatus;
    public String userId;
    public String movedTime;

    public Audit() {} // JAX-RS requirement

    public Audit(Integer processId, String mainClass, String processStateChange, String host, String timeStamp,
    String oldStatus, String newStatus, String movedTime, String userId) {

        this.processId = processId;
        this.mainClass = mainClass;
        this.processStateChange = processStateChange;
        this.host = host;
        this.timeStamp = timeStamp;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.movedTime = movedTime;
        this.userId = userId;

    }
}
