package com.ocean.bluectrl;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.ocean.bluectrl.helpers.BluetoothHelper;
import com.ocean.bluectrl.layouts.GlobalBackground;
import com.ocean.bluectrl.util.ColorStyleUtil;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FanControlActivity extends AppCompatActivity implements MaunalControlFragment.OnProgressSendListener, AutomaticControlFragment.OnBTClickListener{
    private TextView deviceNameText;
    private TextView deviceAddressText;
    private TextView speedGet;
    private TextView humidityGet;
    private TextView temperatureGet;
    private ViewPager viewPager;
    private TabLayout methodControl;
    private TextView monitorText;
    private TextView controlText;
    private int setSpeed;
    private BluetoothHelper bluetoothHelper;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private final String endMarket = "#";
    private final String startMarket = "$";
    private final String devideMarket = " ";
    private String deviceName;
    private String deviceAddress;
    public enum Tags {
        NATW(0x01),
        HOLDH(0x02),
        HOLDT(0x03),
        SPEED(0x04),
        HUMIDITY(0x05),
        TEMPERATURE(0x06);

        private final int value;

        // 构造函数，用于初始化每个枚举值的整数值
        Tags(int value) {
            this.value = value;
        }

        // 获取枚举对应的整数值
        public int getValue() {
            return value;
        }

        // 根据整数值获取对应的枚举
        public static Tags fromValue(int value) {
            for (Tags tag : Tags.values()) {
                if (tag.getValue() == value) {
                    return tag;
                }
            }
            throw new IllegalArgumentException("Invalid tag value: " + value);
            // 或者你可以返回 null / 默认值
        }

    }

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
        viewPager = findViewById(R.id.viewPager);
        methodControl = findViewById(R.id.control_method);
        monitorText = findViewById(R.id.text_monitor);
        controlText = findViewById(R.id.text_control);
        setSpeed = 0;

        monitorText.setText(R.string.status_monitor);
        controlText.setText(R.string.fan_control);
        humidityGet.setText(getString(R.string.humidity_show, getString(R.string.humidity), getString(R.string.no_data)));
        temperatureGet.setText(getString(R.string.temperature_show, getString(R.string.temperature), getString(R.string.no_data)));
        speedGet.setText(getString(R.string.fan_speed_show, getString(R.string.fan_speed), getString(R.string.no_data)));

        //获取全局背景并设置颜色
        GlobalBackground backgroundLayout = findViewById(R.id.background_layout);
        ImageView imageView = backgroundLayout.getImageView();
        View transparentView = backgroundLayout.getBackgroundView();
        ColorStyleUtil.setViewColor(this, imageView, transparentView, ColorStyleUtil.VIBRANT_COLOR, 0.75f);
        //设置状态栏颜色
        ColorStyleUtil.setStatusBarColor(this, imageView, ColorStyleUtil.VIBRANT_COLOR);
        //设置全部的控件的颜色
        ColorStyleUtil.setAllViewBackgroundColors(this, imageView, ColorStyleUtil.MUTED_COLOR, 0.5f);

        //设置tabindicator颜色
        methodControl.setSelectedTabIndicatorColor(ColorStyleUtil.getColor(this, ColorStyleUtil.MUTED_DARK_COLOR));

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

        // 控制方式适配器
        MyPagerAdapter adapter = new MyPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        methodControl.setupWithViewPager(viewPager);

        bluetoothHelper = BluetoothHelper.getInstance();
        bluetoothHelper.connectToDevice(deviceAddress);
        bluetoothHelper.setOnMessageReceivedListener(new BluetoothHelper.OnConnectionListener() {
            @Override
            public void onMessageReceived(String message) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

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

                            //String tag = parts[0].trim();
                            int tagValue = Integer.parseInt(parts[0].trim());
                            Tags tag = Tags.fromValue(tagValue);
                            String data = parts[1].trim();

                            // 根据标签路由处理
                            switch (tag) {
                                case SPEED:
                                    speedGet.setText(String.format(getString(R.string.fan_speed_show), getString(R.string.fan_speed), data));
                                    break;
                                case HUMIDITY:
                                    humidityGet.setText(String.format(getString(R.string.humidity_show), getString(R.string.humidity), data));
                                    break;
                                case TEMPERATURE:
                                    temperatureGet.setText(String.format(getString(R.string.temperature_show), getString(R.string.temperature), data));
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

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bluetoothHelper.closeConnection();
    }

    private String bluetoothMessage(String sendTag, int sendData) {
        return startMarket + sendTag + devideMarket + sendData + endMarket;
    }

    @Override
    public void onProgressSent(int progress) {
        //sendBluetoothData(progress);
        setSpeed = progress;
        String messageToSend = bluetoothMessage(Tags.SPEED, progress);
        bluetoothHelper.sendMessage(messageToSend);
    }

    @Override
    public void clickBTTemperature(int temperature) {
        String messageToSend = bluetoothMessage(Tags.HOLDT, temperature);
        bluetoothHelper.sendMessage(messageToSend);
    }

    @Override
    public void clickBTHumidity(int humidity) {
        String messageToSend = bluetoothMessage(Tags.HOLDH, humidity);
        bluetoothHelper.sendMessage(messageToSend);
    }

    @Override
    public void clickBTSimulate() {
        String messageToSend = bluetoothMessage(Tags.NATW, 0);
        bluetoothHelper.sendMessage(messageToSend);
    }

    private String bluetoothMessage(Tags sendTag, int sendData) {
        char tagHex = (char) sendTag.getValue();
        return startMarket + tagHex + devideMarket + sendData + endMarket;
    }

    class MyPagerAdapter extends FragmentPagerAdapter {
        private final String[] tabTitles = new String[]{getString(R.string.maunal), getString(R.string.auto)};

        public MyPagerAdapter(FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @Override
        public Fragment getItem(int position) {
            return (position == 0) ? new MaunalControlFragment() : new AutomaticControlFragment();
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabTitles[position];
        }
    }

}