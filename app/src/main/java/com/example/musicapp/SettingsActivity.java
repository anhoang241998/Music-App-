package com.example.musicapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.widget.CompoundButton;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingsActivity extends AppCompatActivity {


    @BindView(R.id.switchNightMode)
    Switch mSwitchNightMode;

    private SharedPreferences mAppSettingPrefs;
    private SharedPreferences.Editor mSharedPrefsEdit;
    private Boolean isNightModeOn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAppSettingPrefs = getSharedPreferences("AppSettingPrefs", 0);
        mSharedPrefsEdit = mAppSettingPrefs.edit();
        isNightModeOn = mAppSettingPrefs.getBoolean("NightMode", false);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);

        if (isNightModeOn) {
            mSwitchNightMode.setChecked(true);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            mSwitchNightMode.setChecked(false);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        }

        mSwitchNightMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    mSharedPrefsEdit.putBoolean("NightMode", true);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    mSharedPrefsEdit.putBoolean("NightMode", false);
                }
                mSharedPrefsEdit.apply();
                finish();
                restartCurrentActivity();
            }
        });
    }

    private void restartCurrentActivity() {
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        startActivity(new Intent(this, SettingsActivity.class));
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportActionBar().setTitle("Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        return super.onCreateOptionsMenu(menu);
    }
}