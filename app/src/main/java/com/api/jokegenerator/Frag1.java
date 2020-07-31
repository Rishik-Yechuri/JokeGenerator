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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

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
import java.util.Objects;

import static android.content.Context.MODE_PRIVATE;


public class Frag1 extends Fragment {
    JSONObject jsonObject;
    Button generateJokeButton;
    Button downloadButton;
    TextView jokeQuestionText;
    TextView punchlineText;
    int endNumber;
    ArrayList<String> playerNames;
    ArrayList<Button> holdButtons;
    ArrayList<Button> holdButtons2;

    @Nullable
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag1_layout, container, false);
        generateJokeButton = view.findViewById(R.id.generateJokeButton);
        generateJokeButton.setOnClickListener(new GenerateJokeListener());
        jokeQuestionText = view.findViewById(R.id.jokeQuestionText);
        punchlineText = view.findViewById(R.id.punchlineText);
        downloadButton = view.findViewById(R.id.downloadButton);
        downloadButton.setOnClickListener(new GenerateJokeListener());
        JSONObject jokeJSON = null;
        try {
            jokeJSON = new JSONObject(Objects.requireNonNull(((Objects.requireNonNull(getActivity()))).getSharedPreferences("_", MODE_PRIVATE).getString("joke", "")));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (jokeJSON != null) {
            try {
                if(jokeJSON.getString("type").equals("single")){
                    jokeQuestionText.setText(jokeJSON.getString("joke"));
                    punchlineText.setText("");
                }else if(jokeJSON.getString("type").equals("twopart")){
                    jokeQuestionText.setText(jokeJSON.getString("setup"));
                    punchlineText.setText(jokeJSON.getString("delivery"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        /*jokeQuestionText.setText(getActivity().getSharedPreferences("_", MODE_PRIVATE).getString("setup", "No Joke Yet"));
        punchlineText.setText(getActivity().getSharedPreferences("_", MODE_PRIVATE).getString("delivery", ""));*/
        return view;
    }

    class GenerateJokeListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.generateJokeButton) {
                try {
                    getJokeAndDisplay();
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            } else if (v.getId() == R.id.downloadButton) {
                downloadJoke(v);
            }
        }
    }


    public void getJokeAndDisplay() throws IOException, JSONException {
        getJoke task = new getJoke(Frag1.this);
        task.execute();
    }

    public void downloadJoke(View v) {
        //v.setBackground(ContextCompat.getDrawable(Objects.requireNonNull(getActivity()),R.drawable.checkred));
        String tempJoke = "Why did the chicken cross the road? I don't even know";
        StorageReference whereToSaveJoke = FirebaseStorage.getInstance().getReference().child("storedjokes/" + System.currentTimeMillis() + ".json");
        /*whereToSaveJoke.putFile(Uri.parse(String.valueOf(jsonObject)))*/whereToSaveJoke.putBytes(tempJoke.getBytes()).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(getActivity(),"Joke Added",Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity(),"Joke Failed:" + e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
        v.setBackground(ContextCompat.getDrawable(Objects.requireNonNull(getActivity()),R.drawable.checkred));
    }

    private static class getJoke extends AsyncTask<Integer, Integer, String> {
        WeakReference<Frag1> activityWeakReference;

        String type = "";

        getJoke(Frag1 activity) {
            activityWeakReference = new WeakReference<Frag1>(activity);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Frag1 activity = activityWeakReference.get();
        }

        @Override
        protected String doInBackground(Integer... integers) {
            Frag1 activity = activityWeakReference.get();
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
                activity.jsonObject = new JSONObject(finalJSON);
                type = activity.jsonObject.getString("type");
            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }
            return "done";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Frag1 activity = activityWeakReference.get();
            if (type.equals("single")) {
                try {
                    activity.jokeQuestionText.setText(activity.jsonObject.getString("joke"));
                    activity.punchlineText.setText("");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else if (type.equals("twopart")) {
                try {
                    activity.jokeQuestionText.setText(activity.jsonObject.getString("setup"));
                    activity.punchlineText.setText(activity.jsonObject.getString("delivery"));
                    //activity.getActivity().getSharedPreferences("_", MODE_PRIVATE).edit().putString("setup", jsonObject.getString("setup")).apply();
                    //activity.getActivity().getSharedPreferences("_", MODE_PRIVATE).edit().putString("delivery", jsonObject.getString("delivery")).apply();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            String jokeJSON = activity.jsonObject.toString();
            activity.getActivity().getSharedPreferences("_", MODE_PRIVATE).edit().putString("joke", jokeJSON).apply();
        }
    }

    class SaveJoke extends AsyncTask<Integer, Void, String> {
        private WeakReference<Frag1> imageViewReference;
        private int data = 0;

        SaveJoke(Frag1 activity) {
            // Use a WeakReference to ensure the ImageView can be garbage collected
        }

        // Decode image in background.
        @Override
        protected String doInBackground(Integer... params) {
            return "done";
        }

        // Once complete, see if ImageView is still around and set bitmap.
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }
}