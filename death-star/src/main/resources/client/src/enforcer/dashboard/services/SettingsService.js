/**
 * SettingsService - maintains the settings to be used across the various controllers in app
 *                 - allows settings to be updated and retrieved
 *
 */
angular.module('Enforcer.Dashboard')
    .service('SettingsService', ['$q', '$rootScope', function($q, $scope, $rootScope) {

        /* ========================================================================================
         * Settings Variables
         * ===================================================================================== */

        var settings = {
            "connection" : "None",
            "missingTime" : 20,
            "statusScanTime" : 5,
            "deathTime" : 2,
            "escalationTime" : 5,
            "autoEscalation" : true,
            "notificationToasts" : true
        };

        /* ========================================================================================
         * Functions
         * ===================================================================================== */

        // Creates a promise and resolves it with available data, otherwise reject the promise.
        function getSettings() {

            var deferred = $q.defer();

            if(true) {
                deferred.resolve(settings);
            }
            else {
                deferred.reject("Error retrieving settings.");
            }

            return deferred.promise;
        }

        // Receives new settings and overwrites the old
        function overwriteSettings(newSettings) {
            var deferred = $q.defer();

            if (true) {
                settings = newSettings;
                deferred.resolve(true);
            }
            else {
                deferred.reject("Error overriding settings.")
            }

            return deferred.promise;
        }

        // Calls to functions inside this service from external controllers
        return {
            overwriteSettings: overwriteSettings,
            getSettings: getSettings
        };

    }]);