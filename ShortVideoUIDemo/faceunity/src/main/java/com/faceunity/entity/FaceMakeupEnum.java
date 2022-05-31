package com.faceunity.entity;

import androidx.core.util.Pair;
import android.util.SparseArray;

import com.faceunity.R;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LiuQiang on 2018.11.12
 * 口红用 JSON 表示，其他都是图片
 */
public enum FaceMakeupEnum {

    /**
     * 美妆项，前几项是预置的效果
     * 排在列表最前方，顺序为桃花妆、雀斑妆、朋克妆（其中朋克没有腮红，3个妆容的眼线、眼睫毛共用1个的）
     */
    MAKEUP_NONE("卸妆", "", FaceMakeup.FACE_MAKEUP_TYPE_NONE, R.drawable.makeup_none_normal, R.string.makeup_radio_remove, true),

    // 腮红
    MAKEUP_BLUSHER_01("MAKEUP_BLUSHER_01", "blusher/mu_blush_01.png", FaceMakeup.FACE_MAKEUP_TYPE_BLUSHER, R.drawable.demo_blush_01, R.string.makeup_radio_blusher, false),
    MAKEUP_BLUSHER_02("MAKEUP_BLUSHER_02", "blusher/mu_blush_02.png", FaceMakeup.FACE_MAKEUP_TYPE_BLUSHER, R.drawable.demo_blush_02, R.string.makeup_radio_blusher, false),
    MAKEUP_BLUSHER_03("MAKEUP_BLUSHER_03", "blusher/mu_blush_03.png", FaceMakeup.FACE_MAKEUP_TYPE_BLUSHER, R.drawable.demo_blush_03, R.string.makeup_radio_blusher, false),
    MAKEUP_BLUSHER_04("MAKEUP_BLUSHER_04", "blusher/mu_blush_04.png", FaceMakeup.FACE_MAKEUP_TYPE_BLUSHER, R.drawable.demo_blush_04, R.string.makeup_radio_blusher, false),
    MAKEUP_BLUSHER_05("MAKEUP_BLUSHER_05", "blusher/mu_blush_05.png", FaceMakeup.FACE_MAKEUP_TYPE_BLUSHER, R.drawable.demo_blush_05, R.string.makeup_radio_blusher, false),
    MAKEUP_BLUSHER_06("MAKEUP_BLUSHER_06", "blusher/mu_blush_06.png", FaceMakeup.FACE_MAKEUP_TYPE_BLUSHER, R.drawable.demo_blush_06, R.string.makeup_radio_blusher, false),
    MAKEUP_BLUSHER_07("MAKEUP_BLUSHER_07", "blusher/mu_blush_07.png", FaceMakeup.FACE_MAKEUP_TYPE_BLUSHER, R.drawable.demo_blush_07, R.string.makeup_radio_blusher, false),
    MAKEUP_BLUSHER_08("MAKEUP_BLUSHER_08", "blusher/mu_blush_08.png", FaceMakeup.FACE_MAKEUP_TYPE_BLUSHER, R.drawable.demo_blush_08, R.string.makeup_radio_blusher, false),
    MAKEUP_BLUSHER_09("MAKEUP_BLUSHER_09", "blusher/mu_blush_09.png", FaceMakeup.FACE_MAKEUP_TYPE_BLUSHER, R.drawable.demo_blush_09, R.string.makeup_radio_blusher, false),
    MAKEUP_BLUSHER_10("MAKEUP_BLUSHER_10", "blusher/mu_blush_10.png", FaceMakeup.FACE_MAKEUP_TYPE_BLUSHER, R.drawable.demo_blush_10, R.string.makeup_radio_blusher, false),
    MAKEUP_BLUSHER_11("MAKEUP_BLUSHER_11", "blusher/mu_blush_11.png", FaceMakeup.FACE_MAKEUP_TYPE_BLUSHER, R.drawable.demo_blush_11, R.string.makeup_radio_blusher, false),
    MAKEUP_BLUSHER_12("MAKEUP_BLUSHER_12", "blusher/mu_blush_12.png", FaceMakeup.FACE_MAKEUP_TYPE_BLUSHER, R.drawable.demo_blush_12, R.string.makeup_radio_blusher, false),
    MAKEUP_BLUSHER_13("MAKEUP_BLUSHER_13", "blusher/mu_blush_13.png", FaceMakeup.FACE_MAKEUP_TYPE_BLUSHER, R.drawable.demo_blush_13, R.string.makeup_radio_blusher, false),
    MAKEUP_BLUSHER_14("MAKEUP_BLUSHER_14", "blusher/mu_blush_14.png", FaceMakeup.FACE_MAKEUP_TYPE_BLUSHER, R.drawable.demo_blush_14, R.string.makeup_radio_blusher, false),
    MAKEUP_BLUSHER_15("MAKEUP_BLUSHER_15", "blusher/mu_blush_15.png", FaceMakeup.FACE_MAKEUP_TYPE_BLUSHER, R.drawable.demo_blush_15, R.string.makeup_radio_blusher, false),
    MAKEUP_BLUSHER_16("MAKEUP_BLUSHER_16", "blusher/mu_blush_16.png", FaceMakeup.FACE_MAKEUP_TYPE_BLUSHER, R.drawable.demo_blush_16, R.string.makeup_radio_blusher, false),
    MAKEUP_BLUSHER_17("MAKEUP_BLUSHER_17", "blusher/mu_blush_17.png", FaceMakeup.FACE_MAKEUP_TYPE_BLUSHER, R.drawable.demo_blush_17, R.string.makeup_radio_blusher, false),
    MAKEUP_BLUSHER_18("MAKEUP_BLUSHER_18", "blusher/mu_blush_18.png", FaceMakeup.FACE_MAKEUP_TYPE_BLUSHER, R.drawable.demo_blush_18, R.string.makeup_radio_blusher, true),
    MAKEUP_BLUSHER_19("MAKEUP_BLUSHER_19", "blusher/mu_blush_19.png", FaceMakeup.FACE_MAKEUP_TYPE_BLUSHER, R.drawable.demo_blush_19, R.string.makeup_radio_blusher, true),
    MAKEUP_BLUSHER_20("MAKEUP_BLUSHER_20", "blusher/mu_blush_20.png", FaceMakeup.FACE_MAKEUP_TYPE_BLUSHER, R.drawable.demo_blush_20, R.string.makeup_radio_blusher, false),
    MAKEUP_BLUSHER_21("MAKEUP_BLUSHER_21", "blusher/mu_blush_21.png", FaceMakeup.FACE_MAKEUP_TYPE_BLUSHER, R.drawable.demo_blush_21, R.string.makeup_radio_blusher, true),
    MAKEUP_BLUSHER_22("MAKEUP_BLUSHER_22", "blusher/mu_blush_22.png", FaceMakeup.FACE_MAKEUP_TYPE_BLUSHER, R.drawable.demo_blush_22, R.string.makeup_radio_blusher, false),
    MAKEUP_BLUSHER_23("MAKEUP_BLUSHER_23", "blusher/mu_blush_23.png", FaceMakeup.FACE_MAKEUP_TYPE_BLUSHER, R.drawable.demo_blush_23, R.string.makeup_radio_blusher, false),
    MAKEUP_BLUSHER_24("MAKEUP_BLUSHER_24", "blusher/mu_blush_24.png", FaceMakeup.FACE_MAKEUP_TYPE_BLUSHER, R.drawable.demo_blush_24, R.string.makeup_radio_blusher, false),
    // 眉毛
    MAKEUP_EYEBROW_01("MAKEUP_EYEBROW_01", "eyebrow/mu_eyebrow_01.png", FaceMakeup.FACE_MAKEUP_TYPE_EYEBROW, R.drawable.demo_eyebrow_01, R.string.makeup_radio_eyebrow, false),
    MAKEUP_EYEBROW_02("MAKEUP_EYEBROW_02", "eyebrow/mu_eyebrow_02.png", FaceMakeup.FACE_MAKEUP_TYPE_EYEBROW, R.drawable.demo_eyebrow_02, R.string.makeup_radio_eyebrow, false),
    MAKEUP_EYEBROW_03("MAKEUP_EYEBROW_03", "eyebrow/mu_eyebrow_03.png", FaceMakeup.FACE_MAKEUP_TYPE_EYEBROW, R.drawable.demo_eyebrow_03, R.string.makeup_radio_eyebrow, false),
    MAKEUP_EYEBROW_04("MAKEUP_EYEBROW_04", "eyebrow/mu_eyebrow_04.png", FaceMakeup.FACE_MAKEUP_TYPE_EYEBROW, R.drawable.demo_eyebrow_04, R.string.makeup_radio_eyebrow, false),
    MAKEUP_EYEBROW_05("MAKEUP_EYEBROW_05", "eyebrow/mu_eyebrow_05.png", FaceMakeup.FACE_MAKEUP_TYPE_EYEBROW, R.drawable.demo_eyebrow_05, R.string.makeup_radio_eyebrow, false),
    MAKEUP_EYEBROW_06("MAKEUP_EYEBROW_06", "eyebrow/mu_eyebrow_06.png", FaceMakeup.FACE_MAKEUP_TYPE_EYEBROW, R.drawable.demo_eyebrow_06, R.string.makeup_radio_eyebrow, false),
    MAKEUP_EYEBROW_07("MAKEUP_EYEBROW_07", "eyebrow/mu_eyebrow_07.png", FaceMakeup.FACE_MAKEUP_TYPE_EYEBROW, R.drawable.demo_eyebrow_07, R.string.makeup_radio_eyebrow, false),
    MAKEUP_EYEBROW_08("MAKEUP_EYEBROW_08", "eyebrow/mu_eyebrow_08.png", FaceMakeup.FACE_MAKEUP_TYPE_EYEBROW, R.drawable.demo_eyebrow_08, R.string.makeup_radio_eyebrow, false),
    MAKEUP_EYEBROW_09("MAKEUP_EYEBROW_09", "eyebrow/mu_eyebrow_09.png", FaceMakeup.FACE_MAKEUP_TYPE_EYEBROW, R.drawable.demo_eyebrow_09, R.string.makeup_radio_eyebrow, false),
    MAKEUP_EYEBROW_10("MAKEUP_EYEBROW_10", "eyebrow/mu_eyebrow_10.png", FaceMakeup.FACE_MAKEUP_TYPE_EYEBROW, R.drawable.demo_eyebrow_10, R.string.makeup_radio_eyebrow, true),
    MAKEUP_EYEBROW_11("MAKEUP_EYEBROW_11", "eyebrow/mu_eyebrow_11.png", FaceMakeup.FACE_MAKEUP_TYPE_EYEBROW, R.drawable.demo_eyebrow_11, R.string.makeup_radio_eyebrow, false),
    MAKEUP_EYEBROW_12("MAKEUP_EYEBROW_12", "eyebrow/mu_eyebrow_12.png", FaceMakeup.FACE_MAKEUP_TYPE_EYEBROW, R.drawable.demo_eyebrow_12, R.string.makeup_radio_eyebrow, true),
    MAKEUP_EYEBROW_13("MAKEUP_EYEBROW_13", "eyebrow/mu_eyebrow_13.png", FaceMakeup.FACE_MAKEUP_TYPE_EYEBROW, R.drawable.demo_eyebrow_13, R.string.makeup_radio_eyebrow, false),
    MAKEUP_EYEBROW_14("MAKEUP_EYEBROW_14", "eyebrow/mu_eyebrow_14.png", FaceMakeup.FACE_MAKEUP_TYPE_EYEBROW, R.drawable.demo_eyebrow_14, R.string.makeup_radio_eyebrow, false),
    MAKEUP_EYEBROW_15("MAKEUP_EYEBROW_15", "eyebrow/mu_eyebrow_15.png", FaceMakeup.FACE_MAKEUP_TYPE_EYEBROW, R.drawable.demo_eyebrow_15, R.string.makeup_radio_eyebrow, false),
    MAKEUP_EYEBROW_16("MAKEUP_EYEBROW_16", "eyebrow/mu_eyebrow_16.png", FaceMakeup.FACE_MAKEUP_TYPE_EYEBROW, R.drawable.demo_eyebrow_16, R.string.makeup_radio_eyebrow, false),
    MAKEUP_EYEBROW_17("MAKEUP_EYEBROW_17", "eyebrow/mu_eyebrow_17.png", FaceMakeup.FACE_MAKEUP_TYPE_EYEBROW, R.drawable.demo_eyebrow_17, R.string.makeup_radio_eyebrow, true),
    MAKEUP_EYEBROW_18("MAKEUP_EYEBROW_18", "eyebrow/mu_eyebrow_18.png", FaceMakeup.FACE_MAKEUP_TYPE_EYEBROW, R.drawable.demo_eyebrow_18, R.string.makeup_radio_eyebrow, false),
    MAKEUP_EYEBROW_19("MAKEUP_EYEBROW_19", "eyebrow/mu_eyebrow_19.png", FaceMakeup.FACE_MAKEUP_TYPE_EYEBROW, R.drawable.demo_eyebrow_19, R.string.makeup_radio_eyebrow, false),

