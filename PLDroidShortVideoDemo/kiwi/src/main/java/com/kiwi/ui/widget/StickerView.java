package com.kiwi.ui.widget;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.kiwi.tracker.bean.conf.StickerConfig;
import com.kiwi.ui.OnViewEventListener;
import com.kiwi.ui.R;
import com.kiwi.ui.StickerAdapter;
import com.kiwi.ui.StickerConfigMgr;

import java.util.ArrayList;
import java.util.List;

import static com.kiwi.tracker.bean.conf.StickerConfig.NO_STICKER;

public class StickerView extends FrameLayout implements StickerAdapter.IStickerHandler, StickerAdapter.onStickerChangeListener {
    private RecyclerView mStickerListView;
    private OnViewEventListener onEventListener;
    private StickerAdapter mStickerAdapter;

    private StickerConfigMgr stickerConfigMgr;

    public void setOnEventListener(OnViewEventListener onEventListener) {
        this.onEventListener = onEventListener;
    }

    public StickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    private void init(AttributeSet attrs, int defStyle) {
        this.stickerConfigMgr = new StickerConfigMgr();
        LayoutInflater.from(getContext()).inflate(R.layout.sticker_layout, this);
        mStickerListView = (RecyclerView) findViewById(R.id.sticker_listView);
    }

    @Override
    public void onStickerChanged(StickerConfig item) {
        //切换贴纸
        onEventListener.onStickerChanged(item);
    }


    @Override
    public void writeSticker(StickerConfig stickerConfig) {
        this.stickerConfigMgr.writeStickerConfig(stickerConfig);
    }

    private List<StickerConfig> getStickers() {
        //获取json文件中的贴纸信息
        return this.stickerConfigMgr.readStickerConfig().getStickers();
    }


    /**
     * 贴纸
     */
    public void initStickerListView() {
        mStickerListView.setLayoutManager(new GridLayoutManager(getContext(), 5));
        List<StickerConfig> list = new ArrayList<>();
        list.add(NO_STICKER);

        list.addAll(getStickers());
        mStickerAdapter = new StickerAdapter(getContext(), list, this, this);
        mStickerListView.setAdapter(mStickerAdapter);
        mStickerListView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                List<StickerConfig> mValues = mStickerAdapter.getValues();
                //得到当前显示的最后一个item的view
                RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
                View lastChildView = layoutManager.getChildAt(layoutManager.getChildCount() - 1);

                int lastChildBottom = lastChildView.getBottom();
                int recyclerBottom = recyclerView.getBottom() - recyclerView.getPaddingBottom();

                int lastPosition = layoutManager.getPosition(lastChildView);
                if (lastChildBottom == recyclerBottom && lastPosition == layoutManager.getItemCount() - 1) {
                    //滑动到底部
                    int location = mValues.size() - 1;
                    if (mStickerAdapter.isLoading(mValues.get(location)) && !mValues.get(location).isDownloaded()) {
                        mStickerAdapter.notifyItemChanged(location);
                    }
                }

                StickerConfig sticker1 = mValues.get(1);
                if (mStickerAdapter.isLoading(sticker1) && !sticker1.isDownloaded()) {
                    mStickerAdapter.notifyItemChanged(1);
                }

            }
        });
    }
}
