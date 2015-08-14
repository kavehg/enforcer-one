/**
 * WebSocketService - opens a websocket and waits for messages to arrive.  Adds them to an array where they
 * are acceessed by controllers.
 */
angular.module('Enforcer.Common')
    .service('WebSocketService', ['$q', '$rootScope', function($q, $rootScope) {

        var ws = new WebSocket("ws://localhost:9090");

        var reports = [];

        var statuses = [];

        // Creates a promise and
        function getReport() {

            var deferred = $q.defer();

            if(reports.length > 0) {
                deferred.resolve(reports.shift());
            }
            else {
                deferred.reject("No reports!");
            }

            return deferred.promise;
        }

        function getStatus() {

            var deferred = $q.defer();

            if(statuses.length > 0) {
                deferred.resolve(statuses.shift());
            }
            else {
                deferred.reject("No statuses!");
            }

            return deferred.promise;
        }


        // Broadcast that connection has been opened
        ws.onopen = function() {
            console.log("Socket has been opened");
            $rootScope.$broadcast('connectionOpen');
        };

        // When websocket receives a message
        ws.onmessage = function(message) {
            console.log("Received message: " + JSON.stringify(message.data));

            var theMsg = JSON.parse(message.data);

            if(isStatus(theMsg)){
                statuses.push(theMsg);
                $rootScope.$broadcast('statusReceived');
            }
            else {
                reports.push(theMsg);
                $rootScope.$broadcast('reportReceived');
            }


        };

        // Check if message is a status. If not, then it is a report.
        function isStatus(message) {
            if (message.xwingId != null)
                return true;

            return false;
        }

        return {

            getReport: getReport,
            getStatus: getStatus

        };



    }]);