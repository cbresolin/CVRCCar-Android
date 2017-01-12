package com.kreolite.cvrccar.BluetoothService;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class BtManager {

    private static final String TAG = "BtManager";
    private final UUID SERIAL_PORT_SERVICE_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private String mBtDeviceName;
    private String mBtDeviceAddress;
    private BluetoothDevice mBtDevice;
    private BluetoothSocket mBtSocket;
    private OutputStream mBtOutputStream;
    private InputStream mBtInputStream;
    private boolean mDevicePaired = false;
    private boolean mDeviceConnected = false;
    private Context mSettingsContext;

    public void setContext(Context context) {
        mSettingsContext = context;
    }

    public void setBtDeviceName(String BtDeviceName) {
        mBtDeviceName = BtDeviceName;
    }

    public void scan() {

        Set<BluetoothDevice> bondedDevices = mBluetoothAdapter.getBondedDevices();

        if (bondedDevices.isEmpty()) {
            CharSequence text = "No paired devices!";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(mSettingsContext, text, duration);
            toast.show();
            Log.w(TAG, "No paired devices!");
        }
        else
        {
            mDevicePaired = false;
            for (BluetoothDevice iterator : bondedDevices)
            {
                if (iterator.getName().equals(mBtDeviceName))
                {
                    mBtDevice = iterator;
                    mDevicePaired = true;
                    mBtDeviceAddress = iterator.getAddress();
                    Log.i(TAG, iterator.getName() + " device is paired!");
                    break;
                }
            }

            if (!mDevicePaired)
            {
                CharSequence text = mBtDeviceName + " device is not paired!";
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(mSettingsContext, text, duration);
                toast.show();
                Log.w(TAG, mBtDeviceName + " device is not paired!");
            }
        }
    }

    public boolean isDevicePaired() {
        return mDevicePaired;
    }

    public String getBtDeviceAddress() {
        return mBtDeviceAddress;
    }

    public void connect()
    {
        if (mDevicePaired && !mDeviceConnected) {
            mDeviceConnected = true;

            try {
                mBtSocket = mBtDevice.createRfcommSocketToServiceRecord(SERIAL_PORT_SERVICE_UUID);
                mBluetoothAdapter.cancelDiscovery();
                mBtSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                try {
                    connectException.printStackTrace();
                    mDeviceConnected = false;
                    mBtSocket.close();
                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close the client socket", closeException);
                }
            }

            if(mDeviceConnected)
            {
                CharSequence text = mBtDeviceName + " device is connected!";
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(mSettingsContext, text, duration);
                toast.show();
                Log.i(TAG, mBtDeviceName + " device is connected!");

                try {
                    mBtOutputStream = mBtSocket.getOutputStream();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    mBtInputStream = mBtSocket.getInputStream();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else {
                CharSequence text = mBtDeviceName + " device is not connected!";
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(mSettingsContext, text, duration);
                toast.show();
                Log.w(TAG, mBtDeviceName + " device is not connected!");
            }
        }
    }

    public boolean isDeviceConnected() {
        return mDeviceConnected;
    }

    public void disconnect() {

        if (mDevicePaired && mDeviceConnected) {
            mDeviceConnected=false;

            try {
                mBtOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
                mDeviceConnected = true;
            }
            try {
                mBtInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
                mDeviceConnected = true;
            }
            try {
                mBtSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
                mDeviceConnected = true;
            }

            if(!mDeviceConnected)
            {
                CharSequence text = mBtDeviceName + " device is disconnected!";
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(mSettingsContext, text, duration);
                toast.show();
                Log.i(TAG, mBtDeviceName + " device is disconnected!");
            }
            else {
                CharSequence text = mBtDeviceName + " device is still connected!";
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(mSettingsContext, text, duration);
                toast.show();
                Log.w(TAG, mBtDeviceName + " device is still connected!");
            }
        }
    }
}
