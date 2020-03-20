package com.faceunity.entity;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Arrays;

/**
 * @author Richie on 2019.03.21
 * 捏脸五官类型
 */
public class AvatarFaceType implements Parcelable {

    public static final int AVATAR_FACE_HAIR = 0;
    public static final int AVATAR_FACE_SHAPE = 1;
    public static final int AVATAR_FACE_EYE = 2;
    public static final int AVATAR_FACE_LIP = 3;
    public static final int AVATAR_FACE_NOSE = 4;
    //    public static final int AVATAR_FACE_EYEBROW = 5;
//    public static final int AVATAR_FACE_EYELASH = 6;
    public static final Creator<AvatarFaceType> CREATOR = new Creator<AvatarFaceType>() {
        @Override
        public AvatarFaceType createFromParcel(Parcel source) {
            return new AvatarFaceType(source);
        }

        @Override
        public AvatarFaceType[] newArray(int size) {
            return new AvatarFaceType[size];
        }
    };
    private String name;
    private int type;
    private double[][] colors;

    public AvatarFaceType(String name, int type) {
        this.name = name;
        this.type = type;
    }

    public AvatarFaceType(String name, int type, double[][] colors) {
        this.name = name;
        this.type = type;
        this.colors = colors;
    }

    protected AvatarFaceType(Parcel in) {
        this.name = in.readString();
        this.type = in.readInt();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public double[][] getColors() {
        return colors;
    }

    public void setColors(double[][] colors) {
        this.colors = colors;
    }

    @Override
    public String toString() {
        return "AvatarFaceType{" +
                "name='" + name + '\'' +
                ", type=" + type +
                ", colors=" + Arrays.toString(colors) +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeInt(this.type);
    }
}
