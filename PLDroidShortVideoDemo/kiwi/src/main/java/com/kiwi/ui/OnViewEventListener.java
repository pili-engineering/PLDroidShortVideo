package com.kiwi.ui;

import com.kiwi.tracker.KwFilterType;
import com.kiwi.tracker.bean.conf.StickerConfig;

public interface OnViewEventListener {

    void onStickerChanged(StickerConfig stickerConfig);

    void onSwitchEyeAndThin(boolean enable);

    void onDistortionChanged(KwFilterType filterType);

    void onAdjustFaceBeauty(int type, float param);

    void onFaceBeautyLevel(float level);

    void onSwitchFaceBeauty(boolean enable);
}