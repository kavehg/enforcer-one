angular.module('Enforcer.Common')
    .service('WebSocketService', ['$q', '$rootScope', function($q, $rootScope) {

        var ws = new WebSocket("ws://localhost:9090");

        ws.onopen = function() {
            console.log("Socket has been opened");
        };

        ws.onmessage = function(message) {
            console.log("Received message: " + JSON.stringify(message.data));
        };

    }]);