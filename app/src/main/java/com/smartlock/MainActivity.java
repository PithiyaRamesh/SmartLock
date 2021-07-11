package com.smartlock;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "_MainActivity";
    Button btnOnOff;

    static final int RESULT_ENABLE=1;
    DevicePolicyManager deviceManager;
    ActivityManager activityManager;
    ComponentName compName;
    boolean isAdminEnabled;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            btnOnOff = findViewById(R.id.btn_main_onOff);
            SmartLockService smartLockService = new SmartLockService();
            if (isServiceStopped(smartLockService.getClass())) {
                btnOnOff.setText(R.string.turn_on);
            }
            deviceManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
            activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            compName = new ComponentName(this, DeviceAdmin.class);
            isAdminEnabled = deviceManager.isAdminActive(compName); //true if DeviceAdmin permission was granted to app
            btnOnOff.setOnClickListener(view -> {
                if(isAdminEnabled){
                    turnOnOffService();
                }
                else {
                    requestAdminPermission();
                }
            });
        } catch (Exception ex) {
                Log.e(TAG, ex.toString());
                Toast.makeText(getApplicationContext(),"ERROR: "+ex,Toast.LENGTH_LONG).show();
        }
    }

    private void requestAdminPermission() {
        AlertDialog alertDialog;
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setMessage("We Need admin permission to Lock device");
        adb.setNegativeButton("Exit", (dialogInterface, i) -> finishAffinity());
        adb.setPositiveButton("Settings", (dialogInterface, i) -> {
            Intent intentAdmin = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intentAdmin.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, compName);
            intentAdmin.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "We Need Admin Permission To Lock The Device.");
            startActivityForResult(intentAdmin, RESULT_ENABLE);
            isAdminEnabled = deviceManager.isAdminActive(compName);
            if (isAdminEnabled) {
                turnOnOffService();
            }
        });
        alertDialog = adb.create();
        alertDialog.show();
    }

    public void turnOnOffService() {
        try {
            SmartLockService smartLockService = new SmartLockService();
            Intent intentService = new Intent(this, smartLockService.getClass());
                if (isServiceStopped(smartLockService.getClass())) {  //If service is stopped then start it
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(intentService);  //Start SmartLockService
                    }
                    else{
                        startService(intentService);
                    }
                    btnOnOff.setText(R.string.turn_off);
//                    btnOnOff.setBackgroundColor(getResources().getColor(R.color.colorGreen));
                }else {
                    try {
                        stopService(intentService);//stop SmartLockService
                        //Make sure service is stopped
                        if (isServiceStopped(smartLockService.getClass())) {
                            Toast.makeText(getApplicationContext(), "Smart Lock Disabled", Toast.LENGTH_SHORT).show();
                            btnOnOff.setText(R.string.turn_on);  //changing text to On
                            //btnOnOff.setBackgroundColor(getResources().getColor(R.color.colorGreen));
                        } else {
                            Toast.makeText(getApplicationContext(), "Something went wrong!", Toast.LENGTH_SHORT).show();
                        }
                    }
                    catch(Exception ex){
                        Log.e(TAG, "Exception: "+ex);
                    }
                }
        }
        catch (Exception ex) {
            Log.e(TAG,"ERROR:- "+ex);
            Toast.makeText(getApplicationContext(), "ERROR: " + ex, Toast.LENGTH_LONG).show();
        }
    }

    private boolean isServiceStopped(Class<?> serviceClass) {
            ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (serviceClass.getName().equals(service.service.getClassName())) {
                    return false;
                }
            }
        return true;
    }

}
