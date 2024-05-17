package com.qiniu.pili.droid.shortvideo.demo.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;

import com.qiniu.pili.droid.shortvideo.demo.R;
import com.qiniu.pili.droid.shortvideo.demo.view.IndicatorSeekBar;

public class ConfigActivity extends AppCompatActivity {
    // PLRecordSetting
    public static final long DEFAULT_MIN_RECORD_DURATION = 3 * 1000;
    public static final long DEFAULT_MAX_RECORD_DURATION = 10 * 1000;
    public static final boolean DEFAULT_RECORDING_SPEED_VARIABLE_ENABLED = true;

    // PLCameraSetting 镜头参数
    public static final int DEFAULT_PREVIEW_SIZE_RATIO_POS = 0;
    public static final int DEFAULT_PREVIEW_SIZE_LEVEL_POS = 3;

    // PLVideoEncodeSetting
    public static final int DEFAULT_VIDEO_ENCODE_SIZE_LEVEL_POS = 7;
    public static final int DEFAULT_VIDEO_ENCODE_BITRATE_LEVEL_POS = 2;
    public static final boolean DEFAULT_VIDEO_HARDWARE_ENCODE_ENABLE = false;

    // PLAudioEncodeSetting
    public static final int DEFAULT_AUDIO_SAMPLE_RATE = 44100;
    public static final int DEFAULT_AUDIO_CHANNEL_NUM_POS = 0;
    public static final int DEFAULT_AUDIO_ENCODE_BITRATE_LEVEL_POS = 2;
    public static final boolean DEFAULT_AUDIO_HARDWARE_ENCODE_ENABLE = false;

    // PLMicrophoneSetting
    public static final int DEFAULT_SAMPLING_SAMPLE_RATE = 44100;
    public static final int DEFAULT_SAMPLING_FORMAT_POS = 2;
    public static final int DEFAULT_SAMPLING_SOURCE_POS = 1;
    public static final int DEFAULT_SAMPLING_CHANNEL_NUM_POS = 0;
    public static final boolean DEFAULT_SAMPLING_BLUETOOTH_SCO_ENABLE = false;
    public static final boolean DEFAULT_SAMPLING_AUDIO_PTS_OPTIMIZE_ENABLE = false;
    public static final boolean DEFAULT_SAMPLING_NSE_ENABLE = false;
    public static final boolean DEFAULT_SAMPLING_AEC_ENABLE = false;

    // PLWatermarkSetting
    public static final int DEFAULT_WATERMARK_SETTING_TYPE_POS = 0;
    public static final int DEFAULT_WATERMARK_ALPHA = 255;
    public static final float DEFAULT_WATERMARK_WIDTH = 0f;
    public static final float DEFAULT_WATERMARK_HEIGHT = 0f;
    public static final float DEFAULT_WATERMARK_POSITION_X = 0f;
    public static final float DEFAULT_WATERMARK_POSITION_Y = 0f;

    public static final boolean DEFAULT_MIRROR_ENCODE_ENABLE = false;

    // SharedPreferences 保存字段的 key
    public static final String SP_NAME = "Config";

    // PLRecordSetting
    public static final String KEY_MAX_RECORD_DURATION = "MAX_RECORD_DURATION";
    public static final String KEY_RECORDING_SPEED_VARIABLE_ENABLED = "RECORDING_SPEED_VARIABLE_ENABLED";

    // PLShortVideoRecorder
    public static final String KEY_MIRROR_ENCODE_ENABLE = "MIRROR_ENCODE_ENABLE";

    // PLCameraSetting 镜头参数
    public static final String KEY_PREVIEW_SIZE_RATIO_POS = "PREVIEW_SIZE_RATIO_POS";
    public static final String KEY_PREVIEW_SIZE_LEVEL_POS = "PREVIEW_SIZE_LEVEL_POS";

    // PLVideoEncodeSetting
    public static final String KEY_VIDEO_ENCODE_SIZE_LEVEL_POS = "VIDEO_ENCODE_SIZE_LEVEL_POS";
    public static final String KEY_VIDEO_ENCODE_BITRATE_LEVEL_POS = "VIDEO_ENCODE_BITRATE_LEVEL_POS";
    public static final String KEY_VIDEO_ENCODE_HARDWARE_CODEC_ENABLE = "VIDEO_ENCODE_HARDWARE_CODEC_ENABLE";

