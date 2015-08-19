angular.module('Enforcer.Dashboard')
    .directive('settings', function() {
        return {
            restrict: 'EA',
            require: true,
            scope: {
                ngModel: '='
            },
            controller: '',
            templateUrl: 'dashboard/templates/settings.html'
        };
    });