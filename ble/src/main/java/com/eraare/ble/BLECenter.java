package com.eraare.ble;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.os.Build;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.eraare.ble.OnStateChangedListener.STATE_CONNECTED;
import static com.eraare.ble.OnStateChangedListener.STATE_CONNECTING;
import static com.eraare.ble.OnStateChangedListener.STATE_DISCONNECTED;
import static com.eraare.ble.OnStateChangedListener.STATE_DISCONNECTING;
import static com.eraare.ble.OnStateChangedListener.STATE_INITIAL;
import static com.eraare.ble.OnStateChangedListener.STATE_SERVICING;

/**
 * @file BLECenter.java
 * @author Leo
 * @version 1
 * @detail 蓝牙通信控制中心
 * @since 2016/12/30 17:25
 */

/**
 * 文件名：BLECenter.java
 * 作  者：Leo
 * 版  本：1
 * 日  期：2016/12/30 17:25
 * 描  述：蓝牙通信控制中心
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public final class BLECenter {
    private static final String TAG = BLECenter.class.getSimpleName();

    private volatile static BLECenter singleton = null;

    public static BLECenter getInstance() {
        if (singleton == null) {
            synchronized (BLECenter.class) {
                if (singleton == null) {
                    singleton = new BLECenter();
                }
            }
        }
        return singleton;
    }

    /*蓝牙适配器和蓝牙GATT*/
    private BluetoothAdapter mBluetoothAdapter;
    private Map<String, BluetoothGatt> mGatts;

    private BLECenter() {
        /*初始化数据*/
        initial();
    }

    /*初始化*/
    private void initial() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mGatts = new ConcurrentHashMap<>();
    }

    /**
     * 结束的一些操作动作
     */
    public void suicide() {
        if (mGatts == null) return;
        /*通过Key遍历进行关闭所有的设备*/
        Set<String> keySet = mGatts.keySet();
        Iterator<String> iterator = keySet.iterator();
        while (iterator.hasNext()) {
            disconnect(iterator.next(), true);
        }
        mGatts.clear();
    }

    /*Section: BLE回调类*/
    private final BluetoothGattCallback mBluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            int currentState = STATE_INITIAL;
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                currentState = STATE_CONNECTED;
                Log.d(TAG, "LWBLE: Connected");
                if (!gatt.discoverServices()) {
                    Log.d(TAG, "LWBLE: Cannot Discovery Services");
                }
            } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                currentState = STATE_DISCONNECTED;
                Log.d(TAG, "LWBLE: Disconnected");
            } else if (newState == BluetoothGatt.STATE_CONNECTING) {
                currentState = STATE_CONNECTING;
                Log.d(TAG, "LWBLE: Connecting");
            } else if (newState == BluetoothGatt.STATE_DISCONNECTING) {
                currentState = STATE_DISCONNECTING;
                Log.d(TAG, "LWBLE: Disconnecting");
            }
            String address = gatt.getDevice().getAddress();
            sendState(address, currentState);
            super.onConnectionStateChange(gatt, status, newState);
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.d(TAG, "LWBLE: Fond Services");
            String address = gatt.getDevice().getAddress();
            sendState(address, STATE_SERVICING);
            Log.d(TAG, "LWBLE: Servicing");

            /*通知配置和密码验证必须由事件间隔*/
            final BluetoothGatt bgatt = gatt;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    SystemClock.sleep(150);
                    setCharacteristicNotification(bgatt, true);
                }
            }).start();
            super.onServicesDiscovered(gatt, status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic
                characteristic) {
            String address = gatt.getDevice().getAddress();
            byte[] data = characteristic.getValue();
            Log.d(TAG, "LWBLE: Received Data-[" + new String(data) + "]");
            sendData(address, data);
            super.onCharacteristicChanged(gatt, characteristic);
        }
    };

    private void sendState(String address, int state) {
        if (mStateListeners != null) {
            for (OnStateChangedListener listener : mStateListeners) {
                listener.onStateChanged(address, state);
            }
        }
    }

    private void sendData(String address, byte[] data) {
        if (mDataListeners != null) {
            for (OnDataReceivedListener listener : mDataListeners) {
                listener.onDataReceived(address, data);
            }
        }
    }

    /**
     * 设置蓝牙设备可接受通知
     *
     * @param gatt
     * @param enabled
     */
    private void setCharacteristicNotification(BluetoothGatt gatt, boolean enabled) {
        /*gatt为空或则characteristic为空则退出*/
        if (gatt == null) {
            return;
        }
        BluetoothGattCharacteristic characteristic = getCharacteristic(gatt);
        if (characteristic == null) {
            return;
        }
        /*设置属性并通知*/
        gatt.setCharacteristicNotification(characteristic, enabled);
        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(BLEUUID.UUID_DESCRIPTOR);
        // 查看是否带有可通知属性notify 查看是否带有indecation属性
        if (0 != (characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY)) {
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        } else if (0 != (characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_INDICATE)) {
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
        }
        gatt.writeDescriptor(descriptor);
    }

    /**
     * 获取BluetoothGattCharacteristic
     *
     * @param gatt
     * @return
     */
    private BluetoothGattCharacteristic getCharacteristic(BluetoothGatt gatt) {
        BluetoothGattService bluetoothGattService = gatt.getService(BLEUUID.UUID_SERVICE);
        if (bluetoothGattService == null) {
            return null;
        }
        return bluetoothGattService.getCharacteristic(BLEUUID.UUID_CHARACTERISTIC);
    }

    /**
     * 根据地址连接设备
     *
     * @param context
     * @param deviceAddress
     * @param isAutoConnect
     * @return
     */
    public boolean connect(Context context, String deviceAddress, boolean isAutoConnect) {
        /*地址为空则退出*/
        if (TextUtils.isEmpty(deviceAddress)) return false;
        // 从集合中取得gatt
        BluetoothGatt gatt = mGatts.get(deviceAddress);
        if (gatt == null) {
            /*第一次连接*/
            final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(deviceAddress);
            if (device == null) return false;
            gatt = device.connectGatt(context, isAutoConnect, mBluetoothGattCallback);
            if (gatt == null) return false;
            mGatts.put(deviceAddress, gatt);
            return true;
        } else {
            /*非第一次只是重新连接*/
            return gatt.connect();
        }
    }

    /**
     * 断开连接
     *
     * @param deviceAddress
     * @param isRemove
     */
    public void disconnect(String deviceAddress, boolean isRemove) {
        BluetoothGatt gatt = mGatts.get(deviceAddress);
        // 为空则返回
        if (gatt == null) return;
        // 断开连接
        gatt.disconnect();
        // 移除并关闭
        if (isRemove) mGatts.remove(deviceAddress).close();
    }

    /*Section: 对外接口*/

    /**
     * 发送数据
     *
     * @param deviceAddress
     * @param data
     * @return
     */
    public boolean send(String deviceAddress, byte[] data) {
        /*判空*/
        BluetoothGatt gatt = mGatts.get(deviceAddress);
        if (gatt == null) return false;
        BluetoothGattCharacteristic character = getCharacteristic(gatt);
        if (character == null) return false;
        /*向设置发送数据*/
        character.setValue(data);
        return gatt.writeCharacteristic(character);
    }

    /*Section: 状态回掉接口*/
    private List<OnStateChangedListener> mStateListeners;

    private List<OnDataReceivedListener> mDataListeners;

    public void addOnStateChangedListener(OnStateChangedListener onStateChangedListener) {
        if (mStateListeners == null) {
            mStateListeners = new CopyOnWriteArrayList<>();
        }
        mStateListeners.add(onStateChangedListener);
    }

    public void addOnDataReceivedListener(OnDataReceivedListener onDataReceivedListener) {
        if (mDataListeners == null) {
            mDataListeners = new CopyOnWriteArrayList<>();
        }
        mDataListeners.add(onDataReceivedListener);
    }

    public boolean removeOnStateChangedListener(OnStateChangedListener onStateChangedListener) {
        return mStateListeners.remove(onStateChangedListener);
    }

    public boolean removeOnDataReceivedListener(OnDataReceivedListener onDataReceivedListener) {
        return mDataListeners.remove(onDataReceivedListener);
    }
}
