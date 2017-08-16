package com.eraare.ble;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Build;
import android.os.Handler;

/**
 * @author Leo
 * @version 1
 * @since 2016-12-30
 * 蓝牙设备扫描器
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public final class BLEScanner {
    private static final long DEFAULT_SCAN_PERIOD = 10000;// 扫描10s
    private volatile static BLEScanner singleton = null;
    /*蓝牙适配器*/
    private BluetoothAdapter mBluetoothAdapter;
    private Handler mHandler = new Handler();// 用于postDelay
    private boolean mScanning = false;// 循环标志位
    private long mDuration = DEFAULT_SCAN_PERIOD;

    /**
     * 扫描蓝牙BLE设备的回掉函数
     */
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice bluetoothDevice, int rssi, byte[] bytes) {
            System.out.println(bluetoothDevice.getName());
            if (mCallback != null) {
                mCallback.onDeviceDiscovered(bluetoothDevice, rssi, bytes);
            }
        }
    };
    /*Section: 设备扫描*/
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            /*扫描状态回调出去*/
            if (mCallback != null) mCallback.onStateChanged(mScanning);
        }
    };

    private BLEScanner() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public static BLEScanner getInstance() {
        if (singleton == null) {
            synchronized (BLEScanner.class) {
                if (singleton == null) {
                    singleton = new BLEScanner();
                }
            }
        }
        return singleton;
    }

    /**
     * 扫描BLE设备
     *
     * @param enable
     */
    private void scanLeDevice(final boolean enable) {
        if (enable) {
            mHandler.postDelayed(mRunnable, mDuration);
            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mHandler.removeCallbacks(mRunnable);
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
        /*扫描状态回调出去*/
        if (mCallback != null) mCallback.onStateChanged(mScanning);
    }

    /**
     * 是否正在扫描
     *
     * @return
     */
    public boolean isScanning() {
        return this.mScanning;
    }

    /**
     * 扫描或者停止
     */
    public void startScan(long duration) {
        this.mDuration = duration;
        scanLeDevice(true);
    }

    public void stopScan() {
        scanLeDevice(false);
    }

    public interface Callback {
        void onStateChanged(boolean scanning);

        void onDeviceDiscovered(BluetoothDevice device, int rssi, byte[] bytes);
    }

    private Callback mCallback;

    public void setCallback(Callback callback) {
        this.mCallback = callback;
    }
}
