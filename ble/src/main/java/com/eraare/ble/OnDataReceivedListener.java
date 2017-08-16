package com.eraare.ble;

/**
 * @author Leo
 * @version 1
 * @since 2017-08-16
 * BLE接收到数据回调
 */
public interface OnDataReceivedListener {
    void onDataReceived(String address, byte[] data);
}