    // 睫毛
    MAKEUP_EYELASH_01("MAKEUP_EYELASH_01", "eyelash/mu_eyelash_01.png", FaceMakeup.FACE_MAKEUP_TYPE_EYELASH, R.drawable.demo_eyelash_01, R.string.makeup_radio_eyelash, false),
    MAKEUP_EYELASH_02("MAKEUP_EYELASH_02", "eyelash/mu_eyelash_02.png", FaceMakeup.FACE_MAKEUP_TYPE_EYELASH, R.drawable.demo_eyelash_02, R.string.makeup_radio_eyelash, false),
    MAKEUP_EYELASH_03("MAKEUP_EYELASH_03", "eyelash/mu_eyelash_03.png", FaceMakeup.FACE_MAKEUP_TYPE_EYELASH, R.drawable.demo_eyelash_03, R.string.makeup_radio_eyelash, false),
    MAKEUP_EYELASH_04("MAKEUP_EYELASH_04", "eyelash/mu_eyelash_04.png", FaceMakeup.FACE_MAKEUP_TYPE_EYELASH, R.drawable.demo_eyelash_04, R.string.makeup_radio_eyelash, false),
    MAKEUP_EYELASH_05("MAKEUP_EYELASH_05", "eyelash/mu_eyelash_05.png", FaceMakeup.FACE_MAKEUP_TYPE_EYELASH, R.drawable.demo_eyelash_05, R.string.makeup_radio_eyelash, false),
    MAKEUP_EYELASH_06("MAKEUP_EYELASH_06", "eyelash/mu_eyelash_06.png", FaceMakeup.FACE_MAKEUP_TYPE_EYELASH, R.drawable.demo_eyelash_06, R.string.makeup_radio_eyelash, false),
    MAKEUP_EYELASH_07("MAKEUP_EYELASH_07", "eyelash/mu_eyelash_07.png", FaceMakeup.FACE_MAKEUP_TYPE_EYELASH, R.drawable.demo_eyelash_07, R.string.makeup_radio_eyelash, false),
    MAKEUP_EYELASH_08("MAKEUP_EYELASH_08", "eyelash/mu_eyelash_08.png", FaceMakeup.FACE_MAKEUP_TYPE_EYELASH, R.drawable.demo_eyelash_08, R.string.makeup_radio_eyelash, false),

