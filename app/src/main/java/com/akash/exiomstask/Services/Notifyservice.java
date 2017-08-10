package com.akash.exiomstask.Services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.akash.exiomstask.Activities.MainActivity;
import com.akash.exiomstask.Activities.NotificationActivity;
import com.akash.exiomstask.Constants.Constant;
import com.akash.exiomstask.R;

/**
 * Created by akash on 8/9/2017.
 */

public class Notifyservice extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sendNotification();
        return super.onStartCommand(intent, flags, startId);
    }

    private void sendNotification() {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_android_black_24dp)
                        .setContentTitle(Constant.NOTIFICATION_TITLE)
                        .setContentText(Constant.NOTIFICATION_MESSAGE);
        Intent resultIntent = new Intent(this, NotificationActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntent(resultIntent);
        stackBuilder.addParentStack(MainActivity.class);

        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        mBuilder.setSound(alarmSound);
        mBuilder.setContentIntent(resultPendingIntent);
        mBuilder.setAutoCancel(true);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(this.NOTIFICATION_SERVICE);

        mNotificationManager.notify(Constant.NOTIFICATION_ID, mBuilder.build());
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
