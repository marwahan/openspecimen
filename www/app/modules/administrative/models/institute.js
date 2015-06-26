angular.module('os.administrative.models.institute', ['os.common.models'])
  .factory('Institute', function(osModel, $http, $q) {
    var Institute = osModel('institutes');

    Institute.prototype.getType = function() {
      return 'institute';
    }

    Institute.prototype.getDisplayName = function() {
      return this.name;
    }

    Institute.prototype.newDepartment = function() {
      return {id: '', name: ''}
    };

    Institute.prototype.addDepartment = function(department) {
      if(!this.departments) {
        this.departments = [];
      }

      this.departments.push(department);
    }

    Institute.prototype.removeDepartment = function(department) {
      var idx = this.departments ? this.departments.indexOf(department) : -1;
      if (idx != -1) {
        this.departments.splice(idx, 1);
      }

      return idx;
    };

    Institute.prototype.$saveProps = function() {
      var departmentNames = {};

      this.departments = this.departments.filter(
        function(department) {
          if(department.name in departmentNames) {
            return false;
          } else {
            departmentNames[department.name] = true;
            return true;
          }
        }
      );

      return this;
    }

    Institute.getByName = function (instituteName) {
      return $http.get(Institute.url() + 'byname?name=' + instituteName).then(
        function(result) {
              console.warn(result.name);

          return result ? result.data : null;
        });
    }

    return Institute;
  });

