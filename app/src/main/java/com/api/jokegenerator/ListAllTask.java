package com.api.jokegenerator;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ListAllTask {
    private boolean isComplete;
    private String id;
    private JSONObject jokeJSON = null;
    private int position;
    private Task<ListResult> holdReturnedJokes;

    public ListAllTask(boolean isComplete, JSONObject jokeJSON,int position) {
        //Initialize local variables
        this.isComplete = isComplete;
        this.jokeJSON = jokeJSON;
        this.position = position;
    }

    public void storeJoke(Context context) throws JSONException {
        //Calls "saveJokeIDFirebase" which saves the joke on firebase
        saveJokeIDFirebase(context);
    }

    //Returns whether the task is completed or not
    public boolean isComplete() {
        return isComplete;
    }

    public Task<ListResult> getJokes() {
        return holdReturnedJokes;
    }

    public void saveJokeIDFirebase(Context context) throws JSONException {
        final String[] idToken = {""};
        //Stores data to be sent to firebase
        Map<String, Object> data = new HashMap<>();
        //Gets the user
        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
        //Gets the token
        mUser.getIdToken(true)
                .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                    public void onComplete(@NonNull Task<GetTokenResult> task) {
                        if (task.isSuccessful()) {
                            //Data is added to the HashMap
                            idToken[0] = task.getResult().getToken();
                            data.put("token", idToken[0]);
                            data.put("fcmtoken", MyFirebaseMessagingService.getToken(context));
                            data.put("jokejson", jokeJSON);
                            data.put("position", String.valueOf(position));
                            try {
                                data.put("jokeid", jokeJSON.getString("id"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            //Saves joke locally
                            try {
                                StoreJokesLocally.saveJoke(jokeJSON, context);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            //Calls "saveJokeID" Firebase function
                            FirebaseFunctions functions = FirebaseFunctions.getInstance();
                            functions.useEmulator("10.0.2.2.", 5001);
                            //FirebaseFunctions.getInstance()
                            functions
                                    .getHttpsCallable("saveJokeID")
                                    .call(data)
                                    .continueWith(new Continuation<HttpsCallableResult, String>() {
                                        @Override
                                        public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                                            //Gets the results and saves them as a JSONObject
                                            HashMap result = (HashMap) task.getResult().getData();
                                            JSONObject res = new JSONObject(result);
                                            return null;
                                        }
                                    });
                            // ...
                        } else {
                            //Logs if task isn't successful
                            Log.d("tokencheck", "task is not successful");
                        }
                    }
                });
    }
}
