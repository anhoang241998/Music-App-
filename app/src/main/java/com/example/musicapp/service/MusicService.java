package com.example.musicapp.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.musicapp.PlayerActivity;
import com.example.musicapp.R;
import com.example.musicapp.model.Music;

import static com.example.musicapp.notification.MusicNotification.CHANNEL_ID;

public class MusicService extends Service implements Playable {

    public static final String ACTION_PLAY = "actionPlay";
    public static final String ACTION_CLOSE = "actionClose";

    Music mMusic;
    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getExtras().getString("actionName");
            switch (action) {
                case MusicService.ACTION_PLAY:
                    if (mMusic.isSongPlaying()) {
                        onPauseMusic();
                    } else {
                        onPlayMusic();
                    }
                    break;
                case MusicService.ACTION_CLOSE:
                    stopSelf();
                    break;
            }
        }
    };

    @Override
    public void onCreate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mMusic = Music.getInstance();
            registerReceiver(mBroadcastReceiver, new IntentFilter("TRACKS_TRACKS"));
        }
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //PendingIntent tạo ra activity khi ấn vào notification
        Intent openActivity = new Intent(this, PlayerActivity.class);
        PendingIntent pendingIntentOpenActivity = PendingIntent.getActivity(getApplicationContext(), 0, openActivity, PendingIntent.FLAG_UPDATE_CURRENT);

        //PendingIntent cho nút play
        Intent playIntent = new Intent(this, NotificationActionService.class).setAction(ACTION_PLAY);
        PendingIntent pendingIntentPlay = PendingIntent.getBroadcast(getApplicationContext(), 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        //PendingIntent cho nút close
        Intent closeNotificationIntent = new Intent(this, NotificationActionService.class).setAction(ACTION_CLOSE);
        PendingIntent closeNotificationPendinIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, closeNotificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        //tạo ra style cho notification
        MediaSessionCompat mediaSessionCompat = new MediaSessionCompat(getApplicationContext(), "tag");
        Bitmap icon = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.mipmap.photo);

        //tạo ra notification
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("When Night Falls")
                .setContentText("Eddy Kim")
                .setSmallIcon(R.drawable.ic_music)
                .setContentIntent(pendingIntentPlay)
                .setContentIntent(pendingIntentOpenActivity)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(0, 1, 2, 3)
                        .setMediaSession(mediaSessionCompat.getSessionToken()))
                .addAction(R.drawable.ic_previous, "Previous", null)
                .addAction(R.drawable.ic_play, "Play", pendingIntentPlay)
                .addAction(R.drawable.ic_next, "Next", null)
                .addAction(R.drawable.ic_close, "Close", closeNotificationPendinIntent)
                .setLargeIcon(icon)
                .setAutoCancel(true)
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

    //Interface play bài hát
    @Override
    public void onPlayMusic() {
        mMusic.playMusic();
    }

    //Interface dừng bài hát
    @Override
    public void onPauseMusic() {
        mMusic.pauseMusic();
    }
}
