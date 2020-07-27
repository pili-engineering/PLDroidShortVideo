package com.qiniu.shortvideo.app.tusdk;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.makeramen.roundedimageview.RoundedImageView;
import com.qiniu.shortvideo.app.R;

import org.lasque.tusdk.core.TuSdkContext;
import org.lasque.tusdk.core.utils.TLog;
import org.lasque.tusdk.core.view.TuSdkImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * 场景特效适配器
 *
 * @author xujie
 * @Date 2018/9/18
 */

public class SceneRecyclerAdapter extends RecyclerView.Adapter<SceneRecyclerAdapter.SceneViewHolder> {

    private List<String> mScreenString;
    private int mCurrentPosition = -1;
    private boolean isCanDeleted = false;

    public interface ItemClickListener {
        void onItemClick(int position);
    }

    public interface OnItemTouchListener{
        void onItemTouch(MotionEvent event, int position, SceneViewHolder ScreenViewHolder);
    }

    public ItemClickListener listener;
    public OnItemTouchListener onItemTouchListener;

    public void setItemCilckListener(ItemClickListener listener) {
        this.listener = listener;
    }

    public void setOnItemTouchListener(OnItemTouchListener onItemTouchListener){
        this.onItemTouchListener = onItemTouchListener;
    }

    public SceneRecyclerAdapter() {
        super();
        mScreenString = new ArrayList<>();
    }

    public void setSceneList(List<String> ScreenList) {
        this.mScreenString = ScreenList;
        notifyDataSetChanged();
    }

    public void setCurrentPosition(int position) {
        this.mCurrentPosition = position;
        notifyDataSetChanged();
    }

    public boolean isCanDeleted(){
        return isCanDeleted;
    }

    public void setCanDeleted(boolean canDeleted){
        this.isCanDeleted = canDeleted;
    }

    public List<String> getScreenList() {
        return this.mScreenString;
    }

    @Override
    public int getItemCount() {
        return mScreenString.size();
    }

    @Override
    public SceneViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_scene_view, null);
        SceneViewHolder viewHolder = new SceneViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final SceneViewHolder ScreenViewHolder, final int position) {
        String screenCode = mScreenString.get(position);
        String screenImageCode = screenCode.toLowerCase();
        String screenImageName = getThumbPrefix() + screenImageCode;
        ScreenViewHolder.mImageLayout.setVisibility(View.VISIBLE);
        ScreenViewHolder.mNoneLayout.setVisibility(View.GONE);
        ScreenViewHolder.mTitleView.setVisibility(View.VISIBLE);
        ScreenViewHolder.mSelectLayout.setVisibility(View.VISIBLE);
        ScreenViewHolder.mTitleView.setText(TuSdkContext.getString(getTextPrefix() + screenCode));
        int screenId = TuSdkContext.getDrawableResId(screenImageName);
//            SceneViewHolder.mThumbImageView.setImageResource(screenId);
        //设置图片圆角角度
        RoundedCorners roundedCorners = new RoundedCorners(TuSdkContext.dip2px(8));
        RequestOptions options = RequestOptions.bitmapTransform(roundedCorners).override(ScreenViewHolder.mItemImage.getWidth(), ScreenViewHolder.mItemImage.getHeight());
        Glide.with(ScreenViewHolder.mItemImage.getContext()).asGif().load(screenId).apply(options).into(ScreenViewHolder.mItemImage);

        // 反馈点击
        ScreenViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null)
                    listener.onItemClick(position);

            }
        });

        ScreenViewHolder.itemView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(onItemTouchListener == null) return false;
                onItemTouchListener.onItemTouch(event, position, ScreenViewHolder);
                return false;
            }
        });

        ScreenViewHolder.itemView.setTag(position);
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

    public String getSceneCode(int position){
        if(mScreenString == null && mScreenString.size() < position)return "None";
        return mScreenString.get(position);
    }
    public class SceneViewHolder extends RecyclerView.ViewHolder {

        public TextView mTitleView;
        public TuSdkImageView mItemImage;
        public RoundedImageView mSelectLayout;
        public FrameLayout mNoneLayout;
        public RelativeLayout mImageLayout;

        public SceneViewHolder(View itemView) {
            super(itemView);
            mTitleView = itemView.findViewById(R.id.lsq_item_title);
            mItemImage = itemView.findViewById(R.id.lsq_item_image);
            mSelectLayout = itemView.findViewById(R.id.lsq_select_layout);
            mNoneLayout = itemView.findViewById(R.id.lsq_none_layout);
            mImageLayout = itemView.findViewById(R.id.lsq_image_layout);
        }
    }
}
