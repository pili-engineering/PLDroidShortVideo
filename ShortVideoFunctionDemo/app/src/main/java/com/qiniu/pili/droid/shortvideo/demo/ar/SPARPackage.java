package com.qiniu.pili.droid.shortvideo.demo.ar;

import android.content.Context;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

public class SPARPackage {

    private static final String MANIFEST = "manifest.json";

    private static final String KEY_PACKAGE = "package";

    private String mURL;
    private Context mContext;
    private HashMap<String, String> mFileMap = new HashMap<>();

    SPARPackage(Context context, String url) {
        mURL = url;
        mContext = context;
        // TODO: async?
        try {
            createFileMap();
        } catch (Exception e) {
            // ignored
        }
    }

    public String getPackageURL() {
        return mURL;
    }

    public String getDownloadPath() {
        return String.format("%s.zip", getUnpackPath());
    }

    public String getUnpackPath() {
        String downloadPath = Downloader.getDownloadPath(mContext, Downloader.PACKAGES_PATH);
        String localName = Downloader.getLocalName(mURL);
        return String.format("%s/%s", downloadPath, localName);
    }

    private String getFileLocalPath(String fileName) {
        return String.format("%s/%s", getUnpackPath(), fileName);
    }

    public String getManifestURL() {
        return String.format("file://%s", getFileLocalPath(MANIFEST));
    }

    private void createFileMap() throws Exception {
        JSONObject manifest = new JSONObject(getFileContent(MANIFEST));
        JSONObject pkg = manifest.getJSONObject(KEY_PACKAGE);
        Iterator<String> keys = pkg.keys();
        while (keys.hasNext()) {
            String url = keys.next();
            String fileName = pkg.getString(url);
            String localPath = getFileLocalPath(fileName);
            mFileMap.put(url, localPath);
        }
        String manifestPath = getFileLocalPath(MANIFEST);
        mFileMap.put(getManifestURL(), manifestPath);
    }

    public String getURLLocalPath(String url) {
        return mFileMap.get(url);
    }

    private String getFileContent(String fileName) throws IOException {
        File file = new File(getFileLocalPath(fileName));
        BufferedReader br = new BufferedReader(new FileReader(file));
        try {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append('\n');
            }
            return sb.toString();
        } finally {
            br.close();
        }
    }

    public void deploy(final boolean force, final AsyncCallback<Void> cb) {
        final String downloadPath = getDownloadPath();
        Downloader.download(mURL, downloadPath, force, new AsyncCallback<String>() {
            @Override
            public void onSuccess(String result) {
                String unpackPath = getUnpackPath();
                Unpacker.unpack(downloadPath, unpackPath, true, new AsyncCallback<String>() {
                    @Override
                    public void onSuccess(String result) {
                        try {
                            createFileMap();
                        } catch (Exception e) {
                            cb.onFail(e);
                            return;
                        }
                        cb.onSuccess(null);
                    }

                    @Override
                    public void onFail(Throwable t) {
                        cb.onFail(t);
                    }

                    @Override
                    public void onProgress(String taskName, float progress) {
                        cb.onProgress(taskName, progress);
                    }
                });
            }

            @Override
            public void onFail(Throwable t) {
                cb.onFail(t);
            }

            @Override
            public void onProgress(String taskName, float progress) {
                cb.onProgress(taskName, progress);
            }
        });
    }

    public void destroy() {
        Util.deleteQuietly(getDownloadPath());
        Util.deleteQuietly(getUnpackPath());
    }

}

