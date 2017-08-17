package com.eraare.ble;

import java.util.UUID;

/**
 * @author Leo
 * @version 1
 * @since 2017-08-16
 * UUID
 */
public interface BLEUUID {
    /**
     * 服务所用的UUID
     *
     * @return
     */
    UUID getServiceUUID();

    /**
     * 可读可写可通知特征的UUID
     *
     * @return
     */
    UUID getCharacteristicUUID();

    /**
     * 上报通知的描述UUID
     *
     * @return
     */
    UUID getDescriptorUUID();
}
