package br.ft.unicamp.v206907.c195743.services;

import android.content.Intent;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class FirebaseMessaging extends FirebaseMessagingService {
    public FirebaseMessaging() {
    }

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        mySendBroadcast();
    }

    private void mySendBroadcast(){
        Intent intent = new Intent();
        intent.setAction("REDIRECTING");
        sendBroadcast(intent);
    }
}
