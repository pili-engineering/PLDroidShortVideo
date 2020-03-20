package org.lasque.tusdkdemohelper.tusdk.model;

import org.lasque.tusdk.modules.view.widget.sticker.StickerGroup;
import org.lasque.tusdk.modules.view.widget.sticker.StickerLocalPackage;
import org.lasque.tusdk.video.editor.TuSdkMediaEffectData;
import org.lasque.tusdk.video.editor.TuSdkMediaStickerEffectData;

/******************************************************************
 * droid-sdk-video 
 * org.lasque.tusdkvideodemo.views.props.model
 *
 * @author sprint
 * @Date 2018/12/28 1:41 PM
 * @Copyright (c) 2018 tutucloud.com. All rights reserved.
 ******************************************************************/
/* 贴纸道具 */
public class PropsItemSticker extends PropsItem {

    /** 贴纸对象 */
    private StickerGroup mStickerGrop;

    public PropsItemSticker(StickerGroup stickerGroup) {
        this.mStickerGrop  = stickerGroup;
    }

    public StickerGroup getStickerGrop () {
        return mStickerGrop;
    }

    private TuSdkMediaStickerEffectData mStickerEffect;

    /**
     * 获取道具对应的 SDK 特效
     *
     * @return TuSdkMediaEffectData
     */
    @Override
    public TuSdkMediaEffectData effect() {

        if (mStickerEffect != null) return mStickerEffect;

        /** 使用下载后的贴纸数据生成 TuSdkMediaStickerEffectData */
        StickerGroup stickerGroup = StickerLocalPackage.shared().getStickerGroup(mStickerGrop.groupId);

        if (stickerGroup != null)
            mStickerEffect = new TuSdkMediaStickerEffectData(stickerGroup);

        return mStickerEffect;
    }
}

