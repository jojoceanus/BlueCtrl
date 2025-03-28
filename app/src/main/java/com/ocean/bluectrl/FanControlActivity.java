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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FanControlActivity extends AppCompatActivity {

    private TextView deviceNameText;
    private TextView deviceAddressText;
    private TextView speedGet;
    private TextView humidityGet;
    private TextView temperatureGet;
    private SeekBar speedControl;
    private int setSpeed;
    private BluetoothSocket socket;
    private OutputStream outputStream;
    private InputStream inputStream;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothHelper bluetoothHelper;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private final String endMarket = "#";
    private final String startMarket = "$";
    private final String devideMarket = " ";
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
        speedGet = findViewById(R.id.speedget_text);
        humidityGet = findViewById(R.id.humidity_text);
        temperatureGet = findViewById(R.id.temperature_text);
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
                        /*
                        String show = String.format(Locale.CHINA ,"转速：" + message);
                        speedGet.setText(show);
                        */

                        // 使用正则表达式匹配所有独立消息单元（格式：$... #）
                        Pattern pattern = Pattern.compile("\\$([^#]+)#");
                        Matcher matcher = pattern.matcher(message);

                        while (matcher.find()) {
                            String payload = matcher.group(1).trim(); // 提取$和#之间的内容
                            String[] parts = payload.split(" ", 2); // 限制分割次数

                            if (parts.length < 2) {
                                Log.e("Protocol", "Incomplete data: " + payload);
                                continue;
                            }

                            String tag = parts[0].trim();
                            String data = parts[1].trim();

                            // 根据标签路由处理
                            switch (tag) {
                                case "SPEED":
                                    speedGet.setText(String.format(Locale.CHINA, "转速：%srpm", data));
                                    break;
                                case "HUMIDITY":
                                    humidityGet.setText(String.format(Locale.CHINA, "湿度：%s%%", data));
                                    break;
                                case "TEMPERATURE":
                                    temperatureGet.setText(String.format(Locale.CHINA, "温度：%s℃", data));
                                    break;
                                default:
                                    Log.w("Protocol", "Unknown tag: " + tag);
                                    break;
                            }
                        }
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
                String messageToSend = bluetoothMessage("SPEED", progress);
                bluetoothHelper.sendMessage(messageToSend);
                /*
                if (sendFlag) {
                    String messageToSend = progress + endMarket;
                    bluetoothHelper.sendMessage(messageToSend);
                    sendFlag = false;
                    handler.postDelayed(resetRunnable, 200);
                }
                */
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

    private String bluetoothMessage(String sendTag, int sendData) {
        return startMarket + sendTag + devideMarket + sendData + endMarket;
    }

    private String bluetoothMessage(String sendTag, String sendData) {
        return startMarket + sendTag + devideMarket + sendData + endMarket;
    }

}