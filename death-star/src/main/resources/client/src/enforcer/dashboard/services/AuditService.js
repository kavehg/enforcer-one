/**
 * AuditService  - contains the audit list of all changes made inside the app
 *               - allows controllers to add an audit for things like moving cards, changing settings etc
 *               - also broadcasts when an audit is received, in order to update the audit screen
 */
angular.module('Enforcer.Dashboard')
    .service('AuditService', ['$q', '$resource', '$log', '$rootScope', function($q, $resource, $log, $scope, $rootScope) {

        /** ========================================================================================
         ** Audits
         ** ===================================================================================== */

        var audits = [];

        /** ========================================================================================
         ** API Setup
         ** ===================================================================================== */

        var auditAPI = $resource('http://localhost:8000/api/persistence/audits', null, {
            get: { // retrieve a specific audit with an auditId
                method: 'GET',
                isArray: false,
                url: 'http://localhost:8000/api/persistence/audits/:auditId'
            },
            getAll: { // retrieves all available Audits
                method: 'GET',
                isArray: true,
                url: 'http://localhost:8000/api/persistence/audits'
            },
            post: { // for creating NEW Audits
                method: 'POST',
                isArray: false,
                url: 'http://localhost:8000/api/persistence/audits/:auditId'
            }
        });

        /** ========================================================================================
         ** Functions
         ** ===================================================================================== */

        // Retrieves all audits available in the database
        function getAuditTrail() {

            var deferred = $q.defer();

            auditAPI.getAll().$promise.then(
                function(audits) {
                    $log.info('Successfully retrieved ' + audits.length + ' audits');
                    deferred.resolve(audits);
                }, function(err) {
                    $log.error('Failed to retrieve audits: ' + err);
                    deferred.reject(err);
                }
            );

            return deferred.promise;
        }

        // Posts audit object to database and stores it locally in audits[]
        function addAudit(audit) {

            var deferred = $q.defer();

            auditAPI.post({auditId: audit.processId}, audit).$promise.then(
                function(data) {
                    $log.info('Successfully posted audit: ' + data.processId);
                    audits.push(data);
                    deferred.resolve(data);
                }, function(err) {
                    $log.error('Failed to post audit: ' + err);
                    deferred.reject(err);
                }
            );

            return deferred.promise;
        }

        /** ========================================================================================
         ** Return
         ** ===================================================================================== */

        // Calls to functions inside this service from external controllers
        return {

            getAuditTrail: getAuditTrail,
            addAudit: addAudit
        };

    }]);