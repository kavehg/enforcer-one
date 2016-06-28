/**
 * AuditController - controls the auditTrail.html template
 *                 - actively updates and displays audits
 *
 */
angular.module('Enforcer.Dashboard')
    .controller('AuditCtrl', function($scope, $rootScope, $log, AuditService, SettingsService, AnimationFactory) {

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

            refreshAudits();

        };

        init();

        /** ========================================================================================
         ** Scope Variables
         ** ===================================================================================== */

        $scope.auditTrail = [];

        /** ========================================================================================
         ** Broadcast Listeners
         ** ===================================================================================== */

        // Listen for broadcast update when settings are changed
        $scope.$on('settingsChanged', function() {
            refreshSettings();
        });

        // Listen for broadcast update from the websocketservice
        $scope.$on('auditTrailChanged', function() {
            refreshAudits();
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
                    log('AuditCtrl: Settings Refreshed');

                }, function() {
                    $scope.received = false;
                    logError('AuditCtrl: Settings Refreshed FAILED');
                }
            );
        }

        // Calls the AuditService and retrieves the updated auditTrail
        function refreshAudits() {
            // If promise received is resolved
            AuditService.getAuditTrail().then(
                function(returnedAuditTrail) {
                    $scope.received = true;
                    $scope.auditTrail = returnedAuditTrail;
                    reportAuditToast($scope.auditTrail[$scope.auditTrail.length - 1]);
                    AnimationFactory.playAnimation("#"+$scope.auditTrail[$scope.auditTrail.length - 1].newStatus+"Col", "flash");
                    log('AuditCtrl: Audits Refreshed');
                }, function() {
                    $scope.received = false
                    logWarning('AuditCtrl: No Audits to Load');
                }
            );
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

        // Logs warning to console and prints toast if applicable
        function logWarning (message) {
            $log.warn(message);
            if ($scope.settings.notificationToasts)
                Materialize.toast(message, 5000);
        }

        function reportAuditToast(audit) {
            AnimationFactory.playAnimation("#gear-button", "bounce");
            var toast = "<p>"+ audit.type +" "+audit.header+" Moved from <span class='"+audit.oldStatus+"'>"+audit.oldStatus+"</span> to <span class='"+audit.newStatus+"'>"+audit.newStatus+"</span></p>"
            Materialize.toast(toast, 4000);
        }


    });