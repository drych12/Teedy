'use strict';

/**
 * User profile controller.
 */
angular.module('docs').controller('UserProfile', function($stateParams, Restangular, $scope, User, $uibModal, $dialog, $translate) {
  // Load user info
  User.userInfo().then(function(data) {
    $scope.userInfo = data;
  });
  
  // Load user
  Restangular.one('user', $stateParams.username).get().then(function(data) {
    $scope.user = data;
  });
  
  // 处理注册请求
  $scope.requestRegistration = function() {
    var modalInstance = $uibModal.open({
      templateUrl: 'partial/docs/usergroup/user.register.html',
      controller: 'UserRegisterModalCtrl',
      resolve: {
        user: function() {
          return {
            username: '',
            password: '',
            passwordConfirm: '',
            email: '',
            message: ''
          };
        }
      }
    });

    modalInstance.result.then(function(data) {
      if (data) {
        // 发送注册请求到后端API
        Restangular.one('user/register').put({
          username: data.username,
          password: data.password,
          email: data.email,
          message: data.message
        }).then(function() {
          // 注册请求提交成功
          var successTitle = '申请已发送';
          var successMsg = '您的注册申请已发送给管理员，请等待审核。';
          var successBtns = [{ result: 'ok', label: '确定', cssClass: 'btn-primary' }];
          $dialog.messageBox(successTitle, successMsg, successBtns);
        }, function(response) {
          // 处理错误情况
          var errorTitle = '申请失败';
          var errorMsg = '提交注册申请时出现错误';
          
          if (response.data && response.data.type === 'AlreadyExistingUsername') {
            errorMsg = '用户名已存在，请选择其他用户名';
          } else if (response.data && response.data.type === 'AlreadyExistingRequest') {
            errorMsg = '您已经有一个待处理的注册申请，请等待管理员审核';
          } else if (response.data && response.data.type === 'ValidationError') {
            errorMsg = '请检查您的输入是否正确';
          }
          
          var errorBtns = [{ result: 'ok', label: '确定', cssClass: 'btn-primary' }];
          $dialog.messageBox(errorTitle, errorMsg, errorBtns);
        });
      }
    });
  };
});

/**
 * User register dialog controller.
 */
angular.module('docs').controller('UserRegisterModalCtrl', function($scope, $uibModalInstance, user) {
  $scope.user = angular.copy(user);
  $scope.error = '';

  $scope.cancel = function() {
    $uibModalInstance.dismiss('cancel');
  };
  
  $scope.close = function() {
    if ($scope.user.password !== $scope.user.passwordConfirm) {
      $scope.error = '两次输入的密码不一致';
      return;
    }
    if (!$scope.user.username || !$scope.user.password || !$scope.user.email) {
      $scope.error = '请填写所有必填项';
      return;
    }
    $uibModalInstance.close($scope.user);
  };
});