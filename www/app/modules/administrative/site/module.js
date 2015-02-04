
angular.module('os.administrative.site',
  [
    'ui.router',
    'os.administrative.site.list',
    'os.administrative.site.detail',
    'os.administrative.site.addedit'
  ])

  .config(function($stateProvider) {
    $stateProvider
      .state('site-list', {
        url: '/sites',
        templateUrl: 'modules/administrative/site/list.html',
        controller: 'SiteListCtrl',
        parent: 'signed-in'
      })
      .state('site-new', {
        url: '/new-site',
        templateUrl: 'modules/administrative/site/addedit.html',
        controller: 'SiteAddEditCtrl',
        parent: 'signed-in'
      })
      .state('site-detail', {
        url: '/sites/:siteId',
        templateUrl: 'modules/administrative/site/detail.html',
        resolve: {
          site: function($stateParams, Site) {
            return Site.getById($stateParams.siteId);
          }
        },
        controller: 'SiteDetailCtrl',
        parent: 'signed-in'
      })
      .state('site-detail.overview', {
        url: '/overview',
        templateUrl: 'modules/administrative/site/overview.html',
        controller: function() {
        },
        parent: 'site-detail'
      });
  });