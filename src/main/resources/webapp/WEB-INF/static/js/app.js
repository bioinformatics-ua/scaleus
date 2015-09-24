/**
 * @lgonzalez
 */

var app = angular.module('scaleusApp', [
  'ngRoute',
  'scaleusApp.queries',
  'scaleusApp.resources',
  'scaleusApp.services',
  'scaleusApp.directives'
]);

app.config(function ($locationProvider) {

    //routing DOESN'T work without html5Mode
	//required to access route parameters
	$locationProvider.html5Mode({
		  enabled: true,
		  requireBase: false
		});
});

app.config(['$routeProvider', function($routeProvider) {
	  $routeProvider.
	  when("/app/index.html", {
		  templateUrl: "partials/sparqler.html"
	  }).
	  when("/app/namespaces", {
		  templateUrl: "partials/namespaces.html"
	  }).
	  when("/app/triples", {
		  templateUrl: "partials/triples_manager.html"
	  }).
//	  when("/app/resource/:database/:prefix/:resource", {
//		  templateUrl: "../../../partials/resource.html",
//		  controller: "resourcesController",
//		  controllerAs: "Resource"
//	  }).
	  when("/resource/:database/:prefix/:resource", {
		  templateUrl: "../../../app/partials/resource.html",
		  controller: "resourcesController",
		  controllerAs: "Resource"
	  }).
	  otherwise({redirectTo: '/app/index.html'});
	}]);