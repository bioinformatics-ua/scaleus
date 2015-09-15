/**
 * @lgonzalez
 */
var app = angular.module('scaleusApp', ['ngRoute']);

app.config(function ($locationProvider) {

    //routing DOESN'T work without html5Mode
	//required to access route parameters
	$locationProvider.html5Mode({
		  enabled: true,
		  requireBase: false
		});
});

app.controller('appTest', function($scope, $http, $rootElement, $location) {

	var Resource = this;
	Resource.dataset = $location.search().dataset;
	Resource.prefix = $location.search().prefix;
	Resource.resource = $location.search().resource;

	// GET describe resources
	Resource.getResources = function () {
		$http.get("../api/v1/resource/"+Resource.dataset+"/"+Resource.prefix+"/"+Resource.resource+"/js")
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
