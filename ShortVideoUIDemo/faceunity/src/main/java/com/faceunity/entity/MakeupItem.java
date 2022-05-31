package com.faceunity.entity;

/**
 * @author LiuQiang on 2018.11.12
 * 妆容单项
 */
public class MakeupItem {
    private String name;
    private String path;
    private int type;
    private int iconId;
    private int strId;
    public static final float DEFAULT_MAKEUP_LEVEL = 0.4f;
    private float level;
    private float defaultLevel;

    public MakeupItem(String name, String path, int type, int strId, int iconId) {
        this(name, path, type, strId, iconId, DEFAULT_MAKEUP_LEVEL);
    }

    public MakeupItem(String name, String path, int type, int strId, int iconId, float level) {
        this(name, path, type, strId, iconId, level, level);
    }

    public MakeupItem(String name, String path, int type, int strId, int iconId, float level, float defaultLevel) {
        this.name = name;
        this.path = path;
        this.type = type;
        this.strId = strId;
        this.iconId = iconId;
        this.level = level;
        this.defaultLevel = defaultLevel;
    }

    public MakeupItem cloneSelf() {
        return new MakeupItem(name, path, type, strId, iconId, level);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
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

    public int getStrId() {
        return strId;
    }

    public void setStrId(int strId) {
        this.strId = strId;
    }

    public float getLevel() {
        return level;
    }

    public void setLevel(float level) {
        this.level = level;
    }

    public float getDefaultLevel() {
        return defaultLevel;
    }

    public void setDefaultLevel(float defaultLevel) {
        this.defaultLevel = defaultLevel;
    }

    @Override
    public String toString() {
        return "MakeupItem{" +
                "name='" + name + '\'' +
                ", path='" + path + '\'' +
                ", type=" + type +
                ", iconId=" + iconId +
                ", strId=" + strId +
                ", level=" + level +
                ", defaultLevel=" + defaultLevel +
                '}';
    }
}
