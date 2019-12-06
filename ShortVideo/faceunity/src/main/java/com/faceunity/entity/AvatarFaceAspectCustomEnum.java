package com.faceunity.entity;

import com.faceunity.R;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Richie on 2019.03.22
 * 自定义 avatar 脸型
 */
public enum AvatarFaceAspectCustomEnum {

    /**
     * 脸型
     */
    FACE_LENGTH_STRETCH("4", 0, R.drawable.sel_edit_avatar_face_length, R.string.avatar_face_length, AvatarFaceType.AVATAR_FACE_SHAPE),
    FACE_WIDTH_FAT("2", 0, R.drawable.sel_edit_avatar_face_face_width, R.string.avatar_face_width, AvatarFaceType.AVATAR_FACE_SHAPE),
    CHIN_WIDTH_WIDE("10", 0, R.drawable.sel_edit_avatar_face_chin_width, R.string.avatar_chin_width, AvatarFaceType.AVATAR_FACE_SHAPE),
    CHIN_HEIGHT_UP("8", 0, R.drawable.sel_edit_avatar_face_chin_height, R.string.avatar_chin_height, AvatarFaceType.AVATAR_FACE_SHAPE),
    /**
     * 眼睛
     */
    EYE_POSITION_UP("36", 0, R.drawable.sel_edit_avatar_face_eye_position, R.string.avatar_eye_position, AvatarFaceType.AVATAR_FACE_EYE),
    EYE_CORNER_HEIGHT_UP("35", 0, R.drawable.sel_edit_avatar_face_eye_corner_height, R.string.avatar_eye_corner_height, AvatarFaceType.AVATAR_FACE_EYE),
    EYE_HEIGHT_OPEN("33", 0, R.drawable.sel_edit_avatar_face_eye_height, R.string.avatar_eye_height, AvatarFaceType.AVATAR_FACE_EYE),
    EYE_WIDTH_OUT("28", 0, R.drawable.sel_edit_avatar_face_eye_width, R.string.avatar_eye_width, AvatarFaceType.AVATAR_FACE_EYE),
    /**
     * 鼻子
     */
    NOSE_POSITION_UP("19", 0, R.drawable.sel_edit_avatar_face_nose_position, R.string.avatar_nose_position, AvatarFaceType.AVATAR_FACE_NOSE),
    NOSE_WIDTH_OUT("23", 0, R.drawable.sel_edit_avatar_face_nose_width, R.string.avatar_nose_width, AvatarFaceType.AVATAR_FACE_NOSE),
    NOSE_HEIGHT_UP("21", 0, R.drawable.sel_edit_avatar_face_nose_height, R.string.avatar_nose_height, AvatarFaceType.AVATAR_FACE_NOSE),
    /**
     * 嘴唇
     */
    MOUTH_POSITION_UP("17", 0, R.drawable.sel_edit_avatar_face_mouth_position, R.string.avatar_mouth_position, AvatarFaceType.AVATAR_FACE_LIP),
    UP_LIP_THICKNESS_THICK("24", 0, R.drawable.sel_edit_avatar_face_uplip_thickness, R.string.avatar_up_lip_thickness, AvatarFaceType.AVATAR_FACE_LIP),
    DOWN_LIP_THICKNESS_THICK("13", 0, R.drawable.sel_edit_avatar_face_downlip_thickness, R.string.avatar_down_lip_thickness, AvatarFaceType.AVATAR_FACE_LIP),
    LIP_WIDTH_OUT("12", 0, R.drawable.sel_edit_avatar_face_lip_width, R.string.avatar_lip_width, AvatarFaceType.AVATAR_FACE_LIP);

    private String name;
    private float level;
    private int iconId;
    private int descriptionId;
    private int type;

    AvatarFaceAspectCustomEnum(String name, float level, int iconId, int descriptionId, int type) {
        this.name = name;
        this.level = level;
        this.iconId = iconId;
        this.descriptionId = descriptionId;
        this.type = type;
    }

    public static List<AvatarFaceAspect> getAvatarAspectsByType(int type) {
        AvatarFaceAspectCustomEnum[] values = values();
        List<AvatarFaceAspect> avatarFaceAspects = new ArrayList<>(values.length);
        for (AvatarFaceAspectCustomEnum value : values) {
            if (value.type == type) {
                avatarFaceAspects.add(value.build());
            }
        }
        return avatarFaceAspects;
    }

    public AvatarFaceAspect build() {
        return new AvatarFaceAspect(type, name, level, iconId, descriptionId);
    }

}
