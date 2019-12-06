package org.lasque.tusdkdemohelper.tusdk.model;

import org.lasque.tusdk.core.seles.tusdk.TuSDKMonsterFaceWrap;
import org.lasque.tusdk.video.editor.TuSDKMediaMonsterFaceEffect;
import org.lasque.tusdk.video.editor.TuSdkMediaEffectData;

/******************************************************************
 * droid-sdk-video 
 * org.lasque.tusdkvideodemo.views.props.model
 *
 * @author sprint
 * @Date 2018/12/28 5:58 PM
 * @Copyright (c) 2018 tutucloud.com. All rights reserved.
 ******************************************************************/
// 哈哈镜道具
public class PropsItemMonster extends PropsItem {

    /** 哈哈镜类型 */
    private TuSDKMonsterFaceWrap.TuSDKMonsterFaceType mMonsterFaceType;
    /** 哈哈镜特效 */
    private TuSDKMediaMonsterFaceEffect mMonsterFaceEffect;

    /** 缩略图名称 */
    private String mThumbName;


    public PropsItemMonster(TuSDKMonsterFaceWrap.TuSDKMonsterFaceType monsterFaceType) {
        this.mMonsterFaceType = monsterFaceType;
    }

    /**
     * 设置缩略图名称
     *
     * @param thumbName
     */
    public void setThumbName(String thumbName) {
        this.mThumbName = thumbName;
    }

    /**
     * 获取缩略图名称
     *
     * @return
     */
    public String getThumbName() {
        return mThumbName;
    }

    /**
     * 获取道具对应的 SDK 特效
     *
     * @return TuSdkMediaEffectData
     */
    @Override
    public TuSdkMediaEffectData effect() {

        if (mMonsterFaceEffect == null)
            mMonsterFaceEffect = new TuSDKMediaMonsterFaceEffect(mMonsterFaceType);

        return mMonsterFaceEffect;
    }
}

