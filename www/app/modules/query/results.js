
angular.module('os.query.results', ['os.query.models'])
  .controller('QueryResultsCtrl', function(
    $scope, $state, $stateParams, $modal, 
    queryCtx, QueryUtil, QueryExecutor, SpecimenList, QueryCtxHolder, SpecimensHolder, Alerts) {

    function init() {
      $scope.queryCtx = queryCtx;
      $scope.selectedRows = [];

      $scope.resultsCtx = {
        waitingForRecords: true,
        error: false,
        moreData: false,
        columnDefs: [],
        rows: [],
        numRows: 0,
        labelIndices: [],
        gridOpts: getGridOpts()
      }

      executeQuery($stateParams.editMode);
    }

    function getGridOpts() {
      return {
        columnDefs        : 'resultsCtx.columnDefs',
        data              : 'resultsCtx.rows',
        enableColumnResize: true,
        showFooter        : true,
        totalServerItems  : 'resultsCtx.numRows',
        plugins           : [gridFilterPlugin],
        headerRowHeight   : 70,
        selectedItems     : $scope.selectedRows,
        enablePaging      : false
      };
    }

    function executeQuery(editMode) {
      var queryCtx = QueryCtxHolder.getQueryCtx();
      if ($stateParams.isRedirected && !!queryCtx) {
        $scope.queryCtx = queryCtx;
        editMode = true;
      }

      if (!editMode && isParameterized()) {
        showParameterizedFilters();
      } else {
        loadRecords();
      }

      QueryCtxHolder.setQueryCtx(undefined);
    }

    function loadAllSpecimenList() {
      SpecimenList.query().then(
        function(lists) {
          $scope.specimenLists = lists;
        }
      );
    }

    function isParameterized() {
      var filters = $scope.queryCtx.filters;
      for (var i = 0; i < filters.length; ++i) {
        if (filters[i].parameterized) {
          return true;
        }
      }

      return false;
    }

    function showParameterizedFilters() {
      var modal = $modal.open({
        templateUrl: 'modules/query/parameters.html',
        controller: 'ParameterizedFilterCtrl',
        resolve: {
          queryCtx: function() {
            return $scope.queryCtx;
          }
        },
        size: 'lg'
      });

      modal.result.then(
        function(result) {
          if (result) {
            $scope.queryCtx = result;
          }

          loadRecords();
        }
      );
    };

    function loadRecords() {
      var qc = $scope.queryCtx;
      $scope.showAddToSpecimenList = showAddToSpecimenList();
      $scope.resultsCtx.waitingForRecords = true;
      $scope.resultsCtx.error = false;
      QueryExecutor.getRecords(undefined, qc.selectedCp.id, getAql(), 'DEEP').then(
        function(result) {
          $scope.resultsCtx.waitingForRecords = false;
          if (qc.reporting.type == 'crosstab') {
            preparePivotTable(result);
          } else {
            var showColSummary = qc.reporting.type == 'columnsummary';
            prepareDataGrid(showColSummary, result);
            $scope.resultsCtx.gridOpts.headerRowHeight = showColSummary ? 101 : 70;
          }
        },

        function() {
          $scope.resultsCtx.waitForRecords = false;
          $scope.resultsCtx.error = true;
        }
      );
    }

    function getAql() { 
      var qc = $scope.queryCtx;
      return QueryUtil.getDataAql(
        qc.selectedFields, 
        qc.filtersMap, 
        qc.exprNodes, 
        qc.reporting);
    }

    function preparePivotTable(result) {
      $scope.resultsCtx.rows = result.rows;
      $scope.resultsCtx.columnLabels = result.columnLabels;

      $scope.resultsCtx.pivotTableOpts = {
        height: '500px',
        width: '1200px',
        colHeaders: $scope.resultsCtx.columnLabels,
        numGrpCols: $scope.queryCtx.reporting.params.groupRowsBy.length,
        multipleVal: $scope.queryCtx.reporting.params.summaryFields.length > 1,
        data: $scope.resultsCtx.rows
      };
    };

    function prepareDataGrid(showColSummary, result) {
      var idx = -1,
          summaryRow = [];

      if (showColSummary && !!result.rows && result.rows.length > 0) {
        summaryRow = result.rows.splice(result.rows.length - 1, 1)[0];
      }

      var colDefs = result.columnLabels.map(
        function(columnLabel) {
          ++idx;
          return {
            field: "col" + idx,
            displayName: columnLabel,
            width: 100,
            headerCellTemplate: 'modules/query/column-filter.html',
            showSummary: showColSummary,
            summary: summaryRow[idx]
          };
        }
      );

      $scope.resultsCtx.columnDefs = colDefs;
      $scope.resultsCtx.labelIndices = result.columnIndices;
      $scope.resultsCtx.rows = getFormattedRows(result.rows);
      $scope.resultsCtx.numRows = result.rows.length;
      $scope.resultsCtx.moreData = (result.dbRowsCount >= 10000);
      $scope.selectedRows.length = 0;

      /** Hack to make grid resize **/
      window.setTimeout(function(){
        $(window).resize();
        $(window).resize();
      }, 500);
    }

    function getFormattedRows(rows) {
      var formattedRows = [];
      for (var i = 0; i < rows.length; ++i) {
        var formattedRow = {};
        for (var j = 0; j < rows[i].length; ++j) {
          formattedRow["col" + j] = rows[i][j];
        }
        formattedRows.push(formattedRow);
      }
      return formattedRows;
    }

    function showAddToSpecimenList() {
      if ($scope.queryCtx.selectedFields.indexOf('Specimen.label') != -1) {
        loadAllSpecimenList();
        return true;
      }

      return false;
    };

    function getSelectedSpecimens() {
      var idx = $scope.queryCtx.selectedFields.indexOf('Specimen.label');
      var colName = $scope.resultsCtx.columnDefs[idx].field;
      return $scope.selectedRows.map(function(row) {
         return {label: row[colName]};
      })
    }

    var gridFilterPlugin = {
      init: function(scope, grid) {
        gridFilterPlugin.scope = scope;
        gridFilterPlugin.grid = grid;
        $scope.$watch(
          function() {
            var searchQuery = "";
            angular.forEach(
              gridFilterPlugin.scope.columns, 
              function(col) {
                if (col.visible && col.filterText) {
                  var filterText = '';
                  if (col.filterText.indexOf('*') == 0 ) {
                    filterText = col.filterText.replace('*', '');
                  } else {
                    filterText = col.filterText;
                  }
                  filterText += ";";
                  searchQuery += col.displayName + ": " + filterText;
                }
              }
            );
            return searchQuery;
          },

          function(searchQuery) {
            gridFilterPlugin.scope.$parent.filterText = searchQuery;
            gridFilterPlugin.grid.searchProvider.evalFilter();
          }
        );
      },
      scope: undefined,
      grid: undefined
    };

    $scope.editFilters = function() {
      $state.go('query-addedit', {queryId: $scope.queryCtx.id});
    }

    $scope.defineView = function() {
      var mi = $modal.open({
        templateUrl: 'modules/query/define-view.html',
        controller: 'DefineViewCtrl',
        resolve: {
          queryCtx: function() {
            return $scope.queryCtx;
          }
        },
        size: 'lg'
      });

      mi.result.then(
        function(queryCtx) {
          $scope.queryCtx = queryCtx;
          QueryUtil.disableCpSelection(queryCtx);
          loadRecords();
        }
      );
    }

    $scope.rerun = function() {
      executeQuery(false);
    }

    $scope.downloadResults = function() {
      var qc = $scope.queryCtx;

      var alert = Alerts.info('queries.export_initiated', {}, false);  
      QueryExecutor.exportQueryResultsData(undefined, qc.selectedCp.id, getAql(), 'DEEP').then(
        function(result) {
          Alerts.remove(alert);
          if (result.completed) {
            Alerts.info('queries.downloading_data_file');
            QueryExecutor.downloadDataFile(result.dataFile);
          } else if (result.dataFile) {
            Alerts.info('queries.data_file_will_be_emailed');
          }
        },

        function() {
          Alerts.remove(alert);
        }
      );
    };

    $scope.selectAllRows = function() {
      $scope.resultsCtx.gridOpts.selectAll(true);
      $scope.resultsCtx.selectAll = true;
    };

    $scope.unSelectAllRows = function() {
      $scope.resultsCtx.gridOpts.selectAll(false);
      $scope.resultsCtx.selectAll = false;
    };

    $scope.addSelectedSpecimensToSpecimenList = function(list) {
      list.addSpecimens(getSelectedSpecimens()).then(
        function(specimens) {
          Alerts.success('specimen_list.specimens_added', {name: list.name});
        }
      );
    }

    $scope.createNewSpecimenList = function() {
      QueryCtxHolder.setQueryCtx($scope.queryCtx);
      SpecimensHolder.setSpecimens(getSelectedSpecimens());
      $state.go('specimen-list-addedit', {listId: '', page: 'query'});
    };
 
    init();
  });
