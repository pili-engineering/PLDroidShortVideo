package org.lasque.tusdkdemohelper.tusdk;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import org.lasque.tusdk.core.TuSdkContext;
import org.lasque.tusdk.core.secret.TuSDKOnlineStickerDownloader;
import org.lasque.tusdk.core.type.DownloadTaskStatus;
import org.lasque.tusdk.modules.view.widget.sticker.StickerGroup;
import org.lasque.tusdk.modules.view.widget.sticker.StickerLocalPackage;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xujie
 * @Date 2018/10/25
 */

public class StickerRecyclerAdapter extends RecyclerView.Adapter<StickerRecyclerAdapter.StickerViewHolder>
        implements TuSDKOnlineStickerDownloader.TuSDKOnlineStickerDownloaderDelegate {

    private Context mContext;
    /**
     * 数据集合
     */
    public List<StickerGroup> mStickerGroupList;
    /**
     * 当前选中
     */
    private int mCurrentPosition = -1;
    private int mCurrentLongPos = -1;
    /**
     * 贴纸下载器
     */
    private TuSDKOnlineStickerDownloader mStickerDownloader;

    // 下载回调
    public ItemDownStateListener downStateListener;

    public interface ItemDownStateListener {
        void onDownState(int position, float progress, DownloadTaskStatus status);
    }

    public void setItemDownStateListener(ItemDownStateListener listener) {
        this.downStateListener = listener;
    }

    // 点击回调
    public ItemClickListener listener;

    public interface ItemClickListener {
        void onItemClick(int position);
    }

    public void setItemClickListener(ItemClickListener listener) {
        this.listener = listener;
    }

    // 点击回调
    public ItemDeleteListener itemDeleteListener;

    public interface ItemDeleteListener {
        void onItemDelete(int position);
    }

    public void setItemDeleteListener(ItemDeleteListener listener) {
        this.itemDeleteListener = listener;
    }

    public StickerRecyclerAdapter(Context context) {
        super();
        this.mContext = context;
        mStickerGroupList = new ArrayList<>();
        mStickerDownloader = new TuSDKOnlineStickerDownloader();
        mStickerDownloader.setDelegate(this);
    }

    public void setStickerGroupList(List<StickerGroup> list) {
        this.mStickerGroupList = list;
    }

    public void setSelectedPosition(int position) {
        this.mCurrentPosition = position;
        notifyDataSetChanged();
    }

    public TuSDKOnlineStickerDownloader getStickerDownloader() {
        return mStickerDownloader;
    }

    public List<StickerGroup> getStickerGroupList() {
        return mStickerGroupList;
    }

    @Override
    public int getItemCount() {
        return mStickerGroupList.size();
    }

    @Override
    public StickerViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(TuSdkContext.getLayoutResId("tusdk_sticker_list_cell_view"), null);
        StickerViewHolder viewHolder = new StickerViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(StickerViewHolder stickerViewHolder, final int position) {
        StickerGroup model = mStickerGroupList.get(position);

        hideProgressAnimation(stickerViewHolder.mLoadProgressImage);

        if (model == null) return;

        // 已下载到本地
        boolean isContains = StickerLocalPackage.shared().containsGroupId(model.groupId);
        if (isContains) {
            model = StickerLocalPackage.shared().getStickerGroup(model.groupId);
            StickerLocalPackage.shared().loadGroupThumb(model, stickerViewHolder.mItemImage);
            stickerViewHolder.mLoadProgressImage.setVisibility(View.GONE);
            stickerViewHolder.mDownStateImage.setVisibility(View.GONE);
        } else if (isDownloading(position)) {
            stickerViewHolder.mDownStateImage.setVisibility(View.GONE);
            stickerViewHolder.mLoadProgressImage.setVisibility(View.VISIBLE);
//            StickerLocalPackage.shared().loadGroupThumb(model,stickerViewHolder.mItemImage);
            Glide.with(mContext)
                    .asBitmap()
                    .load(model.getPreviewNamePath())
                    .into(stickerViewHolder.mItemImage);
            showProgressAnimation(stickerViewHolder.mLoadProgressImage);
        } else {
            if (mContext == null) return;
//            StickerLocalPackage.shared().loadGroupThumb(model,stickerViewHolder.mItemImage);
            Glide.with(mContext).asBitmap()
                    .load(model.getPreviewNamePath()).into(stickerViewHolder.mItemImage);
            stickerViewHolder.mDownStateImage.setVisibility(View.VISIBLE);
            stickerViewHolder.mLoadProgressImage.setVisibility(View.GONE);
        }

        /** 点击选中处理 */
        if (position == mCurrentPosition) {
            stickerViewHolder.mItemWarp.setBackground(TuSdkContext.getDrawable("tusdk_sticker_cell_background"));
        } else {
            stickerViewHolder.mItemWarp.setBackground(null);
        }

        /** 长按后UI处理 */
        if (position == mCurrentLongPos && isContains && position == mCurrentLongPos) {
            stickerViewHolder.mItemWarp.setBackground(TuSdkContext.getDrawable("tusdk_sticker_cell_remove_background"));
            stickerViewHolder.mRemoveStickerImage.setVisibility(View.VISIBLE);
        } else {
            stickerViewHolder.mRemoveStickerImage.setVisibility(View.GONE);
        }

        /** 点击选中事件 */
        stickerViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                StickerGroup itemData = getStickerGroupList().get(position);
                // 如果贴纸已被下载到本地
                if (mStickerDownloader.isDownloaded(itemData.groupId)) {
                    if (mCurrentLongPos == position) {
                        removeSticker(position);
                        return;
                    } else {

                        // 必须重新获取StickerGroup,否则itemData.stickers为null
                        if (listener != null && mCurrentPosition != position)
                            listener.onItemClick(position);
                    }
                } else {
                    mStickerDownloader.downloadStickerGroup(itemData);
                }
                mCurrentLongPos = -1;
                mCurrentPosition = position;
                notifyDataSetChanged();
            }
        });

        /** 点击长按事件 */
        stickerViewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                notifyItemChanged(mCurrentLongPos);
                mCurrentLongPos = position;
                notifyItemChanged(position);
                return true;
            }
        });
    }

    /**
     * 移除贴纸
     *
     * @param position
     */
    private void removeSticker(final int position) {
        AlertDialog.Builder adBuilder = new AlertDialog.Builder(mContext, android.R.style.Theme_Material_Dialog_Alert);
        adBuilder.setTitle("确认删除本地文件?");
        adBuilder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        adBuilder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                StickerGroup itemData = getStickerGroupList().get(position);
                if (position == mCurrentLongPos && mStickerDownloader.isDownloaded(itemData.groupId)) {
                    StickerLocalPackage.shared().removeDownloadWithIdt(itemData.groupId);
                    mCurrentLongPos = -1;
                    notifyItemChanged(position);
                    if (itemDeleteListener != null) itemDeleteListener.onItemDelete(position);
                }
                dialog.dismiss();
            }
        });
        adBuilder.show();
    }

    /**
     * 显示进度动画
     */
    public void showProgressAnimation(ImageView view) {
        if (view == null) return;

        view.setVisibility(View.VISIBLE);
        RotateAnimation rotate = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF,
                0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        LinearInterpolator lin = new LinearInterpolator();
        rotate.setInterpolator(lin);
        rotate.setDuration(2000);
        rotate.setRepeatCount(-1);
        rotate.setFillAfter(true);

        view.setAnimation(rotate);

    }

    /**
     * 隐藏进度显示动画
     */
    public void hideProgressAnimation(ImageView view) {
        if (view == null) return;

        view.clearAnimation();
        view.setVisibility(View.GONE);
    }

    @Override
    public void onDownloadProgressChanged(long stickerGroupId, float progress, DownloadTaskStatus status) {
        if (status == DownloadTaskStatus.StatusDowned || status == DownloadTaskStatus.StatusDownFailed) {
            int position = getStickerCellViewPosition(stickerGroupId);

            if (position != -1 && mCurrentPosition == position)
                if (listener != null) listener.onItemClick(position);
            notifyItemChanged(position);
        }
    }

    /**
     * 获取贴纸的位置
     *
     * @param groupId
     * @return
     */
    public int getStickerCellViewPosition(long groupId) {
        List<StickerGroup> groups = mStickerGroupList;

        if (groups == null) return -1;

        for (int i = 0; i < groups.size(); i++) {
            StickerGroup group = groups.get(i);
            if (group.groupId == groupId)
                return i;
        }
        return -1;
    }

    /**
     * 贴纸是否正在下载
     *
     * @return
     */
    public boolean isDownloading(int position) {
        return mStickerDownloader != null && mStickerDownloader.containsTask(mStickerGroupList.get(position).groupId);
    }

    class StickerViewHolder extends RecyclerView.ViewHolder {

        public ImageView mItemImage;
        public ImageView mLoadProgressImage;
        public ImageView mDownStateImage;
        public ImageView mRemoveStickerImage;
        public View mItemWarp;

        public StickerViewHolder(View itemView) {
            super(itemView);
            mItemImage = (ImageView) itemView.findViewById(TuSdkContext.getIDResId("lsq_item_image"));
            mLoadProgressImage = (ImageView) itemView.findViewById(TuSdkContext.getIDResId("lsq_progress_image"));
            mDownStateImage = (ImageView) itemView.findViewById(TuSdkContext.getIDResId("lsq_item_state_image"));
            mItemWarp = itemView.findViewById(TuSdkContext.getIDResId("lsq_item_wrap"));
            mRemoveStickerImage = (ImageView) itemView.findViewById(TuSdkContext.getIDResId("lsq_item_remove"));
        }
    }
}
