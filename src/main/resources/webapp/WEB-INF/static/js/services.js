/**
 * @lgonzalez
 */

var services = angular.module('restapp.services', []);


services.factory('SharedService', function() {
    var selectedDataset = {};
    return selectedDataset;
});

services.factory('DatasetsService', function($resource) {
    return $resource('../api/v1/dataset/:id', {id: '@id'}, {
      query: {method:'GET', isArray:true},
      save: {method:'POST'},
      delete: {method:'DELETE'}
    });
    
});

services.factory('NamespacesService', function($resource) {
    return $resource('../api/v1/namespace/:dataset', {dataset: '@dataset'}, {
      get: {method:'GET'}
    });
    
});

services.factory('APIservice', function($http) {

	var scaleusAPI = {};

	// GET datasets
	scaleusAPI.getDatasets = function () {
		return $http.get("../api/v1/dataset/");
	};

	// POST new database
	scaleusAPI.addDatabase = function (database) {
		return $http.post("../api/v1/dataset/"+database,{});
	}

	// DELETE database
	scaleusAPI.deleteDatabase = function (database) {
		return $http.delete("../api/v1/dataset/"+database);
	}

	// GET namespaces
	scaleusAPI.getNamespaces = function (database) {
		return $http.get("../api/v1/namespaces/"+database);
	}

	// POST namespaces
	scaleusAPI.addNamespace = function (database, prefix, namespace) {
		return $http.post("../api/v1/namespace/"+database, {'prefix': prefix, 'namespace': namespace})
	}

	// DELETE namespace
	scaleusAPI.deleteNamespace = function (database, prefix) {
		return $http.delete("../api/v1/namespace/"+database+"/"+prefix);
	}

	// GET query sparql
	scaleusAPI.getSparqler = function (database, query, inf) {
		return $http.get("../api/v1/sparqler/"+database+"/sparql?query="+encodeURIComponent(query)+"&inference="+inf);
	}

	// POST new triple
	scaleusAPI.addTriple = function (database, s, p, o) {
		return $http.post("../api/v1/store/"+database, 
				{'s': s, 
			'p': p,
			'o': o});
	}

	// DELETE triple
	scaleusAPI.deleteTriple = function (database, s, p, o) {
		// inject custom config to send 'data' on a DELETE request
		var config = {
				method : 'DELETE',
				url : "../api/v1/remove/"+database,
				data : {'s': s, 
					'p': p,
					'o': o},
					headers: {"Content-Type": "application/json;charset=utf-8"}
		};
		return $http(config);
	}

	// GET describe resources
	scaleusAPI.getResources = function (database, prefix, resource) {
		return $http.get("../api/v1/resource/"+database+"/"+prefix+"/"+resource+"/js");
	}

	return scaleusAPI;
});