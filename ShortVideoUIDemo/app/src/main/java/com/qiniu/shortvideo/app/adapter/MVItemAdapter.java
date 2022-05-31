package com.qiniu.shortvideo.app.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.qiniu.shortvideo.app.R;
import com.qiniu.shortvideo.app.utils.Config;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * MV 特效适配器
 */
public class MVItemAdapter extends RecyclerView.Adapter<MVItemAdapter.MvItemViewHolder> {
    private JSONArray mMVArray;
    private OnMvSelectListener mOnMvSelectListener;

    public interface OnMvSelectListener {
        void onMvSelected(String mvFilePath, String maskFilePath);
    }

    public MVItemAdapter(JSONArray mvArray) {
        mMVArray = mvArray;
    }

    public void setOnMvSelectListener(OnMvSelectListener listener) {
        mOnMvSelectListener = listener;
    }

    @NonNull
    @Override
    public MvItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        Context context = viewGroup.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View contactView = inflater.inflate(R.layout.item_filter, viewGroup, false);
        return new MvItemViewHolder(contactView);
    }

    @Override
    public void onBindViewHolder(@NonNull MvItemViewHolder holder, int position) {
        final String mvsDir = Config.VIDEO_STORAGE_DIR + "mvs/";

        try {
            if (position == 0) {
                holder.mName.setText("None");
                holder.mIcon.setImageResource(R.mipmap.qn_none_filter);
                holder.mIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mOnMvSelectListener != null) {
                            mOnMvSelectListener.onMvSelected(null, null);
                        }
                    }
                });
                return;
            }

            final JSONObject mv = mMVArray.getJSONObject(position - 1);
            holder.mName.setText(mv.getString("name"));
            Bitmap bitmap = BitmapFactory.decodeFile(mvsDir + mv.getString("coverDir") + ".png");
            holder.mIcon.setImageBitmap(bitmap);
            holder.mIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        String selectedMV = mvsDir + mv.getString("colorDir") + ".mp4";
                        String selectedMask = mvsDir + mv.getString("alphaDir") + ".mp4";
                        if (mOnMvSelectListener != null) {
                            mOnMvSelectListener.onMvSelected(selectedMV, selectedMask);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return mMVArray != null ? mMVArray.length() + 1 : 0;
    }

    class MvItemViewHolder extends RecyclerView.ViewHolder {
        ImageView mIcon;
        TextView mName;

        public MvItemViewHolder(View itemView) {
            super(itemView);
            mIcon = itemView.findViewById(R.id.filter_image);
            mName = itemView.findViewById(R.id.filter_name);
        }
    }
}
