package com.qiniu.pili.droid.shortvideo.demo.activity;

import com.pili.pldroid.player.PLOnCompletionListener;
import com.qiniu.pili.droid.shortvideo.PLAudioEncodeSetting;
import com.qiniu.pili.droid.shortvideo.PLExternalMediaRecorder;
import com.qiniu.pili.droid.shortvideo.PLExternalRecordStateListener;

import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.qiniu.pili.droid.shortvideo.PLRecordSetting;
import com.qiniu.pili.droid.shortvideo.PLVideoEncodeSetting;
import com.qiniu.pili.droid.shortvideo.demo.R;

import com.pili.pldroid.player.AVOptions;
import com.pili.pldroid.player.PLOnAudioFrameListener;
import com.pili.pldroid.player.PLOnVideoFrameListener;
import com.pili.pldroid.player.widget.PLVideoTextureView;

import java.io.IOException;

public class ExternalMediaRecordActivity extends AppCompatActivity implements View.OnClickListener, PLExternalRecordStateListener {
    private static final String TAG = "ExternalRecordActivity";

    private PLExternalMediaRecorder mExternalMediaRecorder;

    private final static String OUTPUT_VIDEO_PATH = "/sdcard/ShortVideo/ExternalRecord.mp4";
    private final static String SRC_VIDEO_FILE_PATH = "/sdcard/ShortVideo/record.mp4";

    // video parameters
    private int mVideoFrameWidth = 480;
    private int mVideoFrameHeight = 480;
    private int mFrameRate = 25;

    // audio parameters
    private int mSampleRate = 44100;
    private int mChannels = 1;
    private long mAudioPacketCount = 0;

    // Buttons
    private Button mPlayButton;
    private Button mStopButton;

    // VideoView
    private PLVideoTextureView mVideoTextureView;

