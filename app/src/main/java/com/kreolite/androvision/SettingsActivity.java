package com.kreolite.androvision;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class SettingsActivity extends AppCompatActivity {

    private static final String _TAG = "settingsActivity";
    // private final String DEVICE_ADDRESS="98:D3:31:FC:1E:28";
    //Serial Port Service ID
    private final UUID PORT_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    private BluetoothDevice btDevice;
    private BluetoothSocket btSocket;
    private OutputStream btOutputStream;
    private InputStream btInputStream;
    private static final int MY_BT_ENABLE_REQUEST = 0;
    private boolean devicePaired = false;
    private boolean deviceConnected=false;

    @InjectView(R.id.editTextBtDevice)
    EditText btDeviceControl;

    @InjectView(R.id.buttonScan)
    Button scanButton;
    @InjectView(R.id.buttonConnect)
    Button connectButton;
    @InjectView(R.id.buttonDisconnect)
    Button disconnectButton;

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
    @InjectView(R.id.editTextMinRadius)
    EditText minRadiusControl;

    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_view);
        ButterKnife.inject(this);

        // Set button states
        setButtonEnabled(false);

        // Initialise BT
        btInit();

        // Read current values if any, and change layout
        sharedPref = getApplicationContext().getSharedPreferences(getString(R.string.settings_file),
                Context.MODE_PRIVATE);
        btDeviceControl.setText(sharedPref.getString(getString(R.string.bt_device_name), "HC-05"));
        reso1Control.setChecked(sharedPref.getBoolean(getString(R.string.is_reso1), true));
        reso2Control.setChecked(sharedPref.getBoolean(getString(R.string.is_reso2), false));
        reso3Control.setChecked(sharedPref.getBoolean(getString(R.string.is_reso3), false));
        reso4Control.setChecked(sharedPref.getBoolean(getString(R.string.is_reso4), false));
        forwardBoundaryControl.setText(sharedPref.getString(getString(R.string.forward_boundary_percent), "-15"));
        reverseBoundaryControl.setText(sharedPref.getString(getString(R.string.reverse_boundary_percent), "25"));
        minRadiusControl.setText(sharedPref.getString(getString(R.string.minimum_radius_value), "15"));

        setBtDeviceControlListener();
        setReso1ControlListener();
        setReso2ControlListener();
        setReso3ControlListener();
        setReso4ControlListener();
        setForwardBoundaryControlListener();
        setReverseBoundaryControlListener();
        setMinRadiusControlListener();
    }

    private void setButtonEnabled(boolean isEnabled)
    {
        scanButton.setEnabled(!isEnabled);
        connectButton.setEnabled(isEnabled);
        disconnectButton.setEnabled(isEnabled);
    }

    private void btInit()
    {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            Context context = getApplicationContext();
            CharSequence text = "Device does not support BT!";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();

            Log.i(_TAG, "Device does not support BT!");
        }

        if(!bluetoothAdapter.isEnabled())
        {
            Intent enableAdapter = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableAdapter, MY_BT_ENABLE_REQUEST);
        }
    }

    public void onClickScan(View view) {

        String btDeviceToScan = sharedPref.getString(getString(R.string.bt_device_name), "");
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();

        if (bondedDevices.isEmpty()) {
            Context context = getApplicationContext();
            CharSequence text = "BT is disabled or no paired devices!";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }
        else
        {
            for (BluetoothDevice iterator : bondedDevices)
            {
                if (iterator.getName().equals(btDeviceToScan))
                {
                    btDevice = iterator;
                    devicePaired = true;

                    Context context = getApplicationContext();
                    CharSequence text = iterator.getName() + " device is paired!";
                    int duration = Toast.LENGTH_SHORT;
                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();

                    Log.i(_TAG, iterator.getName() + " device is paired!");
                    break;
                }
            }

            if (!devicePaired)
            {
                Context context = getApplicationContext();
                CharSequence text = btDeviceToScan + " device is not paired!";
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();

                Log.i(_TAG, btDeviceToScan + " device is not paired!");
            }
        }
    }

    private void onClickConnect(View view) {

    }

    private void onClickDisconnect(View view) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == MY_BT_ENABLE_REQUEST) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                Log.i(_TAG, "BT is enabled!");
            }
            else
            {
                Log.i(_TAG, "BT is disabled!");
            }
        }
    }

    private void setBtDeviceControlListener() {

        btDeviceControl.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                editor = sharedPref.edit();
                editor.putString(getString(R.string.bt_device_name), btDeviceControl.getText().toString());
                editor.commit();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
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

    private void setMinRadiusControlListener() {

        minRadiusControl.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                editor = sharedPref.edit();
                editor.putString(getString(R.string.minimum_radius_value), minRadiusControl.getText().toString());
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
}
