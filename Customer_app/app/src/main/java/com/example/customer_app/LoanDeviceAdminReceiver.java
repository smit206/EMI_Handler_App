package com.example.customer_app;

import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

@RequiresApi(api = Build.VERSION_CODES.FROYO)
public class LoanDeviceAdminReceiver extends DeviceAdminReceiver {

    @Override
    public void onEnabled(Context context, Intent intent) { }

    @Override
    public void onProfileProvisioningComplete(Context context, Intent intent) {
        // Called when QR provisioning sets this app as Device Owner during setup
        applyDoPolicies(context);
        // Start monitor service
        ContextCompat.startForegroundService(
                context, new Intent(context, MonitorService.class));
    }

    static void applyDoPolicies(Context context) {
        DevicePolicyManager dpm =
                (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName admin = new ComponentName(context, LoanDeviceAdminReceiver.class);
        if (dpm != null && dpm.isDeviceOwnerApp(context.getPackageName())) {
            // Examples – tailor to your needs
            dpm.setStatusBarDisabled(admin, true);
            dpm.addUserRestriction(admin, android.os.UserManager.DISALLOW_FACTORY_RESET);
            dpm.addUserRestriction(admin, android.os.UserManager.DISALLOW_DEBUGGING_FEATURES);
            dpm.addUserRestriction(admin, android.os.UserManager.DISALLOW_ADD_USER);
            dpm.addUserRestriction(admin, android.os.UserManager.DISALLOW_MOUNT_PHYSICAL_MEDIA);
            dpm.setLockTaskPackages(admin, new String[]{context.getPackageName()});
        }
    }
}
