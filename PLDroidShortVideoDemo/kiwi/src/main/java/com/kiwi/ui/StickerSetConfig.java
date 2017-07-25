package com.kiwi.ui;

import com.kiwi.tracker.bean.conf.StickerConfig;

import java.util.ArrayList;
import java.util.List;

public class StickerSetConfig {

    List<StickerConfig> stickers = new ArrayList();

    public void addItem(StickerConfig sticker) {
        stickers.add(sticker);
    }

    public List<StickerConfig> getStickers() {
        return stickers;
    }

    public StickerConfig findSticker(String name) {
        for(StickerConfig sticker :stickers ){
            if(name.equals(sticker.getName())){
                return sticker;
            }
        }
        return null;
    }

    public void removeItem(StickerConfig sticker) {
        stickers.remove(sticker);
    }
}
