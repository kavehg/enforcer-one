/**
 * WebSocketService - opens a websocket and waits for messages to arrive.  Adds them to an array where they
 * are acceessed by controllers.
 */
angular.module('Enforcer.Common')
    .service('WebSocketService', ['$q', '$rootScope', function($q, $rootScope) {

        var ws = new WebSocket("ws://localhost:9090");

        var reports = [];

        var statuses = [];

        var metrics = [];

        // Creates a promise and resolves it with available data, otherwise reject the promise.
        function getReport() {

            var deferred = $q.defer();

            if(reports.length > 0) {
                deferred.resolve(reports.shift()); // Treats reports[] like a FIFO queue
            }
            else {
                deferred.reject("No reports!");
            }

            return deferred.promise;
        }

        // Creates a promise and resolves it with available data, otherwise reject the promise.
        function getStatus() {

            var deferred = $q.defer();

            if(statuses.length > 0) {
                deferred.resolve(statuses.shift()); // Treats statuses[] like a FIFO queue
            }
            else {
                deferred.reject("No statuses!");
            }

            return deferred.promise;
        }


        function getMetric() {

            var deferred = $q.defer();

            if (metrics.length > 0) {
                deferred.resolve(metrics.shift());
            }
            else {
                deferred.reject('No metrics!');
            }

            return deferred.promise;
        }


        // Broadcast that connection has been opened
        ws.onopen = function() {
            console.log("Socket has been opened");
            $rootScope.$broadcast('connectionOpen');
        };

        ws.onerror = function() {
            console.log("Error with socket");
            $rootScope.$broadcast('connectionError');
        };

        ws.onclose = function() {
            console.log("Socket has been closed");
            $rootScope.$broadcast('connectionClosed');
        };

        // When WebSocket receives a message
        ws.onmessage = function(message) {
            console.log("Received message: " + JSON.stringify(message.data));

            var theMsg = JSON.parse(message.data);

            // Depending on type of message received, broadcast on rootScope to alert controllers
            if(isStatus(theMsg)){
                statuses.push(theMsg);
                $rootScope.$broadcast('statusReceived');
            }
            else if (isMetric(theMsg)){
                metrics.push(theMsg);
                $rootScope.$broadcast('metricsReceived');
            }
            else if (isReport(theMsg)) {
                reports.push(theMsg);
                $rootScope.$broadcast('reportReceived');
            }

        };

        // Check if message is a status.
        function isStatus(message) {
            if (message.xwingId != null)
                return true;

            return false;
        }

        // Check if the message is a metric
        function isMetric(message) {
            if (message.target != null)
                return true;

            return false;
        }

        //Checks if message is report
        function isReport(message) {
            if (message.processId != null)
                return true;

            return false;
        }

        function sendMetricRequest(request) {
            ws.send(request);
        }

        // Calls to functions inside this service from external controllers
        return {

            getReport: getReport,
            getStatus: getStatus,
            getMetric: getMetric,
            sendMetricRequest: sendMetricRequest

        };



    }]);