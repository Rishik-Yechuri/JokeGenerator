package com.api.jokegenerator;

import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Space;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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

import static android.content.Context.MODE_PRIVATE;


public class Frag1 extends Fragment {
    Button generateJokeButton;
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
        jokeQuestionText.setText(getActivity().getSharedPreferences("_",MODE_PRIVATE).getString("setup","No Joke Yet"));
        punchlineText.setText(getActivity().getSharedPreferences("_",MODE_PRIVATE).getString("delivery",""));
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
            }
        }
    }

    public void getJokeAndDisplay() throws IOException, JSONException {
        getJoke task = new getJoke(Frag1.this);
        task.execute();
    }

    private static class getJoke extends AsyncTask<Integer, Integer, String> {
        WeakReference<Frag1> activityWeakReference;
        JSONObject jsonObject;
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
                while(received != null){
                    received = bufferedReader.readLine();
                    finalJSON += received;
                }

                Log.d("broke","received:" + finalJSON);
                jsonObject = new JSONObject(finalJSON);
                type = jsonObject.getString("type");
            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }
            return "done";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Frag1 activity = activityWeakReference.get();
            if(type.equals("single")){
                try {
                    activity.jokeQuestionText.setText(jsonObject.getString("joke"));
                    activity.punchlineText.setText("");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }else if(type.equals("twopart")){
                try {
                    activity.jokeQuestionText.setText(jsonObject.getString("setup"));
                    activity.punchlineText.setText(jsonObject.getString("delivery"));
                    activity.getActivity().getSharedPreferences("_", MODE_PRIVATE).edit().putString("setup",jsonObject.getString("setup")).apply();
                    activity.getActivity().getSharedPreferences("_", MODE_PRIVATE).edit().putString("delivery",jsonObject.getString("delivery")).apply();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}