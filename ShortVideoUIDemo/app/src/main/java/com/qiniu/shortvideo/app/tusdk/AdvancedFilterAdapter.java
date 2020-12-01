package com.qiniu.shortvideo.app.tusdk;

import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.qiniu.shortvideo.app.R;

import org.lasque.tusdk.core.TuSdkContext;
import org.lasque.tusdk.core.view.TuSdkImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * 编辑模块高级滤镜的适配器
 */
public class AdvancedFilterAdapter extends RecyclerView.Adapter<AdvancedFilterAdapter.FilterViewHolder> {

    // 滤镜列表
    private List<String> mFilterString;
    // 当前选中
    private int mCurrentPosition = -1;
    // 是否显示调节图
    private boolean isShowParameter = true;

    public interface OnFilterItemClickListener {
        void onFilterItemClicked(int position);
    }

    public OnFilterItemClickListener listener;

    public void setOnFilterItemClickListener(OnFilterItemClickListener listener) {
        this.listener = listener;
    }

    public AdvancedFilterAdapter() {
        super();
        mFilterString = new ArrayList<>();
    }

    public void setFilterList(List<String> filterList) {
        this.mFilterString = filterList;
        notifyDataSetChanged();
    }

    /**
     * 设置当前选中
     *
     * @param position
     */
    public void setCurrentPosition(int position) {
        this.mCurrentPosition = position;
        notifyDataSetChanged();
    }

    /**
     * 获取滤镜列表
     *
     * @return
     */
    public List<String> getFilterList() {
        return this.mFilterString;
    }


    /**
     * 设置是否显示调节图
     *
     * @param isShow
     */
    public void isShowImageParameter(boolean isShow) {
        this.isShowParameter = isShow;
    }

    @Override
    public int getItemCount() {
        return mFilterString.size();
    }

    @Override
    public FilterViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_advanced_filter, null);
        FilterViewHolder viewHolder = new FilterViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(FilterViewHolder filterViewHolder, final int position) {
        String filterCode = mFilterString.get(position);
        // 相应的资源文件放在 raw 目录下
        String imageCode = filterCode.toLowerCase().replaceAll("_", "");
        String filterImageName = getThumbPrefix() + imageCode;
        filterViewHolder.mImageLayout.setVisibility(View.VISIBLE);
        if (position == 0) {
            filterViewHolder.mNoneLayout.setVisibility(View.VISIBLE);
            filterViewHolder.mTitleView.setVisibility(View.GONE);
            filterViewHolder.mSelectLayout.setVisibility(View.GONE);
            filterViewHolder.mImageLayout.setVisibility(View.GONE);
        } else if (position == mCurrentPosition) {
            filterViewHolder.mNoneLayout.setVisibility(View.GONE);
            filterViewHolder.mTitleView.setVisibility(View.GONE);
            filterViewHolder.mSelectLayout.setVisibility(View.VISIBLE);
            if (!isShowParameter) {
                filterViewHolder.mImageParameter.setVisibility(View.GONE);
            }
        } else {
            filterViewHolder.mNoneLayout.setVisibility(View.GONE);
            filterViewHolder.mTitleView.setVisibility(View.VISIBLE);
            filterViewHolder.mSelectLayout.setVisibility(View.GONE);

            filterViewHolder.mTitleView.setText(TuSdkContext.getString(getTextPrefix() + filterCode));
        }
        Bitmap filterImage = TuSdkContext.getRawBitmap(filterImageName);
        if (filterImage != null) {
            filterViewHolder.mItemImage.setImageBitmap(filterImage);
        }
        // 反馈点击
        filterViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onFilterItemClicked(position);
                }
                notifyItemChanged(mCurrentPosition);
                notifyItemChanged(position);
                mCurrentPosition = position;
            }
        });
        filterViewHolder.itemView.setTag(position);
    }

    /**
     * 缩略图前缀
     *
     * @return
     */
    protected String getThumbPrefix() {
        return "lsq_filter_thumb_";
    }

    /**
     * Item名称前缀
     *
     * @return
     */
    protected String getTextPrefix() {
        return "lsq_filter_";
    }

    class FilterViewHolder extends RecyclerView.ViewHolder {

        TextView mTitleView;
        TuSdkImageView mItemImage;
        FrameLayout mSelectLayout;
        FrameLayout mNoneLayout;
        RelativeLayout mImageLayout;
        ImageView mImageParameter;

        public FilterViewHolder(View itemView) {
            super(itemView);
            mTitleView = itemView.findViewById(R.id.item_title);
            mItemImage = itemView.findViewById(R.id.lsq_item_image);
            mSelectLayout = itemView.findViewById(R.id.select_layout);
            mNoneLayout = itemView.findViewById(R.id.none_layout);
            mImageLayout = itemView.findViewById(R.id.image_layout);
            mImageParameter = itemView.findViewById(R.id.filter_parameter);
            mItemImage.setCornerRadiusDP(5);
        }
    }
}
