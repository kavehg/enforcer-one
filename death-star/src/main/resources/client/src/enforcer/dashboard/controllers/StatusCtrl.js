/**
 * StatusController - primarily handles statuses
 *                  - actively updates and displays statuses in the sidebar menu
 *                  - constantly compares the time difference between incoming statuses and current time
 *                  - automatically generates reports if statuses "die"
 *
 */
angular.module('Enforcer.Dashboard')
    .controller('StatusCtrl', function($scope, $rootScope, $log, WebSocketService, ReportService, SettingsService, MetricService) {

        /** ========================================================================================
         ** Init
         ** ===================================================================================== */

        var init = function() {

            $scope.settings = {
                "connection" : "None",
                "missingTime" : 20,
                "statusScanTime" : 5,
                "deathTime" : 2,
                "escalationTime" : 5,
                "autoEscalation" : true,
                "notificationToasts" : false
            };

            refreshSettings();

            // Check statuses every 5 seconds
            setInterval(function () {
                statusScan();
            }, ($scope.settings.statusScanTime * 1000));
        };

        init();

        /** ========================================================================================
         ** Scope Variables
         ** ===================================================================================== */

        $scope.statuses = [];

        $scope.received = false;

        $scope.timeDiff = 0;

        $scope.monitoredMetricRequests = [];

        /** ========================================================================================
         ** Broadcast Listeners
         ** ===================================================================================== */

        // Listen for broadcast update from the websocketservice
        $scope.$on('statusReceived', function() {
            $scope.checkForStatus();
        });

        // Listen for broadcast update from the websocketservice
        $scope.$on('settingsChanged', function() {
            refreshSettings();
        });


        $scope.$on('metricRequestReceived', function() {
            $scope.monitoredMetricRequests = [];

            MetricService.getMetricRequests().then(
                function (metrics) {
                    $scope.monitoredMetricRequests = metrics;
                },
                function (err) {
                    log("Could not display metrics to sidebar");
                }
            );
        });

        /** ========================================================================================
         ** Functions
         ** ===================================================================================== */

        // Calls the WebSocketService and retrieves any new reports
        $scope.checkForStatus = function () {

            WebSocketService.getStatus().then(
                function(returnedStatus) {
                    $scope.received = true;

                    handleStatus(returnedStatus);

                }, function() {
                    $scope.received = false
                }
            );
        };

        // Calls the SettingsService and retrieves the updated settings
        function refreshSettings() {

            SettingsService.getSettings().then(
                function(returnedSettings) {
                    $scope.received = true;
                    $scope.settings = returnedSettings;
                    log('StatusCtrl: Settings Refreshed');

                }, function() {
                    $scope.received = false
                }
            );
        }

        // Handles a received status
        function handleStatus(status) {

            // Check if status is in list
            var index = statusCompare(status);

            // Update status timestamps and make it blink in menu bar
            if (index > -1) {
                $scope.statuses[index].timestamp = status.timestamp;
                $scope.statuses[index].timeStampInstant = status.timeStampInstant;

                goodStatus(status);
            }
            else if (index == -1) { // Status is new, add to list
                $scope.statuses.push(status);
            }
            else { // index == -2, remove old x-wing on sidebar
                // This would occur when an x-wing is restarted and now has new x-wingid, but same host
                // The old menu item will remain dead forever since new x-wing has new x-wingid
                replaceStatus(status);
                goodStatus(status);
            }
        }

        // Compares the statuses based on the xwingId
        function statusCompare(status) {

            if ($scope.statuses.length > 0) {
                for (var i = 0; i < $scope.statuses.length; i++) {
                    if ($scope.statuses[i].host == status.host) {

                        if ($scope.statuses[i].xwingId == status.xwingId)
                            return i; // x-wing exists, flash current status
                        else
                            return -2; // Means new x-wing on same host. remove old one
                        // This would occur when an x-wing is restarted and now has new x-wingid, but same host
                    }
                }
                return -1; // new x-wing/status
            }
            return -1; // new x-wing/status
        }

        // Calculates the time difference between a given status and now
        $scope.calculateTimeDiff = function(status) {

            var currentTime = new Date() / 1000;
            $scope.timeDiff = Math.round((currentTime - status.timeStampInstant.epochSecond) * 100) / 100;
        };

        // Checks all of the statuses for validity
        function statusScan() {

            var currentTime;
            var differential;

            if ($scope.statuses.length == 0)
            {
                return true;
            }

            for (var i=0; i < $scope.statuses.length; i++)
            {
                currentTime = new Date() / 1000;
                differential = currentTime - $scope.statuses[i].timeStampInstant.epochSecond;

                if (differential < $scope.settings.missingTime) { // missingTime

                    goodStatus($scope.statuses[i]);
                }
                else {
                    if (differential < ($scope.settings.deathTime*60)) // deathTime
                        brokenStatus($scope.statuses[i]);
                    else
                        deadStatus($scope.statuses[i]);
                }
            }
            return true;
        }

        // Status is a-ok
        function goodStatus(status) {

            if ($('#text-'+status.xwingId).hasClass('red-text'))
                $('#text-'+status.xwingId).addClass('green-text').removeClass('red-text');

            // Make the menu item blink
            $("#text-"+status.xwingId).fadeOut(300).fadeIn(300);
            $('.tooltipped').tooltip({delay: 25});

            status.state = "Good";
        }

        // Status is more than 10 seconds old, but not 2 mins old
        function brokenStatus(status) {

            if ($('#text-'+status.xwingId).hasClass('green-text'))
                $('#text-'+status.xwingId).addClass('red-text').removeClass('green-text');

            status.state = "Broken";
        }

        // Status is more 2 mins old
        function deadStatus(status) {

            var report = {
                "_id" : "",
                "processId" : status.xwingId.toString(),
                "host" : status.host,
                "mainClass" : "X-Wing missing more than 2 mins",
                "processStateChange" : "MISSING",
                "status" : "New",
                "timeStamp" : status.timeStamp
            };

            if ($('#text-'+status.xwingId).hasClass('red-text'))
                $('#text-'+status.xwingId).addClass('grey-text').removeClass('red-text');

            if (status.state != "Dead") {
                status.state = "Dead";

                if($scope.settings.autoEscalation)
                    $scope.sendReport(report);
            }
        }

        // Replace a status
        // This would occur when an x-wing is restarted and now has new x-wingid, but same host
        function replaceStatus(status) {

            for(var i=0; i < $scope.statuses.length; i++) {
                if ($scope.statuses[i].host == status.host && $scope.statuses[i].state != 'Good')
                    $scope.statuses[i] = status;
            }
        }

        // Sends a report to the ReportService
        $scope.sendReport = function (report) {

            ReportService.addReport(report).then(
                function(data) {
                    $scope.received = true;
                    $rootScope.$broadcast('reportsChanged');
                    log('StatusCtrl: Added Report');

                }, function(err) {
                    $scope.received = false
                    logError('StatusCtrl: Add Report FAILED');
                }
            );
        };

        //opens a modal for configuring a metric monitor
        $scope.openMetric = function() {
            $("#modal5").openModal();
        }

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
