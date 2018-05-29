package com.qiniu.pili.droid.shortvideo.demo.view.tusdk.sticker;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.qiniu.pili.droid.shortvideo.demo.R;

import org.lasque.tusdk.core.TuSdkContext;
import org.lasque.tusdk.core.secret.TuSDKOnlineStickerDownloader;
import org.lasque.tusdk.core.utils.TLog;
import org.lasque.tusdk.core.view.listview.TuSdkCellRelativeLayout;
import org.lasque.tusdk.core.view.listview.TuSdkListSelectableCellViewInterface;
import org.lasque.tusdk.modules.view.widget.sticker.StickerGroup;
import org.lasque.tusdk.modules.view.widget.sticker.StickerLocalPackage;

public class StickerCellView extends TuSdkCellRelativeLayout<StickerGroup> implements TuSdkListSelectableCellViewInterface {

    /**
     * 缩略图
     */
    private ImageView mThumbView;

    /**
     * 下载图标
     */
    private ImageView mDownloadView;

    // 贴纸选中边框
    private RelativeLayout mStickerBorderView;

    // 上下文对象
    private Context mContext;

    // 下载进度视图
    private ImageView mDownloadProgressView;

    private TuSDKOnlineStickerDownloader mStickerDownloader;

    public StickerCellView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        this.mContext = context;
    }

    public StickerCellView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StickerCellView(Context context) {
        this(context, null);
    }

    public void setStickerDownloader(TuSDKOnlineStickerDownloader stickerDownloader) {
        this.mStickerDownloader = stickerDownloader;
    }

    @Override
    protected void bindModel() {
        TLog.e("bingdModel ==================");
        StickerGroup model = this.getModel();

        hideProgressAnimation();

        if (model == null || getImageView() == null) return;

        final ImageView downloadView = getDownloadImageView();

        // 贴纸栏上的禁用按钮
        if ((Integer) getTag() == 0) {
            getImageView().setImageResource(TuSdkContext.getDrawableResId("lsq_style_default_btn_sticker_off"));
            downloadView.setVisibility(View.GONE);
            return;
        }

        // 已下载到本地
        boolean isContains = StickerLocalPackage.shared().containsGroupId(model.groupId);

        if (isContains) {
            model = StickerLocalPackage.shared().getStickerGroup(model.groupId);
            StickerLocalPackage.shared().loadGroupThumb(model, this.getImageView());
            downloadView.setVisibility(View.GONE);
            getDownloadProgressView().setVisibility(View.GONE);

        } else if (isDownlowding()) {
            downloadView.setVisibility(View.GONE);
            getDownloadProgressView().setVisibility(View.VISIBLE);
            Glide.with(mContext).load(model.getPreviewNamePath()).asBitmap().into(this.getImageView());
            showProgressAnimation();
        } else {
            if (mContext == null) return;

            Glide.with(mContext).load(model.getPreviewNamePath()).asBitmap().into(this.getImageView());
            downloadView.setVisibility(View.VISIBLE);
            getDownloadProgressView().setVisibility(View.GONE);
        }

    }


    /**
     * 显示进度动画
     */
    public void showProgressAnimation() {
        ImageView view = this.getDownloadProgressView();

        if (view == null) return;

        view.setVisibility(View.VISIBLE);
        RotateAnimation rotate = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        LinearInterpolator lin = new LinearInterpolator();
        rotate.setInterpolator(lin);
        rotate.setDuration(2000);
        rotate.setRepeatCount(-1);
        rotate.setFillAfter(true);

        view.setAnimation(rotate);

        this.getDownloadImageView().setVisibility(View.GONE);
    }

    /**
     * 隐藏进度显示动画
     */
    public void hideProgressAnimation() {
        ImageView view = (ImageView) this.getDownloadProgressView();

        if (view == null) return;

        view.clearAnimation();
        view.setVisibility(View.GONE);
    }

    private ImageView getImageView() {
        if (mThumbView == null)
            mThumbView = (ImageView) findViewById(R.id.lsq_item_image);

        return mThumbView;
    }

    public ImageView getDownloadImageView() {
        if (mDownloadView == null)
            mDownloadView = (ImageView) findViewById(R.id.lsq_item_state_image);

        return mDownloadView;
    }

    /**
     * 贴纸边框
     *
     * @return
     */
    public RelativeLayout getBorderView() {
        if (mStickerBorderView == null) {
            mStickerBorderView = (RelativeLayout) findViewById(R.id.lsq_item_wrap);
        }
        return mStickerBorderView;
    }

    public ImageView getDownloadProgressView() {
        if (mDownloadProgressView == null) {
            mDownloadProgressView = (ImageView) findViewById(R.id.lsq_progress_image);
        }
        return mDownloadProgressView;
    }

    public void viewNeedRest() {
        super.viewNeedRest();

        if (this.getImageView() != null) {
            this.getImageView().setImageBitmap(null);

            StickerLocalPackage.shared().cancelLoadImage(this.getImageView());
        }
    }

    @Override
    public void onCellSelected(int i) {
        this.getBorderView().setBackground(TuSdkContext.getDrawable(R.drawable.sticker_cell_background));
    }

    @Override
    public void onCellDeselected() {
        this.getBorderView().setBackground(null);
    }

    /**
     * 贴纸是否正在下载
     *
     * @return
     */
    public boolean isDownlowding() {
        return mStickerDownloader != null && mStickerDownloader.containsTask(getModel().groupId);
    }
}



