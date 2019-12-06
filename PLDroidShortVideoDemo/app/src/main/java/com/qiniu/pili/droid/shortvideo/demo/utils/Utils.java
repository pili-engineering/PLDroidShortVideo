package com.qiniu.pili.droid.shortvideo.demo.utils;

import android.graphics.PointF;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 基础工具类，包含了单位转换等
 */
public class Utils {

    /**
     * 弧度换算成角度
     */
    public static double radianToDegree(double radian) {
        return radian * 180 / Math.PI;
    }

    /**
     * 角度换算成弧度
     */
    public static double degreeToRadian(double degree) {
        return degree * Math.PI / 180;
    }

    /**
     * 获取变长参数最大的值
     */
    public static int getMaxValue(Integer... array) {
        List<Integer> list = Arrays.asList(array);
        Collections.sort(list);
        return list.get(list.size() - 1);
    }

    /**
     * 获取变长参数最小的值
     */
    public static int getMinValue(Integer... array) {
        List<Integer> list = Arrays.asList(array);
        Collections.sort(list);
        return list.get(0);
    }

    /**
     * 两个点之间的距离
     */
    public static float getTwoPointsDistance(PointF pf1, PointF pf2) {
        float disX = pf2.x - pf1.x;
        float disY = pf2.y - pf1.y;
        return (float) Math.sqrt(disX * disX + disY * disY);
    }

}
