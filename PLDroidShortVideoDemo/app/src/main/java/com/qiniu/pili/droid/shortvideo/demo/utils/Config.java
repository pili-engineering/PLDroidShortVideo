package com.qiniu.pili.droid.shortvideo.demo.utils;

import android.os.Environment;

public class Config {
    public static final String TOKEN = "MqF35-H32j1PH8igh-am7aEkduP511g-5-F7j47Z:0gzBOkhm3KsFGbGk2HdKfA4jZp4=:eyJzY29wZSI6InNob3J0LXZpZGVvIiwiZGVhZGxpbmUiOjE2NTA3MTExMDcsInVwaG9zdHMiOlsiaHR0cDovL3VwLXoyLnFpbml1LmNvbSIsImh0dHA6Ly91cGxvYWQtejIucWluaXUuY29tIiwiLUggdXAtejIucWluaXUuY29tIGh0dHA6Ly8xODMuNjAuMjE0LjE5OCJdfQ==";
    public static final String ak = "MqF35-H32j1PH8igh-am7aEkduP511g-5-F7j47Z";
    public static final String DOMAIN = "shortvideo.pdex-service.com";

    public static final String VIDEO_STORAGE_DIR = Environment.getExternalStorageDirectory() + "/ShortVideo/";
    public static final String RECORD_FILE_PATH = VIDEO_STORAGE_DIR + "record.mp4";
    public static final String DUB_FILE_PATH = VIDEO_STORAGE_DIR + "dub.mp4";
    public static final String AUDIO_RECORD_FILE_PATH = VIDEO_STORAGE_DIR + "audio_record.m4a";
    public static final String EDITED_FILE_PATH = VIDEO_STORAGE_DIR + "edited.mp4";
    public static final String TRIM_FILE_PATH = VIDEO_STORAGE_DIR + "trimmed.mp4";
    public static final String TRANSCODE_FILE_PATH = VIDEO_STORAGE_DIR + "transcoded.mp4";
    public static final String CAPTURED_FRAME_FILE_PATH = VIDEO_STORAGE_DIR + "captured_frame.jpg";
    public static final String GIF_SAVE_PATH = VIDEO_STORAGE_DIR + "generated.gif";
    public static final String SCREEN_RECORD_FILE_PATH = VIDEO_STORAGE_DIR + "screen_record.mp4";
    public static final String COMPOSE_FILE_PATH = VIDEO_STORAGE_DIR + "composed.mp4";
    public static final String IMAGE_COMPOSE_FILE_PATH = VIDEO_STORAGE_DIR + "image_composed.mp4";
}