package org.apache.cordova.thumbpicker;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.util.Log;

import com.luck.picture.lib.PictureSelectionModel;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.style.PictureSelectorUIStyle;
import com.luck.picture.lib.tools.PictureFileUtils;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class echoes a string called from JavaScript.
 */
public class ThumbPicker extends CordovaPlugin {

    private static final String TAG = "ThumbPicker";

    private Activity mActivity;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);

        this.mActivity = cordova.getActivity();
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        JSONObject jsonObject = args.getJSONObject(0);
        switch (action) {
            case "choosePicture":
                choosePicture(callbackContext, jsonObject);
                return true;
            case "chooseVideo":
                chooseVideo(callbackContext, jsonObject);
                return true;
            case "clearCache":
                clearCache(callbackContext, jsonObject);
                return true;
            case "chooseAll":
                chooseAll(callbackContext, jsonObject);
                return true;
            case "takePicture":
                takePicture(callbackContext, jsonObject);
                return true;
            case "takeVideo":
                takeVideo(callbackContext, jsonObject);
                return true;
        }
        return false;
    }

    private PictureSelectionModel initSelector(int pictureMimeType, JSONObject jsonObject) {
        int language = jsonObject.optInt("language", 2);
        int maxCount = jsonObject.optInt("maxCount", 1);
        int minCount = jsonObject.optInt("minCount", 1);
        int openCamera = jsonObject.optInt("openCamera", 0);
        PictureSelectionModel pictureModel;
        if (openCamera == 0) {
            pictureModel = PictureSelector.create(mActivity).openGallery(pictureMimeType);
        } else {
            pictureModel = PictureSelector.create(mActivity).openCamera(pictureMimeType);
        }

        return pictureModel
                .imageEngine(GlideEngine.createGlideEngine())
                .setPictureUIStyle(PictureSelectorUIStyle.ofNewStyle())
                .isWeChatStyle(true)
                .setLanguage(language)
                .maxSelectNum(maxCount)
                .minSelectNum(minCount)
                .isMaxSelectEnabledMask(false)
                .isAndroidQTransform(true)
                .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) // 显示方向
                .isEnablePreviewAudio(false) // 排除音频
                .isGif(false)
                .isOpenClickSound(false) // 禁止点击声音
                .imageSpanCount(4); // 每行显示4个
    }

    private void choosePicture(CallbackContext callbackContext, JSONObject jsonObject) {
        int maxSizeMB = jsonObject.optInt("maxSizeMB", 20);
        int showCamera = jsonObject.optInt("showCamera", 1);
        int isOriginal = jsonObject.optInt("isOriginal", 0);
        int isCompress = jsonObject.optInt("isCompress", 1);
        int compressMinSize = jsonObject.optInt("compressMinSize", 100);
        int compressQuality = jsonObject.optInt("compressQuality", 90);
        int isCrop = jsonObject.optInt("isCrop", 0);
        int cropQuality = jsonObject.optInt("cropQuality", 90);
        int cropRatioX = jsonObject.optInt("cropRatioX", 1);
        int cropRatioY = jsonObject.optInt("cropRatioY", 1);
        int cropMove = jsonObject.optInt("cropMove", 0);
        initSelector(PictureMimeType.ofImage(), jsonObject)
                .selectionMode(PictureConfig.MULTIPLE) // 多选 or 单选 PictureConfig.SINGLE
                .isOriginalImageControl(isOriginal == 1) // 是否显示原图控制按钮，如果设置为true则用户可以自由选择是否使用原图，压缩、裁剪功能将会失效
                .isCamera(showCamera == 1) // 是否显示拍照按钮
                .queryMaxFileSize(maxSizeMB) // 只查多少M以内的图片、视频、音频  单位M
                .isCompress(isCompress == 1) // 是否压缩
                .minimumCompressSize(compressMinSize) // 小于多少kb的图片不压缩
                .compressQuality(compressQuality) // 图片压缩后输出质量 0~ 100
                .isEnableCrop(isCrop == 1) // 是否裁剪
                .cutOutQuality(cropQuality) // 裁剪输出质量
                .withAspectRatio(cropRatioX, cropRatioY) // 裁剪比例 如16:9 3:2 3:4 1:1 可自定义
                .hideBottomControls(false) // 是否显示uCrop工具栏
                .freeStyleCropEnabled(cropMove == 1) // 裁剪框是否可拖拽
                .circleDimmedLayer(false) // 是否圆形裁剪
                .showCropFrame(true) // 是否显示裁剪矩形边框 圆形裁剪时建议设为false
                .showCropGrid(false) // 是否显示裁剪矩形网格 圆形裁剪时建议设为false
                .isDragFrame(false) // 是否可拖动裁剪框(固定)
                .forResult(new ThumbBackEngine(ThumbBackEngine.ACTION_CHOOSE_PICTURE, new ThumbBackEngine.IOnBackListener() {
                    @Override
                    public void onBack(Object obj, boolean isOrigin) {
                        if (obj != null) {
                            try {
                                JSONObject res = new JSONObject();
                                res.put("images", obj);
                                res.put("isOrigin", isOrigin);
                                callbackContext.success(res);
                            } catch (Exception e) {
                                e.printStackTrace();
                                callbackContext.error("parse res error");
                            }
                        } else {
                            callbackContext.error("obj is null");
                        }

                    }
                }));
    }

    private void chooseVideo(CallbackContext callbackContext, JSONObject jsonObject) {
        // 一下 2.6.1 版本有bug，获取的宽高一致，ThumbBackEngine 里面自己处理了
        //.closeAndroidQChangeVideoWH(SdkVersionUtils.checkedAndroid_Q())// 如果视频有旋转角度则对换宽高，默认false
        int showCamera = jsonObject.optInt("showCamera", 0);
        int minSecond = jsonObject.optInt("minSecond", 3);
        int maxSecond = jsonObject.optInt("maxSecond", 30);
        int maxSizeMB = jsonObject.optInt("maxSizeMB", 35);

        initSelector(PictureMimeType.ofVideo(), jsonObject)
                .isCamera(showCamera == 1) // 是否显示拍照按钮
                .videoMinSecond(minSecond) // 查询多少秒以内的视频
                .videoMaxSecond(maxSecond) // 查询多少秒以内的视频
                .queryMaxFileSize(maxSizeMB) // 只查多少M以内的图片、视频、音频  单位M
                .forResult(new ThumbBackEngine(ThumbBackEngine.ACTION_CHOOSE_VIDEO, new ThumbBackEngine.IOnBackListener() {
                    @Override
                    public void onBack(Object obj, boolean isOrigin) {
                        if (obj != null) {
                            callbackContext.success((JSONArray) obj);
                        } else {
                            callbackContext.error("obj is null");
                        }
                    }
                }));
    }

    private void chooseAll(CallbackContext callbackContext, JSONObject jsonObject) {
        //.isWithVideoImage(true)// 图片和视频是否可以同选,只在ofAll模式下有效
        //.maxVideoSelectNum(1) // 视频最大选择数量
        //.minVideoSelectNum(1)// 视频最小选择数量
        initSelector(PictureMimeType.ofAll(), jsonObject)
                .forResult(new ThumbBackEngine(ThumbBackEngine.ACTION_CHOOSE_ALL, new ThumbBackEngine.IOnBackListener() {
                    @Override
                    public void onBack(Object obj, boolean isOrigin) {

                    }
                }));
    }

    private void takePicture(CallbackContext callbackContext, JSONObject jsonObject) {
        try {
            int isCompress = jsonObject.optInt("isCompress", 1);
            int compressQuality = jsonObject.optInt("compressQuality", 90);
            int isCrop = jsonObject.optInt("isCrop", 0);
            int cropQuality = jsonObject.optInt("cropQuality", 90);
            int cropRatioX = jsonObject.optInt("cropRatioX", 1);
            int cropRatioY = jsonObject.optInt("cropRatioY", 1);
            int cropMove = jsonObject.optInt("cropMove", 0);
            jsonObject.put("openCamera", 1);
            initSelector(PictureMimeType.ofImage(), jsonObject)
                    .isCompress(isCompress == 1) // 是否压缩
                    .compressQuality(compressQuality) // 图片压缩后输出质量 0~ 100
                    .isEnableCrop(isCrop == 1) // 是否裁剪
                    .cutOutQuality(cropQuality) // 裁剪输出质量
                    .withAspectRatio(cropRatioX, cropRatioY) // 裁剪比例 如16:9 3:2 3:4 1:1 可自定义
                    .hideBottomControls(false) // 是否显示uCrop工具栏
                    .freeStyleCropEnabled(cropMove == 1) // 裁剪框是否可拖拽
                    .circleDimmedLayer(false) // 是否圆形裁剪
                    .showCropFrame(true) // 是否显示裁剪矩形边框 圆形裁剪时建议设为false
                    .showCropGrid(false) // 是否显示裁剪矩形网格 圆形裁剪时建议设为false
                    .isDragFrame(false) // 是否可拖动裁剪框(固定)
                    .minSelectNum(1)
                    .selectionMode(PictureConfig.SINGLE)
                    .forResult(new ThumbBackEngine(ThumbBackEngine.ACTION_CHOOSE_PICTURE, new ThumbBackEngine.IOnBackListener() {
                        @Override
                        public void onBack(Object obj, boolean isOrigin) {
                            if (obj != null) {
                                try {
                                    JSONObject res = new JSONObject();
                                    res.put("images", obj);
                                    res.put("isOrigin", isOrigin);
                                    callbackContext.success(res);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    callbackContext.error("parse res error");
                                }
                            } else {
                                callbackContext.error("obj is null");
                            }
                        }
                    }));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void takeVideo(CallbackContext callbackContext, JSONObject jsonObject) {
        try {
            jsonObject.put("openCamera", 1);
            int maxSecond = jsonObject.optInt("maxSecond", 10);
            initSelector(PictureMimeType.ofVideo(), jsonObject)
                    .recordVideoSecond(maxSecond)
                    .minSelectNum(1)
                    .selectionMode(PictureConfig.SINGLE)
                    .forResult(new ThumbBackEngine(ThumbBackEngine.ACTION_CHOOSE_VIDEO, new ThumbBackEngine.IOnBackListener() {
                        @Override
                        public void onBack(Object obj, boolean isOrigin) {
                            if (obj != null) {
                                callbackContext.success((JSONArray) obj);
                            } else {
                                callbackContext.error("obj is null");
                            }
                        }
                    }));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("StaticFieldLeak")
    private void clearCache(CallbackContext callbackContext, JSONObject jsonObject) {
        new AsyncTask<JSONObject, Void, Void>() {
            @Override
            protected Void doInBackground(JSONObject... integers) {
                // TYPE_ALL = 0; TYPE_IMAGE = 1; TYPE_VIDEO = 2;
                JSONObject obj = integers[0];
                int clearType = obj.optInt("type");
                if (clearType == 0) {
                    PictureFileUtils.deleteAllCacheDirFile(cordova.getContext());
                } else {
                    PictureFileUtils.deleteCacheDirFile(cordova.getContext(), clearType);
                }
                callbackContext.success();
                return null;
            }
        }.execute(jsonObject);
    }
}
