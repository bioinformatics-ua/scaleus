/**
 * @lgonzalez
 */
var app = angular.module('scaleusApp.resources', []);

app.controller('resourcesController', function($scope, $routeParams, scaleusAPIservice) {

	var Resource = this;
	Resource.database = $routeParams.database;
	Resource.prefix = $routeParams.prefix;
	Resource.resource = $routeParams.resource;
	
	Resource.getResources = function () {
		scaleusAPIservice.getResources(Resource.database, Resource.prefix, Resource.resource)
		.then(function (response) {
			console.log('MyData = ' + response.data);
			Resource.description = response.data;
		}, function (response) {
			// an error occured
			alert (response.status + " " + response.statusText);
		});
	};

	Resource.getResources();
});
