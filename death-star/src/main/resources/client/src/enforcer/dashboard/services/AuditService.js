/**
 * AuditService  - contains the audit list of all changes made inside the app
 *               - allows controllers to add an audit for things like moving cards, changing settings etc
 *               - also broadcasts when an audit is received, in order to update the audit screen
 */
angular.module('Enforcer.Dashboard')
    .service('AuditService', ['$q', '$rootScope', function($q, $scope, $rootScope) {

        /* ========================================================================================
         * Audits
         * ===================================================================================== */

        var audits = [];

        var settingChanges = [];

        /* ========================================================================================
         * Functions
         * ===================================================================================== */

        // Creates a promise and resolves it with available data, otherwise reject the promise.
        function getAuditTrail() {

            var deferred = $q.defer();

            if(audits.length > 0) {
                deferred.resolve(audits);
            }
            else {
                deferred.reject("No audits!");
            }

            return deferred.promise;
        }

        // Receives an audit and adds it to list
        function addAudit(audit) {
            var deferred = $q.defer();

            if (true) {
                audits.push(audit);
                deferred.resolve('auditAdded');
            }
            else {
                deferred.reject("Error adding audit.")
            }

            return deferred.promise;
        }

        // Calls to functions inside this service from external controllers
        return {

            getAuditTrail: getAuditTrail,
            addAudit: addAudit
        };

    }]);