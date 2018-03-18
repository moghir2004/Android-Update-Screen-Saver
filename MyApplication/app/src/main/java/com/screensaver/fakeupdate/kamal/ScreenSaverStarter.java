package com.screensaver.fakeupdate.kamal;

import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import static android.content.Intent.ACTION_SCREEN_OFF;

/**
 * Service only for registering and unregistering ScreenSaverStarter.
 */
public class ScreenSaverStarter extends Service {
    private BroadcastReceiver receiver;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d("ScreenSaverStarter", "Service started");

        // Add persistent notification to keep service alive
        Notification.Builder builder = new Notification.Builder(this)
                    .setContentTitle("Update Screen Saver");
        Notification notification = builder.build();
        startForeground(1, notification);

        // Create BroadcastReceiver
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action == null || !action.equals(ACTION_SCREEN_OFF)) return;

                // Start screen saver activity
                Intent activityIntent = new Intent(context, ScreenSaverActivity.class);
                context.startActivity(activityIntent);
            }
        };

        // Register receiver
        IntentFilter filter = new IntentFilter(ACTION_SCREEN_OFF);
        registerReceiver(receiver, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.d("ScreenSaverStarter", "Service destroyed");

        // Unregister receiver
        unregisterReceiver(receiver);
    }
}
