package com.faceunity.entity;

/**
 * @author LiuQiang on 2018.11.14
 * 动漫滤镜
 */
public class CartoonFilter {
    // 切换风格滤镜
    /*0. 动漫
1. 素描
2. 人像
3. 油画
4. 沙画
5. 钢笔画
6. 铅笔画
7. 涂鸦
    * */
    public static final int NO_FILTER = -1;
    public static final int COMIC_FILTER = 0;
    public static final int SKETCH_FILTER = 1;
    public static final int PORTRAIT_EFFECT = 2;
    public static final int OIL_PAINTING = 3;
    public static final int SAND_PAINTING = 4;
    public static final int PEN_PAINTING = 5;
    public static final int PENCIL_PAINTING = 6;
    public static final int GRAFFITI = 7;

    private int imageResId;
    private String name;
    private int style;

    public CartoonFilter(int imageResId, String name, int style) {
        this.imageResId = imageResId;
        this.name = name;
        this.style = style;
    }

    public int getImageResId() {
        return imageResId;
    }

    public void setImageResId(int imageResId) {
        this.imageResId = imageResId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStyle() {
        return style;
    }

    public void setStyle(int style) {
        this.style = style;
    }

    @Override
    public String toString() {
        return "CartoonFilter{" +
                "imageResId=" + imageResId +
                ", name='" + name + '\'' +
                ", style=" + style +
                '}';
    }
}