    // 眼线
    MAKEUP_EYELINER_01("MAKEUP_EYELINER_01", "eyeliner/mu_eyeliner_01.png", FaceMakeup.FACE_MAKEUP_TYPE_EYE_LINER, R.drawable.demo_eyeliner_01, R.string.makeup_radio_eye_liner, false),
    MAKEUP_EYELINER_02("MAKEUP_EYELINER_02", "eyeliner/mu_eyeliner_02.png", FaceMakeup.FACE_MAKEUP_TYPE_EYE_LINER, R.drawable.demo_eyeliner_02, R.string.makeup_radio_eye_liner, false),
    MAKEUP_EYELINER_03("MAKEUP_EYELINER_03", "eyeliner/mu_eyeliner_03.png", FaceMakeup.FACE_MAKEUP_TYPE_EYE_LINER, R.drawable.demo_eyeliner_03, R.string.makeup_radio_eye_liner, false),
    MAKEUP_EYELINER_04("MAKEUP_EYELINER_04", "eyeliner/mu_eyeliner_04.png", FaceMakeup.FACE_MAKEUP_TYPE_EYE_LINER, R.drawable.demo_eyeliner_04, R.string.makeup_radio_eye_liner, false),
    MAKEUP_EYELINER_05("MAKEUP_EYELINER_05", "eyeliner/mu_eyeliner_05.png", FaceMakeup.FACE_MAKEUP_TYPE_EYE_LINER, R.drawable.demo_eyeliner_05, R.string.makeup_radio_eye_liner, false),
    MAKEUP_EYELINER_06("MAKEUP_EYELINER_06", "eyeliner/mu_eyeliner_06.png", FaceMakeup.FACE_MAKEUP_TYPE_EYE_LINER, R.drawable.demo_eyeliner_06, R.string.makeup_radio_eye_liner, false),
    MAKEUP_EYELINER_07("MAKEUP_EYELINER_07", "eyeliner/mu_eyeliner_07.png", FaceMakeup.FACE_MAKEUP_TYPE_EYE_LINER, R.drawable.demo_eyeliner_07, R.string.makeup_radio_eye_liner, false),
    MAKEUP_EYELINER_08("MAKEUP_EYELINER_08", "eyeliner/mu_eyeliner_08.png", FaceMakeup.FACE_MAKEUP_TYPE_EYE_LINER, R.drawable.demo_eyeliner_08, R.string.makeup_radio_eye_liner, false),

