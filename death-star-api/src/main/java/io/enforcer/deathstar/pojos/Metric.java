package io.enforcer.deathstar.pojos;

import javax.xml.bind.annotation.XmlRootElement;
/**
 * Created by SCHIAJ2 on 6/23/2016.
 */

/**
 * Holds information about the Metric data from the Graphite server
 */
@XmlRootElement
public class Metric {

    /** ========================================================================================
     ** Variables
     ** ===================================================================================== */
    public String target;
    public float[] datapoints;
    public float average;
    public float threshold;
    public String timeStamp;
    public String metricDetail;
    public String status;

    /** ========================================================================================
     ** Constructors
     ** ===================================================================================== */


    public Metric(String target, float[] datapoints, float average, float threshold, String timeStamp, String metricDetail,  String status) {
        this.target = target;
        this.datapoints = datapoints;
        this.average = average;
        this.threshold = threshold;
        this.timeStamp = timeStamp;
        this.metricDetail = metricDetail;
        this.status = status;
    }

    /**
     * Assume only 5 datapoints to be analyzed
     *
     */
    public Metric(){
        this.target = null;
        this.datapoints = new float[5];
        this.average = 0;
        this.threshold = 0;
        this.timeStamp = null;
        this.metricDetail = null;
        this.status = null;
    }

    //ToDo: add toSting Override

}