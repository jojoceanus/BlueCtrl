package com.ocean.bluectrl.adapters;

import android.bluetooth.BluetoothDevice;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ocean.bluectrl.R;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BluetoothDeviceAdapter extends RecyclerView.Adapter<BluetoothDeviceAdapter.ViewHolder> {

    private List<BluetoothDevice> btDevices;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        public void onItemClick(int position);
    }

    public BluetoothDeviceAdapter (List<BluetoothDevice> deviceList) {
        this.btDevices = deviceList;
    }

    public void setOnItemClickListener(OnItemClickListener itemClickListener) {
        this.onItemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.bluetoothdevice_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BluetoothDevice btDevice = btDevices.get(position);
        if (TextUtils.isEmpty(btDevice.getName())) {
            holder.deviceName.setText("unknown device");
        } else {
            holder.deviceName.setText(btDevice.getName());
        }
        holder.deviceAddress.setText(btDevice.getAddress());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.onItemClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return btDevices.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            deviceName = itemView.findViewById(R.id.devicename_text);
            deviceAddress = itemView.findViewById(R.id.deviceaddress_text);
        }
    }

}
