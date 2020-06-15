package com.example.musicapp.model;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.media.MediaPlayer;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.musicapp.R;

import java.util.ArrayList;
import java.util.List;

public class Music {

    /*
     * Sử dụng Singleton Design Pattern tạo ra object rồi service và activity gọi đến để lấy dữ liệu
     * nhưng do service vẫn còn tồn tại nên instance không bị xoá đi
     */

    private static final Music instance = new Music();
    private MediaPlayer mMediaPlayer;
    private int mSongTotalTime, mSongPlayListsPosition = 0;
    public List<SongPlayList> mSongPlayLists = new ArrayList<>();

    private Music() {
    }

    public static Music getInstance() {
        return instance;
    }

    //Hàm khởi tạo bài hát
    public void initializeMusic(Context context) {
        addSong();
        mMediaPlayer = MediaPlayer.create(context, mSongPlayLists.get(mSongPlayListsPosition).getSongFile());
        mMediaPlayer.seekTo(0);
    }

    //Hàm chơi nhạc cho button play của notification và activity
    public void playMusicActivity(TextView title, TextView author, ImageView songImage) {
        title.setText(mSongPlayLists.get(mSongPlayListsPosition).getSongTitle());
        author.setText(mSongPlayLists.get(mSongPlayListsPosition).getSongAuthor());
        songImage.setImageResource(mSongPlayLists.get(mSongPlayListsPosition).getSongImage());
        mMediaPlayer.start();
    }

    public void playMusic() {
        mMediaPlayer.start();
    }

    //Hàm dừng nhạc
    public void stopMusic() {
        mMediaPlayer.stop();
        mMediaPlayer.release();
    }

    //Hàm tạm thời dừng nhạc
    public void pauseMusic() {
        mMediaPlayer.pause();
    }

    //Hàm nhảy sang nhạc mới
    public void nextSong(Context context, TextView title, TextView author, ImageView songImage, Button buttonPlay, ObjectAnimator animator) {
        mSongPlayListsPosition++;
        if (mSongPlayListsPosition > mSongPlayLists.size() - 1) {
            mSongPlayListsPosition = 0;
        }
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
        }
        mMediaPlayer = MediaPlayer.create(context, mSongPlayLists.get(mSongPlayListsPosition).getSongFile());
        title.setText(mSongPlayLists.get(mSongPlayListsPosition).getSongTitle());
        author.setText(mSongPlayLists.get(mSongPlayListsPosition).getSongAuthor());
        songImage.setImageResource(mSongPlayLists.get(mSongPlayListsPosition).getSongImage());
        buttonPlay.setBackgroundResource(R.drawable.ic_play);
        animator.end();
    }

    //Hàm next song cho notification
    public void nextSongNotification(Context context) {
        mSongPlayListsPosition++;
        if (mSongPlayListsPosition > mSongPlayLists.size() - 1) {
            mSongPlayListsPosition = 0;
        }
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
        }
        mMediaPlayer = MediaPlayer.create(context, mSongPlayLists.get(mSongPlayListsPosition).getSongFile());
    }

    //Hàm trở về sang nhạc cũ
    public void previousSong(Context context, TextView title, TextView author, ImageView songImage, Button buttonPlay, ObjectAnimator animator) {
        mSongPlayListsPosition--;
        if (mSongPlayListsPosition < 0) {
            mSongPlayListsPosition = mSongPlayLists.size() - 1;
        }
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
        }
        mMediaPlayer = MediaPlayer.create(context, mSongPlayLists.get(mSongPlayListsPosition).getSongFile());
        title.setText(mSongPlayLists.get(mSongPlayListsPosition).getSongTitle());
        author.setText(mSongPlayLists.get(mSongPlayListsPosition).getSongAuthor());
        songImage.setImageResource(mSongPlayLists.get(mSongPlayListsPosition).getSongImage());
        buttonPlay.setBackgroundResource(R.drawable.ic_play);
        animator.end();
    }

    //Hàm previous song cho notification
    public void previousSongNotification(Context context) {
        mSongPlayListsPosition++;
        if (mSongPlayListsPosition > mSongPlayLists.size() - 1) {
            mSongPlayListsPosition = 0;
        }
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
        }
        mMediaPlayer = MediaPlayer.create(context, mSongPlayLists.get(mSongPlayListsPosition).getSongFile());
    }

    //Hàm lấy tên bài hát
    public String getSongAuthor() {
        return mSongPlayLists.get(mSongPlayListsPosition).getSongAuthor();
    }

    //hàm lấy tên bài hát
    public String getSongTitle() {
        return mSongPlayLists.get(mSongPlayListsPosition).getSongTitle();
    }

    //Hàm lấy hình ảnh bài hát
    public int getSongImage() {
        return mSongPlayLists.get(mSongPlayListsPosition).getSongImage();
    }

    //Hàm lấy giá trị thời gian tổng của bài hát, giá trị trả về là int
    public int getSongTotalTime(Context context) {
        mMediaPlayer = MediaPlayer.create(context, mSongPlayLists.get(mSongPlayListsPosition).getSongFile());
        mSongTotalTime = mMediaPlayer.getDuration();
        return mSongTotalTime;
    }

    //Hàm lấy giá trị thời gian nhạc đang phát hiện tại
    public int getSongCurrentTime() {
        return mMediaPlayer.getCurrentPosition();
    }

    //Hàm check xem nhạc có đang được phát không
    public boolean isSongPlaying() {
        return mMediaPlayer.isPlaying();
    }

    //Hàm di chuyển bài tới vị trí nào, giá trị trả về là progress
    public void seekSongToTime(int progress) {
        mMediaPlayer.seekTo(progress);
    }

    public void setSongVolumeLow() {
        mMediaPlayer.setVolume(0.2f, 0.2f);
    }

    public void setSongVolumeNormal() {
        mMediaPlayer.setVolume(1f, 1f);
    }

    //Hàm tạo ra danh sách bài hát
    public void addSong() {
        mSongPlayLists.add(new SongPlayList("When night falls (While you were sleeping)", "Eddy Kim", R.drawable.photo, R.raw.when_night_falls));
        mSongPlayLists.add(new SongPlayList("Someone Like You", "Adele", R.drawable.adele, R.raw.someone_like_you));
        mSongPlayLists.add(new SongPlayList("Someone You Loved", "Lewis Capaldi", R.drawable.lewis_capaldi, R.raw.someone_you_loved));
        mSongPlayLists.add(new SongPlayList("Do You Remember?", "Phil Collins", R.drawable.phil_collins, R.raw.do_you_remember));
    }
}
