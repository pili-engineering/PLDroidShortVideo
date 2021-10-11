package com.qiniu.pili.droid.shortvideo.demo.gif;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;

/**
 * GIF 动图加载器
 */
public class GifFrameLoader {
    private final GifDecoder mGifDecoder;
    private final Handler mHandler;
    private Bitmap mCurrentFrame;
    private Bitmap mNextFrame;
    private final Bitmap mFirstFrame;
    private Bitmap mPendingFrame;

    private boolean mIsRunning;
    private boolean mIsLoadPending;
    private boolean mStartFromFirstFrame;
    private boolean mIsCleared;

    private FrameCallback mFrameCallback;

    public interface FrameCallback {
        void onFrameReady();
    }

    public GifFrameLoader(GifDecoder gifDecoder, Bitmap firstFrame) {
        mGifDecoder = gifDecoder;
        mFirstFrame = firstFrame;
        mHandler = new Handler(Looper.getMainLooper(), new FrameLoaderCallback());
    }

    public void setFrameCallback(FrameCallback callback) {
        mFrameCallback = callback;
    }

    public void start() {
        if (mIsRunning) {
            return;
        }
        mIsRunning = true;
        mIsCleared = false;

        loadNextFrame();
    }

    public void stop() {
        mIsRunning = false;
    }

    public boolean isRunning() {
        return mIsRunning;
    }

    public void setNextStartFromFirstFrame() {
        mStartFromFirstFrame = true;
        if (mPendingFrame != null) {
            mPendingFrame.recycle();
            mPendingFrame = null;
        }
    }

    public Bitmap getCurrentFrame() {
        return mCurrentFrame != null ? mCurrentFrame : mFirstFrame;
    }

    private void loadNextFrame() {
        if (!mIsRunning || mIsLoadPending) {
            return;
        }
        if (mStartFromFirstFrame) {
            if (mPendingFrame != null) {
                mPendingFrame = null;
            }
            mGifDecoder.resetFramePointer();
            mStartFromFirstFrame = false;
        }
        if (mPendingFrame != null) {
            Bitmap temp = mPendingFrame;
            mPendingFrame = null;
            onFrameAvailable(temp);
            return;
        }
        mIsLoadPending = true;
        // Get the delay before incrementing the pointer because the delay indicates the amount of time
        // we want to spend on the current frame.
        int delay = mGifDecoder.getNextDelay();
        long targetTime = SystemClock.uptimeMillis() + delay;

        mGifDecoder.advance();
        mNextFrame = mGifDecoder.getNextFrame();
        Message msg = mHandler.obtainMessage(FrameLoaderCallback.MSG_DELAY, mNextFrame);
        mHandler.sendMessageAtTime(msg, targetTime);
    }

    private void onFrameAvailable(Bitmap frame) {
        mIsLoadPending = false;
        if (mIsCleared) {
            mHandler.obtainMessage(FrameLoaderCallback.MSG_CLEAR, frame).sendToTarget();
            return;
        }
        if (!mIsRunning) {
            mPendingFrame = frame;
            return;
        }
        if (frame != null) {
            mCurrentFrame = frame;
            if (mFrameCallback != null) {
                mFrameCallback.onFrameReady();
            }
        }
        loadNextFrame();
    }

    private class FrameLoaderCallback implements Handler.Callback {
        static final int MSG_DELAY = 1;
        static final int MSG_CLEAR = 2;

        FrameLoaderCallback() {
        }

        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == MSG_DELAY) {
                Bitmap bitmap = (Bitmap) msg.obj;
                onFrameAvailable(bitmap);
                return true;
            } else if (msg.what == MSG_CLEAR) {
                Bitmap bitmap = (Bitmap) msg.obj;
                bitmap.recycle();
            }
            return false;
        }
    }
}
