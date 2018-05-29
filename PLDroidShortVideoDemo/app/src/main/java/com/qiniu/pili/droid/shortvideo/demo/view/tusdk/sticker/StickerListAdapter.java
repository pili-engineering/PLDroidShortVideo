package com.qiniu.pili.droid.shortvideo.demo.view.tusdk.sticker;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.qiniu.pili.droid.shortvideo.demo.R;

import org.lasque.tusdk.core.TuSdkContext;
import org.lasque.tusdk.core.secret.TuSDKOnlineStickerDownloader;
import org.lasque.tusdk.core.type.DownloadTaskStatus;
import org.lasque.tusdk.core.view.listview.TuSdkListSelectableCellViewInterface;
import org.lasque.tusdk.modules.view.widget.sticker.StickerGroup;
import org.lasque.tusdk.modules.view.widget.sticker.StickerLocalPackage;

import java.util.ArrayList;
import java.util.List;

public class StickerListAdapter extends RecyclerView.Adapter<StickerListAdapter.StickerHolder> {
    private List<StickerGroup> list;
    private Context mContext;
    /**
     * 当前选中位置
     */
    private int mSelectedPosition = -1;


    public StickerListAdapter() {
        list = new ArrayList<>();
    }

    public void setStickerList(List<StickerGroup> list) {
        this.list = list;
        notifyDataSetChanged();
    }


