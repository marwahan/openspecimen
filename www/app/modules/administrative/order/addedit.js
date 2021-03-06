
angular.module('os.administrative.order.addedit', ['os.administrative.models', 'os.biospecimen.models'])
  .controller('OrderAddEditCtrl', function(
    $scope, $state, $translate, order, 
    Specimen, SpecimensHolder, Site, DistributionProtocol, DistributionOrder, Alerts) {
    
    function init() {
      $scope.order = order;
      $scope.dpList = [];
      $scope.siteList = [];
      $scope.userFilterOpts = {};
      $scope.input = {labelText: ''};

      loadItemStatusPvs();
      loadDps();
      setUserAndSiteList(order);

      if (!order.id && angular.isArray(SpecimensHolder.getSpecimens())) {
        order.orderItems = getOrderItems(SpecimensHolder.getSpecimens());
        SpecimensHolder.setSpecimens(null);
      } 
    }

    function loadItemStatusPvs() {
      $translate('common.home').then(
        function() {
          $scope.orderItemStatuses = DistributionOrder.getItemStatusPvs().map(
            function(status) {
              return {id: status, name: $translate.instant('orders.item_statuses.' + status)};
            }
          );
        }
      );
    }

    function loadDps() {
      DistributionProtocol.query().then(
        function(dps) {
          $scope.dpList = dps;
        }
      );
    }

    function loadSites(instituteName) {
      Site.listForInstitute(instituteName).then(
        function(sites) {
          $scope.siteList = sites;
        }
      );    
    }

    function setUserFilterOpts(institute) {
      $scope.userFilterOpts = {institute: institute};
    }

    function getInstituteName(order) {
      return !order || !order.distributionProtocol ? undefined : order.distributionProtocol.instituteName;
    }

    function setUserAndSiteList(order) {
      var instituteName = getInstituteName(order);
      setUserFilterOpts(instituteName);
      loadSites(instituteName);
    }

    function getOrderItems(specimens) {
      return specimens.map(
        function(specimen) {
          return {
            specimen: specimen,
            quantity: specimen.availableQty,
            status: 'DISTRIBUTED_AND_CLOSED'
          };
        }
      );
    }

    function saveOrUpdate(order) {
      angular.copy(order).$saveOrUpdate().then(
        function(savedOrder) {
          $state.go('order-detail.overview', {orderId: savedOrder.id});
        }
      );
    };

    $scope.onDpSelect = function() {
      setUserAndSiteList($scope.order);
    }

    $scope.addSpecimens = function() {
      var labels = $scope.input.labelText.split(/,|\t|\n/).filter(
        function(label) {
          return label.trim().length != 0;
        }
      );

      if (labels.length == 0) {
        return; 
      }

      angular.forEach(order.orderItems, function(item) {
        var idx = labels.indexOf(item.specimen.label);
        if (idx != -1) {
          labels.splice(idx, 1);
        }
      });

      if (labels.length == 0) {
        return;
      }

      Specimen.listByLabels(labels).then(
        function(specimens) {
          if (labels.length != specimens.length) {
            Alerts.error('orders.specimens_not_found_or_no_access');
          }
          order.orderItems = order.orderItems.concat(getOrderItems(specimens));
          $scope.input.labelText = '';
        }
      );
    }

    $scope.removeOrderItem = function(orderItem) {
      var idx = order.orderItems.indexOf(orderItem);
      if (idx != -1) {
        order.orderItems.splice(idx, 1);
      }
    }

    $scope.distribute = function() {
      $scope.order.status = 'EXECUTED';
      saveOrUpdate($scope.order);
    }

    $scope.saveDraft = function() {
      $scope.order.status = 'PENDING';
      saveOrUpdate($scope.order);
    }

    $scope.passThrough = function() {
      return true;
    }
    
    init();
  });
