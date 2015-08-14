angular.module('Enforcer.Dashboard')
    .controller('DashboardCtrl', function($scope, $rootScope, WebSocketService) {

        // init function
        var init = function() {

            // Check statuses every 5 seconds
            setInterval(function () {
                checkStatuses();
            }, 5000);
        };

        init();

        $scope.statuses = [];

        var dashboard = this;

        $scope.new = [];

        //$scope.statuses = [];

        $scope.received = false;

        $scope.connectionOpen = false;

        // Listen for broadcast update from the websocketservice
        $scope.$on('connectionOpen', function() {
            $scope.connectionOpen = true;
        });

        // Listen for broadcast update from the websocketservice
        $scope.$on('reportReceived', function() {
           $scope.checkForReport();
        });

        // Listen for broadcast update from the websocketservice
        $scope.$on('statusReceived', function() {
            $scope.checkForStatus();
        });

        // Checks all of the statuses for validity
        function checkStatuses() {

            var currentTime;
            var differential;

            if ($scope.statuses.length == 0)
            {
                Materialize.toast("no statuses", 5000);
                return true;
            }

            for (var i=0; i < $scope.statuses.length; i++)
            {
                currentTime = new Date() / 1000;
                differential = currentTime - $scope.statuses[i].timeStampInstant.epochSecond;
                Materialize.toast(differential, 3000);
                if (differential > 20) { // 5 seconds

                    if (differential > 120) // 2 minutes
                        deadStatus($scope.statuses[i]);
                    else
                        brokenStatus($scope.statuses[i]);
                }
                else
                    goodStatus($scope.statuses[i]);

            }

            return true;
        }


        // Status is more than 10 seconds old, but not 2 mins old
        function brokenStatus(status) {

            if ($('#text-'+status.xwingId).hasClass('green-text'))
                $('#text-'+status.xwingId).addClass('red-text').removeClass('green-text');

            ///Materialize.toast("X-Wing " + status.xwingId + " is missing on host " + status.host, 10000);
         }

        // Status is more 2 mins old
        function deadStatus(status) {
            var report = {
                "processId" : status.xwingId,
                "host" : status.host,
                "mainClass" : "X-Wing is down",
                "processStateChange" : "MISSING",
                "status" : "New",
                "timeStamp" : status.timeStamp
            };

            if ($('#text-'+status.xwingId).hasClass('red-text'))
                $('#text-'+status.xwingId).addClass('grey-text').removeClass('red-text');

            $scope.new.push(report);

            //Materialize.toast("X-Wing " + status.xwingId + " is dead on host " + status.host, 10000);
        }

        // Status is a-ok
        function goodStatus(status) {

            if ($('#text-'+status.xwingId).hasClass('red-text'))
                $('#text-'+status.xwingId).addClass('green-text').removeClass('red-text');

        }

        // Handles a received status
        function handleStatus(status) {
            // check if status is in list
            var index = statusCheck(status);

            if (index == -1)
                $scope.statuses.push(status);
            else {
                $scope.statuses[index].timestamp = status.timestamp;
                // Make the menu item blink
                $("#text-"+status.xwingId).fadeOut(300).fadeIn(300);
                $('.tooltipped').tooltip({delay: 35});
            }
        }

        // Compares the statuses based on the xwingId
        function statusCheck(status) {

            if ($scope.statuses.length > 0) {
                for (var i = 0; i < $scope.statuses.length; i++) {
                    if ($scope.statuses[i].xwingId == status.xwingId)
                        return i;
                }
                return -1;
            }

            return -1;
        }

        // Compares reports based on processId and host
        function reportCheck(report) {

            if ($scope.new.length > 0) {
                for (var i = 0; i < $scope.new.length; i++) {
                    if ($scope.new[i].processId == report.processId && $scope.new[i].host == report.host)
                        return i;
                }
                return -1;
            }

            return -1;
        }

        // Calls the WebSocketService and retrieves any new reports
        $scope.checkForStatus = function () {

            WebSocketService.getStatus().then(
                function(returnedStatus) {
                    $scope.received = true;

                    handleStatus(returnedStatus);

                }, function() {
                    $scope.received = false
                }
            );
        };

        // Calls the WebSocketService and retrieves any new statuses
        $scope.checkForReport = function () {

            WebSocketService.getReport().then(
                function(returnedReport) {

                    $scope.received = true;

                    if (reportCheck(returnedReport) == -1)
                        $scope.new.push(returnedReport);

                }, function() {
                    $scope.received = false
                }
            );
        };

        $scope.new = [
            {
                "processId" : 1875,
                "host" : "tovalrs01",
                "mainClass" : "Valuation Engine",
                "processStateChange" : "STOPPED",
                "status" : "New",
                "timeStamp" : "2015/07/17 16:55:32"
            },
            {
                "processId" : 2443,
                "host" : "tovalrs02",
                "mainClass" : "Pricing Engine",
                "processStateChange" : "STOPPED",
                "status" : "New",
                "timeStamp" : "2015/07/17 16:55:32"
            },
            {
                "processId" : 2901,
                "host" : "tovalrs02",
                "mainClass" : "Pricing Engine",
                "processStateChange" : "STOPPED",
                "status" : "New",
                "timeStamp" : "2015/07/17 16:55:32"
            },
            {
                "processId" : 4443,
                "host" : "tovalrs05",
                "mainClass" : "Reference Data Service",
                "processStateChange" : "STOPPED",
                "status" : "New",
                "timeStamp" : "2015/07/17 16:55:32"
            }
        ];

        $scope.acknowledged = [
            {
                "processId" : 1965,
                "host" : "tovalrs03",
                "mainClass" : "Trade Service",
                "processStateChange" : "STARTED",
                "status" : "Acknowledged",
                "timeStamp" : "2015/07/17 16:55:32"
            },
            {
                "processId" : 765,
                "host" : "tovalrs02",
                "mainClass" : "Pricing Engine",
                "processStateChange" : "STOPPED",
                "status" : "Acknowledged",
                "timeStamp" : "2015/07/17 16:55:32"
            }
        ];

        $scope.escalated = [
            {
                "processId" : 8765,
                "host" : "tovalrs04",
                "mainClass" : "Market Data Service",
                "processStateChange" : "STOPPED",
                "status" : "Escalated",
                "timeStamp" : "2015/07/17 16:55:32"
            }
        ];

        $scope.history = [
            {
                "processId" : 8745,
                "host" : "tovalrs05",
                "mainClass" : "Reference Data Service",
                "processStateChange" : "STOPPED",
                "status" : "History",
                "timeStamp" : "2015/07/17 16:55:32"
            }
        ];

        $scope.onDragComplete=function(data,evt){
            /*console.log("drag success, data:", data);*/
        }

        //
        $scope.removeCard = function(data)
        {
            var index;

            if ($scope.new.indexOf(data) > -1)
            {
                index = $scope.new.indexOf(data);
                $scope.new.splice(index, 1);
                reduceHeight("#newList");
            }
            else if ($scope.escalated.indexOf(data) > -1)
            {
                index = $scope.escalated.indexOf(data);
                $scope.escalated.splice(index, 1);
                reduceHeight("#escalatedList");
            }
            else if ($scope.acknowledged.indexOf(data) > -1)
            {
                index = $scope.acknowledged.indexOf(data);
                $scope.acknowledged.splice(index, 1);
                reduceHeight("acknowledgedList");
            }
            else if ($scope.history.indexOf(data) > -1)
            {
                index = $scope.history.indexOf(data);
                $scope.history.splice(index, 1);
                reduceHeight("#historyList");
            }

        }

        // Reduces the height by one card for a given column
        function reduceHeight(id) {
            var object = $(id);

            if (object.height() > 650)
                object.height("-=165");
        }

        // Increases the height by one card for a given column
        function increaseHeight(id) {
            var object = $(id);

            object.height("+=165");
        }

        // When a card is dropped into New, remove it from any list it was in,
        // add it to $scope.new and increase the New column height
        $scope.onDropCompleteNew=function(data,evt){

            $scope.removeCard(data);

            data.status = "New";

            var index = $scope.new.indexOf(data);
            if (index == -1)
                $scope.new.push(data);

            increaseHeight("#newList");
        }

        // When a card is dropped into Acknowledged, remove it from any list it was in,
        // add it to $scope.acknowledged and increase the Acknowledged column height
        $scope.onDropCompleteAcknowledged=function(data,evt){

            $scope.removeCard(data);

            data.status = "Acknowledged";

            var index = $scope.acknowledged.indexOf(data);
            if (index == -1)
                $scope.acknowledged.push(data);

            increaseHeight("#acknowledgedList");
        }

        // When a card is dropped into Escalated, remove it from any list it was in,
        // add it to $scope.escalated and increase the Escalated column height
        $scope.onDropCompleteEscalated=function(data,evt){

            $scope.removeCard(data);

            data.status = "Escalated";

            var index = $scope.escalated.indexOf(data);
            if (index == -1)
                $scope.escalated.push(data);

            increaseHeight("#escalatedList");
        }

        // When a card is dropped into History, remove it from any list it was in,
        // add it to $scope.history and increase the History column height
        $scope.onDropCompleteHistory=function(data,evt){

            $scope.removeCard(data);

            data.status = "History";

            var index = $scope.history.indexOf(data);
            if (index == -1)
                $scope.history.push(data);

            increaseHeight("#historyList");
        }




    });
