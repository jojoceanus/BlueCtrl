package com.ocean.bluectrl;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ocean.bluectrl.adapters.BluetoothDeviceAdapter;
import com.ocean.bluectrl.layouts.GlobalBackground;
import com.ocean.bluectrl.receivers.BluetoothDeviceReceiver;
import com.ocean.bluectrl.util.PermissionUtil;
import com.ocean.bluectrl.util.ColorStyleUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private int clickCount = 0;
    private final int REQUIRED_CLICKS = 7; // 需要的点击次数
    private final long DURATION_MILLIS = 2000; // 持续时间，毫秒
    private Handler handler;
    private Runnable resetRunnable;
    private TextView titleText;
    private List<BluetoothDevice> findDevices = new ArrayList<BluetoothDevice>();
    private List<BluetoothDevice> pairDevices = new ArrayList<BluetoothDevice>();
    private RecyclerView findRecycleView;
    private RecyclerView pairRecycleView;
    private BluetoothDeviceAdapter findAdapter;
    private BluetoothDeviceAdapter pairAdapter;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDeviceReceiver bluetoothDeviceReceiver;
    private Button refreshButton;
    private Button switchButton;
    private int switchFlag;
//    private static final String[] permissions = new String[] {
//            android.Manifest.permission.BLUETOOTH_SCAN,
//            android.Manifest.permission.BLUETOOTH_CONNECT,
//            android.Manifest.permission.BLUETOOTH_ADVERTISE,
//            Manifest.permission.ACCESS_FINE_LOCATION
//    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult o) {
                if (o.getResultCode() == MainActivity.RESULT_OK) {
                    Toast.makeText(MainActivity.this, "蓝牙已启用", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "蓝牙未启用", Toast.LENGTH_SHORT).show();
                }
            }
        });

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        titleText = findViewById(R.id.title_text);
        findRecycleView = findViewById(R.id.find_list);
        pairRecycleView = findViewById(R.id.pair_list);
        refreshButton = findViewById(R.id.refresh_button);
        switchButton = findViewById(R.id.switch_button);
        LinearLayoutManager findLayoutManager = new LinearLayoutManager(this);
        findRecycleView.setLayoutManager(findLayoutManager);
        LinearLayoutManager pairLayoutManager = new LinearLayoutManager(this);
        pairRecycleView.setLayoutManager(pairLayoutManager);
        findAdapter = new BluetoothDeviceAdapter(findDevices);
        pairAdapter = new BluetoothDeviceAdapter(pairDevices);
        findRecycleView.setAdapter(findAdapter);
        pairRecycleView.setAdapter(pairAdapter);
        switchFlag = 1;

        titleText.setText(R.string.bluetooth_device);
        refreshButton.setText(R.string.refresh);
        switchButton.setText(R.string.custom);

        // 获取全局背景并设置颜色
        GlobalBackground backgroundLayout = findViewById(R.id.background_layout);
        ImageView imageView = backgroundLayout.getImageView();
        View transparentView = backgroundLayout.getBackgroundView();
        ColorStyleUtil.setViewColor(this, imageView, transparentView, ColorStyleUtil.VIBRANT_COLOR, 0.75f);
        // 设置状态栏颜色
        ColorStyleUtil.setStatusBarColor(this, imageView, ColorStyleUtil.VIBRANT_COLOR);
        // 设置全部的控件的颜色
        ColorStyleUtil.setAllViewBackgroundColors(this, imageView, ColorStyleUtil.MUTED_COLOR, 0.5f);

        bluetoothDeviceReceiver = new BluetoothDeviceReceiver(new BluetoothDeviceReceiver.OnDeviceFoundListener() {
            @Override
            public void onDeviceFound(BluetoothDevice device) {
                if (!findDevices.contains(device)) {
                    findDevices.add(device);
                    findAdapter.notifyDataSetChanged();
                }
            }
        });

        // 注册广播接收器
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(bluetoothDeviceReceiver, filter);

