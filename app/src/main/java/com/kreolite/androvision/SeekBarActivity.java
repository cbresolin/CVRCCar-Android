package com.kreolite.androvision;

import android.app.Activity;
import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class SeekBarActivity extends Activity {

    @InjectView(R.id.minHue)
    SeekBar minHueControl;
    @InjectView(R.id.maxHue)
    SeekBar maxHueControl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seekbar);

        ButterKnife.inject(this);
        setMinHueControlListener();
        setMaxHueControlListener();
    }

    private void setMinHueControlListener() {
        minHueControl.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            int minHue = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                minHue = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Toast.makeText(SeekBarActivity.this, " Minimum Hue set to " + minHue, Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }

    private void setMaxHueControlListener() {
        maxHueControl.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            int maxHue = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                maxHue = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Toast.makeText(SeekBarActivity.this, " Maximum Hue set to " + maxHue, Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }
}
