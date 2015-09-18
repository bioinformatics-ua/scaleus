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