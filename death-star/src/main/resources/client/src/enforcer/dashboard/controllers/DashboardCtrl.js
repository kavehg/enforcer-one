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

        //Object used for all items displayed on the dashboard and audits
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
            else if (isMetric(data)) {
                this.header = data.metricDetail;
                this.headerDetail = data.average;
                this.classPath = data.target;
                this.detail = data.threshold;
                this.timeStamp = data.timeStamp;
                this.status = data.status;
                this.type = "Metric";
            }
            else {
                this.header = data.header;
                this.headerDetail = data.headerDetail;
                this.classPath = data.classPath;
                this.detail = data.detail;
                this.timeStamp = data.timeStamp;
                this.status = data.status;
                this.type = data.type;
            }
        };

        function isReport(data) {
            if (data.processId != null)
                return true;

            return false;
        }

        function isMetric(data) {
            if (data.target != null)
                return true;

            return false;
        }


        /** ========================================================================================
         ** Init
         ** ===================================================================================== */

        //Necessary to distinguish between a category drop and a detail drop
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

            $scope.allCards = [];

            //Audits which match unique report query
            $scope.returnedAudits = [];

            //Card currently being viewed in Card Detail modal
            $scope.detailedCard;

            refreshSettings();

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

        $scope.allCards = [];

        $scope.received = false;

        $scope.showDetailBox = false;

        $scope.dashboardSwitch = false;

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
        });

        //Listens for new Metric Requests from StatusCtrl
        $scope.$on('metricRequestReceived', function() {
            MetricService.getRecentRequest().then (
                function(result) {
                    if (result.type == "EDIT") {
                        cleanMetrics(result);
                        refreshMetricsForEdit(result);
                    }
                    else {
                        cleanMetrics(result);
                    }
                }
            );

            MetricService.getMetricRequests().then(
                function(result) {
                    $scope.vaders = [];
                    $scope.vaders = result;
                },
                function(reject) {
                    log(reject);
                }
            );
        });

        $scope.$on('switchDashboards', function() {
            log("DashboardCtrl: Dashboards changed");
            changeDashboards();
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
                            var card = new Card(returnedReport);
                            $scope.allCards.push(card);
                            reportAddedToast(returnedReport);
                            log(result);
                        },
                        function(err){
                            log(err);
                        });
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
                            var card = new Card(returnedMetric);
                            $scope.allCards.push(card);
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

        //Mainly for Metrics: updates metric card with most recent metric report.
        function updateMetricCard(update) {

            var card = new Card(update);
            var oldCard;

            for (var i = 0; i < $scope.allCards.length; i++) {
                if ($scope.allCards[i].classPath == card.classPath && $scope.allCards[i].header == card.header) {
                    oldCard = angular.copy($scope.allCards[i]);

                    $scope.allCards[i].header = card.header;
                    $scope.allCards[i].headerDetail = card.headerDetail;
                    $scope.allCards[i].classPath = card.classPath;
                    $scope.allCards[i].detail = card.detail;
                    $scope.allCards[i].timeStamp = card.timeStamp;
                    $scope.allCards[i].status = oldCard.status;
                    $scope.allCards[i].type = card.type;

                    var newAudit = createUpdateAudit(oldCard, card, "Update");
                    addAudit(newAudit);
                    return;
                }
            }
        }

        //When a request to REMOVE or EDIT comes in, remove any Cards associated with the Vader
        function cleanMetrics(req) {
            var clean = [];

            for(var i = 0; i < $scope.allCards.length; i++) {
                if ($scope.allCards[i].header == req.metricDetail)
                    clean.push(i);
            }
            for (var i = clean.length-1; i >= 0 ; i--) {
                $scope.allCards.splice(clean[i], 1);
            }

            AuditService.cleanAudits(req.metricDetail).then(
                function(result) {
                    log(result);
                },
                function (reject) {
                    log(reject);
                }
            );
        }

        //If the request was EDIT, refresh the dashboard with the edited Vader metrics
        function refreshMetricsForEdit(req) {
            MetricService.getMetrics().then(
                function(result) {
                    for (var i = 0; i < result.length; i++) {
                        if (result[i].header = req.metricDetail) {
                            $scope.allCards.push(new Card(result[i]));
                        }
                    }
                }
            );
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
        }

        // Adds cards to their appropriate sub-list
        // based on their status
        function allocateCard(card, newStatus) {
            card.status = newStatus;
            syncServices(card);
            $scope.$apply();
        }

        // Checks all of the New cards against time
        function reportScan() {

            var currentTime;
            var differential;

            if ($scope.allCards.length == 0)
            {
                return true;
            }

            for (var i=$scope.allCards.length-1; i >= 0; i--)
            {
                if ($scope.allCards[i].status == "New"){
                    currentTime = new Date().getTime();
                    var reportTime = new Date($scope.allCards[i].timeStamp).getTime();

                    differential = currentTime - reportTime;

                    if (differential > ($scope.settings.escalationTime * 60)) { // Convert escalation time in minutes to seconds
                        escalateReport($scope.allCards[i]);
                    }
                }
            }

            return true;
        }

        // Auto moves a card from New to Escalated after $scope.settings.escalationTime has passed
        function escalateReport(report) {

            var newAudit = createAudit(report, "Escalated", "Auto Escalation");
            addAudit(newAudit);

            allocateCard(report, "Escalated");

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
                "userAcf2Id" : userAcf2Id,
                "update" : false
            }

            return audit;
        }

        //Audits specifically for when a Card is updated
        function createUpdateAudit(oldCard, card, userAcf2Id) {

            var audit = {
                "_id" : "",
                "header" : card.header,
                "oldHeaderDetail" : oldCard.headerDetail,
                "headerDetail" : card.headerDetail,
                "classPath" : card.classPath,
                "oldDetail" : oldCard.detail,
                "detail" : card.detail,
                "timeStamp" : oldCard.timeStamp,
                "status" : oldCard.status,
                "type" : card.type,
                "movedTime" : new Date().getTime(),
                "userAcf2Id" : userAcf2Id,
                "update" : true
            }

            return audit
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

            allocateCard(data, newStatus);

            //$scope.removeCard(data);

            addAudit(newAudit);

            //increaseHeight("#newList");
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

            allocateCard(data, newStatus);

            //$scope.removeCard(data);

            addAudit(newAudit);

            //increaseHeight("#acknowledgedList");
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

            allocateCard(data, newStatus);

            //$scope.removeCard(data);

            addAudit(newAudit);

            //increaseHeight("#escalatedList");
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

            allocateCard(data, newStatus);

            //$scope.removeCard(data);

            addAudit(newAudit);

            //increaseHeight("#historyList");

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

        //Distinguishes between report and metric for view on detail drop
        $scope.reportOrMetric;
        //Query for matching audits and also get data type that will modify the modal
        $scope.getReportDetails=function(data,evt){
            detailDrop = true;
            if (data.type == "Report") {
                ReportService.findReport(data).then (
                    function(report) {
                        $scope.detailedCard = report;
                        $scope.reportOrMetric = true;
                    },
                    function (reject) {
                        $log.info(reject);
                    }
                );
            }
            else {
                MetricService.findMetric(data).then (
                    function(metric) {
                        $scope.detailedCard = metric;
                        $scope.reportOrMetric = false;
                    },
                    function(reject) {
                        $log.info(reject);
                    }
                )
            }
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

        //Reset scope variables
        $scope.closeDetailModal = function() {
            $scope.returnedAudits = [];
            $scope.showDetailBox = false;
        }

        // Reduces the height of a column when a card leaves
        /*$scope.removeCard = function(data) {

            if ($scope.new.indexOf(data) > -1)
                reduceHeight("#newList");
            else if ($scope.escalated.indexOf(data) > -1)
                reduceHeight("#escalatedList");
            else if ($scope.acknowledged.indexOf(data) > -1)
                reduceHeight("#acknowledgedList");
            else if ($scope.history.indexOf(data) > -1)
                reduceHeight("#historyList");

        }*/

        // Reduces the height by one card for a given column
        /*function reduceHeight(columnId) {

            var object = $(columnId);

            if (object.height() > 650)
                object.height("-=165");
        }*/

        // Increases the height by one card for a given column
        /*function increaseHeight(columnId) {

            var object = $(columnId);
            object.height("+=165");
        }*/

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

        /** ========================================================================================
         ** Vader Dashboard
         ** ===================================================================================== */

        /** ========================================================================================
         ** Scope Variables
         ** ===================================================================================== */

        $scope.vaders = [];

        /** ========================================================================================
         ** Functions
         ** ===================================================================================== */

        function changeDashboards() {

            if ($('#Dashboard').hasClass("ng-hide")) {
                //$('#Dashboard').css ("opacity", 0);
                $('#VaderDashboard').addClass("fadeOutLeft").one('animationend', function() {
                    $('#VaderDashboard').removeClass("fadeOutLeft");

                    $scope.dashboardSwitch = false;
                    $scope.$apply();

                    $('#Dashboard').addClass("fadeInRight").one('animationend', function() {
                        $('#Dashboard').removeClass("fadeInRight");
                    });
                });
            }
            else if ($('#VaderDashboard').hasClass("ng-hide")) {
                $('#Dashboard').addClass("fadeOutLeft").one('animationend', function() {
                    $('#Dashboard').removeClass("fadeOutLeft");

                    $scope.dashboardSwitch = true;
                    $scope.$apply();

                    $('#VaderDashboard').addClass("fadeInRight").one('animationend', function() {
                        $('#VaderDashboard').removeClass("fadeInRight");
                    });
                });
            }
        }
    });
