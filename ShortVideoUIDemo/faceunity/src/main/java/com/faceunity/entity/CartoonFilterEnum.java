package com.faceunity.entity;

import com.faceunity.R;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LiuQiang on 2018.11.14
 */
public enum CartoonFilterEnum {
    /**
     * 风格滤镜
     */
    NO_FILTER(R.drawable.ic_delete_all, "无效果", CartoonFilter.NO_FILTER),
    COMIC_FILTER(R.drawable.icon_animefilter, "漫画滤镜", CartoonFilter.COMIC_FILTER),
    PORTRAIT_EFFECT(R.drawable.icon_portrait_dynamiceffect, "人像动效", CartoonFilter.PORTRAIT_EFFECT),
    SKETCH_FILTER(R.drawable.icon_sketchfilter, "素描滤镜", CartoonFilter.SKETCH_FILTER),
    OIL_PAINT_FILTER(R.drawable.icon_oilpainting, "油画", CartoonFilter.OIL_PAINTING),
    SAND_PAINT_FILTER(R.drawable.icon_sandlpainting, "沙画", CartoonFilter.SAND_PAINTING),
    PEN_PAINT_FILTER(R.drawable.icon_penpainting, "钢笔画", CartoonFilter.PEN_PAINTING),
    PENCIL_PAINT_FILTER(R.drawable.icon_pencilpainting, "铅笔画", CartoonFilter.PENCIL_PAINTING),
    GRAFFITI_FILTER(R.drawable.icon_graffiti, "涂鸦", CartoonFilter.GRAFFITI);

    private int imageResId;
    private String name;
    private int style;

    CartoonFilterEnum(int imageResId, String name, int style) {
        this.imageResId = imageResId;
        this.name = name;
        this.style = style;
    }

    public static List<CartoonFilter> getAllCartoonFilters() {
        CartoonFilterEnum[] values = values();
        List<CartoonFilter> cartoonFilters = new ArrayList<>(values.length);
        for (CartoonFilterEnum value : values) {
            cartoonFilters.add(new CartoonFilter(value.imageResId, value.name, value.style));
        }
        return cartoonFilters;
    }
}
