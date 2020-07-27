package com.qiniu.pili.droid.shortvideo.demo.ar;

import org.json.JSONException;
import org.json.JSONObject;

public class SPARTarget {

    public static final String TYPE_IMAGE = "image";
    public static final String KEY_UID = "uid";

    private static final String KEY_TARGET_TYPE = "targetType";
    private static final String KEY_TARGET_DESC = "targetDesc";
    private static final String KEY_IMAGE = "image";

    public String type;
    public String uid;
    public String url;
    public JSONObject desc;

    public SPARTarget(String type, JSONObject desc) {
        this.type = type;
        this.desc = desc;
        try {
            if (TYPE_IMAGE.equals(type)) {
                this.url = desc.getString(KEY_IMAGE);
                this.uid = desc.getString(KEY_UID);
            }
        } catch (JSONException e) {
            // ignored
        }
    }

    public static SPARTarget fromJSONObject(JSONObject jso) throws JSONException {
        String targetType = jso.getString(KEY_TARGET_TYPE);
        JSONObject targetDesc = jso.getJSONObject(KEY_TARGET_DESC);
        SPARTarget res = new SPARTarget(targetType, targetDesc);
        return res;
    }

    public JSONObject toJSONObject() throws JSONException {
        JSONObject res = new JSONObject();
        res.put(KEY_TARGET_TYPE, type);
        res.put(KEY_TARGET_DESC, desc);
        return res;
    }

}
