package org.lasque.tusdkdemohelper.tusdk.model;

import org.lasque.tusdk.video.editor.TuSdkMediaEffectData;

/******************************************************************
 * droid-sdk-video 
 * org.lasque.tusdkvideodemo.views.props.model
 *
 * @author sprint
 * @Date 2018/12/28 11:23 AM
 * @Copyright (c) 2018 tutucloud.com. All rights reserved.
 ******************************************************************/
public abstract class PropsItem {

    /**
     * 获取道具对应的 SDK 特效
     *
     * @return TuSdkMediaEffectData
     */
    public abstract TuSdkMediaEffectData effect();

}