    // 美瞳
    MAKEUP_EYEPUPIL_01("MAKEUP_EYEPUPIL_01", "eyepupil/mu_eyepupil_01.png", FaceMakeup.FACE_MAKEUP_TYPE_EYE_PUPIL, R.drawable.demo_eyepupil_01, R.string.makeup_radio_contact_lens, false),
    MAKEUP_EYEPUPIL_02("MAKEUP_EYEPUPIL_02", "eyepupil/mu_eyepupil_02.png", FaceMakeup.FACE_MAKEUP_TYPE_EYE_PUPIL, R.drawable.demo_eyepupil_02, R.string.makeup_radio_contact_lens, false),
    MAKEUP_EYEPUPIL_03("MAKEUP_EYEPUPIL_03", "eyepupil/mu_eyepupil_03.png", FaceMakeup.FACE_MAKEUP_TYPE_EYE_PUPIL, R.drawable.demo_eyepupil_03, R.string.makeup_radio_contact_lens, false),
    MAKEUP_EYEPUPIL_04("MAKEUP_EYEPUPIL_04", "eyepupil/mu_eyepupil_04.png", FaceMakeup.FACE_MAKEUP_TYPE_EYE_PUPIL, R.drawable.demo_eyepupil_04, R.string.makeup_radio_contact_lens, false),
    MAKEUP_EYEPUPIL_05("MAKEUP_EYEPUPIL_05", "eyepupil/mu_eyepupil_05.png", FaceMakeup.FACE_MAKEUP_TYPE_EYE_PUPIL, R.drawable.demo_eyepupil_05, R.string.makeup_radio_contact_lens, false),
    MAKEUP_EYEPUPIL_06("MAKEUP_EYEPUPIL_06", "eyepupil/mu_eyepupil_06.png", FaceMakeup.FACE_MAKEUP_TYPE_EYE_PUPIL, R.drawable.demo_eyepupil_06, R.string.makeup_radio_contact_lens, false),
    MAKEUP_EYEPUPIL_07("MAKEUP_EYEPUPIL_07", "eyepupil/mu_eyepupil_07.png", FaceMakeup.FACE_MAKEUP_TYPE_EYE_PUPIL, R.drawable.demo_eyepupil_07, R.string.makeup_radio_contact_lens, false),
    MAKEUP_EYEPUPIL_08("MAKEUP_EYEPUPIL_08", "eyepupil/mu_eyepupil_08.png", FaceMakeup.FACE_MAKEUP_TYPE_EYE_PUPIL, R.drawable.demo_eyepupil_08, R.string.makeup_radio_contact_lens, false),
    MAKEUP_EYEPUPIL_09("MAKEUP_EYEPUPIL_09", "eyepupil/mu_eyepupil_09.png", FaceMakeup.FACE_MAKEUP_TYPE_EYE_PUPIL, R.drawable.demo_eyepupil_09, R.string.makeup_radio_contact_lens, false),

    // 眼影
    MAKEUP_EYE_SHADOW_01("MAKEUP_EYESHADOW_01", "eyeshadow/mu_eyeshadow_01.png", FaceMakeup.FACE_MAKEUP_TYPE_EYE_SHADOW, R.drawable.demo_eyeshadow_01, R.string.makeup_radio_eye_shadow, false),
    MAKEUP_EYE_SHADOW_02("MAKEUP_EYESHADOW_02", "eyeshadow/mu_eyeshadow_02.png", FaceMakeup.FACE_MAKEUP_TYPE_EYE_SHADOW, R.drawable.demo_eyeshadow_02, R.string.makeup_radio_eye_shadow, false),
    MAKEUP_EYE_SHADOW_03("MAKEUP_EYESHADOW_03", "eyeshadow/mu_eyeshadow_03.png", FaceMakeup.FACE_MAKEUP_TYPE_EYE_SHADOW, R.drawable.demo_eyeshadow_03, R.string.makeup_radio_eye_shadow, false),
    MAKEUP_EYE_SHADOW_04("MAKEUP_EYESHADOW_04", "eyeshadow/mu_eyeshadow_04.png", FaceMakeup.FACE_MAKEUP_TYPE_EYE_SHADOW, R.drawable.demo_eyeshadow_04, R.string.makeup_radio_eye_shadow, false),
    MAKEUP_EYE_SHADOW_05("MAKEUP_EYESHADOW_05", "eyeshadow/mu_eyeshadow_05.png", FaceMakeup.FACE_MAKEUP_TYPE_EYE_SHADOW, R.drawable.demo_eyeshadow_05, R.string.makeup_radio_eye_shadow, false),
    MAKEUP_EYE_SHADOW_06("MAKEUP_EYESHADOW_06", "eyeshadow/mu_eyeshadow_06.png", FaceMakeup.FACE_MAKEUP_TYPE_EYE_SHADOW, R.drawable.demo_eyeshadow_06, R.string.makeup_radio_eye_shadow, false),
    MAKEUP_EYE_SHADOW_07("MAKEUP_EYESHADOW_07", "eyeshadow/mu_eyeshadow_07.png", FaceMakeup.FACE_MAKEUP_TYPE_EYE_SHADOW, R.drawable.demo_eyeshadow_07, R.string.makeup_radio_eye_shadow, false),
    MAKEUP_EYE_SHADOW_08("MAKEUP_EYESHADOW_08", "eyeshadow/mu_eyeshadow_08.png", FaceMakeup.FACE_MAKEUP_TYPE_EYE_SHADOW, R.drawable.demo_eyeshadow_08, R.string.makeup_radio_eye_shadow, false),
    MAKEUP_EYE_SHADOW_09("MAKEUP_EYESHADOW_09", "eyeshadow/mu_eyeshadow_09.png", FaceMakeup.FACE_MAKEUP_TYPE_EYE_SHADOW, R.drawable.demo_eyeshadow_09, R.string.makeup_radio_eye_shadow, false),
    MAKEUP_EYE_SHADOW_10("MAKEUP_EYESHADOW_10", "eyeshadow/mu_eyeshadow_10.png", FaceMakeup.FACE_MAKEUP_TYPE_EYE_SHADOW, R.drawable.demo_eyeshadow_10, R.string.makeup_radio_eye_shadow, false),
    MAKEUP_EYE_SHADOW_11("MAKEUP_EYESHADOW_11", "eyeshadow/mu_eyeshadow_11.png", FaceMakeup.FACE_MAKEUP_TYPE_EYE_SHADOW, R.drawable.demo_eyeshadow_11, R.string.makeup_radio_eye_shadow, false),
    MAKEUP_EYE_SHADOW_12("MAKEUP_EYESHADOW_12", "eyeshadow/mu_eyeshadow_12.png", FaceMakeup.FACE_MAKEUP_TYPE_EYE_SHADOW, R.drawable.demo_eyeshadow_12, R.string.makeup_radio_eye_shadow, false),
    MAKEUP_EYE_SHADOW_13("MAKEUP_EYESHADOW_13", "eyeshadow/mu_eyeshadow_13.png", FaceMakeup.FACE_MAKEUP_TYPE_EYE_SHADOW, R.drawable.demo_eyeshadow_13, R.string.makeup_radio_eye_shadow, false),
    MAKEUP_EYE_SHADOW_14("MAKEUP_EYESHADOW_14", "eyeshadow/mu_eyeshadow_14.png", FaceMakeup.FACE_MAKEUP_TYPE_EYE_SHADOW, R.drawable.demo_eyeshadow_14, R.string.makeup_radio_eye_shadow, false),
    MAKEUP_EYE_SHADOW_15("MAKEUP_EYESHADOW_15", "eyeshadow/mu_eyeshadow_15.png", FaceMakeup.FACE_MAKEUP_TYPE_EYE_SHADOW, R.drawable.demo_eyeshadow_15, R.string.makeup_radio_eye_shadow, false),
    MAKEUP_EYE_SHADOW_16("MAKEUP_EYESHADOW_16", "eyeshadow/mu_eyeshadow_16.png", FaceMakeup.FACE_MAKEUP_TYPE_EYE_SHADOW, R.drawable.demo_eyeshadow_16, R.string.makeup_radio_eye_shadow, true),
    MAKEUP_EYE_SHADOW_17("MAKEUP_EYESHADOW_17", "eyeshadow/mu_eyeshadow_17.png", FaceMakeup.FACE_MAKEUP_TYPE_EYE_SHADOW, R.drawable.demo_eyeshadow_17, R.string.makeup_radio_eye_shadow, true),
    MAKEUP_EYE_SHADOW_18("MAKEUP_EYESHADOW_18", "eyeshadow/mu_eyeshadow_18.png", FaceMakeup.FACE_MAKEUP_TYPE_EYE_SHADOW, R.drawable.demo_eyeshadow_18, R.string.makeup_radio_eye_shadow, false),
    MAKEUP_EYE_SHADOW_19("MAKEUP_EYESHADOW_19", "eyeshadow/mu_eyeshadow_19.png", FaceMakeup.FACE_MAKEUP_TYPE_EYE_SHADOW, R.drawable.demo_eyeshadow_19, R.string.makeup_radio_eye_shadow, true),
    MAKEUP_EYE_SHADOW_20("MAKEUP_EYESHADOW_20", "eyeshadow/mu_eyeshadow_20.png", FaceMakeup.FACE_MAKEUP_TYPE_EYE_SHADOW, R.drawable.demo_eyeshadow_20, R.string.makeup_radio_eye_shadow, false),
    MAKEUP_EYE_SHADOW_21("MAKEUP_EYESHADOW_21", "eyeshadow/mu_eyeshadow_21.png", FaceMakeup.FACE_MAKEUP_TYPE_EYE_SHADOW, R.drawable.demo_eyeshadow_21, R.string.makeup_radio_eye_shadow, false),
    MAKEUP_EYE_SHADOW_22("MAKEUP_EYESHADOW_22", "eyeshadow/mu_eyeshadow_22.png", FaceMakeup.FACE_MAKEUP_TYPE_EYE_SHADOW, R.drawable.demo_eyeshadow_22, R.string.makeup_radio_eye_shadow, false),

