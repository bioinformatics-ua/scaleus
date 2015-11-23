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

    $scope.$on('datasetUpdateView', function (event, datasets) {
        $scope.datasets = datasets;
        //console.log('datasetUpdateView');
    });

    $scope.changeDataset = function (selectedDataset) {
        //console.log(selectedDataset);
        SharedService.update(selectedDataset);
        //$location.path("/app/");
    };
    /* end of event to manage the selected dataset */


    $scope.showAddDatabase = function () {
        $scope.formDatabase = "";
        $scope.showFormDatabase = $scope.showFormDatabase ? false : true;
    };


    $scope.queryDatasets = function () {
        DatasetsService.query(function (response) {
            //console.log("querydatasets");
            //$scope.datasets = response;
            SharedService.updateView(response);
            $scope.changeDataset(response[0]);
            //SharedService.selectedDataset=(response[0]);
            //$scope.selectedDataset = response[0];
        });
    };

    $scope.saveDataset = function () {

        if ($scope.formDatabase) {
            DatasetsService.save({id: $scope.formDatabase}, function (response) {
                $scope.showFormDatabase = false;
                $scope.queryDatasets();
                //var savedDatasetIndex = $scope.datasets.indexOf($scope.formDatabase);
                //if (savedDatasetIndex !== -1)
                //    $scope.selectedDataset = $scope.datasets[savedDatasetIndex];

            });
        } else {
            alert("No database name was introduced");
        }
        ;

    };

    $scope.deleteDataset = function () {

        DatasetsService.delete({id: $scope.selectedDataset}, function (response) {
            $scope.queryDatasets();
        }, function (response) {
            console.log(response);
            alert("Ups! An error has occurred!");
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
                alert("Ups! An error has occurred!");
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


app.controller('TriplesCtrl', function ($scope, TriplesService, SharedService, PropAutoCompleteService, ResAutoCompleteService) {

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


    $scope.getProperties = function (val) {
        var property = PropAutoCompleteService.get({dataset: SharedService.selectedDataset, match: val});
        return property.$promise;
    };

    $scope.getResources = function (val) {
        var property = ResAutoCompleteService.get({dataset: SharedService.selectedDataset, match: val});
        return property.$promise;
    };

});


app.controller('QueriesCtrl', function ($scope, $cookies, QueriesService, NamespacesService, SharedService) {

    var RECENT_QUERIES_COOKIE = 'RECENT_QUERIES';
    console.log('on QueriesCtrl ' + SharedService.selectedDataset);

    $scope.$on('datasetChanged', function (event, dataset) {
        $scope.getNamespaces();
    });

    $scope.getData = function () {
        if ($scope.formSPARQL) {
            $scope.spinner = true;
            $scope.showQueryTime = false;
            var query = $scope.checkedPrefix() + $scope.formSPARQL;
            var startQuery = new Date().getTime();

            QueriesService.query({dataset: SharedService.selectedDataset, query: query, inference: $scope.inference, rules: $scope.rules, format: 'json'}, function (response) {
                if (response.results.bindings && response.results.bindings.length !== 0) {
                    $scope.queryTime = new Date().getTime() - startQuery;
                    $scope.showQueryTime = true;
                    $scope.noResults = false;
                    $scope.queryResults = response.results.bindings;
                    $scope.sparqlRequest = './api/v1/sparqler/' + SharedService.selectedDataset
                            + '/sparql?query=' + encodeURIComponent(query)
                            + '&inference=' + encodeURIComponent($scope.inference)
                            + '&rules=' + encodeURIComponent($scope.rules);

                    // recent queries cookies
                    var recent_queries = $cookies.get(RECENT_QUERIES_COOKIE);
                    if (recent_queries === undefined) {
                        $cookies.put(RECENT_QUERIES_COOKIE, '["' + encodeURIComponent(query) + '"]');
                    } else {
                        try {
                            var array = JSON.parse(recent_queries);
                            query = encodeURIComponent(query);
                            if (array.indexOf(query) === -1)
                                array.push(query);
                            if (array.length > 20)
                                array.splice(0, 1);
                            $cookies.put(RECENT_QUERIES_COOKIE, JSON.stringify(array));
                        } catch (error) {
                            console.log(error);
                        }
                    }
                    $scope.updateRecentQueries();

                } else {
                    $scope.noResults = true;
                    $scope.showQueryTime = false;
                    $scope.queryResults = [];
                }
                $scope.spinner = false;
            }, function (response) {
                // an error occured
                $scope.showQueryTime = false;
                $scope.noResults = true;
                $scope.queryResults = [];
                $scope.spinner = false;
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

    $scope.updateRecentQueries = function () {
        var recent_queries = $cookies.get(RECENT_QUERIES_COOKIE);
        if (recent_queries === undefined || recent_queries === '') {
            $scope.recentQueries = undefined;
        } else {
            try {
                var array = JSON.parse(recent_queries);
                array = array.map(decodeURIComponent);
                $scope.recentQueries = array;
            } catch (error) {
                console(error);
            }
        }
    };

    $scope.clearRecentQueries = function () {
        $cookies.remove(RECENT_QUERIES_COOKIE);
        $scope.updateRecentQueries();
    };

    $scope.putQuery = function (query) {
        $scope.formSPARQL = query;
    };

    // init
    $scope.showQueryTime = false;
    $scope.spinner = false;
    $scope.inference = false;
    $scope.queryResults = [];
    $scope.noResults = false;
    $scope.getNamespaces();
    $scope.updateRecentQueries();

});


app.controller('ResourceCtrl', function ($scope, $routeParams, ResourceService, SharedService) {

    $scope.dataset = $routeParams.database;
    $scope.prefix = $routeParams.prefix;
    $scope.resource = $routeParams.resource;

    var uriToPrefix = function (uri, prefixes) {
        var resource;
        var uriToSlit = uri;
        if (uriToSlit.indexOf("#") !== -1)
            resource = uriToSlit.split('#').pop();
        else
            resource = uriToSlit.split('/').pop();
        var namespace = uri.substr(0, uri.length - resource.length);

        var prefix = function () {
            for (var key in prefixes) {
                //console.log(key);
                if (prefixes[key] === namespace)
                    return key;
            }
        };

        var map = {
            "namespace": namespace,
            "resource": resource,
            "prefix": prefix()
        };

        return map;
    };

    $scope.getResource = function () {
        ResourceService.get({dataset: $scope.dataset, prefix: $scope.prefix, resource: $scope.resource, format: 'ttl'}, function (response) {

            var parser = N3.Parser(), triples = [], pref = [], N3Util = N3.Util;
            parser.parse(function (error, triple, prefixes) {
                if (triple)
                    triples.push(triple);
                if (prefixes)
                    pref = (prefixes);
            });
            parser.addChunk(response.content);
            parser.end();

            var resource = [];
            if (triples.length !== 0) {
                var content = [];
                triples.forEach(function (triple) {
                    var pred_transf = uriToPrefix(triple.predicate, pref);
                    var predicate = {uri: triple.predicate, type: pred_transf.namespace, value: pred_transf.resource, prefix: pred_transf.prefix};

                    if (N3Util.isIRI(triple.object)) {
                        var obj_transf = uriToPrefix(triple.object, pref);
                        var object = {uri: triple.object, type: obj_transf.namespace, value: obj_transf.resource, prefix: obj_transf.prefix};
                    } else {
                        var object = {value: triple.object};
                    }
                    content.push({p: predicate, o: object});
                });
                resource = {uri: triples[0].subject, prefix: $scope.prefix, value: $scope.resource, dataset: $scope.dataset, content: content};
                //console.log(JSON.stringify(resource, null, 2));
            }
            $scope.resource = resource;

        }, function (response) {
            // an error occured
            alert(response.status + " " + response.statusText);
        });
    };

    $scope.getResource();

});


app.controller('FileUploadCtrl', function ($scope, FileUploader, SharedService) {

    $scope.dataset = SharedService.selectedDataset;

    $scope.$on('datasetChanged', function (event, dataset) {
        $scope.dataset = SharedService.selectedDataset;
    });

    var uploader = $scope.uploader = new FileUploader({
        url: './api/v1/upload/' + $scope.dataset
    });
    
   $scope.$on('$locationChangeStart', function( event ) {
    	var items = uploader.getNotUploadedItems();

    	if (items.length > 0) {
	    	var answer = confirm("There are files in the upload queue not yet imported. Are you sure you want to quit?");
	        if (!answer) {
	            event.preventDefault();
	        } 
    	}
    });
	   
    // FILTERS

	var formats = ['ttl', 'rdf', 'owl', 'nt', 'nq', 'jsonld', 'rj', 'n3', 'trig', 'trix', 'trdf', 'rt'];
	
    uploader.filters.push({
        name: 'customFilter',
        fn: function (item /*{File|FileLikeObject}*/, options) {
            return this.queue.length < 10;
        }
    });
    
    uploader.filters.push({
        name: 'formatFilter',
        fn: function(item /*{File|FileLikeObject}*/, options) {
        	console.log(item.name);
            return (formats.indexOf(item.name.toLowerCase().slice(item.name.lastIndexOf('.')+1)) > -1);
        }
    });

    
    // CALLBACKS

    uploader.onWhenAddingFileFailed = function (item /*{File|FileLikeObject}*/, filter, options) {
        console.info('onWhenAddingFileFailed', item, filter, options);
    };
    uploader.onAfterAddingFile = function (fileItem) {
        console.info('onAfterAddingFile', fileItem);
    };
    uploader.onAfterAddingAll = function (addedFileItems) {
        console.info('onAfterAddingAll', addedFileItems);
    };
    uploader.onBeforeUploadItem = function (item) {
        console.info('onBeforeUploadItem', item);
        item.url = './api/v1/upload/' + $scope.dataset;
    };
    uploader.onProgressItem = function (fileItem, progress) {
        console.info('onProgressItem', fileItem, progress);
    };
    uploader.onProgressAll = function (progress) {
        console.info('onProgressAll', progress);
    };
    uploader.onSuccessItem = function (fileItem, response, status, headers) {
        console.info('onSuccessItem', fileItem, response, status, headers);
    };
    uploader.onErrorItem = function (fileItem, response, status, headers) {
        console.info('onErrorItem', fileItem, response, status, headers);
    };
    uploader.onCancelItem = function (fileItem, response, status, headers) {
        console.info('onCancelItem', fileItem, response, status, headers);
    };
    uploader.onCompleteItem = function (fileItem, response, status, headers) {
        console.info('onCompleteItem', fileItem, response, status, headers);
    };
    uploader.onCompleteAll = function () {
        console.info('onCompleteAll');
    };

    console.info('uploader', uploader);

});
