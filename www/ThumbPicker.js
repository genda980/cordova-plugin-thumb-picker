var exec = require('cordova/exec');

exports.coolMethod = function (arg0, success, error) {
    exec(success, error, 'ThumbPicker', 'coolMethod', [arg0]);
};


exports.coolMethod1 = function (arg0, success, error) {
    exec(success, error, 'ThumbPicker', 'coolMethod1', [arg0]);
};