    // 口红
    MAKEUP_LIPSTICK_01("MAKEUP_LIPSTICK_01", "lipstick/mu_lip_01.json", FaceMakeup.FACE_MAKEUP_TYPE_LIPSTICK, R.drawable.demo_lip_01, R.string.makeup_radio_lipstick, false),
    MAKEUP_LIPSTICK_02("MAKEUP_LIPSTICK_02", "lipstick/mu_lip_02.json", FaceMakeup.FACE_MAKEUP_TYPE_LIPSTICK, R.drawable.demo_lip_02, R.string.makeup_radio_lipstick, false),
    MAKEUP_LIPSTICK_03("MAKEUP_LIPSTICK_03", "lipstick/mu_lip_03.json", FaceMakeup.FACE_MAKEUP_TYPE_LIPSTICK, R.drawable.demo_lip_03, R.string.makeup_radio_lipstick, false),
    MAKEUP_LIPSTICK_10("MAKEUP_LIPSTICK_10", "lipstick/mu_lip_10.json", FaceMakeup.FACE_MAKEUP_TYPE_LIPSTICK, R.drawable.demo_lip_10, R.string.makeup_radio_lipstick, false),
    MAKEUP_LIPSTICK_11("MAKEUP_LIPSTICK_11", "lipstick/mu_lip_11.json", FaceMakeup.FACE_MAKEUP_TYPE_LIPSTICK, R.drawable.demo_lip_12, R.string.makeup_radio_lipstick, false),
    MAKEUP_LIPSTICK_12("MAKEUP_LIPSTICK_12", "lipstick/mu_lip_12.json", FaceMakeup.FACE_MAKEUP_TYPE_LIPSTICK, R.drawable.demo_lip_12, R.string.makeup_radio_lipstick, false),
    MAKEUP_LIPSTICK_13("MAKEUP_LIPSTICK_13", "lipstick/mu_lip_13.json", FaceMakeup.FACE_MAKEUP_TYPE_LIPSTICK, R.drawable.demo_lip_13, R.string.makeup_radio_lipstick, false),
    MAKEUP_LIPSTICK_14("MAKEUP_LIPSTICK_14", "lipstick/mu_lip_14.json", FaceMakeup.FACE_MAKEUP_TYPE_LIPSTICK, R.drawable.demo_lip_14, R.string.makeup_radio_lipstick, false),
    MAKEUP_LIPSTICK_15("MAKEUP_LIPSTICK_15", "lipstick/mu_lip_15.json", FaceMakeup.FACE_MAKEUP_TYPE_LIPSTICK, R.drawable.demo_lip_15, R.string.makeup_radio_lipstick, false),
    MAKEUP_LIPSTICK_16("MAKEUP_LIPSTICK_16", "lipstick/mu_lip_16.json", FaceMakeup.FACE_MAKEUP_TYPE_LIPSTICK, R.drawable.demo_lip_16, R.string.makeup_radio_lipstick, true),
    MAKEUP_LIPSTICK_17("MAKEUP_LIPSTICK_17", "lipstick/mu_lip_17.json", FaceMakeup.FACE_MAKEUP_TYPE_LIPSTICK, R.drawable.demo_lip_17, R.string.makeup_radio_lipstick, true),
    MAKEUP_LIPSTICK_18("MAKEUP_LIPSTICK_18", "lipstick/mu_lip_18.json", FaceMakeup.FACE_MAKEUP_TYPE_LIPSTICK, R.drawable.demo_lip_18, R.string.makeup_radio_lipstick, false),
    MAKEUP_LIPSTICK_19("MAKEUP_LIPSTICK_19", "lipstick/mu_lip_19.json", FaceMakeup.FACE_MAKEUP_TYPE_LIPSTICK, R.drawable.demo_lip_19, R.string.makeup_radio_lipstick, true),
    MAKEUP_LIPSTICK_20("MAKEUP_LIPSTICK_20", "lipstick/mu_lip_20.json", FaceMakeup.FACE_MAKEUP_TYPE_LIPSTICK, R.drawable.demo_lip_20, R.string.makeup_radio_lipstick, false),
    MAKEUP_LIPSTICK_21("MAKEUP_LIPSTICK_21", "lipstick/mu_lip_21.json", FaceMakeup.FACE_MAKEUP_TYPE_LIPSTICK, R.drawable.demo_lip_21, R.string.makeup_radio_lipstick, false),
    MAKEUP_LIPSTICK_22("MAKEUP_LIPSTICK_22", "lipstick/mu_lip_22.json", FaceMakeup.FACE_MAKEUP_TYPE_LIPSTICK, R.drawable.demo_lip_22, R.string.makeup_radio_lipstick, false);

