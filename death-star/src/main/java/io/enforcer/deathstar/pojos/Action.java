package io.enforcer.deathstar.pojos;

/**
 * Created by kavehg on 7/31/2015.
 *
 * Represents an action performed on a report.
 *
 * A report is initially in 'RECEIVED' state. It can either transition
 * to 'ESCALATED' or 'ACKNOWLEDGED'. Once ack'ed it will be 'ARCHIVED'.
 */
public class Action {

    private String peformedByUser;

    private ReportState newState;

    private Long timeStamp;

    public Action(ReportState newState, String peformedByUser, Long timeStamp) {
        this.newState = newState;
        this.peformedByUser = peformedByUser;
        this.timeStamp = timeStamp;
    }

    public ReportState getNewState() {
        return newState;
    }

    public String getPeformedByUser() {
        return peformedByUser;
    }

    public Long getTimeStamp() {
        return timeStamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Action action = (Action) o;

        if (!peformedByUser.equals(action.peformedByUser)) return false;
        if (newState != action.newState) return false;
        return timeStamp.equals(action.timeStamp);

    }

    @Override
    public int hashCode() {
        int result = peformedByUser.hashCode();
        result = 31 * result + newState.hashCode();
        result = 31 * result + timeStamp.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Action{" +
                "newState=" + newState +
                ", peformedByUser='" + peformedByUser + '\'' +
                ", timeStamp=" + timeStamp +
                '}';
    }
}
