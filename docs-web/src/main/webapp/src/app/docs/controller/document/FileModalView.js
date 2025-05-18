'use strict';

/**
 * File modal view controller.
 */
angular.module('docs').controller('FileModalView', function ($uibModalInstance, $scope, $state, $stateParams, $sce, Restangular, $transitions, $http) {
  var setFile = function (files) {
    // Search current file
    _.each(files, function (value) {
      if (value.id === $stateParams.fileId) {
        $scope.file = value;
        $scope.trustedFileUrl = $sce.trustAsResourceUrl('../api/file/' + $stateParams.fileId + '/data');
      }
    });
  };

  // Load files
  Restangular.one('file/list').get({ id: $stateParams.id }).then(function (data) {
    $scope.files = data.files;
    setFile(data.files);

    // File not found, maybe it's a version
    if (!$scope.file) {
      Restangular.one('file/' + $stateParams.fileId + '/versions').get().then(function (data) {
        setFile(data.files);
      });
    }
  });

  /**
   * Return the next file.
   */
  $scope.nextFile = function () {
    var next = undefined;
    _.each($scope.files, function (value, key) {
      if (value.id === $stateParams.fileId) {
        next = $scope.files[key + 1];
      }
    });
    return next;
  };

  /**
   * Return the previous file.
   */
  $scope.previousFile = function () {
    var previous = undefined;
    _.each($scope.files, function (value, key) {
      if (value.id === $stateParams.fileId) {
        previous = $scope.files[key - 1];
      }
    });
    return previous;
  };

  /**
   * Navigate to the next file.
   */
  $scope.goNextFile = function () {
    var next = $scope.nextFile();
    if (next) {
      $state.go('^.file', { id: $stateParams.id, fileId: next.id });
    }
  };

  /**
   * Navigate to the previous file.
   */
  $scope.goPreviousFile = function () {
    var previous = $scope.previousFile();
    if (previous) {
      $state.go('^.file', { id: $stateParams.id, fileId: previous.id });
    }
  };

  /**
   * Open the file in a new window.
   */
  $scope.openFile = function () {
    window.open('../api/file/' + $stateParams.fileId + '/data');
  };

  /**
   * Open the file content in a new window with translation.
   */
  $scope.openFileContent = function (targetLang) {
    if (!targetLang || targetLang === 'original') {
      window.open('../api/file/' + $stateParams.fileId + '/data?size=content');
      return;
    }

    $scope.isTranslating = true;

    // Get the original content first
    $http.get('../api/file/' + $stateParams.fileId + '/data?size=content').then(function(response) {
      const originalText = response.data;
      
      // Call Kimi API for translation
      const kimiPayload = {
        model: "moonshot-v1-8k",
        messages: [
          {
            role: "system", 
            content: "You are a professional translator. Translate the text to the specified language. Only return the translated text, without any additional formatting or explanation."
          },
          {
            role: "user", 
            content: "Translate the following text to " + (targetLang === 'zh' ? 'Chinese' : targetLang === 'en' ? 'English' : 'Japanese') + ":\n" + originalText
          }
        ],
        temperature: 0.3,
        stream: false
      };

      $http({
        method: 'POST',
        url: 'https://api.moonshot.cn/v1/chat/completions',
        headers: {
          'Authorization': 'Bearer sk-JUCSl9zHxEI8BtYuJpU32T8t83JlmQH9Gh2ef9V4oE6vng7U',
          'Content-Type': 'application/json'
        },
        data: JSON.stringify(kimiPayload)
      }).then(function(kimiResponse) {
        $scope.isTranslating = false;
        try {
          // 直接使用返回的文本内容
          const translatedText = kimiResponse.data.choices[0].message.content;
          
          // 创建一个新窗口并添加样式
          const newWindow = window.open();
          newWindow.document.write(`
            <!DOCTYPE html>
            <html>
            <head>
              <style>
                body {
                  font-family: Arial, sans-serif;
                  line-height: 1.6;
                  margin: 20px;
                  background-color: #f5f5f5;
                }
                .content {
                  background-color: white;
                  padding: 20px;
                  border-radius: 5px;
                  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
                  white-space: pre-wrap;
                  word-wrap: break-word;
                }
                .language-tag {
                  background-color: #e0e0e0;
                  padding: 5px 10px;
                  border-radius: 3px;
                  margin-bottom: 10px;
                  display: inline-block;
                }
              </style>
            </head>
            <body>
              <div class="language-tag">Translated to ${targetLang === 'zh' ? 'Chinese' : targetLang === 'en' ? 'English' : 'Japanese'}</div>
              <div class="content">${translatedText}</div>
            </body>
            </html>
          `);
        } catch (e) {
          console.error('Translation error:', e);
          alert('Translation failed. Please try again.');
        }
      }).catch(function(error) {
        $scope.isTranslating = false;
        console.error('Kimi API error:', error);
        alert('Translation service error. Please try again later.');
      });
    }).catch(function(error) {
      $scope.isTranslating = false;
      console.error('Content fetch error:', error);
      alert('Failed to fetch original content. Please try again.');
    });
  };

  /**
   * Print the file.
   */
  $scope.printFile = function () {
    var popup = window.open('../api/file/' + $stateParams.fileId + '/data', '_blank');
    popup.onload = function () {
      popup.print();
      popup.close();
    }
  };

  /**
   * Close the file preview.
   */
  $scope.closeFile = function () {
    $uibModalInstance.dismiss();
  };

  // Close the modal when the user exits this state
  var off = $transitions.onStart({}, function(transition) {
    if (!$uibModalInstance.closed) {
      if (transition.to().name === $state.current.name) {
        $uibModalInstance.close();
      } else {
        $uibModalInstance.dismiss();
      }
    }
    off();
  });

  /**
   * Return true if we can display the preview image.
   */
  $scope.canDisplayPreview = function () {
    return $scope.file && $scope.file.mimetype !== 'application/pdf';
  };
});