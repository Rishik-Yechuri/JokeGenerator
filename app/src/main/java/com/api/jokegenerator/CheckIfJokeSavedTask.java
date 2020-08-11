package com.api.jokegenerator;

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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class CheckIfJokeSavedTask {
    private boolean isComplete;
    private boolean isStored;
    private int jokeID;
    final String commonObject = "taskobject";
    public CheckIfJokeSavedTask(boolean isComplete, int jokeID) {
        this.isComplete = isComplete;
        this.jokeID = jokeID;
    }

    public boolean checkIfStored() throws JSONException, InterruptedException {
        Log.d("somethingbrokedebug","In check if stored");
        checkIfStoredFirebase();
        synchronized (commonObject) {
            commonObject.wait();
        }
        return isStored;
    }

    public void checkIfStoredFirebase() throws JSONException {
        final String[] idToken = {""};
        Map<String, Object> data = new HashMap<>();
        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
        mUser.getIdToken(true)
                .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                    public void onComplete(@NonNull Task<GetTokenResult> task) {
                        if (task.isSuccessful()) {
                            idToken[0] = task.getResult().getToken();
                            data.put("token", idToken[0]);
                            data.put("jokeid",jokeID);
                            FirebaseFunctions.getInstance()
                                    .getHttpsCallable("checkIfJokeSaved")
                                    .call(data)
                                    .continueWith(new Continuation<HttpsCallableResult, String>() {
                                        @Override
                                        public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                                            HashMap result = (HashMap) task.getResult().getData();
                                            JSONObject res = new JSONObject(result);
                                            boolean isStoredTemp = Boolean.parseBoolean(res.getString("jokeStored"));
                                            Log.d("finalthing","isStoredTemp:" + isStoredTemp);
                                            isStored = isStoredTemp;
                                            Log.d("somethingbrokedebug","isStored:" + isStored);
                                            synchronized (commonObject) {
                                                commonObject.notify();
                                            }
                                            return null;
                                        }
                                    });
                            // ...
                        } else {
                            Log.d("tokencheck", "task is not successful");
                            synchronized (commonObject) {
                                commonObject.notify();
                            }
                        }
                    }
                });
        Log.d("checkerdebug","pre notify");
    }
}
