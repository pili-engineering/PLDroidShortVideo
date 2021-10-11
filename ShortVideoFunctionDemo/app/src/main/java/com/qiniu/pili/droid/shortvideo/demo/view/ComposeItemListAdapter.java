package com.qiniu.pili.droid.shortvideo.demo.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.qiniu.pili.droid.shortvideo.PLComposeItem;
import com.qiniu.pili.droid.shortvideo.PLMediaFile;
import com.qiniu.pili.droid.shortvideo.demo.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static android.media.ThumbnailUtils.OPTIONS_RECYCLE_INPUT;

public class ComposeItemListAdapter extends BaseAdapter {

    private final Context mContext;
    private final List<PLComposeItem> mItemList = new ArrayList<>();

    private class ViewHolder {
        ImageView mItemThumbnail;
        TextView mItemName;
        TextView mItemParams;
        TextView mItemDuration;
        TextView mItemTransTime;
    }

    public ComposeItemListAdapter(Context context) {
        mContext = context;
    }

    public void addItem(PLComposeItem item) {
        mItemList.add(item);
    }

    public void removeItem(int position) {
        mItemList.remove(position);
    }

    public List<PLComposeItem> getItemList() {
        return mItemList;
    }

    @Override
    public int getCount() {
        return mItemList.size();
    }

    @Override
    public Object getItem(int position) {
        return mItemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_image, null, false);
            ViewHolder holder = new ViewHolder();
            holder.mItemThumbnail = convertView.findViewById(R.id.ImageThumbnail);
            holder.mItemName = convertView.findViewById(R.id.ImageName);
            holder.mItemParams = convertView.findViewById(R.id.ImageParams);
            holder.mItemDuration = convertView.findViewById(R.id.ImageDuration);
            holder.mItemTransTime = convertView.findViewById(R.id.ImageTransTime);
            convertView.setTag(holder);
        }

        PLComposeItem item = (PLComposeItem) getItem(position);
        if (item == null) {
            return convertView;
        }

        ViewHolder holder = (ViewHolder) convertView.getTag();
        if (item.getItemType() == PLComposeItem.ItemType.VIDEO) {
            configVideoItem(holder, item);
        } else {
            configImageItem(holder, item);
        }

        return convertView;
    }

    private void configImageItem(ViewHolder holder, PLComposeItem item) {
        String filepath = item.getFilePath();
        String fileName = getFileNameWithSuffix(filepath);
        if (fileName == null) {
            fileName = "文件名称错误";
        }
        if (filepath != null) {
            holder.mItemName.setText("文件名：" + fileName);
            Bitmap originalImage = BitmapFactory.decodeFile(filepath);
            if (originalImage != null) {
                Bitmap thumbnailImage = getImageThumbnail(originalImage);
                if (thumbnailImage != null) {
                    holder.mItemThumbnail.setImageBitmap(thumbnailImage);
                }

                String imageParams = "分辨率：" + originalImage.getWidth() + "x" + originalImage.getHeight();
                holder.mItemParams.setText(imageParams);
            }
            originalImage.recycle();

            long duration = item.getDurationMs();
            long transTime = item.getTransitionTimeMs();
            holder.mItemDuration.setText("持续时间：" + duration + "ms");
            holder.mItemTransTime.setText("转场时间：" + transTime + "ms");
        }
    }

    private void configVideoItem(ViewHolder holder, PLComposeItem item) {
        Bitmap bitmap = getVideoThumbnail(item.getFilePath());
        if (bitmap != null) {
            holder.mItemThumbnail.setImageBitmap(bitmap);
        }

        PLMediaFile file = new PLMediaFile(item.getFilePath());
        holder.mItemName.setText("文件名：" + new File(item.getFilePath()).getName());

        if (file.hasVideo()) {
            String videoParams = "视频：" + file.getVideoWidth() + "x" + file.getVideoHeight() + ", " + file.getVideoRotation() + " 度";
            holder.mItemParams.setText(videoParams);
        }
        if (file.hasAudio()) {
            String audioParams = "音频：" + file.getAudioSampleRate() + "Hz, ";
            if (file.getAudioChannels() == 1) {
                audioParams += "单通道";
            } else {
                audioParams += "立体声";
            }
            holder.mItemDuration.setText(audioParams);
        } else {
            holder.mItemDuration.setText("音频：无");
        }
        file.release();
    }

    private Bitmap getVideoThumbnail(String videoPath) {
        return ThumbnailUtils.createVideoThumbnail(videoPath, MediaStore.Video.Thumbnails.MINI_KIND);
    }

    private Bitmap getImageThumbnail(Bitmap bitmap) {
        return ThumbnailUtils.extractThumbnail(bitmap, 320, 320, OPTIONS_RECYCLE_INPUT);
    }

    public static String getFileNameWithSuffix(String pathandname) {
        int start = pathandname.lastIndexOf("/");
        if (start != -1) {
            return pathandname.substring(start + 1);
        } else {
            return null;
        }
    }
}