    // PLAudioEncodeSetting
    public static final String KEY_AUDIO_SAMPLE_RATE = "AUDIO_SAMPLE_RATE";
    public static final String KEY_AUDIO_CHANNEL_NUM_POS = "AUDIO_CHANNEL_NUM_POS";
    public static final String KEY_AUDIO_ENCODE_HARDWARE_CODEC_ENABLE = "AUDIO_ENCODE_HARDWARE_CODEC_ENABLE";
    public static final String KEY_AUDIO_ENCODE_BITRATE_LEVEL_POS = "AUDIO_ENCODE_BITRATE_LEVEL_POS";

    // PLMicrophoneSetting
    public static final String KEY_SAMPLING_SAMPLE_RATE = "SAMPLING_SAMPLE_RATE";
    public static final String KEY_SAMPLING_FORMAT_POS = "SAMPLING_FORMAT_POS";
    public static final String KEY_SAMPLING_SOURCE_POS = "SAMPLING_SOURCE_POS";
    public static final String KEY_SAMPLING_CHANNEL_NUM_POS = "SAMPLING_CHANNEL_NUM_POS";
    public static final String KEY_SAMPLING_BLUETOOTH_SCO_ENABLE = "SAMPLING_BLUETOOTH_SCO_ENABLE";
    public static final String KEY_SAMPLING_AUDIO_PTS_OPTIMIZE_ENABLE = "SAMPLING_AUDIO_PTS_OPTIMIZE_ENABLE";
    public static final String KEY_SAMPLING_NSE_ENABLE = "SAMPLING_NSE_ENABLE";
    public static final String KEY_SAMPLING_AEC_ENABLE = "SAMPLING_AEC_ENABLE";

    // PLWatermarkSetting
    public static final String KEY_WATERMARK_SETTING_TYPE_POS = "WATERMARK_SETTING_TYPE_POS";
    public static final String KEY_WATERMARK_ALPHA = "WATERMARK_ALPHA";
    public static final String KEY_WATERMARK_WIDTH = "WATERMARK_WIDTH";
    public static final String KEY_WATERMARK_HEIGHT = "WATERMARK_HEIGHT";
    public static final String KEY_WATERMARK_POSITION_X = "WATERMARK_POSITION_X";
    public static final String KEY_WATERMARK_POSITION_Y = "WATERMARK_POSITION_Y";

