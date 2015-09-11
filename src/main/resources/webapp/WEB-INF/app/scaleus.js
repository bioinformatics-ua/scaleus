/**
 * @lgonzalez
 */
var app = angular.module('scaleusApp', []);

app.controller('appController', function($scope, $http) {

	var DBList = this;


	// GET datasets
	DBList.getDatasets = function () {
		$http.get("../api/v1/dataset/")
		.then(function (response) {
			DBList.dataset = response.data;
			$scope.selectedDB = DBList.dataset[0];
			DBList.getNamespaces();
		}, function (response) {
			// an error occured
			alert (response.status + " " + response.statusText);
		});
	};

	// POST new database
	DBList.addDatabase = function () {
		if ($scope.formDatabase) {
			console.log('adding DB '+$scope.formDatabase+'\n');
			$http.post("../api/v1/dataset/"+$scope.formDatabase,{})
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

	// DELETE database
	DBList.removeDatabase = function () {
		if ($scope.selectedDB) {
			console.log('removing '+$scope.selectedDB+'\n');
			$http.delete("../api/v1/dataset/"+$scope.selectedDB)
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
			$http.get("../api/v1/namespaces/"+$scope.selectedDB)
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

	// POST namespaces
	DBList.putNamespace = function () {
		if ( $scope.formPrefix && $scope.formNamespace ) {
			$http.post("../api/v1/namespace/"+$scope.selectedDB, {'prefix': $scope.formPrefix, 'namespace': $scope.formNamespace})
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

	// DELETE namespace
	DBList.removeNamespace = function (prefix) {
		$http.delete("../api/v1/namespace/"+$scope.selectedDB+"/"+prefix)
		.then(function (response) {
			DBList.getNamespaces();
		}, function (response) {
			// an error occured
			alert (response.status + " " + response.statusText);
		});
	};

	// GET query sparql
	DBList.sparqler = function () {
		$http.get("../api/v1/sparqler/"+$scope.selectedDB+"/sparql?query="+encodeURIComponent(DBList.query))
		.then(function (response) {
			DBList.queryResults = response.data.results.bindings;
			console.log(DBList.queryResults);
		}, function (response) {
			// an error occured
			alert (response.status + " " + response.statusText);
		});
	};

	// POST new triple
	DBList.addTriple = function () {
		if ( $scope.formSubject && $scope.formPredicate && $scope.formObject ) {
			$http.post("../api/v1/store/"+$scope.selectedDB, 
					{'s': $scope.formSubject, 
				'p': $scope.formPredicate,
				'o': $scope.formObject})
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

	// DELETE triple
	DBList.removeTriple = function () {
		if ( $scope.formSubject && $scope.formPredicate && $scope.formObject ) {
			// inject custom config to send 'data' on a DELETE request
			var config = {
					method : 'DELETE',
					url : "../api/v1/remove/"+$scope.selectedDB,
					data : {'s': $scope.formSubject, 
						'p': $scope.formPredicate,
						'o': $scope.formObject},
						headers: {"Content-Type": "application/json;charset=utf-8"}
			};
			$http(config)
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

	DBList.getData = function () {
		if ($scope.formSPARQL) {
			DBList.query = DBList.checkedPrefix()+$scope.formSPARQL;
			DBList.sparqler();
		} else {
			alert("Write your query");
		}
	}

	DBList.checkedPrefix = function() {
		var prefix = "";
		angular.forEach(DBList.modelContainer, function(ns) {
			if (ns.checked) {
				prefix += 'PREFIX ' + ns.item.prefix + ': <' + ns.item.namespace + '> ';
			}
		});
		return prefix;
	};

	DBList.getDatasets();
	DBList.queryResults = [];

});
