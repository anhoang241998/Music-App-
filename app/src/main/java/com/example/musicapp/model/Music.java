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
    private AudioManager mAudioManager;

    private Music() {

    }

    public static Music getInstance() {
        return instance;
    }

    //Hàm khởi tạo bài hát
    public void initializeMusic(Context context){
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mSong = MediaPlayer.create(context, R.raw.when_night_falls);
        mSong.setLooping(true);
        mSong.seekTo(0);
    }

    //Hàm chơi nhạc
    public void playMusic(){
        mSong.start();
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

    // Hàm check Focus Audio, tắt các bài hát khác khi mình đang play nhạc
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
}
