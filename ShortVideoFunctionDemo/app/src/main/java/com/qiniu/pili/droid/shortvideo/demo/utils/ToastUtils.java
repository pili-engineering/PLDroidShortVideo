package com.qiniu.pili.droid.shortvideo.demo.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import static com.qiniu.pili.droid.shortvideo.PLErrorCode.ERROR_LOW_MEMORY;
import static com.qiniu.pili.droid.shortvideo.PLErrorCode.ERROR_MULTI_CODEC_WRONG;
import static com.qiniu.pili.droid.shortvideo.PLErrorCode.ERROR_MUXER_START_FAILED;
import static com.qiniu.pili.droid.shortvideo.PLErrorCode.ERROR_NO_VIDEO_TRACK;
import static com.qiniu.pili.droid.shortvideo.PLErrorCode.ERROR_SETUP_AUDIO_ENCODER_FAILED;
import static com.qiniu.pili.droid.shortvideo.PLErrorCode.ERROR_SETUP_CAMERA_FAILED;
import static com.qiniu.pili.droid.shortvideo.PLErrorCode.ERROR_SETUP_MICROPHONE_FAILED;
import static com.qiniu.pili.droid.shortvideo.PLErrorCode.ERROR_SETUP_VIDEO_DECODER_FAILED;
import static com.qiniu.pili.droid.shortvideo.PLErrorCode.ERROR_SETUP_VIDEO_ENCODER_FAILED;
import static com.qiniu.pili.droid.shortvideo.PLErrorCode.ERROR_SRC_DST_SAME_FILE_PATH;

public class ToastUtils {

    private static final Handler sUIHandler = new Handler(Looper.getMainLooper());
    private static Toast sToast;

    public static void init(Context context) {
        sToast = Toast.makeText(context, "", Toast.LENGTH_SHORT);
    }

    public static void showShortToast(String message) {
        showToast(message, Toast.LENGTH_SHORT);
    }

    public static void showLongToast(String message) {
        showToast(message, Toast.LENGTH_LONG);
    }

    public static void toastErrorCode(int errorCode) {
        switch (errorCode) {
            case ERROR_SETUP_CAMERA_FAILED:
                showShortToast("摄像头配置错误");
                break;
            case ERROR_SETUP_MICROPHONE_FAILED:
                showShortToast("麦克风配置错误");
                break;
            case ERROR_NO_VIDEO_TRACK:
                showShortToast("该文件没有视频信息！");
                break;
            case ERROR_SRC_DST_SAME_FILE_PATH:
                showShortToast("源文件路径和目标路径不能相同！");
                break;
            case ERROR_MULTI_CODEC_WRONG:
                showShortToast("当前机型暂不支持该功能");
                break;
            case ERROR_SETUP_VIDEO_ENCODER_FAILED:
                showShortToast("视频编码器启动失败");
                break;
            case ERROR_SETUP_VIDEO_DECODER_FAILED:
                showShortToast("视频解码器启动失败");
                break;
            case ERROR_SETUP_AUDIO_ENCODER_FAILED:
                showShortToast("音频编码器启动失败");
                break;
            case ERROR_LOW_MEMORY:
                showShortToast("手机内存不足，无法对该视频进行时光倒流！");
                break;
            case ERROR_MUXER_START_FAILED:
                showShortToast("MUXER 启动失败, 请检查视频格式");
                break;
            default:
                showShortToast("错误码： " + errorCode);
        }
    }

    public static void cancel() {
        if (sToast != null) {
            sToast.cancel();
        }
    }

    private static void showToast(String message, int duration) {
        runOnUiThread(() -> {
            if (sToast != null) {
                sToast.setText(message);
                sToast.setDuration(duration);
                sToast.show();
            }
        });
    }

    private static void runOnUiThread(final Runnable runnable) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            runnable.run();
        } else {
            sUIHandler.post(runnable);
        }
    }

}