    // 每个妆容默认强度 0.7，对应至尊美颜效果
    public static final float DEFAULT_BATCH_MAKEUP_LEVEL = 0.7f;

    private String name;
    private String path;
    private int type;
    private int iconId;
    private int strId;
    /**
     * 妆容组合 整体强度
     */
    public final static SparseArray<Float> MAKEUP_OVERALL_LEVEL = new SparseArray<>();
    /**
     * http://confluence.faceunity.com/pages/viewpage.action?pageId=20332259
     * 妆容和滤镜的组合
     */
    public static final SparseArray<Pair<Filter, Float>> MAKEUP_FILTERS = new SparseArray<>(16);

    /**
     * 根据类型查询美妆
     */
    public static List<MakeupItem> getFaceMakeupByType(int type) {
        FaceMakeupEnum[] values = values();
        List<MakeupItem> makeups = new ArrayList<>(16);
        MakeupItem none = MAKEUP_NONE.faceMakeup();
        none.setType(type);
        makeups.add(none);
        for (FaceMakeupEnum value : values) {
            if (value.showInMakeup && value.type == type) {
                makeups.add(value.faceMakeup());
            }
        }
        return makeups;
    }

    static {
        MAKEUP_OVERALL_LEVEL.put(R.string.makeup_peach_blossom, 0.9f);
        MAKEUP_OVERALL_LEVEL.put(R.string.makeup_grapefruit, 1.0f);
        MAKEUP_OVERALL_LEVEL.put(R.string.makeup_clear, 0.9f);
        MAKEUP_OVERALL_LEVEL.put(R.string.makeup_boyfriend, 1.0f);
        MAKEUP_OVERALL_LEVEL.put(R.string.makeup_red_tea, 1.0f);
        MAKEUP_OVERALL_LEVEL.put(R.string.makeup_winter, 0.9f);
        MAKEUP_OVERALL_LEVEL.put(R.string.makeup_cream, 1.0f);

        /*
        MAKEUP_OVERALL_LEVEL.put(R.string.makeup_punk, 0.85f);
        MAKEUP_OVERALL_LEVEL.put(R.string.makeup_maple_leaf, 1.0f);
        MAKEUP_OVERALL_LEVEL.put(R.string.makeup_brocade_carp, 0.9f);
        MAKEUP_OVERALL_LEVEL.put(R.string.makeup_plum, 0.85f);
        MAKEUP_OVERALL_LEVEL.put(R.string.makeup_tipsy, 1.0f);
        MAKEUP_OVERALL_LEVEL.put(R.string.makeup_classical, 1.0f);
        MAKEUP_OVERALL_LEVEL.put(R.string.makeup_disgusting, 1.0f);
        MAKEUP_OVERALL_LEVEL.put(R.string.makeup_black_white, 1.0f);
        */
    }

    // 桃花、西柚、清透、男友, 赤茶妆、冬日妆、奶油妆，
    static {
        MAKEUP_FILTERS.put(R.string.makeup_peach_blossom, Pair.create(Filter.create(Filter.Key.FENNEN_3), 1.0f));
        MAKEUP_FILTERS.put(R.string.makeup_grapefruit, Pair.create(Filter.create(Filter.Key.LENGSEDIAO_4), 0.7f));
        MAKEUP_FILTERS.put(R.string.makeup_clear, Pair.create(Filter.create(Filter.Key.XIAOQINGXIN_1), 0.8f));
        MAKEUP_FILTERS.put(R.string.makeup_boyfriend, Pair.create(Filter.create(Filter.Key.XIAOQINGXIN_3), 0.9f));
        MAKEUP_FILTERS.put(R.string.makeup_red_tea, Pair.create(Filter.create(Filter.Key.XIAOQINGXIN_2), 0.75f));
        MAKEUP_FILTERS.put(R.string.makeup_winter, Pair.create(Filter.create(Filter.Key.NUANSEDIAO_1), 0.8f));
        MAKEUP_FILTERS.put(R.string.makeup_cream, Pair.create(Filter.create(Filter.Key.BAILIANG_1), 0.75f));
//        MAKEUP_FILTERS.put(R.string.makeup_punk, Pair.create(FilterEnum.dry.filter(), 0.5f));
//        MAKEUP_FILTERS.put(R.string.makeup_maple_leaf, Pair.create(FilterEnum.delta.filter(), 0.8f));
//        MAKEUP_FILTERS.put(R.string.makeup_brocade_carp, Pair.create(FilterEnum.linjia.filter(), 0.7f));
//        MAKEUP_FILTERS.put(R.string.makeup_classical, Pair.create(FilterEnum.hongkong.filter(), 0.85f));
//        MAKEUP_FILTERS.put(R.string.makeup_plum, Pair.create(FilterEnum.red_tea.filter(), 0.8f));
//        MAKEUP_FILTERS.put(R.string.makeup_tipsy, Pair.create(FilterEnum.hongrun.filter(), 0.55f));
//        MAKEUP_FILTERS.put(R.string.makeup_freckles, Pair.create(FilterEnum.warm.filter(), 0.4f));
    }

    private boolean showInMakeup;

    FaceMakeupEnum(String name, String path, int type, int iconId, int strId, boolean showInMakeup) {
        this.name = name;
        this.path = path;
        this.type = type;
        this.iconId = iconId;
        this.strId = strId;
        this.showInMakeup = showInMakeup;
    }

