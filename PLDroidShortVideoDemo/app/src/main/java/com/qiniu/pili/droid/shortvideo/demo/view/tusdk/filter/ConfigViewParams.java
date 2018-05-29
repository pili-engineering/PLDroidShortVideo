package com.qiniu.pili.droid.shortvideo.demo.view.tusdk.filter;

import java.util.ArrayList;
import java.util.List;

/**
 * 可以设置调节栏参数
 */
public class ConfigViewParams {
    /**
     * 调节栏参数集合
     */
    private List<ConfigViewArg> mArgs = new ArrayList<ConfigViewArg>();

    /**
     * 添加一个浮点参数 (默认:minValue=0.0f, maxValue=1.0f)
     *
     * @param key   参数键名
     * @param value
     */
    public void appendFloatArg(String key, float value) {
        this.appendFloatArg(key, value, 0.0f, 1.0f);
    }

    /**
     * 添加一个浮点参数
     *
     * @param key      参数键名
     * @param value    当前值
     * @param minValue 最小值
     * @param maxValue 最大值
     */
    public void appendFloatArg(String key, float value, float minValue, float maxValue) {
        if (key == null) return;
        ConfigViewArg arg = new ConfigViewArg();
        arg.mKey = key;
        arg.mDefaultValue = arg.mFloatValue = value;
        arg.minFloatValue = minValue;
        arg.maxFloatValue = maxValue;

        this.mArgs.add(arg);
    }

    /**
     * 配置参数总数
     */
    public int size() {
        if (mArgs == null) return 0;
        return mArgs.size();
    }

    /**
     * 参数列表
     */
    public List<ConfigViewArg> getArgs() {
        return mArgs;
    }

    public class ConfigViewArg {
        /**
         * 参数键名
         */
        private String mKey;

        /**
         * 浮点数值
         */
        private float mFloatValue;

        /**
         * 默认值
         */
        private float mDefaultValue;

        /**
         * 最小参数值
         */
        private float minFloatValue;

        /**
         * 最大参数值
         */
        private float maxFloatValue;

        public String getKey() {
            return mKey;
        }

        public void setKey(String key) {
            this.mKey = key;
        }

        public float getFloatValue() {
            return mFloatValue;
        }

        public void setFloatValue(float floatValue) {
            this.mFloatValue = floatValue;
        }

        public float getMinFloatValue() {
            return minFloatValue;
        }

        public void setMinFloatValue(float minFloatValue) {
            this.minFloatValue = minFloatValue;
        }

        public float getMaxFloatValue() {
            return maxFloatValue;
        }

        public void setMaxFloatValue(float maxFloatValue) {
            this.maxFloatValue = maxFloatValue;
        }


        /**
         * 设置百分比 0 - 1
         */
        public void setPercentValue(float precent) {
            if (precent < 0) {
                precent = 0;
            } else if (precent > 1) {
                precent = 1;
            }

            if (this.getPercentValue() != precent) {
                mFloatValue = (maxFloatValue - minFloatValue) * precent + minFloatValue;
            }
        }

        /**
         * 获取百分比
         */
        public float getPercentValue() {
            return (mFloatValue - minFloatValue) / (maxFloatValue - minFloatValue);
        }

        /**
         * 获取值
         */
        public float getValue() {
            return mFloatValue;
        }

        /**
         * 默认值
         */
        public void reset() {
            if (mFloatValue != mDefaultValue) {
                mFloatValue = mDefaultValue;
            }
        }
    }

}
