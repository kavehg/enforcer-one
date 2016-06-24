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
    public float threshold;

    /** ========================================================================================
     ** Constructors
     ** ===================================================================================== */

    public MetricRequest(String url, float threshold) {
        this.url = url;
        this.threshold = threshold;
    }
}
