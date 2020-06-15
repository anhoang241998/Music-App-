package com.example.musicapp.service;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
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
import static com.example.musicapp.notification.MusicNotification.PLAYER_SERVICE_NOTIFICATION_ID;

public class MusicService extends Service implements Playable {

    public static final String ACTION_PLAY = "actionPlay";
    public static final String ACTION_CLOSE = "actionClose";
    public static final String ACTION_DISMISS = "actionDismiss";
    public static final String ACTION_NEXT = "actionNext";
    public static final String ACTION_PREVIOUS = "actionPrevious";
    private static MediaSessionCompat mMediaSessionCompat;
    private String mSongTitle;
    private String mSongAuthor;
    private int mSongImage;
    private boolean isActivityAlive = PlayerActivity.getIsActivityActive();

    /*
     * biến đếm để giải quyết vấn đề đầu khi chạy app, do là app khi chạy lần đầu notification chưa set được sang hình pause
     * nhờ việc check service đã được chạy chưa và nếu service đã dc chạy và ở lần đầu thì sẽ tạo ra notification với hình
     * pause tương ứng việc đang phát nhạc
     */
    private int serviceCount = 0;
    Music mMusic;

    @Override
    public void onCreate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mMusic = Music.getInstance();
            registerReceiver(mBroadcastReceiver, new IntentFilter("TRACKS_TRACKS"));
        }
        super.onCreate();
    }

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
                    mMusic.stopMusic();
                    stopSelf();
                    break;
                case PlayerActivity.ACTION_BUTTON_PAUSE:
                    serviceCount++;
                    mMusic.pauseMusic();
                    update();
                    stopForeground(false);
                    break;
                case PlayerActivity.ACTION_BUTTON_PLAY:
                    serviceCount++;
                    mMusic.playMusic();
                    update();
                    break;
                case MusicService.ACTION_DISMISS:
                    stopSelf();
                    mMusic.stopMusic();
                    break;
                case PlayerActivity.ACTION_AUDIO_LOSS:
                case PlayerActivity.ACTION_AUDIO_GAIN:
                    serviceCount++;
                    update();
                    break;
                case MusicService.ACTION_NEXT:
                    serviceCount++;
                    if (isActivityAlive = true) {
                        mMusic.nextSongNotification(MusicService.this);
                    }
                    update();
                    break;
                case MusicService.ACTION_PREVIOUS:
                    serviceCount++;
                    if (isActivityAlive = true)
                        mMusic.previousSongNotification(MusicService.this);
                    update();
                    break;
            }
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        serviceCount = 1;
        mMediaSessionCompat = new MediaSessionCompat(this, "tag");
        Notification notification = getNotificationBuilder().build();
        startForeground(PLAYER_SERVICE_NOTIFICATION_ID, notification);
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
        serviceCount++;
        Notification notification = getNotificationBuilder().build();
        startForeground(PLAYER_SERVICE_NOTIFICATION_ID, notification);
        mMusic.playMusic();
        update();

    }

    //Interface dừng bài hát
    @Override
    public void onPauseMusic() {
        serviceCount++;
        mMusic.pauseMusic();
        update();
        stopForeground(false);
    }

    private NotificationCompat.Builder getNotificationBuilder() {

        mSongAuthor = mMusic.getSongAuthor();
        mSongTitle = mMusic.getSongTitle();
        mSongImage = mMusic.getSongImage();


        //PendingIntent tạo ra activity khi ấn vào notification (xoá stack)
        Intent openActivity = new Intent(this, PlayerActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntentOpenActivity = PendingIntent.getActivity(getApplicationContext(), 0, openActivity, PendingIntent.FLAG_CANCEL_CURRENT);

        //PendingIntent cho nút play
        Intent playIntent = new Intent(this, NotificationActionService.class).setAction(ACTION_PLAY);
        PendingIntent pendingIntentPlay = PendingIntent.getBroadcast(getApplicationContext(), 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        //PendingIntent cho nút next
        Intent nextIntent = new Intent(this, NotificationActionService.class).setAction(ACTION_NEXT);
        PendingIntent pendingIntentNext = PendingIntent.getBroadcast(getApplicationContext(), 0, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent previousIntent = new Intent(this, NotificationActionService.class).setAction(ACTION_PREVIOUS);
        PendingIntent pendingIntentPrevious = PendingIntent.getBroadcast(getApplicationContext(), 0, previousIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        //PendingIntent cho nút previous
        Intent closeNotificationIntent = new Intent(this, NotificationActionService.class).setAction(ACTION_CLOSE);
        PendingIntent closeNotificationPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, closeNotificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        //Intent cho việc swipe để dimiss notification nhưng không xoá đi service
        Intent swipeActionIntent = new Intent(this, NotificationActionService.class).setAction(ACTION_DISMISS);
        PendingIntent swipeActionPendingIntent = PendingIntent.getBroadcast(this, 0, swipeActionIntent, 0);

        //tạo ra style cho notification
        Bitmap icon = BitmapFactory.decodeResource(getApplicationContext().getResources(), mSongImage);

        androidx.media.app.NotificationCompat.MediaStyle style = new androidx.media.app.NotificationCompat.MediaStyle();
        style.setMediaSession(mMediaSessionCompat.getSessionToken());
        style.setShowActionsInCompactView(0, 1, 2, 3);
        style.setShowCancelButton(true); // pre-Lollipop works
        style.setCancelButtonIntent(swipeActionPendingIntent);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(mSongTitle)
                .setContentText(mSongAuthor)
                .setSmallIcon(R.drawable.ic_music)
                .setContentIntent(pendingIntentPlay)
                .setContentIntent(pendingIntentOpenActivity)
                .setStyle(style)
                .setLargeIcon(icon)
                .setAutoCancel(true)
                .setOngoing(false)
                .setDeleteIntent(swipeActionPendingIntent);

        //Giải thuật cho việc notification lần đầu
        if (isServiceRunning(MusicService.class) && serviceCount == 1) {
            builder.addAction(R.drawable.ic_previous, "Previous", pendingIntentPrevious)
                    .addAction(R.drawable.ic_pause, "Stop", pendingIntentPlay)
                    .addAction(R.drawable.ic_next, "Next", pendingIntentNext)
                    .addAction(R.drawable.ic_close, "Close", closeNotificationPendingIntent);
        } else if (isServiceRunning(MusicService.class )&& serviceCount>1) {
            if (mMusic.isSongPlaying()) {
                builder.addAction(R.drawable.ic_previous, "Previous", pendingIntentPrevious)
                        .addAction(R.drawable.ic_pause, "Stop", pendingIntentPlay)
                        .addAction(R.drawable.ic_next, "Next", pendingIntentNext)
                        .addAction(R.drawable.ic_close, "Close", closeNotificationPendingIntent);
            } else if (!mMusic.isSongPlaying()) {
                builder.addAction(R.drawable.ic_previous, "Previous", pendingIntentPrevious)
                        .addAction(R.drawable.ic_play, "Play", pendingIntentPlay)
                        .addAction(R.drawable.ic_next, "Next", pendingIntentNext)
                        .addAction(R.drawable.ic_close, "Close", closeNotificationPendingIntent);
            }
        }
        return builder;
    }

    //Hàm update lại notification khi nhấn nút play, nút pause trên notification
    private void update() {
        Notification Notification = getNotificationBuilder().build();
        // display updated notification
        NotificationManager notificationManager = (NotificationManager) getApplication().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(PLAYER_SERVICE_NOTIFICATION_ID, Notification);
    }

    //Hàm check Service có được chạy chưa
    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


}
