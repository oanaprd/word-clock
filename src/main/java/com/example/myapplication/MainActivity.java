package com.example.myapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.graphics.drawable.DrawableCompat;

import yuku.ambilwarna.AmbilWarnaDialog;

import java.util.Set;
import java.util.UUID;

@RequiresApi(api = Build.VERSION_CODES.S)
public class MainActivity extends AppCompatActivity {
    private static final String DEVICE_NAME = "WordClock";
    private static final String SERVICE_UUID = "19B10000-E8F2-537E-4F6C-D104768A1214";
    private static final String CHARACTERISTIC_UUID = "19B10001-E8F2-537E-4F6C-D104768A1214";
    private static final String BRIGHTNESS_CHARACTERISTIC_UUID = "19B10002-E8F2-537E-4F6C-D104768A1214";
    private static final String AUTO_BRIGHTNESS_CHARACTERISTIC_UUID = "19B10003-E8F2-537E-4F6C-D104768A1214";
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGatt bluetoothGatt;
    private BluetoothGattCharacteristic ledCharacteristic;
    private BluetoothGattCharacteristic brightnessCharacteristic;
    private BluetoothGattCharacteristic autoBrightnessCharacteristic;
    private Button connectButton;
    private Button disconnectButton;
    private TextView colorDisplay;
    private int selectedColor = 0xFFFFFF;
    private boolean autoBrightnessEnabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI components
        connectButton = findViewById(R.id.connectButton);
        disconnectButton = findViewById(R.id.disconnectButton);
        Button colorPickerButton = findViewById(R.id.colorPickerButton);
        SeekBar brightnessSeekBar = findViewById(R.id.brightnessSeekBar);
        ToggleButton autoBrightnessButton = findViewById(R.id.autoBrightnessButton);

        // Ensure connect button is visible initially
        connectButton.setVisibility(View.VISIBLE);
        disconnectButton.setVisibility(View.GONE);

