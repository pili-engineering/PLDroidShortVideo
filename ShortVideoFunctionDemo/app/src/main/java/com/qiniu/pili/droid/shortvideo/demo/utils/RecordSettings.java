package com.qiniu.pili.droid.shortvideo.demo.utils;

import android.media.AudioFormat;
import android.media.MediaRecorder;

import com.qiniu.pili.droid.shortvideo.PLCameraSetting;
import com.qiniu.pili.droid.shortvideo.PLVideoEncodeSetting;

public class RecordSettings {
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

    // PLMicrophoneSetting
    public static final int[] SAMPLING_FORMAT_ARRAY = {
            AudioFormat.ENCODING_INVALID,
            AudioFormat.ENCODING_DEFAULT,
            AudioFormat.ENCODING_PCM_16BIT,
            AudioFormat.ENCODING_PCM_8BIT,
            AudioFormat.ENCODING_PCM_FLOAT,
            AudioFormat.ENCODING_AC3,
            AudioFormat.ENCODING_E_AC3,
            AudioFormat.ENCODING_DTS,
            AudioFormat.ENCODING_DTS_HD,
            AudioFormat.ENCODING_MP3,
            AudioFormat.ENCODING_AAC_LC,
            AudioFormat.ENCODING_AAC_HE_V1,
            AudioFormat.ENCODING_AAC_HE_V2,
            AudioFormat.ENCODING_IEC61937,
            AudioFormat.ENCODING_DOLBY_TRUEHD,
            AudioFormat.ENCODING_AAC_ELD,
            AudioFormat.ENCODING_AAC_XHE,
            AudioFormat.ENCODING_AC4,
            AudioFormat.ENCODING_E_AC3_JOC,
            AudioFormat.ENCODING_DOLBY_MAT,
            AudioFormat.ENCODING_OPUS,
    };

    public static final int[] SAMPLING_SOURCE_ARRAY = {
            MediaRecorder.AudioSource.DEFAULT,
            MediaRecorder.AudioSource.MIC,
            MediaRecorder.AudioSource.VOICE_UPLINK,
            MediaRecorder.AudioSource.VOICE_DOWNLINK,
            MediaRecorder.AudioSource.VOICE_CALL,
            MediaRecorder.AudioSource.CAMCORDER,
            MediaRecorder.AudioSource.VOICE_RECOGNITION,
            MediaRecorder.AudioSource.VOICE_COMMUNICATION,
            MediaRecorder.AudioSource.REMOTE_SUBMIX,
            MediaRecorder.AudioSource.UNPROCESSED,
            MediaRecorder.AudioSource.VOICE_PERFORMANCE,
    };

    public static final int[] SAMPLING_CHANNEL_NUM_ARRAY = {
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.CHANNEL_IN_STEREO
    };

    public static final double[] RECORD_SPEED_ARRAY = {
            0.25,
            0.5,
            1,
            2,
            4,
    };

    public static PLCameraSetting.CAMERA_FACING_ID chooseCameraFacingId() {
        if (PLCameraSetting.hasCameraFacing(PLCameraSetting.CAMERA_FACING_ID.CAMERA_FACING_FRONT)) {
            return PLCameraSetting.CAMERA_FACING_ID.CAMERA_FACING_FRONT;
        } else {
            return PLCameraSetting.CAMERA_FACING_ID.CAMERA_FACING_BACK;
        }
    }
}
