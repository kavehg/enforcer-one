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

        $scope.auditTrail = {
            shownAudits: [],
            snapshotLength: 0
        };
        $scope.showAuditsThrough = 10;

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

        $scope.$on('openChangeLog', function() {
            $scope.showAuditsThrough = 10;
            $scope.auditTrail.shownAudits = $scope.auditTrail.shownAudits.length < $scope.showAuditsThrough ? $scope.auditTrail.shownAudits : $scope.auditTrail.shownAudits.splice(0, $scope.showAuditsThrough);
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
                    var allAudits = angular.copy(returnedAuditTrail).reverse();
                    $scope.auditTrail.snapshotLength = allAudits.length;
                    $scope.auditTrail.shownAudits = allAudits.length < $scope.showAuditsThrough ? allAudits.splice(0, allAudits.length) : allAudits.splice(0, $scope.showAuditsThrough);
                    var audit = $scope.auditTrail.shownAudits[0];
                    if (audit.update) {
                        reportAuditUpdateToast(audit);
                    }
                    else {
                        reportAuditToast(audit);
                        AnimationFactory.playAnimation("#"+audit.newStatus+"Col", "flash");
                    }
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
            Materialize.toast(toast, 5000);
        }

        function reportAuditUpdateToast(audit) {
            var classPathInfo = audit.classPath.split('.');
            var updatedPathString = classPathInfo.splice(classPathInfo.length - 3, 3).join('.');
            AnimationFactory.playAnimation("#gear-button", "bounce");
            var toast = "<p>" + audit.type + " Update <span class='"+audit.status+"'>"+audit.header +"</span> " + updatedPathString + ": " + audit.headerDetail + "</p>"
            Materialize.toast(toast, 10000);
        }

        $scope.showMoreAudits = function() {
            var prevShown = angular.copy($scope.showAuditsThrough);
                $scope.showAuditsThrough += 10;
                AuditService.getAuditTrail().then(
                    function(returnedAuditTrail) {
                        var allAudits = angular.copy(returnedAuditTrail).reverse();
                        $scope.showAuditsThrough = allAudits.length > $scope.showAuditsThrough ? $scope.showAuditsThrough : allAudits.length;
                        for (var i = prevShown; i < $scope.showAuditsThrough; i++) {
                            $scope.auditTrail.shownAudits.push(allAudits[i]);
                        }
                    },
                    function(err) {
                        log(err);
                    }
                );
        }
    });