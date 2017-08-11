package com.qiniu.pili.droid.shortvideo.demo.utils;

import android.app.Activity;
import android.content.Context;
import android.opengl.GLES20;
import android.os.Build;
import android.util.Log;

import com.kiwi.tracker.KwFaceTracker;
import com.kiwi.tracker.KwFilterType;
import com.kiwi.tracker.KwTrackerManager;
import com.kiwi.tracker.KwTrackerSettings;
import com.kiwi.tracker.bean.conf.StickerConfig;
import com.kiwi.tracker.common.Config;
import com.kiwi.ui.OnViewEventListener;
import com.kiwi.ui.ResourceHelper;
import com.kiwi.ui.SharedPreferenceManager;

import static com.kiwi.ui.widget.KwControlView.BEAUTY_BIG_EYE_TYPE;
import static com.kiwi.ui.widget.KwControlView.BEAUTY_THIN_FACE_TYPE;
import static com.kiwi.ui.widget.KwControlView.REMOVE_BLEMISHES;
import static com.kiwi.ui.widget.KwControlView.SKIN_SHINNING_TENDERNESS;
import static com.kiwi.ui.widget.KwControlView.SKIN_TONE_PERFECTION;
import static com.kiwi.ui.widget.KwControlView.SKIN_TONE_SATURATION;


public class KiwiTrackWrapper {
    private static final String TAG = KiwiTrackWrapper.class.getName();

    private KwTrackerSettings mKwTrackerSettings;
    private KwTrackerManager mKwTrackerManager;

    public KiwiTrackWrapper(Context context, int cameraFaceId) {
        SharedPreferenceManager spManager = SharedPreferenceManager.getInstance();

        // 瘦脸大眼
        KwTrackerSettings.BeautySettings beautySettings = new KwTrackerSettings.BeautySettings();
        beautySettings.setBigEyeScaleProgress(spManager.getBigEye());
        beautySettings.setThinFaceScaleProgress(spManager.getThinFace());

        // 美颜
        KwTrackerSettings.BeautySettings2 beautySettings2 = new KwTrackerSettings.BeautySettings2();
        beautySettings2.setWhiteProgress(spManager.getSkinWhite());
        beautySettings2.setDermabrasionProgress(spManager.getSkinRemoveBlemishes());
        beautySettings2.setSaturatedProgress(spManager.getSkinSaturation());
        beautySettings2.setPinkProgress(spManager.getSkinTenderness());

        mKwTrackerSettings = new KwTrackerSettings().
                setBeauty2Enabled(spManager.isBeautyEnabled()).
                setBeautySettings2(beautySettings2).
                setBeautyFaceEnabled(spManager.isLocalBeautyEnabled()).
                setBeautySettings(beautySettings).
                setCameraFaceId(cameraFaceId).
                setDefaultDirection(KwFaceTracker.CV_CLOCKWISE_ROTATE_180);

        mKwTrackerManager = new KwTrackerManager(context).setTrackerSetting(mKwTrackerSettings).build();

        ResourceHelper.copyResource2SD(context);

        String manufacturer = Build.MANUFACTURER.toLowerCase();
        Log.i(TAG, String.format("manufacturer:%s,model:%s,sdk:%s", manufacturer, Build.MODEL, Build.VERSION.SDK_INT));
        boolean isOppoVivo = manufacturer.contains("oppo") || manufacturer.contains("vivo");
        if (isOppoVivo || Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            Config.TRACK_MODE = Config.TRACK_PRIORITY_PERFORMANCE;
        }
        //关闭日志打印,release版本请务必关闭日志打印
        Config.isDebug = true;
        Config.outPutTestBitmap = true;
    }

    public void onCreate(Activity activity) {
        mKwTrackerManager.onCreate(activity);
    }

    public void onResume(Activity activity) {
        mKwTrackerManager.onResume(activity);
    }

    public void onPause(Activity activity) {
        mKwTrackerManager.onPause(activity);
    }

    public void onDestroy(Activity activity) {
        mKwTrackerManager.onDestory(activity);
    }

    public void onSurfaceCreated(Context context) {
        mKwTrackerManager.onSurfaceCreated(context);
    }

    public void onSurfaceChanged(int width, int height, int previewWidth, int previewHeight) {
        mKwTrackerManager.onSurfaceChanged(width, height, previewWidth, previewHeight);
    }

    public void onSurfaceDestroyed() {
        mKwTrackerManager.onSurfaceDestroyed();
    }

    public void switchCamera(int ordinal) {
        mKwTrackerManager.switchCamera(ordinal);
    }

    /**
     * 对纹理进行特效处理（美颜、大眼瘦脸、人脸贴纸、哈哈镜、滤镜）
     *
     * @param texId     YUV格式纹理
     * @param texWidth  纹理宽度
     * @param texHeight 纹理高度
     * @return 特效处理后的纹理
     */
    public int onDrawFrame(int texId, int texWidth, int texHeight) {
        int newTexId = texId;
        int filterTexId = mKwTrackerManager.onDrawTexture2D(texId, texWidth, texHeight, 1);
        if (filterTexId != -1) {
            newTexId = filterTexId;
        }
        int error = GLES20.glGetError(); //请勿删除当前行获取opengl错误代码
        if (error != GLES20.GL_NO_ERROR) {
            Log.e(TAG, "glError:" + error);
        }
        return newTexId;
    }

    public OnViewEventListener initUIEventListener() {
        OnViewEventListener eventListener = new OnViewEventListener() {

            @Override
            public void onStickerChanged(StickerConfig item) {
                getKwTrackerManager().switchSticker(item);
                Log.e(TAG, "switchSticker, item: " + item.getName() + " Dir: " + item.getDir());
            }

            @Override
            public void onSwitchEyeAndThin(boolean enable) {
                getKwTrackerManager().setBeautyFaceEnabled(enable);
            }

            @Override
            public void onSwitchFaceBeauty(boolean enable) {
                getKwTrackerManager().setBeauty2Enabled(enable);
            }

            @Override
            public void onDistortionChanged(KwFilterType filterType) {
                getKwTrackerManager().switchDistortion(filterType);

            }

            @Override
            public void onAdjustFaceBeauty(int type, float param) {
                switch (type) {
                    case BEAUTY_BIG_EYE_TYPE:
                        getKwTrackerManager().setEyeMagnifying((int) param);
                        break;
                    case BEAUTY_THIN_FACE_TYPE:
                        getKwTrackerManager().setChinSliming((int) param);
                        break;
                    case SKIN_SHINNING_TENDERNESS:
                        getKwTrackerManager().setSkinTenderness((int) param);
                        break;
                    case SKIN_TONE_SATURATION:
                        getKwTrackerManager().setSkinSaturation((int) param);
                        break;
                    case REMOVE_BLEMISHES:
                        getKwTrackerManager().setSkinBlemishRemoval((int) param);
                        break;
                    case SKIN_TONE_PERFECTION:
                        getKwTrackerManager().setSkinWhitening((int) param);
                        break;
                }
            }

            @Override
            public void onFaceBeautyLevel(float level) {
                getKwTrackerManager().adjustBeauty(level);
            }

            private KwTrackerManager getKwTrackerManager() {
                return mKwTrackerManager;
            }
        };

        return eventListener;
    }
}
