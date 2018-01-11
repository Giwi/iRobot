package org.giwi.irobot.callbacks;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.util.Log;

import org.giwi.irobot.BTHandler;

/**
 * The type B le scan callback.
 */
public class MyLeScanCallback implements BluetoothAdapter.LeScanCallback {
    private final String TAG = MyLeScanCallback.class.getCanonicalName();
    private BluetoothGatt mBluetoothGatt;
    private BTHandler mainActivity;

    /**
     * Instantiates a new B le scan callback.
     *
     * @param mainActivity the main activity
     */
    public MyLeScanCallback(BTHandler mainActivity) {
        super();
        this.mainActivity = mainActivity;
    }

    @Override
    public void onLeScan(BluetoothDevice device, int i, byte[] bytes) {
        Log.i(TAG, "BLE device found: " + device.getName() + "; MAC " + device.getAddress());
        if ("Evolution-Robot".equals(device.getName())) {
            mBluetoothGatt = device.connectGatt(mainActivity.getActivity(), false, new MyBluetoothGattCallback(this.mainActivity));
        }
    }
}