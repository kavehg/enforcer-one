/**
 * ReportService - contains the list of New reports to be displayed/manipulated on the dashboard
 *               - allows controller to add a report, much like the StatusController when an x-wing goes down
 *               - also broadcasts when a report is received, allowing a controller to retrieve it
 */
angular.module('Enforcer.Dashboard')
    .service('ReportService', ['$q', '$rootScope', function($q, $scope, $rootScope) {

        /* ========================================================================================
         * New Reports Queue
         * ===================================================================================== */

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

        /* ========================================================================================
         * Functions
         * ===================================================================================== */

        // Compares reports based on processId and host
        /*function reportCompareNew(report) {

            if (newReports.length > 0) {
                for (var i = 0; i < newReports.length; i++) {
                    if (newReports[i].processId == report.processId && newReports[i].host == report.host)
                        return i;
                }
                return -1;
            }

            return -1;
        }

        // Creates a promise and resolves it with available data, otherwise reject the promise.
        function getReport() {

            var deferred = $q.defer();

            if(newReports.length > 0) {
                deferred.resolve(newReports.shift()); //Treats newReports[] like  FIFO queue
            }
            else {
                deferred.reject("No reports!");
            }

            return deferred.promise;
        }*/

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

        // Receives a report and saves it if new
        function addReport(report) {
            var deferred = $q.defer();

            if (reportCompare(report) == -1) {
                reports.push(report);
                deferred.resolve('reportHandled');
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

            //getReport: getReport,
            getReports: getReports,
            addReport: addReport,
            moveReport: moveReport
        };

    }]);