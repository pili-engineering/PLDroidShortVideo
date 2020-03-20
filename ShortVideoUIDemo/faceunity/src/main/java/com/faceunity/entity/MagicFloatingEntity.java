package com.faceunity.entity;

import com.faceunity.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author LiuQiang on 2018.12.13
 */
public class MagicFloatingEntity {
    private int iconId;
    private int nameId;
    private int type;
    private String imageAssetsPath;
    private float[] dots;

    public MagicFloatingEntity(int iconId, String imageAssetsPath, int nameId, int type, float[] dots) {
        this.iconId = iconId;
        this.imageAssetsPath = imageAssetsPath;
        this.nameId = nameId;
        this.type = type;
        this.dots = dots;
    }

    public static List<MagicFloatingEntity> getDefaultEntities() {
        List<MagicFloatingEntity> recyclerItemEntities = new ArrayList<>();
        recyclerItemEntities.add(new MagicFloatingEntity(R.drawable.icon_left_eye, "image/magic_adjust_leye.png", R.string.magic_left_eye, MagicPhotoEntity.TYPE_LEFT_EYE, MagicPhotoEntity.LEFT_EYE));
        recyclerItemEntities.add(new MagicFloatingEntity(R.drawable.icon_right_eye, "image/magic_adjust_reye.png", R.string.magic_right_eye, MagicPhotoEntity.TYPE_RIGHT_EYE, MagicPhotoEntity.RIGHT_EYE));
        recyclerItemEntities.add(new MagicFloatingEntity(R.drawable.icon_mouth, "image/magic_adjust_mouth.png", R.string.magic_mouth, MagicPhotoEntity.TYPE_MOUTH, MagicPhotoEntity.MOUTH));
        recyclerItemEntities.add(new MagicFloatingEntity(R.drawable.icon_nose, "image/magic_adjust_nose.png", R.string.magic_nose, MagicPhotoEntity.TYPE_NOSE, MagicPhotoEntity.NOSE));
        recyclerItemEntities.add(new MagicFloatingEntity(R.drawable.icon_left_eyebrow, "image/magic_adjust_lbrow.png", R.string.magic_left_eyebrow, MagicPhotoEntity.TYPE_LEFT_EYEBROW, MagicPhotoEntity.LEFT_BROW));
        recyclerItemEntities.add(new MagicFloatingEntity(R.drawable.icon_right_eyebrow, "image/magic_adjust_rbrow.png", R.string.magic_right_eyebrow, MagicPhotoEntity.TYPE_RIGHT_EYEBROW, MagicPhotoEntity.RIGHT_BROW));
        return recyclerItemEntities;
    }

    public int getIconId() {
        return iconId;
    }

    public void setIconId(int iconId) {
        this.iconId = iconId;
    }

    public int getNameId() {
        return nameId;
    }

    public void setNameId(int nameId) {
        this.nameId = nameId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getImageAssetsPath() {
        return imageAssetsPath;
    }

    public void setImageAssetsPath(String imageAssetsPath) {
        this.imageAssetsPath = imageAssetsPath;
    }

    public float[] getDots() {
        return dots;
    }

    public void setDots(float[] dots) {
        this.dots = dots;
    }

    @Override
    public String toString() {
        return "MagicFloatingEntity{" +
                "iconId=" + iconId +
                ", nameId=" + nameId +
                ", type=" + type +
                ", imageAssetsPath='" + imageAssetsPath + '\'' +
                ", dots=" + Arrays.toString(dots) +
                '}';
    }
}
