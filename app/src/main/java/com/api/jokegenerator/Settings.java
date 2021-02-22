package com.api.jokegenerator;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
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
    float widthToSetGlobal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        widthToSetGlobal = getWidthDp(getApplicationContext()) + 150;
        widthToSetGlobal = 580;
        filterChips = findViewById(R.id.filterChips);
        chipString = new ArrayList<String>(Arrays.asList("Single", "Twopart", "Programming", "Misc", "Dark", "Pun", "Spooky", "Christmas", "Nsfw", "Religious", "Political", "Racist", "Sexist", "Explicit"));
        chipList = new ArrayList<>();
        ArrayList<String> savedChips = new ArrayList<>();
        String stringOfArray = getApplicationContext().getSharedPreferences("_", MODE_PRIVATE).getString("savedchips", "");
        if (stringOfArray != "") {
            String[] tempJokes = stringOfArray.split("\\.");
            savedChips.addAll(Arrays.asList(tempJokes));
        }
        for (int x = 0; x < chipString.size(); x++) {
            Chip chipToCreate = (Chip) this.getLayoutInflater().inflate(R.layout.chiptocreate, null, false);
            chipToCreate.setText(chipString.get(x));
            if(savedChips.contains(chipString.get(x))){
                chipToCreate.setChecked(true);
                widthToSetGlobal+=20;
            }
            chipToCreate.setOnCheckedChangeListener(filterChipChecked);
            filterChips.addView(chipToCreate);
        }
        filterChips.setLayoutParams(new LinearLayout.LayoutParams((int) convertDpToPx(getApplicationContext(), widthToSetGlobal), ViewGroup.LayoutParams.MATCH_PARENT));
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
            if(isChecked){
                savedChips.add(String.valueOf(buttonView.getText()));
            }else{
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
        }
    };

    public float convertDpToPx(Context context, float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }
}