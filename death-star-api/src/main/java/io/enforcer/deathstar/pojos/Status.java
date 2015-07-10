package io.enforcer.deathstar.pojos;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by kaveh on 2/24/2015.
 *
 * A status is periodically submitted by all x-wings and
 * is comparable to a heartbeat by which the x-wings
 * regularly check in with the death star and notify it
 * that they are still monitoring the server that they
 * are running on.
 */
@XmlRootElement
public class Status {

    public int processId;
    public String host;
    public String timeStamp;

    public Status() {} // JAX-RS requirement

    public Status(int processId, String host, String timeStamp) {
        this.processId = processId;
        this.host = host;
        this.timeStamp = timeStamp;
    }

    public int getProcessId() {
        return processId;
    }

    public String getHost() {
        return host;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Status status = (Status) o;

        if (processId != status.processId) return false;
        if (!host.equals(status.host)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = processId;
        result = 31 * result + host.hashCode();
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Status{");
        sb.append("processId=").append(processId);
        sb.append(", host='").append(host).append('\'');
        sb.append(", timeStamp='").append(timeStamp).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
