angular.module('Enforcer.Dashboard')
    .controller('DashboardCtrl', function($scope) {

        var dashboard = this;

        dashboard.reports = [
            {
                "processId" : 1875,
                "host" : "tovalrs01",
                "command" : "Valuation Engine",
                "stateChange" : "STOPPED",
                "status" : "New",
                "timeStamp" : "2015/07/17 16:55:32"
            },
            {
                "processId" : 2443,
                "host" : "tovalrs02",
                "command" : "Pricing Engine",
                "stateChange" : "STOPPED",
                "status" : "New",
                "timeStamp" : "2015/07/17 16:55:32"
            },
            {
                "processId" : 1965,
                "host" : "tovalrs03",
                "command" : "Trade Service",
                "stateChange" : "STARTED",
                "status" : "Acknowledged",
                "timeStamp" : "2015/07/17 16:55:32"
            },
            {
                "processId" : 8765,
                "host" : "tovalrs04",
                "command" : "Market Data Service",
                "stateChange" : "STOPPED",
                "status" : "Escalated",
                "timeStamp" : "2015/07/17 16:55:32"
            },
            {
                "processId" : 8745,
                "host" : "tovalrs05",
                "command" : "Reference Data Service",
                "stateChange" : "STOPPED",
                "status" : "History",
                "timeStamp" : "2015/07/17 16:55:32"
            }
        ];

        dashboard.statuses = [
            {name: 'New'},
            {name: 'Acknowledged'},
            {name: 'Escalated'},
            {name: 'History'}
        ];

    });
