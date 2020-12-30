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
    private Task<ListResult> holdReturnedJokes;

    public ListAllTask(boolean isComplete,JSONObject jokeJSON) {
        this.isComplete = isComplete;
        //this.id = id;
        this.jokeJSON = jokeJSON;
    }
   public void storeJoke(Context context) throws JSONException {
        saveJokeIDFirebase(context);
   }
    public boolean isComplete() {
        return isComplete;
    }
    public Task<ListResult> getJokes(){
        Log.d("TAG","holdReturnedJokes sent:" + holdReturnedJokes);
        return holdReturnedJokes;
    }
    public void saveJokeIDFirebase(Context context) throws JSONException {
        final String[] idToken = {""};
        Map<String, Object> data = new HashMap<>();
        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
        mUser.getIdToken(true)
                .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                    public void onComplete(@NonNull Task<GetTokenResult> task) {
                        if (task.isSuccessful()) {
                            Log.d("tokencheck","task.getToken:" + task.getResult().getToken());
                            idToken[0] = task.getResult().getToken();
                            Log.d("tokencheck","idToken[0]" + task.getResult().getToken());
                            // Send token to your backend via HTTPS
                            data.put("token", idToken[0]);
                            data.put("fcmtoken",MyFirebaseMessagingService.getToken(context));
                            data.put("jokejson",jokeJSON);
                            Log.d("tokencheck","token value:" + idToken[0]);
                            try {
                                data.put("jokeid",jokeJSON.getString("id"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            try {
                                StoreJokesLocally.saveJoke(jokeJSON,context);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            FirebaseFunctions.getInstance()
                                    .getHttpsCallable("saveJokeID")
                                    .call(data)
                                    .continueWith(new Continuation<HttpsCallableResult, String>() {
                                        @Override
                                        public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                                            HashMap result = (HashMap) task.getResult().getData();
                                            JSONObject res = new JSONObject(result);
                                            //String message = res.getString("gameID");
                                            Log.d("serverresult","gameID: none");
                                            //openShowCode(message);
                                            return null;
                                        }
                                    });
                            // ...
                        } else {
                            Log.d("tokencheck","task is not successful");
                            // Handle error -> task.getException();
                        }
                    }
                });
    }
}
