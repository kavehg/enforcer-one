angular.module('Enforcer.Dashboard')
    .controller('AuditCtrl', function($scope, $rootScope, $log, AuditService) {

        // init function
        var init = function() {

            refreshAudits();

        };

        init();

        /* ========================================================================================
         * Scope Variables
         * ===================================================================================== */

        $scope.auditTrail = [];

        /* ========================================================================================
         * Broadcast Listeners
         * ===================================================================================== */

        // Listen for broadcast update from the websocketservice
        $scope.$on('auditTrailChanged', function() {
            refreshAudits();
        });

        /* ========================================================================================
         * Functions
         * ===================================================================================== */

        // Calls the AuditService and retrieves the updated auditTrail
        function refreshAudits() {
            // If promise received is resolved
            AuditService.getAuditTrail().then(
                function(returnedAuditTrail) {
                    $scope.received = true;
                    $scope.auditTrail = returnedAuditTrail;
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

    });