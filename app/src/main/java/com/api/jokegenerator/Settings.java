package com.api.jokegenerator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Space;
import android.widget.TextView;

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
    RadioButton lightButton;
    RadioButton darkButton;
    RadioGroup themeGroup;
    View divider2;
    View divider3;
    TextView filters;
    Space filtersSpace;
    Space space2;
    Space space3;
    Space space4;
    Space space5;
    Space space6;
    float widthToSetGlobal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(MainActivity.currentTheme.equals("dark")){setTheme(R.style.AppTheme);}else{setTheme(R.style.AppThemeLight);}
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        filters = findViewById(R.id.textView);
        filtersSpace = findViewById(R.id.filtersSpace);
        space2 = findViewById(R.id.space2);
        space3 = findViewById(R.id.space3);
        space4 = findViewById(R.id.space4);
        space5 = findViewById(R.id.space5);
        space6 = findViewById(R.id.space6);
        setViewWidthInInches(.03,filtersSpace);
        setViewWidthInInches(.03,space2);
        setViewWidthInInches(.09,space3);
        setViewWidthInInches(.03,space4);
        setViewWidthInInches(.03,space5);
        setViewWidthInInches(.03,space6);

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
        themeGroup = findViewById(R.id.themeGroup);
        themeGroup.setOnCheckedChangeListener(themeChanged);
        lightButton = findViewById(R.id.lightButton);
        darkButton = findViewById(R.id.darkButton);
        String currentTheme = getApplicationContext().getSharedPreferences("_",MODE_PRIVATE).getString("theme","dark");
        if(currentTheme.equals("light")){
            lightButton.setChecked(true);
        }else if(currentTheme.equals("dark")){
            darkButton.setChecked(true);
        }
        divider2 = findViewById(R.id.divider2);
        divider3 = findViewById(R.id.divider3);
        if(MainActivity.currentTheme.equals("light")){
            divider2.setBackgroundColor(Color.parseColor("#CCCCCC"));
            divider3.setBackgroundColor(Color.parseColor(String.valueOf("#CCCCCC")));
        }
        AppCompatRadioButton rb;
        rb = new AppCompatRadioButton(getApplicationContext());
        ColorStateList darkColorList = new ColorStateList(
                new int[][]{
                        new int[]{-android.R.attr.state_checked}
                },
                new int[]{
                        R.color.colorPrimarySubtle,
                }
        );
        ColorStateList lightColorList = new ColorStateList(
                new int[][]{
                        new int[]{-android.R.attr.state_checked}
                },
                new int[]{
                        R.color.colorSecondarySubtle,
                }
        );
        if(MainActivity.currentTheme.equals("dark")){

        }
    }

    RadioGroup.OnCheckedChangeListener themeChanged = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (group == findViewById(R.id.themeGroup)) {
                if (checkedId == 2131230949) {
                    getApplicationContext().getSharedPreferences("_", MODE_PRIVATE).edit().putString("theme", "light").apply();
                }
                if (checkedId == 2131230847) {
                    getApplicationContext().getSharedPreferences("_", MODE_PRIVATE).edit().putString("theme", "dark").apply();
                }
            }
        }
    };

    public void setDefaultChips() {
        ArrayList<String> defaultChipList = new ArrayList<String>(Arrays.asList("Single", "Twopart", "Programming", "Misc", "Spooky", "Christmas", "Political", "Pun"));
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
            urlEnding += jokeTypeString;
        }
        link += urlEnding;
        getApplicationContext().getSharedPreferences("_", MODE_PRIVATE).edit().putString("jokeurl", link).apply();
        Log.d("filtercustom", "Custom URL:" + link);
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
    public void setViewWidthInInches(double inches, View v) {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        float mXDpi = metrics.xdpi;
        int twoInches = (int) Math.round(inches*mXDpi);
        v.setLayoutParams(new LinearLayout.LayoutParams(twoInches, ViewGroup.LayoutParams.WRAP_CONTENT));
        v.requestLayout();
    }
    public float convertDpToPx(Context context, float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }
}