package io.enforcer.xwing;

/**
 * Created by kaveh on 2/24/2015.
 */
public class MonitoredProcessDiff {

    private Integer processId;
    private String mainClass;
    private ProcessStateChanges stateChange;

    public MonitoredProcessDiff(Integer processId, String mainClass, ProcessStateChanges stateChange) {
        this.processId = processId;
        this.mainClass = mainClass;
        this.stateChange = stateChange;
    }

    public Integer getProcessId() {
        return processId;
    }

    public String getMainClass() {
        return mainClass;
    }

    public ProcessStateChanges getStateChange() {
        return stateChange;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MonitoredProcessDiff that = (MonitoredProcessDiff) o;

        if (!mainClass.equals(that.mainClass)) return false;
        if (!processId.equals(that.processId)) return false;
        if (stateChange != that.stateChange) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = processId.hashCode();
        result = 31 * result + mainClass.hashCode();
        result = 31 * result + stateChange.hashCode();
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MonitoredProcessDiff{");
        sb.append("processId=").append(processId);
        sb.append(", mainClass='").append(mainClass).append('\'');
        sb.append(", stateChange=").append(stateChange);
        sb.append('}');
        return sb.toString();
    }
}
