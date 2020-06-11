package com.example.musicapp.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationActionService extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        //Send BroadCast tới các activity, service mong muốn với action cần thiết
        context.sendBroadcast(new Intent("TRACKS_TRACKS").putExtra("actionName", intent.getAction()));
    }
}
