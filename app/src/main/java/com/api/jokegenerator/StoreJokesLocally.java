package com.api.jokegenerator;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.Iterator;

import static android.content.Context.MODE_PRIVATE;

public class StoreJokesLocally {
    public StoreJokesLocally() {
    }

    //Saves the joke using sharedPreferences
    public static void saveJoke(JSONObject jokeToSave, Context context) throws JSONException {
        JSONArray tempJSONArray;
        String stringOfArray = context.getSharedPreferences("_", MODE_PRIVATE).getString("localjokes", "");
        if (stringOfArray != "") {
            tempJSONArray = new JSONArray(stringOfArray);
        } else {
            tempJSONArray = new JSONArray();
        }
        String tempID = String.valueOf(jokeToSave.getInt("id"));
        if (!checkIfJokeSaved(tempID, context)) {
            tempJSONArray.put(jokeToSave);
            context.getSharedPreferences("_", MODE_PRIVATE).edit().putString("localjokes", String.valueOf(tempJSONArray)).apply();
            String stringOfJSONObject = context.getSharedPreferences("_", MODE_PRIVATE).getString("localids", "");
            JSONObject tempJSONObject;
            if (stringOfJSONObject != "") {
                tempJSONObject = new JSONObject(stringOfJSONObject);
            } else {
                tempJSONObject = new JSONObject();
            }
            tempJSONObject.put(jokeToSave.getString("id"), "true");
            context.getSharedPreferences("_", MODE_PRIVATE).edit().putString("localids", String.valueOf(tempJSONObject)).apply();
        }
    }

    //Deletes the joke using sharedPreferences
    public static void deleteJoke(String jokeID, Context context) throws JSONException {
        String listOfIDString = context.getSharedPreferences("_", MODE_PRIVATE).getString("localids", "");
        JSONObject idJSON = new JSONObject(listOfIDString);
        idJSON.remove(jokeID);
        String listOfJokesString = context.getSharedPreferences("_", MODE_PRIVATE).getString("localjokes", "");
        JSONArray jokeList = new JSONArray(listOfJokesString);
        boolean keepRunning = true;
        int x = 0;
        while (keepRunning) {
            JSONObject tempObj = jokeList.getJSONObject(x);
            if (tempObj.getString("id").equals(jokeID)) {
                keepRunning = false;
                jokeList.remove(x);
            }
            x++;
        }
        context.getSharedPreferences("_", MODE_PRIVATE).edit().putString("localjokes", String.valueOf(jokeList)).apply();
        context.getSharedPreferences("_", MODE_PRIVATE).edit().putString("localids", String.valueOf(idJSON)).apply();
    }
    //Checks if a joke is saved locally,using its ID
    public static boolean checkIfJokeSaved(String jokeID, Context context) throws JSONException {
        String JSONIdsString = context.getSharedPreferences("_", MODE_PRIVATE).getString("localids", "");
        JSONObject JSONIds = null;
        if (!JSONIdsString.equals("")) {
            JSONIds = new JSONObject(JSONIdsString);
        } else {
            return false;
        }
        boolean jokeSaved = false;
        Iterator<String> iter = JSONIds.keys();
        while (iter.hasNext()) {
            String key = iter.next();
            if(key.equals(jokeID)){
                jokeSaved = true;
            }
        }
        return jokeSaved;
    }
    //Returns all of the jokes in a JSONArray
    public static JSONArray returnSavedJokes(Context context) throws JSONException {
        JSONArray tempJSONArray;
        String stringOfArray = context.getSharedPreferences("_", MODE_PRIVATE).getString("localjokes", "");
        if (stringOfArray != "") {
            tempJSONArray = new JSONArray(stringOfArray);
        } else {
            tempJSONArray = new JSONArray();
        }
        return tempJSONArray;
    }
}
