var exec = require('cordova/exec');

// 返回值
// 图片
    // {
    //      isOrigin: 是否是原图，
    //      images: [
    //          path: "/private/var/mobile/Containers/Data/Application/A08FE35A-8901-44AD-B967-3B2432AF3195/tmp/ImagePicker/IMG_0046.JPG"
    //          uri: "file:///private/var/mobile/Containers/Data/Application/A08FE35A-8901-44AD-B967-3B2432AF3195/tmp/ImagePicker/IMG_0046.JPG"
    //          size: 85855  单位 KB
    //          height: 2436
    //          width: 1125
    //      ]
    // }

// 视频
    // [
    //      index: 0,
    //      mediaType: 'video',
    //      name: 'xxx.mp4',
    //      path: '',
    //      uri: '',
    //      size: 3213 单位KB
    // ]

// 选择器js传参
    // language         语言 简体中文 0，繁体 1，英语 2，韩语 3，德语 4，法语 5，日语 6，越语 7，西班牙语 8
    // maxCount         最大选择数量 默认 1
    // minCount         最小选择数量 默认 1
    // openCamera       是否直接进行拍照或拍视频
    // maxSizeMB        只查多少M以内的图片、视频、音频  单位MB 默认 图片 20 视频 35
    // showCamera       是否在选择器中的第一个，显示拍摄入口 默认 1
    // isOriginal       是否显示 原图 按钮  默认 0
    // isCompress       是否开启图片选择后压缩 默认 1
    // compressMinSize  小于此值不进行压缩 单位kb 默认 100
    // compressQuality  压缩质量 1～100 默认 90
    // isCrop           是否开启图片裁剪 默认 0
    // cropQuality      裁剪质量 1～100 默认 90
    // cropRatioX       裁剪比例 如16:9 3:2 3:4 1:1 可自定义 默认 1
    // cropRatioY       裁剪比例 如16:9 3:2 3:4 1:1 可自定义 默认 1
    // cropMove         裁剪框是否可拖拽 默认 0
    // minSecond        过滤此秒数以下的视频 默认 3
    // maxSecond        过滤此秒数以上的视频 默认 30 （拍摄视频时，此值是视频拍摄的最大秒数）

exports.choosePicture = function (arg0, success, error) {
    exec(success, error, 'ThumbPicker', 'choosePicture', [arg0]);
};

exports.chooseVideo = function (arg0, success, error) {
    exec(success, error, 'ThumbPicker', 'chooseVideo', [arg0]);
};

// 未处理
exports.chooseAll = function (arg0, success, error) {
    exec(success, error, 'ThumbPicker', 'chooseAll', [arg0]);
};

exports.takePicture = function (arg0, success, error) {
    exec(success, error, 'ThumbPicker', 'takePicture', [arg0]);
};

exports.takeVideo = function (arg0, success, error) {
    exec(success, error, 'ThumbPicker', 'takeVideo', [arg0]);
};

// 仅Android
exports.clearCache = function (arg0, success, error) {
    exec(success, error, 'ThumbPicker', 'clearCache', [arg0]);
};
