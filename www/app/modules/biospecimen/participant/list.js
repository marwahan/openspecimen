
angular.module('os.biospecimen.participant.list', ['os.biospecimen.models'])
  .controller('ParticipantListCtrl', function(
    $scope, $state, $stateParams, $modal, $q, osRightDrawerSvc,
    cp, CollectionProtocolRegistration, Util, CpConfigSvc) {

    function init() {
      $scope.cpId = $stateParams.cpId;
      $scope.cp = cp;
      $scope.filterOpts = {};
      $scope.participantResource = {
        registerOpts: {resource: 'ParticipantPhi', operations: ['Create'], cp: $scope.cp.shortTitle},
      }
      loadParticipants();
      Util.filter($scope, 'filterOpts', loadParticipants);
      showOrHideBulkRegBtn();
    }

    function loadParticipants() {
      CollectionProtocolRegistration.listForCp($scope.cpId, true, $scope.filterOpts).then(
        function(cprList) {
          if (!$scope.cprList && cprList.length > 12) {
            //
            // Show search options when number of participants are more than 12
            //
            osRightDrawerSvc.open();
          }

          $scope.cprList = cprList;
        }
      )
    }

    function showOrHideBulkRegBtn() {
      $q.when(CpConfigSvc.getBulkRegParticipantTmpl($scope.cpId, -1)).then(
        function(tmpl) {
          $scope.showBulkRegBtn = tmpl != undefined;
        }
      );
    };

    $scope.clearFilters = function() {
      $scope.filterOpts = {};
    };

    $scope.showParticipantOverview = function(cpr) {
      $state.go('participant-detail.overview', {cprId: cpr.cprId});
    };

    init();
  });
