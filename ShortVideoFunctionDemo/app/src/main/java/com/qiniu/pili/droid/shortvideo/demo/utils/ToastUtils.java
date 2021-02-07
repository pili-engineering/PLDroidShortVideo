package com.qiniu.pili.droid.shortvideo.demo.utils;

import android.content.Context;
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
    public static void showShortToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public static void showLongToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

    public static void toastErrorCode(Context context, int errorCode) {
        switch (errorCode) {
            case ERROR_SETUP_CAMERA_FAILED:
                ToastUtils.showShortToast(context, "摄像头配置错误");
                break;
            case ERROR_SETUP_MICROPHONE_FAILED:
                ToastUtils.showShortToast(context, "麦克风配置错误");
                break;
            case ERROR_NO_VIDEO_TRACK:
                ToastUtils.showShortToast(context, "该文件没有视频信息！");
                break;
            case ERROR_SRC_DST_SAME_FILE_PATH:
                ToastUtils.showShortToast(context, "源文件路径和目标路径不能相同！");
                break;
            case ERROR_MULTI_CODEC_WRONG:
                ToastUtils.showShortToast(context, "当前机型暂不支持该功能");
                break;
            case ERROR_SETUP_VIDEO_ENCODER_FAILED:
                ToastUtils.showShortToast(context, "视频编码器启动失败");
                break;
            case ERROR_SETUP_VIDEO_DECODER_FAILED:
                ToastUtils.showShortToast(context, "视频解码器启动失败");
                break;
            case ERROR_SETUP_AUDIO_ENCODER_FAILED:
                ToastUtils.showShortToast(context, "音频编码器启动失败");
                break;
            case ERROR_LOW_MEMORY:
                ToastUtils.showShortToast(context, "手机内存不足，无法对该视频进行时光倒流！");
                break;
            case ERROR_MUXER_START_FAILED:
                ToastUtils.showShortToast(context, "MUXER 启动失败, 请检查视频格式");
                break;
            default:
                ToastUtils.showShortToast(context, "错误码： " + errorCode);
        }
    }
}
