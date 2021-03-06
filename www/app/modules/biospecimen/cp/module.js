
angular.module('os.biospecimen.cp', 
  [
    'ui.router',
    'os.biospecimen.cp.list',
    'os.biospecimen.cp.addedit',
    'os.biospecimen.cp.import',
    'os.biospecimen.cp.detail',
    'os.biospecimen.cp.consents',
    'os.biospecimen.cp.events',
    'os.biospecimen.cp.specimens'
  ])

  .config(function($stateProvider) {
    $stateProvider
      .state('cps', {
        url: '/cps',
        abstract: true,
        template: '<div ui-view></div>',
        controller: function($scope) {
          // Collection Protocol Authorization Options
          $scope.cpResource = {
            createOpts: {resource: 'CollectionProtocol', operations: ['Create']},
            updateOpts: {resource: 'CollectionProtocol', operations: ['Update']},
            deleteOpts: {resource: 'CollectionProtocol', operations: ['Delete']}
          }
        },
        parent: 'signed-in'
      })
      .state('cp-list', {
        url: '', 
        templateUrl: 'modules/biospecimen/cp/list.html',
        controller: 'CpListCtrl',
        parent: 'cps'
      })
      .state('import-biospecimen-objs', {
        url: '/bulk-import?objectType&entityType',
        templateUrl: 'modules/common/import/add.html',
        controller: 'ImportObjectCtrl',
        resolve: {
          importDetail: function($stateParams) {
            var objectType = $stateParams.objectType;
            var importDetail = {
              breadcrumbs: [{state: 'cp-list', title: 'cp.list'}],
              objectType: objectType,
              onSuccess: {state: 'cp-list', params: {}},
            };

            if (objectType == 'cpr') {
              angular.extend(importDetail, {
                title:          'participant.bulk_reg_participants',
                showImportType: false,
                importType:     'CREATE'
              });
            } else if (objectType == 'participant') {
              angular.extend(importDetail, {
                title:          'participant.bulk_update_participants',
                showImportType: false,
                importType:     'UPDATE'
              });
            } else if (objectType == 'visit') {
              importDetail.title = 'visits.bulk_import';
            } else if (objectType == 'specimen') {
              importDetail.title = 'specimens.bulk_import';
            } else if (objectType == 'extensions') {
              var entityType = $stateParams.entityType;
              var title = undefined;
              if (entityType == 'Participant') {
                title = 'extensions.bulk_import_participant_extns';
              } else if (entityType == 'SpecimenCollectionGroup') {
                title = 'extensions.bulk_import_visit_extns';
              } else if (entityType == 'Specimen') {
                title = 'extensions.bulk_import_specimen_extns';
              } else if (entityType == 'SpecimenEvent') {
                title = 'extensions.bulk_import_specimen_events';
              }

              angular.extend(importDetail, {
                title: title,
                entityType: entityType
              });
            }
 
            return importDetail;
          }
        },
        parent: 'cps'
      })
      .state('biospecimen-obj-import-jobs', {
        url: '/bulk-import-jobs',
        templateUrl: 'modules/common/import/list.html',
        controller: 'ImportJobsListCtrl',
        resolve: {
          importDetail: function() {
            return {
              breadcrumbs: [{state: 'cp-list', title: 'cp.list'}],
              title: 'cp.bulk_import_biospecimen_obj_jobs',
              objectTypes: ['cpr', 'participant', 'visit', 'specimen', 'extensions']
            }
          }
        },
        parent: 'cps'
      })
      .state('cp-addedit', {
        url: '/addedit/:cpId',
        templateUrl: 'modules/biospecimen/cp/addedit.html',
        resolve: {
          cp: function($stateParams, CollectionProtocol) {
            if ($stateParams.cpId) {
              return CollectionProtocol.getById($stateParams.cpId);
            }
            return new CollectionProtocol();
          }
        },
        controller: 'CpAddEditCtrl',
        parent: 'cps'
      })
      .state('cp-import', {
        url: '/import',
        templateUrl: 'modules/biospecimen/cp/import.html',
        controller: 'CpImportCtrl',
        parent: 'cps'
      })
      .state('cp-detail', {
        url: '/:cpId',
        templateUrl: 'modules/biospecimen/cp/detail.html',
        parent: 'cps',
        resolve: {
          cp: function($stateParams, CollectionProtocol) {
            return CollectionProtocol.getById($stateParams.cpId);
          }
        },
        controller: 'CpDetailCtrl',
        breadcrumb: {
          title: '{{cp.title}}',
          state: 'cp-detail.overview'
        }
      })
      .state('cp-detail.overview', {
        url: '/overview',
        templateUrl: 'modules/biospecimen/cp/overview.html',
        parent: 'cp-detail'
      })
      .state('cp-detail.consents', {
        url: '/consents',
        templateUrl: 'modules/biospecimen/cp/consents.html',
        parent: 'cp-detail',
        resolve: {
          consentTiers: function(cp) {
            return cp.getConsentTiers();
          }
        },
        controller: 'CpConsentsCtrl'
      })
      .state('cp-detail.events', {
        templateUrl: 'modules/biospecimen/cp/events.html',
        parent: 'cp-detail',
        resolve: {
          events: function($stateParams, cp, CollectionProtocolEvent) {
            return CollectionProtocolEvent.listFor(cp.id);
          }
        },
        controller: 'CpEventsCtrl'
      })
      .state('cp-detail.specimen-requirements', {
        url: '/specimen-requirements?eventId',
        templateUrl: 'modules/biospecimen/cp/specimens.html',
        parent: 'cp-detail.events',
        resolve: {
          specimenRequirements: function($stateParams, SpecimenRequirement) {
            var eventId = $stateParams.eventId;
            if (!eventId) {
              return [];
            }

            return SpecimenRequirement.listFor(eventId);
          }
        },
        controller: 'CpSpecimensCtrl'
      });
    });
  
