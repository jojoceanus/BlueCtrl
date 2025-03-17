package com.ocean.bluectrl.models;

public class BluetoothDeviceUnused {
    private String name;
    private String address;

    public BluetoothDeviceUnused(String name, String address) {
        this.name = name;
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
