
angular.module('openspecimen')
  .config(function($stateProvider) {
    $stateProvider
      .state('login', {
        url: '/?logout',
        templateUrl: 'modules/user/signin.html',
        controller: 'LoginCtrl',
        parent: 'default'
      })
      .state('forgot-password', {
        url: '/forgot-password',
        templateUrl: 'modules/user/forgot-password.html',
        controller: 'ForgotPasswordCtrl',
        parent: 'default'
      })
      .state('reset-password', {
        url: '/reset-password',
        templateUrl: 'modules/user/reset-password.html',
        controller: 'ResetPasswordCtrl',
        parent: 'default'
      })
      .state('sign-up', {
        url: '/sign-up',
        templateUrl: 'modules/user/signup.html',
        resolve: {
          user: function(User) {
            return new User();
          }
        },
        controller: 'UserAddEditCtrl',
        parent: 'default'
      });
  });


