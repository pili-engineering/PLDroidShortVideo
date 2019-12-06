package com.faceunity.utils;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by tujh on 2018/11/6.
 */
public abstract class ColorConstant {
    private static final String COLOR_PATH = "avatar_color.json";

    /* 0--1 颜色值*/
    public static double[][] skin_color;
    public static double[][] lip_color;
    public static double[][] iris_color;
    public static double[][] hair_color;
    //    public static double[][] beard_color;
//    public static double[][] glass_frame_color;
//    public static double[][] glass_color;
//    public static double[][] hat_color;
    private static boolean sInited;

    public static void init(Context context) {
        if (sInited) {
            return;
        }
        try {
            InputStream is = context.getAssets().open(COLOR_PATH);
            byte[] itemData = new byte[is.available()];
            is.read(itemData);
            is.close();
            String json = new String(itemData);
            JSONObject jsonObject = new JSONObject(json);

            skin_color = parseJson(jsonObject, "skin_color");
            lip_color = parseJson(jsonObject, "lip_color");
            iris_color = parseJson(jsonObject, "iris_color");
            hair_color = parseJson(jsonObject, "hair_color");
//            beard_color = parseJson(jsonObject, "beard_color");
//            glass_frame_color = parseJson(jsonObject, "glass_frame_color");
//            glass_color = parseJson(jsonObject, "glass_color");
//            hat_color = parseJson(jsonObject, "hat_color");
            sInited = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static double[][] parseJson(JSONObject jsonObject, String key) throws JSONException {
        JSONObject object = jsonObject.getJSONObject(key);
        List<double[]> colors = new ArrayList<>(16);
        for (int i = 1; object.has(String.valueOf(i)); i++) {
            JSONObject color = object.getJSONObject(String.valueOf(i));
            int r = color.getInt("r");
            int g = color.getInt("g");
            int b = color.getInt("b");
            if (color.has("intensity")) {
                int intensity = color.getInt("intensity");
                colors.add(new double[]{r, g, b, intensity});
            } else {
                colors.add(new double[]{r, g, b});
            }
        }
        double[][] doubles = new double[colors.size()][];
        colors.toArray(doubles);
        return doubles;
    }
}
