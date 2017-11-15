package com.qiniu.pili.droid.shortvideo.demo.ar;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SPARManager {

    private static final String KEY_STATUS_CODE = "statusCode";
    private static final String KEY_RESULT = "result";
    private static final String KEY_MESSAGE = "message";
    private static final String KEY_INFO = "info";
    private static final String KEY_TIMESTAMP = "timestamp";
    private static final String KEY_SCENE = "scene";
    private static final String KEY_PACKAGE = "package";

    private static final String KEY_APPS = "apps";
    private static final String KEY_ARID = "arid";
    private static final String KEY_PACKAGE_URL = "packageURL";
    private static final String KEY_TARGET_TYPE = "targetType";
    private static final String KEY_TARGET_DESC = "targetDesc";

    private static final String PACKAGE_PATH = "/mobile/info/";
    private static final String PRELOAD_PATH = "/mobile/preload/";

    private static SPARManager sInst;

    public static SPARManager getInstance(Context context) {
        if (sInst == null) {
            sInst = new SPARManager(context.getApplicationContext());
        }
        return sInst;
    }

    private SPARManager(Context context) {
        mContext = context;
        mCache = new SPARAppCache(context);
        mPreloadCache = new SPARPreloadCache(context);
    }

    private Context mContext;
    private SPARAppCache mCache;
    private SPARPreloadCache mPreloadCache;
    private String mServerAddr;
    private String mAppKey;
    private String mAppSecret;

    public void setServerAddress(String url) {
        mServerAddr = url;
    }

    public void setAccessTokens(String key, String secret) {
        mAppKey = key;
        mAppSecret = secret;
    }

    public String getURLLocalPath(String url) {
        return mCache.getURLLocalPath(url);
    }

    public SPARApp getAppByTarget(String targetDesc) {
        return mCache.getAppByTarget(targetDesc);
    }

    public void clearCache() {
        mCache.clearCache();
        mPreloadCache.clearCache();
        Util.deleteQuietly(Downloader.getDownloadPath(mContext, Downloader.PACKAGES_PATH));
        Util.deleteQuietly(Downloader.getDownloadPath(mContext, Downloader.TARGETS_PATH));
    }

    private class PreloadTasks {

        private List<SPARApp> mApps;
        private List<String> mFinished;

        public PreloadTasks(Context context, List<SPARApp> apps) throws JSONException {
            mApps = apps;
        }

        public void execute(boolean force, final AsyncCallback<List<SPARApp>> cb) {
            mFinished = new ArrayList<>();
            if (mApps == null || mApps.isEmpty()) {
                cb.onFail(new Exception("Empty app list"));
                return;
            }
            for (SPARApp app : mApps) {
                mCache.updateApp(app);
                app.prepareTarget(force, new AsyncCallback<String>() {
                    @Override
                    public void onSuccess(String result) {
                        mFinished.add(result);
                        if (mFinished.size() == mApps.size()) {
                            cb.onSuccess(mApps);
                        }
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

        }

    }

    private List<SPARApp> appsFromJSONArray(JSONArray jsa) throws JSONException {
        List<SPARApp> apps = new ArrayList<>();
        for (int i = 0; i < jsa.length(); i++) {
            JSONObject app = jsa.getJSONObject(i);
            JSONObject jso = new JSONObject();
            jso.put(KEY_ARID, app.get(KEY_ARID));
            jso.put(KEY_PACKAGE_URL, app.getJSONObject(KEY_SCENE).getString(KEY_PACKAGE));
            jso.put(KEY_TIMESTAMP, app.get(KEY_TIMESTAMP));
            if (app.has(KEY_TARGET_TYPE)) jso.put(KEY_TARGET_TYPE, app.get(KEY_TARGET_TYPE));
            if (app.has(KEY_TARGET_DESC)) jso.put(KEY_TARGET_DESC, app.get(KEY_TARGET_DESC));
            apps.add(SPARApp.fromJSONObject(mContext, jso));
        }
        return apps;
    }

    public void preloadApps(String preloadID, final AsyncCallback<List<SPARApp>> cb) {
        preloadApps(preloadID, false, cb);
    }

    public void preloadApps(String preloadID, final boolean force, final AsyncCallback<List<SPARApp>> cb) {
        preloadApps(preloadID, mAppKey, mAppSecret, force, cb);
    }

    private void preloadFromJSON(JSONObject result, boolean force, AsyncCallback<List<SPARApp>> cb) {
        try {
            JSONArray jsa = result.getJSONObject(KEY_RESULT).getJSONArray(KEY_APPS);
            List<SPARApp> apps = appsFromJSONArray(jsa);
            new PreloadTasks(mContext, apps).execute(force, cb);
        } catch (Exception e) {
            cb.onFail(e);
        }
    }

    private boolean tryPreloadFromCache(String preloadID, boolean force, AsyncCallback<List<SPARApp>> cb) {
        JSONObject preloadJSON = mPreloadCache.getPreloadInfo(preloadID);
        if (preloadJSON != null && !force) {
            preloadFromJSON(preloadJSON, force, cb);
            return true;
        }
        return false;
    }

    public void preloadApps(final String preloadID, String appKey, String appSecret, final boolean force, final AsyncCallback<List<SPARApp>> cb) {
        String preloadURL;
        try {
            JSONObject params = Auth.signParam(new JSONObject(), appKey, appSecret);
            String query = Util.toQueryString(params);
            preloadURL = String.format("%s%s%s?%s", mServerAddr, PRELOAD_PATH, preloadID, query);
        } catch (Exception e) {
            cb.onFail(e);
            return;
        }

        JSONLoader.loadFromURL(preloadURL, new AsyncCallback<JSONObject>() {
            @Override
            public void onSuccess(final JSONObject result) {
                try {
                    int statusCode = result.getInt(KEY_STATUS_CODE);
                    if (statusCode != 0) {
                        throw new Exception(result.getJSONObject(KEY_RESULT).getString(KEY_MESSAGE));
                    }

                    preloadFromJSON(result, force, new AsyncCallback<List<SPARApp>>() {
                        @Override
                        public void onSuccess(List<SPARApp> res) {
                            mPreloadCache.updatePreloadInfo(preloadID, result);
                            cb.onSuccess(res);
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
                } catch (Exception e) {
                    cb.onFail(e);
                }
            }

            @Override
            public void onFail(Throwable t) {
                if (!tryPreloadFromCache(preloadID, force, cb)) cb.onFail(t);
            }

            @Override
            public void onProgress(String taskName, float progress) {
                cb.onProgress(taskName, progress);
            }
        });

    }

    public void loadApp(String arid, AsyncCallback<SPARApp> cb) {
        loadApp(arid, false, cb);
    }

    public void loadApp(String arid, boolean force, AsyncCallback<SPARApp> cb) {
        loadApp(arid, mAppKey, mAppSecret, force, cb);
    }

    private boolean tryFromCache(String arid, boolean force, AsyncCallback<SPARApp> cb) {
        SPARApp app = mCache.getApp(arid);
        if (app != null && !force) {
            cb.onSuccess(app);
            return true;
        }
        return false;
    }

    public void loadApp(final String arid, String appKey, String appSecret, final boolean force, final AsyncCallback<SPARApp> cb) {
        String url;
        try {
            JSONObject params = Auth.signParam(new JSONObject(), appKey, appSecret);
            String query = Util.toQueryString(params);
            url = String.format("%s%s%s?%s", mServerAddr, PACKAGE_PATH, arid, query);
        } catch (Exception e) {
            cb.onFail(e);
            return;
        }

        JSONLoader.loadFromURL(url, new AsyncCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject result) {
                try {
                    int statusCode = result.getInt(KEY_STATUS_CODE);
                    if (statusCode != 0) {
                        throw new Exception(result.getJSONObject(KEY_RESULT).getString(KEY_MESSAGE));
                    }

                    JSONObject info = result.getJSONObject(KEY_RESULT).getJSONObject(KEY_INFO);
                    final long timestamp = info.getLong(KEY_TIMESTAMP);
                    String pkgURL = info.getJSONObject(KEY_SCENE).getString(KEY_PACKAGE);

                    if (timestamp <= mCache.getAppLastUpdateTime(arid) && !force) {
                        cb.onSuccess(mCache.getApp(arid));
                        return;
                    }

                    final SPARApp newApp = new SPARApp(mContext, arid, pkgURL, timestamp);
                    newApp.deployPackage(true, new AsyncCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            mCache.updateApp(newApp);
                            cb.onSuccess(newApp);
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
                } catch (Exception e) {
                    cb.onFail(e);
                }
            }

            @Override
            public void onFail(Throwable t) {
                if (!tryFromCache(arid, force, cb)) cb.onFail(t);
            }

            @Override
            public void onProgress(String taskName, float progress) {
                cb.onProgress(taskName, progress);
            }
        });
    }

}
