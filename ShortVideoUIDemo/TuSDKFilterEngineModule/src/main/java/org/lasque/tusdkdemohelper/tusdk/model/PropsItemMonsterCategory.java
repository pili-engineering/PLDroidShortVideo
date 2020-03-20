package org.lasque.tusdkdemohelper.tusdk.model;

import org.lasque.tusdk.core.TuSdkContext;
import org.lasque.tusdk.core.seles.tusdk.TuSDKMonsterFaceWrap;
import org.lasque.tusdk.video.editor.TuSdkMediaEffectData;

import java.util.ArrayList;
import java.util.List;

import static org.lasque.tusdk.core.seles.tusdk.TuSDKMonsterFaceWrap.TuSDKMonsterFaceType.TuSDKMonsterFaceTypeBigNose;
import static org.lasque.tusdk.core.seles.tusdk.TuSDKMonsterFaceWrap.TuSDKMonsterFaceType.TuSDKMonsterFaceTypePapayaFace;
import static org.lasque.tusdk.core.seles.tusdk.TuSDKMonsterFaceWrap.TuSDKMonsterFaceType.TuSDKMonsterFaceTypePieFace;
import static org.lasque.tusdk.core.seles.tusdk.TuSDKMonsterFaceWrap.TuSDKMonsterFaceType.TuSDKMonsterFaceTypeSmallEyes;
import static org.lasque.tusdk.core.seles.tusdk.TuSDKMonsterFaceWrap.TuSDKMonsterFaceType.TuSDKMonsterFaceTypeSnakeFace;
import static org.lasque.tusdk.core.seles.tusdk.TuSDKMonsterFaceWrap.TuSDKMonsterFaceType.TuSDKMonsterFaceTypeSquareFace;
import static org.lasque.tusdk.core.seles.tusdk.TuSDKMonsterFaceWrap.TuSDKMonsterFaceType.TuSDKMonsterFaceTypeThickLips;

/******************************************************************
 * droid-sdk-video 
 * org.lasque.tusdkvideodemo.views.props.model
 *
 * @author sprint
 * @Date 2018/12/28 11:20 AM
 * @Copyright (c) 2018 tutucloud.com. All rights reserved.
 ******************************************************************/
// 哈哈镜分类
public class PropsItemMonsterCategory extends PropsItemCategory<PropsItemMonster> {

    /**
     * 根据哈哈镜道具列表初始化道具分类
     *
     * @param propsItemMonsters
     */
    public PropsItemMonsterCategory(List<PropsItemMonster> propsItemMonsters) {
        super(TuSdkMediaEffectData.TuSdkMediaEffectDataType.TuSdkMediaEffectDataTypeMonsterFace,propsItemMonsters);
    }

    /**
     * 获取所有哈哈镜分类
     *
     * @return List<PropsItemMonsterCategory>
     */
    public static List<PropsItemMonsterCategory> allCategories() {

        TuSDKMonsterFaceWrap.TuSDKMonsterFaceType[] faceTypes =
                {
                        TuSDKMonsterFaceTypeBigNose, // 大鼻子
                        TuSDKMonsterFaceTypePapayaFace, // 木瓜脸
                        TuSDKMonsterFaceTypePieFace, // 大饼脸
                        TuSDKMonsterFaceTypeSmallEyes, // 眯眯眼
                        TuSDKMonsterFaceTypeSnakeFace, // 蛇精脸
                        TuSDKMonsterFaceTypeSquareFace, // 国字脸
                        TuSDKMonsterFaceTypeThickLips // 厚嘴唇
                };


        // 缩略图后缀
        String[] faceTypeTitles =
                {
                        "bignose",
                        "papaya",
                        "pie",
                        "smalleyes",
                        "snake",
                        "square",
                        "thicklips"
                };



        List<PropsItemMonsterCategory> categories = new ArrayList<>();
        List<PropsItemMonster> monsters = new ArrayList<>();

        for (int i = 0; i<faceTypes.length; i++) {

            PropsItemMonster monster = new PropsItemMonster(faceTypes[i]);
            monster.setThumbName(faceTypeTitles[i]);
            monsters.add(monster);
        }

        PropsItemMonsterCategory monsterCategory = new PropsItemMonsterCategory(monsters);
        monsterCategory.setName(TuSdkContext.getString(TuSdkContext.getStringResId("lsq_face_monster")));
        categories.add(monsterCategory);

        return categories;
    }
}

