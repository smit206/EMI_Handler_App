package com.example.customer_app;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {
    private DevicePolicyManager dpm;
    private ComponentName admin;

    @SuppressLint("MissingInflatedId")
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dpm = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        admin = new ComponentName(this, LoanDeviceAdminReceiver.class);

        Button btnBlock = findViewById(R.id.btnBlock);
        Button btnUnblock = findViewById(R.id.btnUnblock);
        Button btnAdminPin = findViewById(R.id.btnAdminPin);
        TextView tv = findViewById(R.id.tvStatus);

        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, new ComponentName(this, LoanDeviceAdminReceiver.class));
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Required to enforce EMI/device policies.");
        startActivityForResult(intent, 1001);

        btnBlock.setOnClickListener(v -> {
            MonitorService.blockNow(this);
            tv.setText("Blocked (EMI unpaid)");
        });

        btnUnblock.setOnClickListener(v -> {
            // Example: stop kiosk if you started it and relax restrictions as needed
            tv.setText("Unblocked (EMI paid)");
        });

        btnAdminPin.setOnClickListener(v ->
                startActivity(new Intent(this, AdminPinActivity.class)));
    }

    @Override protected void onResume() {
        super.onResume();
        // If already DO after QR provisioning, ensure policies and service are active
        if (Build.VERSION.SDK_INT >= 21 && dpm.isDeviceOwnerApp(getPackageName())) {
            LoanDeviceAdminReceiver.applyDoPolicies(this);
            ContextCompat.startForegroundService(this, new Intent(this, MonitorService.class));
            if (!new AdminPinStore(this).hasPin()) {
                startActivity(new Intent(this, AdminPinActivity.class));
            }
        }
    }
}