package com.kiwi.ui;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.kiwi.tracker.bean.conf.StickerConfig;
import com.kiwi.tracker.common.Config;
import com.kiwi.tracker.utils.ZipUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cn.aigestudio.downloader.bizs.DLError;
import cn.aigestudio.downloader.bizs.DLManager;
import cn.aigestudio.downloader.interfaces.SimpleDListener;

public class StickerAdapter extends RecyclerView.Adapter<StickerAdapter.ViewHolder> {
    public interface IStickerHandler {
        void writeSticker(StickerConfig stickerConfig);
    }

    interface IFinishDownload {
        void finishDownload();

        void finishError(int status);
    }

    public interface onStickerChangeListener {
        void onStickerChanged(StickerConfig item);
    }

    public static final int FAILED_TO_DOWNLOAD = 1;
    public static final int FINISH_TO_DOWNLOAD = 0;
    public static final int NETWORK_ERROR = 2;

    private List<StickerConfig> mValues;
    private Context mContext;

    private onStickerChangeListener onStickerChangeListener;
    private IStickerHandler iStickerHandler;
    private final TypedValue mTypedValue = new TypedValue();

    private Map<String, String> downloadingStickers = new ConcurrentHashMap();

    private void loadingStart(StickerConfig sticker, String url) {
        downloadingStickers.put(sticker.getName(), url);
        Log.d("Tracker", "loading start,name:" + sticker.getName() + ",url:" + url);
    }

    private void loadingEnd(StickerConfig sticker) {
        downloadingStickers.remove(sticker.getName());
        Log.d("Tracker", "loading end,name:" + sticker.getName());
    }

    public boolean isLoading(StickerConfig sticker) {
        return downloadingStickers.containsKey(sticker.getName());
    }

    public StickerAdapter(Context context, List<StickerConfig> items,
                          onStickerChangeListener onStickerChangeListener,
                          IStickerHandler iStickerHandler) {
        context.getTheme().resolveAttribute(R.attr.selectableItemBackground, mTypedValue, true);
        this.mContext = context;
        this.mValues = items;
        this.onStickerChangeListener = onStickerChangeListener;
        this.iStickerHandler = iStickerHandler;

        DLManager.getInstance(mContext).setMaxTask(5);
    }

    public List<StickerConfig> getValues() {
        return mValues;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_sticker, parent, false);
        ViewHolder KiwiViewHolder = new ViewHolder(view);
        return KiwiViewHolder;
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final StickerConfig item = this.mValues.get(position);
        Log.d("tracker", "onBindViewHolder  dev:" + position + ",name:" + item.getName() + ",isLoading" + isLoading(item) + ",isdownoed:" + item.isDownloaded());

        holder.onBindView(item, position);
        holder.mView.setOnClickListener(new StickerItemClickListener(item, position));
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

//    @Override
//    public void onViewDetachedFromWindow(StickerAdapter.ViewHolder holder) {
//        super.onViewDetachedFromWindow(holder);
//        holder.mdownloadView.clearAnimation();
//    }

