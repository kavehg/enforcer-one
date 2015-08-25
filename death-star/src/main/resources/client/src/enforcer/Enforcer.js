var myModule = angular.module('Enforcer',[
    'ui.router',
    'ngDraggable',
    'ngResource',
    'Enforcer.Common',
    'Enforcer.Dashboard'
]);

myModule.config(function($stateProvider, $urlRouterProvider) {
    $urlRouterProvider.otherwise('dashboard');

    $stateProvider

        // Home State and Nested Views
        /*.state('dashboard', {
            url: '/dashboard',
            templateUrl: 'src/enforcer/dashboard/templates/dashboard.html'
        })*/
        .state('dashboard', {
            url: '/dashboard',
            views: {
                '': {
                    templateUrl: 'src/enforcer/dashboard/templates/dashboard.html'
                },
                'auditTrail@': {
                    templateUrl: 'src/enforcer/dashboard/templates/auditTrail.html',
                    controller: 'AuditCtrl',
                    replace: true
                },
                'sidebar@': {
                    templateUrl: 'src/enforcer/dashboard/templates/sidebar.html',
                    controller: 'StatusCtrl',
                    replace: true
                }
            }
        })


});

