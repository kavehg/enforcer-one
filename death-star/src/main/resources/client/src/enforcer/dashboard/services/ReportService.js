/**
 * ReportService - contains the list of New reports to be displayed/manipulated on the dashboard
 *               - allows controller to add a report, much like the StatusController when an x-wing goes down
 *               - also broadcasts when a report is received, allowing a controller to retrieve it
 */
angular.module('Enforcer.Dashboard')
    .service('ReportService', ['$resource', '$q', '$log', '$rootScope', function($resource, $q, $log, $scope, $rootScope) {

        /** ========================================================================================
         ** Report Object
         ** ===================================================================================== */
        var Report = function(data) {
            this.processId = data.header;
            this.host = data.headerDetail;
            this.mainClass = data.classPath;
            this.processStateChange = data.detail;
            this.status = data.status;
            this.timeStamp = data.timeStamp;
        }

        /** ========================================================================================
         ** Reports
         ** ===================================================================================== */

        var reports = [
            /*{
                "processId" : "901",
                "host" : "tovalrs01",
                "mainClass" : "Valuation Engine",
                "processStateChange" : "STOPPED",
                "status" : "New",
                "timeStamp" : "2015/07/17 16:55:32"
            },
            {
                "processId" : "1875",
                "host" : "tovalrs02",
                "mainClass" : "Trade Service",
                "processStateChange" : "STOPPED",
                "status" : "New",
                "timeStamp" : "2015/08/25 16:55:32"
            },
            {
                "processId" : "1234",
                "host" : "tovalrs03",
                "mainClass" : "Derived Market Data Service",
                "processStateChange" : "STOPPED",
                "status" : "Acknowledged",
                "timeStamp" : "2015/08/17 16:55:32"
            },
            {
                "processId" : "1000",
                "host" : "tovalrs01",
                "mainClass" : "Valuation Engine",
                "processStateChange" : "STOPPED",
                "status" : "Acknowledged",
                "timeStamp" : "2015/08/30 19:55:32"
            },
            {
                "processId" : "2424",
                "host" : "tovalrs02",
                "mainClass" : "Valuation Engine",
                "processStateChange" : "STOPPED",
                "status" : "Escalated",
                "timeStamp" : "2015/08/17 16:55:32"
            },
            {
                "processId" : "357",
                "host" : "tovalrs03",
                "mainClass" : "Trade Service",
                "processStateChange" : "STOPPED",
                "status" : "Escalated",
                "timeStamp" : "2015/08/31 17:55:32"
            },
            {
                "processId" : "98411",
                "host" : "tovalrs01",
                "mainClass" : "Valuation Engine",
                "processStateChange" : "STOPPED",
                "status" : "History",
                "timeStamp" : "2015/07/12 09:41:23"
            },
            {
                "processId" : "1245",
                "host" : "tovalrs02",
                "mainClass" : "Valuation Engine",
                "processStateChange" : "STOPPED",
                "status" : "History",
                "timeStamp" : "2015/07/29 12:52:48"
            }*/

        ];

        /** ========================================================================================
         ** API Setup
         ** ===================================================================================== */

        var reportAPI = $resource('http://localhost:8000/api/persistence/reports', null, {
            get: { // for retrieving a single report
                method: 'GET',
                isArray: false,
                url: 'http://localhost:8000/api/persistence/reports/:reportId'
            },
            getAll: { // for retrieving all reportsS
                method: 'GET',
                isArray: true,
                headers: {
                    'Accept': 'application/json, text/javascript',
                    'Content-Type': 'application/json; charset=utf-8'
                },
                url: 'http://localhost:8000/api/persistence/reports'
            },
            post: { // for creating NEW reports
                method: 'POST',
                isArray: false,
                headers: {
                    'Accept': 'application/json, text/javascript',
                    'Content-Type': 'application/json; charset=utf-8'
                },
                url: 'http://localhost:8000/api/persistence/reports/:reportId'
            },
            put: { // for updating reports
                method: 'PUT',
                isArray: false,
                headers: {
                    'Accept': 'application/json, text/javascript',
                    'Content-Type': 'application/json; charset=utf-8'
                },
                url: 'http://localhost:8000/api/persistence/reports/:reportId'
            }
        });

        /** ========================================================================================
         ** Functions
         ** ===================================================================================== */

        // Creates a promise and resolves it with available data, otherwise reject the promise.
        function getReport(reportId) {

            var deferred = $q.defer();

            reportAPI.get({reportId: reportId}).$promise.then(
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

            if (reports.length > 0){
                deferred.resolve(reports);
            }
            else {
                deferred.resolve('There are no reports.');
            }

            // MongoDB
            /*reportAPI.getAll().$promise.then(
                function(reports) {
                    $log.info('Successfully retrieved reports: ' + reports.length);
                    deferred.resolve(reports);
                }, function(err) {
                    $log.error('Failed to retrieve report: ' + err);
                    deferred.reject(err);
                }
            );*/

            return deferred.promise;
        }

        // Receives a report and saves it to database if new
        function addReport(report) {

            var deferred = $q.defer();

            if (reportCompare(report) == -1) {
                reports.push(report);
                deferred.resolve('reportHandled');
            }
            else {
                deferred.reject('report already exists');
            }

            // MongoDB
            /*reportAPI.post({reportId: report.processId}, report).$promise.then(
                function(data) {
                    $log.info('ReportService: Successfully posted report: ' + data.processId);
                    //reports.push(data);
                    deferred.resolve(data);
                }, function(err) {
                    $log.error('Failed to post report: ' + err);
                    deferred.reject(err);
                }
            );*/

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

            // MongoDB
            /*reportAPI.put({reportId: report._id}, report).$promise.then(
                function(data) {
                    $log.info('ReportService: Successfully moved report: ' + data.processId);
                    //reports.push(data);
                    deferred.resolve(data);
                }, function(err) {
                    $log.error('Failed to move report: ' + err);
                    deferred.reject(err);
                }
            );*/

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

        // Compares reports based on processId and host and processState and mainClass
        function reportCompare(report) {

            if (reports.length > 0) {
                for (var i = 0; i < reports.length; i++) {
                    if (reports[i].processId == report.processId && reports[i].host == report.host && reports[i].processStateChange == report.processStateChange && reports[i].mainClass == report.mainClass)
                        return i;
                }
                return -1;
            }
            return -1;
        }

        function updateReports(report) {
            var deferred = $q.defer();

            //if the object is not a Report object, convert then try again
            if (report.processId != null) {
                if (reports.length > 0) {
                    for (var i = 0; i < reports.length; i++) {
                        if (reports[i].processId == report.processId && reports[i].host == report.host && reports[i].processStateChange == report.processStateChange && reports[i].mainClass == report.mainClass) {
                            reports[i] = report;
                            //ToDo: The promise is not returned when the card is moved in the dashboard for some reason
                            deferred.reject('Report Service: Report Updated');
                            return deferred.promise;
                        }
                    }
                    //if the report cannot be updated it does not exist and must be added
                    reports.push(report);
                    deferred.resolve('Report Service: New Report Added');
                    return deferred.promise;
                    //return -1;
                }
                //if the report list is empty add the new metric
                else {
                    reports.push(report);
                    deferred.resolve('Report Service: New Report Added');
                    return deferred.promise;
                    //return -1;
                }
            }
            //assuming the object is a card object, try to convert to metric object and update
            else {
                var newReport = new Report(report);
                if (newReport.processId != null && report.type != "Metric") {
                    updateReports(newReport);
                }
                else {
                 deferred.reject('Report Service: Invalid Report object, could not update reports');
                 return deferred.promise;
                }
            }

            return deferred.promise;
        }

        /** ========================================================================================
         ** Return
         ** ===================================================================================== */

        // Calls to functions inside this service from external controllers
        return {

            getReport: getReport,
            getReports: getReports,
            addReport: addReport,
            moveReport: moveReport,
            updateReports: updateReports
        };

    }]);