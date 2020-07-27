package com.qiniu.shortvideo.app.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import com.qiniu.shortvideo.app.R;

/**
 * 编辑模块 MV、滤镜等效果添加的视图
 */
public class ListBottomView extends BaseBottomView {

    private RecyclerView mRecyclerView;

    public ListBottomView(@NonNull Context context) {
        this(context, null);
    }

    public ListBottomView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ListBottomView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void init(RecyclerView.Adapter adapter) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_list_bottom_view, this);
        mRecyclerView = view.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
        mRecyclerView.setAdapter(adapter);
    }

    @Override
    protected void init() {
    }

    @Override
    public boolean isPlayerNeedZoom() {
        return false;
    }
}
