package com.faceunity.entity;

import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;

import com.faceunity.R;
import com.faceunity.utils.ColorConstant;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Richie on 2019.04.01
 * 存储 avatar 捏脸中的变量
 */
public final class AvatarFaceHelper {
    public static final String DEFAULT_HAIR_PATH = "avatarHair1.bundle";

    // 记录捏脸期间，要保存的参数对，除了头发以外的五官参数
    public static final Map<String, Float> FACE_ASPECT_MAP = new HashMap<>(64);
    // 记录自定义捏脸的参数
    public static final Map<String, Float> CUSTOM_FACE_ASPECT_MAP = new HashMap<>(8);
    // 记录捏脸期间，要保存的颜色，包含所有的五官颜色
    // 由于包含两个 channel0, key 使用"关键字+value数组长度"的组合
    public static final Map<String, double[]> FACE_ASPECT_COLOR_MAP = new HashMap<>(8);
    // 根据类型划分参数, 只读的属性
    public static final Map<Integer, Set<String>> FACE_ASPECT_TYPE_MAP = new HashMap<>(8);
    // 捏脸维度相对维度，只读的属性
    private static final Map<String, String> FACE_ASPECT_OPPORATE_MAP = new HashMap<>(64);
    // 默认的模型组件，只读的属性
    private static final Map<Integer, List<AvatarComponent>> AVATAR_COMPONENTS = new HashMap<>(8);
    // 编辑时，头发道具的路径
    public static String sFaceHairBundlePath = DEFAULT_HAIR_PATH;

    static {
        FACE_ASPECT_OPPORATE_MAP.put("1", "2");
        FACE_ASPECT_OPPORATE_MAP.put("2", "1");
        FACE_ASPECT_OPPORATE_MAP.put("3", "4");
        FACE_ASPECT_OPPORATE_MAP.put("4", "3");
        FACE_ASPECT_OPPORATE_MAP.put("5", "6");
        FACE_ASPECT_OPPORATE_MAP.put("6", "5");
        FACE_ASPECT_OPPORATE_MAP.put("7", "8");
        FACE_ASPECT_OPPORATE_MAP.put("8", "7");
        FACE_ASPECT_OPPORATE_MAP.put("9", "10");
        FACE_ASPECT_OPPORATE_MAP.put("10", "9");
        FACE_ASPECT_OPPORATE_MAP.put("11", "12");
        FACE_ASPECT_OPPORATE_MAP.put("12", "11");
        FACE_ASPECT_OPPORATE_MAP.put("13", "14");
        FACE_ASPECT_OPPORATE_MAP.put("14", "13");
//        F1ACE_ASPECT_MAP.put("15", "26");
        FACE_ASPECT_OPPORATE_MAP.put("16", "17");
        FACE_ASPECT_OPPORATE_MAP.put("17", "16");
        FACE_ASPECT_OPPORATE_MAP.put("18", "19");
        FACE_ASPECT_OPPORATE_MAP.put("19", "18");
        FACE_ASPECT_OPPORATE_MAP.put("20", "21");
        FACE_ASPECT_OPPORATE_MAP.put("21", "20");
        FACE_ASPECT_OPPORATE_MAP.put("22", "23");
        FACE_ASPECT_OPPORATE_MAP.put("23", "22");
        FACE_ASPECT_OPPORATE_MAP.put("24", "25");
        FACE_ASPECT_OPPORATE_MAP.put("25", "24");
//        F1ACE_ASPECT_MAP.put("26", "15");
        FACE_ASPECT_OPPORATE_MAP.put("27", "28");
        FACE_ASPECT_OPPORATE_MAP.put("28", "27");
        FACE_ASPECT_OPPORATE_MAP.put("29", "33");
        FACE_ASPECT_OPPORATE_MAP.put("30", "36");
        FACE_ASPECT_OPPORATE_MAP.put("31", "32");
        FACE_ASPECT_OPPORATE_MAP.put("32", "31");
        FACE_ASPECT_OPPORATE_MAP.put("33", "29");
        FACE_ASPECT_OPPORATE_MAP.put("34", "35");
        FACE_ASPECT_OPPORATE_MAP.put("35", "34");
        FACE_ASPECT_OPPORATE_MAP.put("36", "30");
    }

