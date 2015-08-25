/**
 * ReportService - contains the list of New reports to be displayed/manipulated on the dashboard
 *               - allows controller to add a report, much like the StatusController when an x-wing goes down
 *               - also broadcasts when a report is received, allowing a controller to retrieve it
 */
angular.module('Enforcer.Dashboard')
    .service('ReportService', ['$resource', '$q', '$log', '$rootScope', function($resource, $q, $log, $scope, $rootScope) {

        /** ========================================================================================
         ** New Reports Queue
         ** ===================================================================================== */

        var newReports = [];

        var reports = [
            {
                "processId" : 1875,
                "host" : "tovalrs01",
                "mainClass" : "Valuation Engine",
                "processStateChange" : "STOPPED",
                "status" : "New",
                "timeStamp" : "2015/07/17 16:55:32"
            },
            {
                "processId" : 2443,
                "host" : "tovalrs02",
                "mainClass" : "Pricing Engine",
                "processStateChange" : "STOPPED",
                "status" : "New",
                "timeStamp" : "2015/07/17 16:55:32"
            },
            {
                "processId" : 2901,
                "host" : "tovalrs02",
                "mainClass" : "Pricing Engine",
                "processStateChange" : "STOPPED",
                "status" : "New",
                "timeStamp" : "2015/07/17 16:55:32"
            },
            {
                "processId" : 4443,
                "host" : "tovalrs05",
                "mainClass" : "Reference Data Service",
                "processStateChange" : "STOPPED",
                "status" : "New",
                "timeStamp" : "2015/07/17 16:55:32"
            },
        //acknowledged
            {
                "processId" : 1965,
                "host" : "tovalrs03",
                "mainClass" : "Trade Service",
                "processStateChange" : "STARTED",
                "status" : "Acknowledged",
                "timeStamp" : "2015/07/17 16:55:32"
            },
            {
                "processId" : 765,
                "host" : "tovalrs02",
                "mainClass" : "Pricing Engine",
                "processStateChange" : "STOPPED",
                "status" : "Acknowledged",
                "timeStamp" : "2015/07/17 16:55:32"
            },
         //escalated
            {
                "processId" : 8765,
                "host" : "tovalrs04",
                "mainClass" : "Market Data Service",
                "processStateChange" : "STOPPED",
                "status" : "Escalated",
                "timeStamp" : "2015/07/17 16:55:32"
            },
        //history
            {
                "processId" : 8745,
                "host" : "tovalrs05",
                "mainClass" : "Reference Data Service",
                "processStateChange" : "STOPPED",
                "status" : "History",
                "timeStamp" : "2015/07/17 16:55:32"
            }
        ];

        /** ========================================================================================
         ** API
         ** ===================================================================================== */

        var reportAPI = $resource('http://localhost:8000/api/persistence/reports', null, {
            get: {
                method: 'GET',
                isArray: false,
                url: 'http://localhost:8000/api/persistence/report/:reportId'
            },
            post: { // for creating NEW pricers (will validate it is actually new on server)
                method: 'POST',
                isArray: false,
                url: 'http://localhost:8000/api/persistence/report/:reportId'
            },
            put: { // for creating NEW pricers (will validate it is actually new on server)
                method: 'PUT',
                isArray: false,
                url: 'http://localhost:8000/api/persistence/report/:reportId'
            }
        });


        /** ========================================================================================
         ** Functions
         ** ===================================================================================== */

        // Creates a promise and resolves it with available data, otherwise reject the promise.
        function getReport() {

            var deferred = $q.defer();

            reportAPI.get({reportId: 1299}).$promise.then(
                function(report) {
                    $log.info('Successfully retrieved report: ' + report.processId);
                    deferred.resolve(report);
                }, function(err) {
                    $log.error('Failed to retrieve report: ' + err);
                    deferred.reject(err);
                }
            );

            return deferred.promise;
        }

        // Creates a promise and resolves it with available data, otherwise reject the promise.
        function getReports() {

            var deferred = $q.defer();

            if(reports.length > 0) {
                deferred.resolve(reports);
            }
            else {
                deferred.reject("No reports!");
            }

            return deferred.promise;
        }

        // Receives a report and saves it to database if new
        function addReport(report) {
            var deferred = $q.defer();

            var newReport = {
                    "processId": report.processId+110,
                    "mainClass": report.mainClass,
                    "processStateChange": report.processStateChange,
                    "host": report.host + "abc",
                    "timeStamp": report.timeStamp
            };

            if (reportCompare(newReport) == -1) {
                reportAPI.post({reportId: newReport.processId}, newReport).$promise.then(
                    function(data) {
                        $log.info('ReportService: Successfully posted report: ' + data.processId);
                        reports.push(data);
                        deferred.resolve(data);
                    }, function(err) {
                        $log.error('Failed to post report: ' + err);
                        deferred.reject(err);
                    }
                );
            }
            else {
                deferred.reject("Error adding report.")
            }

            return deferred.promise;
        }

        // Receives a report, replaces old version in list with new report
        function moveReport(report) {
            var deferred = $q.defer();

            if (reportCompare(report) > -1) {
                if (swapReports(report)) {
                    deferred.resolve('reportsModified');
                }
                else {
                    deferred.reject("Error moving report.")
                }
            }
            else {
                deferred.reject("Report doesn't exist to be moved")
            }

            return deferred.promise;
        }

        // Swaps a pre-existing report with an updated version
        function swapReports(newReport) {

            var index = reportCompare(newReport);

            // Report exists, swap it
            if(index > -1)
                reports[index] = newReport;
            else // Report doesn't exist, add it
                reports.push(newReport);

            return true;

        }

        // Compares reports based on processId and host
        function reportCompare(report) {

            if (reports.length > 0) {
                for (var i = 0; i < reports.length; i++) {
                    if (reports[i].processId == report.processId && reports[i].host == report.host)
                        return i;
                }
                return -1;
            }
            return -1;
        }

        // Calls to functions inside this service from external controllers
        return {

            getReport: getReport,
            getReports: getReports,
            addReport: addReport,
            moveReport: moveReport
        };

    }]);