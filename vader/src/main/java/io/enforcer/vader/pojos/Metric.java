package io.enforcer.vader.pojos;

/**
 * Created by SCHIAJ2 on 6/23/2016.
 */

/**
 * Holds information about the Metric data from the Graphite server
 */
public class Metric {

    /** ========================================================================================
     ** Variables
     ** ===================================================================================== */
    public String target;
    public float[] datapoints;

    /** ========================================================================================
     ** Constructors
     ** ===================================================================================== */

    public Metric(String target, float[] datapoints) {
        this.target = target;
        this.datapoints = datapoints;
    }

    /**
     * Assume only 5 datapoints to be analyzed
     *
     */
    public Metric(){
        this.target = null;
        this.datapoints = new float[5];

    }

}
