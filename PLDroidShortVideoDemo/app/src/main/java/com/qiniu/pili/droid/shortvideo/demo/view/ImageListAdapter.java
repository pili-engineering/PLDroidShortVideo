package com.qiniu.pili.droid.shortvideo.demo.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.qiniu.pili.droid.shortvideo.PLComposeItem;
import com.qiniu.pili.droid.shortvideo.demo.R;

import java.util.LinkedList;
import java.util.List;

import static android.media.ThumbnailUtils.OPTIONS_RECYCLE_INPUT;

public class ImageListAdapter extends BaseAdapter {
    private Context mContext;
    private List<PLComposeItem> mElements = new LinkedList<>();

    private class ViewHolder {
        ImageView mImageThumbnail;
        TextView mImageName;
        TextView mImageParams;
        TextView mImageDuration;
        TextView mImageTransTime;
    }

    public ImageListAdapter(Context context) {
        mContext = context;
    }

    public void addItem(PLComposeItem section) {
        mElements.add(section);
    }

    public void addItem(int position,PLComposeItem section) {
        mElements.add(position,section);
    }

    public void removeItem(int position) {
        mElements.remove(position);
    }

    public List<PLComposeItem> getItemList(){
        return mElements;
    }

    @Override
    public int getCount() {
        return mElements.size();
    }

    @Override
    public Object getItem(int position) {
        return mElements.get(position);
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
            holder.mImageThumbnail = (ImageView) convertView.findViewById(R.id.ImageThumbnail);
            holder.mImageName = (TextView) convertView.findViewById(R.id.ImageName);
            holder.mImageParams = (TextView) convertView.findViewById(R.id.ImageParams);
            holder.mImageDuration = (TextView) convertView.findViewById(R.id.ImageDuration);
            holder.mImageTransTime = (TextView) convertView.findViewById(R.id.ImageTransTime);
            convertView.setTag(holder);
        }

        PLComposeItem section = (PLComposeItem) getItem(position);
        String filepath = section.getFilePath();
        String fileName = getFileNameWithSuffix(filepath);
        if (fileName == null){
            fileName = "文件名称错误";
        }
        if (filepath != null) {
            ViewHolder holder = (ViewHolder) convertView.getTag();
            holder.mImageName.setText("文件名：" + fileName);
            Bitmap originalImage = BitmapFactory.decodeFile(filepath);
            if (originalImage != null) {
                Bitmap thumbnailImage = getImageThumbnail(originalImage);
                if (thumbnailImage != null) {
                    holder.mImageThumbnail.setImageBitmap(thumbnailImage);
                }

                String imageParams = "分辨率：" + originalImage.getWidth() + "x" + originalImage.getHeight();
                holder.mImageParams.setText(imageParams);
            }
            originalImage.recycle();

            long duration = section.getDurationMs();
            long transTime = section.getTransitionTimeMs();
            holder.mImageDuration.setText("持续时间：" + duration + "ms");
            holder.mImageTransTime.setText("转场时间：" + transTime + "ms");
        }

        return convertView;
    }

    private Bitmap getImageThumbnail(Bitmap bitmap) {
        return ThumbnailUtils.extractThumbnail(bitmap, 320, 320, OPTIONS_RECYCLE_INPUT);
    }

    private String getFileNameWithSuffix(String pathandname) {
        int start = pathandname.lastIndexOf("/");
        if (start != -1 ) {
            return pathandname.substring(start + 1);
        } else {
            return null;
        }
    }
}