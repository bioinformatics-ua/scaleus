/**
 * @lgonzalez
 */
var app = angular.module('scaleusApp.queries', []);


app.controller('queriesController', function($scope, scaleusAPIservice) {

	var DBList = this;
	
	DBList.getDatasets = function () {
	scaleusAPIservice.getDatasets()
		.then(function (response) {
			DBList.dataset = response.data;
			$scope.selectedDB = DBList.dataset[0];
			DBList.getNamespaces();
		}, function (response) {
			// an error occured
			alert (response.status + " " + response.statusText);
		});
	};

	DBList.addDatabase = function () {
		if ($scope.formDatabase) {
			scaleusAPIservice.addDatabase($scope.formDatabase)
			.then(function (response) {
				DBList.getDatasets();
				$scope.formDatabase = "";
			}, function (response) {
				// an error occured
				alert (response.status + " " + response.statusText);
			});
		} else {
			alert("No database name was introduced");
		};
	};

	DBList.removeDatabase = function () {
		if ($scope.selectedDB) {
			scaleusAPIservice.deleteDatabase($scope.selectedDB)
			.then(function (response) {
				console.log(response);
				DBList.getDatasets();
			}, function (response) {
				// an error occured
				alert (response.status + " " + response.statusText);
			});
		} else {
			alert("Select a database to remove");
		};
	};

	// GET namespaces
	DBList.getNamespaces = function () {
		if ($scope.selectedDB) {
			scaleusAPIservice.getNamespaces($scope.selectedDB)
			.then(function (response) {
				DBList.namespaces = response.data;
				DBList.modelContainer = [];
				angular.forEach(DBList.namespaces, function(val, key){
					DBList.modelContainer.push({item:{prefix:key, namespace:val}, checked:false});
				});
			}, function (response) {
				// an error occured
				alert (response.status + " " + response.statusText);
			});
		};
	};

	DBList.putNamespace = function () {
		if ( $scope.formPrefix && $scope.formNamespace ) {
			scaleusAPIservice.addNamespace($scope.selectedDB, $scope.formPrefix, $scope.formNamespace)
			.then(function (response) {
				console.log(response);
				DBList.getNamespaces();
				$scope.formPrefix = "";
				$scope.formNamespace = "";
			}, function (response) {
				// an error occured
				alert (response.status + " " + response.statusText);
			});
		} else {
			alert("Incomplete namespace");
		};
	};

	DBList.removeNamespace = function (prefix) {
		scaleusAPIservice.deleteNamespace($scope.selectedDB, prefix)
		.then(function (response) {
			DBList.getNamespaces();
		}, function (response) {
			// an error occured
			alert (response.status + " " + response.statusText);
		});
	};


	DBList.getData = function () {
		if ($scope.formSPARQL) {
			var query = DBList.checkedPrefix()+$scope.formSPARQL;
			scaleusAPIservice.getSparqler($scope.selectedDB, query)
			.then(function (response) {
				DBList.queryResults = response.data.results.bindings;
				console.log(DBList.queryResults);
			}, function (response) {
				// an error occured
				alert (response.status + " " + response.statusText);
			});
		} else {
			alert("Write your query");
		};
	};
	
	DBList.sparqler = function () {
		
	};

	DBList.addTriple = function (database) {
		console.log('Adding on database ' + database);
		if ( $scope.formSubject && $scope.formPredicate && $scope.formObject ) {
			scaleusAPIservice.addTriple($scope.selectedDB, $scope.formSubject, $scope.formPredicate, $scope.formObject)
				.then(function (response) {
					console.log(response);
					$scope.formSubject = "";
					$scope.formPredicate = "";
					$scope.formObject = "";
				}, function (response) {
					// an error occured
					alert (response.status + " " + response.statusText);
				});
		} else {
			alert("Incomplete triple");
		};
	};

	DBList.removeTriple = function (database) {
		if ( $scope.formSubject && $scope.formPredicate && $scope.formObject ) {
			scaleusAPIservice.deleteTriple(database, $scope.formSubject, $scope.formPredicate, $scope.formObject)
			.then(function (response) {
				console.log('STATUS ' + response.status);
				$scope.formSubject = "";
				$scope.formPredicate = "";
				$scope.formObject = "";
			}, function (response) {
				// an error occured
				alert (response.status + " " + response.statusText);
			});
		} else {
			alert("Incomplete triple");
		};
	};

	DBList.checkedPrefix = function() {
		var prefix = "";
		angular.forEach(DBList.modelContainer, function(ns) {
			if (ns.checked) {
				prefix += 'PREFIX ' + ns.item.prefix + ': <' + ns.item.namespace + '> ';
			};
		});
		return prefix;
	};
	
	DBList.getAll = function () {
		$scope.formSPARQL = 'SELECT * { \n?S ?P ?O \n} LIMIT 1000';
	};
	
	DBList.countAll = function () {
		$scope.formSPARQL = 'SELECT (COUNT(*) as ?count)\nWHERE {\n?s ?p ?o .\n}';
	};
	
	DBList.showAddDatabase = function () {
		$scope.showFormDatabase = $scope.showFormDatabase ? false : true;
	};

	DBList.getDatasets();
	DBList.queryResults = [];

});
