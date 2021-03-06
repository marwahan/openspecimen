angular.module('os.administrative.models.setting', ['os.common.models'])
  .factory('Setting', function(osModel, $http) {
    var Setting = new osModel('config-settings');

    Setting.getLocale = function() {
      return $http.get(Setting.url() + 'locale').then(
        function(resp) {
          return resp.data;
        }
      );
    }

    return Setting;
  });
