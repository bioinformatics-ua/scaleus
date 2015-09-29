/**
 * @lgonzalez
 */
var app = angular.module('restapp.controllers', []);


app.controller('DatasetsCtrl', function ($scope, DatasetsService, SharedService) {

    /* event to manage the selected dataset */
    $scope.selectedDataset = SharedService.selectedDataset;

    $scope.$on('datasetChanged', function (event, dataset) {
        $scope.selectedDataset = dataset;
        //console.log($scope.selectedDataset);
    });

    $scope.changeDataset = function (selectedDataset) {
        SharedService.update(selectedDataset);
    };
    /* end of event to manage the selected dataset */


    $scope.showAddDatabase = function () {
        $scope.showFormDatabase = $scope.showFormDatabase ? false : true;
    };


    $scope.queryDatasets = function () {
        DatasetsService.query(function (response) {
            $scope.datasets = response;
            SharedService.update($scope.selectedDataset);
        });
    };

    $scope.saveDataset = function () {

        if ($scope.formDatabase) {
            DatasetsService.save({id: $scope.formDatabase}, function (response) {
                $scope.queryDatasets();
                $scope.showFormDatabase = false;
                var savedDatasetIndex = $scope.datasets.indexOf($scope.formDatabase);
                if (savedDatasetIndex !== -1)
                    $scope.selectedDataset = $scope.datasets[savedDatasetIndex];

            });
        } else {
            alert("No database name was introduced");
        };

    };

    $scope.deleteDataset = function () {

        if ($scope.selectedDataset) {
            DatasetsService.delete({id: $scope.selectedDataset}, function (response) {
                $scope.queryDatasets();
            });
        } else {
            alert("No database name to remove");
        }
        ;

    };



    //init
    $scope.queryDatasets();

});


app.controller('NamespacesCtrl', function ($scope, NamespacesService, SharedService) {

	console.log('on NamespacesCtrl ' + SharedService.selectedDataset);

	$scope.getNamespaces = function () {
		if (SharedService.selectedDataset) {
			NamespacesService.get({dataset: SharedService.selectedDataset}, function (response) {
//				var results = response.namespaces;
				$scope.namespaces = response.namespaces;
//				angular.forEach(results, function (val, key) {
//				$scope.namespaces.push({item: {prefix: key, namespace: val}, checked: false});
//				});
			}, function (response) {
				// an error occured
				alert(response.status + " " + response.statusText);
			});
		};
	};

	$scope.addNamespace = function () {
		if ($scope.formPrefix && $scope.formNamespace) {
			var ns = {'prefix': $scope.formPrefix, 
					'namespace': $scope.formNamespace}
			NamespacesService.save({dataset: SharedService.selectedDataset}, ns, function (response) {
				$scope.getNamespaces();
				$scope.formPrefix = "";
				$scope.formNamespace = "";
			}, function (response) {
				// an error occured
				alert(response.status + " " + response.statusText);
			});
		} else {
			alert("Invalid namespace");
		};
	};

	$scope.removeNamespace = function (prefix) {
		NamespacesService.delete({dataset: SharedService.selectedDataset, prefix: prefix}, function (response) {
			$scope.getNamespaces();
		}, function (response) {
			// an error occured
			alert(response.status + " " + response.statusText);
		});
	};

	//init
	$scope.getNamespaces();
});


app.controller('TriplesCtrl', function ($scope, TriplesService, SharedService) {

	console.log('on TriplesCtrl ' + SharedService.selectedDataset);

	$scope.addTriple = function () {
		if ($scope.formSubject && $scope.formPredicate && $scope.formObject) {

			var triple = {'s': $scope.formSubject, 
					'p': $scope.formPredicate,
					'o': $scope.formObject};
			TriplesService().save({dataset: SharedService.selectedDataset}, triple, function (response) {
				$scope.formSubject = "";
				$scope.formPredicate = "";
				$scope.formObject = "";
				// show alert: successful
				$scope.successTextAlert = "Triple added successfully!";
				$scope.showSuccessAlert = true;
				$scope.showErrorAlert = false;
			}, function (response) {
				// an error occured
				alert(response.status + " " + response.statusText);
				// show alert: failed
				$scope.errorTextAlert = "Couldn't add triple";
				$scope.showErrorAlert = true;
				$scope.showSuccessAlert = false;
			});

		} else {
			alert("Invalid triple");
		};
	};


	$scope.removeTriple = function () {
		if ($scope.formSubject && $scope.formPredicate && $scope.formObject) {

			var triple = {'s': $scope.formSubject, 
					'p': $scope.formPredicate,
					'o': $scope.formObject};
			TriplesService(triple).delete({dataset: SharedService.selectedDataset}, function (response) {
				$scope.formSubject = "";
				$scope.formPredicate = "";
				$scope.formObject = "";
				// show alert: successful
				$scope.successTextAlert = "Triple removed successfully!";
				$scope.showSuccessAlert = true;
				$scope.showErrorAlert = false;
			}, function (response) {
				// an error occured
				alert(response.status + " " + response.statusText);
				// show alert: failed
				$scope.errorTextAlert = "Couldn't remove triple";
				$scope.showErrorAlert = true;
				$scope.showSuccessAlert = false;
			});

		} else {
			alert("Invalid triple");
		};
	};

});


