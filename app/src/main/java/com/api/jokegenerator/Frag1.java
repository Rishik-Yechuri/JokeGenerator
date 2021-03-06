package com.api.jokegenerator;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import com.api.jokegenerator.JokeScreen;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import javax.sql.DataSource;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Predicate;
import io.reactivex.rxjava3.schedulers.Schedulers;


import static android.content.Context.MODE_PRIVATE;


public class Frag1 extends Fragment implements PopupMenu.OnMenuItemClickListener {
    //boolean backgroundTaskFinished = false;

    //Used for Tasks and RxJava. It is cleared when activity is destroyed
    private CompositeDisposable disposables = new CompositeDisposable();

    //Current Joke saved in here
    JSONObject jsonObject;

    //Declare mAuth which will be used for authentication
    FirebaseAuth mAuth;

    //Declare values for Views
    View view;
    Button generateJokeButton;
    Button downloadButton;
    Button optionsButton;
    TextView jokeQuestionText;
    TextView punchlineText;
    Space spaceForDownload;

    //Only for rxJava
    String type = "";
    boolean jokeSaved = false;
    List<CheckIfJokeSavedTask> tasks = new ArrayList<>();

    //Declare Broadcast receiver things
    BroadcastReceiver _updateJokes;

    //jokeJSON stores current joke
    Task<ListResult> jokes;
    JSONObject jokeJSON = null;

