/**
 * @lgonzalez
 */

var app = angular.module('restapp', [
    'ngRoute',
    'ngResource',
    'restapp.controllers',
    'restapp.services',
    'restapp.directives',
    'ui.bootstrap',
    'angularFileUpload'
]);

app.config(['$routeProvider', function ($routeProvider) {
        $routeProvider.
                when("/index", {
                    templateUrl: "partials/dashboard.html"
                }).
                when("/sparqler", {
                    templateUrl: "partials/sparqler.html"
                }).
                when("/namespaces", {
                    templateUrl: "partials/namespaces.html"
                }).
                when("/triples", {
                    templateUrl: "partials/triples_manager.html"
                }).
                when("/data", {
                    templateUrl: "partials/data.html"
                }).
                when("/fileupload", {
                    templateUrl: "partials/file_upload.html"
                }).
                when("/resource/:database/:prefix/:resource", {
                    templateUrl: "../../../app/partials/resource.html"
                }).
                when("/resource", {
                    templateUrl: "/app/partials/resource.html"
                }).
                otherwise({redirectTo: '/index'});
    }]);
