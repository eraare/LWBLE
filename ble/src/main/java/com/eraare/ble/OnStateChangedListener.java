package com.eraare.ble;

/**
 * @author Leo
 * @version 1
 * @since 2017-08-16
 * BLE状态改变回调
 */
public interface OnStateChangedListener {
    /*连接断开状态码*/
    int STATE_INITIAL = -1;
    int STATE_CONNECTING = 1;
    int STATE_CONNECTED = 2;
    int STATE_DISCONNECTING = 3;
    int STATE_DISCONNECTED = 4;
    int STATE_SERVICING = 5;

    void onStateChanged(String address, int state);
}
