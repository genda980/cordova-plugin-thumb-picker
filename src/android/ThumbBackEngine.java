package org.apache.cordova.thumbpicker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.util.Size;

import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.listener.OnResultCallbackListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

class ThumbBackEngine implements OnResultCallbackListener<LocalMedia> {

    private static final String TAG = "ThumbBackEngine";
    public static final int ACTION_CHOOSE_PICTURE = 1;
    public static final int ACTION_CHOOSE_VIDEO = 2;
    public static final int ACTION_CHOOSE_ALL = 3;
    public static final int ACTION_TAKE_PICTURE = 4;
    public static final int ACTION_TAKE_VIDEO = 5;

    private final int mActionType;
    private final IOnBackListener mIOnBackListener;

    public ThumbBackEngine(int actionType, IOnBackListener iOnBackListener) {
        mActionType = actionType;
        this.mIOnBackListener = iOnBackListener;
    }

    @Override
    public void onResult(List<LocalMedia> result) {
        if (result.size() <= 0) {
            if (mIOnBackListener != null) {
                mIOnBackListener.onBack(null, false);
            }
            return;
        }

        if (mActionType == ACTION_CHOOSE_PICTURE || mActionType == ACTION_TAKE_PICTURE) {
            boolean isOrigin = result.get(0).isOriginal();
            JSONArray jsonArray = parseBackPicture(result);
            if (mIOnBackListener != null) {
                mIOnBackListener.onBack(jsonArray, isOrigin);
            }
        } else if (mActionType == ACTION_CHOOSE_VIDEO || mActionType == ACTION_TAKE_VIDEO) {
            JSONArray jsonArray = parseBackVideo(result);
            if (mIOnBackListener != null) {
                mIOnBackListener.onBack(jsonArray, true);
            }
        }
    }

    @Override
    public void onCancel() {

    }

    private JSONArray parseBackPicture(List<LocalMedia> localMediaList) {
        try {
            JSONArray jsonArray = new JSONArray();
            for (int i = 0; i < localMediaList.size(); i++) {
                LocalMedia localMedia = localMediaList.get(i);
                JSONObject itemObj = new JSONObject();
                String path = TextUtils.isEmpty(localMedia.getAndroidQToPath()) ? localMedia.getRealPath() : localMedia.getAndroidQToPath();
                itemObj.put("path", path);
                itemObj.put("uri", "file://" + path);
                itemObj.put("width", localMedia.getWidth());
                itemObj.put("height", localMedia.getHeight());
                Log.e(TAG, "parseBackPicture: '----> " + localMedia.getCompressPath());
                long size = localMedia.getSize();
                if (localMedia.isCompressed()) {
                    size = new File(localMedia.getCompressPath()).length() / 1024;
                }
                itemObj.put("size", size);
                jsonArray.put(itemObj);
            }
            return jsonArray;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private JSONArray parseBackVideo(List<LocalMedia> localMediaList) {
        try {
            JSONArray jsonArray = new JSONArray();
            for (int i = 0; i < localMediaList.size(); i++) {
                LocalMedia localMedia = localMediaList.get(i);
                JSONObject itemObj = new JSONObject();
                String path = TextUtils.isEmpty(localMedia.getAndroidQToPath()) ? localMedia.getRealPath() : localMedia.getAndroidQToPath();
                itemObj.put("index", i);
                itemObj.put("mediaType", "video");
                itemObj.put("name", localMedia.getFileName());
                itemObj.put("size", localMedia.getSize());
                itemObj.put("path", path);
                itemObj.put("uri", "file://" + path);
                jsonArray.put(itemObj);
            }
            return jsonArray;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        //    localMedia = parseQWH(localMedia);
        //}
        //String coverPath = "";
        //
        //Bitmap coverBitmap = getVideoCover(context, localMedia.getId(), localMedia.getRealPath(), localMedia.getWidth(), localMedia.getHeight());
        //if (coverBitmap != null) {
        //    File coverFile = getImageGalleryFile(context, coverBitmap);
        //    if (coverFile != null) {
        //        coverPath = coverFile.getAbsolutePath();
        //    }
        //}
        //Log.e(TAG, "sendBackPicture: ----< " + coverPath);
        //Log.e(TAG, "sendBackPicture: ----< width = " + localMedia.getWidth() + " height = " + localMedia.getHeight());
    }


    private LocalMedia parseQWH(Context context, LocalMedia localMedia) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(context, Uri.parse(localMedia.getPath()));
        String orientation = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);//视频方向 0, 90, 180, or 270
        String width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
        String height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
        if (orientation.equals("90") || orientation.equals("270")) {
            localMedia.setWidth(Integer.parseInt(height));
            localMedia.setHeight(Integer.parseInt(width));
        }
        return localMedia;
    }

    private Bitmap getVideoCover(Context context, long id, String path, int width, int height) {
        Bitmap bitmap = null;
        if (Build.VERSION.SDK_INT >= 29) {
            try {
                Uri videoUri = Uri.withAppendedPath(Uri.parse("content://media/external/video/media"), "" + id);
                bitmap = context.getContentResolver().loadThumbnail(videoUri, new Size(width, height), null);
            } catch (IOException e) {
                Log.e(TAG, "getVideoCover: '----> " + e);
            }
        } else {
            bitmap = ThumbnailUtils.createVideoThumbnail(path, MediaStore.Images.Thumbnails.MICRO_KIND);
            if (bitmap != null) {
                bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
            }
        }
        return bitmap;
    }

    public static File getImageGalleryFile(Context context, Bitmap bmp) {
        try {
            String storePath = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES).getAbsolutePath();
            File appDir = new File(storePath);
            if (!appDir.exists()) {
                appDir.mkdir();
            }
            String fileName = System.currentTimeMillis() + ".jpg";
            File file = new File(appDir, fileName);
            FileOutputStream fos = new FileOutputStream(file);
            boolean isSuccess = bmp.compress(Bitmap.CompressFormat.JPEG, 60, fos);
            fos.flush();
            fos.close();
            if (isSuccess) {
                return file;
            } else {
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Uri getVideoContentUri(Context context, String path) {
        Cursor cursor = context.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Video.Media._ID}, MediaStore.Video.Media.DATA + "=? ",
                new String[]{path}, null);
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
            Uri baseUri = Uri.parse("content://media/external/video/media");
            return Uri.withAppendedPath(baseUri, "" + id);
        } else {
            if (new File(path).exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Video.Media.DATA, path);
                return context.getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }

    public interface IOnBackListener {
        void onBack(Object obj, boolean isOrigin);
    }
}
