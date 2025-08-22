package com.example.customer_app;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class AdminPinActivity extends AppCompatActivity {
    private AdminPinStore store;
    private DevicePolicyManager dpm;
    private ComponentName admin;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_pin);
        store = new AdminPinStore(this);
        dpm = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        admin = new ComponentName(this, LoanDeviceAdminReceiver.class);

        EditText et1 = findViewById(R.id.etPin);
        EditText et2 = findViewById(R.id.etPin2);
        Button bSave = findViewById(R.id.btnSavePin);
        Button bAllowUninstall = findViewById(R.id.btnAllowUninstall);

        bSave.setOnClickListener(v -> {
            String p1 = et1.getText().toString().trim();
            String p2 = et2.getText().toString().trim();
            if (p1.length() < 6) { toast("PIN must be at least 6 digits"); return; }
            if (!p1.equals(p2)) { toast("PIN mismatch"); return; }
            store.setPin(p1); toast("Admin PIN saved");
        });

        bAllowUninstall.setOnClickListener(v -> {
            EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
            new AlertDialog.Builder(this)
                    .setTitle("Enter Admin PIN")
                    .setView(input)
                    .setPositiveButton("Confirm", (d, w) -> {
                        String pin = input.getText().toString().trim();
                        if (!store.verify(pin)) { toast("Invalid PIN"); return; }
                        allowUninstall();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void allowUninstall() {
        boolean isDo = Build.VERSION.SDK_INT >= 21 && dpm.isDeviceOwnerApp(getPackageName());
        if (isDo) {
            // DO role cannot be dropped silently; typical path is factory reset or ownership transfer.
            // We still remove admin and open uninstall to guide the operator.
        }
        if (dpm.isAdminActive(admin)) dpm.removeActiveAdmin(admin);
        Intent i = new Intent(Intent.ACTION_DELETE, Uri.parse("package:" + getPackageName()));
        startActivity(i);
    }

    private void toast(String s) { Toast.makeText(this, s, Toast.LENGTH_SHORT).show(); }
}