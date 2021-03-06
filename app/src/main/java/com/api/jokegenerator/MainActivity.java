package com.api.jokegenerator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class MainActivity extends AppCompatActivity {
    //Declares buttons and text
    TextView signUpView;
    Button signUpButton;
    EditText editTextEmailAddressLogIn;
    EditText editTextPasswordLogIn;
    //Declares Firebase stuff
    FirebaseAuth mAuth;
    FirebaseUser user;
    //Declares an Intent which is used for the BroadcastReceiver
    Intent updateJokes;
    GroupsUpdated groupsUpdated;
    public static String currentTheme = "dark";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        currentTheme = getApplicationContext().getSharedPreferences("_",MODE_PRIVATE).getString("theme","dark");
        setTheme(currentTheme.equals("dark")?R.style.AppTheme:R.style.AppThemeLight);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Gets the currrent user
        user = FirebaseAuth.getInstance().getCurrentUser();
        //Checks if the user is logged in
        if (user != null) {
            //Goes to the joke screen
            Intent i = new Intent(MainActivity.this, JokeScreen.class);
            startActivity(i);
            try {
                getSavedGroups();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            finish();
        }
        //Initializes text and buttons
        signUpView = findViewById(R.id.logInView);
        signUpView.setOnClickListener(new ViewClicked());
        signUpButton = findViewById(R.id.signUpButton);
        signUpButton.setOnClickListener(new ViewClicked());
        editTextEmailAddressLogIn = findViewById(R.id.editTextEmailAddressLogIn);
        editTextPasswordLogIn = findViewById(R.id.editTextPasswordLogIn);
        //Initializes mAuth
        mAuth = FirebaseAuth.getInstance();
        //mAuth.useEmulator("10.0.2.2", 9099);
        groupsUpdated = new GroupsUpdated(getApplicationContext());
    }

    //A onClickListener for buttons
    class ViewClicked implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            //Checks if user is logging in or signing up
            if (v.getId() == R.id.logInView) {
                Intent intent = new Intent(getApplicationContext(), SignUp.class);
                startActivity(intent);
            } else if (v.getId() == R.id.signUpButton) {
                logIn();
            }
        }
    }
    public void setTheme(String themeName){
        if(themeName.equals("dark")){
            setTheme(R.style.AppTheme);
        }
    }
    public void logIn() {
        //Gets the entered username and password
        String email = editTextEmailAddressLogIn.getText().toString();
        String password = editTextPasswordLogIn.getText().toString();
        //Logs into firebase with the information provided
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Intent intent = new Intent(getApplicationContext(), JokeScreen.class);
                    FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
                    mUser.getIdToken(true)
                            .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                                public void onComplete(@NonNull Task<GetTokenResult> task) {
                                    if (task.isSuccessful()) {
                                        getApplicationContext().getSharedPreferences("_", MODE_PRIVATE).edit().putString("jokeurl", "https://v2.jokeapi.dev/joke/Programming,Miscellaneous,Pun,Spooky,Christmas?blacklistFlags=nsfw,religious,racist,sexist,explicit").apply();
                                        //Subscribes to a topic using the users token
                                        String topicToSubscribe = task.getResult().getToken().split("\\.")[0];
                                        FirebaseMessaging.getInstance().subscribeToTopic(topicToSubscribe);
                                        //Gets all the saved jokes from Firebase
                                        try {
                                            getSavedJokesFirebase();
                                            getSavedGroups();
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    } else {
                                    }
                                }
                            });
                    //Goes to the main screen
                    startActivity(intent);
                    finish();
                } else {
                    //Shows a toast if there is an error
                    Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    //Saves a joke in Firebase
    public void getSavedJokesFirebase() throws JSONException {
        final String[] idToken = {""};
        Map<String, Object> data = new HashMap<>();
        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
        mUser.getIdToken(true)
                .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                    public void onComplete(@NonNull Task<GetTokenResult> task) {
                        if (task.isSuccessful()) {
                            idToken[0] = task.getResult().getToken();
                            //Adds data to send
                            data.put("token", idToken[0]);
                            //Calls "getSavedJokes"
                            /*FirebaseFunctions functions = FirebaseFunctions.getInstance();
                            functions.useEmulator("10.0.2.2.", 5001);*/
                             FirebaseFunctions.getInstance()
                            //functions
                                    .getHttpsCallable("getSavedJokes")
                                    .call(data)
                                    .continueWith(new Continuation<HttpsCallableResult, String>() {
                                        @Override
                                        public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                                            //Gets the results and converts it to a JSONObject
                                            HashMap result = (HashMap) task.getResult().getData();
                                            JSONObject res = new JSONObject(result);
                                            //Converts the returned joke IDs to an array
                                            String returnedIDs = res.getString("value");
                                            String[] jokeIDArray = returnedIDs.split(" ");
                                            //Goes through the array of IDs,and gets each joke from firebase
                                            for (int x = 0; x < jokeIDArray.length; x++) {
                                                //Calls Firebase to get the joke
                                                getJokeFirebase(Integer.parseInt(jokeIDArray[x]));
                                            }
                                            return null;
                                        }
                                    });
                            // ...
                        } else {
                        }
                    }
                });
    }

    //Gets a single joke back from Firebase given an ID
    public void getJokeFirebase(int jokeID) throws JSONException {
        final String[] idToken = {""};
        Map<String, Object> data = new HashMap<>();
        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
        mUser.getIdToken(true)
                .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                    public void onComplete(@NonNull Task<GetTokenResult> task) {
                        if (task.isSuccessful()) {
                            //Adds data to send to Firebase
                            idToken[0] = task.getResult().getToken();
                            data.put("token", idToken[0]);
                            data.put("id", String.valueOf(jokeID));
                            //Calls "returnJoke" firebase function
                            /*FirebaseFunctions functions = FirebaseFunctions.getInstance();
                            functions.useEmulator("10.0.2.2.", 5001);*/
                            FirebaseFunctions.getInstance()
                            //functions
                                    .getHttpsCallable("returnJoke")
                                    .call(data)
                                    .continueWith(new Continuation<HttpsCallableResult, String>() {
                                        @Override
                                        public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                                            //Converts return information to a JSONObject
                                            HashMap result = (HashMap) task.getResult().getData();
                                            JSONObject res = new JSONObject(result);
                                            //finalJoke is the joke in JSON format
                                            JSONObject finalJoke = new JSONObject(res.getString("value"));
                                            //Saves the jokes locally
                                            StoreJokesLocally.saveJoke(finalJoke, getApplicationContext());
                                            //Sends a broadcast with the joke
                                            updateJokes = new Intent("UPDATEJOKE");
                                            updateJokes.putExtra("instruction", "save");
                                            updateJokes.putExtra("joke", String.valueOf(finalJoke));
                                            sendBroadcast(updateJokes);
                                            return null;
                                        }
                                    });
                            // ...
                        } else {
                        }
                    }
                });
    }
    public void getSavedGroups() throws JSONException {
        final String[] idToken = {""};
        Map<String, Object> data = new HashMap<>();
        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
        mUser.getIdToken(true)
                .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                    public void onComplete(@NonNull Task<GetTokenResult> task) {
                        if (task.isSuccessful()) {
                            //Adds data to send to Firebase
                            idToken[0] = task.getResult().getToken();
                            data.put("token", idToken[0]);
                            //Calls "returnSavedGroups" firebase function
                            /*FirebaseFunctions functions = FirebaseFunctions.getInstance();
                            functions.useEmulator("10.0.2.2.", 5001);*/
                            FirebaseFunctions.getInstance()
                            //functions
                                    .getHttpsCallable("returnSavedGroups")
                                    .call(data)
                                    .continueWith(new Continuation<HttpsCallableResult, String>() {
                                        @Override
                                        public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                                            //Converts return information to a JSONObject
                                            HashMap result = (HashMap) task.getResult().getData();
                                            JSONObject res = new JSONObject(result);
                                            String groupMap = res.getString("map");
                                            JSONObject groupMapJSON = new JSONObject(groupMap);
                                            Log.d("mapsbois","groupMap:" + groupMap);
                                            Log.d("mapsbois","res:" + res);
                                            getApplicationContext().getSharedPreferences("_",MODE_PRIVATE).edit().putString("groupmap", String.valueOf(groupMapJSON)).apply();
                                            return null;
                                        }
                                    });
                            // ...
                        } else {
                        }
                    }
                });
    }
}