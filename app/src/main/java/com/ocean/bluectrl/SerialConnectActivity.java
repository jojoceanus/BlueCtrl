package com.ocean.bluectrl;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.ocean.bluectrl.helpers.BluetoothHelper;
import com.ocean.bluectrl.layouts.GlobalBackground;
import com.ocean.bluectrl.util.ColorStyleUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class SerialConnectActivity extends AppCompatActivity {

    private TextView deviceNameText;
    private TextView deviceAddressText;
    private TextView receiveText;
    private ScrollView receiveScrollView;
    private EditText sendEditText;
    private Button sendButton;
    private CheckBox hexFormat;
    private BluetoothAdapter bluetoothAdapter;
    private String deviceName;
    private String deviceAddress;
    private BluetoothHelper bluetoothHelper;
    private final String startMarket = "$";
    private final String endMarket = "#";
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_serial_connect);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        deviceNameText = findViewById(R.id.devicename_text);
        deviceAddressText = findViewById(R.id.deviceaddress_text);
        receiveText = findViewById(R.id.receive_text);
        receiveScrollView = findViewById(R.id.receive_scrollview);
        sendEditText = findViewById(R.id.send_edittext);
        sendButton = findViewById(R.id.send_button);
        hexFormat = findViewById(R.id.hex_format);

        //获取全局背景并设置颜色
        GlobalBackground backgroundLayout = findViewById(R.id.background_layout);
        ImageView imageView = backgroundLayout.getImageView();
        View transparentView = backgroundLayout.getBackgroundView();
        ColorStyleUtil.setViewColor(this, imageView, transparentView, ColorStyleUtil.VIBRANT_COLOR, 0.75f);
        //设置状态栏颜色
        ColorStyleUtil.setStatusBarColor(this, imageView, ColorStyleUtil.VIBRANT_COLOR);
        //设置全部控件的颜色
        ColorStyleUtil.setAllViewBackgroundColors(this, imageView, ColorStyleUtil.MUTED_COLOR, 0.5f);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("DEVICE_NAME") && intent.hasExtra("DEVICE_ADDRESS")) {
            deviceName = intent.getStringExtra("DEVICE_NAME");
            deviceAddress = intent.getStringExtra("DEVICE_ADDRESS");

            deviceNameText.setText(deviceName);
            deviceAddressText.setText(deviceAddress);
        } else {
            Intent errorIntent = new Intent(SerialConnectActivity.this, MainActivity.class);
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
                        String currentTime = new SimpleDateFormat("HH:mm:ss:SSS", Locale.getDefault()).format(new Date());
                        String show = "[ " + currentTime + " ] " + " received: " + message + "\n";
                        receiveText.append(show);
                        receiveScrollView.post(() -> {
                            // 计算并设置滚动位置
                            receiveScrollView.smoothScrollTo(0, receiveText.getBottom());
                        });
                    }
                });
            }

            @Override
            public void onMessageSent(String message) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String currentTime = new SimpleDateFormat("HH:mm:ss:SSS", Locale.getDefault()).format(new Date());
                        String show = "[ " + currentTime + " ] " + " send: " + message + "\n";
                        receiveText.append(show);
                    }
                });
            }

            @Override
            public void onMessageSendFailed(String errorMessage) {

            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String rawMessage = sendEditText.getText().toString();
                String messageToSend;
                // 根据CheckBox状态决定是否转换
                if (hexFormat.isChecked()) {
                    messageToSend = convertHex(rawMessage);
                    if(messageToSend.isEmpty()) {
                        return;
                    }
                } else {
                    messageToSend = rawMessage; // 不转换，直接发送原始文本
                }
                bluetoothHelper.sendMessage(messageToSend);
                //bluetoothHelper.sendMessage(rawMessage);
                //sendtext.setText(""); // 清空输入框
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bluetoothHelper.closeConnection();
    }

    private String convertHex(String input) {
        // 处理空输入
        if (input == null || input.isEmpty()) {
            Toast.makeText(this, "illeagle hex string", Toast.LENGTH_SHORT).show();
            return "";
        }
        // 校验长度必须为偶数（每两位表示一个字节）
        if (input.length() % 2 != 0) {
            Toast.makeText(this, "illeagle hex string", Toast.LENGTH_SHORT).show();
            return "";
            // throw new IllegalArgumentException("输入字符串长度必须为偶数（每两位表示一个十六进制字节）");
        }
        StringBuilder result = new StringBuilder();
        // 遍历每两位十六进制字符
        for (int i = 0; i < input.length(); i += 2) {
            // 截取当前两位十六进制字符（如 "48", "65"）
            String hexPair = input.substring(i, i + 2);
            try {
                // 将十六进制字符串转换为整数（0-255）
                int byteValue = Integer.parseInt(hexPair, 16);

                // 转换为字符并追加到结果
                result.append((char) byteValue);
            } catch (NumberFormatException e) {
                // 捕获无效十六进制字符异常（如包含 G、H 等非十六进制字符）
                // throw new IllegalArgumentException("无效的十六进制字符: '" + hexPair + "'", e);
                Toast.makeText(this, "illeagle hex string", Toast.LENGTH_SHORT).show();
                return "";
            }
        }

        return result.toString();
    }
}