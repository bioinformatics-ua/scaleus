/**
 * @lgonzalez
 */
var app = angular.module('scaleusApp.services', ['ngResource']);

app.factory('Database', function($resource) {
  return $resource('http://localhost:8000/api/v1/:query');
});

app.controller('RESTcontroller', function($scope, 'Database'){
	Database.query({query: 'dataset'}, function(data) {
		$scope.datasets = data;
	});
});

