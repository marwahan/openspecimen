
angular.module('os.biospecimen.participant.specimen-position')
  .controller('SpecimenPositionSelectorCtrl', 
    function($scope, $modalInstance, $timeout, $q, specimen, cpId, Container) {
      var queryParams = {
        specimenClass: specimen.specimenClass,
        specimenType: specimen.type,
        onlyFreeContainers: true,
        cpId: cpId,
        storeSpecimensEnabled: true,
        hierarchical: true
      };

      var extend = angular.extend;

      function addChildPlaceholders(containers) {
        angular.forEach(containers, function(container) {
          container.childContainers = [{id: -1, name: 'Loading ...'}];
          container.childContainersLoaded = false;
        });
      }

      function init() {
        $scope.selectedContainer = undefined;
        $scope.selectedPos = {};
        $scope.showGrid = false;
        $scope.containers = [];

        Container.query(extend({topLevelContainers: true}, queryParams)).then(
          function(containers) {
            addChildPlaceholders(containers);
            $scope.containers = Container.flatten(containers);
          }
        );
      };

      $scope.toggleSelectedContainer = function(wizard, container) {
        $scope.showGrid = false;

        if ($scope.selectedContainer && $scope.selectedContainer != container) {
          $scope.selectedContainer.selected = false;
        }

        container.selected = !container.selected;
        if (container.selected) {
          $scope.selectedPos = {id: container.id, name: container.name};
          $scope.selectedContainer = container;
          wizard.next(false);
        } else {
          $scope.selectedPos = {};
          $scope.selectedContainer = undefined;
        }
      };

      $scope.getOccupancyMap = function() {
        if ($scope.selectedContainer.occupiedPositions) {
          $scope.showGrid = true;
          return true;
        }

        return Container.getById($scope.selectedContainer.id).then(
          function(container) {
            angular.extend($scope.selectedContainer, container);
            $scope.showGrid = true;
            return true;
          }
        );
      };

      $scope.loadChildren = function(container) {
        if (container.childContainersLoaded) {
          return;
        }

        var idx = $scope.containers.indexOf(container);
        Container.query(extend({parentContainerId: container.id}, queryParams)).then(
          function(containers) {
            addChildPlaceholders(containers);
            container.childContainersLoaded = true;

            var childContainers = Container._flatten(containers, 'childContainers', container, container.depth + 1);
            var args = [idx + 1, 1].concat(childContainers)
            Array.prototype.splice.apply($scope.containers, args);
            if (containers.length == 0) {
              container.hasChildren = false;
            }
          }
        );
      };

      $scope.cancel = function() {
        $modalInstance.dismiss('cancel');
      };

      $scope.ok = function() {
        if (!$scope.selectedContainer) {
          $scope.cancel();
        } else {
          $modalInstance.close($scope.selectedPos);
        }
      };
             
      init();
    }
  );
