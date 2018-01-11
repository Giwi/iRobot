package org.giwi.irobot.callbacks;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.util.Log;

import org.giwi.irobot.BTHandler;

import java.util.List;

/**
 * The type My bluetooth gatt callback.
 */
public class MyBluetoothGattCallback extends BluetoothGattCallback {
    private final String TAG = MyBluetoothGattCallback.class.getCanonicalName();

    private BTHandler mainActivity;

    private static final int REQUEST_ENABLE_BT = 666;
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;
    private int mConnectionState = STATE_DISCONNECTED;
    private final static String ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    private final static String ACTION_GATT_DISCONNECTED = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    private final static String ACTION_GATT_SERVICES_DISCOVERED = "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    private final static String ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    private static final String UUID_KEY_WRITE = "0000fff5-0000-1000-8000-00805f9b34fb";
    /**
     * The constant EXTRA_DATA.
     */
    public final static String EXTRA_DATA = "com.example.bluetooth.le.EXTRA_DATA";

    /**
     * Instantiates a new My bluetooth gatt callback.
     *
     * @param mainActivity the main activity
     */
    public MyBluetoothGattCallback(BTHandler mainActivity) {
        super();
        this.mainActivity = mainActivity;
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                        int newState) {
        String intentAction;
        if (newState == BluetoothProfile.STATE_CONNECTED) {
            intentAction = ACTION_GATT_CONNECTED;
            mConnectionState = STATE_CONNECTED;
            this.mainActivity.broadcastUpdate(intentAction);
            Log.i(TAG, "Connected to GATT server.");
            Log.i(TAG, "Attempting to start service discovery : " + gatt.discoverServices());

        } else if (newState == STATE_DISCONNECTED) {
            intentAction = ACTION_GATT_DISCONNECTED;
            mConnectionState = STATE_DISCONNECTED;
            Log.i(TAG, "Disconnected from GATT server.");
            this.mainActivity.broadcastUpdate(intentAction);
        }
    }

    @Override
    // New services discovered
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        Log.i(TAG, "onServicesDiscovered()");
        if (status == BluetoothGatt.GATT_SUCCESS) {
            List<BluetoothGattService> gattServices = gatt.getServices();
            Log.i(TAG, "Services count: " + gattServices.size());
            for (BluetoothGattService gattService : gattServices) {
                String serviceUUID = gattService.getUuid().toString();
                Log.i(TAG, "Service uuid " + serviceUUID);
                for (BluetoothGattCharacteristic carac : gattService.getCharacteristics()) {
                    Log.i(TAG, "Carac uuid " + carac.getUuid().toString());
                    if(UUID_KEY_WRITE.equals(carac.getUuid().toString())) {
                        Log.e(TAG, carac.getUuid().toString() + " found");
                        this.mainActivity.setCarac(gatt, carac);
                    }
                }
            }
            this.mainActivity.broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
        } else {
            Log.w(TAG, "onServicesDiscovered received: " + status);
        }

    }

    @Override
    // Result of a characteristic read operation
    public void onCharacteristicRead(BluetoothGatt gatt,
                                     BluetoothGattCharacteristic characteristic,
                                     int status) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            this.mainActivity.broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }
    }
}