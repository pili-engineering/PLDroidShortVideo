package org.lasque.tusdkdemohelper.tusdk;

import android.graphics.Bitmap;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import org.lasque.tusdk.core.TuSdkContext;
import org.lasque.tusdk.core.view.TuSdkImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * 滤镜适配器
 * @author xujie
 * @Date 2018/9/18
 */

public class FilterRecyclerAdapter extends RecyclerView.Adapter<FilterRecyclerAdapter.FilterViewHolder>{

    // 滤镜列表
    private List<String> mFilterString;
    // 当前选中
    private int mCurrentPosition = -1;
    // 是否显示调节图
    private boolean isShowParameter = true;

    public interface ItemClickListener{
        void onItemClick(int position);
    }
    public ItemClickListener listener;

    public void setItemCilckListener(ItemClickListener listener){
        this.listener = listener;
    }

    public FilterRecyclerAdapter() {
        super();
        mFilterString = new ArrayList<>();
    }

    public void setFilterList(List<String> filterList){
        this.mFilterString = filterList;
        notifyDataSetChanged();
    }

    public void setCurrentPosition(int position){
        this.mCurrentPosition = position;
        notifyDataSetChanged();
    }

    public List<String> getFilterList(){
        return this.mFilterString;
    }

    /**
     * 设置是否显示调节图
     * @param isShow
     */
    public void isShowImageParameter(boolean isShow){
        this.isShowParameter = isShow;
    }

    @Override
    public int getItemCount() {
        return mFilterString.size();
    }

    @Override
    public FilterViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(TuSdkContext.getLayoutResId("tusdk_filter_recycler_item_view"),null);
        FilterViewHolder viewHolder = new FilterViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(FilterViewHolder filterViewHolder, final int position) {
        String filterCode = mFilterString.get(position);
        String imageCode = filterCode.toLowerCase().replaceAll("_","");
        String filterImageName = getThumbPrefix() + imageCode;
        filterViewHolder.mImageLayout.setVisibility(View.VISIBLE);
        if(position == 0){
            filterViewHolder.mNoneLayout.setVisibility(View.VISIBLE);
            filterViewHolder.mTitleView.setVisibility(View.GONE);
            filterViewHolder.mSelectLayout.setVisibility(View.GONE);
            filterViewHolder.mImageLayout.setVisibility(View.GONE);
        }else if(position == mCurrentPosition){
            filterViewHolder.mNoneLayout.setVisibility(View.GONE);
            filterViewHolder.mTitleView.setVisibility(View.GONE);
            filterViewHolder.mSelectLayout.setVisibility(View.VISIBLE);
            if(!isShowParameter){
                filterViewHolder.mImageParameter.setVisibility(View.GONE);
            }
        }else{
            filterViewHolder.mNoneLayout.setVisibility(View.GONE);
            filterViewHolder.mTitleView.setVisibility(View.VISIBLE);
            filterViewHolder.mSelectLayout.setVisibility(View.GONE);

            filterViewHolder.mTitleView.setText(TuSdkContext.getString(getTextPrefix()+ filterCode));
        }
        Bitmap filterImage = TuSdkContext.getRawBitmap(filterImageName);
        if (filterImage != null)
        {
            filterViewHolder.mItemImage.setImageBitmap(filterImage);
        }
        // 反馈点击
        filterViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listener != null)
                    listener.onItemClick(position);
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
    protected String getThumbPrefix()
    {
        return "lsq_filter_thumb_";
    }

    /**
     * Item名称前缀
     *
     * @return
     */
    protected String getTextPrefix()
    {
        return "lsq_filter_";
    }

    class FilterViewHolder extends RecyclerView.ViewHolder{

        public TextView mTitleView;
        public TuSdkImageView mItemImage;
        public FrameLayout mSelectLayout;
        public FrameLayout mNoneLayout;
        public RelativeLayout mImageLayout;
        public ImageView mImageParameter;

        public FilterViewHolder(View itemView) {
            super(itemView);
            mTitleView = (TextView)itemView.findViewById(TuSdkContext.getIDResId("lsq_item_title"));
            mItemImage = (TuSdkImageView) itemView.findViewById(TuSdkContext.getIDResId("lsq_item_image"));
            mSelectLayout = (FrameLayout) itemView.findViewById(TuSdkContext.getIDResId("lsq_select_layout"));
            mNoneLayout = (FrameLayout) itemView.findViewById(TuSdkContext.getIDResId("lsq_none_layout"));
            mImageLayout = (RelativeLayout) itemView.findViewById(TuSdkContext.getIDResId("lsq_image_layout"));
            mImageParameter = (ImageView) itemView.findViewById(TuSdkContext.getIDResId("lsq_filter_parameter"));
            mItemImage.setCornerRadiusDP(5);
        }
    }
}
