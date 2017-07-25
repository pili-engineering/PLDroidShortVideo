package com.kiwi.ui;

import com.blankj.utilcode.utils.SPUtils;

public class SharedPreferenceManager {

    public static SharedPreferenceManager getInstance() {
        return instance;
    }

    private static SharedPreferenceManager instance = new SharedPreferenceManager();

    private SPUtils beautyConfig;
    private SharedPreferenceManager(){
        beautyConfig = new SPUtils("beautyConfig");
    }

    public boolean isLocalBeautyEnabled() {
        return  beautyConfig.getBoolean("localBeautyEnbaled",false);
    }

    public void setLocalBeautyEnabled(boolean isOpen) {
        beautyConfig.putBoolean("localBeautyEnbaled",isOpen);
    }

    public int getBigEye() {
        return beautyConfig.getInt("bigEye",50);
    }

    public int getThinFace() {
        return beautyConfig.getInt("thinFace",50);
    }

    public void setBigEye(int progress) {
        beautyConfig.putInt("bigEye",progress);
    }

    public void setThinFace(int progress) {
        beautyConfig.putInt("thinFace",progress);
    }

    public boolean isBeautyEnabled(){
        return beautyConfig.getBoolean("beautyEnabled",true);
    }

    public void setBeautyEnabled(boolean enabled){
        beautyConfig.putBoolean("beautyEnabled",enabled);
    }

    public int getSkinWhite(){
        return beautyConfig.getInt("skinPerfection",50);
    }

    public void setSkinPerfection(int value){
        beautyConfig.putInt("skinPerfection",value);
    }

    public int getSkinRemoveBlemishes(){
        return beautyConfig.getInt("skinRemoveBlemishes",50);
    }

    public void setSkinRemoveBlemishes(int value){
        beautyConfig.putInt("skinRemoveBlemishes",value);
    }

    public int getSkinSaturation(){
        return beautyConfig.getInt("skinSaturation",50);
    }

    public void setSkinSaturation(int value){
        beautyConfig.putInt("skinSaturation",value);
    }

    public int getSkinTenderness(){
        return beautyConfig.getInt("skinTenderness",50);
    }

    public void setSkinTenderness(int value){
        beautyConfig.putInt("skinTenderness",value);
    }
}
