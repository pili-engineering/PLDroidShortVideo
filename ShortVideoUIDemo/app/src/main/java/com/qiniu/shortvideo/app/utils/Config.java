package com.qiniu.shortvideo.app.utils;

import android.content.Context;
import android.os.Environment;

/**
 * 定义 app 视频相关的存储路径
 */
public class Config {
    public static String FUNCTION_LIST_PATH = "https://developer.qiniu.com/pili/sdk/3731/short-video";
    public static final String VIDEO_PUBLIC_STORAGE_DIR = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES) + "/QNShortVideo";
    public static final String MIME_TYPE_VIDEO = "video/mp4";
    public static String VIDEO_STORAGE_DIR;
    public static String RECORD_FILE_PATH;
    public static String EDITED_FILE_PATH;
    public static String MIX_RECORD_FILE_PATH;
    public static String CAMERA_RECORD_CACHE_PATH;
    public static String TRIM_FILE_PATH;
    public static String TRANSCODE_FILE_PATH;
    public static String GIF_STICKER_DIR;
    public static String CAPTURED_FRAME_FILE_PATH;
    public static String COMPOSE_FILE_PATH;
    public static String VIDEO_MIX_PATH;

    public static void init(Context context) {
        VIDEO_STORAGE_DIR = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES) + "/QNShortVideo/";
        RECORD_FILE_PATH = VIDEO_STORAGE_DIR + "record.mp4";
        EDITED_FILE_PATH = VIDEO_STORAGE_DIR + "edited.mp4";
        MIX_RECORD_FILE_PATH = VIDEO_STORAGE_DIR + "mix_record.mp4";
        CAMERA_RECORD_CACHE_PATH = VIDEO_STORAGE_DIR + "mix_camera_cache.mp4";
        TRIM_FILE_PATH = VIDEO_STORAGE_DIR + "trimmed.mp4";
        TRANSCODE_FILE_PATH = VIDEO_STORAGE_DIR + "transcoded.mp4";
        GIF_STICKER_DIR = VIDEO_STORAGE_DIR + "gif/";
        CAPTURED_FRAME_FILE_PATH = VIDEO_STORAGE_DIR + "captured_frame.jpg";
        COMPOSE_FILE_PATH = VIDEO_STORAGE_DIR + "composed.mp4";
        VIDEO_MIX_PATH = VIDEO_STORAGE_DIR + "video_mix.mp4";
    }
}
