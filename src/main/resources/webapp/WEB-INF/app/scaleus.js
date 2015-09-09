/**
 * @lgonzalez
 */
var app = angular.module('scaleusApp', []);

app.controller('appController', function($scope, $http) {

	var DBList = this;


	// GET datasets
	DBList.getDatasets = function () {
		$http.get("http://localhost:8000/api/v1/dataset/")
		.then(function (response) {
			DBList.dataset = response.data;
		});
	};

	// POST new database
	DBList.addDatabase = function () {
		if ($scope.formDatabase) {
			console.log('adding DB '+$scope.formDatabase+'\n');
			$http.post("http://localhost:8000/api/v1/dataset/"+$scope.formDatabase,{})
			.then(function (response) {
				DBList.getDatasets();
				$scope.formDatabase = "";
			});
		} else {
			alert("No database name was introduced");
		};
	};

	// DELETE database
	DBList.removeDatabase = function () {
		if ($scope.selectedDB) {
			console.log('removing '+$scope.selectedDB+'\n');
			$http.delete("http://localhost:8000/api/v1/dataset/"+$scope.selectedDB)
			.then(function (response) {
				console.log(response);
				DBList.getDatasets();
			});
		} else {
			alert("Select a database to remove");
		};
	};

	// GET namespaces
	DBList.getNamespaces = function () {
		if ($scope.selectedDB) {
			$http.get("http://localhost:8000/api/v1/namespaces/"+$scope.selectedDB)
			.then(function (response) {
				DBList.namespaces = response.data;
				DBList.modelContainer = [];
				angular.forEach(DBList.namespaces, function(val, key){
					DBList.modelContainer.push({item:{prefix:key, namespace:val}, checked:false});
				});
			});
		};
	};

	// POST namespaces
	DBList.putNamespace = function () {
		if ( $scope.formPrefix && $scope.formNamespace ) {
			$http.post("http://localhost:8000/api/v1/namespace/"+$scope.selectedDB, {'prefix': $scope.formPrefix, 'namespace': $scope.formNamespace})
			.then(function (response) {
				console.log(response);
				// TODO dont uncheck checked namespaces
				DBList.getNamespaces();
				$scope.formPrefix = "";
				$scope.formNamespace = "";
			});
		} else {
			alert("Incomplete namespace");
		}
	};

	// DELETE namespace
	DBList.removeNamespace = function (prefix) {
		$http.delete("http://localhost:8000/api/v1/namespace/"+$scope.selectedDB+"/"+prefix)
		.then(function (response) {
			// TODO dont uncheck checked namespaces
			DBList.getNamespaces();
		});
	};

	// GET query sparql
	DBList.sparqler = function () {
		if ($scope.formSPARQL) {
		console.log("http://localhost:8000/api/v1/"+$scope.selectedDB+"/sparql?query="+encodeURIComponent($scope.formSPARQL));
		$http.get("http://localhost:8000/api/v1/"+$scope.selectedDB+"/sparql?query="+encodeURIComponent($scope.formSPARQL))
		.then(function (response) {
			// TODO show data
			console.log(response);
		});
		} else {
			alert("Write your query");
		}
	};

	DBList.checkedPrefix = function() {
		var prefix = "";
		angular.forEach(DBList.modelContainer, function(ns) {
			if (ns.checked) {
				prefix += 'PREFIX ' + ns.item.prefix + ': <' + ns.item.namespace + '>/n';
			}
		});
		return prefix;
	};

	DBList.setPrefix = function () {
		console.log('setPrefix');
		// TODO : no funciona --> no elimina cuando uncheck!!
		$scope.formSPARQL = DBList.checkedPrefix() + $scope.formSPARQL;
	};

	DBList.getDatasets();

});
