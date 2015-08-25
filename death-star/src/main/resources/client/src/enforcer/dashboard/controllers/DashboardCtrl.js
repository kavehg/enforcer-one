angular.module('Enforcer.Dashboard')
    .controller('DashboardCtrl', function($scope, $rootScope, $log, WebSocketService, ReportService, SettingsService, AuditService) {

        // init function
        var init = function() {

            $scope.settings = {
                "connection" : "None",
                "missingTime" : 20,
                "statusScanTime" : 5,
                "deathTime" : 2,
                "escalationTime" : 5,
                "autoEscalation" : true,
                "notificationToasts" : true
            };

            $scope.new = [];

            $scope.acknowledged = [];

            $scope.escalated = [];

            $scope.history = [];

            refreshSettings();

            refreshReports();

            // Check reports every $scope.settings.escalationTime
            setInterval(function () {
                reportScan();
            }, ($scope.settings.escalationTime * 60000));

            $('.modal-trigger').leanModal();

        };

        init();

        /** ========================================================================================
         ** Scope Variables
         ** ===================================================================================== */

        $scope.new = [];

        $scope.acknowledged = [];

        $scope.escalated = [];

        $scope.history = [];


        /** ========================================================================================
         ** Broadcast Listeners
         ** ===================================================================================== */

        // Listen for broadcast update when settings are changed
        $scope.$on('settingsChanged', function() {
            refreshSettings();
        });

        // Listen for broadcast update from the websocketservice when reports arrive
        $scope.$on('reportsChanged', function() {
            refreshReports();
        });


        /** ========================================================================================
         ** Functions
         ** ===================================================================================== */

        // Calls the SettingsService and retrrieves the updated settings
        function refreshSettings() {
            // If promise received is resolved
            SettingsService.getSettings().then(
                function(returnedSettings) {

                    $scope.received = true;
                    $scope.settings = returnedSettings;
                    log('DashboardCtrl: Settings Refreshed');

                }, function() {
                    $scope.received = false;
                    logError('DashboardCtrl: Settings Refreshed FAILED');
                }
            );
        }


        // Sends a report to the ReportService
        function addReport (report) {

            ReportService.addReport(report).then(
                function(returnedBroadcast) {
                    $rootScope.$broadcast('reportsChanged');
                    log('DashboardCtrl: Added Report');

                }, function() {
                    $scope.received = false;
                    logError('DashboardCtrl: Add Report FAILED');
                }
            );
        };

        // Calls the ReportService and retrieves any new reports
        function refreshReports() {

            // If promise received is resolved
            ReportService.getReports().then(
                function(returnedReports) {

                    // If report doesn't already exist, add to $scope.new
                    if (returnedReports.length > 0) {
                        allocateReports(returnedReports);
                    };

                    //addReport(returnedReports[0]);

                    log('DashboardCtrl: Reports Refreshed');

                }, function() {
                    $scope.received = false;
                    logError('DashboardCtrl: Reports Refresh FAILED');
                }
            );
        }

        function moveReport(report) {
            ReportService.moveReport(report).then(
                function(data) {
                    $rootScope.$broadcast('reportsChanged');
                    log('DashboardCtrl: Moved Report ' + data);

                }, function(err) {
                    $scope.received = false;
                    logError('DashboardCtrl: Move Report FAILED ' + err);
                }
            );
        }

        function allocateReports(reports) {

            // Reset arrays
            $scope.new.length = 0;
            $scope.acknowledged.length = 0;
            $scope.escalated.length = 0;
            $scope.history.length = 0;

            // Loop through reports and assign to arrays
            for(var i=0; i < reports.length; i++) {

                if (reports[i].status == 'New')
                    $scope.new.push(reports[i]);
                else if (reports[i].status == 'Acknowledged')
                    $scope.acknowledged.push(reports[i]);
                else if (reports[i].status == 'Escalated')
                    $scope.escalated.push(reports[i]);
                else if (reports[i].status == 'History')
                    $scope.history.push(reports[i]);
            }

            return true;

        }

        // Checks all of the New reports against time
        function reportScan() {

            var currentTime;
            var differential;

            if ($scope.new.length == 0)
            {
                return true;
            }

            for (var i=$scope.new.length-1; i >= 0; i--)
            {
                currentTime = new Date().getTime();
                var reportTime = new Date($scope.new[i].timeStamp).getTime();

                differential = currentTime - reportTime;

                if (differential > ($scope.settings.escalationTime * 60)) { // Convert escalation time in minutes to seconds
                    escalateReport($scope.new[i]);
                }
            }

            return true;
        }

        // Auto moves a report from New to Escalated after $scope.settings.escalationTime has passed
        function escalateReport(report) {

            var newAudit = createAudit(report, "Escalated", "Auto Escalation");
            addAudit(newAudit);

            $scope.$apply(function() {
                $scope.removeCard(report);
                report.status = "Escalated";
                moveReport(report);
            });
        }

        // Sends new audit to AuditService
        function addAudit(audit) {

            AuditService.addAudit(audit).then(
                function(auditAdded) {
                    log("DashboardCtrl: Audit Added");
                    $rootScope.$broadcast('auditTrailChanged');
                    return true;

                }, function() {
                    logError("SettingsCtrl: Audit Add FAILED");
                    return false;
                }
            );
        };

        // Creates an Audit item from a given report and newStatus
        function createAudit(report, newStatus, user) {

            var audit = {
                "processId" : report.processId,
                "host" : report.host,
                "mainClass" : report.mainClass,
                "processStateChange" : report.processStateChange,
                "oldStatus" : report.status,
                "newStatus" : newStatus,
                "timeStamp" : report.timeStamp,
                "movedTime" : new Date(),
                "userId" : user
            }

            return audit;
        }


        /** ========================================================================================
         ** Drag and Drop Functions
         ** ===================================================================================== */

        // When a card is dropped into New, remove it from any list it was in,
        // add it to $scope.new and increase the New column height
        $scope.onDropCompleteNew=function(data,evt){

            var newAudit = createAudit(data,"New","HERRET2");

            addAudit(newAudit);

            $scope.removeCard(data);

            data.status = "New";
            moveReport(data);

            increaseHeight("#newList");
        }

        // When a card is dropped into Acknowledged, remove it from any list it was in,
        // add it to $scope.acknowledged and increase the Acknowledged column height
        $scope.onDropCompleteAcknowledged=function(data,evt){

            var newAudit = createAudit(data,"Acknowledged","HERRET2");

            addAudit(newAudit);

            $scope.removeCard(data);

            data.status = "Acknowledged";
            moveReport(data);

            increaseHeight("#acknowledgedList");
        }

        // When a card is dropped into Escalated, remove it from any list it was in,
        // add it to $scope.escalated and increase the Escalated column height
        $scope.onDropCompleteEscalated=function(data,evt){

            var newAudit = createAudit(data,"Escalated","HERRET2");

            addAudit(newAudit);

            $scope.removeCard(data);

            data.status = "Escalated";
            moveReport(data);

            increaseHeight("#escalatedList");
        }

        // When a card is dropped into History, create audit item,
        // reduce height of old row and increase the History column height
        $scope.onDropCompleteHistory=function(data,evt){

            var newAudit = createAudit(data,"History","HERRET2");

            addAudit(newAudit);

            $scope.removeCard(data);

            data.status = "History";
            moveReport(data);

            increaseHeight("#historyList");
        }

        $scope.onDragComplete=function(data,evt){
            /*console.log("drag success, data:", data);*/
        }

        // Reduces the height of a column when a card leaves
        $scope.removeCard = function(data)
        {

            if ($scope.new.indexOf(data) > -1)
                reduceHeight("#newList");
            else if ($scope.escalated.indexOf(data) > -1)
                reduceHeight("#escalatedList");
            else if ($scope.acknowledged.indexOf(data) > -1)
                reduceHeight("acknowledgedList");
            else if ($scope.history.indexOf(data) > -1)
                reduceHeight("#historyList");

        }

        // Reduces the height by one card for a given column
        function reduceHeight(columnId) {
            var object = $(columnId);

            if (object.height() > 650)
                object.height("-=165");
        };

        // Increases the height by one card for a given column
        function increaseHeight(columnId) {
            var object = $(columnId);

            object.height("+=165");
        };

        // Logs message to console and prints toast if applicable
        function log (message) {

            $log.info(message);
            if ($scope.settings.notificationToasts)
                Materialize.toast(message, 5000);
        };

        // Logs error to console and prints toast if applicable
        function logError (message) {

            $log.error(message);
            if ($scope.settings.notificationToasts)
                Materialize.toast(message, 5000);
        };

    });
