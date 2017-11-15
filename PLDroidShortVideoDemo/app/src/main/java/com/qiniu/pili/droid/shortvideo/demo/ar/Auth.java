package com.qiniu.pili.droid.shortvideo.demo.ar;

import org.json.JSONObject;

import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.TimeZone;

/**
 * Created by qinsi on 9/13/16.
 */
class Auth {

    private static final String KEY_DATE = "date";
    private static final String KEY_APP_KEY = "appKey";
    private static final String KEY_SIGNATURE = "signature";

    private static byte[] shasum(String data) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        return digest.digest(data.getBytes("UTF-8"));
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public static String sha1Hex(String str) throws Exception {
        return bytesToHex(shasum(str));
    }

    public static String generateSignature(JSONObject jso, String appSecret) throws Exception {
        ArrayList<String> keyList = new ArrayList<>();
        Iterator<String> keys = jso.keys();
        while (keys.hasNext()) {
            keyList.add(keys.next());
        }

        Collections.sort(keyList);

        StringBuilder sb = new StringBuilder();
        for (String key : keyList) {
            sb.append(key + jso.getString(key));
        }
        sb.append(appSecret);

        return sha1Hex(sb.toString());
    }

    public static JSONObject signParam(JSONObject param, String appKey, String appSecret) throws Exception {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        df.setTimeZone(tz);
        param.put(KEY_DATE, df.format(new Date()));
        param.put(KEY_APP_KEY, appKey);
        param.put(KEY_SIGNATURE, generateSignature(param, appSecret));
        return param;
    }

}
