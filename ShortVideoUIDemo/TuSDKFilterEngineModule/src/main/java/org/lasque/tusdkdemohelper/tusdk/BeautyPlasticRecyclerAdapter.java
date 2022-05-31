package org.lasque.tusdkdemohelper.tusdk;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.TextView;

import org.lasque.tusdk.core.TuSdkContext;

import java.util.ArrayList;
import java.util.List;

/**
 * 微整形
 * @author xujie
 * @Date 2018/9/29
 */

public class BeautyPlasticRecyclerAdapter extends RecyclerView.Adapter<BeautyPlasticRecyclerAdapter.BeautyViewHolder>{

    private Context mContext;
    private List<String> mBeautyParams;
    private int mCurrentPos = -1;

    public OnBeautyPlasticItemClickListener listener;

    public interface OnBeautyPlasticItemClickListener{
        void onItemClick(View v, int position);
        void onClear();
    }

    public void setOnBeautyPlasticItemClickListener(OnBeautyPlasticItemClickListener onBeautyPlasticItemClickListener){
        this.listener = onBeautyPlasticItemClickListener;
    }

    public BeautyPlasticRecyclerAdapter(Context context, List<String> params) {
        mContext = context;
        mBeautyParams = new ArrayList<>(params);
    }

    /**
     * 设置参数
     * @param beautyParams
     */
    public void setBeautyParams(List<String> beautyParams){
        mBeautyParams = beautyParams;
        notifyDataSetChanged();
    }

    /**
     * 获取当前参数列表
     * @return
     */
    public List<String> getBeautyParams(){
        return mBeautyParams;
    }

    /**
     * 获取当前选中position
     * @return
     */
    public int getCurrentPos(){
        return mCurrentPos;
    }

    @Override
    public BeautyViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(TuSdkContext.getLayoutResId("tusdk_recycler_beauty_item_layout"),null);
        BeautyViewHolder viewHolder = new BeautyViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final BeautyViewHolder beautyViewHolder,final int position) {

        String code = mBeautyParams.get(position);
        code = code.toLowerCase();

        boolean selected = mCurrentPos == position && position != 0;
        beautyViewHolder.mBeautyLevelImage.setSelected(selected);
        beautyViewHolder.mBeautyName.setChecked(selected);

        beautyViewHolder.mBeautyLevelImage.setImageResource(TuSdkContext.getDrawableResId("lsq_ic_"+code));
        beautyViewHolder.mBeautyName.setText(TuSdkContext.getString("lsq_filter_set_"+code));

        beautyViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(position == 0 && listener != null) {
                    mCurrentPos = 0;
                    notifyDataSetChanged();
                    listener.onClear();
                }else {
                    notifyItemChanged(mCurrentPos);
                    mCurrentPos = position;
                    notifyItemChanged(position);

                    if(listener != null) listener.onItemClick(beautyViewHolder.itemView,position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mBeautyParams.size();
    }

    class BeautyViewHolder extends RecyclerView.ViewHolder{

        public TextView mBeautyLevelText;
        public ImageView mBeautyLevelImage;
        public CheckedTextView mBeautyName;


        public BeautyViewHolder(View itemView) {
            super(itemView);
            mBeautyLevelText =  (TextView)itemView.findViewById(TuSdkContext.getIDResId("lsq_beauty_level_text"));
            mBeautyLevelImage =  (ImageView)itemView.findViewById(TuSdkContext.getIDResId("lsq_beauty_level_image"));
            mBeautyLevelText.setVisibility(View.GONE);
            mBeautyName = (CheckedTextView)itemView.findViewById(TuSdkContext.getIDResId("lsq_beauty_name"));
            mBeautyName.setVisibility(View.VISIBLE);
        }
    }
}
