package com.api.jokegenerator;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

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


public class Frag1 extends Fragment {
    //GitHub Test
    boolean backgroundTaskFinished = false;
    private CompositeDisposable disposables = new CompositeDisposable();
    JSONObject jsonObject;
    Button generateJokeButton;
    Button downloadButton;
    TextView jokeQuestionText;
    TextView punchlineText;
    FirebaseAuth mAuth;
    int endNumber;


    //Only for rxJava
    String type = "";
    boolean jokeSaved = false;
    List<CheckIfJokeSavedTask> tasks = new ArrayList<>();
    //


    //Context context;
    ArrayList<String> playerNames;
    ArrayList<Button> holdButtons;
    ArrayList<Button> holdButtons2;
    Task<ListResult> jokes;
    JSONObject jokeJSON = null;
    boolean isJokeSaved;

    @Nullable
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag1_layout, container, false);
        generateJokeButton = view.findViewById(R.id.generateJokeButton);
        generateJokeButton.setOnClickListener(new GenerateJokeListener());
        jokeQuestionText = view.findViewById(R.id.jokeQuestionText);
        punchlineText = view.findViewById(R.id.punchlineText);
        downloadButton = view.findViewById(R.id.downloadButton);
        downloadButton.setOnClickListener(new GenerateJokeListener());
        mAuth = FirebaseAuth.getInstance();
        //context = getActivity();
        try {
            jokeJSON = new JSONObject(Objects.requireNonNull(((Objects.requireNonNull(getActivity()))).getSharedPreferences("_", MODE_PRIVATE).getString("joke", "")));
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
        }else{
            try {
                jokeJSON = new JSONObject();
                jokeJSON.put("id","-1");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        jsonObject = jokeJSON;
        try {
            checkIfJokeSavedFirebase();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        /*jokeQuestionText.setText(getActivity().getSharedPreferences("_", MODE_PRIVATE).getString("setup", "No Joke Yet"));
        punchlineText.setText(getActivity().getSharedPreferences("_", MODE_PRIVATE).getString("delivery", ""));*/
        return view;
    }

    class GenerateJokeListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.generateJokeButton) {
                getJokeAndDisplay();
            } else if (v.getId() == R.id.downloadButton) {
                try {
                    downloadJoke(v);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void getJokeAndDisplay() {
        //List<CheckIfJokeSavedTask> tasks = new ArrayList<>();
        tasks.add(new CheckIfJokeSavedTask(false, 208));
        Observable<CheckIfJokeSavedTask> taskObservable = Observable
                .fromIterable(tasks)
                .subscribeOn(Schedulers.io())
                .filter(new Predicate<CheckIfJokeSavedTask>() {
                    @Override
                    public boolean test(CheckIfJokeSavedTask jokeSavedTask) throws Throwable {
                        Log.d("rxjavaflow", "Pre API call");
                        URL url = null;
                        try {
                            url = new URL("https://sv443.net/jokeapi/v2/joke/Any");
                            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                            InputStream inputStream = urlConnection.getInputStream();
                            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                            String finalJSON = "";
                            String received = "";
                            while (received != null) {
                                received = bufferedReader.readLine();
                                finalJSON += received;
                            }

                            Log.d("broke", "received:" + finalJSON);
                            jsonObject = new JSONObject(finalJSON);
                            type = jsonObject.getString("type");
                        } catch (JSONException | IOException e) {
                            e.printStackTrace();
                        }
                        //checkIfJokeSavedFirebase();
                        return true;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread());
        taskObservable.subscribe(new Observer<CheckIfJokeSavedTask>() {
            @Override
            public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {
                Log.d("TAG", "on subscribe called");
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
                Log.d("rxjavaflow", "on Complete");
                if (type.equals("single")) {
                    try {
                        jokeQuestionText.setText(jsonObject.getString("joke"));
                        punchlineText.setText("");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else if (type.equals("twopart")) {
                    try {
                        jokeQuestionText.setText(jsonObject.getString("setup"));
                        punchlineText.setText(jsonObject.getString("delivery"));
                        //activity.getActivity().getSharedPreferences("_", MODE_PRIVATE).edit().putString("setup", jsonObject.getString("setup")).apply();
                        //activity.getActivity().getSharedPreferences("_", MODE_PRIVATE).edit().putString("delivery", jsonObject.getString("delivery")).apply();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    Log.d("somethingbrokedebug","pre check");
                    checkIfJokeSavedFirebase();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String jokeJSONTemp = jsonObject.toString();
                try {
                    jokeJSON = new JSONObject(jokeJSONTemp);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                getActivity().getSharedPreferences("_", MODE_PRIVATE).edit().putString("joke", jokeJSONTemp).apply();
            }
        });
    }

    public void downloadJoke(View v) throws JSONException {
        //v.setBackground(ContextCompat.getDrawable(Objects.requireNonNull(getActivity()),R.drawable.checkred));
        String tempJoke = "Why did the chicken cross the road? I don't even know";
        StorageReference whereToSaveJoke = FirebaseStorage.getInstance().getReference().child("storedjokes/" + jokeJSON.getString("id") + ".json");
        /*whereToSaveJoke.putFile(Uri.parse(String.valueOf(jsonObject)))*/
        whereToSaveJoke.putBytes(tempJoke.getBytes()).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(getActivity(), "Joke Added", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity(), "Joke Failed:" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        List<ListAllTask> tasks = new ArrayList<>();
        tasks.add(new ListAllTask(false, jokeJSON));
        Observable<ListAllTask> taskObservable = Observable
                .fromIterable(tasks)
                .subscribeOn(Schedulers.io())
                .filter(new Predicate<ListAllTask>() {
                    @Override
                    public boolean test(ListAllTask listAllTask) throws Throwable {
                        listAllTask.storeJoke();
                        return true;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread());
        taskObservable.subscribe(new Observer<ListAllTask>() {
            @Override
            public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {
                Log.d("TAG", "on subscribe called");
                disposables.add(d);
            }

            @Override
            public void onNext(@io.reactivex.rxjava3.annotations.NonNull ListAllTask task) {
                Log.d("TAG", "on next:" + Thread.currentThread().getName());
                //Log.d("TAG", "onNext:" + task.getJokes());
                //task.setList();
                //jokes = task.getJokes();
            }

            @Override
            public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                Log.d("TAG", "onError: " + e);
            }

            @Override
            public void onComplete() {
                Log.d("TAG", "onComplete");
                v.setBackground(ContextCompat.getDrawable(Objects.requireNonNull(getActivity()), R.drawable.checkred));
            }
        });
    }
    public void checkIfJokeSavedFirebase() throws JSONException {
        List<CheckIfJokeSavedTask> tasks = new ArrayList<>();
        tasks.add(new CheckIfJokeSavedTask(false, jsonObject.getInt("id")));
        Log.d("somethingbrokedebug","pre Observable");
        Observable<CheckIfJokeSavedTask> taskObservable = Observable
                .fromIterable(tasks)
                .subscribeOn(Schedulers.io())
                .filter(new Predicate<CheckIfJokeSavedTask>() {
                    @Override
                    public boolean test(CheckIfJokeSavedTask jokeSavedTask) throws Throwable {
                        Log.d("somethingbrokedebug","In test");
                        jokeSaved = jokeSavedTask.checkIfStored();
                        Log.d("somethingbrokedebug","jokeSaved:" + jokeSaved);
                        //downloadButton.setBackground(ContextCompat.getDrawable(Objects.requireNonNull(getActivity()), R.drawable.checkred));
                        return true;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread());
        taskObservable.subscribe(new Observer<CheckIfJokeSavedTask>() {
            @Override
            public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {
                Log.d("TAG", "on subscribe called");
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
                Log.d("checkerdebug", "value of jokeSaved:" + jokeSaved);
                if (jokeSaved) {
                    downloadButton.setBackground(ContextCompat.getDrawable(Objects.requireNonNull(getActivity()), R.drawable.checkred));
                } else {
                    downloadButton.setBackground(ContextCompat.getDrawable(Objects.requireNonNull(getActivity()), R.drawable.downloadicon));
                }
                /*JSONArray tempARRAYJSON = new JSONArray();
                JSONObject tempObject = new JSONObject();
                tempARRAYJSON.put(tempObject);
                getActivity().getSharedPreferences("_", MODE_PRIVATE).edit().putString("joke", String.valueOf(tempARRAYJSON)).apply();*/
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disposables.clear();
    }//hi
    //please notice this
}