package com.faceunity.entity;

import java.util.Arrays;

/**
 * @author Richie on 2019.03.22
 * Avatar 捏脸维度
 */
public class AvatarFaceAspect {
    // 图像开发定义的奇葩键值，两个 channel0，完全看不懂含义，需要自己处理
    // 肤色
    public static final String COLOR_FACE = "channel0";
    // 头发
    public static final String COLOR_HAIR = "channel0";
    // 唇色
    public static final String COLOR_LIP = "channel1";
    // 眼睛色 值为rgb数组，[r,g,b]（范围0~255）
    public static final String COLOR_EYE = "channel2";

    private int iconId;
    private int descriptionId;
    private int type;
    private String name;
    private float level;
    private String bundlePath;
    private double[] color;

    public AvatarFaceAspect() {
    }

    public AvatarFaceAspect(int type, String name, float level, int iconId, int descriptionId) {
        this(type, name, level, iconId, descriptionId, null, null);
    }

    public AvatarFaceAspect(int type, String name, float level, int iconId, int descriptionId, String bundlePath, double[] color) {
        this.type = type;
        this.name = name;
        this.level = level;
        this.iconId = iconId;
        this.descriptionId = descriptionId;
        this.bundlePath = bundlePath;
        this.color = color;
    }

    public static AvatarFaceAspect ofValue(int type, String name, float level) {
        AvatarFaceAspect avatarFaceAspect = new AvatarFaceAspect();
        avatarFaceAspect.type = type;
        avatarFaceAspect.name = name;
        avatarFaceAspect.level = level;
        return avatarFaceAspect;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIconId() {
        return iconId;
    }

    public void setIconId(int iconId) {
        this.iconId = iconId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public float getLevel() {
        return level;
    }

    public void setLevel(float level) {
        this.level = level;
    }

    public String getBundlePath() {
        return bundlePath;
    }

    public void setBundlePath(String bundlePath) {
        this.bundlePath = bundlePath;
    }

    public int getDescriptionId() {
        return descriptionId;
    }

    public void setDescriptionId(int descriptionId) {
        this.descriptionId = descriptionId;
    }

    public double[] getColor() {
        return color;
    }

    public void setColor(double[] color) {
        this.color = color;
    }

    @Override
    public String toString() {
        return "AvatarFaceAspect{" +
                "type=" + type +
                ", name='" + name + '\'' +
                ", level=" + level +
                ", iconId=" + iconId +
                ", descriptionId=" + descriptionId +
                ", bundlePath='" + bundlePath + '\'' +
                ", color=" + Arrays.toString(color) +
                '}';
    }
}
