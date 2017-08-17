package com.eraare.ble;

import java.util.UUID;

/**
 * @author Leo
 * @version 1
 * @since 2017-08-17
 * 默认的UUID
 */
public final class DefaultUUID implements BLEUUID {
    //服务所用的UUID
    private static final UUID UUID_SERVICE = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
    //特征所用的UUID
    private static final UUID UUID_CHARACTERISTIC = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");
    //描述所用的UUID
    private static final UUID UUID_DESCRIPTOR = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    @Override
    public UUID getServiceUUID() {
        return UUID_SERVICE;
    }

    @Override
    public UUID getCharacteristicUUID() {
        return UUID_CHARACTERISTIC;
    }

    @Override
    public UUID getDescriptorUUID() {
        return UUID_DESCRIPTOR;
    }
}
