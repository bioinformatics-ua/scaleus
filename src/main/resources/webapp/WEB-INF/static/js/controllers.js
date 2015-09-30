/**
 * @lgonzalez
 */
var app = angular.module('restapp.controllers', []);


app.controller('DatasetsCtrl', function ($scope, $location, DatasetsService, SharedService) {

    /* event to manage the selected dataset */
    $scope.selectedDataset = SharedService.selectedDataset;

    $scope.$on('datasetChanged', function (event, dataset) {
        $scope.selectedDataset = dataset;
        //console.log($scope.selectedDataset);
    });

    $scope.changeDataset = function (selectedDataset) {
        //console.log(selectedDataset);
        SharedService.update(selectedDataset);
        //$location.path("/app/");
    };
    /* end of event to manage the selected dataset */


    $scope.showAddDatabase = function () {
        $scope.showFormDatabase = $scope.showFormDatabase ? false : true;
    };


    $scope.queryDatasets = function () {
        DatasetsService.query(function (response) {
            //console.log("querydatasets");
            $scope.datasets = response;
            $scope.changeDataset(response[0]);
            //SharedService.selectedDataset=(response[0]);
            //$scope.selectedDataset = response[0];
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
        }
        ;

    };

    $scope.deleteDataset = function () {

        DatasetsService.delete({id: $scope.selectedDataset}, function (response) {
            $scope.queryDatasets();
        }, function (error) {
            console.log();
            alert(error.statusText);
        });

    };



    //init
    $scope.queryDatasets();

});

app.controller('RDFDataCtrl', function ($scope, RDFDataService, SharedService) {

    $scope.$on('datasetChanged', function (event, dataset) {
        $scope.getRDFData();
        //console.log($scope.selectedDataset);
    });

    $scope.getRDFData = function () {
        RDFDataService.get({id: SharedService.selectedDataset}, function (response) {
            $scope.data = response.content;
        });
    };

    $scope.saveRDFData = function () {

        if ($scope.data) {
            RDFDataService.save({id: SharedService.selectedDataset}, $.param({data: $scope.data}), function (response) {
                alert("Data submitted!");
                $scope.getRDFData();
            }, function (error) {
                console.log(error.statusText);
            });
        } else {
            alert("No data was introduced");
        }
        ;

    };

    //init
    $scope.getRDFData();

});

app.controller('NamespacesCtrl', function ($scope, NamespacesService, SharedService) {

    console.log('on NamespacesCtrl ' + SharedService.selectedDataset);

    $scope.$on('datasetChanged', function (event, dataset) {
        $scope.getNamespaces();
        //console.log($scope.selectedDataset);
    });

    $scope.getNamespaces = function () {
        if (SharedService.selectedDataset) {
            NamespacesService.get({dataset: SharedService.selectedDataset}, function (response) {
                $scope.namespaces = response.namespaces;
            }, function (response) {
                // an error occured
                alert(response.status + " " + response.statusText);
            });
        }
        ;
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
        }
        ;
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
        }
        ;
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
        }
        ;
    };

});


app.controller('QueriesCtrl', function ($scope, QueriesService, NamespacesService, SharedService) {

    console.log('on QueriesCtrl ' + SharedService.selectedDataset);

    $scope.$on('datasetChanged', function (event, dataset) {
        $scope.getNamespaces();
        //console.log($scope.selectedDataset);
    });

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
        }
        ;
    };

    $scope.getNamespaces = function () {
        if (SharedService.selectedDataset) {
            NamespacesService.get({dataset: SharedService.selectedDataset}, function (response) {
                angular.forEach(response.namespaces, function (val, key, obj) {
                    val.checked = false;
                });
                $scope.namespacesContainer = response.namespaces;
            }, function (response) {
                // an error occured
                alert(response.status + " " + response.statusText);
            });
        }
        ;
    };

    $scope.checkedPrefix = function () {
        var prefix = "";
        angular.forEach($scope.namespacesContainer, function (ns) { //TODO modelContainer is from other scope
            if (ns.checked) {
                prefix += 'PREFIX ' + ns.prefix + ': <' + ns.uri + '> ';
            }
            ;
        });
        return prefix;
    };

    $scope.addAllPrefix = function () {
        angular.forEach($scope.namespacesContainer, function (ns) {
            ns.checked = true;
        });
    };

    $scope.getAll = function () {
        $scope.formSPARQL = 'SELECT * { \n?S ?P ?O \n} LIMIT 1000';
    };

    $scope.countAll = function () {
        $scope.formSPARQL = 'SELECT (COUNT(*) as ?count)\nWHERE {\n?s ?p ?o .\n}';
    };

    // init
    $scope.inference = false;
    $scope.queryResults = [];
    $scope.getNamespaces();

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
        alert(response.status + " " + response.statusText);
    });

    $scope.getResources();

});