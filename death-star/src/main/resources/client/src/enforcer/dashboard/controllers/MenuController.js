angular.module('Enforcer.Dashboard')
    .controller('MenuCtrl', function($scope, $rootScope, WebSocketService) {

        // init function
        (function () {
            Materialize.toast("menu test", 10000);
        })();

        $scope.statuses = [];

        $scope.$on('statusReceived', function() {
            $scope.checkForStatuses();
        });

        // Calls the WebSocketService and retrieves any new reports
       /* $scope.checkForStatuses = function () {

            WebSocketService.getStatuses().then(
                function(returnedStatuses) {
                    $scope.received = true;

                    returnedStatuses.forEach(function(status) {
                        if ($scope.statuses.indexOf(status) == -1)
                            $scope.statuses.push(status);
                    });


                    $('.collapsible').collapsible({
                        accordion: false // A setting that changes the collapsible behavior to expandable instead of the default accordion style
                    });

                }, function() {
                    $scope.received = false
                }
            );
        };*/


    });
