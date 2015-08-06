var myModule = angular.module('Enforcer',[
    'ngRoute',
    'Enforcer.Common',
    'Enforcer.Dashboard'
]);

myModule.config(function($routeProvider) {
    $routeProvider
        .when('/', {
            templateUrl: 'src/enforcer/dashboard/tmpl/dashboard.html',
            controller: 'DashboardCtrl',
            controllerAs: 'dashboard'
        })
        .otherwise({redirectTo: '/'});
});

