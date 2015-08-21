
angular.module('Enforcer.Dashboard')
    .directive('helloWorld', function() {
        return {
            restrict: 'AE',
            templateUrl: 'src/enforcer/dashboard/templates/settings.html'
        }
    });