        // Bluetooth setup
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Toast.makeText(this, "Bluetooth is not enabled", Toast.LENGTH_SHORT).show();
            return;
        }

        // Set up color picker button
        colorPickerButton.setOnClickListener(v -> openColorPicker());

        // Set up brightness SeekBar
        brightnessSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                sendBrightnessToArduino(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Do something when the touch starts
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Do something when the touch stops
            }
        });

        autoBrightnessButton.setOnClickListener(this::onAutoBrightnessButtonClick);

        // Check and request permissions if needed
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            startScanning();
        }

        // If no saved instance state, add SettingsFragment
        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, new SettingsFragment());
            transaction.commit();
        }
    }

    @SuppressLint("MissingPermission")
    private void sendBrightnessToArduino(int sliderPosition) {
        // Ensure slider position is within bounds
        if (sliderPosition < 0) sliderPosition = 0;
        if (sliderPosition > 100) sliderPosition = 100;

        int brightness = mapSliderToBrightness(sliderPosition);

        if (brightnessCharacteristic != null && bluetoothGatt != null) {
            byte[] brightnessValue = new byte[]{(byte) brightness};
            brightnessCharacteristic.setValue(brightnessValue);
            bluetoothGatt.writeCharacteristic(brightnessCharacteristic);
        }
    }

    private int mapSliderToBrightness(int sliderPosition) {
        // Linear mapping from slider position (0-100) to brightness (1-255)
        return (int) ((sliderPosition / 100.0) * 254) + 1;
    }

    @SuppressLint("MissingPermission")
    private void startScanning() {
        BluetoothLeScanner bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        bluetoothLeScanner.startScan(scanCallback);
    }

    private final ScanCallback scanCallback = new ScanCallback() {
        @SuppressLint("MissingPermission")
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice device = result.getDevice();
            if (DEVICE_NAME.equals(device.getName())) {
                BluetoothLeScanner bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
                bluetoothLeScanner.stopScan(this);

                // Assign click listener to connect button to initiate connection
                connectButton.setOnClickListener(v -> connectToDevice(device));
            }
        }
    };

    @SuppressLint("MissingPermission")
    public void onConnectButtonClick(View view) {
        connectToDeviceByName();
    }

    @SuppressLint("MissingPermission")
    public void onDisconnectButtonClick(View view) {
        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
            bluetoothGatt.close();
            bluetoothGatt = null;

            // Show toast indicating device is disconnected
            Toast.makeText(MainActivity.this, "Device disconnected", Toast.LENGTH_SHORT).show();

            // Update UI
            connectButton.setVisibility(View.VISIBLE); // Show connect button
            disconnectButton.setVisibility(View.GONE); // Hide disconnect button
        }
    }

    @SuppressLint("MissingPermission")
    private void connectToDevice(BluetoothDevice device) {
        if (bluetoothGatt != null) {
            bluetoothGatt.close();
            bluetoothGatt = null;
        }

        // Connect to the device
        bluetoothGatt = device.connectGatt(this, false, new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    gatt.discoverServices();

                    // Show toast indicating device is connected
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "Device connected", Toast.LENGTH_SHORT).show();
                        connectButton.setVisibility(View.GONE); // Hide connect button
                        disconnectButton.setVisibility(View.VISIBLE); // Show disconnect button
                    });
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    // Handle disconnection
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "Device disconnected", Toast.LENGTH_SHORT).show();
                        connectButton.setVisibility(View.VISIBLE); // Show connect button
                        disconnectButton.setVisibility(View.GONE); // Hide disconnect button
                    });
                }
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                BluetoothGattService service = gatt.getService(UUID.fromString(SERVICE_UUID));
                if (service != null) {
                    ledCharacteristic = service.getCharacteristic(UUID.fromString(CHARACTERISTIC_UUID));
                    brightnessCharacteristic = service.getCharacteristic(UUID.fromString(BRIGHTNESS_CHARACTERISTIC_UUID)); // Initialize brightness characteristic
                    autoBrightnessCharacteristic = service.getCharacteristic(UUID.fromString(AUTO_BRIGHTNESS_CHARACTERISTIC_UUID));
                }
            }

            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                // Handle write operations, e.g., after sending data to Arduino
            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                // Handle incoming data from Arduino
                byte[] data = characteristic.getValue();
                // Process incoming data
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void connectToDeviceByName() {
        // Get the list of bonded (paired) devices
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.isEmpty()) {
            Toast.makeText(this, "No paired devices found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Find the device by name
        BluetoothDevice targetDevice = null;
        for (BluetoothDevice device : pairedDevices) {
            if (DEVICE_NAME.equals(device.getName())) {
                targetDevice = device;
                break;
            }
        }

        if (targetDevice == null) {
            Toast.makeText(this, "Device with name " + DEVICE_NAME + " not found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Attempt to connect to the device
        connectToDevice(targetDevice);
    }

    private void openColorPicker() {
        AmbilWarnaDialog colorPicker = new AmbilWarnaDialog(this, selectedColor, new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                selectedColor = color;
                sendColorToArduino();
                updateUIColors(color); // Update UI with the new color
            }

            @Override
            public void onCancel(AmbilWarnaDialog dialog) {
                // Action cancelled
            }
        });
        colorPicker.show();
    }

    @SuppressLint("MissingPermission")
    private void sendColorToArduino() {
        if (ledCharacteristic != null && bluetoothGatt != null) {
            byte[] color = new byte[]{
                    (byte) ((selectedColor >> 16) & 0xFF),
                    (byte) ((selectedColor >> 8) & 0xFF),
                    (byte) (selectedColor & 0xFF)
            };
            ledCharacteristic.setValue(color);
            bluetoothGatt.writeCharacteristic(ledCharacteristic);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startScanning();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void toggleAutoBrightness() {
        autoBrightnessEnabled = !autoBrightnessEnabled;
        if (autoBrightnessEnabled) {
            sendAutoBrightnessToArduino(true);
        } else {
            sendAutoBrightnessToArduino(false);
        }
        updateAutoBrightnessButton(); // Update button text based on state
    }
    @SuppressLint("MissingPermission")
    private void sendAutoBrightnessToArduino(boolean enabled) {
        if (autoBrightnessCharacteristic != null && bluetoothGatt != null) {
            byte[] autoBrightnessValue = new byte[]{(byte) (enabled ? 1 : 0)};
            autoBrightnessCharacteristic.setValue(autoBrightnessValue);
            bluetoothGatt.writeCharacteristic(autoBrightnessCharacteristic);
        }
    }
    private void updateAutoBrightnessButton() {
        ToggleButton autoBrightnessButton = findViewById(R.id.autoBrightnessButton);
        autoBrightnessButton.setText(autoBrightnessEnabled ? "AUTO (ON)" : "AUTO (OFF)");
    }

    public void onAutoBrightnessButtonClick(View view) {
        toggleAutoBrightness();
    }
    private void updateUIColors(int color) {
        LinearLayout mainLayout = findViewById(R.id.mainLayout);
        updateViewTextColors(mainLayout, color); // Update text colors
    }

    private void updateViewTextColors(View view, int color) {
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View child = viewGroup.getChildAt(i);
                if (child instanceof TextView) {
                    ((TextView) child).setTextColor(color); // Update text color
                } else if (child instanceof ViewGroup) {
                    updateViewTextColors(child, color); // Recursively update text colors
                }
            }
        }
    }
}
