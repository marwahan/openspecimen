angular.module('os.biospecimen.models.participant', ['os.common.models'])
  .factory('Participant', function(osModel, $http) {
    var Participant = osModel('participants');
 
    Participant.formatSsn = function(ssn) {
      if (!!ssn && ssn.indexOf('-') == -1) {
        ssn = [ssn.slice(0, 3), '-', ssn.slice(3, 5), '-', ssn.slice(5)].join('');
      }

      return ssn;
    };

    Participant.prototype.newPmi = function() {
      return {siteName: '', mrn: ''};
    };

    Participant.prototype.addPmi = function(pmi) {
      if (!this.pmis) {
        this.pmis = [];
      }

      this.pmis.push(pmi);
    };

    Participant.prototype.removePmi = function(pmi) {
      var idx = this.pmis ? this.pmis.indexOf(pmi) : -1;
      if (idx != -1) {
        this.pmis.splice(idx, 1);
      }

      return idx;
    };

    Participant.prototype.formatSsn = function() {
      return Participant.formatSsn(this.ssn);
    };

    Participant.prototype.getPmis = function() {
      var pmis = []; 
      angular.forEach(this.pmis, function(pmi) {
        if (pmi.siteName && pmi.mrn) {
          pmis.push(pmi);
        }
      });

      return pmis;
    };

    Participant.prototype.getMrnSites = function() {
      if (!this.pmis) {
        return [];
      }

      return this.pmis.map(function(pmi) { return pmi.siteName; });
    };

    Participant.prototype.isMatchingInfoPresent = function() {
      return (this.lastName && this.birthDate) ||
             this.empi ||
             this.ssn ||
             this.getPmis().length > 0;
    };

    Participant.prototype.getMatchingCriteria = function() {
      return {
        lastName: this.lastName,
        birthDate: this.birthDate,
        empi: this.empi,
        ssn : Participant.formatSsn(this.ssn),
        pmis: this.getPmis()
      };
    };

    Participant.prototype.getMatchingParticipants = function() {
      var criteria = this.getMatchingCriteria();
      return $http.post(Participant.url() + '/match', criteria)
        .then(function(result) {
          var response = result.data;
          angular.forEach(response, function(matched) {
            matched.participant = new Participant(matched.participant);
          });
          return response;
        });
    };

    Participant.prototype.$saveProps = function() {
      var pmis = this.getPmis();
      this.pmis = pmis.length == 0 ? [] : pmis;
      this.ssn = this.formatSsn();
      return this;
    };

    return Participant;
  });
