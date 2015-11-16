/**
 * @lgonzalez
 */

var app = angular.module('restapp', [
    'ngRoute',
    'ngResource',
    'ngCookies',
    'restapp.controllers',
    'restapp.services',
    'restapp.directives',
    'ui.bootstrap'
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
                when("/resource/:database/:prefix/:resource", {
                    templateUrl: "partials/resource.html"
                }).
                otherwise({redirectTo: '/index'});
    }]);
