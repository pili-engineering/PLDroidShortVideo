package com.faceunity;


import com.faceunity.entity.Effect;
import com.faceunity.entity.Filter;
import com.faceunity.entity.MagicPhotoEntity;
import com.faceunity.entity.MakeupItem;

import java.util.List;

/**
 * FURenderer与界面之间的交互接口
 */
public interface OnFUControlListener {

    /**
     * 音乐滤镜时间
     *
     * @param time
     */
    void onMusicFilterTime(long time);

    /**
     * 道具贴纸选择
     *
     * @param effectItemName 道具贴纸文件名
     */
    void onEffectSelected(Effect effectItemName);

    /**
     * 滤镜强度
     *
     * @param progress 滤镜强度
     */
    void onFilterLevelSelected(float progress);

    /**
     * 滤镜选择
     *
     * @param filterName 滤镜名称
     */
    void onFilterNameSelected(Filter filterName);

    /**
     * 美发颜色
     *
     * @param type
     * @param hairColorIndex 美发颜色
     * @param hairColorLevel 美发颜色强度
     */
    void onHairSelected(int type, int hairColorIndex, float hairColorLevel);

    /**
     * 调整美发强度
     *
     * @param type
     * @param hairColorIndex
     * @param hairColorLevel
     */
    void onHairLevelSelected(int type, int hairColorIndex, float hairColorLevel);

    /**
     * 精准磨皮
     *
     * @param isOpen 是否开启精准磨皮（0关闭 1开启）
     */
    void onSkinDetectSelected(float isOpen);

    /**
     * 美肤类型
     *
     * @param isOpen 0:清晰美肤 1:朦胧美肤
     */
    void onHeavyBlurSelected(float isOpen);

    /**
     * 磨皮选择
     *
     * @param level 磨皮level
     */
    void onBlurLevelSelected(float level);

    /**
     * 美白选择
     *
     * @param level 美白
     */
    void onColorLevelSelected(float level);

    /**
     * 红润
     */
    void onRedLevelSelected(float level);

    /**
     * 亮眼
     */
    void onEyeBrightSelected(float level);

    /**
     * 美牙
     */
    void onToothWhitenSelected(float level);

    /**
     * 脸型选择
     */
    void onFaceShapeSelected(float faceShape);

    /**
     * 大眼选择
     *
     * @param level 大眼
     */
    void onEyeEnlargeSelected(float level);

    /**
     * 瘦脸选择
     *
     * @param level 瘦脸
     */
    void onCheekThinningSelected(float level);

    /**
     * 下巴
     */
    void onIntensityChinSelected(float level);

    /**
     * 额头
     */
    void onIntensityForeheadSelected(float level);

    /**
     * 瘦鼻
     */
    void onIntensityNoseSelected(float level);


    /**
     * 嘴形
     */
    void onIntensityMouthSelected(float level);

    /**
     * 切换海报模板
     *
     * @param tempWidth
     * @param tempHeight
     * @param temp
     * @param landmark
     */
    void onPosterTemplateSelected(int tempWidth, int tempHeight, byte[] temp, float[] landmark);

    /**
     * 海报换脸输入照片
     *
     * @param inputWidth
     * @param inputHeight
     * @param input
     * @param landmark
     */
    void onPosterInputPhoto(int inputWidth, int inputHeight, byte[] input, float[] landmark);

    /**
     * 设置风格滤镜
     *
     * @param style
     */
    void onCartoonFilterSelected(int style);

    /**
     * 选择美妆效果
     *
     * @param makeupItem
     * @param level
     */
    void onMakeupSelected(MakeupItem makeupItem, float level);

    /**
     * 调节美妆强度
     *
     * @param makeupType
     * @param level
     */
    void onMakeupLevelChanged(int makeupType, float level);

    /**
     * 调节多个妆容
     *
     * @param makeupItems
     */
    void onMakeupBatchSelected(List<MakeupItem> makeupItems);

    /**
     * 妆容总体调节
     *
     * @param level
     */
    void onMakeupOverallLevelChanged(float level);

    /**
     * 选择美妆效果（轻美妆，质感美颜）
     *
     * @param makeupItem
     * @param level
     */
    void onLightMakeupSelected(MakeupItem makeupItem, float level);

    /**
     * 调节多个妆容（轻美妆，质感美颜）
     *
     * @param makeupItems
     */
    void onLightMakeupBatchSelected(List<MakeupItem> makeupItems);

    /**
     * 妆容总体调节（轻美妆，质感美颜）
     *
     * @param level
     */
    void onLightMakeupOverallLevelChanged(float level);

    /**
     * 设置异图的点位和图像数据，用来驱动图像
     *
     * @param magicPhotoEntity
     */
    void setMagicPhoto(final MagicPhotoEntity magicPhotoEntity);

    /**
     * 重置美肤参数
     */
    void onSkinBeautyReset();

    /**
     * 重置美型参数
     */
    void onFaceShapeReset();
}
