package com.api.jokegenerator;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static android.content.Context.MODE_PRIVATE;

public class StoreJokesLocally {
    public StoreJokesLocally() {
    }

    public static void saveJoke(JSONObject jokeToSave, Context context) throws JSONException {
        JSONArray tempJSONArray;
        String stringOfArray = context.getSharedPreferences("_",MODE_PRIVATE).getString("localjokes","");
        tempJSONArray = new JSONArray(stringOfArray);
        tempJSONArray.put(jokeToSave);
        context.getSharedPreferences("_",MODE_PRIVATE).edit().putString("localjokes", String.valueOf(tempJSONArray)).apply();
        String stringOfJSONObject = context.getSharedPreferences("_",MODE_PRIVATE).getString("localids","");
        JSONObject tempJSONObject = new JSONObject(stringOfJSONObject);
        tempJSONObject.put(jokeToSave.getString("id"),"true");
        context.getSharedPreferences("_",MODE_PRIVATE).edit().putString("localids", String.valueOf(tempJSONObject)).apply();
        //context.getSharedPreferences("_", MODE_PRIVATE).edit().putString("joke", String.valueOf(tempARRAYJSON)).apply();
    }
}
