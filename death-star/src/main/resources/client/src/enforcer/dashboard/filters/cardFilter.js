angular.module('Enforcer.Dashboard')
    .filter ('cardFilter', function(){
        return function(items, status) {
            var filtered = [];
            angular.forEach(items, function(item) {
                if (item.status == status) {
                    filtered.push(item);
                }
            });
            return filtered;
        }
    });