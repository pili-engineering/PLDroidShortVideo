package com.faceunity.entity;

import android.os.Parcel;
import android.os.Parcelable;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Transient;

/**
 * @author Richie on 2019.03.20
 */
@Entity
public class AvatarModel implements Parcelable {
    public static final int MALE = 1;
    public static final int FEMALE = 0;

    public static final Creator<AvatarModel> CREATOR = new Creator<AvatarModel>() {
        @Override
        public AvatarModel createFromParcel(Parcel source) {
            return new AvatarModel(source);
        }

        @Override
        public AvatarModel[] newArray(int size) {
            return new AvatarModel[size];
        }
    };
    @Id(autoincrement = true)
    private Long id;
    @Transient
    private int iconId;
    @Transient
    private boolean isDefault;
    private String iconPath;
    private String configJson;
    // 性别，男 1，女 0
    private int gender;

    public AvatarModel(int iconId, boolean isDefault, int gender) {
        this.iconId = iconId;
        this.isDefault = isDefault;
        this.gender = gender;
    }

    @Generated(hash = 1021308026)
    public AvatarModel(Long id, String iconPath, String configJson, int gender) {
        this.id = id;
        this.iconPath = iconPath;
        this.configJson = configJson;
        this.gender = gender;
    }

    @Generated(hash = 1677474030)
    public AvatarModel() {
    }

    protected AvatarModel(Parcel in) {
        this.id = (Long) in.readValue(Long.class.getClassLoader());
        this.iconId = in.readInt();
        this.gender = in.readInt();
        this.isDefault = in.readByte() != 0;
        this.iconPath = in.readString();
        this.configJson = in.readString();
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    public int getIconId() {
        return iconId;
    }

    public void setIconId(int iconId) {
        this.iconId = iconId;
    }

    public String getIconPath() {
        return iconPath;
    }

    public void setIconPath(String iconPath) {
        this.iconPath = iconPath;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean getIsDefault() {
        return this.isDefault;
    }

    public void setIsDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public String getConfigJson() {
        return this.configJson;
    }

    public void setConfigJson(String configJson) {
        this.configJson = configJson;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        AvatarModel that = (AvatarModel) o;

        if (gender != that.gender)
            return false;
        if (iconPath != null ? !iconPath.equals(that.iconPath) : that.iconPath != null)
            return false;
        return configJson != null ? configJson.equals(that.configJson) : that.configJson == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (iconPath != null ? iconPath.hashCode() : 0);
        result = 31 * result + (configJson != null ? configJson.hashCode() : 0);
        result = 31 * result + gender;
        return result;
    }

    public AvatarModel cloneIt() {
        AvatarModel avatarModel = new AvatarModel();
        avatarModel.gender = this.gender;
        avatarModel.iconId = this.iconId;
        avatarModel.configJson = this.configJson + ""; // deep copy string
        avatarModel.iconPath = this.iconPath + "";
        return avatarModel;
    }

    @Override
    public String toString() {
        return "AvatarModel{" +
                "id=" + id +
                ", iconId=" + iconId +
                ", isDefault=" + isDefault +
                ", iconPath='" + iconPath + '\'' +
                ", configJson='" + configJson + '\'' +
                ", gender=" + gender +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(this.id);
        dest.writeInt(this.iconId);
        dest.writeInt(this.gender);
        dest.writeByte(this.isDefault ? (byte) 1 : (byte) 0);
        dest.writeString(this.iconPath);
        dest.writeString(this.configJson);
    }
}
