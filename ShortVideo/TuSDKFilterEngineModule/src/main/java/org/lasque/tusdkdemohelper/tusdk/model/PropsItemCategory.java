package org.lasque.tusdkdemohelper.tusdk.model;

import org.lasque.tusdk.video.editor.TuSdkMediaEffectData;

import java.util.ArrayList;
import java.util.List;

/******************************************************************
 * droid-sdk-video 
 * org.lasque.tusdkvideodemo.views.props.model
 *
 * @author sprint
 * @Date 2018/12/28 11:20 AM
 * @Copyright (c) 2018 tutucloud.com. All rights reserved.
 ******************************************************************/
// 道具分类
public class PropsItemCategory <Item extends PropsItem>{

    /** 分类名称 */
    private String mName;

    /** 道具列表 */
    private List<Item> mItems;

    /** 道具分类对应的特效类型 */
    private TuSdkMediaEffectData.TuSdkMediaEffectDataType mMediaEffectType;

    public PropsItemCategory(TuSdkMediaEffectData.TuSdkMediaEffectDataType mediaEffectType, List<Item> items) {
        mItems = new ArrayList<>(items);
        this.mMediaEffectType =  mediaEffectType;
    }

    /**
     * 获取分离下的所有道具
     *
     * @return 道具列表
     */
    public List<Item> getItems() {
        return mItems;
    }

    /**
     * 设置分类名称
     *
     * @param name 分离名称
     */
    public void setName(String name) {
        this.mName = name;
    }

    /**
     * 获取分类名称
     *
     * @return
     */
    public String getName() {
        return this.mName;
    }

    /**
     * 道具分类对应的特效类型
     *
     * @return TuSdkMediaEffectData.TuSdkMediaEffectDataType
     */
    public TuSdkMediaEffectData.TuSdkMediaEffectDataType getMediaEffectType() {
        return mMediaEffectType;
    }
}

