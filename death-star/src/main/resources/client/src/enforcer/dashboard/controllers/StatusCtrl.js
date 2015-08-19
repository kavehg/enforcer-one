angular.module('Enforcer.Dashboard')
    .controller('StatusCtrl', function($scope, $rootScope, $log, WebSocketService, ReportService, SettingsService) {

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

            refreshSettings();

            // Check statuses every 5 seconds
            setInterval(function () {
                statusScan();
            }, ($scope.settings.statusScanTime * 1000));
        };

        init();

        /* ========================================================================================
         * Scope Variables
         * ===================================================================================== */

        $scope.statuses = [];

        $scope.received = false;

        $scope.timeDiff = 0;

        /* ========================================================================================
         * Broadcast Listeners
         * ===================================================================================== */

        // Listen for broadcast update from the websocketservice
        $scope.$on('statusReceived', function() {
            $scope.checkForStatus();
        });

        // Listen for broadcast update from the websocketservice
        $scope.$on('settingsChanged', function() {
            refreshSettings();
        });

        /* ========================================================================================
         * Functions
         * ===================================================================================== */

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
            // If promise received is resolved
            SettingsService.getSettings().then(
                function(returnedSettings) {
                    $scope.received = true;
                    $scope.settings = returnedSettings;
                    log('StatusCtrl: Settings Updated');

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
                "processId" : status.xwingId,
                "host" : status.host,
                "mainClass" : "X-Wing is down",
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
                function(returnedBroadcast) {
                    $scope.received = true;
                    $rootScope.$broadcast('reportsChanged');

                }, function() {
                    $scope.received = false
                }
            );
        };

        function log (message) {
            $log.info(message);
            if ($scope.settings.notificationToasts)
                Materialize.toast(message, 5000);
        };

        function logError (message) {
            $log.error(message);
            if ($scope.settings.notificationToasts)
                Materialize.toast(message, 5000);
        };


    });
