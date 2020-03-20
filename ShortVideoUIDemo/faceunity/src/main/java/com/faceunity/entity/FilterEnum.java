package com.faceunity.entity;

import com.faceunity.R;

import java.util.ArrayList;

/**
 * Created by tujh on 2018/1/30.
 */
public enum FilterEnum {

    /**
     * 滤镜资源
     */
    nature(Filter.Key.ORIGIN, R.drawable.nature, R.string.origin),
    bailiang(Filter.Key.BAILIANG_2, R.drawable.bailiang2, R.string.bailiang),
    fennen(Filter.Key.FENNEN_1, R.drawable.fennen1, R.string.fennen),
    xiaoqingxin(Filter.Key.XIAOQINGXIN_6, R.drawable.xiaoqingxin6, R.string.qingxin),
    lengsediao(Filter.Key.LENGSEDIAO_1, R.drawable.lengsediao1, R.string.lengsediao),
    nuansediao(Filter.Key.NUANSEDIAO_1, R.drawable.nuansediao1, R.string.nuansediao);

    /*
    slowlived(Filter.Key.SLOWLIVED, R.drawable.slowlived, R.string.slowlived, Filter.FILTER_TYPE_FILTER),
    qingxin(Filter.Key.QINGXIN, R.drawable.ziran, R.string.qingxin, Filter.FILTER_TYPE_FILTER),
    warm(Filter.Key.WARM, R.drawable.warm, R.string.warm, Filter.FILTER_TYPE_FILTER),

    delta(Filter.Key.DELTA, R.drawable.delta, R.string.delta, Filter.FILTER_TYPE_FILTER),
    electric(Filter.Key.ELECTRIC, R.drawable.electric, R.string.electric, Filter.FILTER_TYPE_FILTER),
    tokyo(Filter.Key.TOKYO, R.drawable.tokyo, R.string.tokyo, Filter.FILTER_TYPE_FILTER),

    nature_beauty(Filter.Key.ORIGIN, R.drawable.nature, R.string.origin_beauty, Filter.FILTER_TYPE_FILTER),
    ziran(Filter.Key.ZIRAN, R.drawable.origin, R.string.ziran, Filter.FILTER_TYPE_FILTER),
    danya(Filter.Key.DANYA, R.drawable.qingxin, R.string.danya, Filter.FILTER_TYPE_FILTER),
    hongrun(Filter.Key.HONGRUN, R.drawable.hongrun, R.string.hongrun, Filter.FILTER_TYPE_FILTER);
    */

    private String filterName;
    private int resId;
    private int description;

    FilterEnum(String name, int resId, int description) {
        this.filterName = name;
        this.resId = resId;
        this.description = description;
    }

    public String filterName() {
        return filterName;
    }

    public int resId() {
        return resId;
    }

    public int description() {
        return description;
    }

    public static ArrayList<Filter> getFiltersByFilterType() {
        FilterEnum[] values = FilterEnum.values();
        ArrayList<Filter> filters = new ArrayList<>(values.length);
        for (FilterEnum f : values) {
            filters.add(f.filter());
        }
        return filters;
    }

    public Filter filter() {
        return new Filter(filterName, resId, description);
    }
}
