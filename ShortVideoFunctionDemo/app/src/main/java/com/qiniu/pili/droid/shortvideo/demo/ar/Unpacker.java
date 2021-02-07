package com.qiniu.pili.droid.shortvideo.demo.ar;

import android.os.AsyncTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class Unpacker {

    public static final String TASK_NAME = "unpack";

    // TODO: optional to remove original file
    public static void unpack(String src, String dst, boolean force, AsyncCallback<String> cb) {
        File dstFile = new File(dst);
        if (dstFile.exists() && !force) {
            cb.onSuccess(null);
            return;
        }
        new UnpackTask(src, dst, cb).execute();
    }
    public static void unpack2(String src, String dst, AsyncCallback<String> cb){
        new UnpackTask(src, dst, cb).execute();
    }

    private static class UnpackTask extends AsyncTask<Void, Double, Void> {

        private String mSrc;
        private String mDst;
        private AsyncCallback<String> mCb;
        private Throwable mThrowable;

        UnpackTask(String src, String dst, AsyncCallback<String> cb) {
            mSrc = src;
            mDst = dst;
            mCb = cb;
            mThrowable = null;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                long totalSize = 0;
                ZipFile zf = new ZipFile(mSrc);
                try {
                    Enumeration e = zf.entries();
                    while (e.hasMoreElements()) {
                        ZipEntry ze = (ZipEntry) e.nextElement();
                        totalSize += ze.getSize();
                    }
                } finally {
                    zf.close();
                }

                ZipInputStream zis = new ZipInputStream(new FileInputStream(mSrc));
                try {
                    byte[] buf = new byte[4096];
                    int received = 0, totalReceived = 0;
                    ZipEntry ze;
                    while ((ze = zis.getNextEntry()) != null) {
                        File file = new File(mDst, ze.getName());
                        File dir = ze.isDirectory() ? file : file.getParentFile();
                        if (!dir.isDirectory() && !dir.mkdirs()) {
                            throw new FileNotFoundException("Failed to ensure directory: " + dir.getAbsolutePath());
                        }
                        if (ze.isDirectory()) {
                            continue;
                        }

                        FileOutputStream fos = new FileOutputStream(file);
                        try {
                            while ((received = zis.read(buf)) != -1) {
                                fos.write(buf, 0, received);
                                totalReceived += received;
                                publishProgress(((double) totalReceived) / totalSize);
                            }
                        } finally {
                            fos.close();
                        }
                    }
                } finally {
                    zis.close();
                }
            } catch (IOException e) {
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
