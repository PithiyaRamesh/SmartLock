package com.smartlock;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;
import android.widget.Toast;
import androidx.core.app.NotificationCompat;

public class SmartLockService extends Service {
    DevicePolicyManager deviceManager;
    ActivityManager activityManager;
    ComponentName compName;
    public static final String CHANNEL_ID = "ForegroundServiceChannel";

    public void onCreate() {
        Toast.makeText(this,"Smart Lock Running In Background",Toast.LENGTH_LONG).show();
        try {
            deviceManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
            activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            compName = new ComponentName(this, DeviceAdmin.class);

            SensorManager mSensor_Manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            if (mSensor_Manager != null) {
                Sensor mProximitySensor = mSensor_Manager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
                if (mProximitySensor == null) {
                    Toast.makeText(this, "Required Hardware Not Found!", Toast.LENGTH_SHORT).show();
                } else {
                    mSensor_Manager.registerListener(mSensorListener, mProximitySensor, SensorManager.SENSOR_DELAY_FASTEST);
                }
            }
        } catch (Exception ex) {
            Toast.makeText(this, "ERROR: "+ex, Toast.LENGTH_LONG).show();
        }
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
            //Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();
            String input = intent.getStringExtra("inputExtra");
            createNotificationChannel();
            Intent notificationIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this,
                    0, notificationIntent, 0);
            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("Smart Lock")
                    .setContentText(input)
                    .setSmallIcon(R.drawable.ic_launcher_background)
                    .setContentIntent(pendingIntent)
                    .build();
            startForeground(1, notification);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
    private final SensorEventListener mSensorListener=new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if(event.values[0]<5) {
                boolean active=deviceManager.isAdminActive(compName);
                if(active) {
                   deviceManager.lockNow();
                }
            }
        }
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    public void onDestroy()
    {
        super.onDestroy();
//        System.exit(0);
    }


    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel Channel = new NotificationChannel(CHANNEL_ID, "Smart Lock running status",NotificationManager.IMPORTANCE_DEFAULT);
                    NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(Channel);
        }
    }
}