    static {
        FACE_ASPECT_TYPE_MAP.put(AvatarFaceType.AVATAR_FACE_SHAPE, new HashSet<>(Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15")));
        FACE_ASPECT_TYPE_MAP.put(AvatarFaceType.AVATAR_FACE_NOSE, new HashSet<>(Arrays.asList("18", "19", "20", "21", "22", "23")));
        FACE_ASPECT_TYPE_MAP.put(AvatarFaceType.AVATAR_FACE_LIP, new HashSet<>(Arrays.asList("11", "12", "13", "14", "15", "16", "17", "24", "25", "26")));
        FACE_ASPECT_TYPE_MAP.put(AvatarFaceType.AVATAR_FACE_EYE, new HashSet<>(Arrays.asList("27", "28", "29", "30", "15", "31", "32", "33", "34", "35", "36")));
    }

    public static Set<String> getFaceNamesByType(int type) {
        return FACE_ASPECT_TYPE_MAP.get(type);
    }

    /**
     * 相对值，上对下，高对低
     *
     * @param name
     * @return
     */
    public static String oppositeOf(String name) {
        return FACE_ASPECT_OPPORATE_MAP.get(name);
    }

    /*
  [
{
  "name": "name",
  "level": 1.0,
  "color": [
    0,
    0,
    0
  ]
}
]
  * */
    // 将从数据库读取的 JSON 解析成 List
    public static List<AvatarFaceAspect> config2List(String config) {
        try {
            JSONArray jsonArray = new JSONArray(config);
            int length = jsonArray.length();
            List<AvatarFaceAspect> avatarFaceAspects = new ArrayList<>(length);
            AvatarFaceAspect avatarFaceAspect;
            for (int i = 0; i < length; i++) {
                avatarFaceAspect = new AvatarFaceAspect();
                JSONObject jsonObject = jsonArray.optJSONObject(i);
                String name = jsonObject.optString("name");
                if (jsonObject.has("level")) {
                    double level = jsonObject.optDouble("level");
                    avatarFaceAspect.setLevel((float) level);
                    avatarFaceAspect.setName(name);
                } else if (jsonObject.has("color")) {
                    JSONArray color = jsonObject.optJSONArray("color");
                    if (color != null) {
                        int cLen = color.length();
                        double[] col = new double[cLen];
                        for (int j = 0; j < cLen; j++) {
                            col[j] = color.optDouble(j);
                        }
                        avatarFaceAspect.setColor(col);
                    }
                    avatarFaceAspect.setName(name);
                } else if (jsonObject.has("path")) {
                    String path = jsonObject.optString("path");
                    avatarFaceAspect.setBundlePath(path);
                }
                avatarFaceAspects.add(avatarFaceAspect);
            }
            return avatarFaceAspects;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    /*
    * [
  {
    "name": "name",
    "level": 1.0,
    "color": [
      0,
      0,
      0
    ]
  }
]
    * */
    // 保存到数据库的是 JSON 字符串
    public static String list2Config() {
        // 无关参数
        Set<Map.Entry<String, Float>> faceAspectEntries = FACE_ASPECT_MAP.entrySet();
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject;
        for (Map.Entry<String, Float> faceAspectEntry : faceAspectEntries) {
            try {
                jsonObject = new JSONObject();
                jsonObject.putOpt("name", faceAspectEntry.getKey());
                jsonObject.putOpt("level", faceAspectEntry.getValue());
                jsonArray.put(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        // 五官颜色
        Set<Map.Entry<String, double[]>> faceAspectColorEntries = FACE_ASPECT_COLOR_MAP.entrySet();
        JSONArray colors;
        for (Map.Entry<String, double[]> faceAspectColorEntry : faceAspectColorEntries) {
            try {
                double[] value = faceAspectColorEntry.getValue();
                colors = new JSONArray();
                for (double v : value) {
                    colors.put(v);
                }
                jsonObject = new JSONObject();
                String key = faceAspectColorEntry.getKey();
                key = key.substring(0, key.length() - 1);
                jsonObject.putOpt("name", key);
                jsonObject.putOpt("color", colors);
                jsonArray.put(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        // 头发道具
        if (!TextUtils.isEmpty(sFaceHairBundlePath)) {
            jsonObject = new JSONObject();
            try {
                jsonObject.putOpt("name", "hair_bundle_path");
                jsonObject.putOpt("path", sFaceHairBundlePath);
                jsonArray.put(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return jsonArray.toString();
    }

    /**
     * 是否需要自定义
     *
     * @param type
     * @return
     */
    public static boolean isNeedCustomize(int type) {
        return type == AvatarFaceType.AVATAR_FACE_SHAPE || type == AvatarFaceType.AVATAR_FACE_EYE || type == AvatarFaceType.AVATAR_FACE_LIP
                || type == AvatarFaceType.AVATAR_FACE_NOSE;
    }

    /**
     * 某个五官的组件
     *
     * @param type
     * @return
     */
    public static List<AvatarComponent> getAvatarComponents(int type) {
        List<AvatarComponent> avatarComponents = AVATAR_COMPONENTS.get(type);
        if (avatarComponents == null) {
            registerAvatarComponent(type);
            avatarComponents = AVATAR_COMPONENTS.get(type);
            if (avatarComponents == null) {
                avatarComponents = new ArrayList<>(8);
                AVATAR_COMPONENTS.put(type, avatarComponents);
            }
        }
        return avatarComponents;
    }

    /**
     * 五官的类型
     *
     * @param context
     * @return
     */
    public static List<AvatarFaceType> getAvatarFaceTypes(Context context) {
        Resources resources = context.getResources();
        List<AvatarFaceType> avatarFaceTypes = new ArrayList<>(8);
        avatarFaceTypes.add(new AvatarFaceType(resources.getString(R.string.avatar_face_hair), AvatarFaceType.AVATAR_FACE_HAIR, ColorConstant.hair_color));
        avatarFaceTypes.add(new AvatarFaceType(resources.getString(R.string.avatar_face_face), AvatarFaceType.AVATAR_FACE_SHAPE, ColorConstant.skin_color));
        avatarFaceTypes.add(new AvatarFaceType(resources.getString(R.string.avatar_face_eye), AvatarFaceType.AVATAR_FACE_EYE, ColorConstant.iris_color));
        avatarFaceTypes.add(new AvatarFaceType(resources.getString(R.string.avatar_face_lip), AvatarFaceType.AVATAR_FACE_LIP, ColorConstant.lip_color));
        avatarFaceTypes.add(new AvatarFaceType(resources.getString(R.string.avatar_face_nose), AvatarFaceType.AVATAR_FACE_NOSE));
//        avatarFaceTypes.add(new AvatarFaceType(resources.getString(R.string.avatar_face_eyebrow), AVATAR_FACE_EYEBROW));
//        avatarFaceTypes.add(new AvatarFaceType(resources.getString(R.string.avatar_face_eyelash), AVATAR_FACE_EYELASH));
        return avatarFaceTypes;
    }

    public static void registerAvatarComponent(int type) {
        switch (type) {
            case AvatarFaceType.AVATAR_FACE_SHAPE: {
                List<AvatarComponent> avatarComponents = new ArrayList<>(8);
                if (AvatarFaceHelper.isNeedCustomize(type)) {
                    avatarComponents.add(new AvatarComponent(type, R.drawable.demo_icon_diy, null, true));
                }

                List<AvatarFaceAspect> faceShapeAspects = new ArrayList<>(2);
                faceShapeAspects.add(AvatarFaceAspect.ofValue(type, "2", 0.6f));
                faceShapeAspects.add(AvatarFaceAspect.ofValue(type, "7", 0.45f));
                AvatarComponent faceShape01 = new AvatarComponent(type, R.drawable.lianxing_01, faceShapeAspects);
                avatarComponents.add(faceShape01);

                faceShapeAspects = new ArrayList<>(2);
                faceShapeAspects.add(AvatarFaceAspect.ofValue(type, "10", 0.7f));
                AvatarComponent faceShape02 = new AvatarComponent(type, R.drawable.lianxing_02, faceShapeAspects);
                avatarComponents.add(faceShape02);

                faceShapeAspects = new ArrayList<>(4);
                faceShapeAspects.add(AvatarFaceAspect.ofValue(type, "1", 0.2f));
                faceShapeAspects.add(AvatarFaceAspect.ofValue(type, "9", 0.75f));
                faceShapeAspects.add(AvatarFaceAspect.ofValue(type, "7", 0.4f));
                AvatarComponent faceShape03 = new AvatarComponent(type, R.drawable.lianxing_03, faceShapeAspects);
                avatarComponents.add(faceShape03);

                AVATAR_COMPONENTS.put(type, avatarComponents);
            }
            break;
            case AvatarFaceType.AVATAR_FACE_EYE: {
                List<AvatarComponent> avatarComponents = new ArrayList<>(8);
                if (AvatarFaceHelper.isNeedCustomize(type)) {
                    avatarComponents.add(new AvatarComponent(type, R.drawable.demo_icon_diy, null, true));
                }

                List<AvatarFaceAspect> eyeAspects = new ArrayList<>(4);
                eyeAspects.add(AvatarFaceAspect.ofValue(type, "35", 0.4f));
                eyeAspects.add(AvatarFaceAspect.ofValue(type, "33", 0.45f));
                eyeAspects.add(AvatarFaceAspect.ofValue(type, "28", 0.58f));
                AvatarComponent eye01 = new AvatarComponent(type, R.drawable.yanjing_01, eyeAspects);
                avatarComponents.add(eye01);

                eyeAspects = new ArrayList<>(4);
                eyeAspects.add(AvatarFaceAspect.ofValue(type, "34", 0.25f));
                eyeAspects.add(AvatarFaceAspect.ofValue(type, "29", 0.6f));
                AvatarComponent eye02 = new AvatarComponent(type, R.drawable.yanjing_02, eyeAspects);
                avatarComponents.add(eye02);

                eyeAspects = new ArrayList<>(4);
                eyeAspects.add(AvatarFaceAspect.ofValue(type, "34", 0.7f));
                eyeAspects.add(AvatarFaceAspect.ofValue(type, "29", 0.25f));
                eyeAspects.add(AvatarFaceAspect.ofValue(type, "28", 0.25f));
                AvatarComponent eye03 = new AvatarComponent(type, R.drawable.yanjing_03, eyeAspects);
                avatarComponents.add(eye03);

                AVATAR_COMPONENTS.put(type, avatarComponents);
            }
            break;
            case AvatarFaceType.AVATAR_FACE_NOSE: {
                List<AvatarComponent> avatarComponents = new ArrayList<>(8);
                if (AvatarFaceHelper.isNeedCustomize(type)) {
                    avatarComponents.add(new AvatarComponent(type, R.drawable.demo_icon_diy, null, true));
                }

                List<AvatarFaceAspect> noseAspects = new ArrayList<>(2);
                noseAspects.add(AvatarFaceAspect.ofValue(type, "22", 0.75f));
                noseAspects.add(AvatarFaceAspect.ofValue(type, "20", 0.05f));
                AvatarComponent nose01 = new AvatarComponent(type, R.drawable.bizi_01, noseAspects);
                avatarComponents.add(nose01);

                noseAspects = new ArrayList<>(2);
                noseAspects.add(AvatarFaceAspect.ofValue(type, "19", 0.15f));
                noseAspects.add(AvatarFaceAspect.ofValue(type, "22", 0.8f));
                noseAspects.add(AvatarFaceAspect.ofValue(type, "20", 0.2f));
                AvatarComponent nose02 = new AvatarComponent(type, R.drawable.bizi_02, noseAspects);
                avatarComponents.add(nose02);

                noseAspects = new ArrayList<>(2);
                noseAspects.add(AvatarFaceAspect.ofValue(type, "19", 0.1f));
                noseAspects.add(AvatarFaceAspect.ofValue(type, "22", 0.3f));
                noseAspects.add(AvatarFaceAspect.ofValue(type, "21", 0.2f));
                AvatarComponent nose03 = new AvatarComponent(type, R.drawable.bizi_03, noseAspects);
                avatarComponents.add(nose03);

                noseAspects = new ArrayList<>(2);
                noseAspects.add(AvatarFaceAspect.ofValue(type, "22", 0.6f));
                noseAspects.add(AvatarFaceAspect.ofValue(type, "21", 0.25f));
                AvatarComponent nose04 = new AvatarComponent(type, R.drawable.bizi_04, noseAspects);
                avatarComponents.add(nose04);

                AVATAR_COMPONENTS.put(type, avatarComponents);
            }
            break;
            case AvatarFaceType.AVATAR_FACE_LIP: {
                List<AvatarComponent> avatarComponents = new ArrayList<>(8);
                if (AvatarFaceHelper.isNeedCustomize(type)) {
                    avatarComponents.add(new AvatarComponent(type, R.drawable.demo_icon_diy, null, true));
                }

                List<AvatarFaceAspect> noseAspects = new ArrayList<>(2);
                noseAspects.add(AvatarFaceAspect.ofValue(type, "11", 0.65f));
                AvatarComponent lip01 = new AvatarComponent(type, R.drawable.zuiba_01, noseAspects);
                avatarComponents.add(lip01);

                noseAspects = new ArrayList<>(2);
                noseAspects.add(AvatarFaceAspect.ofValue(type, "24", 0.25f));
                noseAspects.add(AvatarFaceAspect.ofValue(type, "11", 0.7f));
                AvatarComponent lip02 = new AvatarComponent(type, R.drawable.zuiba_02, noseAspects);
                avatarComponents.add(lip02);

                noseAspects = new ArrayList<>(2);
                noseAspects.add(AvatarFaceAspect.ofValue(type, "24", 0.45f));
                noseAspects.add(AvatarFaceAspect.ofValue(type, "13", 0.4f));
                AvatarComponent lip03 = new AvatarComponent(type, R.drawable.zuiba_03, noseAspects);
                avatarComponents.add(lip03);

                noseAspects = new ArrayList<>(8);
                Set<String> namesByType = AvatarFaceHelper.getFaceNamesByType(type);
                for (String s : namesByType) {
                    noseAspects.add(AvatarFaceAspect.ofValue(type, s, 0f));
                }
                AvatarComponent lip04 = new AvatarComponent(type, R.drawable.zuiba_04, noseAspects);
                avatarComponents.add(lip04);

                AVATAR_COMPONENTS.put(type, avatarComponents);
            }
            break;
            case AvatarFaceType.AVATAR_FACE_HAIR: {
                List<AvatarComponent> avatarComponents = new ArrayList<>(8);
                avatarComponents.add(new AvatarComponent("", type, R.drawable.male_no_hair));
                avatarComponents.add(new AvatarComponent(DEFAULT_HAIR_PATH, type, R.drawable.male_hair_01));
                avatarComponents.add(new AvatarComponent("avatarHair6.bundle", type, R.drawable.female_hair_06));
                avatarComponents.add(new AvatarComponent("avatarHair4.bundle", type, R.drawable.female_hair_04));
                avatarComponents.add(new AvatarComponent("avatarHair3.bundle", type, R.drawable.female_hair_03));
                avatarComponents.add(new AvatarComponent("avatarHair5.bundle", type, R.drawable.female_hair_05));
                avatarComponents.add(new AvatarComponent("avatarHair2.bundle", type, R.drawable.female_hair_02));

                AVATAR_COMPONENTS.put(type, avatarComponents);
            }
            break;
            default:
        }
    }

    /**
     * 临时解决方案，项目太着急，抱歉
     *
     * @return
     */
    public static List<AvatarFaceAspect> getDefaultNose() {
        List<AvatarFaceAspect> noseAspects = new ArrayList<>(2);
        noseAspects.add(AvatarFaceAspect.ofValue(AvatarFaceType.AVATAR_FACE_NOSE, "22", 0.75f));
        noseAspects.add(AvatarFaceAspect.ofValue(AvatarFaceType.AVATAR_FACE_NOSE, "20", 0.05f));
        return noseAspects;
    }

}
