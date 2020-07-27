package org.lasque.tusdkdemohelper.tusdk.model;

import android.os.Environment;

import org.json.JSONArray;
import org.json.JSONObject;
import org.lasque.tusdk.core.TuSdk;
import org.lasque.tusdk.core.TuSdkBundle;
import org.lasque.tusdk.core.TuSdkContext;
import org.lasque.tusdk.core.utils.TLog;
import org.lasque.tusdk.core.utils.json.JsonHelper;
import org.lasque.tusdk.modules.view.widget.sticker.StickerGroup;
import org.lasque.tusdk.modules.view.widget.sticker.StickerLocalPackage;
import org.lasque.tusdk.video.editor.TuSdkMediaEffectData;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/******************************************************************
 * droid-sdk-video 
 * org.lasque.tusdkvideodemo.views.props.model
 *
 * @author sprint
 * @Date 2018/12/28 11:19 AM
 * @Copyright (c) 2018 tutucloud.com. All rights reserved.
 ******************************************************************/
// 贴纸分类
public class PropsItemStickerCategory extends PropsItemCategory<PropsItemSticker>{

    public PropsItemStickerCategory(List<PropsItemSticker> stickerPropsItems) {
        super(TuSdkMediaEffectData.TuSdkMediaEffectDataType.TuSdKMediaEffectDataTypeSticker,stickerPropsItems);
    }

    /**
     * 获取本地贴纸
     * @return
     */
    public static void prepareLocalSticker() {

        try
        {

            /**
             * step 1. 准备设置 Master 信息
             * 每次从 TUTU 控制台打包资源后，在 lsq_tusdk_configs.json 文件中可以获取到 master 信息。
             */
            String asset = TuSdkBundle.sdkBundleOther(TuSdk.SDK_CONFIGS);
            String json = TuSdkContext.getAssetsText(asset);
            String master = JsonHelper.json(json).getString("master");


            /**
             *
             * step 2. 将下载后的贴纸加入 TuSDKPFStickerLocalPackage.
             *        将本地贴纸加入 TuSDKPFStickerLocalPackage 后，将负责解析并生成 TuSDKPFStickerGroup 对象。
             *
             *  注 ： 为演示方便，示例代码为简陋设计，开发者可读取指定的贴纸目录。
             */
            File stickerDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(),"stickers");

            if (!stickerDir.exists()) {
                return ;
            }

            String[] stickerPaths = stickerDir.list();

            for (String stickerFileName : stickerPaths) {

                // 解析该文件贴纸id (开发者可自己做对照表，这里根据文件名解析id)
                String groupId = stickerFileName.substring(stickerFileName.lastIndexOf("_") + 1,stickerFileName.lastIndexOf("."));

                File stickerFile = new File(stickerDir,stickerFileName);


                //  将下载后的贴纸加入 TuSDKPFStickerLocalPackage
                boolean result = StickerLocalPackage.shared().addStickerGroupFile(stickerFile, Long.parseLong(groupId),master);

                TLog.e("result" + result);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }


        /**
         * 延伸需求 : 如果开发者要移除指定的贴纸，可通过如下方法实现
         * 注意： TuSDKPFStickerLocalPackage 不负责移除物理贴纸文件，只是移除对贴纸的管理。
         */
        // [[TuSDKPFStickerLocalPackage package] removeDownloadWithIdt:1432];


        /**
         * step 3. 通过 addStickerGroupFile 加入后， 可以通过 TuSDKPFStickerLocalPackage 读取贴纸数据。
         */

//        List<StickerGroup> localList = StickerLocalPackage.shared().getSmartStickerGroups();

    }


    /**
     * 获取所有贴纸分类
     *
     * @return List<PropsItemStickerCategory>
     */
    public static List<PropsItemStickerCategory> allCategories() {

        prepareLocalSticker();

        List<PropsItemStickerCategory> categories = new ArrayList<>();

        try {
            InputStream stream = TuSdkContext.context().getResources().openRawResource(TuSdkContext.getRawResId("customstickercategories"));

            if (stream == null) return null;

            byte buffer[] = new byte[stream.available()];
            stream.read(buffer);
            String json = new String(buffer, "UTF-8");

            JSONObject jsonObject = JsonHelper.json(json);
            JSONArray jsonArray = jsonObject.getJSONArray("categories");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject item = jsonArray.getJSONObject(i);

                // 该分类下的所有贴纸道具
                List<PropsItemSticker> propsItems = new ArrayList<PropsItemSticker>();

                JSONArray jsonArrayGroup = item.getJSONArray("stickers");

                for (int j = 0; j < jsonArrayGroup.length(); j++) {

                    JSONObject itemGroup = jsonArrayGroup.getJSONObject(j);
                    StickerGroup group = new StickerGroup();
                    group.groupId = itemGroup.optLong("id");
                    group.previewName = itemGroup.optString("previewImage");
                    group.name = itemGroup.optString("name");

                    PropsItemSticker propsItem = new PropsItemSticker(group);
                    propsItems.add(propsItem);
                }

                // 该贴纸道具分类
                PropsItemStickerCategory category = new PropsItemStickerCategory(propsItems);
                category.setName(item.getString("categoryName"));

                categories.add(category);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return categories;

    }
}

