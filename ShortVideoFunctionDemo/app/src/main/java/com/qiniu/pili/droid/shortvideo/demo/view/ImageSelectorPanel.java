package com.qiniu.pili.droid.shortvideo.demo.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.qiniu.pili.droid.shortvideo.demo.R;

import java.io.IOException;
import java.io.InputStream;


public class ImageSelectorPanel extends LinearLayout {
    private Context mContext;
    private RecyclerView mImageListView;
    private OnImageSelectedListener mOnImageSelectedListener;

    private static String[] imagePaths = {
            "1960s", "camomile", "candy", "cold", "dark"
    };

    public ImageSelectorPanel(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;

        View view = LayoutInflater.from(context).inflate(R.layout.panel_image_selector, this);
        mImageListView = (RecyclerView) view.findViewById(R.id.recycler_paint_image);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
        mImageListView.setLayoutManager(layoutManager);
        mImageListView.setAdapter(new ImageListAdapter());
    }

    public void setOnImageSelectedListener(OnImageSelectedListener listener) {
        mOnImageSelectedListener = listener;
    }

    public interface OnImageSelectedListener {
        void onImageSelected(Drawable drawable);
    }

    private class ItemViewHolder extends RecyclerView.ViewHolder {
        public ImageView mIcon;
        public TextView mName;

        public ItemViewHolder(View itemView) {
            super(itemView);
            mIcon = (ImageView) itemView.findViewById(R.id.icon);
            mName = (TextView) itemView.findViewById(R.id.name);
        }
    }

    private class ImageListAdapter extends RecyclerView.Adapter<ImageSelectorPanel.ItemViewHolder> {

        @Override
        public ImageSelectorPanel.ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);
            View contactView = inflater.inflate(R.layout.filter_item, parent, false);
            ItemViewHolder viewHolder = new ItemViewHolder(contactView);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(final ImageSelectorPanel.ItemViewHolder holder, int position) {
            try {
                final String imagePath = "filters/" + imagePaths[position] + "/thumb.png";
                InputStream is = mContext.getAssets().open(imagePath);
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                holder.mName.setVisibility(GONE);
                holder.mIcon.setImageBitmap(bitmap);
                holder.mIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mOnImageSelectedListener != null) {
                            mOnImageSelectedListener.onImageSelected(holder.mIcon.getDrawable());
                        }
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            return imagePaths.length;
        }
    }
}
