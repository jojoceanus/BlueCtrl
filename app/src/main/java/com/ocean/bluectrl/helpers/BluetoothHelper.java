package com.ocean.bluectrl.helpers;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothHelper {

    private static final String TAG = "BluetoothHelper";
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket socket;
    private InputStream inputStream;
    private OutputStream outputStream;

    private OnConnectionListener connectionListener;
    private OnConnectionStatusListener connectionStatusListener;

    private Thread receiveThread;
    private final String MARKET_START = "$";
    private final String MARKET_END = "#";

    // 单例模式
    private static BluetoothHelper instance;

    private BluetoothHelper() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public static BluetoothHelper getInstance() {
        if (instance == null) {
            synchronized (BluetoothHelper.class) {
                if (instance == null) {
                    instance = new BluetoothHelper();
                }
            }
        }
        return instance;
    }

    // 设置消息接收监听器
    public void setOnMessageReceivedListener(OnConnectionListener listener) {
        this.connectionListener = listener;
    }

    // 设置连接状态监听器
    public void setOnConnectionStatusListener(OnConnectionStatusListener listener) {
        this.connectionStatusListener = listener;
    }

    // 连接到指定设备
    public void connectToDevice(String deviceAddress) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
                    socket = device.createRfcommSocketToServiceRecord(MY_UUID);
                    bluetoothAdapter.cancelDiscovery(); // 取消任何正在进行的发现过程

                    // 连接到设备
                    socket.connect();
                    Log.d(TAG, "Connected to the device.");

                    // 获取输入输出流
                    outputStream = socket.getOutputStream();
                    inputStream = socket.getInputStream();

                    // 启动接收消息线程
                    startReceivingMessages();

                    // 通知连接成功
                    if (connectionStatusListener != null) {
                        connectionStatusListener.onConnected();
                    }

                } catch (IOException e) {
                    Log.e(TAG, "Failed to connect to the device.", e);
                    closeConnection();

                    // 通知连接失败
                    if (connectionStatusListener != null) {
                        connectionStatusListener.onConnectionFailed(e.getMessage());
                    }
                }
            }
        }).start();
    }

    // 开始接收消息
    private void startReceivingMessages() {
        receiveThread = new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] buffer = new byte[1024];
                int bytes;

                try {
                    while (!Thread.currentThread().isInterrupted()) {
                        bytes = inputStream.read(buffer);
                        if (bytes > 0) {
                            final String receivedMessage = new String(buffer, 0, bytes);
                            Log.d(TAG, "Received: " + receivedMessage);

                            // 通知消息接收
                            if (connectionListener != null) {
                                connectionListener.onMessageReceived(receivedMessage);
                            }
                        }
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Failed to receive messages.", e);
                    closeConnection();

                    // 通知连接断开
                    if (connectionStatusListener != null) {
                        connectionStatusListener.onDisconnected(e.getMessage());
                    }
                }
            }
        });
        receiveThread.start();
    }

    // 发送消息
    public void sendMessage(final String message) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    outputStream.write(message.getBytes());
                    Log.d(TAG, "Sent: " + message);

                    // 通知消息发送成功
                    if (connectionListener != null) {
                        connectionListener.onMessageSent(message);
                    }

                } catch (IOException e) {
                    Log.e(TAG, "Failed to send message.", e);
                    closeConnection();

                    // 通知消息发送失败
                    if (connectionListener != null) {
                        connectionListener.onMessageSendFailed(e.getMessage());
                    }
                }
            }
        }).start();
    }

    // 关闭连接
    public void closeConnection() {
        if (socket != null) {
            try {
                socket.close();
                socket = null;
                inputStream = null;
                outputStream = null;

                // 中断接收线程
                if (receiveThread != null) {
                    receiveThread.interrupt();
                    receiveThread = null;
                }

                Log.d(TAG, "Connection closed.");

            } catch (IOException e) {
                Log.e(TAG, "Failed to close socket.", e);
            }
        }
    }

    // 消息接收监听器接口
    public interface OnConnectionListener {
        void onMessageReceived(String message);
        void onMessageSent(String message);
        void onMessageSendFailed(String errorMessage);
    }

    // 连接状态监听器接口
    public interface OnConnectionStatusListener {
        void onConnected();
        void onConnectionFailed(String errorMessage);
        void onDisconnected(String errorMessage);
    }

}
