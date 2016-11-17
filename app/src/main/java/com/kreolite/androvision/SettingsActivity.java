package com.kreolite.androvision;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Switch;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class settingsActivity extends AppCompatActivity {

    @InjectView(R.id.seekBarMinHue)
    SeekBar minHueControl;
    @InjectView(R.id.seekBarMaxHue)
    SeekBar maxHueControl;
    @InjectView(R.id.seekBarMinSat)
    SeekBar minSatControl;
    @InjectView(R.id.seekBarMaxSat)
    SeekBar maxSatControl;
    @InjectView(R.id.seekBarMinVal)
    SeekBar minValControl;
    @InjectView(R.id.seekBarMaxVal)
    SeekBar maxValControl;
    @InjectView(R.id.switchContour)
    Switch showContourControl;
    @InjectView(R.id.radioButtonReso1)
    RadioButton reso1Control;
    @InjectView(R.id.radioButtonReso2)
    RadioButton reso2Control;
    @InjectView(R.id.radioButtonReso3)
    RadioButton reso3Control;

    /*@InjectView(R.id.textViewMinArea)
    TextInputEditText minAreaControl;
    @InjectView(R.id.textViewForwardBoundary)
    TextInputEditText forwardBoundaryControl;
    @InjectView(R.id.textViewReverseBoundary)
    TextInputEditText reverseBoundaryControl;*/


    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.inject(this);

        // Read current values if any, and change layout
        sharedPref = getApplicationContext().getSharedPreferences(getString(R.string.settings_file),
                Context.MODE_PRIVATE);
        reso1Control.setChecked(sharedPref.getBoolean(getString(R.string.is_reso1), true));
        reso2Control.setChecked(sharedPref.getBoolean(getString(R.string.is_reso2), false));
        reso3Control.setChecked(sharedPref.getBoolean(getString(R.string.is_reso3), false));
        showContourControl.setChecked(sharedPref.getBoolean(getString(R.string.is_contour), true));
        minHueControl.setProgress(sharedPref.getInt(getString(R.string.min_hue), 0));
        maxHueControl.setProgress(sharedPref.getInt(getString(R.string.max_hue), R.integer.maxHueKey));
        minSatControl.setProgress(sharedPref.getInt(getString(R.string.min_sat), 0));
        maxSatControl.setProgress(sharedPref.getInt(getString(R.string.max_sat), R.integer.maxSatValKey));
        minValControl.setProgress(sharedPref.getInt(getString(R.string.min_val), 0));
        maxValControl.setProgress(sharedPref.getInt(getString(R.string.max_val), R.integer.maxSatValKey));

        setMinHueControlListener();
        setMaxHueControlListener();
        setMinSatControlListener();
        setMaxSatControlListener();
        setMinValControlListener();
        setMaxValControlListener();
        setShowContourControlListener();
        setReso1ControlListener();
        setReso2ControlListener();
        setReso3ControlListener();
    }

    private void setShowContourControlListener() {

        showContourControl.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editor = sharedPref.edit();
                editor.putBoolean(getString(R.string.is_contour), isChecked);
                editor.commit();
            }
        });
    }

    private void setReso1ControlListener() {

        reso1Control.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editor = sharedPref.edit();
                editor.putBoolean(getString(R.string.is_reso1), isChecked);
                editor.commit();
            }
        });
    }

    private void setReso2ControlListener() {

        reso2Control.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editor = sharedPref.edit();
                editor.putBoolean(getString(R.string.is_reso2), isChecked);
                editor.commit();
            }
        });
    }

    private void setReso3ControlListener() {

        reso3Control.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editor = sharedPref.edit();
                editor.putBoolean(getString(R.string.is_reso3), isChecked);
                editor.commit();
            }
        });
    }

    private void setMinHueControlListener() {

        final TextView minHueVal = (TextView)findViewById(R.id.textViewMinHueVal);
        minHueVal.setText(String.valueOf(minHueControl.getProgress()));

        minHueControl.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                minHueVal.setText(String.valueOf(progress));
                editor = sharedPref.edit();
                editor.putInt(getString(R.string.min_hue), progress);
                editor.commit();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    private void setMaxHueControlListener() {

        final TextView maxHueVal = (TextView)findViewById(R.id.textViewMaxHueVal);
        maxHueVal.setText(String.valueOf(maxHueControl.getProgress()));

        maxHueControl.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                maxHueVal.setText(String.valueOf(progress));
                editor = sharedPref.edit();
                editor.putInt(getString(R.string.max_hue), progress);
                editor.commit();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    private void setMinSatControlListener() {

        final TextView minSatVal = (TextView)findViewById(R.id.textViewMinSatVal);
        minSatVal.setText(String.valueOf(minSatControl.getProgress()));

        minSatControl.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                minSatVal.setText(String.valueOf(progress));
                editor = sharedPref.edit();
                editor.putInt(getString(R.string.min_sat), progress);
                editor.commit();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    private void setMaxSatControlListener() {

        final TextView maxSatVal = (TextView)findViewById(R.id.textViewMaxSatVal);
        maxSatVal.setText(String.valueOf(maxSatControl.getProgress()));

        maxSatControl.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                maxSatVal.setText(String.valueOf(progress));
                editor = sharedPref.edit();
                editor.putInt(getString(R.string.max_sat), progress);
                editor.commit();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    private void setMinValControlListener() {

        final TextView minValVal = (TextView)findViewById(R.id.textViewMinValVal);
        minValVal.setText(String.valueOf(minValControl.getProgress()));

        minValControl.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                minValVal.setText(String.valueOf(progress));
                editor = sharedPref.edit();
                editor.putInt(getString(R.string.min_val), progress);
                editor.commit();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    private void setMaxValControlListener() {

        final TextView maxValVal = (TextView)findViewById(R.id.textViewMaxValVal);
        maxValVal.setText(String.valueOf(maxValControl.getProgress()));

        maxValControl.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                maxValVal.setText(String.valueOf(progress));
                editor = sharedPref.edit();
                editor.putInt(getString(R.string.max_val), progress);
                editor.commit();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }
}
