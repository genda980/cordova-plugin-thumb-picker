cordova.define("cordova-plugin-thumb-picker.ThumbPicker", function(require, exports, module) {
    var exec = require('cordova/exec');

    exports.choosePicture = function (arg0, success, error) {
        exec(success, error, 'ThumbPicker', 'choosePicture', [arg0]);
    };

    exports.chooseVideo = function (arg0, success, error) {
        exec(success, error, 'ThumbPicker', 'chooseVideo', [arg0]);
    };

    exports.chooseAll = function (arg0, success, error) {
        exec(success, error, 'ThumbPicker', 'chooseAll', [arg0]);
    };

    exports.takePicture = function (arg0, success, error) {
        exec(success, error, 'ThumbPicker', 'takePicture', [arg0]);
    };

    exports.takeVideo = function (arg0, success, error) {
        exec(success, error, 'ThumbPicker', 'takeVideo', [arg0]);
    };

    exports.clearCache = function (arg0, success, error) {
        exec(success, error, 'ThumbPicker', 'clearCache', [arg0]);
    };
});
