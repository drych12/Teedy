'use strict';

/**
 * Settings user page controller.
 */
angular.module('docs').controller('SettingsUser', function($scope, $state, Restangular, $dialog) {
  /**
   * Load users from server.
   */
  $scope.loadUsers = function() {
    Restangular.one('user/list').get({
      sort_column: 1,
      asc: true
    }).then(function(data) {
      $scope.users = data.users;
    });
  };
  
  $scope.loadUsers();
  
  /**
   * Load register requests from server.
   */
  $scope.loadRegisterRequests = function() {
    Restangular.one('user/register/pending').get().then(function(data) {
      $scope.requests = data.requests || [];
      
      // 为每个请求添加回复字段
      angular.forEach($scope.requests, function(request) {
        request.responseText = '';
      });
    });
  };
  
  /**
   * Approve a register request.
   */
  $scope.approveRequest = function(request) {
    var title = '确认批准';
    var msg = '确定要批准用户"' + request.username + '"的注册申请吗？';
    var btns = [
      { result: 'cancel', label: '取消' },
      { result: 'ok', label: '确定', cssClass: 'btn-primary' }
    ];
    
    $dialog.messageBox(title, msg, btns, function(result) {
      if (result === 'ok') {
        // 修正API调用方式
        Restangular.one('user/register/approve', request.id).post(null, {
          response: request.responseText
        }).then(function() {
          // 从当前列表中移除该请求
          var index = $scope.requests.indexOf(request);
          if (index > -1) {
            $scope.requests.splice(index, 1);
          }
          
          // 重新加载用户列表
          $scope.loadUsers();
          
          // 显示成功消息
          $dialog.messageBox('成功', '已批准用户"' + request.username + '"的注册申请', [{label: '确定'}]);
        }, function(response) {
          // 处理错误
          var errorMsg = '批准申请时出错';
          if (response.data && response.data.message) {
            errorMsg = response.data.message;
          }
          $dialog.messageBox('错误', errorMsg, [{label: '确定'}]);
        });
      }
    });
  };
  
  /**
   * Reject a register request.
   */
  $scope.rejectRequest = function(request) {
    var title = '确认拒绝';
    var msg = '确定要拒绝用户"' + request.username + '"的注册申请吗？';
    var btns = [
      { result: 'cancel', label: '取消' },
      { result: 'ok', label: '确定', cssClass: 'btn-danger' }
    ];
    
    $dialog.messageBox(title, msg, btns, function(result) {
      if (result === 'ok') {
        // 修正拒绝请求的API调用方式
        Restangular.one('user/register/reject', request.id).post(null, {
          response: request.responseText
        }).then(function() {
          // 从当前列表中移除该请求
          var index = $scope.requests.indexOf(request);
          if (index > -1) {
            $scope.requests.splice(index, 1);
          }
          
          // 显示成功消息
          $dialog.messageBox('成功', '已拒绝用户"' + request.username + '"的注册申请', [{label: '确定'}]);
        }, function(response) {
          // 处理错误
          var errorMsg = '拒绝申请时出错';
          if (response.data && response.data.message) {
            errorMsg = response.data.message;
          }
          $dialog.messageBox('错误', errorMsg, [{label: '确定'}]);
        });
      }
    });
  };
  
  /**
   * Edit a user.
   */
  $scope.editUser = function(user) {
    $state.go('settings.user.edit', { username: user.username });
  };
});