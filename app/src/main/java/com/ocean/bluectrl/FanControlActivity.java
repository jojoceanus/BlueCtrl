package com.ocean.bluectrl;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.ocean.bluectrl.helpers.BluetoothHelper;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;
import java.util.UUID;

public class FanControlActivity extends AppCompatActivity {

    private TextView deviceNameText;
    private TextView deviceAddressText;
    private TextView speedSet;
    private TextView speedGet;
    private SeekBar speedControl;
    private int setSpeed;
    private BluetoothSocket socket;
    private OutputStream outputStream;
    private InputStream inputStream;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothHelper bluetoothHelper;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private final String endMarket = "#";
    private String deviceName;
    private String deviceAddress;
    private Handler handler;
    private Runnable resetRunnable;
    private boolean sendFlag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_fan_control);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        deviceNameText = findViewById(R.id.devicename_text);
        deviceAddressText = findViewById(R.id.deviceaddress_text);
        speedSet = findViewById(R.id.speedset_text);
        speedGet = findViewById(R.id.speedget_text);
        speedControl = findViewById(R.id.speedcontrol_seekbar);
        setSpeed = 0;

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("DEVICE_NAME") && intent.hasExtra("DEVICE_ADDRESS")) {
            deviceName = intent.getStringExtra("DEVICE_NAME");
            deviceAddress = intent.getStringExtra("DEVICE_ADDRESS");

            deviceNameText.setText(deviceName);
            deviceAddressText.setText(deviceAddress);
        } else {
            Intent errorIntent = new Intent(FanControlActivity.this, MainActivity.class);
            startActivity(errorIntent);
        }

        bluetoothHelper = BluetoothHelper.getInstance();
        bluetoothHelper.connectToDevice(deviceAddress);
        bluetoothHelper.setOnMessageReceivedListener(new BluetoothHelper.OnConnectionListener() {
            @Override
            public void onMessageReceived(String message) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String show = String.format(Locale.CHINA ,"实际转速：" + message);
                        speedGet.setText(show);
                    }
                });
            }

            @Override
            public void onMessageSent(String message) {

            }

            @Override
            public void onMessageSendFailed(String errorMessage) {

            }
        });

        sendFlag = true;
        handler = new Handler(Looper.getMainLooper());
        resetRunnable = new Runnable() {
            @Override
            public void run() {
                sendFlag = !sendFlag;
            }
        };

        speedControl.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                String show = String.format(Locale.CHINA ,"设定风速：%d%%", progress);
                setSpeed = progress;
                speedSet.setText(show);
                String messageToSend = progress + endMarket;
                bluetoothHelper.sendMessage(messageToSend);
/*                if (sendFlag) {
                    String messageToSend = progress + endMarket;
                    bluetoothHelper.sendMessage(messageToSend);
                    sendFlag = false;
                    handler.postDelayed(resetRunnable, 200);
                }*/
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bluetoothHelper.closeConnection();
    }

}