    /**
     * 美颜模块的美妆组合 资源和顺序为：桃花、西柚、清透、男友
     *
     * @return
     */
    public static List<FaceMakeup> getBeautyFaceMakeup() {
        List<FaceMakeup> faceMakeups = new ArrayList<>();
        FaceMakeup none = new FaceMakeup(null, R.string.makeup_radio_remove, R.drawable.makeup_none_normal);
        faceMakeups.add(none);

        // 桃花
        List<MakeupItem> peachBlossomMakeups = new ArrayList<>(8);
        peachBlossomMakeups.add(MAKEUP_BLUSHER_01.faceMakeup(0.9f));
        peachBlossomMakeups.add(MAKEUP_EYE_SHADOW_01.faceMakeup(0.9f));
        peachBlossomMakeups.add(MAKEUP_EYEBROW_01.faceMakeup(0.5f));
        peachBlossomMakeups.add(MAKEUP_LIPSTICK_01.faceMakeup(0.9f));
        FaceMakeup peachBlossom = new FaceMakeup(peachBlossomMakeups, R.string.makeup_peach_blossom, R.drawable.demo_makeup_peachblossom);
        faceMakeups.add(peachBlossom);

        // 西柚
        List<MakeupItem> grapeMakeups = new ArrayList<>(4);
        grapeMakeups.add(MAKEUP_BLUSHER_23.faceMakeup(1.0f));
        grapeMakeups.add(MAKEUP_EYE_SHADOW_21.faceMakeup(0.75f));
        grapeMakeups.add(MAKEUP_EYEBROW_19.faceMakeup(0.6f));
        grapeMakeups.add(MAKEUP_LIPSTICK_21.faceMakeup(0.8f));
        FaceMakeup grape = new FaceMakeup(grapeMakeups, R.string.makeup_grapefruit, R.drawable.demo_makeup_grapefruit);
        faceMakeups.add(grape);

        // 清透
        List<MakeupItem> clearMakeups = new ArrayList<>(4);
        clearMakeups.add(MAKEUP_BLUSHER_22.faceMakeup(0.9f));
        clearMakeups.add(MAKEUP_EYE_SHADOW_20.faceMakeup(0.65f));
        clearMakeups.add(MAKEUP_EYEBROW_18.faceMakeup(0.45f));
        clearMakeups.add(MAKEUP_LIPSTICK_20.faceMakeup(0.8f));
        FaceMakeup clear = new FaceMakeup(clearMakeups, R.string.makeup_clear, R.drawable.demo_makeup_clear);
        faceMakeups.add(clear);

        // 男友
        List<MakeupItem> boyFriendMakeups = new ArrayList<>(4);
        boyFriendMakeups.add(MAKEUP_BLUSHER_20.faceMakeup(0.8f));
        boyFriendMakeups.add(MAKEUP_EYE_SHADOW_18.faceMakeup(0.9f));
        boyFriendMakeups.add(MAKEUP_EYEBROW_16.faceMakeup(0.65f));
        boyFriendMakeups.add(MAKEUP_LIPSTICK_18.faceMakeup(1.0f));
        FaceMakeup boyFriend = new FaceMakeup(boyFriendMakeups, R.string.makeup_boyfriend, R.drawable.demo_makeup_boyfriend);
        faceMakeups.add(boyFriend);
        return faceMakeups;
    }

