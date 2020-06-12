package com.example.musicapp.model;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;

import com.example.musicapp.R;

public class Music {

    /*
    * Sử dụng Singleton Design Pattern tạo ra object rồi service và activity gọi đến để lấy dữ liệu
    * nhưng do service vẫn còn tồn tại nên instance không bị xoá đi
    */

    private static final Music instance = new Music();
    private MediaPlayer mSong;
    private int mSongTotalTime;


    private Music() {

    }

    public static Music getInstance() {
        return instance;
    }

    //Hàm khởi tạo bài hát
    public void initializeMusic(Context context){
        mSong = MediaPlayer.create(context, R.raw.when_night_falls);
        mSong.setLooping(true);
        mSong.seekTo(0);
    }

    //Hàm chơi nhạc
    public void playMusic(){
        mSong.start();
    }

    //Hàm dừng nhạc
    public void stopMusic(){
        mSong.stop();
    }

    //Hàm tạm thời dừng nhạc
    public void pauseMusic(){
        mSong.pause();
    }

    //Hàm lấy giá trị thời gian tổng của bài hát, giá trị trả về là int
    public int getSongTotalTime(){
        mSongTotalTime = mSong.getDuration();
        return mSongTotalTime;
    }

    //Hàm lấy giá trị thời gian nhạc đang phát hiện tại
    public int getSongCurrentTime(){
        return mSong.getCurrentPosition();
    }

    //Hàm check xem nhạc có đang được phát không
    public boolean isSongPlaying(){
        return mSong.isPlaying();
    }

    //Hàm di chuyển bài tới vị trí nào, giá trị trả về là progress
    public void seekSongToTime(int progress){
        mSong.seekTo(progress);
    }

    public void setSongVolumeLow(){
        mSong.setVolume(0.2f, 0.2f);
    }
    public void setSongVolumeNormal(){
        mSong.setVolume(1f, 1f);
    }

}
