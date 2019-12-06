package org.lasque.tusdkdemohelper.tusdk;

import org.lasque.tusdk.modules.view.widget.sticker.StickerGroup;

import java.io.Serializable;
import java.util.List;

/**
 * @author xujie
 * @Date 2018/9/27
 */

public class StickerGroupCategories implements Serializable {

    private String categoryName;
    private List<StickerGroup> stickerGroupList;

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public List<StickerGroup> getStickerGroupList() {
        return stickerGroupList;
    }

    public void setStickerGroupList(List<StickerGroup> stickerGroupList) {
        this.stickerGroupList = stickerGroupList;
    }
}
