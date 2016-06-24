/**
 *  MetricController    - Used primarily in the Metric Modal
 *                      - Configures Metric Monitor Request and sends to deathstar through WebSocketService
 *                      - Very early version, expect updates
**/


angular.module('Enforcer.Dashboard')
    .controller('MetricCtrl', function(WebSocketService, $scope, $log){

        $scope.data = {
            url: "veritas.market-data-service.MDS-SERVER.service.MarketDataService.addQuote.m15_rate",
            threshold: 0
        };

        $scope.prepareMetricRequest = function() {
            var metric = angular.copy($scope.data);
            metric.url = "http://cpvalrsvz203.cibg.tdbank.ca:18080/render?target=summarize(" + metric.url + ",%221min%22,%22last%22)&from=-5min&format=json";
            WebSocketService.sendMetricRequest(JSON.stringify(metric));
        }
    })