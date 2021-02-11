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
    //This is the function that other classes call
    public boolean checkIfStored() throws JSONException, InterruptedException {
        //Calls the function which communicates with firebase
        checkIfStoredFirebase();
        //Waits until server side operations are finished
        synchronized (commonObject) {
            commonObject.wait();
        }
        //Returns whether or not the joke is stored
        return isStored;
    }
    //Calls a firebase function to see if the joke is saved
    public void checkIfStoredFirebase() throws JSONException {
        final String[] idToken = {""};
        //Holds data which is then sent to firebase
        Map<String, Object> data = new HashMap<>();
        //Gets the user
        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
        //Wait until the user token is received
        mUser.getIdToken(true)
                .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                    public void onComplete(@NonNull Task<GetTokenResult> task) {
                        if (task.isSuccessful()) {
                            idToken[0] = task.getResult().getToken();
                            //add data to the Hashmap
                            data.put("token", idToken[0]);
                            data.put("jokeid",jokeID);
                            //Call the firebase function "checkIfJokeSaved"
                            FirebaseFunctions.getInstance()
                                    .getHttpsCallable("checkIfJokeSaved")
                                    .call(data)
                                    .continueWith(new Continuation<HttpsCallableResult, String>() {
                                        @Override
                                        public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                                            //Converts the returned data to a JSONObject
                                            HashMap result = (HashMap) task.getResult().getData();
                                            JSONObject res = new JSONObject(result);
                                            //isStoredTemp is "true" if the joke is saved,and "false" otherwise
                                            boolean isStoredTemp = Boolean.parseBoolean(res.getString("jokeStored"));
                                            isStored = isStoredTemp;
                                            //Calls .notify() so the .wait() statement stops waiting
                                            synchronized (commonObject) {
                                                commonObject.notify();
                                            }
                                            return null;
                                        }
                                    });
                            // ...
                        } else {
                            //Calls .notify() so the .wait() statement stops waiting
                            synchronized (commonObject) {
                                commonObject.notify();
                            }
                        }
                    }
                });
    }
}
