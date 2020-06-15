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
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.palette.graphics.Palette;

import com.example.musicapp.model.Music;
import com.example.musicapp.notification.MusicNotification;
import com.example.musicapp.service.MusicService;
import com.example.musicapp.service.NotificationActionService;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

import static com.example.musicapp.service.MusicService.ACTION_NEXT;
import static com.example.musicapp.service.MusicService.ACTION_PLAY;
import static com.example.musicapp.service.MusicService.ACTION_PREVIOUS;

public class PlayerActivity extends AppCompatActivity {

    public static final String ACTION_BUTTON_PAUSE = "actionButtonPause";
    public static final String ACTION_BUTTON_PLAY = "actionButtonPlay";
    public static final String ACTION_AUDIO_LOSS = "actionAudioLoss";
    public static final String ACTION_AUDIO_GAIN = "actionAudioGain";

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
    TextView mTitle;
    @BindView(R.id.tv_author)
    TextView mAuthor;
    @BindView(R.id.btn_next)
    Button mBtnNext;
    @BindView(R.id.btn_previous)
    Button mBtnPrevious;
    @BindView(R.id.relativeLayoutBackground)
    RelativeLayout mLayoutBackground;


    private int mSongTotalTime, mRotating = 1;
    private SharedPreferences mAppSettingPrefs;
    private Boolean isNightModeOn;
    private boolean gotFocus;

    public static boolean getIsActivityActive() {
        return isActivityActive;
    }

    private static boolean isActivityActive;
    private Intent mIntent;
    private Intent mPlayableIntent;
    private Music mMusic;
    private AudioManager mAudioManager;
    private Palette.Swatch mDarkMutedSwatch;
    private GradientDrawable mGradientDrawable;
    private Bitmap mBitmap;
    private Animation mSlideAnim;
    ObjectAnimator mAnimator;


    //Gọi lại Broadcast receiver để nhận lại các hành động
    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getExtras().getString("actionName");
            switch (action) {
                case ACTION_PLAY:
                    requestAudioFocusForMyApp(PlayerActivity.this);
                    if (mMusic.isSongPlaying()) {
                        mAnimator.pause();
                        mBtnPlay.setBackgroundResource(R.drawable.ic_play);
                    } else {
                        mAnimator.resume();
                        mBtnPlay.setBackgroundResource(R.drawable.ic_pause);
                    }
                    break;
                case ACTION_NEXT:
                    if (isActivityActive = false) {
                        mMusic.nextSong(PlayerActivity.this, mTitle, mAuthor, mMusicImage, mBtnPlay, mAnimator);
                        createAnimationAndColorForNextButton();
                    }
                    break;
                case ACTION_PREVIOUS:
                    if (isActivityActive = false) {
                        mMusic.previousSong(PlayerActivity.this, mTitle, mAuthor, mMusicImage, mBtnPlay, mAnimator);
                        createAnimationAndColorForPreviousButton();
                    }
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        mAppSettingPrefs = getSharedPreferences("AppSettingPrefs", 0);
        isNightModeOn = mAppSettingPrefs.getBoolean("NightMode", false);
        setContentView(R.layout.activity_player);
        ButterKnife.bind(this);

        mMusic = Music.getInstance();
        mMusic.initializeMusic(this);
        mSongTotalTime = mMusic.getSongTotalTime(PlayerActivity.this);
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mPlayableIntent = new Intent(PlayerActivity.this, NotificationActionService.class);
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

        mBitmap = ((BitmapDrawable) mMusicImage.getDrawable()).getBitmap();
        Palette.from(mBitmap).generate(palette -> {
            if (palette != null) {
                mDarkMutedSwatch = palette.getDarkMutedSwatch();
                if (mDarkMutedSwatch != null) {
                    mGradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{mDarkMutedSwatch.getRgb(), 0xE6353535});
                    mLayoutBackground.setBackground(mGradientDrawable);
                }
            } else mLayoutBackground.setBackgroundColor(Color.WHITE);
        });

