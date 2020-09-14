package com.example.easy_bluetooth.BroadcastReceiver;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.easy_bluetooth.Listener.IntListner;

public class BluetoothDiscoverReceiver extends BroadcastReceiver {

    private final String TAG = "BTHistoryReceiver";
    private IntListner listner;

    private BluetoothAdapter bluetoothAdapter;
    public BluetoothDiscoverReceiver(BluetoothAdapter bluetoothAdapter, IntListner listner) {
        this.bluetoothAdapter = bluetoothAdapter;
        this.listner = listner;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        String action = intent.getAction();
        Log.e(TAG, "broadcastReciever");
        if (action.equals(bluetoothAdapter.ACTION_SCAN_MODE_CHANGED)) {
            // Discovery has found a device. Get the BluetoothDevice
            // object and its info from the Intent.
            Log.e(TAG, "bluetoothAdapter.Action_State_changed");

            final int status = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, bluetoothAdapter.ERROR);
            listner.onScanCallback(status);

        }
    }
}
