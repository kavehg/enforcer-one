package io.enforcer.vader.pojos;

/**
 * Created by SCHIAJ2 on 6/23/2016.
 */

/**
 * Data structure for request sent in by user form the dashboard
 */

public class MetricRequest {

    /** ========================================================================================
     ** Variables
     ** ===================================================================================== */

    public String url;
    public String metricDetail;
    public float threshold;

    /** ========================================================================================
     ** Constructors
     ** ===================================================================================== */

    public MetricRequest(String url, String metricDetail, float threshold) {
        this.url = url;
        this.metricDetail = metricDetail;
        this.threshold = threshold;
    }
}