    @Nullable
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.frag1_layout, container, false);
        //Initialize Views
        optionsButton = view.findViewById(R.id.optionbutton);
        generateJokeButton = view.findViewById(R.id.generateJokeButton);
        jokeQuestionText = view.findViewById(R.id.jokeQuestionText);
        punchlineText = view.findViewById(R.id.punchlineText);
        downloadButton = view.findViewById(R.id.downloadButton);
        spaceForDownload = view.findViewById(R.id.spaceForDownload);

        //Set Listeners for Buttons
        generateJokeButton.setOnClickListener(new GenerateJokeListener());
        downloadButton.setOnClickListener(new GenerateJokeListener());
        optionsButton.setOnClickListener(new OpenMenuListener());

        //Initialize mAuth, which is used for authentication
        mAuth = FirebaseAuth.getInstance();

        //context = getActivity();
        setViewWidthInInches(.02,spaceForDownload);
        try {
            //update jokeJSON from shared preferences
            jokeJSON = new JSONObject(Objects.requireNonNull(((Objects.requireNonNull(getActivity()))).getSharedPreferences("_", MODE_PRIVATE).getString("joke", "")));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //if jokeJSON is not null,update the TextViews on screen
        if (jokeJSON != null) {
            try {
                if (jokeJSON.getString("type").equals("single")) {
                    jokeQuestionText.setText(jokeJSON.getString("joke"));
                    punchlineText.setText("");
                } else if (jokeJSON.getString("type").equals("twopart")) {
                    jokeQuestionText.setText(jokeJSON.getString("setup"));
                    punchlineText.setText(jokeJSON.getString("delivery"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            //Otherwise,set joke id to "-1"(indicating there is no joke)
            try {
                jokeJSON = new JSONObject();
                jokeJSON.put("id", "-1");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        //jsonObject is updated to be equal to jokeJSON(there should only be one object to store current joke, but for now there are two objects)
        jsonObject = jokeJSON;
        try {
            checkIfJokeSaved();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //Start a broadcast receiver that receives messages anytime a joke is added or removed
        IntentFilter intentFilter = new IntentFilter("UPDATEJOKE");
        _updateJokes = new GetJokeUpdates();
        getActivity().registerReceiver(_updateJokes, intentFilter);

        return view;
    }

    public void showPopup(View v) {
        Context wrapper = new ContextThemeWrapper(getContext(), R.style.PopupMenu);
        if(MainActivity.currentTheme.equals("light")){
            wrapper = new ContextThemeWrapper(getContext(), R.style.PopupMenuLight);
        }
        PopupMenu popup = new PopupMenu(wrapper, v);
        popup.setOnMenuItemClickListener(this);
        popup.setGravity(Gravity.RIGHT);
        popup.inflate(R.menu.popup_menu);
        popup.show();
    }

    //Gets called when a menu button is clicked
    @Override
    public boolean onMenuItemClick(MenuItem item) {
        //Checks if the settings button is clicked
        if (item.getItemId() == R.id.settings) {
            //Goes to the settings activity
            Intent intent = new Intent(getActivity(), Settings.class);
            startActivity(intent);
            //checks if the logout button is clicked
        }else if(item.getItemId() == R.id.logout){
            FirebaseAuth mAuth;
            mAuth = FirebaseAuth.getInstance();
            mAuth.signOut();
            Intent intent = new Intent(getActivity(), MainActivity.class);
            startActivity(intent);
            getActivity().finish();
        }
        return false;
    }

    //A OnClickListener for Buttons
    class GenerateJokeListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            //If "generate joke" button is pressed, new joke is displayed
            if (v.getId() == R.id.generateJokeButton) {
                getJokeAndDisplay();
            }
            //If download button is pressed,takes action depending on state(joke saved or not)
            else if (v.getId() == R.id.downloadButton) {
                try {
                    if (jokeSaved) {
                        //Asks for confirmation to unsave if the joke is already saved
                        ConfirmToUnSave(getActivity());
                    } else {
                        //Otherwise the joke is downloaded
                        downloadJoke(v);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //Opens a popup when called
    class OpenMenuListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            showPopup(v);
        }
    }

    //Fetches joke from JokeAPI and updates the TextViews
    public void getJokeAndDisplay() {
        //List<CheckIfJokeSavedTask> tasks = new ArrayList<>();

        //A new Task is added(the task is irrelevant and only there so RxJava can run properly)
        tasks.add(new CheckIfJokeSavedTask(false, 208));

        //A observable is created,and tasks are completed
        Observable<CheckIfJokeSavedTask> taskObservable = Observable
                .fromIterable(tasks)
                .subscribeOn(Schedulers.io())
                .filter(new Predicate<CheckIfJokeSavedTask>() {
                    @Override
                    //This is where API is called and joke is updated(joke saved in "jsonObject")
                    public boolean test(CheckIfJokeSavedTask jokeSavedTask) throws Throwable {
                        URL url = null;
                        try {
                            //The URL(how the API is called) is set
                            url = new URL(getContext().getSharedPreferences("_", MODE_PRIVATE).getString("jokeurl", "https://v2.jokeapi.dev/joke/Programming,Miscellaneous,Pun,Spooky,Christmas?blacklistFlags=nsfw,religious,racist,sexist,explicit"));
                            //URL connection setup using URL
                            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                            //InputStream created
                            InputStream inputStream = urlConnection.getInputStream();
                            //BufferedReader created so incoming data can be read
                            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                            //finalJSON is all the information from the server put together.It is a JSON but is saved as a String for now
                            String finalJSON = "";
                            //received is a single line received from the server
                            String received = "";
                            //Continues to add the received message from the server onto "finalJSON"
                            while (received != null) {
                                received = bufferedReader.readLine();
                                finalJSON += received;
                            }
                            //jsonObject is updated
                            jsonObject = new JSONObject(finalJSON);
                            //type refers to whether the joke is a single line or two lines.This is used later to update the Views accordingly
                            type = jsonObject.getString("type");
                        } catch (JSONException | IOException e) {
                            e.printStackTrace();
                        }
                        //checkIfJokeSavedFirebase();
                        return true;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread());

        //Observe taskObservable
        taskObservable.subscribe(new Observer<CheckIfJokeSavedTask>() {
            @Override
            public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {
                //disposable is added to list of disposables, which is cleared when the activity is destroyed
                disposables.add(d);
            }

            @Override
            public void onNext(@io.reactivex.rxjava3.annotations.NonNull CheckIfJokeSavedTask jokeSavedTask) {

            }

            @Override
            public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                Log.d("TAG", "onError: " + e);
            }

            @Override
            public void onComplete() {
                /*Once the joke has been received and updated,TextViews for the joke are updated.
                If the Joke is a single liner only one TextView is updated,otherwise both TextViews are updated.*/
                if (type.equals("single")) {
                    try {
                        //TextViews updated
                        jokeQuestionText.setText(jsonObject.getString("joke"));
                        punchlineText.setText("");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else if (type.equals("twopart")) {
                    try {
                        //TextViews updated
                        jokeQuestionText.setText(jsonObject.getString("setup"));
                        punchlineText.setText(jsonObject.getString("delivery"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    //Checks if the joke is saved using Firebase
                    checkIfJokeSavedFirebase();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //jokeJSON value is updated to jsonObject value
                String jokeJSONTemp = jsonObject.toString();
                try {
                    jokeJSON = new JSONObject(jokeJSONTemp);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                //Joke is added to shared preferences
                getActivity().getSharedPreferences("_", MODE_PRIVATE).edit().putString("joke", jokeJSONTemp).apply();
            }
        });
    }

    //Joke is saved to locally,and to firebase
    public void downloadJoke(View v) throws JSONException {
        //Array of tasks created
        List<ListAllTask> tasks = new ArrayList<>();
        //task added,with jokeJSON as the JSON parameter
        tasks.add(new ListAllTask(false, jokeJSON, -5));
        Observable<ListAllTask> taskObservable = Observable
                .fromIterable(tasks)
                .subscribeOn(Schedulers.io())
                .filter(new Predicate<ListAllTask>() {
                    @Override
                    public boolean test(ListAllTask listAllTask) throws Throwable {
                        //storeJoke is called on the task(stores the joke)
                        listAllTask.storeJoke(getContext());
                        return true;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread());

        //Create an observer
        taskObservable.subscribe(new Observer<ListAllTask>() {
            @Override
            public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {
                //add disposable to list to be cleared when activity is destroyed
                disposables.add(d);
            }

            @Override
            public void onNext(@io.reactivex.rxjava3.annotations.NonNull ListAllTask task) {
                Log.d("TAG", "on next:" + Thread.currentThread().getName());
            }

            @Override
            public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {

            }


            @Override
            public void onComplete() {
                /*Change download icon  to check to indicate the joke is saved.
                Set jokeSaved to true*/
                v.setBackground(ContextCompat.getDrawable(Objects.requireNonNull(getActivity()), R.drawable.checkred));
                jokeSaved = true;
            }
        });
    }

    public void deleteJoke() throws JSONException {
        //Gets the name of the group from the joke that is being deleted
        String groupName = Frag2.updateGroupName(jsonObject.getString("id"), getContext());
        //Updates the groups in Frag2
        Frag2.updateJokeGroups(groupName, jsonObject.getString("id"), Frag2.returnJokeGroups());
        //Updates the sharedpreferences of the groups
        getContext().getSharedPreferences("_", MODE_PRIVATE).edit().putString("groupmap", String.valueOf(Frag2.returnJokeGroups())).apply();
        //Creates a intent
        Intent updategroup = new Intent("UPDATEGROUP");
        //Adds extra with the name of the group to remove
        updategroup.putExtra("grouptoremovefrom",groupName);
        //Creates an arraylist of all the jokes in the current group
        ArrayList<String> jokesToAddToGroup = new ArrayList<>(Arrays.asList(jsonObject.getString("id")));
        updategroup.putExtra("idlistoremovefromgroup",String.valueOf(jokesToAddToGroup));
        //Sends a broadcast
        getContext().sendBroadcast(updategroup);
        //Deletes the joke from the shared preferences
        StoreJokesLocally.deleteJoke(jokeJSON.getString("id"), getActivity());
        final String[] idToken = {""};
        //Stores data to be sent to firebase
        Map<String, Object> data = new HashMap<>();
        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
        mUser.getIdToken(true)
                .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                    public void onComplete(@NonNull Task<GetTokenResult> task) {
                        if (task.isSuccessful()) {
                            //Adds some data to "data"
                            idToken[0] = task.getResult().getToken();
                            data.put("token", idToken[0]);
                            data.put("fcmtoken", MyFirebaseMessagingService.getToken(getActivity()));
                            try {
                                data.put("jokeid", jokeJSON.getString("id"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            //Calls the "deleteJoke" function
                            /*FirebaseFunctions functions = FirebaseFunctions.getInstance();
                            functions.useEmulator("10.0.2.2.", 5001);*/
                             FirebaseFunctions.getInstance()
                            //functions
                                    .getHttpsCallable("deleteJoke")
                                    .call(data)
                                    .continueWith(new Continuation<HttpsCallableResult, String>() {
                                        @Override
                                        public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                                            //Changes the check mark back to a download icon
                                            downloadButton.setBackgroundResource(R.drawable.downloadicon);
                                            //Marks that the joke isn't saved anymore
                                            jokeSaved = false;
                                            return null;
                                        }
                                    });
                            // ...
                        } else {
                        }
                    }
                });
    }
    //Builds a custom dialog
    public void ConfirmToUnSave(Context context) {
        //Creates the builder
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        //Adds text and buttons
        builder.setMessage("Do you want to unsave this joke?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            //Deletes joke when this button is pressed
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                try {
                    deleteJoke();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //Unsave Joke With Firebase
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            //When this button is pressed,the dialog is dismissed
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        //Shows the dialog
        AlertDialog alert = builder.create();
        alert.show();
    }

    //Checks if the joke is saved in firebase
    public void checkIfJokeSavedFirebase() throws JSONException {
        List<CheckIfJokeSavedTask> tasks = new ArrayList<>();
        tasks.add(new CheckIfJokeSavedTask(false, jsonObject.getInt("id")));
        Observable<CheckIfJokeSavedTask> taskObservable = Observable
                .fromIterable(tasks)
                .subscribeOn(Schedulers.io())
                .filter(new Predicate<CheckIfJokeSavedTask>() {
                    @Override
                    public boolean test(CheckIfJokeSavedTask jokeSavedTask) throws Throwable {
                        //Updates jokeSaved to be whatever is returned
                        jokeSaved = jokeSavedTask.checkIfStored();
                        return true;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread());
        taskObservable.subscribe(new Observer<CheckIfJokeSavedTask>() {
            @Override
            public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {
                //Adds the current disposable to a list,which is later destroyed
                disposables.add(d);
            }

            @Override
            public void onNext(@io.reactivex.rxjava3.annotations.NonNull CheckIfJokeSavedTask jokeSavedTask) {

            }

            @Override
            public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
            }

            //Once the task is completed,changes the download button to either a download button or check
            @Override
            public void onComplete() {
                if (jokeSaved) {
                    downloadButton.setBackground(ContextCompat.getDrawable(Objects.requireNonNull(getActivity()), R.drawable.checkred));
                } else {
                    downloadButton.setBackground(ContextCompat.getDrawable(Objects.requireNonNull(getActivity()), R.drawable.downloadicon));
                }
            }
        });
    }

    //Checks if the jokes is saved in sharedpreferences,and sets the download button icon accordingly
    public void checkIfJokeSaved() throws JSONException {
        if (StoreJokesLocally.checkIfJokeSaved(jsonObject.getString("id"), getActivity())) {
            jokeSaved = true;
            downloadButton.setBackground(ContextCompat.getDrawable(Objects.requireNonNull(getActivity()), R.drawable.checkred));
        } else {
            downloadButton.setBackground(ContextCompat.getDrawable(Objects.requireNonNull(getActivity()), R.drawable.downloadicon));
        }
    }

    //Gets called whenever there are new jokes
    public class GetJokeUpdates extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Gets the isntruction
            String instruction = intent.getExtras().getString("instruction");
            //Depending on the instruction,it does different things
            if (instruction.equals("delete")) {
                jokeSaved = false;
                downloadButton.setBackground(ContextCompat.getDrawable(Objects.requireNonNull(getActivity()), R.drawable.downloadicon));
            } else {
                jokeSaved = true;
                downloadButton.setBackground(ContextCompat.getDrawable(Objects.requireNonNull(getActivity()), R.drawable.checkred));
            }
        }
    }

    //Sets the width of a view in inches,used to set certain views in a specific position regardless of screen size
    public void setViewWidthInInches(double inches, View v) {
        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        float mXDpi = metrics.xdpi;
        int twoInches = (int) Math.round(inches*mXDpi);
        v.setLayoutParams(new LinearLayout.LayoutParams(twoInches, ViewGroup.LayoutParams.WRAP_CONTENT));
        v.requestLayout();
    }
    //Gets rid of uneccesary data when destroyed
    @Override
    public void onDestroy() {
        super.onDestroy();
        disposables.clear();
        if(_updateJokes!=null) getActivity().unregisterReceiver(_updateJokes);
    }
}