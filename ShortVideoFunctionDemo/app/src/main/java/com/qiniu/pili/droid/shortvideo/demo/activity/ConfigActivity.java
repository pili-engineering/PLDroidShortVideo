package com.qiniu.pili.droid.shortvideo.demo.activity;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.qiniu.pili.droid.shortvideo.demo.R;
import com.qiniu.pili.droid.shortvideo.demo.utils.RecordSettings;

public class ConfigActivity extends AppCompatActivity {

    public static int PREVIEW_SIZE_RATIO_POS = 0;
    public static int PREVIEW_SIZE_LEVEL_POS = 3;
    public static int ENCODING_MODE_LEVEL_POS = 0;
    public static int ENCODING_SIZE_LEVEL_POS = 7;
    public static int ENCODING_BITRATE_LEVEL_POS = 2;
    public static int AUDIO_CHANNEL_NUM_POS = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        Spinner previewSizeRatioSpinner = findViewById(R.id.PreviewSizeRatioSpinner);
        Spinner previewSizeLevelSpinner = findViewById(R.id.PreviewSizeLevelSpinner);
        Spinner encodingModeLevelSpinner = findViewById(R.id.EncodingModeLevelSpinner);
        Spinner encodingSizeLevelSpinner = findViewById(R.id.EncodingSizeLevelSpinner);
        Spinner encodingBitrateLevelSpinner = findViewById(R.id.EncodingBitrateLevelSpinner);
        Spinner audioChannelNumSpinner = findViewById(R.id.AudioChannelNumSpinner);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, RecordSettings.PREVIEW_SIZE_RATIO_TIPS_ARRAY);
        previewSizeRatioSpinner.setAdapter(adapter);
        previewSizeRatioSpinner.setSelection(PREVIEW_SIZE_RATIO_POS);
        previewSizeRatioSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                PREVIEW_SIZE_RATIO_POS = position;
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, RecordSettings.PREVIEW_SIZE_LEVEL_TIPS_ARRAY);
        previewSizeLevelSpinner.setAdapter(adapter);
        previewSizeLevelSpinner.setSelection(PREVIEW_SIZE_LEVEL_POS);
        previewSizeLevelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                PREVIEW_SIZE_LEVEL_POS = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, RecordSettings.ENCODING_MODE_LEVEL_TIPS_ARRAY);
        encodingModeLevelSpinner.setAdapter(adapter);
        encodingModeLevelSpinner.setSelection(ENCODING_MODE_LEVEL_POS);
        encodingModeLevelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ENCODING_MODE_LEVEL_POS = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, RecordSettings.ENCODING_SIZE_LEVEL_TIPS_ARRAY);
        encodingSizeLevelSpinner.setAdapter(adapter);
        encodingSizeLevelSpinner.setSelection(ENCODING_SIZE_LEVEL_POS);
        encodingSizeLevelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ENCODING_SIZE_LEVEL_POS = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, RecordSettings.ENCODING_BITRATE_LEVEL_TIPS_ARRAY);
        encodingBitrateLevelSpinner.setAdapter(adapter);
        encodingBitrateLevelSpinner.setSelection(ENCODING_BITRATE_LEVEL_POS);
        encodingBitrateLevelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ENCODING_BITRATE_LEVEL_POS = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, RecordSettings.AUDIO_CHANNEL_NUM_TIPS_ARRAY);
        audioChannelNumSpinner.setAdapter(adapter);
        audioChannelNumSpinner.setSelection(AUDIO_CHANNEL_NUM_POS);
        audioChannelNumSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                AUDIO_CHANNEL_NUM_POS = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public void onClickBack(View view) {
        finish();
    }
}
