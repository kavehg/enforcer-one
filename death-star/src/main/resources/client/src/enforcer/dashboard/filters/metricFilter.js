
angular.module('Enforcer.Dashboard')
    .filter('metricFilter', function() {
        return function(items, vader) {
            var filtered = [];
            angular.forEach(items, function (item) {
                if (item.header == vader.metricDetail && item.type == "Metric") {
                    filtered.push(item);
                }
            });
            return filtered;
        }
    });