/**
 * DashboardController - primarily handles reports
 *                     - actively updates and displays reports
 *                     - allows reports to be moved and dynamically creates audits
 *                     - automatically escalates reports from "New" to "Escalated" after a given time
 *
 */
angular.module('Enforcer.Dashboard')
    .controller('DashboardCtrl', function($scope, $rootScope, $log, WebSocketService, ReportService, MetricService, SettingsService, AuditService, AnimationFactory) {
        /** ========================================================================================
         ** Card Object
         ** ===================================================================================== */

        //used for all items displayed on the dashboard and audits
        var Card = function(data) {
            if (isReport(data)) {
                this.header = data.processId;
                this.headerDetail = data.host;
                this.classPath = data.mainClass;
                this.detail = data.processStateChange;
                this.timeStamp = data.timeStamp;
                this.status = data.status;
                this.type = "Report";
            }
            else {
                this.header = data.metricDetail;
                this.headerDetail = data.average;
                this.classPath = data.target;
                this.detail = data.threshold;
                this.timeStamp = data.timeStamp;
                this.status = data.status;
                this.type = "Metric";
            }
        };

        function isReport(data) {
            if (data.processId != null)
                return true;

            return false;
        }


        /** ========================================================================================
         ** Init
         ** ===================================================================================== */
        //currently necessary to distinguish between a category drop and a detail drop
        var detailDrop = false;

        var init = function() {

            $scope.settings = {
                "connection" : "None",
                "missingTime" : 20,
                "statusScanTime" : 5,
                "deathTime" : 2,
                "escalationTime" : 1,
                "autoEscalation" : true,
                "notificationToasts" : false
            };

            $scope.new = [];

            $scope.acknowledged = [];

            $scope.escalated = [];

            $scope.history = [];

            //Audits which match unique report query
            $scope.returnedAudits = [];

            //Report currently being viewed in Report Detail modal
            $scope.detailReport;

            refreshSettings();

            //refreshCards();
            //refreshReports();

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

        $scope.received = false;

        $scope.showDetailBox = false;

        /** ========================================================================================
         ** Broadcast Listeners
         ** ===================================================================================== */

        // Listen for broadcast update when settings are changed
        $scope.$on('settingsChanged', function() {
            refreshSettings();
        });

        //Listens to WebSocketService for new reports
        $scope.$on('reportReceived', function() {
            $scope.checkForReport();
        });

        //Listens to WebSocketService for new metrics
        $scope.$on('metricsReceived', function() {
            $scope.checkForMetric();
        })

        /** ========================================================================================
         ** Functions
         ** ===================================================================================== */
        // Checks the WebSocketService for any new reports
        $scope.checkForReport = function() {
            WebSocketService.getReport().then(
                function(returnedReport) {
                    $scope.received = true;
                    ReportService.updateReports(returnedReport).then(
                        function(result) {
                            $scope.new.push(new Card(returnedReport));
                            reportAddedToast(returnedReport);
                            log(result);
                        },
                        function(err){
                            log(err);
                        });
                    //$scope.$broadcast('cardsChanged');
                },
                function() {
                    $scope.received = false;
                }
            );
        }

        // Checks the WebSocketService for any new metrics
        $scope.checkForMetric = function() {
            WebSocketService.getMetric().then(
                function(returnedMetric) {
                    $scope.received = true;
                    MetricService.updateMetrics(returnedMetric).then(
                        function(result) {
                            $scope.new.push(new Card(returnedMetric));
                            $rootScope.$broadcast('metricAdded');
                            metricAddedToast(returnedMetric);
                            log(result);
                        },
                        function (update) {
                            updateMetricCard(returnedMetric);
                            log(update);
                        }
                    );
                },
                function() {
                    $scope.received = false;
                }
            );
        }

        //ToDo: Metrics which are still above threshold be moved to new
        function updateMetricCard(update) {

            var card = new Card(update);
            for (var i = 0; i < $scope.new.length; i++) {
                if ($scope.new[i].classPath == update.classPath) {
                    log("New Updating...")
                    moveCard($scope.new[i], update.status);
                    return;
                }
            }
            for (var i = 0; i < $scope.acknowledged.length; i++) {
                if ($scope.acknowledged[i].classPath == update.classPath) {
                    log("Ack Updating...")
                    moveCard($scope.acknowledged[i], update.status);
                    return;
                }
            }
            for (var i = 0; i < $scope.escalated.length; i++) {
                if ($scope.escalated[i].classPath == update.classPath) {
                    log("Esc Updating...")
                    moveCard($scope.escalated[i], update.status);
                    return;
                }
            }
            for (var i = 0; i < $scope.history.length; i++) {
                if ($scope.history[i].classPath == update.classPath) {
                    log("Hist Updating...")
                    moveCard($scope.history[i], update.status);
                    return;
                }
            }
        }

        // Calls the SettingsService and retrieves the updated settings
        function refreshSettings() {

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

        // Removes a card from one column and adds it to
        // the column of the newStatus
        function moveCard(card, newStatus) {

            if (card.status == "New"){
                $scope.new.splice($scope.new.indexOf(card), 1);
                allocateCard(card, newStatus);
            }
            else if (card.status == "Acknowledged") {
                $scope.acknowledged.splice($scope.acknowledged.indexOf(card), 1);
                allocateCard(card, newStatus);
            }
            else if (card.status == "Escalated") {
                $scope.escalated.splice($scope.escalated.indexOf(card), 1);
                allocateCard(card, newStatus);
            }
            else if (card.status == "History") {
                $scope.history.splice($scope.history.indexOf(card), 1);
                allocateCard(card, newStatus);
            }
        }

        // Makes sure that each service has the right information for the cards on the dashboard
        function syncServices(card) {
            if (card.type == "Metric"){
                MetricService.updateMetrics(card).then(
                    function(result){
                        log(result);
                    },
                    function(update){
                        log(update);
                    }
                );
            }
            else {
                ReportService.updateReports(card).then(
                    function(result){
                        log(result);
                    },
                    function(update){
                        log(update);
                    }
                );
            }

            MetricService.getMetrics().then (function(metrics) {
                log(metrics);
            });
        }

        // Adds cards to their appropriate sub-list
        // based on their status
        function allocateCard(card, newStatus) {
            card.status = newStatus;
            if (newStatus == "New"){
                $scope.new.push(card);
            }
            else if (newStatus == "Acknowledged") {
                $scope.acknowledged.push(card);
            }
            else if (newStatus == "Escalated") {
                $scope.escalated.push(card);
            }
            else if (newStatus == "History") {
                $scope.history.push(card);
            }

            syncServices(card);
        }

        // Checks all of the New cards against time
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

        // Auto moves a card from New to Escalated after $scope.settings.escalationTime has passed
        function escalateReport(report) {

            var newAudit = createAudit(report, "Escalated", "Auto Escalation");
            addAudit(newAudit);

            $scope.$apply(function() {
                $scope.removeCard(report);
                moveCard(report, "Escalated");
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
                    logError("DashboardCtrl: Audit Add FAILED");
                    return false;
                }
            );
        }

        // Creates an Audit item from a given card, oldStatus and userAcf2Id
        function createAudit(card, newStatus, userAcf2Id) {

            var audit = {
                "_id" : "",
                "header" : card.header,
                "headerDetail" : card.headerDetail,
                "classPath" : card.classPath,
                "detail" : card.detail,
                "timeStamp" : card.timeStamp,
                "oldStatus" : card.status,
                "newStatus" : newStatus,
                "type" : card.type,
                "movedTime" : new Date().getTime(),
                "userAcf2Id" : userAcf2Id
            }

            return audit;
        }

        //Creates toast specifically for when a new report comes in from DeathStar
        function reportAddedToast(report) {
            AnimationFactory.playAnimation("#"+report.status+"Col", "flash");
            var toast = "<p><span class='New'>NEW</span> REPORT: "+report.processId+" "+report.processStateChange+"</p>";
            Materialize.toast(toast, 4000);
        }

        //Creates a toast specifically for when a new metric comes in from DeathStar
        function metricAddedToast(metric) {
            AnimationFactory.playAnimation("#"+metric.status+"Col", "flash");
            var toast = "<p><span class='New'>NEW</span> METRIC: "+metric.metricDetail+" "+metric.average+"</p>";
            Materialize.toast(toast, 4000);
        }


        /** ========================================================================================
         ** Drag and Drop Functions
         ** ===================================================================================== */

        // When a card is dropped into New, remove it from any list it was in,
        // add it to $scope.new and increase the New column height
        $scope.onDropCompleteNew=function(data,evt){
            $scope.showDetailBox = false;
            //Todo: add proper order for moving report and generating audit
            if (detailDrop || data.status == "New")
            return

            var newStatus = "New";

            var newAudit = createAudit(data, newStatus,"HERRET2");

            moveCard(data, newStatus);

            $scope.removeCard(data);

            addAudit(newAudit);

            increaseHeight("#newList");
        }

        // When a card is dropped into Acknowledged, remove it from any list it was in,
        // add it to $scope.acknowledged and increase the Acknowledged column height
        $scope.onDropCompleteAcknowledged=function(data,evt){
            $scope.showDetailBox = false;
            //Todo: add proper order for moving report and generating audit
            if (detailDrop || data.status == "Acknowledged")
            return

            var newStatus = "Acknowledged";

            var newAudit = createAudit(data, newStatus,"HERRET2");

            moveCard(data, newStatus);

            $scope.removeCard(data);

            addAudit(newAudit);

            increaseHeight("#acknowledgedList");
        }

        // When a card is dropped into Escalated, remove it from any list it was in,
        // add it to $scope.escalated and increase the Escalated column height
        $scope.onDropCompleteEscalated=function(data,evt){
            $scope.showDetailBox = false;
            //Todo: add proper order for moving report and generating audit
            if (detailDrop || data.status == "Escalated")
            return

            var newStatus = "Escalated";

            var newAudit = createAudit(data, newStatus,"HERRET2");

            moveCard(data, newStatus);

            $scope.removeCard(data);

            addAudit(newAudit);

            increaseHeight("#escalatedList");
        }

        // When a card is dropped into History, create audit item,
        // reduce height of old row and increase the History column height
        $scope.onDropCompleteHistory=function(data,evt){
            $scope.showDetailBox = false;
            //Todo: add proper order for moving report and generating audit
            if (detailDrop || data.status == "History")
            return;

            var newStatus = "History";

            var newAudit = createAudit(data, newStatus,"HERRET2");

            moveCard(data, newStatus);

            $scope.removeCard(data);

            addAudit(newAudit);

            increaseHeight("#historyList");

        }

        $scope.onDragComplete=function(data,evt){
            /*console.log("drag success, data:", data);*/
        }

        //Show detail drag container when report is picked up
        $scope.onDrag= function(data,evt){
            $scope.showDetailBox = true;
            detailDrop = false;
            AnimationFactory.playAnimation("#detailDrop", "fadeIn");
        }

        //Query for matching audits and set as report to be detailed when there is a drop in the detail box
        $scope.getReportDetails=function(data,evt){
            detailDrop = true;
            $scope.detailReport = data;
            $("#modal4").openModal();
            AuditService.getAuditTrail().then(
                function(returnedAuditTrail) {
                    for (var i = 0; i < returnedAuditTrail.length; i++) {
                        if (returnedAuditTrail[i].header == data.header && returnedAuditTrail[i].classPath == data.classPath && returnedAuditTrail[i].detail == data.detail) {
                            $scope.returnedAudits.push(returnedAuditTrail[i]);
                        }
                    }
                }, function(err) {
                    log("Could not retrieve audits.");
                }
            );
        }

        // Reduces the height of a column when a card leaves
        $scope.removeCard = function(data) {

            if ($scope.new.indexOf(data) > -1)
                reduceHeight("#newList");
            else if ($scope.escalated.indexOf(data) > -1)
                reduceHeight("#escalatedList");
            else if ($scope.acknowledged.indexOf(data) > -1)
                reduceHeight("#acknowledgedList");
            else if ($scope.history.indexOf(data) > -1)
                reduceHeight("#historyList");

        }

        // Reduces the height by one card for a given column
        function reduceHeight(columnId) {

            var object = $(columnId);

            if (object.height() > 650)
                object.height("-=165");
        }

        // Increases the height by one card for a given column
        function increaseHeight(columnId) {

            var object = $(columnId);
            object.height("+=165");
        }

        // Logs message to console and prints toast if applicable
        function log (message) {

            $log.info(message);
            if ($scope.settings.notificationToasts)
                Materialize.toast(message, 5000);
        }

        // Logs error to console and prints toast if applicable
        function logError (message) {

            $log.error(message);
            if ($scope.settings.notificationToasts)
                Materialize.toast(message, 5000);
        }

    });
