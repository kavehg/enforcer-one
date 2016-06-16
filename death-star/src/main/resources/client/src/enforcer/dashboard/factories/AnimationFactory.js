//Used for playing animations on elements from any component

angular.module('Enforcer.Dashboard')
    .factory('AnimationFactory', function(){

        function playAnimation(elmt, animationName){
            $(elmt).addClass("animated "+animationName).one('animationend', function() {
                $(elmt).removeClass("animated "+animationName);
            });
        }

        return {
            playAnimation: playAnimation
        };

    });