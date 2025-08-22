package com.example.customer_app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class MonitorService extends android.app.Service {
    private static final String CH = "loan_admin_ch";
    private static final int ID = 29;

    @Override public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel ch = new NotificationChannel(
                    CH, "Loan Admin Monitor", NotificationManager.IMPORTANCE_LOW);
            NotificationManagerCompat.from(this).createNotificationChannel(ch);
        }
        Notification n = new NotificationCompat.Builder(this, CH)
                .setSmallIcon(android.R.drawable.stat_sys_warning)
                .setContentTitle("Loan Protection Active")
                .setContentText("Monitoring EMI compliance")
                .setOngoing(true).build();
        startForeground(ID, n);
    }

    @Override public int onStartCommand(Intent i, int f, int id) {
        // TODO: Poll server for EMI status and call block/unblock accordingly.
        return START_STICKY;
    }

    @Override public android.os.IBinder onBind(Intent i) { return null; }

    // Helpers to block/unblock
    public static void blockNow(android.content.Context ctx) {
        DevicePolicyManager dpm = (DevicePolicyManager) ctx.getSystemService(DEVICE_POLICY_SERVICE);
        ComponentName admin = new ComponentName(ctx, LoanDeviceAdminReceiver.class);
        if (dpm != null) {
            // Optional: start kiosk
            try { dpm.setLockTaskPackages(admin, new String[]{ctx.getPackageName()}); } catch (Throwable ignored) {}
            // Immediate lock
            dpm.lockNow();
        }
    }
}