    /**
     * 预置的美妆
     *
     * @return
     */
    public static List<FaceMakeup> getDefaultMakeups() {
        List<FaceMakeup> faceMakeups = new ArrayList<>();
        FaceMakeup none = new FaceMakeup(null, R.string.makeup_radio_remove, R.drawable.makeup_none_normal);
        faceMakeups.add(none);

/*
        // 朋克
        float punkLevel = MAKEUP_OVERALL_LEVEL.get(R.string.makeup_punk);
        List<MakeupItem> punkMakeups = new ArrayList<>(4);
        punkMakeups.add(MAKEUP_EYE_SHADOW_03.faceMakeup(punkLevel));
        punkMakeups.add(MAKEUP_EYEBROW_03.faceMakeup(0.5f));
        punkMakeups.add(MAKEUP_LIPSTICK_03.faceMakeup(punkLevel));
        FaceMakeup punk = new FaceMakeup(punkMakeups, R.string.makeup_punk, R.drawable.demo_makeup_punk);
        faceMakeups.add(punk);

        // 枫叶
        float mapleLevel = MAKEUP_OVERALL_LEVEL.get(R.string.makeup_maple_leaf);
        List<MakeupItem> mapleLeafMakeups = new ArrayList<>(4);
        mapleLeafMakeups.add(MAKEUP_BLUSHER_13.faceMakeup(mapleLevel));
        mapleLeafMakeups.add(MAKEUP_EYE_SHADOW_10.faceMakeup(mapleLevel));
        mapleLeafMakeups.add(MAKEUP_EYEBROW_10.faceMakeup(0.5f));
        mapleLeafMakeups.add(MAKEUP_LIPSTICK_10.faceMakeup(mapleLevel));
        FaceMakeup mapleLeaf = new FaceMakeup(mapleLeafMakeups, R.string.makeup_maple_leaf, R.drawable.demo_makeup_maple_leaves);
        faceMakeups.add(mapleLeaf);

        // 复古
        float classicalLevel = MAKEUP_OVERALL_LEVEL.get(R.string.makeup_classical);
        List<MakeupItem> classicalMakeups = new ArrayList<>(4);
        classicalMakeups.add(MAKEUP_BLUSHER_14.faceMakeup(classicalLevel));
        classicalMakeups.add(MAKEUP_EYE_SHADOW_11.faceMakeup(classicalLevel));
        classicalMakeups.add(MAKEUP_EYEBROW_11.faceMakeup(0.5f));
        classicalMakeups.add(MAKEUP_LIPSTICK_11.faceMakeup(classicalLevel));
        FaceMakeup classical = new FaceMakeup(classicalMakeups, R.string.makeup_classical, R.drawable.demo_makeup_classical);
        faceMakeups.add(classical);

        // 锦鲤
        float brocadeCarpLevel = MAKEUP_OVERALL_LEVEL.get(R.string.makeup_brocade_carp);
        List<MakeupItem> brocadeCarpMakeups = new ArrayList<>(4);
        brocadeCarpMakeups.add(MAKEUP_BLUSHER_15.faceMakeup(brocadeCarpLevel));
        brocadeCarpMakeups.add(MAKEUP_EYE_SHADOW_12.faceMakeup(brocadeCarpLevel));
        brocadeCarpMakeups.add(MAKEUP_EYEBROW_12.faceMakeup(0.5f));
        brocadeCarpMakeups.add(MAKEUP_LIPSTICK_12.faceMakeup(brocadeCarpLevel));
        FaceMakeup brocadeCarp = new FaceMakeup(brocadeCarpMakeups, R.string.makeup_brocade_carp, R.drawable.demo_makeup_brocade_carp);
        faceMakeups.add(brocadeCarp);

        // 梅子
        float plumLevel = MAKEUP_OVERALL_LEVEL.get(R.string.makeup_plum);
        List<MakeupItem> plumMakeups = new ArrayList<>(4);
        plumMakeups.add(MAKEUP_BLUSHER_16.faceMakeup(plumLevel));
        plumMakeups.add(MAKEUP_EYE_SHADOW_13.faceMakeup(plumLevel));
        plumMakeups.add(MAKEUP_EYEBROW_13.faceMakeup(0.5f));
        plumMakeups.add(MAKEUP_LIPSTICK_13.faceMakeup(plumLevel));
        FaceMakeup plum = new FaceMakeup(plumMakeups, R.string.makeup_plum, R.drawable.demo_makeup_plum);
        faceMakeups.add(plum);

        // 宿醉
        float tipsyLevel = MAKEUP_OVERALL_LEVEL.get(R.string.makeup_tipsy);
        List<MakeupItem> tipSyMakeups = new ArrayList<>(4);
        tipSyMakeups.add(MAKEUP_BLUSHER_17.faceMakeup(tipsyLevel));
        tipSyMakeups.add(MAKEUP_EYE_SHADOW_14.faceMakeup(tipsyLevel));
        tipSyMakeups.add(MAKEUP_EYEBROW_14.faceMakeup(0.5f));
        tipSyMakeups.add(MAKEUP_LIPSTICK_14.faceMakeup(tipsyLevel));
        FaceMakeup tipsy = new FaceMakeup(tipSyMakeups, R.string.makeup_tipsy, R.drawable.demo_makeup_tipsy);
        faceMakeups.add(tipsy);

        // 厌世
        float disgustingLevel = MAKEUP_OVERALL_LEVEL.get(R.string.makeup_disgusting);
        List<MakeupItem> disgustingMakeups = new ArrayList<>(4);
        disgustingMakeups.add(MAKEUP_BLUSHER_24.faceMakeup(disgustingLevel));
        disgustingMakeups.add(MAKEUP_EYE_SHADOW_22.faceMakeup(disgustingLevel));
        disgustingMakeups.add(MAKEUP_EYEBROW_13.faceMakeup(disgustingLevel));
        disgustingMakeups.add(MAKEUP_LIPSTICK_22.faceMakeup(disgustingLevel));
        FaceMakeup disgusting = new FaceMakeup(disgustingMakeups, R.string.makeup_disgusting, R.drawable.demo_makeup_world_weariness);
        faceMakeups.add(disgusting);

        // 黑白
        float blackWhiteLevel = MAKEUP_OVERALL_LEVEL.get(R.string.makeup_black_white);
        List<MakeupItem> blackWhiteMakeups = new ArrayList<>(4);
        blackWhiteMakeups.add(MAKEUP_EYE_SHADOW_15.faceMakeup(blackWhiteLevel));
        blackWhiteMakeups.add(MAKEUP_EYEBROW_15.faceMakeup(blackWhiteLevel));
        blackWhiteMakeups.add(MAKEUP_LIPSTICK_15.faceMakeup(blackWhiteLevel));
        FaceMakeup blackWhite = new FaceMakeup(blackWhiteMakeups, R.string.makeup_black_white, R.drawable.demo_makeup_black);
        faceMakeups.add(blackWhite);
*/

        // 赤茶
        List<MakeupItem> redTeaMakeups = new ArrayList<>(4);
        redTeaMakeups.add(MAKEUP_BLUSHER_18.faceMakeup(1.0f));
        redTeaMakeups.add(MAKEUP_EYE_SHADOW_16.faceMakeup(1.0f));
        redTeaMakeups.add(MAKEUP_EYEBROW_10.faceMakeup(0.6f));
        redTeaMakeups.add(MAKEUP_LIPSTICK_16.faceMakeup(0.9f));
        FaceMakeup redTea = new FaceMakeup(redTeaMakeups, R.string.makeup_red_tea, R.drawable.demo_makeup_red_tea);
        faceMakeups.add(redTea);

        // 冬日
        List<MakeupItem> winterMakeups = new ArrayList<>(4);
        winterMakeups.add(MAKEUP_BLUSHER_19.faceMakeup(0.8f));
        winterMakeups.add(MAKEUP_EYE_SHADOW_17.faceMakeup(0.8f));
        winterMakeups.add(MAKEUP_EYEBROW_12.faceMakeup(0.6f));
        winterMakeups.add(MAKEUP_LIPSTICK_17.faceMakeup(0.9f));
        FaceMakeup winter = new FaceMakeup(winterMakeups, R.string.makeup_winter, R.drawable.demo_makeup_winter);
        faceMakeups.add(winter);

        // 奶油
        List<MakeupItem> creamMakeups = new ArrayList<>(4);
        creamMakeups.add(MAKEUP_BLUSHER_21.faceMakeup(1.0f));
        creamMakeups.add(MAKEUP_EYE_SHADOW_19.faceMakeup(0.95f));
        creamMakeups.add(MAKEUP_EYEBROW_17.faceMakeup(0.5f));
        creamMakeups.add(MAKEUP_LIPSTICK_19.faceMakeup(0.75f));
        FaceMakeup cream = new FaceMakeup(creamMakeups, R.string.makeup_cream, R.drawable.demo_makeup_cream);
        faceMakeups.add(cream);

        return faceMakeups;
    }

    public MakeupItem faceMakeup() {
        return new MakeupItem(name, path, type, strId, iconId);
    }

    public MakeupItem faceMakeup(float level) {
        return new MakeupItem(name, path, type, strId, iconId, level);
    }
}