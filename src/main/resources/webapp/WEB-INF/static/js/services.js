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
    return $resource('../api/v1/namespaces/:dataset/:prefix', {}, {
      get: {method:'GET'},	// TODO - filter results
      add: {method:'POST'},
      delete: {method:'DELETE'}
    });   
});

services.factory('TriplesService', function($resource) {
    return $resource('../api/v1/store/:dataset', {dataset: '@dataset'}, {
      add: {method:'POST'},
      delete: {method:'DELETE',	// TODO - send data on body
    	  		headers: {"Content-Type": "application/json;charset=utf-8"},
    	  		data: '@triple'}
    });   
});

services.factory('QueriesService', function($resource) {
    return $resource('../api/v1/sparqler/:dataset/sparql', {}, {
      query: {method:'GET'}	
    });   
});

services.factory('ResourceService', function($resource) {
    return $resource('../api/v1/resource/:dataset/:prefix/:resource', {}, {
      get: {method:'GET'}	// TODO - implement this method
    });   
});

services.factory('APIservice', function($http) {

	var scaleusAPI = {};

//	// GET datasets
//	scaleusAPI.getDatasets = function () {
//		return $http.get("../api/v1/dataset/");
//	};
//
//	// POST new database
//	scaleusAPI.addDatabase = function (database) {
//		return $http.post("../api/v1/dataset/"+database,{});
//	}
//
//	// DELETE database
//	scaleusAPI.deleteDatabase = function (database) {
//		return $http.delete("../api/v1/dataset/"+database);
//	}

//	// GET namespaces
//	scaleusAPI.getNamespaces = function (database) {
//		return $http.get("../api/v1/namespaces/"+database);
//	}
//
//	// POST namespaces
//	scaleusAPI.addNamespace = function (database, prefix, namespace) {
//		return $http.post("../api/v1/namespace/"+database, {'prefix': prefix, 'namespace': namespace})
//	}
//
//	// DELETE namespace
//	scaleusAPI.deleteNamespace = function (database, prefix) {
//		return $http.delete("../api/v1/namespace/"+database+"/"+prefix);
//	}

	// GET query sparql
	scaleusAPI.getSparqler = function (database, query, inf) {
		return $http.get("../api/v1/sparqler/"+database+"/sparql?query="+encodeURIComponent(query)+"&inference="+inf);
	}

//	// POST new triple
//	scaleusAPI.addTriple = function (database, s, p, o) {
//		return $http.post("../api/v1/store/"+database, 
//				{'s': s, 
//			'p': p,
//			'o': o});
//	}

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