package com.example.redvings.smartlock;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

@RequiresApi(api = Build.VERSION_CODES.O)
public class DeviceAdmin extends android.app.admin.DeviceAdminReceiver {
    @Override
    public void onEnabled(Context context, Intent intent)
    {
        super.onEnabled(context,intent);
        Toast.makeText(context,"Device Admin Enabled",Toast.LENGTH_SHORT).show();
    }
    public void onDisabled(Context context,Intent intent)
    {
        super.onDisabled(context,intent);
        Toast.makeText(context,"Device Admin Disabled",Toast.LENGTH_SHORT).show();
    }
}

