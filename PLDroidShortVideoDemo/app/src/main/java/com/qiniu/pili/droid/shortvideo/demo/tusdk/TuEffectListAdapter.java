package com.qiniu.pili.droid.shortvideo.demo.tusdk;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.qiniu.pili.droid.shortvideo.demo.R;

import java.io.IOException;
import java.io.InputStream;

public class TuEffectListAdapter extends RecyclerView.Adapter<TuEffectListAdapter.EffectItemViewHolder> {
    private String[] mEffectNames = {
            "无", "抖动", "幻视", "灵魂出窍", "魔法", "扭曲", "信号"
    };
    private String[] mEffectPaths = {
            "none", "liveshake", "livemegrim", "livesoulout", "edgemagic", "livefancy", "livesignal"
    };
    private String[] mEffectCodes = {
            "", "LiveShake01", "LiveMegrim01", "LiveSoulOut01", "EdgeMagic01", "LiveFancy01_1", "LiveSignal01"
    };
    private int[] mEffectColors = {
            R.color.colorAccent, R.color.effectBar1, R.color.effectBar2, R.color.effectBar3, R.color.effectBar4, R.color.effectBar5, R.color.effectBar6
    };

    private Context mContext;
    private OnEffectTouchListener mOnEffectTouchListener;

    public TuEffectListAdapter(Context context) {
        mContext = context;
    }

    public void setEffectOnTouchListener(OnEffectTouchListener onEffectTouchListener) {
        mOnEffectTouchListener = onEffectTouchListener;
    }

    public interface OnEffectTouchListener {
        boolean onTouch(View v, MotionEvent event, String effectCode, int color);
    }

    @Override
    public EffectItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View contactView = inflater.inflate(R.layout.filter_item, parent, false);
        EffectItemViewHolder viewHolder = new EffectItemViewHolder(contactView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(EffectItemViewHolder holder, final int position) {
        try {
            final String imagePath = "effects/" + mEffectPaths[position] + ".png";
            InputStream is = mContext.getAssets().open(imagePath);
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            holder.mName.setText(mEffectNames[position]);
            holder.mIcon.setImageBitmap(bitmap);
            holder.mIcon.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (mOnEffectTouchListener != null) {
                        return mOnEffectTouchListener.onTouch(v, event, mEffectCodes[position], mContext.getResources().getColor(mEffectColors[position]));
                    }
                    return false;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return mEffectNames.length;
    }

    public class EffectItemViewHolder extends RecyclerView.ViewHolder {
        public ImageView mIcon;
        public TextView mName;

        public EffectItemViewHolder(View itemView) {
            super(itemView);
            mIcon = (ImageView) itemView.findViewById(R.id.icon);
            mName = (TextView) itemView.findViewById(R.id.name);
        }
    }
}