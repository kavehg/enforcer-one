var myModule = angular.module('Enforcer',[
    'ui.router',
    'ngDraggable',
    'ngResource',
    'Enforcer.Common',
    'Enforcer.Dashboard'
]);

myModule.config(function($stateProvider, $urlRouterProvider, $httpProvider) {

    delete $httpProvider.defaults.headers.common['X-Requested-With'];
    $httpProvider.defaults.headers.post['Accept'] = 'application/json, text/javascript';
    $httpProvider.defaults.headers.post['Content-Type'] = 'application/json; charset=utf-8';
    $httpProvider.defaults.headers.post['Access-Control-Max-Age'] = '1728000';
    $httpProvider.defaults.headers.common['Access-Control-Max-Age'] = '1728000';
    $httpProvider.defaults.headers.common['Accept'] = 'application/json, text/javascript';
    $httpProvider.defaults.headers.common['Content-Type'] = 'application/json; charset=utf-8';
    $httpProvider.defaults.useXDomain = true;


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
                },
                'vaderDashboard@': {
                    templateUrl: 'src/enforcer/dashboard/templates/vaderDashboard.html',
                }
            }
        })


});

