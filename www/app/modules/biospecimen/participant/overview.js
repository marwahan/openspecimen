
angular.module('os.biospecimen.participant.overview', ['os.biospecimen.models'])
  .controller('ParticipantOverviewCtrl', function($scope, visits, Visit) {

    function init() {
      $scope.occurredVisits    = Visit.completedVisits(visits);
      $scope.anticipatedVisits = Visit.anticipatedVisits(visits);
      addMaskMarker();
    }

    function addMaskMarker() {
      if ($scope.cpr.participant.phiAccess) {
        return;
      }

      $scope.cpr.participant.birthDate = "###";
    }

    init();
  });
