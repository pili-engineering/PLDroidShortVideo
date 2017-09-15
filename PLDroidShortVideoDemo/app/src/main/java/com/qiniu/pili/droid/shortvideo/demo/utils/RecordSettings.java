package com.qiniu.pili.droid.shortvideo.demo.utils;

import com.qiniu.pili.droid.shortvideo.PLCameraSetting;
import com.qiniu.pili.droid.shortvideo.PLVideoEncodeSetting;

public class RecordSettings {

    public static final long DEFAULT_MIN_RECORD_DURATION = 3 * 1000;
    public static final long DEFAULT_MAX_RECORD_DURATION = 10 * 1000;

    public static final String[] RECORD_ORIENTATION_TIPS_ARRAY = {
            "竖屏", "横屏"
    };

    public static final String[] RECORD_SPEED_LEVEL_TIPS_ARRAY = {
            "0.125x",
            "0.25x",
            "0.5x",
            "1.0x",
            "2.0x",
            "4.0x",
            "8.0x",
    };

    public static final String[] PREVIEW_SIZE_RATIO_TIPS_ARRAY = {
            "4:3", "16:9"
    };

    public static final String[] PREVIEW_SIZE_LEVEL_TIPS_ARRAY = {
            "240P",
            "360P",
            "480P",
            "720P",
            "960P",
            "1080P",
            "1200P",
    };

    public static final String[] ENCODING_SIZE_LEVEL_TIPS_ARRAY = {
            "240x240",
            "320x240",
            "352x352",
            "640x352",
            "360x360",
            "480x360",
            "640x360",
            "480x480",
            "640x480",
            "848x480",
            "544x544",
            "720x544",
            "720x720",
            "960x720",
            "1280x720",
            "1088x1088",
            "1440x1088"
    };

    public static final String[] ENCODING_BITRATE_LEVEL_TIPS_ARRAY = {
            "500Kbps",
            "800Kbps",
            "1000Kbps",
            "1200Kbps",
            "1600Kbps",
            "2000Kbps",
            "2500Kbps",
            "4000Kbps",
            "8000Kbps"
    };

    public static final PLCameraSetting.CAMERA_PREVIEW_SIZE_RATIO[] PREVIEW_SIZE_RATIO_ARRAY = {
            PLCameraSetting.CAMERA_PREVIEW_SIZE_RATIO.RATIO_4_3,
            PLCameraSetting.CAMERA_PREVIEW_SIZE_RATIO.RATIO_16_9
    };

    public static final PLCameraSetting.CAMERA_PREVIEW_SIZE_LEVEL[] PREVIEW_SIZE_LEVEL_ARRAY = {
            PLCameraSetting.CAMERA_PREVIEW_SIZE_LEVEL.PREVIEW_SIZE_LEVEL_240P,
            PLCameraSetting.CAMERA_PREVIEW_SIZE_LEVEL.PREVIEW_SIZE_LEVEL_360P,
            PLCameraSetting.CAMERA_PREVIEW_SIZE_LEVEL.PREVIEW_SIZE_LEVEL_480P,
            PLCameraSetting.CAMERA_PREVIEW_SIZE_LEVEL.PREVIEW_SIZE_LEVEL_720P,
            PLCameraSetting.CAMERA_PREVIEW_SIZE_LEVEL.PREVIEW_SIZE_LEVEL_960P,
            PLCameraSetting.CAMERA_PREVIEW_SIZE_LEVEL.PREVIEW_SIZE_LEVEL_1080P,
            PLCameraSetting.CAMERA_PREVIEW_SIZE_LEVEL.PREVIEW_SIZE_LEVEL_1200P,
    };

    public static final PLVideoEncodeSetting.VIDEO_ENCODING_SIZE_LEVEL[] ENCODING_SIZE_LEVEL_ARRAY = {
            /**
             * 240x240
            */
            PLVideoEncodeSetting.VIDEO_ENCODING_SIZE_LEVEL.VIDEO_ENCODING_SIZE_LEVEL_240P_1,
            /**
             * 320x240
            */
            PLVideoEncodeSetting.VIDEO_ENCODING_SIZE_LEVEL.VIDEO_ENCODING_SIZE_LEVEL_240P_2,
            /**
             * 352x352
            */
            PLVideoEncodeSetting.VIDEO_ENCODING_SIZE_LEVEL.VIDEO_ENCODING_SIZE_LEVEL_352P_1,
            /**
             * 640x352
            */
            PLVideoEncodeSetting.VIDEO_ENCODING_SIZE_LEVEL.VIDEO_ENCODING_SIZE_LEVEL_352P_2,
            /**
             * 360x360
            */
            PLVideoEncodeSetting.VIDEO_ENCODING_SIZE_LEVEL.VIDEO_ENCODING_SIZE_LEVEL_360P_1,
            /**
             * 480x360
            */
            PLVideoEncodeSetting.VIDEO_ENCODING_SIZE_LEVEL.VIDEO_ENCODING_SIZE_LEVEL_360P_2,
            /**
             * 640x360
            */
            PLVideoEncodeSetting.VIDEO_ENCODING_SIZE_LEVEL.VIDEO_ENCODING_SIZE_LEVEL_360P_3,
            /**
             * 480x480
            */
            PLVideoEncodeSetting.VIDEO_ENCODING_SIZE_LEVEL.VIDEO_ENCODING_SIZE_LEVEL_480P_1,
            /**
             * 640x480
            */
            PLVideoEncodeSetting.VIDEO_ENCODING_SIZE_LEVEL.VIDEO_ENCODING_SIZE_LEVEL_480P_2,
            /**
             * 848x480
            */
            PLVideoEncodeSetting.VIDEO_ENCODING_SIZE_LEVEL.VIDEO_ENCODING_SIZE_LEVEL_480P_3,
            /**
             * 544x544
            */
            PLVideoEncodeSetting.VIDEO_ENCODING_SIZE_LEVEL.VIDEO_ENCODING_SIZE_LEVEL_544P_1,
            /**
             * 720x544
            */
            PLVideoEncodeSetting.VIDEO_ENCODING_SIZE_LEVEL.VIDEO_ENCODING_SIZE_LEVEL_544P_2,
            /**
             * 720x720
            */
            PLVideoEncodeSetting.VIDEO_ENCODING_SIZE_LEVEL.VIDEO_ENCODING_SIZE_LEVEL_720P_1,
            /**
             * 960x720
            */
            PLVideoEncodeSetting.VIDEO_ENCODING_SIZE_LEVEL.VIDEO_ENCODING_SIZE_LEVEL_720P_2,
            /**
             * 1280x720
            */
            PLVideoEncodeSetting.VIDEO_ENCODING_SIZE_LEVEL.VIDEO_ENCODING_SIZE_LEVEL_720P_3,
            /**
             * 1088x1088
            */
            PLVideoEncodeSetting.VIDEO_ENCODING_SIZE_LEVEL.VIDEO_ENCODING_SIZE_LEVEL_1088P_1,
            /**
             * 1440x1088
            */
            PLVideoEncodeSetting.VIDEO_ENCODING_SIZE_LEVEL.VIDEO_ENCODING_SIZE_LEVEL_1088P_2,
    };

    public static final int[] ENCODING_BITRATE_LEVEL_ARRAY = {
        500 * 1000,
        800 * 1000,
        1000 * 1000,
        1200 * 1000,
        1600 * 1000,
        2000 * 1000,
        2500 * 1000,
        4000 * 1000,
        8000 * 1000,
    };

    public static final double[] RECORD_SPEED_ARRAY = {
        0.125,
        0.25,
        0.5,
        1,
        2,
        4,
        8
    };
}
