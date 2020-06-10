package com.example.musicapp;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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

    private MediaPlayer mSong;
    private int mSongTotalTime, mRotating = 1;

    private SharedPreferences mAppSettingPrefs;
    private Boolean isNightModeOn;
    private AudioManager mAudioManager;
    private boolean gotFocus;
    private Intent mIntent;

    private AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case (AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK):
                    mSong.setVolume(0.2f, 0.2f);
                    break;
                case (AudioManager.AUDIOFOCUS_LOSS_TRANSIENT):
                    mSong.stop();
                    break;
                case (AudioManager.AUDIOFOCUS_LOSS):
                    mSong.pause();
                    mBtnPlay.setBackgroundResource(R.drawable.ic_play);
                    break;
                case (AudioManager.AUDIOFOCUS_GAIN):
                    mSong.start();
                    mSong.setVolume(1f, 1f);
                    break;
                default:
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

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mSong = MediaPlayer.create(this, R.raw.when_night_falls);
        mSong.setLooping(true);
        mSong.seekTo(0);
        mSongTotalTime = mSong.getDuration();

        final ObjectAnimator mAnimator = ObjectAnimator.ofFloat(mMusicImage, View.ROTATION, 0f, 360f);
        mAnimator.setDuration(30000).setRepeatCount(Animation.INFINITE);
        mAnimator.setInterpolator(new LinearInterpolator());

        if (isNightModeOn) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        mBtnPlay.setOnClickListener(v -> {
            if (!mSong.isPlaying()) {
                gotFocus = requestAudioFocusForMyApp(PlayerActivity.this);
                if (gotFocus) {
                    startService();
                    mSong.start();
                    mBtnPlay.setBackgroundResource(R.drawable.ic_pause);
                    if (mRotating == 1) {
                        mRotating = 2;
                        mAnimator.start();
                    } else {
                        mAnimator.resume();
                    }
                }
            } else {
                mSong.pause();
                mBtnPlay.setBackgroundResource(R.drawable.ic_play);
                mAnimator.pause();
//                releaseAudioFocusForMyApp(PlayerActivity.this);
            }
        });

        mTimelineSeekBar.setMax(mSongTotalTime);
        mTimelineSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mSong.seekTo(progress);
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
            while (mSong != null) {
                try {
                    Message message = new Message();
                    message.what = mSong.getCurrentPosition();
                    handler.sendMessage(message);
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {

                }
            }
        }).start();


    }

    private boolean requestAudioFocusForMyApp(final Context context) {
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        int result = mAudioManager.requestAudioFocus(mOnAudioFocusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

//    void releaseAudioFocusForMyApp(final Context context) {
//        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
//        mAudioManager.abandonAudioFocus(mOnAudioFocusChangeListener);
//    }


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

    public String createTimeText(int time) {
        String timeText;
        int min = time / 1000 / 60;
        int sec = time / 1000 % 60;
        timeText = min + ":";
        if (sec < 10) timeText += 0;
        timeText += sec;
        return timeText;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_item_setting) {
            Intent intent = new Intent(PlayerActivity.this, SettingsActivity.class);
            intent.putExtra("duration", mSong.getCurrentPosition());
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    public void startService() {
        mIntent = new Intent(PlayerActivity.this, MusicService.class);
        ContextCompat.startForegroundService(PlayerActivity.this, mIntent);
    }

    public void stopService() {
        mIntent = new Intent(PlayerActivity.this, MusicService.class);
        stopService(mIntent);
    }
}