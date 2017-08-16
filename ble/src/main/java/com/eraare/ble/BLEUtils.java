package com.eraare.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.pm.PackageManager;

import java.lang.reflect.Method;

/**
 * @file BLEUtils.java
 * @author Leo
 * @version 1
 * @detail BLE用到的工具类，包括检查是否支持蓝牙及BLE
 * @since 2016/12/29 8:57
 */

/**
 * 文件名：BLEUtils.java
 * 作  者：Leo
 * 版  本：1
 * 日  期：2016/12/29 8:57
 * 描  述：BLE用到的工具类，包括检查是否支持蓝牙及BLE
 */
public final class BLEUtils {
    private BLEUtils() {
    }

    /**
     * 检测设备是否支持蓝牙BLE
     *
     * @param context
     * @return
     */
    public static boolean isSupportBluetoothBLE(Context context) {
        if (isSupportBluetooth()) {
            if (isSupportBLE(context)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断是否支持BLE
     *
     * @return
     */
    public static boolean isSupportBLE(Context context) {
        // 判断是否支持BLE
        if (!context.getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_BLUETOOTH_LE)) {
            return false;
        }

        return true;
    }


    /**
     * 是否支持蓝牙
     *
     * @return
     */
    public static boolean isSupportBluetooth() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            return false;
        }
        return true;
    }

    /**
     * 蓝牙是否打开可用
     *
     * @return
     */
    public static boolean isBluetoothEnabled() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        return adapter.isEnabled();
    }

    /**
     * 打开蓝牙
     */
    public static boolean openBluetooth() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter != null) {
            if (!adapter.isEnabled()) {
                return adapter.enable();
            }
        }
        return false;
    }

    /**
     * 关闭蓝牙
     */
    public static boolean closeBluetooth() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter != null) {
            if (adapter.isEnabled()) {
                return adapter.disable();
            }
        }
        return false;
    }

    /**
     * unpair bluetooth device
     *
     * @param device
     */
    public static void removeBond(BluetoothDevice device) {
        try {
            Class c = BluetoothDevice.class;
            Method m = c.getMethod("removeBond", (Class[]) null);
            m.invoke(device, (Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * unpair bluetooth device by address
     *
     * @param address
     */
    public static void removeBond(String address) {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        try {
            BluetoothDevice device = adapter.getRemoteDevice(address);
            Class c = BluetoothDevice.class;
            Method m = c.getMethod("removeBond", (Class[]) null);
            m.invoke(device, (Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}