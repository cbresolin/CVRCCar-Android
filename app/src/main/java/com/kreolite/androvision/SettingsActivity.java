package com.kreolite.androvision;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Switch;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class SettingsActivity extends AppCompatActivity {

    @InjectView(R.id.seekBarMinRadiusVal)
    SeekBar minRadiusControl;
    @InjectView(R.id.seekBarMaxRadiusVal)
    SeekBar maxRadiusControl;

    @InjectView(R.id.radioButtonReso1)
    RadioButton reso1Control;
    @InjectView(R.id.radioButtonReso2)
    RadioButton reso2Control;
    @InjectView(R.id.radioButtonReso3)
    RadioButton reso3Control;
    @InjectView(R.id.radioButtonReso4)
    RadioButton reso4Control;

    @InjectView(R.id.editTextForwardBoundary)
    EditText forwardBoundaryControl;
    @InjectView(R.id.editTextReverseBoundary)
    EditText reverseBoundaryControl;

    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_view);
        ButterKnife.inject(this);

        // Read current values if any, and change layout
        sharedPref = getApplicationContext().getSharedPreferences(getString(R.string.settings_file),
                Context.MODE_PRIVATE);
        reso1Control.setChecked(sharedPref.getBoolean(getString(R.string.is_reso1), true));
        reso2Control.setChecked(sharedPref.getBoolean(getString(R.string.is_reso2), false));
        reso3Control.setChecked(sharedPref.getBoolean(getString(R.string.is_reso3), false));
        reso4Control.setChecked(sharedPref.getBoolean(getString(R.string.is_reso4), false));
        minRadiusControl.setProgress(sharedPref.getInt(getString(R.string.min_radius), 0));
        maxRadiusControl.setProgress(sharedPref.getInt(getString(R.string.max_radius), 0));
        forwardBoundaryControl.setText(sharedPref.getString(getString(R.string.forward_boundary_percent), "-15"));
        reverseBoundaryControl.setText(sharedPref.getString(getString(R.string.reverse_boundary_percent), "30"));

        setReso1ControlListener();
        setReso2ControlListener();
        setReso3ControlListener();
        setReso4ControlListener();
        setMinRadiusControlListener();
        setMaxRadiusControlListener();
        setForwardBoundaryControlListener();
        setReverseBoundaryControlListener();
    }

    private void setForwardBoundaryControlListener() {

        forwardBoundaryControl.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                editor = sharedPref.edit();
                editor.putString(getString(R.string.forward_boundary_percent), forwardBoundaryControl.getText().toString());
                editor.commit();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void setReverseBoundaryControlListener() {

        reverseBoundaryControl.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                editor = sharedPref.edit();
                editor.putString(getString(R.string.reverse_boundary_percent), reverseBoundaryControl.getText().toString());
                editor.commit();
            }

            @Override
            public void afterTextChanged(Editable s) {

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

    private void setReso4ControlListener() {

        reso4Control.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editor = sharedPref.edit();
                editor.putBoolean(getString(R.string.is_reso4), isChecked);
                editor.commit();
            }
        });
    }

    private void setMinRadiusControlListener() {

        final TextView minRadiusVal = (TextView)findViewById(R.id.textViewMinRadiusVal);
        minRadiusVal.setText(String.valueOf(minRadiusControl.getProgress()));

        minRadiusControl.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                minRadiusVal.setText(String.valueOf(progress));
                editor = sharedPref.edit();
                editor.putInt(getString(R.string.min_radius), progress);
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

    private void setMaxRadiusControlListener() {

        final TextView maxRadiusVal = (TextView)findViewById(R.id.textViewMaxRadiusVal);
        maxRadiusVal.setText(String.valueOf(maxRadiusControl.getProgress()));

        maxRadiusControl.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                maxRadiusVal.setText(String.valueOf(progress));
                editor = sharedPref.edit();
                editor.putInt(getString(R.string.max_radius), progress);
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
