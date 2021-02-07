package com.qiniu.pili.droid.shortvideo.demo.ar;

import android.content.Context;
import android.os.AsyncTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

class Downloader {

    public static final String TASK_NAME = "download";

    private static int sBufferSize = 1024 * 64;
    private static int sConnectTimeout = 1000 * 10;
    private static int sReadTimeout = 1000 * 10;

    public static void setBufferSize(int size) {
        sBufferSize = size;
    }

    public static void setConnectTimeout(int timeout) {
        sConnectTimeout = timeout;
    }

    public static void setReadTimeout(int timeout) {
        sReadTimeout = timeout;
    }

    public static String getLocalName(String url) {
        try {
            return Auth.sha1Hex(url);
        } catch (Exception e) {
            return "placeholder";
        }
    }

    public static final String PACKAGES_PATH = "pacakges";
    public static final String TARGETS_PATH = "targets";

    public static String getDownloadPath(Context context, String path) {
        String dir = String.format("%s/%s", context.getFilesDir().getAbsolutePath(), path);
        new File(dir).mkdirs();
        return dir;
    }

    public static void download(String url, String dst, boolean force, AsyncCallback<String> cb) {
        File dstFile = new File(dst);
        if (dstFile.exists() && !force) {
            cb.onSuccess(dstFile.getAbsolutePath());
            return;
        }
        new DownloadTask(url, dst, cb).execute();
    }

    private static class DownloadTask extends AsyncTask<Void, Double, Void> {

        private String mURL;
        private String mDst;
        private AsyncCallback<String> mCb;
        private Throwable mThrowable;

        public DownloadTask(String url, String dst, AsyncCallback<String> cb) {
            mURL = url;
            mDst = dst;
            mCb = cb;
            mThrowable = null;
        }

        @Override
        protected Void doInBackground(Void... params) {
            final String tempDst = String.format("%s.download", mDst);
            try {
                HttpURLConnection conn = null;
                InputStream is = null;
                OutputStream os = null;
                try {
                    URL url = new URL(mURL);
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(sConnectTimeout);
                    conn.setReadTimeout(sReadTimeout);
                    conn.setInstanceFollowRedirects(true);
                    conn.connect();

                    if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                        throw new Exception(String.format("%d: %s", conn.getResponseCode(), conn.getResponseMessage()));
                    }

                    int contentLength = conn.getContentLength();

                    is = conn.getInputStream();
                    os = new FileOutputStream(tempDst);

                    byte[] buffer = new byte[sBufferSize];
                    int received = 0, totalReceived = 0;
                    while ((received = is.read(buffer)) != -1) {
                        totalReceived += received;
                        if (contentLength > 0) {
                            publishProgress(((double) totalReceived) / contentLength);
                        }
                        os.write(buffer, 0, received);
                    }
                    os.close(); os = null;
                    if (!new File(tempDst).renameTo(new File(mDst))) {
                        throw new Exception("Cannot move downloaded file into position");
                    }
                } finally {
                    if (os != null) {
                        os.close();
                    }
                    if (is != null) {
                        is.close();
                    }
                    if (conn != null) {
                        conn.disconnect();
                    }
                }
            } catch (Exception e) {
                mThrowable = e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (mThrowable != null) {
                mCb.onFail(mThrowable);
            } else {
                mCb.onSuccess(mDst);
            }
        }

        @Override
        protected void onProgressUpdate(Double... values) {
            mCb.onProgress(TASK_NAME, values[0].floatValue());
        }

    }

}
