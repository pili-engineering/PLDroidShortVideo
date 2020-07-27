package com.qiniu.shortvideo.app.utils;

import com.qiniu.pili.droid.shortvideo.PLMediaFile;

/**
 * 缩略图获取设置类
 */
public class ThumbLineViewSettings {
    private int mThumbnailCount = 20;
    private int mThumbnailWidth = 50;
    private int mThumbnailHeight = 50;
    private int mScreenWidth;
    private long mVideoDuration;
    private PLMediaFile mMediaFile;

    public int getThumbnailCount() {
        return mThumbnailCount;
    }

    public ThumbLineViewSettings setThumbnailCount(int thumbnailCount) {
        mThumbnailCount = thumbnailCount;
        return this;
    }

    public int getThumbnailWidth() {
        return mThumbnailWidth;
    }

    public ThumbLineViewSettings setThumbnailWidth(int thumbnailWidth) {
        mThumbnailWidth = thumbnailWidth;
        return this;
    }

    public int getThumbnailHeight() {
        return mThumbnailHeight;
    }

    public ThumbLineViewSettings setThumbnailHeight(int thumbnailHeight) {
        mThumbnailHeight = thumbnailHeight;
        return this;
    }

    public long getVideoDuration() {
        return mVideoDuration;
    }

    public ThumbLineViewSettings setVideoDuration(long videoDuration) {
        mVideoDuration = videoDuration;
        return this;
    }

    public int getScreenWidth() {
        return mScreenWidth;
    }

    public ThumbLineViewSettings setScreenWidth(int screenWidth) {
        mScreenWidth = screenWidth;
        return this;
    }

    public PLMediaFile getMediaFile() {
        return mMediaFile;
    }

    public ThumbLineViewSettings setMediaFile(PLMediaFile mediaFile) {
        mMediaFile = mediaFile;
        return this;
    }
}
