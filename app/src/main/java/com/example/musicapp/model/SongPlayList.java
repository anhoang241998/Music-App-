package com.example.musicapp.model;

public class SongPlayList {
    private String mSongTitle;
    private String mSongAuthor;
    private int mSongImage;
    private int mSongFile;

    public SongPlayList() {
    }


    public SongPlayList(String songTitle, String songAuthor, int songImage, int songFile) {
        mSongTitle = songTitle;
        mSongAuthor = songAuthor;
        mSongImage = songImage;
        mSongFile = songFile;
    }


    public String getSongTitle() {
        return mSongTitle;
    }

    public void setSongTitle(String songTitle) {
        mSongTitle = songTitle;
    }

    public String getSongAuthor() {
        return mSongAuthor;
    }

    public void setSongAuthor(String songAuthor) {
        mSongAuthor = songAuthor;
    }

    public int getSongImage() {
        return mSongImage;
    }

    public void setSongImage(int songImage) {
        mSongImage = songImage;
    }

    public int getSongFile() {
        return mSongFile;
    }

    public void setSongFile(int songFile) {
        mSongFile = songFile;
    }

}
