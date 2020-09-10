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
        Log.d("storagenotification", "newToken");
        getSharedPreferences("_", MODE_PRIVATE).edit().putString("fb", s).apply();
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // TODO(developer): Handle FCM messages here.
        Log.d("amg","firebase got message");
        if (remoteMessage.getData().get("purpose").equals("savejoke")) {
            Log.d("amg","save joke");
            JSONObject jsonReceived = null;
            try {
                jsonReceived = new JSONObject(remoteMessage.getData().get("actualJSON"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                Log.d("woah", "saved jokes:" + "precall");
                StoreJokesLocally.saveJoke((JSONObject) jsonReceived, getApplicationContext());
                Log.d("woah", "postcall");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (remoteMessage.getData().get("purpose").equals("deletejoke")) {
            Log.d("amg","delete joke");
            String jokeIDToDelete = remoteMessage.getData().get("jokeid");
            try {
                StoreJokesLocally.deleteJoke(jokeIDToDelete,getApplicationContext());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Log.d("amg","end of message");
        Log.d("amg","value of jokes:" + getApplicationContext().getSharedPreferences("_",MODE_PRIVATE).getString("localjokes",""));
    }

    public static String getToken(Context context) {
        return context.getSharedPreferences("_", MODE_PRIVATE).getString("fb", "empty");
    }
}