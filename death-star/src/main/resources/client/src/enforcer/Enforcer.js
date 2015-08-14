var myModule = angular.module('Enforcer',[
    'ngRoute',
    'ngDraggable',
    'Enforcer.Common',
    'Enforcer.Dashboard'
]);

myModule.config(function($routeProvider) {
    $routeProvider
        .when('/', {
            templateUrl: 'src/enforcer/dashboard/tmpl/dashboard.html',
            controller: '',
            controllerAs: 'dashboard'
        })
        .otherwise({redirectTo: '/'});
});

