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
        SharedService.update(selectedDataset);
        $location.path("/app/");
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
        }
        ;

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

    console.log(SharedService.selectedDataset);


});

app.controller('RDFDataCtrl', function ($scope, RDFDataService, SharedService) {

    $scope.getRDFData = function () {
        RDFDataService.get({id: SharedService.selectedDataset},function (response) {
            $scope.data = response.content;
        });
    };
    
    $scope.saveRDFData = function () {

        if ($scope.data) {
            RDFDataService.save({id: SharedService.selectedDataset},$.param({data: $scope.data}), function (response) {
                alert("Data submitted!");
                $scope.getRDFData();
            }, function (error) {
                console.log(error.statusText);
            });
        } else {
            alert("No data was introduced");
        };

    };
    
    //init
    $scope.getRDFData();

});

app.controller('queriesController', function ($scope, APIservice) {

    var DBList = this;

    DBList.getDatasets = function () {
        APIservice.getDatasets()
                .then(function (response) {
                    DBList.dataset = response.data;
                    $scope.selectedDB = DBList.dataset[0];
                    DBList.getNamespaces();
                }, function (response) {
                    // an error occured
                    alert(response.status + " " + response.statusText);
                });
    };

    DBList.addDatabase = function () {
        if ($scope.formDatabase) {
            APIservice.addDatabase($scope.formDatabase)
                    .then(function (response) {
                        DBList.getDatasets();
                        $scope.formDatabase = "";
                    }, function (response) {
                        // an error occured
                        alert(response.status + " " + response.statusText);
                    });
        } else {
            alert("No database name was introduced");
        }
        ;
    };

    DBList.removeDatabase = function () {
        if ($scope.selectedDB) {
            APIservice.deleteDatabase($scope.selectedDB)
                    .then(function (response) {
                        console.log(response);
                        DBList.getDatasets();
                    }, function (response) {
                        // an error occured
                        alert(response.status + " " + response.statusText);
                    });
        } else {
            alert("Select a database to remove");
        }
        ;
    };

    // GET namespaces
    DBList.getNamespaces = function () {
        if ($scope.selectedDB) {
            APIservice.getNamespaces($scope.selectedDB)
                    .then(function (response) {
                        DBList.namespaces = response.data;
                        DBList.modelContainer = [];
                        angular.forEach(DBList.namespaces, function (val, key) {
                            DBList.modelContainer.push({item: {prefix: key, namespace: val}, checked: false});
                        });
                    }, function (response) {
                        // an error occured
                        alert(response.status + " " + response.statusText);
                    });
        }
        ;
    };

    DBList.putNamespace = function () {
        if ($scope.formPrefix && $scope.formNamespace) {
            APIservice.addNamespace($scope.selectedDB, $scope.formPrefix, $scope.formNamespace)
                    .then(function (response) {
                        console.log(response);
                        DBList.getNamespaces();
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

    DBList.removeNamespace = function (prefix) {
        APIservice.deleteNamespace($scope.selectedDB, prefix)
                .then(function (response) {
                    DBList.getNamespaces();
                }, function (response) {
                    // an error occured
                    alert(response.status + " " + response.statusText);
                });
    };


    DBList.getData = function () {
        if ($scope.formSPARQL) {
            DBList.queryResults = [];
            var query = DBList.checkedPrefix() + $scope.formSPARQL;
            APIservice.getSparqler($scope.selectedDB, query, $scope.inference)
                    .then(function (response) {
                        $scope.sparqlRequest = response.config.url;
                        console.log(response.config.url);
                        if (response.data.results.bindings) {
                            DBList.queryResults = response.data.results.bindings;
                            console.log(DBList.queryResults);
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

    DBList.addTriple = function (database) {
        console.log('Adding on database ' + database);
        if ($scope.formSubject && $scope.formPredicate && $scope.formObject) {
            APIservice.addTriple($scope.selectedDB, $scope.formSubject, $scope.formPredicate, $scope.formObject)
                    .then(function (response) {
                        console.log(response);
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

    DBList.removeTriple = function (database) {
        if ($scope.formSubject && $scope.formPredicate && $scope.formObject) {
            APIservice.deleteTriple(database, $scope.formSubject, $scope.formPredicate, $scope.formObject)
                    .then(function (response) {
                        console.log('STATUS ' + response.status);
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

    DBList.checkedPrefix = function () {
        var prefix = "";
        angular.forEach(DBList.modelContainer, function (ns) {
            if (ns.checked) {
                prefix += 'PREFIX ' + ns.item.prefix + ': <' + ns.item.namespace + '> ';
            }
            ;
        });
        return prefix;
    };

    DBList.changeDB = function () {
        DBList.getNamespaces();
        // Clear results
        DBList.queryResults = [];
        $scope.formSPARQL = "";
    }

    DBList.addAllPrefix = function () {
        angular.forEach(DBList.modelContainer, function (ns) {
            console.log(ns);
            ns.checked = true;
        });
    }

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
    $scope.inference = false;

});
