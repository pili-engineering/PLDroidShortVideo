package com.qiniu.pili.droid.shortvideo.demo.ar;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by qinsi on 9/26/16.
 */

public class SPARAppCache {

    private static final String PREF_SPAR_APP_CACHE = "spar_app_cache";

    private HashMap<String, SPARApp> mApps = new HashMap<>();
    private HashMap<String, SPARApp> mAppsByTarget = new HashMap<>();

    private Context mContext;
    private SharedPreferences mPref;

    public SPARAppCache(Context context) {
        mContext = context;
        mPref = mContext.getSharedPreferences(PREF_SPAR_APP_CACHE, Context.MODE_PRIVATE);
        loadCacheInfo();
    }

    private void updateAppTargetIndex(SPARApp app) {
        SPARTarget target = app.getTarget();
        if (target != null) {
            mAppsByTarget.put(target.uid, app);
        }
    }

    private void loadCacheInfo() {
        for (Map.Entry<String, ?> entry : mPref.getAll().entrySet()) {
            String arid = entry.getKey();
            String infoStr = (String) entry.getValue();
            try {
                SPARApp app = SPARApp.fromJSONObject(mContext, new JSONObject(infoStr));
                mApps.put(arid, app);
                updateAppTargetIndex(app);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public String getURLLocalPath(String url) {
        for (SPARApp app : mApps.values()) {
            SPARTarget target = app.getTarget();
            if (target != null && url.equals(target.url)) return app.getTargetURLLocalPath();
            String res = app.getPackageFileURLLocalPath(url);
            if (res != null) return res;
        }
        return null;
    }

    public SPARApp getApp(String arid) {
        return mApps.get(arid);
    }

    public SPARApp getAppByTarget(String targetDesc) {
        String targetUid = "";
        try {
            JSONObject target = new JSONObject(targetDesc);
            targetUid = target.getJSONArray(SPARApp.KEY_IMAGES).getJSONObject(0).getString(SPARTarget.KEY_UID);
        } catch (JSONException e) {
            // ignored
        }
        return mAppsByTarget.get(targetUid);
    }

    public long getAppLastUpdateTime(String arid) {
        SPARApp app = getApp(arid);
        if (app == null) return 0;
        return app.getTimestamp();
    }

    public void updateApp(SPARApp app) {
        final String arid = app.getARID();
        mApps.put(arid, app);
        updateAppTargetIndex(app);
        try {
            mPref.edit().putString(arid, app.toJSONObject().toString()).commit();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void clearCache() {
        mApps.clear();
        mAppsByTarget.clear();
        mPref.edit().clear().commit();
    }

}
