package org.apache.cordova.thumbpicker;

import android.util.Log;

import com.luck.picture.lib.engine.ImageEngine;
import com.luck.picture.lib.engine.PictureSelectorEngine;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.listener.OnResultCallbackListener;

import java.util.List;

public class PictureSelectorEngineImp implements PictureSelectorEngine {
    private static final String TAG = "PictureSelectorEngineIm";

    @Override
    public ImageEngine createEngine() {
        // 重新创建图片加载引擎
        return GlideEngine.createGlideEngine();
    }

    @Override
    public OnResultCallbackListener<LocalMedia> getResultCallbackListener() {
        return new OnResultCallbackListener<LocalMedia>() {
            @Override
            public void onResult(List<LocalMedia> result) {
                Log.i(TAG, "onResult:" + result.size());
            }

            @Override
            public void onCancel() {
                Log.i(TAG, "PictureSelector onCancel");
            }
        };
    }
}