app.controller('QueriesCtrl', function ($scope, QueriesService, SharedService) {

	console.log('on QueriesCtrl ' + SharedService.selectedDataset);

	$scope.getData = function () {
		if ($scope.formSPARQL) {
			queryResults = [];
			var query = $scope.checkedPrefix() + $scope.formSPARQL;
			QueriesService.query({dataset: SharedService.selectedDataset, query: query, inference: $scope.inference}, function (response) {
				if (response.results.bindings) {
					$scope.queryResults = response.results.bindings;
				} else {
					$scope.noResults = true;
				}
			}, function (response) {
				// an error occured
				alert(response.status + " " + response.statusText);
			});
		} else {
			alert("Write your query");
		};
	};

	$scope.checkedPrefix = function () {
		var prefix = "";
//		angular.forEach(DBList.modelContainer, function (ns) { //TODO modelContainer is from other scope
//		if (ns.checked) {
//		prefix += 'PREFIX ' + ns.item.prefix + ': <' + ns.item.namespace + '> ';
//		}
//		;
//		});
		return prefix;
	};

	$scope.addAllPrefix = function () {
		angular.forEach($scope.modelContainer, function (ns) { //TODO: modelContainer is from other scope
			console.log(ns);
			ns.checked = true;
		});
	};

	$scope.getAll = function () {
		$scope.formSPARQL = 'SELECT * { \n?S ?P ?O \n} LIMIT 1000';
	};

	$scope.countAll = function () {
		$scope.formSPARQL = 'SELECT (COUNT(*) as ?count)\nWHERE {\n?s ?p ?o .\n}';
	};

	$scope.inference = false;
	$scope.queryResults = [];

});


app.controller('ResourceCtrl', function ($scope, $rootElement, $location, ResourceService, SharedService) {

	$scope.dataset = $location.search().dataset;
	$scope.prefix = $location.search().prefix;
	$scope.resource = $location.search().resource;

	ResourceService.get({dataset: $scope.dataset, prefix: $scope.prefix, resource: $scope.resource}, function (response) {
			console.log(response.data);
			$scope.description = response.data;
		}, function (response) {
			// an error occured
			alert (response.status + " " + response.statusText);
		});

	$scope.getResources();

});


//app.controller('queriesController', function ($scope, APIservice) {
//
//	var DBList = this;
//


//	// GET namespaces
//	DBList.getNamespaces = function () {
//	if ($scope.selectedDB) {
//	APIservice.getNamespaces($scope.selectedDB)
//	.then(function (response) {
//	DBList.namespaces = response.data;
//	DBList.modelContainer = [];
//	angular.forEach(DBList.namespaces, function (val, key) {
//	DBList.modelContainer.push({item: {prefix: key, namespace: val}, checked: false});
//	});
//	}, function (response) {
//	// an error occured
//	alert(response.status + " " + response.statusText);
//	});
//	}
//	;
//	};


//	DBList.getData = function () {
//		if ($scope.formSPARQL) {
//			DBList.queryResults = [];
//			var query = DBList.checkedPrefix() + $scope.formSPARQL;
//			APIservice.getSparqler($scope.selectedDB, query, $scope.inference)
//			.then(function (response) {
//				$scope.sparqlRequest = response.config.url;
//				console.log(response.config.url);
//				if (response.data.results.bindings) {
//					DBList.queryResults = response.data.results.bindings;
//					console.log(DBList.queryResults);
//				} else {
//					$scope.noResults = true;
//				}
//			}, function (response) {
//				// an error occured
//				alert(response.status + " " + response.statusText);
//			});
//		} else {
//			alert("Write your query");
//		}
//		;
//	};
//
//	DBList.checkedPrefix = function () {
//		var prefix = "";
//		angular.forEach(DBList.modelContainer, function (ns) {
//			if (ns.checked) {
//				prefix += 'PREFIX ' + ns.item.prefix + ': <' + ns.item.namespace + '> ';
//			}
//			;
//		});
//		return prefix;
//	};
//
//	DBList.addAllPrefix = function () {
//		angular.forEach(DBList.modelContainer, function (ns) {
//			console.log(ns);
//			ns.checked = true;
//		});
//	};
//
//});
