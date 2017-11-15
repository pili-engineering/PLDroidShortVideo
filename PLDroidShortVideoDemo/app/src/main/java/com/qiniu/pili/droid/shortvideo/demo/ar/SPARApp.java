package com.qiniu.pili.droid.shortvideo.demo.ar;

import android.content.Context;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Iterator;

public class SPARApp {

    private static final String KEY_ARID = "arid";
    private static final String KEY_PACKAGE_URL = "packageURL";
    private static final String KEY_TIMESTAMP = "timestamp";

    static final String KEY_IMAGES = "images";
    private static final String KEY_SIZE = "size";

    public static SPARApp fromJSONObject(Context context, JSONObject jso) throws JSONException {
        String arid = jso.getString(KEY_ARID);
        String packageURL = jso.getString(KEY_PACKAGE_URL);
        long timestamp = jso.getLong(KEY_TIMESTAMP);
        SPARApp res = new SPARApp(context, arid, packageURL, timestamp);
        try {
            SPARTarget target = SPARTarget.fromJSONObject(jso);
            res.mTarget = target;
        } catch (JSONException e) {
            // ignored
        }
        return res;
    }

    public JSONObject toJSONObject() throws JSONException {
        JSONObject res = mTarget == null ? new JSONObject() : mTarget.toJSONObject();
        res.put(KEY_ARID, mARID);
        res.put(KEY_PACKAGE_URL, mPackage.getPackageURL());
        res.put(KEY_TIMESTAMP, mTimestamp);
        return res;
    }

    private Context mContext;
    private String mARID;
    private long mTimestamp;
    private SPARTarget mTarget;
    private SPARPackage mPackage;

    public SPARApp(Context context, String arid, String packageURL) {
        this(context, arid, packageURL, 0);
    }

    public SPARApp(Context context, String arid, String packageURL, long timestamp) {
        mContext = context;
        mARID = arid;
        mPackage = new SPARPackage(mContext, packageURL);
        mTimestamp = timestamp;
    }

    public String getARID() {
        return mARID;
    }

    public long getTimestamp() {
        return mTimestamp;
    }

    public SPARTarget getTarget() {
        return mTarget;
    }

    public String getTargetURL() {
        if (mTarget == null) return null;
        return mTarget.url;
    }

    public String getTargetDesc() {
        JSONObject res = new JSONObject();
        try {
            JSONArray jsa = new JSONArray();
            JSONObject desc = new JSONObject();
            Iterator<String> keys = mTarget.desc.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                if (KEY_SIZE.equals(key)) {
                    JSONArray size = new JSONArray(mTarget.desc.getString(KEY_SIZE));
                    desc.put(key, size);
                } else {
                    desc.put(key, mTarget.desc.get(key));
                }
            }
            jsa.put(desc);
            res.put(KEY_IMAGES, jsa);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return res.toString();
    }

    public SPARPackage getPackage() {
        return mPackage;
    }

    public boolean hasTarget() {
        return mTarget != null && !TextUtils.isEmpty(mTarget.url);
    }

    public String getTargetURLLocalPath() {
        if (!hasTarget()) return null;
        String downloadPath = Downloader.getDownloadPath(mContext, Downloader.TARGETS_PATH);
        String downloadFileName = Downloader.getLocalName(mTarget.url);
        return String.format("%s/%s", downloadPath, downloadFileName);
    }

    public String getPackageFileURLLocalPath(String url) {
        return mPackage.getURLLocalPath(url);
    }

    public void prepareTarget(AsyncCallback<String> cb) {
        prepareTarget(false, cb);
    }

    public void prepareTarget(boolean force, AsyncCallback<String> cb) {
        if (!hasTarget()) {
            cb.onFail(new Exception("No target specified"));
            return;
        }

        String dst = getTargetURLLocalPath();
        if (new File(dst).exists() && !force) {
            cb.onSuccess(dst);
            return;
        }

        Downloader.download(mTarget.url, dst, force, cb);
    }

    public void deployPackage(AsyncCallback<Void> cb) {
        deployPackage(false, cb);
    }

    public void deployPackage(boolean force, AsyncCallback<Void> cb) {
        mPackage.deploy(force, cb);
    }

    public String getManifestURL() {
        return mPackage.getManifestURL();
    }

    public void destroy() {
        if (hasTarget()) {
            Util.deleteQuietly(getTargetURLLocalPath());
        }
        mPackage.destroy();
    }

}
