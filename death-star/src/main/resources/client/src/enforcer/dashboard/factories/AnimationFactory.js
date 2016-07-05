//Used for playing animations on elements from any component

angular.module('Enforcer.Dashboard')
    .factory('AnimationFactory', function($q){

        function playAnimation(elmt, animationName){
            $(elmt).addClass("animated "+animationName).one('animationend', function() {
                $(elmt).removeClass("animated "+animationName);
            });
        }

        function animateSwitchDashboard(bool) {
            //var deferred = $q.defer();


            return !bool;
            //deferred.reject("AnimationFactory: Could not animate dashboard switch");
            //return deferred.promise;
        }

        return {
            playAnimation: playAnimation,
            animateSwitchDashboard: animateSwitchDashboard
        };

    });