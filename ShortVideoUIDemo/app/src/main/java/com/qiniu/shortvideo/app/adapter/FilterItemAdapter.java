package com.qiniu.shortvideo.app.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.qiniu.pili.droid.shortvideo.PLBuiltinFilter;
import com.qiniu.shortvideo.app.R;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 内置滤镜的适配器
 */
public class FilterItemAdapter extends RecyclerView.Adapter<FilterItemAdapter.FilterItemViewHolder> {
    private Context mContext;
    private ArrayList<PLBuiltinFilter> mFilters;
    private ArrayList<String> mFilterDescriptions;
    private OnFilterSelectListener mOnFilterSelectListener;
    private int mCurrentPosition = 0;

    public interface OnFilterSelectListener {
        void onFilterSelected(String filterName, String description);
    }

    public FilterItemAdapter(Context context, List<PLBuiltinFilter> filters, List<String> filterDescriptions) {
        mContext = context;
        mFilters = (ArrayList<PLBuiltinFilter>) filters;
        Iterator<PLBuiltinFilter> it = mFilters.iterator();
        while (it.hasNext()) {
            PLBuiltinFilter filter = it.next();
            if (filter.getName().equals("none.png")) {
                it.remove();
            }
        }
        mFilterDescriptions = (ArrayList<String>) filterDescriptions;
    }

    public void setOnFilterSelectListener(OnFilterSelectListener listener) {
        mOnFilterSelectListener = listener;
    }

    public void changeToLastFilter() {
        setFilter(mCurrentPosition == 0 ? mFilters.size() : mCurrentPosition - 1);
    }

    public void changeToNextFilter() {
        setFilter(mCurrentPosition == mFilters.size() ? 0 : mCurrentPosition + 1);
    }

    public int getCurrentPosition() {
        return mCurrentPosition;
    }

    private void setFilter(int index) {
        mCurrentPosition = index;
        if (mOnFilterSelectListener != null) {
            if (mCurrentPosition == 0) {
                mOnFilterSelectListener.onFilterSelected(null, "无");
            } else {
                PLBuiltinFilter filter = mFilters.get(mCurrentPosition - 1);
                mOnFilterSelectListener.onFilterSelected(filter.getName(), mFilterDescriptions.get(mCurrentPosition - 1));
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FilterItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View contactView = inflater.inflate(R.layout.item_filter, parent, false);
        return new FilterItemViewHolder(contactView);
    }

    @Override
    public void onBindViewHolder(@NonNull final FilterItemViewHolder holder, final int position) {
        try {
            if (position == 0) {
                holder.mName.setText("无");
                holder.mIcon.setImageDrawable(mContext.getResources().getDrawable(R.mipmap.qn_none_filter));
                holder.mIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mCurrentPosition = 0;
                        if (mOnFilterSelectListener != null) {
                            mOnFilterSelectListener.onFilterSelected(null, "无");
                        }
                    }
                });
                return;
            }

            final PLBuiltinFilter filter = mFilters.get(position - 1);
            holder.mName.setText(mFilterDescriptions.get(position - 1));
            InputStream is = mContext.getAssets().open(filter.getAssetFilePath());
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(mContext.getResources(), bitmap);
            drawable.setCircular(true);
            holder.mIcon.setImageDrawable(drawable);
            holder.mIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mCurrentPosition = position;
                    if (mOnFilterSelectListener != null) {
                        mOnFilterSelectListener.onFilterSelected(filter.getName(), mFilterDescriptions.get(position - 1));
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return mFilters != null ? mFilters.size() + 1 : 0;
    }

    class FilterItemViewHolder extends RecyclerView.ViewHolder {
        ImageView mIcon;
        TextView mName;

        public FilterItemViewHolder(View itemView) {
            super(itemView);
            mIcon = itemView.findViewById(R.id.filter_image);
            mName = itemView.findViewById(R.id.filter_name);
        }
    }
}
