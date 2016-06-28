/**
 *  MetricController    - Used primarily in the Metric Modal
 *                      - Configures Metric Monitor Request and sends to DeathStar through WebSocketService
 *                      - Very early version, expect updates
**/


angular.module('Enforcer.Dashboard')
    .controller('MetricCtrl', function(WebSocketService, MetricService, $scope, $log, $rootScope){
        /** ========================================================================================
         ** Scope variables
         ** ===================================================================================== */
        $scope.metricRequest = {
            url: "veritas.market-data-service.MDS-SERVER.service.MarketDataService.addQuote.m15_rate",
            metricDetail: "",
            threshold: 0
        };

        /** ========================================================================================
         ** functions
         ** ===================================================================================== */
        //prepares metricRequest as json string
        $scope.prepareMetricRequest = function() {
            var metric = angular.copy($scope.metricRequest);
            metric.url = "http://cpvalrsvz203.cibg.tdbank.ca:18080/render?target=summarize(" + metric.url + ",%221min%22,%22last%22)&from=-5min&format=json";
            MetricService.updateMetricRequests(metric).then (
                function (result) {
                    $log.info(result);
                    WebSocketService.sendMetricRequest(JSON.stringify(metric));
                    $rootScope.$broadcast('metricRequestReceived');
                },
                function (reject) {
                    $log.info(reject);
                    Materialize.toast(reject, 5000);
                }
            );
        }
    })