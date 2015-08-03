var myModule = angular.module('enforcer',[
    'ngRoute',
    'Enforcer.Dashboard'
]);

myModule.config(function($routeProvider) {
    $routeProvider
        .when('/', {
            templateUrl: 'src/enforcer/dashboard/tmpl/dashboard.html',
            controller: 'DashboardCtrl',
            controllerAs: 'dashboard'
        })
});

