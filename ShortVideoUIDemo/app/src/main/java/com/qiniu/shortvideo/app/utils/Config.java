package com.qiniu.shortvideo.app.utils;

import android.os.Environment;

/**
 * 定义 app 视频相关的存储路径
 */
public class Config {
    public static final String VIDEO_STORAGE_DIR = Environment.getExternalStorageDirectory() + "/QNShortVideo/";
    public static final String RECORD_FILE_PATH = VIDEO_STORAGE_DIR + "record.mp4";
    public static final String EDITED_FILE_PATH = VIDEO_STORAGE_DIR + "edited.mp4";
    public static final String MIX_RECORD_FILE_PATH = VIDEO_STORAGE_DIR + "mix_record.mp4";
    public static final String CAMERA_RECORD_CACHE_PATH = VIDEO_STORAGE_DIR + "mix_camera_cache.mp4";
    public static final String TRIM_FILE_PATH = VIDEO_STORAGE_DIR + "trimmed.mp4";
    public static final String TRANSCODE_FILE_PATH = VIDEO_STORAGE_DIR + "transcoded.mp4";
    public static final String GIF_STICKER_DIR = Environment.getExternalStorageDirectory() + "/NiuPai/gif/";
    public static final String CAPTURED_FRAME_FILE_PATH = VIDEO_STORAGE_DIR + "captured_frame.jpg";
    public static final String FUNCTION_LIST_PATH = "https://developer.qiniu.com/pili/sdk/3731/short-video";
    public static final String COMPOSE_FILE_PATH = VIDEO_STORAGE_DIR + "composed.mp4";
    public static final String VIDEO_MIX_PATH = VIDEO_STORAGE_DIR + "video_mix.mp4";
}
