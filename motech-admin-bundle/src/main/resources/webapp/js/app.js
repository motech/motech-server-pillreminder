'use strict';

/* App Module */

angular.module('motech-admin', ['bundleServices', 'messageServices', 'platformSettingsServices', 'localization',
    'motech-widgets', 'moduleSettingsServices', 'ngCookies']).config(['$routeProvider', function($routeProvider) {
      $routeProvider.
          when('/bundles', {templateUrl: 'partials/bundles.html', controller: BundleListCtrl}).
          when('/settings', {templateUrl: 'partials/settings.html', controller: SettingsCtrl}).
          when('/bundle/:bundleId', {templateUrl: 'partials/bundle.html', controller: ModuleCtrl}).
          when('/bundleSettings/:bundleId', {templateUrl: 'partials/bundleSettings.html', controller: BundleSettingsCtrl}).
          otherwise({redirectTo: '/bundles'});
}]);