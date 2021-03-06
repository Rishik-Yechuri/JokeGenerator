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
        getSharedPreferences("_", MODE_PRIVATE).edit().putString("fb", s).apply();
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // TODO(developer): Handle FCM messages here.
        JSONObject jsonReceived = null;
        Intent updateJokes = null;
        Log.d("firebasesucks","Message Received");
        if(remoteMessage.getData().get("purpose").equals("savejoke") || remoteMessage.getData().get("purpose").equals("deletejoke")){
            try {
                updateJokes = changeJokes(getApplicationContext(),remoteMessage);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        //Send a broadcast if the Intent isn't null
        if(updateJokes != null) {
            sendBroadcast(updateJokes);
        }
    }

    public static Intent changeJokes(Context context,RemoteMessage remoteMessage) throws JSONException {
        Intent updateJokes = null;
        JSONObject jsonReceived = null;
        //Gets the ID from the joke
        String jokeIDToDelete = remoteMessage.getData().get("jokeid");
        //Gets the jokeJSON from Firebase
        updateJokes = new Intent("UPDATEJOKE");
        if(remoteMessage.getData().get("purpose").equals("savejoke")){
            //Saves the joke locally
            jsonReceived = new JSONObject(remoteMessage.getData().get("actualJSON"));
            try {
                StoreJokesLocally.saveJoke((JSONObject) jsonReceived, context);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            updateJokes.putExtra("instruction","save");
            updateJokes.putExtra("joke",String.valueOf(jsonReceived));
            updateJokes.putExtra("position",remoteMessage.getData().get("position"));
        }else if(remoteMessage.getData().get("purpose").equals("deletejoke")){
            //Deletes the joke locally
            try {
                StoreJokesLocally.deleteJoke(jokeIDToDelete,context);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            updateJokes.putExtra("instruction","delete");
            updateJokes.putExtra("id",jokeIDToDelete);

        }
        return updateJokes;
    }
    //Returns the current token
    public static String getToken(Context context) {
        return context.getSharedPreferences("_", MODE_PRIVATE).getString("fb", "empty");
    }
}