package com.faceunity.entity;

import android.os.Parcel;
import android.os.Parcelable;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Transient;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.Arrays;

/**
 * @author LiuQiang on 2018.12.12
 */
@Entity
public class MagicPhotoEntity implements Parcelable {
    // 数组长度
    private static final int BROW_LENGTH = 12;
    private static final int EYE_LENGTH = 16;
    private static final int NOSE_LENGTH = 24;
    private static final int MOUTH_LENGTH = 36;

    // 类型
    public static final int TYPE_LEFT_EYE = 0;
    public static final int TYPE_RIGHT_EYE = 1;
    public static final int TYPE_NOSE = 2;
    public static final int TYPE_MOUTH = 3;
    public static final int TYPE_LEFT_EYEBROW = 4;
    public static final int TYPE_RIGHT_EYEBROW = 5;

    // 操作
    public static final String OPERATION_ADD = "add";
    public static final String OPERATION_DELETE = "delete";

    @Id(autoincrement = true)
    private Long id;
    private int width;
    private int height;
    @Transient
    private double[] groupPoints;
    @Transient
    private double[] groupType;
    private String groupPointsStr;
    private String groupTypeStr;
    private String imagePath;

    public MagicPhotoEntity(int width, int height, double[] groupPoints, double[] groupType, String imagePath) {
        this.width = width;
        this.height = height;
        this.imagePath = imagePath;
        setGroupType(groupType);
        setGroupPoints(groupPoints);
    }

    @Generated(hash = 1265875180)
    public MagicPhotoEntity(Long id, int width, int height, String groupPointsStr, String groupTypeStr, String imagePath) {
        this.id = id;
        this.width = width;
        this.height = height;
        this.groupPointsStr = groupPointsStr;
        this.groupTypeStr = groupTypeStr;
        this.imagePath = imagePath;
    }

    @Generated(hash = 768297740)
    public MagicPhotoEntity() {
    }