//        PermissionUtil.checkPermission(this, permissions, 1);
        PermissionUtil.checkAndRequestPermissions(this, 1);
        refreshList();

        handler = new Handler(Looper.getMainLooper());
        resetRunnable = new Runnable() {
            @Override
            public void run() {
                resetClickCount();
                Log.d("oceanus", "run: time out");
            }
        };

        titleText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (clickCount == 0) {
                    handler.postDelayed(resetRunnable, DURATION_MILLIS);
                }
                clickCount++;
                Log.d("ocean", "onClick: " + String.valueOf(clickCount));
                if (clickCount >= REQUIRED_CLICKS) {
                    showEasterEgg();
                }
            }
        });

        findAdapter.setOnItemClickListener(new BluetoothDeviceAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Toast.makeText(MainActivity.this, "pair with: " + findDevices.get(position).getName(), Toast.LENGTH_SHORT).show();
                pairWithDevice(position);
            }
        });
        pairAdapter.setOnItemClickListener(new BluetoothDeviceAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Toast.makeText(MainActivity.this, "connect to：" + pairDevices.get(position).getName(), Toast.LENGTH_SHORT).show();
                launchConnectActivity(position);
            }
        });

        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshList();
            }
        });

        switchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (switchFlag == 0) {
                    switchFlag = 1;
                    switchButton.setText(R.string.custom);
                } else if (switchFlag == 1) {
                    switchFlag = 0;
                    switchButton.setText(R.string.serial);
                }
            }
        });

        IntentFilter filter1 = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(bondStateReceiver, filter1);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(bluetoothDeviceReceiver);
        unregisterReceiver(bondStateReceiver);
        bluetoothAdapter.cancelDiscovery();
    }

    private void refreshList() {
        findDevices.clear();
        pairDevices.clear();
        if (PermissionUtil.checkAndRequestPermissions(this, 1)) {
//        if (PermissionUtil.checkPermission(this, permissions,1)) {
            if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled()) {
                Toast.makeText(this, "bluetooth is off", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "searching for devices", Toast.LENGTH_SHORT).show();
                Set<BluetoothDevice> devicesSet= bluetoothAdapter.getBondedDevices();
                pairDevices.addAll(devicesSet);
/*                if (pairDevices.size() > 0) {
                    for (BluetoothDevice device : pairDevices) {
                        String deviceName = device.getName();
                        String deviceHardwareAddress = device.getAddress(); // MAC地址
                         Log.d("oceanus!", "蓝牙设备名称："+deviceName);
                         Log.d("oceanus!", "蓝牙设备地址："+deviceHardwareAddress);
                    }
                }*/
                pairAdapter.notifyDataSetChanged();
                bluetoothAdapter.startDiscovery();
            }
        }
    }

    private void pairWithDevice(int position) {
        BluetoothDevice device = findDevices.get(position);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    BluetoothSocket socket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
                    socket.connect();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "配对成功", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "配对失败", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                //refreshList();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        refreshList();
                    }
                });
            }
        }).start();
    }

    private void launchConnectActivity(int position) {
        if (switchFlag == 0) {
            Intent intent = new Intent(MainActivity.this, SerialConnectActivity.class);
            intent.putExtra("DEVICE_NAME", pairDevices.get(position).getName());
            intent.putExtra("DEVICE_ADDRESS", pairDevices.get(position).getAddress());
            startActivity(intent);
        } else if (switchFlag == 1) {
            if (pairDevices.get(position).getName().equals("智能风扇")) {
                Intent intent = new Intent(MainActivity.this, FanControlActivity.class);
                intent.putExtra("DEVICE_NAME", pairDevices.get(position).getName());
                intent.putExtra("DEVICE_ADDRESS", pairDevices.get(position).getAddress());
                startActivity(intent);
            } else {
                Toast.makeText(this, "activity undefined", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showEasterEgg() {
        // 跳转到彩蛋界面的逻辑
        Intent intent = new Intent(this, StyleSetActivity.class);
        startActivity(intent);
        resetClickCount(); // 显示彩蛋后重置点击计数
        handler.removeCallbacks(resetRunnable);
    }
    private void resetClickCount() {
        clickCount = 0;
    }

    private void applyColors(int statusBarColor, int iconColor) {
        // 设置状态栏颜色
        Window window = getWindow();
        window.setStatusBarColor(statusBarColor);

        // 设置图标颜色
        //Toolbar toolbar = findViewById(R.id.toolbar);
        //toolbar.setTitleTextColor(iconColor);
        //toolbar.setSubtitleTextColor(iconColor);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (!PermissionUtil.checkGrant(grantResults)) {
                    Toast.makeText(this, "lack permission", Toast.LENGTH_SHORT).show();
                    for (int i = 0; i < grantResults.length; i++) {
                        Log.d("ocean", permissions[i] + " failed!");
                    }
                    jumpToSettings();
                }
                break;
        }
    }

    private BroadcastReceiver bondStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                int bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.BOND_NONE);
                int previousBondState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.BOND_NONE);

                // 当配对状态变为 BOND_BONDED（已配对）时，刷新列表
                if (bondState == BluetoothDevice.BOND_BONDED) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            refreshList(); // 刷新最新已配对设备列表
                        }
                    });
                }
            }
        }
    };

    private void jumpToSettings() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.fromParts("package", getPackageName(), null));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}