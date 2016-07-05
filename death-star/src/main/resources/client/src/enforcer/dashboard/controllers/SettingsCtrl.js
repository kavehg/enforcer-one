/**
 * SettingsController - primarily handles settings
 *                    - settings can be updated and refreshed
 *
 */
angular.module('Enforcer.Dashboard')
    .controller('SettingsCtrl', function($scope, $rootScope, $log, SettingsService, AuditService, MetricService) {

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

        };

        init();

        /** ========================================================================================
         ** Scope Variables
         ** ===================================================================================== */

        $scope.settings = {
            "connection" : "None",
            "missingTime" : 20,
            "statusScanTime" : 5,
            "deathTime" : 30,
            "escalationTime" : 1,
            "autoEscalation" : true,
            "notificationToasts" : false
        };

        $scope.connection = "None";

        $scope.missingTime = {
            "floor": 0,
            "ceil": 30,
            "value": 20,
            "temp": 20
        };

        $scope.statusScanTime = {
            "floor": 0,
            "ceil": 10,
            "value": 5,
            "temp": 5
        };

        $scope.deathTime = {
            "floor": 0,
            "ceil": 10,
            "value": 2,
            "temp": 2
        };

        $scope.escalationTime = {
            "floor": 0,
            "ceil": 10,
            "value": 5,
            "temp": 5
        };

        $scope.autoEscalation = {
            "value": true,
            "temp": true
        };

        $scope.notificationToasts = {
            "value": false,
            "temp": false
        };

        $scope.changeLogCount = 0;

        /** ========================================================================================
         ** Broadcast Listeners
         ** ===================================================================================== */

        // Listen for broadcast update from the websocketservice
        $scope.$on('settingsChanged', function() {
            refreshSettings();
        });

        // Listen for broadcast update from the websocketservice
        $scope.$on('connectionOpen', function() {

            $scope.$apply(function() {
                $scope.connection = "Open";
                $rootScope.connection = $scope.connection;
            });

            if ($('#connectionStatus').hasClass('red'))
                $('#connectionStatus').addClass('green').removeClass('red');
        });

        // Listen for broadcast update from the websocketservice
        $scope.$on('connectionError', function() {

            $scope.$apply(function() {
                $scope.connection = "Error! Check console log.";
                $rootScope.connection = $scope.connection;
            });

            if ($('#connectionStatus').hasClass('green'))
                $('#connectionStatus').addClass('red').removeClass('green');
        });

        // Listen for broadcast update from the websocketservice
        $scope.$on('connectionClosed', function() {

            $scope.$apply(function() {
                $scope.connection = "Closed";
                $rootScope.connection = $scope.connection;
            });

            if ($('#connectionStatus').hasClass('green'))
                $('#connectionStatus').addClass('red').removeClass('green');
        });

        //Displays how many audits are in Change Log on setting button
        $scope.$on('auditTrailChanged', function(){
            AuditService.getAuditTrail().then(function(returnedAudits){
               $scope.changeLogCount = returnedAudits.length;
            });
        });

        /** ========================================================================================
         ** Functions
         ** ===================================================================================== */

        // Calls the SettingsService and retrieves the updated settings
        function refreshSettings() {
            // If promise received is resolved
            SettingsService.getSettings().then(
                function(returnedSettings) {
                    $scope.received = true;
                    $scope.settings = returnedSettings;
                    log('SettingsCtrl: Settings Refreshed');
                }, function() {
                    $scope.received = false
                    logError('SettingsCtrl: Settings Refresh FAILED');
                }
            );
        }

        // Opens the settings modal
        $scope.openSettings = function () {

            $('#modal1').openModal();

            $('.lean-overlay').click(function() {
                Materialize.toast('clicked', 2000);
                // Restore the temp values to original values
                $scope.missingTime.temp = $scope.missingTime.value;
                $scope.statusScanTime.temp = $scope.statusScanTime.value;
                $scope.deathTime.temp = $scope.deathTime.value;
                $scope.escalationTime.temp = $scope.escalationTime.value;
                $scope.autoEscalation.temp = $scope.autoEscalation.value;
                $scope.notificationToasts.temp = $scope.notificationToasts.value;
            });

        };

        // Saves the settings changes applied by user
        $scope.saveSettings = function () {

            $scope.missingTime.value = $scope.missingTime.temp;
            $scope.statusScanTime.value = $scope.statusScanTime.temp;
            $scope.deathTime.value = $scope.deathTime.temp;
            $scope.escalationTime.value = $scope.escalationTime.temp;
            $scope.autoEscalation.value = $scope.autoEscalation.temp;
            $scope.notificationToasts.value = $scope.notificationToasts.temp;

            $scope.settings = {
                "connection" : "None",
                "missingTime" : $scope.missingTime.value,
                "statusScanTime" : $scope.statusScanTime.value,
                "deathTime" : $scope.deathTime.value ,
                "escalationTime" : $scope.escalationTime.value,
                "autoEscalation" : $scope.autoEscalation.value,
                "notificationToasts" : $scope.notificationToasts.value
            };

            overwriteSettings($scope.settings);

            $('#modal1').closeModal();

        };

        // Sends new settings to Settings Service
        function overwriteSettings(newSettings) {

            SettingsService.overwriteSettings(newSettings).then(
                function(settingsSaved) {
                    $scope.received = true;
                    log("SettingsCtrl: Saved Settings");
                    $rootScope.$broadcast('settingsChanged');
                    return true;

                }, function() {
                    $scope.received = false;
                    logError("SettingsCtrl: Save Settings FAILED");
                    return false;
                }
            );
        }

        // Restores the local settings if user cancels or closes modal
        $scope.restoreSettings = function () {
            log('Settings Not Saved!');

            // Restore the temp values to original values
            $scope.missingTime.temp = $scope.missingTime.value;
            $scope.statusScanTime.temp = $scope.statusScanTime.value;
            $scope.deathTime.temp = $scope.deathTime.value;
            $scope.escalationTime.temp = $scope.escalationTime.value;
            $scope.autoEscalation.temp = $scope.autoEscalation.value;
            $scope.notificationToasts.temp = $scope.notificationToasts.value;

            $('#modal1').closeModal();
        };

        $scope.switchDashboards = function() {
            $rootScope.$broadcast('switchDashboards');
        };

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