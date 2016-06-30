//MetricService
// - Used for handling and storing metrics from server and modifier events from the dashboard

angular.module('Enforcer.Dashboard')
    .service('MetricService', ['$q', '$resource', '$log', '$rootScope', function($q, $resource, $scope, $log, $rootScope) {

        /** ========================================================================================
         ** Metric Object
         ** ===================================================================================== */
        //Constructor for Metric object
        var Metric = function (data) {
            this.target = data.classPath;
            this.datapoints = [];
            this.average = data.headerDetail;
            this.threshold = data.detail;
            this.timeStamp = data.timeStamp;
            this.metricDetail = data.header;
            this.status = data.status;
            this.type = "Metric";
        }

        //Constructor for MetricRequest object
        var MetricRequest = function (data) {
            this.url = data.url;
            this.metricDetail = data.metricDetail;
            this.threshold = data.threshold;
            this.type = data.type;
        }

        /** ========================================================================================
         ** Variables
         ** ===================================================================================== */
        //Most recent EDIT/REMOVE request
        var recentRequest;

        //Holds each metric request made from the metric request modal
        var metricRequests = [];

        //All metrics being monitored
        var metrics = [];

        /** ========================================================================================
         ** Functions
         ** ===================================================================================== */

        //Adds or updates Metric to Metrics array depending on whether or not it already exists
        function updateMetrics(metric) {
            var deferred = $q.defer();

            //if the object is not a Metric object, convert then try again
            if (metric.target != null) {
                if (metrics.length > 0) {
                    for (var i = 0; i < metrics.length; i++) {
                        if (metrics[i].target == metric.target && metrics[i].header == metric.header) {
                            //For when a metric is moved, retains datapoints
                            if (metric.datapoints.length == 0) {
                                metric.datapoints = metrics[i].datapoints;
                            }
                            metrics[i] = metric;
                            //ToDo: The promise is not returned when the card is moved in the dashboard for some reason
                            deferred.reject('Metric Service: Metric Updated');
                            return deferred.promise;
                        }
                    }
                    //if the metric cannot be updated it does not exist and must be added
                    metrics.push(metric);
                    deferred.resolve('Metric Service: New Metric Added');
                    return deferred.promise;
                }
                //if the metric list is empty add the new metric
                else {
                    metrics.push(metric);
                    deferred.resolve('Metric Service: New Metric Added');
                    return deferred.promise;
                }
            }
            //assuming the object is a card object, try to convert to metric object and update
            else {
                var newMetric = new Metric(metric);
                if (newMetric.target != null && metric.type != "Report") {
                    updateMetrics(newMetric);
                }
                else {
                 deferred.reject('Metric Service: Invalid Metric object, could not update metrics');
                 return deferred.promise;
                }
            }
            deferred.resolve("bleh");
           return deferred.promise;
        }

        //Adds or updates Metric Requests depending on whether or not it already exists
        function updateMetricRequests(metricRequest) {
            var deferred = $q.defer();
            if (metricRequests.length > 0) {
                for (var i = 0; i < metricRequests.length; i++) {
                    if (metricRequests[i].url == metricRequest.url && metricRequest.type !== "EDIT" && metricRequest.type !== "REMOVE") {
                        deferred.reject("Vader is already monitoring this Metric.");
                        return deferred.promise;
                    }
                    else if (metricRequests[i].metricDetail == metricRequest.metricDetail) {
                        if (metricRequest.type === "EDIT") {
                            metricRequests[i] = metricRequest;
                            recentRequest = metricRequests[i];
                            removeMetrics(metricRequest);
                            deferred.resolve("MetricService: Vader Updated")
                            return deferred.promise;
                        }
                        else if (metricRequest.type === "REMOVE") {
                            metricRequests.splice(i, 1);
                            deferred.resolve("MetricService: Vader Removed");
                            recentRequest = metricRequest;
                            removeMetrics(metricRequest);
                            return deferred.promise;
                        }
                        else
                        {
                            deferred.reject("This Vader is already in use. Please try a different name.");
                            return deferred.promise;
                        }
                    }
                }
                if (metricRequest.type === "ADD") {
                    metricRequests.push(metricRequest);
                    deferred.resolve("MetricService: Metric Request Added");
                    return deferred.promise;
                }
                else if (metricRequest.type === "EDIT") {
                    deferred.reject("Vader could not be edited. Make sure the Metric Detail matches the name of the Vader to be edited.");
                    return deferred.promise;
                }
                else if (metricRequest.type === "REMOVE") {
                    deferred.reject("Vader does not exist to be removed.")
                    return deferred.promise;
                }
            }
            else {
                metricRequests.push(metricRequest);
                deferred.resolve("MetricService: Metric Request Added");
                return deferred.promise;
            }
        }

        //When an EDIT or REMOVE request is made, query through metrics and remove anything that was being monitored
        function removeMetrics(req) {
            var remove = [];
            for (var i = 0; i < metrics.length; i++) {
                if (metrics[i].metricDetail === req.metricDetail) {
                    remove.push(i);
                }
            }
            for (var i = remove.length-1; i >= 0; i--) {
                metrics.splice(remove[i], 1);
            }
        }

        //Returns metrics array
        function getMetrics() {
            var deferred = $q.defer();

            if (metrics.length > 0) {
                deferred.resolve(metrics);
            }
            else {
                deferred.reject("There are no metrics.");
            }

            return deferred.promise;
        }

        //Returns metric requests if any exist
        function getMetricRequests() {
            var deferred = $q.defer();

            if (metricRequests.length > 0) {
                deferred.resolve(metricRequests);
            }
            else {
                deferred.reject("No metric requests have been made.");
            }

            return deferred.promise;
        }

        //Returns recentRequest which hold the most recent EDIT or REMOVE request
        function getRecentRequest() {
            var deferred = $q.defer();

            if (recentRequest != null) {
                deferred.resolve(recentRequest);
            }
            else {
                deferred.reject("No recent requests for edit or remove");
            }

            return deferred.promise;
        }

        //Find a single Metric
        function findMetric(data) {
            var deferred = $q.defer();

            var metric = data;
            if (metric.target == null) {
                metric = new Metric(metric);
            }

            if (metrics.length > 0) {
                for (var i = 0; i < metrics.length; i++) {
                    if (metrics[i].target == metric.target && metrics[i].metricDetail == metric.metricDetail) {
                        deferred.resolve(metrics[i]);
                        return deferred.promise;
                    }
                }
                deferred.reject("MetricService: Could not find Metric");
            }
            else { deferred.reject("MetricService: No Metrics!"); }

            return deferred.promise;
        }

        return {
            getMetrics: getMetrics,
            getMetricRequests: getMetricRequests,
            updateMetrics: updateMetrics,
            updateMetricRequests: updateMetricRequests,
            getRecentRequest: getRecentRequest,
            findMetric: findMetric
        };

    }]);