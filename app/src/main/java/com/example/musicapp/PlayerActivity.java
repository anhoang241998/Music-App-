package com.example.musicapp;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
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

import com.example.musicapp.model.Music;
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

        //Khởi tạo animation cho imageView
        final ObjectAnimator mAnimator = ObjectAnimator.ofFloat(mMusicImage, View.ROTATION, 0f, 360f);
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
                gotFocus = mMusic.requestAudioFocusForMyApp(PlayerActivity.this);
                if (gotFocus) {
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

}