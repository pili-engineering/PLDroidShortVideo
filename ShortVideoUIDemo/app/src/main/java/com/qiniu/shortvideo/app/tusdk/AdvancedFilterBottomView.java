package com.qiniu.shortvideo.app.tusdk;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;

import com.qiniu.shortvideo.app.R;
import com.qiniu.shortvideo.app.view.BaseBottomView;

import org.lasque.tusdk.core.view.TuSdkViewHelper;
import org.lasque.tusdk.video.editor.TuSdkMediaEffectData;
import org.lasque.tusdk.video.editor.TuSdkMediaFilterEffectData;
import org.lasque.tusdkdemohelper.tusdk.filter.FilterConfigView;

import java.util.Arrays;

public class AdvancedFilterBottomView extends BaseBottomView implements
        AdvancedFilterAdapter.OnFilterItemClickListener {

    private RecyclerView mFilterRecyclerView;
    private AdvancedFilterAdapter mAdvancedFilterAdapter;
    private ImageButton mConfirmBtn;
    private FilterConfigView mAdvancedFilterConfigView;

    protected int mSelectIndex = 0;
    protected TuSdkMediaEffectData mSelectEffectData;

    private OnFilterClickedListener mOnFilterClickedListener;

    public interface OnFilterClickedListener {
        void onFilterClicked(int position, String filterCode);
        void onFilterConfirmClicked();
    }

    public AdvancedFilterBottomView(@NonNull Context context) {
        super(context);
        init();
    }

    public AdvancedFilterBottomView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AdvancedFilterBottomView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setOnFilterClickedListener(OnFilterClickedListener listener) {
        mOnFilterClickedListener = listener;
    }

    public FilterConfigView getFilterConfigView() {
        return mAdvancedFilterConfigView;
    }

    protected void switchFilter(String code) {
        // 切换滤镜前必须打开视频预览, 滤镜切换依赖于视频的编解码
        // 如果视频暂停情况下切换滤镜会导致切换失败，onFilterChanged方法也不会回调
        mSelectEffectData = new TuSdkMediaFilterEffectData(code);
//        getEditorController().getMovieEditor().getEditorEffector().addMediaEffectData(mSelectEffectData);
    }

    @Override
    protected void init() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.editor_advanced_filter_view, this);
        mAdvancedFilterConfigView = view.findViewById(R.id.filter_config_view);
        mConfirmBtn = view.findViewById(R.id.confirm_btn);

        mConfirmBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnFilterClickedListener != null) {
                    mOnFilterClickedListener.onFilterConfirmClicked();
                }
            }
        });

        mFilterRecyclerView = view.findViewById(R.id.advanced_filter_list_view);
        mFilterRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
        mAdvancedFilterAdapter = new AdvancedFilterAdapter();
        mAdvancedFilterAdapter.setOnFilterItemClickListener(this);
        mFilterRecyclerView.setAdapter(mAdvancedFilterAdapter);
        mAdvancedFilterAdapter.setFilterList(Arrays.asList(TuConfig.EDITOR_FILTERS));
    }

    @Override
    public boolean isPlayerNeedZoom() {
        return false;
    }

    @Override
    public void onFilterItemClicked(int position) {
        if (TuSdkViewHelper.isFastDoubleClick()) {
            return;
        }
        if (mSelectIndex == position) {
            return;
        }
        mSelectIndex = position;
        if (mOnFilterClickedListener != null) {
            mOnFilterClickedListener.onFilterClicked(position, mAdvancedFilterAdapter.getFilterList().get(mSelectIndex));
        }
    }
}
