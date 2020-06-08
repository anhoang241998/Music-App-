package com.example.musicapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PlayerActivity extends AppCompatActivity {

    @BindView(R.id.tv_start_time)
    TextView mStartTime;
    @BindView(R.id.tv_end_time)
    TextView mEndTime;
    @BindView(R.id.timeline_seekBar)
    SeekBar mTimelineSeekBar;
    @BindView(R.id.btn_play)
    Button mBtnPlay;

    private MediaPlayer mSong;
    private int mSongTotalTime;

    private SharedPreferences mAppSettingPrefs;
    private Boolean isNightModeOn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAppSettingPrefs = getSharedPreferences("AppSettingPrefs", 0);
        isNightModeOn = mAppSettingPrefs.getBoolean("NightMode", false);
        setContentView(R.layout.activity_player);
        ButterKnife.bind(this);

        mSong = MediaPlayer.create(this, R.raw.when_night_falls);
        mSong.setLooping(true);
        mSong.seekTo(0);
        mSongTotalTime = mSong.getDuration();

        if (isNightModeOn) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }


        mBtnPlay.setOnClickListener(v -> {
            if (!mSong.isPlaying()) {
                mSong.start();
                mBtnPlay.setBackgroundResource(R.drawable.ic_pause);
            } else {
                mSong.pause();
                mBtnPlay.setBackgroundResource(R.drawable.ic_play);
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
        switch (item.getItemId()) {
            case R.id.menu_item_setting:
                Intent intent = new Intent(PlayerActivity.this, SettingsActivity.class);
                intent.putExtra("duration", mSong.getCurrentPosition());
                startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}