package io.enforcer.deathstar.pojos;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by kavehg on 2/24/2015.
 *
 * A report is submitted by an x-wing to notify the death star
 * that a process on its host has changed state.
 */
@XmlRootElement
public class Report {

    public Integer processId;
    public String mainClass;
    public String processStateChange;
    public String host;
    public String timeStamp;

    public Report() {} // JAX-RS requirement

    public Report(Integer processId, String mainClass, String processStateChange, String host, String timeStamp) {
        this.processId = processId;
        this.mainClass = mainClass;
        this.processStateChange = processStateChange;
        this.host = host;
        this.timeStamp = timeStamp;
    }

    public Integer getProcessId() {
        return processId;
    }

    public String getMainClass() {
        return mainClass;
    }

    public String getProcessStateChange() {
        return processStateChange;
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

        Report report = (Report) o;

        if (!host.equals(report.host)) return false;
        if (!mainClass.equals(report.mainClass)) return false;
        if (!processId.equals(report.processId)) return false;
        if (!processStateChange.equals(report.processStateChange)) return false;
        if (!timeStamp.equals(report.timeStamp)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = processId.hashCode();
        result = 31 * result + mainClass.hashCode();
        result = 31 * result + processStateChange.hashCode();
        result = 31 * result + host.hashCode();
        result = 31 * result + timeStamp.hashCode();
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Report{");
        sb.append("processId=").append(processId);
        sb.append(", mainClass='").append(mainClass).append('\'');
        sb.append(", processStateChange='").append(processStateChange).append('\'');
        sb.append(", host='").append(host).append('\'');
        sb.append(", timeStamp='").append(timeStamp).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
