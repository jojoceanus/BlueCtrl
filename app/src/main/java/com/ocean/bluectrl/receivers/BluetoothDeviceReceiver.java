package com.ocean.bluectrl.receivers;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BluetoothDeviceReceiver extends BroadcastReceiver {
    private OnDeviceFoundListener listener;

    public interface OnDeviceFoundListener {
        void onDeviceFound(BluetoothDevice device);
    }

    public BluetoothDeviceReceiver(OnDeviceFoundListener listener) {
        this.listener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if (device != null && listener != null) {
                listener.onDeviceFound(device);
            }
        }
    }
}
