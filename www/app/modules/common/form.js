angular.module('os.common.form', [])
  .directive('osFormValidator', function() {
    return {
      restrict: 'A',

      controller: function($scope) {
        this._formSubmitted = false;

        this._parentValidator = undefined;

        this._form = undefined;

        this.formSubmitted = function(val) {
          this._formSubmitted = val;
        };

        this.isFormSubmitted = function() {
          return this._formSubmitted;
        };

        this.setForm = function(form) {
          this._form = form;
        };

        this.getForm = function() {
          return this._form;
        };

        this.isValidForm = function() {
          return !this._form.$invalid;
        };

        this.isInteracted = function(field) {
          if (this._parentValidator && this._parentValidator.isFormSubmitted()) {
            return true;
          }
            
          return this.isFormSubmitted() || (field && field.$dirty);
        };

        this.setParentValidator = function(parentValidator) {
          this._parentValidator = parentValidator;
        };
      },

      link: function(scope, element, attrs, controller) {
        scope.$watch(attrs.osFormValidator, function(form) {
          controller.setForm(form);
        });

        if (attrs.validator) {
          scope[attrs.validator] = controller;
        }

        if (attrs.parentValidator) {
          scope.$watch(attrs.parentValidator, function(parentValidator) {
            controller.setParentValidator(parentValidator);
          });
        }
      }
    };
  })

  .directive('osFormSubmit', function($document, Alerts) {
    function onSubmit(scope, ctrl, submitHandler) {
      var form = ctrl.getForm();
      if (form.$pending) {
        var pendingWatch = scope.$watch(
          function() {
            return form.$pending;
          },

          function(pending) {
            if (!pending) {
              pendingWatch();
              onSubmit(scope, ctrl, submitHandler);
            }
          }
        );
      } else {
        ctrl.formSubmitted(true);
        if (ctrl.isValidForm()) {
          scope.$eval(submitHandler);
        } else {
          Alerts.error("common.form_validation_error");
        }
      }
    }

    var cnt = 0;

    return {
      restrict: 'A',
  
      require: '^osFormValidator',

      priority: 1,

      link: function(scope, element, attrs, ctrl) {
        if (attrs.mousedownClick == 'true') {
          element.bind('mousedown', function() {
            ++cnt;
            var eventName = "mouseup.formsubmit." + cnt;
            $document.on(eventName, function() {
              $document.unbind(eventName);
              onSubmit(scope, ctrl, attrs.osFormSubmit);
            });
          })
        } else {
          element.bind('click', function() {
            onSubmit(scope, ctrl, attrs.osFormSubmit);
          });
        }
      }
    };
  })

  .directive('osFieldError', function() {
    return {
      restrict: 'A',

      require: '^osFormValidator',

      scope: {
        field: '='
      },

      link: function(scope, element, attrs, ctrl) {
        scope.isInteracted = function() {
          return ctrl.isInteracted(scope.field);
        };
      },

      replace: 'true',

      template: 
        '<div>' +
        '  <div ng-if="isInteracted()" class="alert alert-danger os-form-err-msg" ' +
        '    ng-messages="field.$error" ng-messages-include="modules/common/error-messages.html"> ' +
        '  </div>' +
        '</div>'
    };
  });
