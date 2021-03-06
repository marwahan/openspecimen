
angular.module('openspecimen')
  .filter('osNoValue', function() {
    return function(input, placeholder) {
      var result = placeholder || 'Not Specified';

      if (angular.isUndefined(input) || input === null) {
        return result;
      }

      if (angular.isNumber(input) || angular.isDate(input)) {
        return input; 
      }

      if (angular.isString(input) && input.trim().length == 0) {
        return result;
      }

      return input;
    }
  });
