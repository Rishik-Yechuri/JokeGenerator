package com.api.jokegenerator;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import static android.content.Context.MODE_PRIVATE;


public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        Log.d("storagenotification","newToken");
        getSharedPreferences("_", MODE_PRIVATE).edit().putString("fb", s).apply();
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // TODO(developer): Handle FCM messages here.
        if (/*remoteMessage.getData().get("purpose").equals("savejoke")*/true) {
            Log.d("storagenotification","Bois we got em");
            Log.d("storagenotification","fakedata:" + remoteMessage.getData().get("type"));
            try {
                StoreJokesLocally.saveJoke((JSONObject) remoteMessage.getData(), getApplicationContext());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public static String getToken(Context context) {
        return context.getSharedPreferences("_", MODE_PRIVATE).getString("fb", "empty");
    }
}