package com.faceunity.entity;

import java.util.List;

/**
 * @author Richie on 2019.03.22
 * Avatar 组件，脸型、鼻子、眼睛等等
 */
public class AvatarComponent {
    public static final int CUSTOM = 1;
    public static final int NOT_CUSTOM = 0;
    private int type;
    private int iconId;
    private boolean isCustom;
    private List<AvatarFaceAspect> avatarFaceAspects;
    private String bundlePath;

    public AvatarComponent(String bundlePath, int type, int iconId) {
        this.bundlePath = bundlePath;
        this.type = type;
        this.iconId = iconId;
    }

    public AvatarComponent(int type, int iconId, List<AvatarFaceAspect> avatarFaceAspects) {
        this(type, iconId, avatarFaceAspects, false);
    }

    public AvatarComponent(int type, int iconId, List<AvatarFaceAspect> avatarFaceAspects, boolean isCustom) {
        this.type = type;
        this.iconId = iconId;
        this.avatarFaceAspects = avatarFaceAspects;
        this.isCustom = isCustom;
    }

    public String getBundlePath() {
        return bundlePath;
    }

    public void setBundlePath(String bundlePath) {
        this.bundlePath = bundlePath;
    }

    public boolean isCustom() {
        return isCustom;
    }

    public void setCustom(boolean custom) {
        isCustom = custom;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getIconId() {
        return iconId;
    }

    public void setIconId(int iconId) {
        this.iconId = iconId;
    }

    public List<AvatarFaceAspect> getAvatarFaceAspects() {
        return avatarFaceAspects;
    }

    public void setAvatarFaceAspects(List<AvatarFaceAspect> avatarFaceAspects) {
        this.avatarFaceAspects = avatarFaceAspects;
    }

    @Override
    public String toString() {
        return "AvatarComponent{" +
                "type=" + type +
                ", iconId=" + iconId +
                ", bundlePath='" + bundlePath + '\'' +
                ", avatarFaceAspects=" + avatarFaceAspects +
                '}';
    }

}