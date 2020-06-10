package com.example.musicapp.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.musicapp.R;

import static com.example.musicapp.notification.MusicNotification.CHANNEL_ID;

public class MusicService extends Service{

    public static final String ACTION_PLAY = "actionPlay";


    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Intent playIntent = new Intent(this, NotificationActionService.class).setAction(ACTION_PLAY);
        PendingIntent pendingIntentPlay = PendingIntent.getBroadcast(getApplicationContext(), 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        MediaSessionCompat mediaSessionCompat = new MediaSessionCompat(getApplicationContext(), "tag");
        Bitmap icon = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.mipmap.photo);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("When Night Falls")
                .setContentText("Eddy Kim")
                .setSmallIcon(R.drawable.ic_music)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(0, 1, 2)
                        .setMediaSession(mediaSessionCompat.getSessionToken()))
                .addAction(R.drawable.ic_previous, "Previous", null)
                .addAction(R.drawable.ic_play, "Play", pendingIntentPlay)
                .addAction(R.drawable.ic_next, "Next", null)
                .setLargeIcon(icon)
                .setOngoing(false)
                .build();

        startForeground(1, notification);

        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        stopSelf();
    }




}
