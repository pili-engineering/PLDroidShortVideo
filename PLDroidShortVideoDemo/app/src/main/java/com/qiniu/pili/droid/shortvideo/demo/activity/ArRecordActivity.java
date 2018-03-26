package com.qiniu.pili.droid.shortvideo.demo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.qiniu.pili.droid.shortvideo.demo.R;
import com.qiniu.pili.droid.shortvideo.demo.ar.AsyncCallback;
import com.qiniu.pili.droid.shortvideo.demo.ar.SPARApp;
import com.qiniu.pili.droid.shortvideo.demo.ar.SPARManager;
import com.qiniu.pili.droid.shortvideo.demo.ar.Unpacker;
import com.qiniu.pili.droid.shortvideo.demo.utils.Config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import cn.easyar.FunctorOfVoidFromPermissionStatusAndString;
import cn.easyar.FunctorOfVoidFromRecordStatusAndString;
import cn.easyar.PermissionStatus;
import cn.easyar.PlayerRecorder;
import cn.easyar.RecordStatus;
import cn.easyar.RecordVideoOrientation;
import cn.easyar.RecordZoomMode;
import cn.easyar.Scene;
import cn.easyar.engine.EasyAR3D;


public class ArRecordActivity extends AppCompatActivity implements View.OnClickListener {
    private String recorderResources;
    private String unpackerPath;

    private String uid = "";
    static String key = "zYnUPaCAWtl4WDH3qLu290KRFA7gCCU2iyI9127chA6gvLQyr9CUlawIjMdC1OXxLwsUWvNN2zI2XIElU8AP2QitdZ4WFAfoA8DdJbos2FL4FnPKiSjX52Avh524oxXLF8iOuZXg4YFSQWgKrhkLsJs8K8NxsEdoWh2UCuRsONxjHAdDX0V871RQMydPAyFzx4L0fTUe";
    static String serverAddr = "http://copapi.easyar.cn";
    static String appKey = "cd48a9265b666690c072cefb187dc1c3";
    static String appSecret = "ccdb6314b418829ef65fbfb3b14c8e30eee13f1f6a5370c5cc43955116c0001d";
    static String arid = "287e6520eff14884be463d61efb40ba8";
    //static String arid = "94b179446c0549fbb1c1e0866be6a6ae";

