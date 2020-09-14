package com.example.easy_bluetooth.BroadcastReceiver;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.easy_bluetooth.Listener.BluetoothDeviceListner;

public class BluetoothDeviceReceiver extends BroadcastReceiver {

    private BluetoothDeviceListner listner;

    public BluetoothDeviceReceiver() {
    }

    public BluetoothDeviceReceiver(BluetoothDeviceListner listner) {
        this.listner = listner;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        final String action = intent.getAction();

        if(action.equals(BluetoothDevice.ACTION_FOUND)){
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            listner.onBluetoothDevice(device);
        };

    }


}
