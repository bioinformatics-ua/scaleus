/**
 * @lgonzalez
 */
angular.module('scaleusApp.directives', []).
directive('ngConfirmClick', [
  function(){
    return {
      priority: -1,
      restrict: 'A',
      link: function(scope, element, attrs){
        element.bind('click', function(e){
          var message = attrs.ngConfirmClick;
          if(message && !confirm(message)){
            e.stopImmediatePropagation();
            e.preventDefault();
          }
        });
      }
    }
  }
])
.directive('ngCheckLocalRoute', ['$location',
 function($location){
   var baseUrl = $location.absUrl().replace($location.url(), "");
   console.log('replaced = ' + baseUrl);
   return {
     priority: -1,
     restrict: 'A',
     link: function(scope, element, attrs, $location){
    	 element.bind('click', function(e){
        	 console.log('catch location = ' + baseUrl);
	    	 var href = attrs.ngCheckLocalRoute;
	    	 console.log ("catch href = " + href);
	    	 if (href.indexOf(baseUrl+'/resource/') === 0) {
	    		 attrs.$set('href', "/resource.html");
	    	 } else {
		    	 attrs.$set('href', href);
	    	 }
      	  });
    	 }
       }
     }
   ]);