    private Scene scene;
    private ImageView imageView;
    private Button btn_record;
    boolean started = false;
    String url;
    PlayerRecorder recorder = null;
    private ImageView ivAnim;
    private RelativeLayout rlAnim;
    private TextView tvLoading;
    private ImageButton center;
    private TextView progressText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar_record);
        recorderResources = getFilesDir().getAbsolutePath() + "/easyar3d/RecorderResources.zip";
        unpackerPath = getFilesDir().getAbsolutePath() + "/easyar3d";

        File srcfile=new File(recorderResources);
        if(!srcfile.exists()){
            srcfile.mkdir();
        }
        File dstfile=new File(unpackerPath);
        if(!dstfile.exists()){
            dstfile.mkdir();
        }
        loadResourcesToMobile(recorderResources);
        initSDK();
        initview();
    }

    public void loadResourcesToMobile(String filename) {
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(filename);
            inputStream = this.getAssets().open("RecorderResources.zip");
            byte[] buff = new byte[1024];
            int i = 0;
            while ((i = inputStream.read(buff)) != -1) {
                outputStream.write(buff, 0, i);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (outputStream != null && inputStream != null) {
                    outputStream.flush();
                    inputStream.close();
                    outputStream.close();
                    Unpacker.unpack2(recorderResources, unpackerPath, new AsyncCallback<String>() {
                        @Override
                        public void onSuccess(String result) {

                        }

                        @Override
                        public void onFail(Throwable t) {
                            Toast.makeText(ArRecordActivity.this, "", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onProgress(String taskName, float progress) {

                        }
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void initSDK() {
        EasyAR3D.initialize(this, key);
        final SPARManager sm = SPARManager.getInstance(this);
        Scene.setUriTranslator(new Scene.IUriTranslator() {
            @Override
            public String tryTranslateUriPathToLocalPath(String uri) {
                if (uri.equals("local://Recorder.json")) {
                    return getFilesDir().getAbsolutePath() + "/easyar3d/Recorder.json";
                } else if (uri.equals("local://Recorder.js")) {
                    return getFilesDir().getAbsolutePath() + "/easyar3d/Recorder.js";
                } else if (uri.equals("local://PostBasic.effect")) {
                    return getFilesDir().getAbsolutePath() + "/easyar3d/PostBasic.effect";
                }
                return sm.getURLLocalPath(uri);
            }
        });
        scene = new Scene(this, true);
    }

    private void initview() {
        final ViewGroup previewGroup = (ViewGroup) findViewById(R.id.preview);
        imageView = new ImageView(this);
        imageView.setVisibility(View.GONE);
        previewGroup.addView(scene, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        previewGroup.addView(imageView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        btn_record = (Button) findViewById(R.id.ar_record);
        btn_record.setOnClickListener(this);
        center = (ImageButton) findViewById(R.id.center);
        center.setOnClickListener(this);

        ivAnim = (ImageView) findViewById(R.id.iv_anim);
        rlAnim = (RelativeLayout) findViewById(R.id.rl_anim);
        tvLoading = (TextView) findViewById(R.id.tv_loading);
        Animation animation = AnimationUtils.loadAnimation(ArRecordActivity.this, R.anim.anim);
        animation.setInterpolator(new LinearInterpolator());
        ivAnim.startAnimation(animation);

        progressText = (TextView) findViewById(R.id.progress_text);

        loadScene("scene.js");
        if(TextUtils.isEmpty(appKey)&&TextUtils.isEmpty(appSecret)&&TextUtils.isEmpty(arid)){
            Toast.makeText(this, "数据KEY为空,无法加载", Toast.LENGTH_SHORT).show();
        }else{
            loadARID(arid);
        }
    }

    private String prepareUrl() {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        String fileName = "EasyAR_Recording_" + timestamp + ".mp4";
        File folder = new File(Config.VIDEO_STORAGE_DIR + "ArMovies");
        if (!folder.exists())
            folder.mkdirs();
        return Config.VIDEO_STORAGE_DIR + "ArMovies/" + fileName;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(scene!=null){
            scene.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(scene!=null){
            scene.onPause();
        }
    }

    private void loadScene(final String path) {
        String script = null;
        BufferedReader reader = null;
        try {
            StringBuilder buf = new StringBuilder();
            reader = new BufferedReader(new InputStreamReader(getAssets().open(path), "UTF-8"));
            String str;
            while ((str = reader.readLine()) != null) {
                buf.append(str);
                buf.append("\n");
            }
            script = buf.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        if (script == null)
            return;

        final String final_script = script;

        //load script on ready
        scene.runOnLoaded(new Runnable() {
            @Override
            public void run() {
                scene.setMessageReceiver(new Scene.IReceiver() {
                    @Override
                    public void receiveMessage(String name, String[] params) {
                        if (name.equals("request:JsNativeBinding.openWebView")) {
                            if (null != params && params.length > 0) {
                                Intent intent=new Intent(ArRecordActivity.this,WebViewActivity.class);
                                intent.putExtra("web",params[0]);
                                startActivity(intent);
                            }
                        } else if (name.equals("request:JsNativeBinding.targetLost")) {
                            scene.sendMessage("request:NativeJsBinding.showARWithIMU", new String[]{});
                            uid = params[0];
                        } else if (name.equals("request:JsNativeBinding.targetFound")) {
                            if (!params[0].equals(uid)) {
                                Log.d("TAG", "=-=-=js hidear-=-=-111111" + params[0]);
                                scene.sendMessage("request:NativeJsBinding.hideAR", new String[]{});
                            }
                            rlAnim.setVisibility(View.VISIBLE);
                            uid = params[0];
                            scene.sendMessage("request:NativeJsBinding.showARWithTrack", new String[]{});
                        }
                    }
                });
                scene.loadJavaScript(path, final_script);
            }
        });
    }

    private void loadARID(final String arid) {
        final SPARManager sm = SPARManager.getInstance(this);
        sm.setServerAddress(serverAddr);
        sm.setAccessTokens(appKey, appSecret);
        sm.loadApp(arid, new DefaultCallback<SPARApp>() {
            @Override
            public void onSuccess(SPARApp result) {
                try {
                    final String manifestURL = result.getManifestURL();
                    Log.d("SPAR", manifestURL);
                    String manifestPath = sm.getURLLocalPath(manifestURL);
                    Log.d("SPAR", manifestPath);

                    scene.runOnLoaded(new Runnable() {
                        @Override
                        public void run() {
                            scene.loadManifest(manifestURL);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        final Handler h = new Handler(getMainLooper());
        switch (view.getId()) {
            case R.id.center:
                scene.sendMessage("request:NativeJsBinding.resetContent",new String[]{});
                break;
            case R.id.ar_record:
                if (started) {
                    btn_record.setText(getString(R.string.ar_record));
                    progressText.setVisibility(View.INVISIBLE);
                    h.removeCallbacksAndMessages(null);
                    started = false;
                    if (recorder != null) {
                        recorder.stop();
                        Toast.makeText(ArRecordActivity.this, "Recorded at " + url, Toast.LENGTH_LONG).show();
                        recorder.dispose();
                        recorder = null;
                        VideoEditActivity.start(ArRecordActivity.this, url);
                    }
                } else {
                    if (!PlayerRecorder.isAvailable()) {
                        Toast.makeText(ArRecordActivity.this, "Recorder Module Not Available", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    btn_record.setText(getString(R.string.ar_stop));
                    progressText.setText("00:00");
                    h.postDelayed(new Runnable() {
                        String s = progressText.getText().toString();
                        int min = Integer.parseInt(s.substring(0, 2));
                        int sec = Integer.parseInt(s.substring(3, 5));
                        @Override
                        public void run() {
                            sec++;
                            if (sec == 60) {
                                sec = 0;
                                min++;
                            }
                            String minS = min < 10 ? "0" + min : Integer.toString(min);
                            String secS = sec < 10 ? "0" + sec : Integer.toString(sec);
                            progressText.setText(minS + ":" + secS);
                            h.postDelayed(this, 1000);
                        }
                    }, 1000);
                    progressText.setVisibility(View.VISIBLE);
                    started = true;
                    PlayerRecorder.Configuration conf = new PlayerRecorder.Configuration();
                    conf.Identifier = "04_RecordPass";
                    url = prepareUrl();
                    conf.OutputFilePath = url;
                    conf.VideoOrientation = RecordVideoOrientation.Portrait;
                    conf.ZoomMode = RecordZoomMode.ZoomInWithAllContent;
                    recorder = scene.createRecorder(conf);
                    recorder.requestPermissions(new FunctorOfVoidFromPermissionStatusAndString() {
                        @Override
                        public void invoke(int status, String msg) {

                            switch (status) {
                                case PermissionStatus.Denied:
                                    started = false;
                                    Log.e("HelloAR", "Permission Denied" + msg);
                                    Toast.makeText(ArRecordActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
                                    fail();
                                    break;
                                case PermissionStatus.Error:
                                    started = false;
                                    Log.e("HelloAR", "Permission Error" + msg);
                                    Toast.makeText(ArRecordActivity.this, "Permission Error", Toast.LENGTH_SHORT).show();
                                    fail();
                                    break;
                                case PermissionStatus.Granted:
                                    Log.i("HelloAR", "Permission Granted");

                                    recorder.open(new FunctorOfVoidFromRecordStatusAndString() {
                                        @Override
                                        public void invoke(int status, String value) {
                                            switch (status) {
                                                case RecordStatus.OnStarted:
                                                    break;
                                                case RecordStatus.OnStopped:
                                                    break;
                                                case RecordStatus.FileFailed:
                                                    fail();
                                                    break;
                                                default:
                                                    break;

                                            }
                                            Log.i("HelloAR", "Recorder Callback status: " + Integer.toString(status) + ", MSG: " + value);
                                        }
                                    });
                                    recorder.start();
                                    Toast.makeText(ArRecordActivity.this, "Recording...", Toast.LENGTH_SHORT).show();
                                    break;
                            }
                        }
                    });
                }
                break;
        }
    }

    private void fail() {
        btn_record.setText(getString(R.string.ar_record));
        started = false;
        url = null;
        if (recorder != null) {
            recorder.dispose();
            recorder = null;
        }
    }


    private  abstract class DefaultCallback<T> implements AsyncCallback<T> {
        @Override
        public void onFail(Throwable t) {
            t.printStackTrace();
        }

        @Override
        public void onProgress(String taskName, float progress) {
            if (null != taskName && taskName.equals("download")) {
                rlAnim.setVisibility(View.VISIBLE);
                tvLoading.setText(String.format("loading...\n%.2f%%", progress * 100));
                System.out.println("正在下载"+String.format("loading...\n%.2f%%", progress * 100));
            } else if (null != taskName && taskName.equals("unpack")) {
                if (rlAnim.getVisibility() != View.GONE) {
                    rlAnim.setVisibility(View.GONE);
                }
            }
        }
    }

}
