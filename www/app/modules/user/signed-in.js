angular.module('openspecimen')
  .controller('SignedInCtrl', function($scope, $rootScope, $state, currentUser, Alerts, AuthorizationService) {
     function init() {
       $scope.alerts = Alerts.messages;
       $rootScope.currentUser = currentUser;
     }

     $scope.userCreateUpdateOpts = {resource: 'User', operations: ['Create', 'Update']};
     $scope.cpReadOpts = {resource: 'CollectionProtocol', operations: ['Read']};
     $scope.containerReadOpts = {resource: 'StorageContainer', operations: ['Read']};
     $scope.orderReadOpts = {resource: 'Order', operations: ['Read']};

     init();
  })