    private EditText maxRecordDurationEditText;
    private EditText audioSampleRateEditText;
    private EditText samplingSampleRateEditText;
    private IndicatorSeekBar watermarkAlphaSeekBar;
    private IndicatorSeekBar watermarkWidthSeekBar;
    private IndicatorSeekBar watermarkHeightSeekBar;
    private EditText watermarkPositionXEditText;
    private EditText watermarkPositionYEditText;
    private Switch recordingSpeedVariableSwitch;
    private Switch mirrorEncodeSwitch;
    private Spinner previewSizeRatioSpinner;
    private Spinner encodingSizeLevelSpinner;
    private Spinner encodingBitrateLevelSpinner;
    private Switch encodingHWCodecSwitch;
    private Spinner audioChannelNumSpinner;
    private Switch audioEncodeModeLevelSwitch;
    private Spinner audioBitrateLevelSpinner;
    private Spinner samplingFormatSpinner;
    private Spinner samplingSourceSpinner;
    private Spinner samplingChannelNumSpinner;
    private Switch samplingBluetoothSCOSwitch;
    private Switch samplingAudioPtsOptimizeSwitch;
    private Switch samplingNSESwitch;
    private Switch samplingAECSwitch;
    private Spinner watermarkSpinner;
    private Spinner previewSizeLevelSpinner;

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        initView();
        prefs = getApplicationContext().getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        setMaxRecordDuration();
        setRecordingSpeedVariableEnabled();
        setMirrorEncode();
        setCameraSetting();
        setVideoEncoding();
        setAudioEncodeSetting();
        setMicrophoneSetting();
        setWatermarkSetting();
    }

    private void initView() {
        maxRecordDurationEditText = findViewById(R.id.max_record_duration_edittext);
        recordingSpeedVariableSwitch = findViewById(R.id.record_speed_variable);
        recordingSpeedVariableSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // 可变帧率模式下，仅支持使用硬编
            if (isChecked) {
                encodingHWCodecSwitch.setChecked(true);
            } else {
                boolean isEncodingHardwareEnable = prefs.getBoolean(KEY_VIDEO_ENCODE_HARDWARE_CODEC_ENABLE, DEFAULT_VIDEO_HARDWARE_ENCODE_ENABLE);
                encodingHWCodecSwitch.setChecked(isEncodingHardwareEnable);
            }
            encodingHWCodecSwitch.setEnabled(!isChecked);
        });
        mirrorEncodeSwitch = findViewById(R.id.mirror_encode_switch);
        previewSizeLevelSpinner = findViewById(R.id.preview_size_level_spinner);
        previewSizeRatioSpinner = findViewById(R.id.preview_size_ratio_spinner);
        encodingSizeLevelSpinner = findViewById(R.id.encoding_size_level_spinner);
        encodingBitrateLevelSpinner = findViewById(R.id.encoding_bitrate_level_spinner);
        encodingHWCodecSwitch = findViewById(R.id.encoding_hardware_codec_switch);
        audioChannelNumSpinner = findViewById(R.id.audio_channel_num_spinner);
        audioEncodeModeLevelSwitch = findViewById(R.id.audio_encode_mode_level_switch);
        audioBitrateLevelSpinner = findViewById(R.id.audio_bitrate_level_spinner);
        audioSampleRateEditText = findViewById(R.id.audio_sample_rate_edittext);
        samplingFormatSpinner = findViewById(R.id.sampling_format_spinner);
        samplingSourceSpinner = findViewById(R.id.sampling_source_spinner);
        samplingChannelNumSpinner = findViewById(R.id.sampling_channel_num_spinner);
        samplingBluetoothSCOSwitch = findViewById(R.id.sampling_bluetooth_sco_switch);
        samplingAudioPtsOptimizeSwitch = findViewById(R.id.sampling_audio_pts_optimize_switch);
        samplingNSESwitch = findViewById(R.id.sampling_nse_switch);
        samplingAECSwitch = findViewById(R.id.sampling_aec_switch);
        samplingSampleRateEditText = findViewById(R.id.sampling_sample_rate_edittext);
        watermarkSpinner = findViewById(R.id.watermark_spinner);
        watermarkPositionYEditText = findViewById(R.id.watermark_positionY_edittext);
        watermarkPositionXEditText = findViewById(R.id.watermark_positionX_edittext);
        watermarkAlphaSeekBar = findViewById(R.id.watermark_alpha_seekBar);
        watermarkHeightSeekBar = findViewById(R.id.watermark_height_seekbar);
        watermarkWidthSeekBar = findViewById(R.id.watermark_width_seekbar);
    }

    public void onClickBack(View view) {
        finish();
    }

    private void setMaxRecordDuration() {
        long maxRecordDuration = prefs.getLong(KEY_MAX_RECORD_DURATION, DEFAULT_MAX_RECORD_DURATION);
        maxRecordDurationEditText.setText(String.valueOf(maxRecordDuration));
    }

    private void setRecordingSpeedVariableEnabled() {
        boolean speedVariableEnabled = prefs.getBoolean(
                KEY_RECORDING_SPEED_VARIABLE_ENABLED, DEFAULT_RECORDING_SPEED_VARIABLE_ENABLED);
        recordingSpeedVariableSwitch.setChecked(speedVariableEnabled);
    }

    private void setMirrorEncode() {
        boolean isMirrorEncode = prefs.getBoolean(KEY_MIRROR_ENCODE_ENABLE, DEFAULT_MIRROR_ENCODE_ENABLE);
        mirrorEncodeSwitch.setChecked(isMirrorEncode);
    }

    private void setCameraSetting() {
        int previewSizeRatioPos = prefs.getInt(KEY_PREVIEW_SIZE_RATIO_POS, DEFAULT_PREVIEW_SIZE_RATIO_POS);
        int previewSizeLevelPos = prefs.getInt(KEY_PREVIEW_SIZE_LEVEL_POS, DEFAULT_PREVIEW_SIZE_LEVEL_POS);

        previewSizeRatioSpinner.setSelection(previewSizeRatioPos);
        previewSizeLevelSpinner.setSelection(previewSizeLevelPos);
    }

    private void setVideoEncoding() {
        int encodingSizeLevelPos = prefs.getInt(KEY_VIDEO_ENCODE_SIZE_LEVEL_POS, DEFAULT_VIDEO_ENCODE_SIZE_LEVEL_POS);
        int encodingBitrateLevelPos = prefs.getInt(KEY_VIDEO_ENCODE_BITRATE_LEVEL_POS, DEFAULT_VIDEO_ENCODE_BITRATE_LEVEL_POS);
        boolean isEncodingHardwareEnable = prefs.getBoolean(KEY_VIDEO_ENCODE_HARDWARE_CODEC_ENABLE, DEFAULT_VIDEO_HARDWARE_ENCODE_ENABLE);

        encodingSizeLevelSpinner.setSelection(encodingSizeLevelPos);
        encodingBitrateLevelSpinner.setSelection(encodingBitrateLevelPos);
        encodingHWCodecSwitch.setChecked(isEncodingHardwareEnable);
    }

    private void setAudioEncodeSetting() {
        int audioSampleRate = prefs.getInt(KEY_AUDIO_SAMPLE_RATE, DEFAULT_AUDIO_SAMPLE_RATE);
        int audioChannelNumPos = prefs.getInt(KEY_AUDIO_CHANNEL_NUM_POS, DEFAULT_AUDIO_CHANNEL_NUM_POS);
        boolean isAudioEncodeHardwareEnable = prefs.getBoolean(KEY_AUDIO_ENCODE_HARDWARE_CODEC_ENABLE, DEFAULT_AUDIO_HARDWARE_ENCODE_ENABLE);
        int audioBitrateLevelPos = prefs.getInt(KEY_AUDIO_ENCODE_BITRATE_LEVEL_POS, DEFAULT_AUDIO_ENCODE_BITRATE_LEVEL_POS);

        audioSampleRateEditText.setText(String.valueOf(audioSampleRate));
        audioChannelNumSpinner.setSelection(audioChannelNumPos);
        audioEncodeModeLevelSwitch.setChecked(isAudioEncodeHardwareEnable);
        audioBitrateLevelSpinner.setSelection(audioBitrateLevelPos);
    }

    private void setMicrophoneSetting() {
        int samplingSampleRate = prefs.getInt(KEY_SAMPLING_SAMPLE_RATE, DEFAULT_SAMPLING_SAMPLE_RATE);
        int samplingFormatPos = prefs.getInt(KEY_SAMPLING_FORMAT_POS, DEFAULT_SAMPLING_FORMAT_POS);
        int samplingSourcePos = prefs.getInt(KEY_SAMPLING_SOURCE_POS, DEFAULT_SAMPLING_SOURCE_POS);
        int samplingChannelNumPos = prefs.getInt(KEY_SAMPLING_CHANNEL_NUM_POS, DEFAULT_SAMPLING_CHANNEL_NUM_POS);
        boolean isSamplingBluetoothSCOEnable = prefs.getBoolean(KEY_SAMPLING_BLUETOOTH_SCO_ENABLE, DEFAULT_SAMPLING_BLUETOOTH_SCO_ENABLE);
        boolean isSamplingAudioPTSOptimizeEnable = prefs.getBoolean(KEY_SAMPLING_AUDIO_PTS_OPTIMIZE_ENABLE, DEFAULT_SAMPLING_AUDIO_PTS_OPTIMIZE_ENABLE);
        boolean isSamplingNSEEnable = prefs.getBoolean(KEY_SAMPLING_NSE_ENABLE, DEFAULT_SAMPLING_NSE_ENABLE);
        boolean isSamplingAECEnable = prefs.getBoolean(KEY_SAMPLING_AEC_ENABLE, DEFAULT_SAMPLING_AEC_ENABLE);

        samplingSampleRateEditText.setText(String.valueOf(samplingSampleRate));
        samplingFormatSpinner.setSelection(samplingFormatPos);
        samplingSourceSpinner.setSelection(samplingSourcePos);
        samplingChannelNumSpinner.setSelection(samplingChannelNumPos);
        samplingBluetoothSCOSwitch.setChecked(isSamplingBluetoothSCOEnable);
        samplingAudioPtsOptimizeSwitch.setChecked(isSamplingAudioPTSOptimizeEnable);
        samplingNSESwitch.setChecked(isSamplingNSEEnable);
        samplingAECSwitch.setChecked(isSamplingAECEnable);
    }

    private void setWatermarkSetting() {
        float watermarkPositionX = prefs.getFloat(KEY_WATERMARK_POSITION_X, DEFAULT_WATERMARK_POSITION_X);
        float watermarkPositionY = prefs.getFloat(KEY_WATERMARK_POSITION_Y, DEFAULT_WATERMARK_POSITION_Y);
        int watermarkAlpha = prefs.getInt(KEY_WATERMARK_ALPHA, DEFAULT_WATERMARK_ALPHA);
        float watermarkWidth = prefs.getFloat(KEY_WATERMARK_WIDTH, DEFAULT_WATERMARK_WIDTH);
        float watermarkHeight = prefs.getFloat(KEY_WATERMARK_HEIGHT, DEFAULT_WATERMARK_HEIGHT);
        int watermarkSettingTypePos = prefs.getInt(KEY_WATERMARK_SETTING_TYPE_POS, DEFAULT_WATERMARK_SETTING_TYPE_POS);

        watermarkPositionXEditText.setText(String.valueOf(watermarkPositionX));
        watermarkPositionYEditText.setText(String.valueOf(watermarkPositionY));
        watermarkAlphaSeekBar.setProgress(transWatermarkAlphaToProgress(watermarkAlpha));
        watermarkWidthSeekBar.setProgress(transWatermarkSizeToProgress(watermarkWidth));
        watermarkHeightSeekBar.setProgress(transWatermarkSizeToProgress(watermarkHeight));
        watermarkSpinner.setSelection(watermarkSettingTypePos);
    }

    private int transWatermarkAlphaToProgress(int watermarkAlpha) {
        return (int) (100.0 * (1 - (watermarkAlpha * 1.0 / 255)));
    }

    private int transWatermarkSizeToProgress(float size) {
        return (int) (size * 100f);
    }

    private int transProgressToWatermarkAlpha(int progress) {
        return (int) ((1.0 - (float) progress / 100) * 255);
    }

    private float transProgressToWatermarkSize(int progress) {
        return (float) progress / 100;
    }

    @Override
    protected void onDestroy() {
        saveSharedPreferences();
        super.onDestroy();
    }

    private void saveSharedPreferences() {
        SharedPreferences.Editor prefsEditor = prefs.edit();
        // PLRecordSetting
        long maxRecordDuration = Long.parseLong(maxRecordDurationEditText.getText().toString());
        if (maxRecordDuration < DEFAULT_MIN_RECORD_DURATION) {
            maxRecordDuration = DEFAULT_MAX_RECORD_DURATION;
        }
        prefsEditor.putLong(KEY_MAX_RECORD_DURATION, maxRecordDuration);
        prefsEditor.putBoolean(KEY_RECORDING_SPEED_VARIABLE_ENABLED, recordingSpeedVariableSwitch.isChecked());

        // PLShortVideoRecorder
        prefsEditor.putBoolean(KEY_MIRROR_ENCODE_ENABLE, mirrorEncodeSwitch.isChecked());

        // PLCameraSetting 镜头参数
        prefsEditor.putInt(KEY_PREVIEW_SIZE_RATIO_POS, previewSizeRatioSpinner.getSelectedItemPosition());
        prefsEditor.putInt(KEY_PREVIEW_SIZE_LEVEL_POS, previewSizeLevelSpinner.getSelectedItemPosition());

        // PLVideoEncodeSetting 视频编码参数
        prefsEditor.putInt(KEY_VIDEO_ENCODE_SIZE_LEVEL_POS, encodingSizeLevelSpinner.getSelectedItemPosition());
        prefsEditor.putInt(KEY_VIDEO_ENCODE_BITRATE_LEVEL_POS, encodingBitrateLevelSpinner.getSelectedItemPosition());
        prefsEditor.putBoolean(KEY_VIDEO_ENCODE_HARDWARE_CODEC_ENABLE, encodingHWCodecSwitch.isChecked());

        // PLAudioEncodeSetting
        int audioSampleRate = !audioSampleRateEditText.getText().toString().isEmpty()
                ? Integer.parseInt(audioSampleRateEditText.getText().toString()) : 44100;
        prefsEditor.putInt(KEY_AUDIO_SAMPLE_RATE, audioSampleRate);
        prefsEditor.putInt(KEY_AUDIO_CHANNEL_NUM_POS, audioChannelNumSpinner.getSelectedItemPosition());
        prefsEditor.putBoolean(KEY_AUDIO_ENCODE_HARDWARE_CODEC_ENABLE, audioEncodeModeLevelSwitch.isChecked());
        prefsEditor.putInt(KEY_AUDIO_ENCODE_BITRATE_LEVEL_POS, audioBitrateLevelSpinner.getSelectedItemPosition());

        // PLMicrophoneSetting
        int samplingSampleRate = !samplingSampleRateEditText.getText().toString().isEmpty()
                ? Integer.parseInt(samplingSampleRateEditText.getText().toString()) : 44100;
        prefsEditor.putInt(KEY_SAMPLING_SAMPLE_RATE, samplingSampleRate);
        prefsEditor.putInt(KEY_SAMPLING_FORMAT_POS, samplingFormatSpinner.getSelectedItemPosition());
        prefsEditor.putInt(KEY_SAMPLING_SOURCE_POS, samplingSourceSpinner.getSelectedItemPosition());
        prefsEditor.putInt(KEY_SAMPLING_CHANNEL_NUM_POS, samplingChannelNumSpinner.getSelectedItemPosition());
        prefsEditor.putBoolean(KEY_SAMPLING_BLUETOOTH_SCO_ENABLE, samplingBluetoothSCOSwitch.isChecked());
        prefsEditor.putBoolean(KEY_SAMPLING_AUDIO_PTS_OPTIMIZE_ENABLE, samplingAudioPtsOptimizeSwitch.isChecked());
        prefsEditor.putBoolean(KEY_SAMPLING_AEC_ENABLE, samplingAECSwitch.isChecked());
        prefsEditor.putBoolean(KEY_SAMPLING_NSE_ENABLE, samplingNSESwitch.isChecked());

        // PLWatermarkSetting
        prefsEditor.putFloat(KEY_WATERMARK_HEIGHT, transProgressToWatermarkSize(watermarkHeightSeekBar.getProgress()));
        prefsEditor.putFloat(KEY_WATERMARK_WIDTH, transProgressToWatermarkSize(watermarkWidthSeekBar.getProgress()));
        prefsEditor.putInt(KEY_WATERMARK_ALPHA, transProgressToWatermarkAlpha(watermarkAlphaSeekBar.getProgress()));
        float watermarkPositionX = Float.parseFloat(watermarkPositionXEditText.getText().toString());
        float watermarkPositionY = Float.parseFloat(watermarkPositionYEditText.getText().toString());
        if (watermarkPositionX > 1) {
            watermarkPositionX = 0;
        }
        if (watermarkPositionY > 1) {
            watermarkPositionY = 0;
        }
        prefsEditor.putFloat(KEY_WATERMARK_POSITION_X, watermarkPositionX);
        prefsEditor.putFloat(KEY_WATERMARK_POSITION_Y, watermarkPositionY);
        prefsEditor.putInt(KEY_WATERMARK_SETTING_TYPE_POS, watermarkSpinner.getSelectedItemPosition());
        prefsEditor.apply();
    }
}
