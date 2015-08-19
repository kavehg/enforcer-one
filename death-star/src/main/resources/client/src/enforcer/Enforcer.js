var myModule = angular.module('Enforcer',[
    'ui.router',
    'ngDraggable',
    'Enforcer.Common',
    'Enforcer.Dashboard'
]);

myModule.config(function($stateProvider, $urlRouterProvider) {
    $urlRouterProvider.otherwise('dashboard');

    $stateProvider

        // Home State and Nested Views
        .state('dashboard', {
            url: '/dashboard',
            templateUrl: 'src/enforcer/dashboard/templates/dashboard.html'
        })


        // About page
        .state('dashboard.settings', {
            //url: '/settings',
            //templateUrl: 'src/enforcer/dashboard/templates/settings.html'
        });


});

