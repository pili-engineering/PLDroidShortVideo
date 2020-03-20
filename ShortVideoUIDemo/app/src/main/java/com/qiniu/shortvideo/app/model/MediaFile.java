package com.qiniu.shortvideo.app.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 媒体文件实体类
 */
public class MediaFile implements Parcelable {
    public static final int VIDEO = 0;
    public static final int IMAGE = 1;

    private int id;
    private int type;
    private String path;
    private String thumbPath;
    private long duration;
    private String displayName;
    //记录点击时显示的大小的位置
    private int position;
    private long addTime;

    public MediaFile() {
    }

    //图片
    public MediaFile(int id, int type, String path, String thumbPath, String displayName, long addTime) {
        this.id = id;
        this.type = type;
        this.path = path;
        this.thumbPath = thumbPath;
        this.displayName = displayName;
        this.duration = -1;
        this.addTime = addTime;
    }

    //视频
    public MediaFile(int id, int type, String path, String thumbPath, long duration, String displayName, long addTime) {
        this.id = id;
        this.type = type;
        this.path = path;
        this.thumbPath = thumbPath;
        this.duration = duration;
        this.displayName = displayName;
        this.addTime = addTime;
    }

    protected MediaFile(Parcel in) {
        id = in.readInt();
        type = in.readInt();
        path = in.readString();
        thumbPath = in.readString();
        duration = in.readLong();
        displayName = in.readString();
        position = in.readInt();
        addTime = in.readLong();
    }

    public static final Creator<MediaFile> CREATOR = new Creator<MediaFile>() {
        @Override
        public MediaFile createFromParcel(Parcel in) {
            return new MediaFile(in);
        }

        @Override
        public MediaFile[] newArray(int size) {
            return new MediaFile[size];
        }
    };

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getPath() {
        return path == null ? "" : path;
    }

    public void setPath(String path) {
        this.path = path == null ? "" : path;
    }

    public String getThumbPath() {
        return thumbPath == null ? "" : thumbPath;
    }

    public void setThumbPath(String thumbPath) {
        this.thumbPath = thumbPath == null ? "" : thumbPath;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getDisplayName() {
        return displayName == null ? "" : displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName == null ? "" : displayName;
    }

    public long getAddTime() {
        return addTime;
    }

    public void setAddTime(long addTime) {
        this.addTime = addTime;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(type);
        dest.writeString(path);
        dest.writeString(thumbPath);
        dest.writeLong(duration);
        dest.writeString(displayName);
        dest.writeInt(position);
        dest.writeLong(addTime);
    }
}
