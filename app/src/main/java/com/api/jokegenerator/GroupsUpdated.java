package com.api.jokegenerator;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GroupsUpdated {
    //Declare some things
    BroadcastReceiver _updateJokes;
    Context context;

    public GroupsUpdated(Context context) {
        this.context = context;
        //Create a Intent,and listen for broadcasts
        IntentFilter intentFilter = new IntentFilter("UPDATEGROUP");
        _updateJokes = new UpdateServerGroups();
        context.registerReceiver(_updateJokes, intentFilter);
    }

    //When it is called,server side jokes are updated
    public class UpdateServerGroups extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Checks if the instruction is to add a joke to a group
            if (intent.getExtras() != null && intent.getExtras().getString("grouptoaddto") != null) {
                try {
                    addJokeToGroup(intent.getExtras().getString("idlistotaddtogroup"), intent.getExtras().getString("grouptoaddto"), context);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            //Checks if the instruction is to remove a joke from a group
            if(intent.getExtras() != null && intent.getExtras().getString("grouptoremovefrom") != null) {
                try {
                    String idList = intent.getExtras().getString("idlistoremovefromgroup").replace(",","").replace("[","").replace("]","");
                        removeJokeFromGroup(idList, intent.getExtras().getString("grouptoremovefrom"), context);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //Adds a joke to a group in firebase
    public static void addJokeToGroup(String id, String groupName, Context context) throws JSONException {
        final String[] idToken = {""};
        Map<String, Object> data = new HashMap<>();
        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
        mUser.getIdToken(true)
                .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                    public void onComplete(@NonNull Task<GetTokenResult> task) {
                        if (task.isSuccessful()) {
                            idToken[0] = task.getResult().getToken();
                            data.put("token", idToken[0]);
                            data.put("id", id);
                            data.put("groupName", groupName);
                            FirebaseFunctions functions = FirebaseFunctions.getInstance();
                            functions.useEmulator("10.0.2.2.", 5001);
                            //FirebaseFunctions.getInstance()
                            functions
                                    .getHttpsCallable("addJokeToGroup")
                                    .call(data)
                                    .continueWith(new Continuation<HttpsCallableResult, String>() {
                                        @Override
                                        public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                                            return null;
                                        }
                                    });
                            // ...
                        }
                    }
                });
    }

    //Removes a joke from a group in firebase
    public static void removeJokeFromGroup(String id, String groupName, Context context) throws JSONException {
        final String[] idToken = {""};
        Map<String, Object> data = new HashMap<>();
        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
        mUser.getIdToken(true)
                .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                    public void onComplete(@NonNull Task<GetTokenResult> task) {
                        if (task.isSuccessful()) {
                            idToken[0] = task.getResult().getToken();
                            data.put("token", idToken[0]);
                            data.put("id", id);
                            data.put("groupName", groupName);
                            FirebaseFunctions functions = FirebaseFunctions.getInstance();
                            functions.useEmulator("10.0.2.2.", 5001);
                            //FirebaseFunctions.getInstance()
                            functions
                                    .getHttpsCallable("removeJokeFromGroup")
                                    .call(data)
                                    .continueWith(new Continuation<HttpsCallableResult, String>() {
                                        @Override
                                        public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                                            return null;
                                        }
                                    });
                        }
                    }
                });
    }
}
