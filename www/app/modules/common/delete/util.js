angular.module('os.common.delete')
  .factory('DeleteUtil', function($modal, $state, $translate) {
  
    function deleteEntity(object, props) {
      var confirmDelete = props.confirmDelete ? props.confirmDelete : 'delete_entity.confirm_delete';

      var modalInstance = $modal.open({
        templateUrl: 'modules/common/delete/modal.html',
        controller: 'EntityDeleteCtrl',
        resolve: {
          entityProps: function() {
            return {
              entity: object,
              entityName: object.getDisplayName(),
              entityType: $translate.instant("entities." + object.getType()),
              confirmDelete: confirmDelete
            }
          },
          dependentEntities: function() {
            return props.deleteWithoutCheck ? [] :  object.getDependentEntities();
          }
        }
      });

      modalInstance.result.then(function(object) {
        if (typeof props.onDeletion == 'function') {
          props.onDeletion();
        } else {
          $state.go(props.onDeleteState);
        }
      });
    }

    function confirmDelete(opts) {
      var modalInstance = $modal.open({
        templateUrl: opts.templateUrl,
        controller: function($scope, $modalInstance) {
          $scope.entity = opts.entity;

          $scope.ok = function() {
            $modalInstance.close(true);
          }

          $scope.cancel = function() {
            $modalInstance.dismiss('cancel');
          }
        }
      });

      modalInstance.result.then(
        function() {
          opts.delete();
        }
      );
    };


    return {
      delete : deleteEntity,

      confirmDelete: confirmDelete
    }

  });