        //Nút play
        mBtnPlay.setOnClickListener(v -> {
            if (!mMusic.isSongPlaying()) {
                gotFocus = requestAudioFocusForMyApp(PlayerActivity.this);
                if (gotFocus) {
                    if (!isServiceRunning(MusicService.class)) {
                        startService();
                        mBtnPlay.setBackgroundResource(R.drawable.ic_pause);
                        if (mRotating == 1) {
                            mRotating = 2;
                            mAnimator.start();
                        } else {
                            mAnimator.resume();
                        }
                        mMusic.playMusicActivity(mTitle, mAuthor, mMusicImage);
                    } else if (isServiceRunning(MusicService.class)) {
                        //Nếu notification đã được tạo thì khi ấn play không cần tạo lại notification nữa.
                        if (checkNotification()) {
                            mPlayableIntent.setAction(ACTION_BUTTON_PLAY);
                            sendBroadcast(mPlayableIntent);
                            mBtnPlay.setBackgroundResource(R.drawable.ic_pause);
                            if (mRotating == 1) {
                                mRotating = 2;
                                mAnimator.start();
                            } else {
                                mAnimator.resume();
                            }
                        } else if (!checkNotification()) {  //Nếu notification đã bị xoá thì khi ấn play cần tạo lại notification.
                            startService();
                            mMusic.playMusicActivity(mTitle, mAuthor, mMusicImage);
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
                mPlayableIntent.setAction(ACTION_BUTTON_PAUSE); // nút pause
                sendBroadcast(mPlayableIntent);
                if (checkNotification()) {
                    mBtnPlay.setBackgroundResource(R.drawable.ic_play);
                    mAnimator.pause();
//                releaseAudioFocusForMyApp(PlayerActivity.this);
                } else if (!checkNotification() && isServiceRunning(MusicService.class)) {
                    stopService(mIntent);
                }
            }
        });

        // nút next
        mBtnNext.setOnClickListener(v -> {
            mRotating = 1;
            mMusic.nextSong(PlayerActivity.this, mTitle, mAuthor, mMusicImage, mBtnPlay, mAnimator);
            if (checkNotification()) {
                mPlayableIntent.setAction(ACTION_NEXT);
                sendBroadcast(mPlayableIntent);
            }
            createAnimationAndColorForNextButton();
        });

//        Nút previous
        mBtnPrevious.setOnClickListener(v -> {
            mRotating = 1;
            mMusic.previousSong(PlayerActivity.this, mTitle, mAuthor, mMusicImage, mBtnPlay, mAnimator);
            if (checkNotification()) {
                mPlayableIntent.setAction(ACTION_PREVIOUS);
                sendBroadcast(mPlayableIntent);
            }
            createAnimationAndColorForPreviousButton();
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

        mTitle = findViewById(R.id.tv_music_title);
        mTitle.setSelected(true);

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
                    mPlayableIntent.setAction(ACTION_AUDIO_LOSS);
                    sendBroadcast(mPlayableIntent);
                    mBtnPlay.setBackgroundResource(R.drawable.ic_play);
                    mAnimator.pause();
                    break;
                case (AudioManager.AUDIOFOCUS_GAIN):
                    mMusic.playMusic();
                    mMusic.setSongVolumeNormal();
                    mPlayableIntent.setAction(ACTION_AUDIO_GAIN);
                    sendBroadcast(mPlayableIntent);
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            notifications = notificationManager.getActiveNotifications();
        }
        for (StatusBarNotification notification : notifications) {
            if (notification.getId() == MusicNotification.PLAYER_SERVICE_NOTIFICATION_ID) {
                return true;
            }
        }
        return false;
    }

    private void createAnimationAndColorForNextButton(){
        //tạo ra animation cho hình
        mSlideAnim = AnimationUtils.loadAnimation(PlayerActivity.this, android.R.anim.slide_in_left);
        mMusicImage.startAnimation(mSlideAnim);
        //Dùng Palette Api để tách màu trong hình ra và tạo Gradient Background để gán vô
        mBitmap = ((BitmapDrawable) mMusicImage.getDrawable()).getBitmap();
        Palette.from(mBitmap).generate(palette -> {
            if (palette != null) {
                mDarkMutedSwatch = palette.getDarkMutedSwatch();
                if (mDarkMutedSwatch != null) {
                    mGradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{mDarkMutedSwatch.getRgb(), 0xE6353535});
                    mLayoutBackground.setBackground(mGradientDrawable);
                }
            } else mLayoutBackground.setBackgroundColor(Color.WHITE);
        });
        mSongTotalTime = mMusic.getSongTotalTime(PlayerActivity.this);
    }

    private void createAnimationAndColorForPreviousButton(){
        //tạo ra animation cho hình
        mSlideAnim = AnimationUtils.loadAnimation(PlayerActivity.this, R.anim.slide_in_right);
        mMusicImage.startAnimation(mSlideAnim);
        //Dùng Palette Api để tách màu trong hình ra và tạo Gradient Background để gán vô
        mBitmap = ((BitmapDrawable) mMusicImage.getDrawable()).getBitmap();
        Palette.from(mBitmap).generate(palette -> {
            if (palette != null) {
                mDarkMutedSwatch = palette.getDarkMutedSwatch();
                if (mDarkMutedSwatch != null) {
                    mGradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{mDarkMutedSwatch.getRgb(), 0xE6353535});
                    mLayoutBackground.setBackground(mGradientDrawable);
                }
            } else mLayoutBackground.setBackgroundColor(Color.WHITE);
        });
        mSongTotalTime = mMusic.getSongTotalTime(PlayerActivity.this);
    }

    @Override
    protected void onStart() {
        isActivityActive = false;
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        isActivityActive = true;
        super.onDestroy();
    }
}
