package org.lasque.tusdkdemohelper.tusdk;

import android.content.Context;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import org.lasque.tusdk.core.TuSdkContext;
import org.lasque.tusdkdemohelper.tusdk.model.PropsItemMonster;

import java.util.ArrayList;
import java.util.List;

/**
 * @author H.ys
 * @Date 2019/05/06
 */

public class MonsterFaceRecyclerAdapter extends RecyclerView.Adapter<MonsterFaceRecyclerAdapter.StickerViewHolder> {

    private Context mContext;
    /**
     * 数据集合
     */
    public List<PropsItemMonster> mPropsItemMonsterList;
    /**
     * 当前选中
     */
    private int mCurrentPosition = -1;
    private int mCurrentLongPos = -1;
    // 点击回调
    public ItemClickListener listener;

    public interface ItemClickListener {
        void onItemClick(int position,PropsItemMonster itemData);
    }

    public void setItemClickListener(ItemClickListener listener) {
        this.listener = listener;
    }

    public MonsterFaceRecyclerAdapter(Context context) {
        super();
        this.mContext = context;
        mPropsItemMonsterList = new ArrayList<>();
    }

    public void setPropsItemMonsterList(List<PropsItemMonster> list) {
        this.mPropsItemMonsterList = list;
    }

    public void setSelectedPosition(int position) {
        this.mCurrentPosition = position;
        notifyDataSetChanged();
    }

    public List<PropsItemMonster> getPropsItemMonsterList() {
        return mPropsItemMonsterList;
    }

    @Override
    public int getItemCount() {
        return mPropsItemMonsterList.size();
    }

    @Override
    public StickerViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(TuSdkContext.getLayoutResId("tusdk_sticker_list_cell_view"), null);
        StickerViewHolder viewHolder = new StickerViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(StickerViewHolder stickerViewHolder, final int position) {
        PropsItemMonster model = mPropsItemMonsterList.get(position);

        hideProgressAnimation(stickerViewHolder.mLoadProgressImage);

        if (model == null) return;
        if (mContext == null) return;
        stickerViewHolder.mItemImage.setImageDrawable(TuSdkContext.getDrawable(String.format("lsq_ic_face_monster_%s", model.getThumbName())));
        stickerViewHolder.mDownStateImage.setVisibility(View.GONE);
        stickerViewHolder.mLoadProgressImage.setVisibility(View.GONE);

        /** 点击选中处理 */
        if (position == mCurrentPosition) {
            stickerViewHolder.mItemWarp.setBackground(TuSdkContext.getDrawable("tusdk_sticker_cell_background"));
        } else {
            stickerViewHolder.mItemWarp.setBackground(null);
        }

        /** 点击选中事件 */
        stickerViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                PropsItemMonster itemData = getPropsItemMonsterList().get(position);
                if (listener != null && mCurrentPosition != position)
                    listener.onItemClick(position,itemData);

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
