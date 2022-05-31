package org.lasque.tusdkdemohelper.tusdk;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.widget.ImageView;

import com.example.tusdkdemohelper.R;

import java.util.Arrays;
import java.util.List;

/**
 * 美颜
 * @author xujie
 * @Date 2018/9/29
 */

public class BeautyRecyclerAdapter extends RecyclerView.Adapter<BeautyRecyclerAdapter.BeautyViewHolder>{

    private Context mContext;
    private List<String> mBeautyParams = Arrays.asList("skin");
    private boolean useSkinNatural = false;

    public OnBeautyItemClickListener listener;

    public interface OnBeautyItemClickListener{
        void onChangeSkin(View v, String key, boolean useSkinNatural);
        void onClear();
    }

    public void setOnSkinItemClickListener(OnBeautyItemClickListener onBeautyItemClickListener){
        this.listener = onBeautyItemClickListener;
    }


    public BeautyRecyclerAdapter(Context context) {
        super();
        mContext = context;
    }

    @Override
    public BeautyViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.tusdk_recycler_skin_item_layout,null);
        BeautyViewHolder viewHolder = new BeautyViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final BeautyViewHolder beautyViewHolder,final int position) {

        beautyViewHolder.resetImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listener != null) listener.onClear();

                beautyViewHolder.whiteningImage.setImageResource(R.drawable.lsq_ic_whitening_norl);
                beautyViewHolder.smoothingImage.setImageResource(R.drawable.lsq_ic_smoothing_norl);
                beautyViewHolder.ruddyImage.setImageResource(R.drawable.lsq_ic_ruddy_norl);
                beautyViewHolder.whiteningName.setChecked(false);
                beautyViewHolder.smoothingName.setChecked(false);
                beautyViewHolder.ruddyName.setChecked(false);
            }
        });
        beautyViewHolder.skinBeautyImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listener != null) {
                    listener.onChangeSkin(v,"smoothing",useSkinNatural);
                }
                if(!useSkinNatural) {
                    beautyViewHolder.skinBeautyImage.setImageResource(R.drawable.lsq_ic_skin_extreme_nor);
                    beautyViewHolder.skinBeautyName.setText(R.string.lsq_filter_set_skin_extreme);
                }else {
                    beautyViewHolder.skinBeautyImage.setImageResource(R.drawable.lsq_ic_skin_precision_nor);
                    beautyViewHolder.skinBeautyName.setText(R.string.lsq_filter_set_skin_precision);
                }

                useSkinNatural = !useSkinNatural;

                beautyViewHolder.whiteningImage.setImageResource(R.drawable.lsq_ic_whitening_norl);
                beautyViewHolder.smoothingImage.setImageResource(R.drawable.lsq_ic_smoothing_sele);
                beautyViewHolder.ruddyImage.setImageResource(R.drawable.lsq_ic_ruddy_norl);
                beautyViewHolder.whiteningName.setChecked(false);
                beautyViewHolder.smoothingName.setChecked(true);
                beautyViewHolder.ruddyName.setChecked(false);
            }
        });
        beautyViewHolder.whiteningImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listener != null) listener.onChangeSkin(v,"whitening",!useSkinNatural);

                beautyViewHolder.whiteningImage.setImageResource(R.drawable.lsq_ic_whitening_sele);
                beautyViewHolder.smoothingImage.setImageResource(R.drawable.lsq_ic_smoothing_norl);
                beautyViewHolder.ruddyImage.setImageResource(R.drawable.lsq_ic_ruddy_norl);
                beautyViewHolder.whiteningName.setChecked(true);
                beautyViewHolder.smoothingName.setChecked(false);
                beautyViewHolder.ruddyName.setChecked(false);
            }
        });
        beautyViewHolder.smoothingImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listener != null) listener.onChangeSkin(v,"smoothing",!useSkinNatural);

                beautyViewHolder.whiteningImage.setImageResource(R.drawable.lsq_ic_whitening_norl);
                beautyViewHolder.smoothingImage.setImageResource(R.drawable.lsq_ic_smoothing_sele);
                beautyViewHolder.ruddyImage.setImageResource(R.drawable.lsq_ic_ruddy_norl);
                beautyViewHolder.whiteningName.setChecked(false);
                beautyViewHolder.smoothingName.setChecked(true);
                beautyViewHolder.ruddyName.setChecked(false);
            }
        });
        beautyViewHolder.ruddyImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listener != null) listener.onChangeSkin(v,"ruddy",!useSkinNatural);

                beautyViewHolder.whiteningImage.setImageResource(R.drawable.lsq_ic_whitening_norl);
                beautyViewHolder.smoothingImage.setImageResource(R.drawable.lsq_ic_smoothing_norl);
                beautyViewHolder.ruddyImage.setImageResource(R.drawable.lsq_ic_ruddy_seel);
                beautyViewHolder.whiteningName.setChecked(false);
                beautyViewHolder.smoothingName.setChecked(false);
                beautyViewHolder.ruddyName.setChecked(true);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mBeautyParams.size();
    }

    class BeautyViewHolder extends RecyclerView.ViewHolder{

        public ImageView resetImage;

        public CheckedTextView skinBeautyName;
        public ImageView skinBeautyImage;

        public CheckedTextView whiteningName;
        public ImageView whiteningImage;
        public CheckedTextView smoothingName;
        public ImageView smoothingImage;
        public CheckedTextView ruddyName;
        public ImageView ruddyImage;

        public BeautyViewHolder(View itemView) {
            super(itemView);
            resetImage = (ImageView) itemView.findViewById(R.id.lsq_reset_image);

            skinBeautyName = (CheckedTextView) itemView.findViewById(R.id.lsq_skin_beauty_name);
            skinBeautyImage = (ImageView) itemView.findViewById(R.id.lsq_skin_beauty_image);

            whiteningName = (CheckedTextView) itemView.findViewById(R.id.lsq_whitening_name);
            whiteningImage = (ImageView) itemView.findViewById(R.id.lsq_whitening_image);
            smoothingName = (CheckedTextView) itemView.findViewById(R.id.lsq_smoothing_name);
            smoothingImage = (ImageView) itemView.findViewById(R.id.lsq_smoothing_image);
            ruddyName = (CheckedTextView) itemView.findViewById(R.id.lsq_ruddy_name);
            ruddyImage = (ImageView) itemView.findViewById(R.id.lsq_ruddy_image);
        }
    }
}
