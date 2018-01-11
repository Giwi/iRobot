package org.giwi.irobot;

import android.app.Activity;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;

/**
 * The interface Bt handler.
 */
public interface BTHandler {
    /**
     * Broadcast update.
     *
     * @param action the action
     */
    void broadcastUpdate(final String action);

    /**
     * Broadcast update.
     *
     * @param action         the action
     * @param characteristic the characteristic
     */
    void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic);

    /**
     * Gets activity.
     *
     * @return the activity
     */
    Activity getActivity();

    /**
     * Sets carac.
     *
     * @param gatt
     * @param carac the carac
     */
    void setCarac(BluetoothGatt gatt, BluetoothGattCharacteristic carac);
}