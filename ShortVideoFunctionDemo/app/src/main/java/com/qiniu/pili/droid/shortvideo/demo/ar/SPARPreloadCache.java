package com.qiniu.pili.droid.shortvideo.demo.ar;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SPARPreloadCache {

    private static final String PREF_SPAR_PRELOAD_CACHE = "spar_preload_cache";

    private HashMap<String, JSONObject> mPreloads = new HashMap<>();

    private Context mContext;
    private SharedPreferences mPref;

    public SPARPreloadCache(Context context) {
        mContext = context;
        mPref = mContext.getSharedPreferences(PREF_SPAR_PRELOAD_CACHE, Context.MODE_PRIVATE);
        loadCacheInfo();
    }

    private void loadCacheInfo() {
        for (Map.Entry<String, ?> entry : mPref.getAll().entrySet()) {
            String preloadID = entry.getKey();
            String preloadInfo = (String) entry.getValue();
            try {
                JSONObject preloadJSON = new JSONObject(preloadInfo);
                mPreloads.put(preloadID, preloadJSON);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public JSONObject getPreloadInfo(String preloadID) {
        return mPreloads.get(preloadID);
    }

    public void updatePreloadInfo(String preloadID, JSONObject preloadJSON) {
        mPreloads.put(preloadID, preloadJSON);
        mPref.edit().putString(preloadID, preloadJSON.toString()).commit();
    }

    public void clearCache() {
        mPreloads.clear();
        mPref.edit().clear().commit();
    }

}