    /**
     * Here is the key method to apply the animation
     */
    private void setAnimation(View viewToAnimate) {
        Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.loading_animation);
        viewToAnimate.startAnimation(animation);
    }

    private void stopAnimation(final View viewToAnimate) {
        viewToAnimate.clearAnimation();
    }

    private void startDownloadTicket(final StickerConfig sticker, Handler handler, final int position) {

        String url = getRealDownloadUrl(sticker);
        loadingStart(sticker, url);
        Log.d("tracker", String.format("start to download sticker,name:%s,url:%s", sticker.getName(), url));
        handler.post(new Runnable() {
            @Override
            public void run() {
//                Log.e("Tracker","notifyItemChanged:"+position);
                notifyItemChanged(position);
            }
        });

        final IFinishDownload iFinishDownload = new StickerFinishDownload(sticker, handler);
        DLManager dlManager = DLManager.getInstance(mContext);
        final String fileName = sticker.getDir() + ".zip";
        dlManager.dlStart(url, Config.getTempPath(), fileName, new SimpleDListener() {
            @Override
            public void onPrepare() {
                Log.d("tracker", "download file onPrepare");
            }

            @Override
            public void onStart(String fileName, String realUrl, int fileLength) {
                Log.d("tracker", "download file onstart,fileName:" + fileName + ",url:" + realUrl + ",fileLen:" + fileLength);
            }

            @Override
            public void onProgress(int progress) {
                Log.d("tracker", "download file onProgress,progress:" + progress);
            }

            @Override
            public void onStop(int progress) {
                Log.d("tracker", "download file onStop: ");
                iFinishDownload.finishError(-1);
            }

            @Override
            public void onFinish(File file) {
                String path = Config.getStickerPath();
                Log.e("stickerPath", path);
                File targetDir = new File(path);

                Log.d("tracker", "download file succ ,name:" + sticker.getName() + ",path:" + file.getAbsolutePath() + ",targetDir:" + path);
                try {
                    ZipUtils.unzip(file, targetDir);
                    file.delete();
                } catch (IOException e) {
                    Log.e("tracker", "download file succ ,name:" + sticker.getName() + ",error:" + e.toString());
                    iFinishDownload.finishError(-1);
                    file.delete();
                    return;
                }

                //修改内存与文件
                sticker.setDownloaded(true);
                iStickerHandler.writeSticker(sticker);
                iFinishDownload.finishDownload();
            }

            @Override
            public void onError(int status, String error) {
                Log.d("tracker", String.format("download sticker,name:%ss,error:%s,status:%s", sticker.getName(), error, status));
                if (status == DLError.ERROR_REPEAT_URL) {
                    iFinishDownload.finishError(1);
                    Log.d("tracker", String.format("download sticker repeated,name:%s", sticker.getName()));
                    return;
                }

                iFinishDownload.finishError(status);
            }
        });
    }


    /**
     * SourceType  默认为0
     *
     * @param sticker
     * @return
     */
    private String getRealDownloadUrl(StickerConfig sticker) {
        if (null == sticker.getSourceType() || "0".equals(sticker.getSourceType())) {
            String downloadUrl = sticker.getDownloadUrl(Config.getStickerUrl());
            return downloadUrl;
        } else {
            //可以使用自己的贴纸下载url
            String userUrl = "";
            return userUrl + sticker.getDir() + ".zip";
        }
    }


    //判断是否有网络
    public boolean isNetworkConnected(Context context) {
        boolean bisConnFlag = false;
        ConnectivityManager conManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo network = conManager.getActiveNetworkInfo();
        if (network != null) {
            bisConnFlag = conManager.getActiveNetworkInfo().isAvailable();
        }

        return bisConnFlag;
    }

    private class StickerFinishDownload implements IFinishDownload {
        private final StickerConfig item;
        private final Handler handler;

        public StickerFinishDownload(StickerConfig item, Handler handler) {
            this.item = item;
            this.handler = handler;
        }

        @Override
        public void finishDownload() {
            Log.d("UI", "finishDownload sticker:" + item.getName());
            loadingEnd(item);
            handler.sendEmptyMessage(FINISH_TO_DOWNLOAD);
        }

        public void finishError(int status) {
            loadingEnd(item);
            if (status == DLError.ERROR_NOT_NETWORK
                    || status == DLError.ERROR_OPEN_CONNECT) {
                handler.sendEmptyMessage(NETWORK_ERROR);
            } else {
                Log.e("tracker", "finishError failed to download sticker,name:" + item.getName());
                handler.sendEmptyMessage(FAILED_TO_DOWNLOAD);
            }
        }
    }


    private class StickerItemClickListener implements View.OnClickListener {
        private final StickerConfig item;
        private final int position;
        private final Handler handler = new UIHandler();

        public StickerItemClickListener(StickerConfig item, int position) {
            this.item = item;
            this.position = position;
        }

        @Override
        public void onClick(View view) {
            Log.e("path", Config.getStickerPath());
            if (item.isDownloaded() || position == 0) {

//                  notifyItemChanged(selectedPos);
//                    selectedPos = position;
                StickerConfigMgr.setSelectedStickerConfig(item);
                notifyDataSetChanged();
//                  notifyItemChanged(position);

                onStickerChangeListener.onStickerChanged(item);
                return;
            }

            if (isLoading(item)) {
                return;
            }

            if (!isNetworkConnected(mContext)) {
                Toast.makeText(mContext, "Network error", Toast.LENGTH_LONG).show();
                Log.i("Tracker", "Network error,download sticker,name:" + item.getName());
                return;
            }

            startDownloadTicket(item, handler, position);
        }

        private class UIHandler extends Handler {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == FINISH_TO_DOWNLOAD) {

                }
                if (msg.what == FAILED_TO_DOWNLOAD) {
                    Toast.makeText(mContext, "failed to download sticker", Toast.LENGTH_LONG).show();
                }
                if (msg.what == NETWORK_ERROR) {
                    Toast.makeText(mContext, "Network error", Toast.LENGTH_LONG).show();
                }

//                Log.e("Tracker","notifyItemChanged:"+position);
                notifyItemChanged(position);
            }
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final View mView;
        private final ImageView mImageView;
        private final ImageView mdownloadView;
        private final ImageView mdownloadIconView;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mImageView = (ImageView) view.findViewById(R.id.image_thumb);
            mdownloadView = (ImageView) view.findViewById(R.id.image_download);
            mdownloadIconView = (ImageView) view.findViewById(R.id.image_download_small);
        }

        public void onBindView(StickerConfig item, int postion) {

            //选中高亮实现
            if (item.equals(StickerConfigMgr.getSelectedStickerConfig())) {
//            if (selectedPos==postion) {
                mView.setBackgroundResource(R.drawable.sticker_selected);
            } else {
                mView.setBackgroundResource(R.color.transparent);
            }

            //无贴纸处理
            if (postion == 0) {
                mImageView.setImageResource(R.drawable.filter_none);
                hideLoadingView();
                return;
            }

            String thumbUrlPath = Config.getThumbUrl() + item.getThumb();
            Glide.with(mImageView.getContext())
                    .load(thumbUrlPath)
                    .fitCenter()
                    .into(mImageView);

            if (item.isDownloaded()) {
                hideLoadingView();
            } else {
                boolean loading = isLoading(item);

                mdownloadIconView.setVisibility(loading ? View.GONE : View.VISIBLE);
                mdownloadView.setVisibility(loading ? View.VISIBLE : View.GONE);
                //是否正在下载，如果正在下载，开启动画
                if (loading) {
                    setAnimation(mdownloadView);
                } else {
                    stopAnimation(mdownloadView);
                }
            }
        }

        public void hideLoadingView() {
            mdownloadIconView.setVisibility(View.GONE);
            mdownloadView.setVisibility(View.GONE);
        }
    }
}