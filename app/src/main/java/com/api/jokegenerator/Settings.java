package com.api.jokegenerator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import org.json.JSONArray;
import org.json.JSONException;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;

public class Settings extends AppCompatActivity {
    ChipGroup filterChips;
    ArrayList<String> chipString;
    ArrayList<Chip> chipList;
    ArrayList<String> categories;
    ArrayList<String> blacklisted;
    ArrayList<String> numOfPartsString;
    float widthToSetGlobal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        widthToSetGlobal = getWidthDp(getApplicationContext()) + 150;
        widthToSetGlobal = 580;
        filterChips = findViewById(R.id.filterChips);
        categories = new ArrayList<>(Arrays.asList("Programming", "Misc", "Dark", "Pun", "Spooky", "Christmas"));
        blacklisted = new ArrayList<>(Arrays.asList("nsfw", "religious", "political", "racist", "sexist", "explicit"));
        numOfPartsString = new ArrayList<>(Arrays.asList("single", "twopart"));
        chipString = new ArrayList<String>(Arrays.asList("Single", "Twopart", "Programming", "Misc", "Dark", "Pun", "Spooky", "Christmas", "Nsfw", "Religious", "Political", "Racist", "Sexist", "Explicit"));
        chipList = new ArrayList<>();
        ArrayList<String> savedChips = new ArrayList<>();
        String stringOfArray = getApplicationContext().getSharedPreferences("_", MODE_PRIVATE).getString("savedchips", "");
        boolean firstRun = Boolean.parseBoolean(getApplicationContext().getSharedPreferences("_", MODE_PRIVATE).getString("chipsfirstrun", "true"));
        getApplicationContext().getSharedPreferences("_", MODE_PRIVATE).edit().putString("chipsfirstrun", "false").apply();
        if (stringOfArray != "") {
            String[] tempJokes = stringOfArray.split("\\.");
            savedChips.addAll(Arrays.asList(tempJokes));
        }
        for (int x = 0; x < chipString.size(); x++) {
            Chip chipToCreate = (Chip) this.getLayoutInflater().inflate(R.layout.chiptocreate, null, false);
            chipToCreate.setText(chipString.get(x));
            if (savedChips.contains(chipString.get(x))) {
                chipToCreate.setChecked(true);
                widthToSetGlobal += 20;
            }
            chipToCreate.setOnCheckedChangeListener(filterChipChecked);
            chipToCreate.setChipBackgroundColorResource(R.color.colorAccent);
            chipList.add(chipToCreate);
            filterChips.addView(chipToCreate);
        }
        if (firstRun) {
            setDefaultChips();
            changeJokeURL();
        }
        filterChips.setLayoutParams(new LinearLayout.LayoutParams((int) convertDpToPx(getApplicationContext(), widthToSetGlobal), ViewGroup.LayoutParams.MATCH_PARENT));
    }

    public void setDefaultChips() {
        ArrayList<String> defaultChipList = new ArrayList<String>(Arrays.asList("Single", "Twopart", "Programming", "Misc", "Spooky", "Christmas", "Political","Pun"));
        for (int x = 0; x < chipString.size(); x++) {
            if (defaultChipList.contains(chipString.get(x))) {
                chipList.get(x).setChecked(true);
                filterChips.setLayoutParams(new LinearLayout.LayoutParams((int) convertDpToPx(getApplicationContext(), widthToSetGlobal), ViewGroup.LayoutParams.MATCH_PARENT));
            }
        }
    }

    public void changeJokeURL() {
        String link = "https://v2.jokeapi.dev/joke/";
        String categoriesString = "";
        String blacklistedString = "";
        String jokeTypeString = "";
        int numOfTypesChecked = 0;
        for (int x = 0; x < chipList.size(); x++) {
            String chipText = String.valueOf(chipList.get(x).getText());
            if (chipList.get(x).isChecked()) {
                if (categories.contains(chipText)) {
                    if (!categoriesString.equals("")) {
                        categoriesString += ",";
                    }
                    categoriesString += chipText;
                } else if (numOfPartsString.contains(chipText.toLowerCase())) {
                    if (numOfTypesChecked == 0) {
                        jokeTypeString = "type=" + chipText.toLowerCase();
                    } else {
                        jokeTypeString = "";
                    }
                    numOfTypesChecked++;
                }
            } else {
                if (blacklisted.contains(chipText.toLowerCase())) {
                    if (blacklistedString.equals("")) {
                        blacklistedString = "blacklistFlags=" + chipText.toLowerCase();
                    } else {
                        blacklistedString += "," + chipText.toLowerCase();
                    }
                }
            }
        }
        if (categoriesString.equals("")) {
            categoriesString = "Any";
        }
        boolean questionmarkAdded = false;
        String urlEnding = "";
        urlEnding += categoriesString;
        if (!blacklistedString.equals("")) {
            urlEnding += "?" + blacklistedString;
            questionmarkAdded = true;
        }
        if (!jokeTypeString.equals("")) {
            if (!questionmarkAdded) {
                urlEnding += "?";
            } else {
                urlEnding += "&";
            }
            urlEnding+=jokeTypeString;
        }
        link+=urlEnding;
        getApplicationContext().getSharedPreferences("_", MODE_PRIVATE).edit().putString("jokeurl", link).apply();
        Log.d("filtercustom","Custom URL:" + link);
    }

    public static float getWidthDp(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        return dpWidth;
    }

    CompoundButton.OnCheckedChangeListener filterChipChecked = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                widthToSetGlobal += 20;
            } else {
                widthToSetGlobal -= 20;
            }
            filterChips.setLayoutParams(new LinearLayout.LayoutParams((int) convertDpToPx(getApplicationContext(), widthToSetGlobal), ViewGroup.LayoutParams.MATCH_PARENT));
            ArrayList<String> savedChips = new ArrayList<>();
            String stringOfArray = getApplicationContext().getSharedPreferences("_", MODE_PRIVATE).getString("savedchips", "");
            if (stringOfArray != "") {
                String[] tempJokes = stringOfArray.split("\\.");
                savedChips.addAll(Arrays.asList(tempJokes));
            }
            if (isChecked) {
                savedChips.add(String.valueOf(buttonView.getText()));
            } else {
                savedChips.remove(String.valueOf(buttonView.getText()));
            }
            String stringToSave = "";
            for (int x = 0; x < savedChips.size(); x++) {
                stringToSave += savedChips.get(x);
                if (x < savedChips.size() - 1) {
                    stringToSave += ".";
                }
            }
            getApplicationContext().getSharedPreferences("_", MODE_PRIVATE).edit().putString("savedchips", stringToSave).apply();
            changeJokeURL();
        }
    };

    public float convertDpToPx(Context context, float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }
}