package com.qiniu.pili.droid.shortvideo.demo.tusdk;


import android.content.Context;

import org.lasque.tusdk.api.TuSDKFilterEngine;
import org.lasque.tusdk.core.utils.image.ImageOrientation;
import org.lasque.tusdk.video.editor.TuSDKTimeRange;

import java.util.ArrayList;

public class TuSDKManager {
    // 特效信息集合
    private volatile ArrayList<MagicModel> mMagicModelsList = new ArrayList<>();

    // TuSDK 处理引擎 负责预览时处理
    private TuSDKFilterEngine mPreviewFilterEngine;

    // TuSDK 处理引擎 负责保存时处理
    private TuSDKFilterEngine mSaveFilterEngine;

    private Context mContext;

    public TuSDKManager(Context context) {
        mContext = context;
    }

    /**
     * 初始化预览 TuSDKFilterEngine
     */
    public void setupPreviewFilterEngine() {
        if (mPreviewFilterEngine != null) return;
        mPreviewFilterEngine = createFilterEngine();
    }

    /**
     * 初始化保存 TuSDKFilterEngine
     */
    public void setupSaveFilterEngine() {
        if (mSaveFilterEngine != null) return;
        mSaveFilterEngine = createFilterEngine();
    }

    /**
     * 获取预览 TuSDKFilterEngine
     */
    public TuSDKFilterEngine getPreviewFilterEngine() {
        return mPreviewFilterEngine;
    }

    /**
     * 获取保存 TuSDKFilterEngine
     */
    public TuSDKFilterEngine getSaveFilterEngine() {
        return mSaveFilterEngine;
    }

    /**
     * 销毁预览 TuSDKFilterEngine
     */
    public void destroyPreviewFilterEngine() {
        if (mPreviewFilterEngine != null) {
            mPreviewFilterEngine.destroy();
            mPreviewFilterEngine = null;
        }
    }

    /**
     * 销毁保存 TuSDKFilterEngine
     */
    public void destroySaveFilterEngine() {
        if (mSaveFilterEngine != null) {
            mSaveFilterEngine.destroy();
            mSaveFilterEngine = null;
        }
    }

    /**
     * 添加一个场景特效信息
     *
     * @param magicModel
     */
    public synchronized void addMagicModel(MagicModel magicModel) {
        this.mMagicModelsList.add(magicModel);
    }

    /**
     * 获取设置的最后一个场景特效信息
     *
     * @return MagicModel
     */
    public synchronized MagicModel getLastMagicModel() {
        if (this.mMagicModelsList.size() == 0) return null;
        return this.mMagicModelsList.get(this.mMagicModelsList.size() - 1);
    }

    /**
     * 清除场景特效
     */
    public synchronized void reset() {
        this.mMagicModelsList.clear();
    }

    /**
     * 根据 position 找到该位置设置的场景特效信息
     *
     * @param position 毫秒
     * @return MagicModel
     */
    public synchronized MagicModel findMagicModelWithPosition(long position) {
        for (MagicModel magicModel : mMagicModelsList) {
            if ((magicModel.getTimeRange().start <= position && position <= magicModel.getTimeRange().end))
                return magicModel;
        }
        return null;
    }

    private TuSDKFilterEngine createFilterEngine() {
        // 美颜处理
        TuSDKFilterEngine filterEngine = new TuSDKFilterEngine(mContext, false, false);

        // 设置是否输出原始图片朝向 false: 图像被转正后输出
        filterEngine.setOutputOriginalImageOrientation(true);
        // 设置输入的图片朝向 如果输入的图片不是原始朝向 该选项必须配置
        filterEngine.setInputImageOrientation(ImageOrientation.DownMirrored);
        // 设置是否开启动态贴纸功能
        filterEngine.setEnableLiveSticker(true);
        return filterEngine;
    }

    /**
     * 场景特效信息
     */
    public static class MagicModel {
        // 特效code
        private String mMagicCode;
        // 特效时间段
        private TuSDKTimeRange mTimeRange;

        public MagicModel(String magicCode, TuSDKTimeRange timeRange) {
            this.mMagicCode = magicCode;
            this.mTimeRange = timeRange;
        }

        public String getMagicCode() {
            return mMagicCode;
        }

        public TuSDKTimeRange getTimeRange() {
            return this.mTimeRange;
        }
    }
}
