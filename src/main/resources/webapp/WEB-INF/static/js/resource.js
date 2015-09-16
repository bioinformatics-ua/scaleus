/**
 * @lgonzalez
 */
var app = angular.module('scaleusApp.resources', []);

app.controller('resourcesController', function($scope, $rootElement, $location, scaleusAPIservice) {

	var Resource = this;
	Resource.dataset = $location.search().dataset;
	Resource.prefix = $location.search().prefix;
	Resource.resource = $location.search().resource;


	Resource.getResources = function () {
		scaleusAPIservice.getResources(Resource.dataset, Resource.prefix, Resource.resource)
		.then(function (response) {
			console.log(response.data);
			Resource.description = response.data;
		}, function (response) {
			// an error occured
			alert (response.status + " " + response.statusText);
		});
	};

	Resource.getResources();
	console.log('Loaded scaleusApp --> appTest');

});
