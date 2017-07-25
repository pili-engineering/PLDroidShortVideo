package com.kiwi.ui.widget;

import android.widget.SeekBar;
import android.widget.TextView;

public class SeekBarRow {
    public SeekBar seekBar;
    public TextView value;
    public int id;
    public int initValue;

    public SeekBarRow(int id, SeekBar seekBar, TextView value, int initValue) {
        this.seekBar = seekBar;
        this.value = value;
        this.id = id;
        this.initValue = initValue;
    }
}
