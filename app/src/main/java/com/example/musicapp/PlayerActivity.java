package com.example.musicapp;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.service.notification.StatusBarNotification;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

import com.example.musicapp.model.Music;
import com.example.musicapp.notification.MusicNotification;
import com.example.musicapp.service.MusicService;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class PlayerActivity extends AppCompatActivity {

    @BindView(R.id.tv_start_time)
    TextView mStartTime;
    @BindView(R.id.tv_end_time)
    TextView mEndTime;
    @BindView(R.id.timeline_seekBar)
    SeekBar mTimelineSeekBar;
    @BindView(R.id.btn_play)
    Button mBtnPlay;
    @BindView(R.id.img)
    CircleImageView mMusicImage;

    private int mSongTotalTime, mRotating = 1;
    private SharedPreferences mAppSettingPrefs;
    private Boolean isNightModeOn;
    private boolean gotFocus;
    private Intent mIntent;
    private Music mMusic;
    private AudioManager mAudioManager;
    ObjectAnimator mAnimator;

    //Gọi lại Broadcast receiver để nhận lại các hành động
    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getExtras().getString("actionName");
            switch (action) {
                case MusicService.ACTION_PLAY:
                    if (mMusic.isSongPlaying()) {
                        mAnimator.pause();
                        mBtnPlay.setBackgroundResource(R.drawable.ic_play);
                    } else {
                        mAnimator.resume();
                        mBtnPlay.setBackgroundResource(R.drawable.ic_pause);
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        mAppSettingPrefs = getSharedPreferences("AppSettingPrefs", 0);
        isNightModeOn = mAppSettingPrefs.getBoolean("NightMode", false);
        setContentView(R.layout.activity_player);
        ButterKnife.bind(this);

        mMusic = Music.getInstance();
        mMusic.initializeMusic(this);
        mSongTotalTime = mMusic.getSongTotalTime();
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        registerReceiver(mBroadcastReceiver, new IntentFilter("TRACKS_TRACKS"));

        //Khởi tạo animation cho imageView
        mAnimator = ObjectAnimator.ofFloat(mMusicImage, View.ROTATION, 0f, 360f);
        mAnimator.setDuration(30000).setRepeatCount(Animation.INFINITE);
        mAnimator.setInterpolator(new LinearInterpolator());

        //hàm check cho darkmode
        if (isNightModeOn) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        mBtnPlay.setOnClickListener(v -> {
            if (!mMusic.isSongPlaying()) {
                gotFocus = requestAudioFocusForMyApp(PlayerActivity.this);
                if (gotFocus) {
                    if (!isServiceRunning(MusicService.class)) {
                        startService();
                        checkNotification();
                        mMusic.playMusic();
                        mBtnPlay.setBackgroundResource(R.drawable.ic_pause);
                        if (mRotating == 1) {
                            mRotating = 2;
                            mAnimator.start();
                        } else {
                            mAnimator.resume();
                        }
                    } else if (isServiceRunning(MusicService.class)) {
                        //Nếu notification đã được tạo thì khi ấn play không cần tạo lại notification nữa.
                        if (checkNotification()) {
                            mMusic.playMusic();
                            mBtnPlay.setBackgroundResource(R.drawable.ic_pause);
                            if (mRotating == 1) {
                                mRotating = 2;
                                mAnimator.start();
                            } else {
                                mAnimator.resume();
                            }
                        } else if (!checkNotification()) {  //Nếu notification đã bị xoá thì khi ấn play cần tạo lại notification.
                            startService();
                            mMusic.playMusic();
                            mBtnPlay.setBackgroundResource(R.drawable.ic_pause);
                            if (mRotating == 1) {
                                mRotating = 2;
                                mAnimator.start();
                            } else {
                                mAnimator.resume();
                            }
                        }
                    }
                }
            } else {
                mMusic.pauseMusic();
                mBtnPlay.setBackgroundResource(R.drawable.ic_play);
                mAnimator.pause();
//                releaseAudioFocusForMyApp(PlayerActivity.this);
            }
        });

        //Hàm set độ dài cho seekbar
        mTimelineSeekBar.setMax(mSongTotalTime);

        /*
        * Hàm cho việc kéo seekbar thì nhạc chạy theo bằng cách tạo ra 1 thread để ngủ đi 1s, trong thời điểm đó sẽ lấy
        * thời gian gán lại cho textView
        */
        mTimelineSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mMusic.seekSongToTime(progress);
                    mTimelineSeekBar.setProgress(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        new Thread(() -> {
            while (mMusic != null) {
                try {
                    Message message = new Message();
                    message.what = mMusic.getSongCurrentTime();
                    handler.sendMessage(message);
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {

                }
            }
        }).start();
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @SuppressLint("SetTextI18n")
        @Override
        public void handleMessage(Message message) {
            int SeekBarPosition = message.what;
            mTimelineSeekBar.setProgress(SeekBarPosition);
            String time = createTimeText(SeekBarPosition);
            mStartTime.setText(time);

            String remainTime = createTimeText(mSongTotalTime - SeekBarPosition);
            mEndTime.setText(remainTime);
        }
    };

    //Hàm tạo ra thời gian cho chữ
    public String createTimeText(int time) {
        String timeText;
        int min = time / 1000 / 60;
        int sec = time / 1000 % 60;
        timeText = min + ":";
        if (sec < 10) timeText += 0;
        timeText += sec;
        return timeText;
    }

    //Hàm handle lại option menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    //hàm cho việc nhấn vào các item bên trong menu
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_item_setting) {
            Intent intent = new Intent(PlayerActivity.this, SettingsActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    //Hàm chạy Music Service
    public void startService() {
        mIntent = new Intent(PlayerActivity.this, MusicService.class);
        ContextCompat.startForegroundService(PlayerActivity.this, mIntent);
    }

    // Hàm check Focus Audio, tắt các bài hát khác khi mình đang play nhạc
    private AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case (AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK):
                    mMusic.setSongVolumeLow();
                    break;
                case (AudioManager.AUDIOFOCUS_LOSS_TRANSIENT):
                    mMusic.stopMusic();
                    break;
                case (AudioManager.AUDIOFOCUS_LOSS):
                    mMusic.pauseMusic();
                    mBtnPlay.setBackgroundResource(R.drawable.ic_play);
                    mAnimator.pause();
                    break;
                case (AudioManager.AUDIOFOCUS_GAIN):
                    mMusic.playMusic();
                    mMusic.setSongVolumeNormal();
                    mBtnPlay.setBackgroundResource(R.drawable.ic_pause);
                    mAnimator.start();
                    break;
                default:
                    break;
            }
        }
    };

    //Hàm xin quyền Focus Audio cho app của mình
    public boolean requestAudioFocusForMyApp(final Context context) {
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        int result = mAudioManager.requestAudioFocus(mOnAudioFocusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    //Hàm xin huỷ quyền Focus Audio cho app của mình
    /* void releaseAudioFocusForMyApp(final Context context) {
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.abandonAudioFocus(mOnAudioFocusChangeListener);
    }*/

    //Hàm check xem service đã được tạo hay chưa
    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    //hàm check notification đã được tạo hay chưa
    private boolean checkNotification() {
        NotificationManager notificationManager = (NotificationManager) getApplication().getSystemService(Context.NOTIFICATION_SERVICE);
        StatusBarNotification[] notifications = new StatusBarNotification[0];
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            notifications = notificationManager.getActiveNotifications();
        }
        for (StatusBarNotification notification : notifications) {
            if (notification.getId() == MusicNotification.PLAYER_SERVICE_NOTIFICATION_ID) {
                return true;
            }
        }
        return false;
    }

}