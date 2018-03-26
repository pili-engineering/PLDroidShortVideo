package com.qiniu.pili.droid.shortvideo.demo.view;

import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.draggable.ItemDraggableRange;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractDraggableItemViewHolder;
import com.qiniu.pili.droid.shortvideo.demo.R;

import java.util.ArrayList;
import java.util.List;

import static android.media.ThumbnailUtils.OPTIONS_RECYCLE_INPUT;

public class DragItemAdapter extends RecyclerView.Adapter<DragItemAdapter.FrameItemViewHolder>
        implements DraggableItemAdapter<DragItemAdapter.FrameItemViewHolder> {

    public interface OnItemMovedListener {
        void onMoveItem(int fromPosition, int toPosition);
    }

    private List<FrameItem> mItemList;
    private OnItemMovedListener mOnItemMovedListener;

    public DragItemAdapter(ArrayList<String> paths) {
        setHasStableIds(true);

        mItemList = new ArrayList<>();
        for (int i = 0; i < paths.size(); i++) {
            mItemList.add(new FrameItem(i, paths.get(i)));
        }
    }

    public void updatePaths(ArrayList<String> paths) {
        mItemList.clear();
        for (int i = 0; i < paths.size(); i++) {
            mItemList.add(new FrameItem(i, paths.get(i)));
        }
    }

    public void setOnItemMovedListener(OnItemMovedListener onItemMovedListener) {
        mOnItemMovedListener = onItemMovedListener;
    }

    @Override
    public FrameItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final View v = inflater.inflate(R.layout.item_image, parent, false);
        return new FrameItemViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final FrameItemViewHolder holder, int position) {
        FrameItem item = mItemList.get(position);
        final String filepath = item.mPath;

        Bitmap bitmap = (item.mBitmap == null) ? getVideoThumbnail(filepath) : item.mBitmap;
        if (bitmap != null) {
            holder.mImageView.setImageBitmap(bitmap);
        }
        item.mBitmap = bitmap;
    }

    private Bitmap getVideoThumbnail(String videoPath) {
        Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, MediaStore.Video.Thumbnails.FULL_SCREEN_KIND);
        if (bitmap != null) {
            return ThumbnailUtils.extractThumbnail(bitmap, 250, 250, OPTIONS_RECYCLE_INPUT);
        }
        return null;
    }

    public class FrameItemViewHolder extends AbstractDraggableItemViewHolder {
        ImageView mImageView;

        public FrameItemViewHolder(View v) {
            super(v);
            mImageView = (ImageView) v.findViewById(R.id.ImageThumbnail);
        }
    }

    @Override
    public long getItemId(int position) {
        // requires static value, it means need to keep the same value
        // even if the item position has been changed.
        return mItemList.get(position).mID;
    }


    @Override
    public int getItemCount() {
        return mItemList.size();
    }

    @Override
    public boolean onCheckCanStartDrag(FrameItemViewHolder holder, int position, int x, int y) {
        View dragHandle = holder.mImageView;
        int handleWidth = dragHandle.getWidth();
        int handleHeight = dragHandle.getHeight();
        int handleLeft = dragHandle.getLeft();
        int handleTop = dragHandle.getTop();

        return (x >= handleLeft) && (x < handleLeft + handleWidth) &&
                (y >= handleTop) && (y < handleTop + handleHeight);
    }

    @Override
    public ItemDraggableRange onGetItemDraggableRange(FrameItemViewHolder holder, int position) {
        return null;
    }

    @Override
    public void onMoveItem(int fromPosition, int toPosition) {
        FrameItem movedItem = mItemList.remove(fromPosition);
        mItemList.add(toPosition, movedItem);
        if (mOnItemMovedListener != null) {
            mOnItemMovedListener.onMoveItem(fromPosition, toPosition);
        }
    }

    @Override
    public boolean onCheckCanDrop(int draggingPosition, int dropPosition) {
        return true;
    }

    static class FrameItem {
        final long mID;
        final String mPath;
        Bitmap mBitmap;

        public FrameItem(long id, String text) {
            this.mID = id;
            this.mPath = text;
        }
    }
}
