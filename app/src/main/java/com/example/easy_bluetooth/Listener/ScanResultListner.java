package com.example.easy_bluetooth.Listener;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanResult;

public interface ScanResultListner {

    public void onScanCallback(ScanResult result);


}
