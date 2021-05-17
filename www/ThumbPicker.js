var exec = require('cordova/exec');

exports.coolMethod = function (arg0, success, error) {
    exec(success, error, 'ThumbPicker', 'coolMethod', [arg0]);
};


exports.coolMethod2 = function (arg0, success, error) {
    exec(success, error, 'ThumbPicker', 'coolMethod2', [arg0]);
};


