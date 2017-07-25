package com.kiwi.ui;


import com.blankj.utilcode.utils.FileUtils;
import com.google.gson.Gson;
import com.kiwi.tracker.bean.conf.StickerConfig;
import com.kiwi.tracker.common.Config;

import java.io.File;

public class StickerConfigMgr {

    //the selected stickerConfig,used in StickerAdapter
    private static StickerConfig selectedStickerConfig = null;

    public static synchronized StickerConfig getSelectedStickerConfig() {
        return selectedStickerConfig;
    }

    public static synchronized void setSelectedStickerConfig(StickerConfig stickerConfig) {
        selectedStickerConfig = stickerConfig;
    }

    /**
     * load stickers from stickers.json
     *
     * @return stickers
     */
    public static synchronized StickerSetConfig readStickerConfig() {
        File file = new File(Config.getStickerConfigPath());
        String jsonStr = FileUtils.readFile2String(file, Config.UTF_8);
        StickerSetConfig stickerSetConfig = new Gson().fromJson(jsonStr, StickerSetConfig.class);
        return stickerSetConfig;
    }

    /**
     * add or replace stickers to stickers.json
     *
     * @param sticker new sticker
     */
    public static synchronized void writeStickerConfig(StickerConfig sticker) {
        StickerSetConfig stickerSetConfig = readStickerConfig();
        StickerConfig finded = stickerSetConfig.findSticker(sticker.getName());
        if (finded == null) {
            stickerSetConfig.addItem(sticker);
        } else {
            finded.update(sticker);
        }


        String json = new Gson().toJson(stickerSetConfig);
        File file = new File(Config.getStickerConfigPath());

        FileUtils.writeFileFromString(file, json, false);
    }

    /**
     * delete sticker to stickers.json
     *
     * @param sticker removed sticker
     */
    public static synchronized void removeStickerConfig(StickerConfig sticker) {
        StickerSetConfig stickerSetConfig = readStickerConfig();
        StickerConfig finded = stickerSetConfig.findSticker(sticker.getName());
        if (finded != null) {
            stickerSetConfig.removeItem(sticker);
        }

        String json = new Gson().toJson(stickerSetConfig);
        File file = new File(Config.getStickerConfigPath());

        FileUtils.writeFileFromString(file, json, false);
    }
}
