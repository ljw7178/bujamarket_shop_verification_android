package com.example.easy_bluetooth;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.example.easy_bluetooth.BroadcastReceiver.BluetoothDeviceReceiver;
import com.example.easy_bluetooth.BroadcastReceiver.BluetoothDiscoverReceiver;
import com.example.easy_bluetooth.BroadcastReceiver.BluetoothStateChangedReceiver;
import com.example.easy_bluetooth.Callback.CustomScanCallback;
import com.example.easy_bluetooth.Listener.BluetoothDeviceListner;
import com.example.easy_bluetooth.Listener.IntListner;
import com.example.easy_bluetooth.Listener.ScanResultListner;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class BluetoothFunction {

    private Context context;
    private Activity activity;
    private BluetoothAdapter adapter;
    private BluetoothStateChangedReceiver bluetoothStateChangedReceiver;
    private BluetoothDiscoverReceiver bluetoothDiscoverReceiver;
    private BluetoothDeviceReceiver bluetoothDeviceReceiver;
    private final String TAG = "BluetoothOnOff";

    private List<ScanFilter> scanFilters = new Vector<>();
    private ScanSettings.Builder mScanSettings;
    private ScanSettings scanSettings;

    private BluetoothLeScanner mBluetoothLeScanner;
    private BluetoothManager bluetoothManager;

    private CustomScanCallback customScanCallback;

    private ArrayList<BluetoothDevice> devices = new ArrayList<>();

    public BluetoothFunction() {

    }

    //activity에서 실행할 경우 사용
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public BluetoothFunction(Activity activity, Context context, BluetoothAdapter adapter) {
        this.context = context;
        this.adapter = adapter;
        this.activity = activity;

        this.mScanSettings = new ScanSettings.Builder();
        mScanSettings.setScanMode(ScanSettings.SCAN_MODE_LOW_POWER);
        scanSettings = mScanSettings.build();


    }

    //Service에서 호출할 경우 Activity가 존재하지 않아 Context로 설정함
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public BluetoothFunction(Context context, BluetoothAdapter adapter) {
        this.context = context;
        this.adapter = adapter;

        this.mScanSettings = new ScanSettings.Builder();
        mScanSettings.setScanMode(ScanSettings.SCAN_MODE_LOW_POWER);
        scanSettings = mScanSettings.build();


    }

    //블루투스가 꺼지고, 켜지는 상태 변화를 듣고 있는 BroadcastReceiver를 만드는 기능
    public void bluetoothOnOffBroadcastReceiver(final IntListner listner){
        Log.e("bluetoothOnOff", "start");
        if(bluetoothStateChangedReceiver == null){
            Log.e("bluetoothOnOff", "null");
            bluetoothStateChangedReceiver = new BluetoothStateChangedReceiver(adapter, new IntListner() {
                @Override
                public void onScanCallback(int status) {
                    listner.onScanCallback(status);
                }
            });
        }

        IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        context.registerReceiver(bluetoothStateChangedReceiver, BTIntent);

    }

    //블루투스가 꺼져 있을 때 켜도록 하는 기능
    public void bluetoothTurnOnRequest(){

        if(!adapter.isEnabled()){
            Log.e("bluetoothOnOff", "enabled request start");
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            enableBTIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(enableBTIntent);

        }

    }

    //블루투스 꺼지고 켜지는 상태 변화 BroadcastReceiver 구독 해지
    public void unregisterReceiverBluetoothOnOff(){
        context.unregisterReceiver(bluetoothStateChangedReceiver);
    }

    //블루투스로 검색되는 상태 변화를 듣는 BroadcastReceiver를 만듬
    public void bluetoothDiscoverableBroadcastReceiver(final IntListner listner){
        Log.e(TAG , "btn Enable Dsiable discoverabe");

        if(bluetoothDiscoverReceiver == null){
            bluetoothDiscoverReceiver = new BluetoothDiscoverReceiver(adapter, new IntListner() {
                @Override
                public void onScanCallback(int status) {
                    listner.onScanCallback(status);
                }
            });
        }

        IntentFilter intentFilter = new IntentFilter(adapter.ACTION_SCAN_MODE_CHANGED);
        context.registerReceiver(bluetoothDiscoverReceiver, intentFilter);

    }

    //블루투스로 검색되는 상태로 바꿈
    public void bluetoothTurnOnDiscoverable(){

        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        discoverableIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(discoverableIntent);

    }

    //블루투스 꺼지고 켜지는 상태 변화 BroadcastReceiver 구독 해지
    public void unregisterReceiverBluetoothDiscoverable(){
        if(bluetoothDiscoverReceiver != null && bluetoothDiscoverReceiver.isInitialStickyBroadcast()){
            context.unregisterReceiver(bluetoothDiscoverReceiver);
        }
    }


    //주변 블루투스 기기 검색 브로드케스트 설정
    public void bluetoothDiscover(final BluetoothDeviceListner bluetoothDeviceListner){

        if(bluetoothDeviceReceiver == null){
            bluetoothDeviceReceiver = new BluetoothDeviceReceiver(new BluetoothDeviceListner() {
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onBluetoothDevice(BluetoothDevice device) {
                    Log.e("device", device.toString());
                    bluetoothDeviceListner.onBluetoothDevice(device);
                }
            });
        }

        IntentFilter discoverDeviceIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        context.registerReceiver( bluetoothDeviceReceiver, discoverDeviceIntent);
    }

    //블루투스 검색을 시작함
    public void startBluetoothDeviceDiscover(){

        if(adapter.isDiscovering()){
            adapter.cancelDiscovery();
            adapter.startDiscovery();
        }

        if(!adapter.isDiscovering()){
            adapter.startDiscovery();
        }

    }

    //블루투스 기기 구독을 해제함
    public void unregisterReceiverBluetoothDiscover(){
        if(bluetoothDeviceReceiver != null && bluetoothDeviceReceiver.isInitialStickyBroadcast()){
            context.unregisterReceiver(bluetoothDeviceReceiver);
        }

    }

    //scanfilter 만들기
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public List<ScanFilter> makeScanFilter(ArrayList<BluetoothDevice> devices){

        List<ScanFilter> resultList = new ArrayList<>();
        for(BluetoothDevice device : devices){

            ScanFilter.Builder scanFilter = new ScanFilter.Builder();
            scanFilter.setDeviceAddress(device.getAddress());
            ScanFilter scan = scanFilter.build();
            resultList.add(scan);

        }
        return resultList;
    }

    //scanfilter가 필요한 경우
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void startScanWithAddress(ArrayList<BluetoothDevice> devices, final ScanResultListner listner){

        scanFilters = makeScanFilter(devices);

        if(mBluetoothLeScanner == null){
            mBluetoothLeScanner = adapter.getBluetoothLeScanner();
        }

        if(customScanCallback == null){
            customScanCallback = new CustomScanCallback(new ScanResultListner() {
                @Override
                public void onScanCallback(ScanResult result) {
                    listner.onScanCallback(result);
                }
            });
        }
        mBluetoothLeScanner.startScan(scanFilters, scanSettings, customScanCallback);
    }

    //scanfilter가 필요 없는 경우
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void startLeScan(final ScanResultListner listner){

        if(mBluetoothLeScanner == null){
            mBluetoothLeScanner = adapter.getBluetoothLeScanner();
        }

        if(customScanCallback == null){
            customScanCallback = new CustomScanCallback(new ScanResultListner() {
                @Override
                public void onScanCallback(ScanResult result) {
                    //uuid가 없는 데이터는 생략함
                    listner.onScanCallback(result);
                }
            });
        }
        mBluetoothLeScanner.startScan(null, scanSettings, customScanCallback);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void stopScanner(){
        if(customScanCallback != null && mBluetoothLeScanner != null){
            mBluetoothLeScanner.stopScan(customScanCallback);
        }
    }


    private void checkBTPermissions(){
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
            int permissionCheck = 0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                permissionCheck = context.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                permissionCheck += context.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            }
            if(permissionCheck != 0){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    activity.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001);
                }
            }
        } else {
            Log.e(TAG, "check permission");
        }
    }



}
