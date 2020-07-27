package com.qiniu.pili.droid.shortvideo.demo.ar;

import org.json.JSONObject;

import java.io.File;
import java.net.URLEncoder;
import java.util.Iterator;

public class Util {

    public static void deleteQuietly(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) return;
        if (file.isFile()) {
            file.delete();
        } else if (file.isDirectory()) {
            for (String fn : file.list()) {
                deleteQuietly(filePath + "/" + fn);
            }
            file.delete();
        }
    }

    public static String toQueryString(JSONObject jso) throws Exception {
        StringBuilder sb = new StringBuilder();
        Iterator<String> keys = jso.keys();
        boolean first = true;
        while (keys.hasNext()) {
            if (first) first = false;
            else sb.append("&");
            String key = keys.next();
            sb.append(String.format("%s=%s",
                    URLEncoder.encode(key, "UTF-8"),
                    URLEncoder.encode(jso.getString(key), "UTF-8")
            ));
        }
        return sb.toString();
    }

}
