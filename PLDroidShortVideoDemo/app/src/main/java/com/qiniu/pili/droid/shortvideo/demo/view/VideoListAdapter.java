package com.qiniu.pili.droid.shortvideo.demo.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.qiniu.pili.droid.shortvideo.PLMediaFile;
import com.qiniu.pili.droid.shortvideo.demo.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class VideoListAdapter extends BaseAdapter {

    private Context mContext;
    private List<String> mVideoList = new ArrayList<>();

    private class ViewHolder {
        ImageView mVideoThumbnail;
        TextView mVideoName;
        TextView mVideoParams;
        TextView mAudioParams;
    }

    public VideoListAdapter(Context context) {
        mContext = context;
    }

    public void addVideoFile(String filepath) {
        mVideoList.add(filepath);
    }

    public void removeVideoFile(int position) {
        mVideoList.remove(position);
    }

    public List<String> getVideoList() {
        return mVideoList;
    }

    @Override
    public int getCount() {
        return mVideoList.size();
    }

    @Override
    public Object getItem(int position) {
        return mVideoList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_video, null, false);
            ViewHolder holder = new ViewHolder();
            holder.mVideoThumbnail = (ImageView) convertView.findViewById(R.id.VideoThumbnail);
            holder.mVideoName = (TextView) convertView.findViewById(R.id.VideoName);
            holder.mVideoParams = (TextView) convertView.findViewById(R.id.VideoParams);
            holder.mAudioParams = (TextView) convertView.findViewById(R.id.AudioParams);
            convertView.setTag(holder);
        }

        String filepath = (String) getItem(position);
        if (filepath != null) {
            ViewHolder holder = (ViewHolder) convertView.getTag();
            Bitmap bitmap = getVideoThumbnail(filepath);
            if (bitmap != null) {
                holder.mVideoThumbnail.setImageBitmap(bitmap);
            }
            PLMediaFile file = new PLMediaFile(filepath);
            holder.mVideoName.setText("文件名：" + new File(filepath).getName());
            String videoParams = "视频：" + file.getVideoWidth() + "x" + file.getVideoHeight() + ", " + file.getVideoRotation() + " 度";
            holder.mVideoParams.setText(videoParams);
            String audioParams = "音频：" + file.getAudioSampleRate() + "Hz, ";
            if (file.getAudioChannels() == 1) {
                audioParams += "单通道";
            } else {
                audioParams += "立体声";
            }
            holder.mAudioParams.setText(audioParams);
            file.release();
        }

        return convertView;
    }

    private Bitmap getVideoThumbnail(String videoPath) {
        return ThumbnailUtils.createVideoThumbnail(videoPath, MediaStore.Video.Thumbnails.MINI_KIND);
    }
}