    public static int getPointsLength(int type) {
        switch (type) {
            case TYPE_LEFT_EYE:
            case TYPE_RIGHT_EYE:
                return EYE_LENGTH;
            case TYPE_MOUTH:
                return MOUTH_LENGTH;
            case NOSE_LENGTH:
                return NOSE_LENGTH;
            case TYPE_LEFT_EYEBROW:
            case TYPE_RIGHT_EYEBROW:
                return BROW_LENGTH;
            default:
                return 0;
        }
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public double[] getGroupPoints() {
        if (groupPoints == null) {
            if (groupPointsStr != null) {
                try {
                    JSONArray jsonArray = new JSONArray(groupPointsStr);
                    int size = jsonArray.length();
                    groupPoints = new double[size];
                    for (int i = 0; i < size; i++) {
                        groupPoints[i] = jsonArray.optDouble(i);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return groupPoints;
    }

    public void setGroupPoints(double[] groupPoints) {
        if (groupPoints != null) {
            JSONArray jsonArray = new JSONArray();
            for (double groupPoint : groupPoints) {
                try {
                    jsonArray.put(groupPoint);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            this.groupPointsStr = jsonArray.toString();
        }
        this.groupPoints = groupPoints;
    }

    public double[] getGroupType() {
        if (groupType == null) {
            if (groupTypeStr != null) {
                try {
                    JSONArray jsonArray = new JSONArray(groupTypeStr);
                    int size = jsonArray.length();
                    groupType = new double[size];
                    for (int i = 0; i < size; i++) {
                        groupType[i] = jsonArray.optDouble(i);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return groupType;
    }

    public void setGroupType(double[] groupType) {
        if (groupType != null) {
            JSONArray jsonArray = new JSONArray();
            for (double groupPoint : groupType) {
                try {
                    jsonArray.put(groupPoint);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            this.groupTypeStr = jsonArray.toString();
        }
        this.groupType = groupType;
    }

    public String getPath() {
        return imagePath;
    }

    public void setPath(String imagePath) {
        this.imagePath = imagePath;
    }

    private static final int HEIGHT = 500;

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGroupPointsStr() {
        return this.groupPointsStr;
    }

    public void setGroupPointsStr(String groupPointsStr) {
        this.groupPointsStr = groupPointsStr;
    }

    public String getGroupTypeStr() {
        return groupTypeStr;
    }

    public void setGroupTypeStr(String groupTypeStr) {
        this.groupTypeStr = groupTypeStr;
    }

    public String getImagePath() {
        return this.imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    /**
     * 五官贴图的点位，以视图左上角为原点
     */
    public static final float[] LEFT_EYE = {7, 249, 132, 168, 253, 130, 373, 167, 462, 248, 382, 325, 252, 368, 116, 320};
    public static final float[] RIGHT_EYE = {461, 250, 378, 175, 246, 129, 133, 168, 36, 250, 114, 324, 249, 372, 380, 328};
    public static final float[] MOUTH = {462, 240, 394, 197, 310, 162, 253, 185, 197, 161, 121, 187, 39, 236, 75, 273, 142, 315, 247, 339, 350, 318, 411, 284, 345, 250, 249, 275, 150, 251, 154, 236, 249, 249, 336, 231};
    public static final float[] NOSE = {317, 80, 302, 209, 385, 387, 350, 439, 252, 460, 142, 442, 118, 385, 201, 218, 179, 76, 309, 438, 193, 441, 250, 357};
    public static final float[] LEFT_BROW = {40, 250, 238, 188, 452, 241, 457, 319, 365, 284, 177, 249};
    public static final float[] RIGHT_BROW = {465, 260, 258, 184, 48, 242, 43, 313, 165, 274, 310, 249};
    private static final int WIDTH = 500;

    @Override
    public String toString() {
        return "MagicPhotoEntity{" +
                "width=" + width +
                ", height=" + height +
                ", groupTypeStr='" + groupTypeStr + '\'' +
                ", groupPointsStr='" + groupPointsStr + '\'' +
                ", imagePath='" + imagePath + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        MagicPhotoEntity that = (MagicPhotoEntity) o;

        if (width != that.width)
            return false;
        if (height != that.height)
            return false;
        if (id != null ? !id.equals(that.id) : that.id != null)
            return false;
        if (!Arrays.equals(groupPoints, that.groupPoints))
            return false;
        if (!Arrays.equals(groupType, that.groupType))
            return false;
        if (groupPointsStr != null ? !groupPointsStr.equals(that.groupPointsStr) : that.groupPointsStr != null)
            return false;
        if (groupTypeStr != null ? !groupTypeStr.equals(that.groupTypeStr) : that.groupTypeStr != null)
            return false;
        return imagePath != null ? imagePath.equals(that.imagePath) : that.imagePath == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + width;
        result = 31 * result + height;
        result = 31 * result + Arrays.hashCode(groupPoints);
        result = 31 * result + Arrays.hashCode(groupType);
        result = 31 * result + (groupPointsStr != null ? groupPointsStr.hashCode() : 0);
        result = 31 * result + (groupTypeStr != null ? groupTypeStr.hashCode() : 0);
        result = 31 * result + (imagePath != null ? imagePath.hashCode() : 0);
        return result;
    }

    public static final Creator<MagicPhotoEntity> CREATOR = new Creator<MagicPhotoEntity>() {
        @Override
        public MagicPhotoEntity createFromParcel(Parcel source) {
            return new MagicPhotoEntity(source);
        }

        @Override
        public MagicPhotoEntity[] newArray(int size) {
            return new MagicPhotoEntity[size];
        }
    };

    protected MagicPhotoEntity(Parcel in) {
        this.id = (Long) in.readValue(Long.class.getClassLoader());
        this.width = in.readInt();
        this.height = in.readInt();
        this.groupPoints = in.createDoubleArray();
        this.groupType = in.createDoubleArray();
        this.groupPointsStr = in.readString();
        this.groupTypeStr = in.readString();
        this.imagePath = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(this.id);
        dest.writeInt(this.width);
        dest.writeInt(this.height);
        dest.writeDoubleArray(this.groupPoints);
        dest.writeDoubleArray(this.groupType);
        dest.writeString(this.groupPointsStr);
        dest.writeString(this.groupTypeStr);
        dest.writeString(this.imagePath);
    }
}