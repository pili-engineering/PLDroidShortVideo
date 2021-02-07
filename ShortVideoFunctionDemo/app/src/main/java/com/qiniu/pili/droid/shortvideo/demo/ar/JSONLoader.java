package com.qiniu.pili.droid.shortvideo.demo.ar;

import android.os.AsyncTask;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

class JSONLoader {

    public static void loadFromURL(String url, AsyncCallback<JSONObject> cb) {
        new FetchJSONTask(url, cb).execute();
    }

    private static class FetchJSONTask extends AsyncTask<Void, Double, JSONObject> {
        private String mURL;
        private AsyncCallback<JSONObject> mCb;
        private Throwable mThrowable;

        public FetchJSONTask(String url, AsyncCallback<JSONObject> cb) {
            mURL = url;
            mCb = cb;
            mThrowable = null;
        }

        @Override
        protected JSONObject doInBackground(Void... voids) {
            try {
                StringBuilder sb = new StringBuilder();
                URL url = new URL(mURL);
                HttpURLConnection conn = null;
                BufferedReader br = null;
                try {
                    conn = (HttpURLConnection) url.openConnection();
                    InputStream is = new BufferedInputStream(conn.getInputStream());
                    br = new BufferedReader(new InputStreamReader(is));
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                    }
                    return new JSONObject(sb.toString());
                } finally {
                    if (conn != null) {
                        conn.disconnect();
                    }
                    if (br != null) {
                        br.close();
                    }
                }
            } catch (Exception e) {
                mThrowable = e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject jso) {
            if (mThrowable != null) {
                mCb.onFail(mThrowable);
            } else {
                mCb.onSuccess(jso);
            }
        }

    }

}