    @Override
    public StickerHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View stickerView = inflater.inflate(R.layout.sticker_list_view, null);
        StickerHolder viewHolder = new StickerHolder(stickerView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final StickerHolder holder, int position) {
        StickerGroup model = list.get(position);
        holder.setSelectedPosition(mSelectedPosition, position);

        holder.hideProgressAnimation();

        final StickerGroup finalModel = model;
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onClickItem(finalModel, holder, holder.getLayoutPosition());
                }
            }
        });

        // 贴纸栏上的禁用按钮
        if (position == 0) {
            holder.lsq_item_image.setImageResource(TuSdkContext.getDrawableResId("lsq_style_default_btn_sticker_off"));
            holder.lsq_item_state_image.setVisibility(View.GONE);
            return;
        } else {
            // 设置下载器实例，StickerCellView 用于判断该贴纸是否已被下载
            holder.setStickerDownloader(getStickerDownLoader());
        }

        // 已下载到本地
        boolean isContains = StickerLocalPackage.shared().containsGroupId(model.groupId);

        if (isContains) {
            model = StickerLocalPackage.shared().getStickerGroup(model.groupId);
            StickerLocalPackage.shared().loadGroupThumb(model, holder.lsq_item_image);
            holder.lsq_item_state_image.setVisibility(View.GONE);
            holder.lsq_progress_image.setVisibility(View.GONE);
        } else if (holder.isDownlowding(model.groupId)) {
            holder.lsq_item_state_image.setVisibility(View.GONE);
            holder.lsq_progress_image.setVisibility(View.VISIBLE);
            Glide.with(mContext).load(model.getPreviewNamePath()).asBitmap().into(holder.lsq_item_image);
            holder.showProgressAnimation();
        } else {
            if (mContext == null) return;

            Glide.with(mContext).load(model.getPreviewNamePath()).asBitmap().into(holder.lsq_item_image);
            holder.lsq_item_state_image.setVisibility(View.VISIBLE);
            holder.lsq_progress_image.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class StickerHolder extends RecyclerView.ViewHolder implements TuSdkListSelectableCellViewInterface {
        private TuSDKOnlineStickerDownloader mStickerDownloader;

        public RelativeLayout lsq_item_wrap;
        public ImageView lsq_item_image;
        public ImageView lsq_progress_image;
        public ImageView lsq_item_state_image;

        public StickerHolder(View itemView) {
            super(itemView);
            lsq_item_wrap = (RelativeLayout) itemView.findViewById(R.id.lsq_item_wrap);
            lsq_item_image = (ImageView) itemView.findViewById(R.id.lsq_item_image);
            lsq_progress_image = (ImageView) itemView.findViewById(R.id.lsq_progress_image);
            lsq_item_state_image = (ImageView) itemView.findViewById(R.id.lsq_item_state_image);
        }

        public void setStickerDownloader(TuSDKOnlineStickerDownloader stickerDownloader) {
            this.mStickerDownloader = stickerDownloader;
        }

        /**
         * 贴纸是否正在下载
         *
         * @return
         */
        public boolean isDownlowding(long groupId) {
            return mStickerDownloader != null && mStickerDownloader.containsTask(groupId);
        }

        @Override
        public void onCellSelected(int i) {
            this.lsq_item_wrap.setBackground(TuSdkContext.getDrawable(R.drawable.sticker_cell_background));
        }

        @Override
        public void onCellDeselected() {
            this.lsq_item_wrap.setBackground(null);
        }

        /**
         * 隐藏进度显示动画
         */
        public void hideProgressAnimation() {
            if (this.lsq_progress_image == null) return;

            this.lsq_progress_image.clearAnimation();
            this.lsq_progress_image.setVisibility(View.GONE);
        }

        /**
         * 显示进度动画
         */
        public void showProgressAnimation() {
            if (lsq_progress_image == null) return;

            lsq_progress_image.setVisibility(View.VISIBLE);
            RotateAnimation rotate = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            LinearInterpolator lin = new LinearInterpolator();
            rotate.setInterpolator(lin);
            rotate.setDuration(2000);
            rotate.setRepeatCount(-1);
            rotate.setFillAfter(true);

            lsq_progress_image.setAnimation(rotate);

            lsq_item_state_image.setVisibility(View.GONE);
        }

        /**
         * 当前选中位置
         *
         * @param position the position to set
         */
        public void setSelectedPosition(int mSelectedPosition, int position) {
            if (position < 0) return;

            if (this instanceof TuSdkListSelectableCellViewInterface) {
                boolean isSeleted = (mSelectedPosition == position);
                if (isSeleted && mSelectedPosition != 0) {
                    onCellSelected(position);
                } else {
                    onCellDeselected();
                }
            }
        }

    }

    private TuSDKOnlineStickerDownloader mDownLoader;

    /**
     * 获取贴纸下载器
     *
     * @return TuSDKOnlineStickerDownloader
     */
    public TuSDKOnlineStickerDownloader getStickerDownLoader() {
        if (mDownLoader == null) {
            mDownLoader = new TuSDKOnlineStickerDownloader();
            mDownLoader.setDelegate(new TuSDKOnlineStickerDownloader.TuSDKOnlineStickerDownloaderDelegate() {

                @Override
                public void onDownloadProgressChanged(long stickerGroupId, float progress, DownloadTaskStatus
                        status) {
                    if (status == DownloadTaskStatus.StatusDowned || status == DownloadTaskStatus.StatusDownFailed) {
                        int position = getStickerCellViewPostision(stickerGroupId);
                        notifyItemChanged(position);
                    }
                }
            });
        }
        return mDownLoader;
    }

    /**
     * 获取贴纸的位置
     *
     * @param groupId
     * @return
     */
    public int getStickerCellViewPostision(long groupId) {
        if (list == null) return -1;

        for (int i = 0; i < list.size(); i++) {
            StickerGroup group = list.get(i);
            if (group.groupId == groupId)
                return i;
        }
        return -1;
    }

    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        void onClickItem(StickerGroup itemData, StickerHolder stickerHolder, int position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    /**
     * 当前选中位置 (仅支持TuSdkAdapter<T>)
     *
     * @param position the position to set
     */
    public void setSelectedPosition(int position) {
        this.setSelectedPosition(position, true);
    }

    /**
     * 当前选中位置 (仅支持TuSdkAdapter<T>)
     *
     * @param position   the position to set
     * @param dataChange 是否刷新数据
     */
    public void setSelectedPosition(int position, boolean dataChange) {
        this.mSelectedPosition = position;
        if (dataChange) notifyDataSetChanged();
    }

    /**
     * 判断贴纸是否已被下载到本地
     *
     * @param stickerGroup
     * @return
     */
    public boolean isDownloaded(StickerGroup stickerGroup) {
        return getStickerDownLoader().isDownloaded(stickerGroup.groupId);
    }

    /**
     * 下载指定贴纸，如果贴纸正在下载中则不做任何操作
     *
     * @param stickerGroup 贴纸对象
     */
    public void downloadStickerGroup(StickerGroup stickerGroup, StickerHolder stickerHolder) {
        if (stickerGroup == null || getStickerDownLoader().isDownloading(stickerGroup.groupId))
            return;

        getStickerDownLoader().downloadStickerGroup(stickerGroup);

        stickerHolder.showProgressAnimation();
    }
}
