package io.enforcer.xwing;

/**
 * Created by kaveh on 2/23/2015.
 *
 * This class represents a running process that we are currently
 * watching.
 */
public class MonitoredProcess {

    private Integer processId;
    private String mainClass;

    public MonitoredProcess(Integer processId, String mainClass) {
        this.processId = processId;
        this.mainClass = mainClass;
    }

    public Integer getProcessId() {
        return processId;
    }

    public String getMainClass() {
        return mainClass;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MonitoredProcess that = (MonitoredProcess) o;

        if (!mainClass.equals(that.mainClass)) return false;
        if (!processId.equals(that.processId)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = processId.hashCode();
        result = 31 * result + mainClass.hashCode();
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MonitoredProcess{");
        sb.append("processId=").append(processId);
        sb.append(", mainClass='").append(mainClass).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