    private MediaExtractor mSrcMediaExtractor;
    private MediaFormat mSrcVideoFormat;
    private MediaFormat mSrcAudioFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_external_media_record);

        if (!getSourceVideoParameters()) {
            Log.e(TAG, "Getting paramaters of source video file failed!!!");
            return;
        }

        PLVideoEncodeSetting videoEncodeSetting = new PLVideoEncodeSetting(this);
        videoEncodeSetting.setEncodingBitrate(1600 * 1000);
        videoEncodeSetting.setEncodingFps(mFrameRate);
        videoEncodeSetting.setPreferredEncodingSize(mVideoFrameWidth, mVideoFrameHeight);

        PLAudioEncodeSetting audioEncodeSetting = new PLAudioEncodeSetting();
        audioEncodeSetting.setSampleRate(mSampleRate);
        audioEncodeSetting.setChannels(mChannels);

        PLRecordSetting recordSetting = new PLRecordSetting();
        recordSetting.setVideoFilepath(OUTPUT_VIDEO_PATH);

        mExternalMediaRecorder = new PLExternalMediaRecorder(this);
        mExternalMediaRecorder.setRecordStateListener(this);
        mExternalMediaRecorder.prepare(videoEncodeSetting, audioEncodeSetting, recordSetting);

        mPlayButton = (Button)findViewById(R.id.play);
        mStopButton = (Button)findViewById(R.id.stop);
        mPlayButton.setOnClickListener(this);
        mStopButton.setOnClickListener(this);

        mVideoTextureView = (PLVideoTextureView)findViewById(R.id.video);
        mVideoTextureView.setVideoPath(SRC_VIDEO_FILE_PATH);

        AVOptions options = new AVOptions();
        options.setInteger(AVOptions.KEY_VIDEO_DATA_CALLBACK, 1);
        mVideoTextureView.setAVOptions(options);
        mVideoTextureView.setOnVideoFrameListener(new PLOnVideoFrameListener() {
            @Override
            public void onVideoFrameAvailable(byte[] data, int size, int width, int height, int format, long ts) {
                if (format == 0) {
                    mExternalMediaRecorder.inputVideoFrame(data, width, height, 0, ts * 1000000);
                }
            }
        });

        mVideoTextureView.setOnAudioFrameListener(new PLOnAudioFrameListener() {
            @Override
            public void onAudioFrameAvailable(byte[] data, int size, int samplerate, int channels, int datawidth, long ts) {
                long audioPacketDuration = 1000 * size / (samplerate * channels * (datawidth / 8));
                long packetTimestamp = audioPacketDuration * mAudioPacketCount;
                mExternalMediaRecorder.inputAudioFrame(data, size, packetTimestamp * 1000000);
                mAudioPacketCount++;
            }
        });

        mVideoTextureView.setOnCompletionListener(new PLOnCompletionListener() {
            @Override
            public void onCompletion() {
                if (mPlayButton.isEnabled()) {
                    mPlayButton.setEnabled(false);
                }
                if (mStopButton.isEnabled()) {
                    mStopButton.setEnabled(false);
                }
                mExternalMediaRecorder.stop();
                if (mVideoTextureView != null) {
                    mVideoTextureView.stopPlayback();
                    mVideoTextureView = null;
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        view.setEnabled(false);
        switch (view.getId()) {
            case R.id.play:
                if ((mVideoTextureView != null) && (!mVideoTextureView.isPlaying())) {
                    mVideoTextureView.start();
                    mExternalMediaRecorder.start();
                }
                break;
            case R.id.stop:
                if (mVideoTextureView.isPlaying()) {
                    mExternalMediaRecorder.stop();
                    if (mVideoTextureView != null) {
                        mVideoTextureView.stopPlayback();
                        mVideoTextureView = null;
                    }
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        mExternalMediaRecorder.stop();
        if (mVideoTextureView != null) {
            mVideoTextureView.stopPlayback();
            mVideoTextureView = null;
        }

        super.onDestroy();
    }

    private boolean getSourceVideoParameters() {
        mSrcMediaExtractor = new MediaExtractor();
        try {
            mSrcMediaExtractor.setDataSource(SRC_VIDEO_FILE_PATH);
        } catch (IOException e) {
            Log.e(TAG, "file video setDataSource failed: " + e.getMessage());
            return false;
        }

        final int srcVideoTrackIndex = findTrack(mSrcMediaExtractor, "video/");
        if (srcVideoTrackIndex < 0) {
            Log.e(TAG, "cannot find video in file!");
            return false;
        }
        final int srcAudioTrackIndex = findTrack(mSrcMediaExtractor, "audio/");
        if (srcAudioTrackIndex < 0) {
            Log.e(TAG, "cannot find audio in file!");
            return false;
        }

        mSrcVideoFormat = mSrcMediaExtractor.getTrackFormat(srcVideoTrackIndex);
        if (mSrcVideoFormat == null) {
            Log.e(TAG, "cannot find video format!");
            return false;
        }
        if (mSrcVideoFormat.containsKey(MediaFormat.KEY_WIDTH)) {
            mVideoFrameWidth = mSrcVideoFormat.getInteger(MediaFormat.KEY_WIDTH);
        }
        if (mSrcVideoFormat.containsKey(MediaFormat.KEY_HEIGHT)) {
            mVideoFrameHeight = mSrcVideoFormat.getInteger(MediaFormat.KEY_HEIGHT);
        }
        if (mSrcVideoFormat.containsKey(MediaFormat.KEY_FRAME_RATE)) {
            mFrameRate = mSrcVideoFormat.getInteger(MediaFormat.KEY_FRAME_RATE);
        }

        mSrcAudioFormat = mSrcMediaExtractor.getTrackFormat(srcAudioTrackIndex);
        if (mSrcAudioFormat == null) {
            Log.e(TAG, "cannot find audio format!");
            return false;
        }
        if (mSrcAudioFormat.containsKey(MediaFormat.KEY_SAMPLE_RATE)) {
            mSampleRate = mSrcAudioFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE);
        }
        if (mSrcAudioFormat.containsKey(MediaFormat.KEY_CHANNEL_COUNT)) {
            mChannels = mSrcAudioFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
        }
        Log.i(TAG, "Video width:" + mVideoFrameWidth + ", height:" + mVideoFrameHeight + ", framerate:" + mFrameRate + "; Audio samplerate: " + mSampleRate + ", channels:" + mChannels);

        return true;
    }

    public static int findTrack(final MediaExtractor extractor, final String mimeType) {
        if (extractor == null || mimeType == null) {
            Log.e(TAG, "find track error : extractor or mimeType can't be null!");
            return -1;
        }
        final int numTracks = extractor.getTrackCount();
        MediaFormat format;
        String mime;
        for (int i = 0; i < numTracks; i++) {
            format = extractor.getTrackFormat(i);
            mime = format.getString(MediaFormat.KEY_MIME);
            if (mime.startsWith(mimeType)) {
                Log.i(TAG, "Extractor found track " + i + " (" + mime + "): " + format);
                return i;
            }
        }
        return -1;
    }

    @Override
    public void onReady() {
        Log.d(TAG, "Ready to encode.");
    }

    @Override
    public void onError(int i) {
        Log.e(TAG, "Something goes wrong.");
    }

    @Override
    public void onRecordStarted() {
        Log.d(TAG, "Encoding started.");
    }

    @Override
    public void onRecordStopped() {
        Log.d(TAG, "Encoding stopped.");
    }
}
