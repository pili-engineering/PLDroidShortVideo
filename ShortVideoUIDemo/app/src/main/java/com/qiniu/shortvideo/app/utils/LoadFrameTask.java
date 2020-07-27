package com.qiniu.shortvideo.app.utils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.qiniu.pili.droid.shortvideo.PLMediaFile;
import com.qiniu.pili.droid.shortvideo.PLVideoFrame;

import java.lang.ref.WeakReference;

/**
 * 加载视频缩略图的 Task
 */
public class LoadFrameTask extends AsyncTask<Void, PLVideoFrame, Void> {

    private final WeakReference<Activity> weakActivity;
    private PLMediaFile mMediaFile;
    private int mFrameCount;
    private int mFrameWidth;
    private int mFrameHeight;
    private OnLoadFrameListener mOnLoadFrameListener;
    private int mFrameLoaded;

    public interface OnLoadFrameListener {
        void onFrameReady(Bitmap bitmap);
    }

    public LoadFrameTask(Activity activity, PLMediaFile mediaFile, int frameCount, int frameWidth, int frameHeight, OnLoadFrameListener listener) {
        weakActivity = new WeakReference<>(activity);
        mMediaFile = mediaFile;
        mFrameCount = frameCount;
        mFrameWidth = frameWidth;
        mFrameHeight = frameHeight;
        mOnLoadFrameListener = listener;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        while (!isCancelled() && mFrameLoaded < mFrameCount) {
            PLVideoFrame frame = mMediaFile.getVideoFrameByTime(
                    (long) ((1.0f * mFrameLoaded++ / mFrameCount) * mMediaFile.getDurationMs()),
                    true, mFrameWidth, mFrameHeight);
            publishProgress(frame);
        }
        mFrameLoaded = 0;
        return null;
    }

    @Override
    protected void onProgressUpdate(PLVideoFrame... values) {
        super.onProgressUpdate(values);

        Activity activity = weakActivity.get();
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
            return;
        }

        PLVideoFrame frame = values[0];
        if (mOnLoadFrameListener != null) {
            mOnLoadFrameListener.onFrameReady(frame == null ? null : frame.toBitmap());
        }
    }
}