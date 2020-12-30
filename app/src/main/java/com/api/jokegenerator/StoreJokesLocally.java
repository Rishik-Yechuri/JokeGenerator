package com.api.jokegenerator;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;

import static android.content.Context.MODE_PRIVATE;

public class StoreJokesLocally {
    public StoreJokesLocally() {
    }

    public static void saveJoke(JSONObject jokeToSave, Context context) throws JSONException {
        JSONArray tempJSONArray;
        String stringOfArray = context.getSharedPreferences("_", MODE_PRIVATE).getString("localjokes", "");
        if (stringOfArray != "") {
            tempJSONArray = new JSONArray(stringOfArray);
        } else {
            tempJSONArray = new JSONArray();
        }
        //tempJSONArray = new JSONArray();
        //tempJSONArray.put(jokeToSave);
        tempJSONArray.put( jokeToSave);
        Log.d("turnin", "tempJSONArray:" + tempJSONArray);
        Log.d("turnin", "pre transform joke:" + jokeToSave);
        //String tempTransform = jokeToSave.getString("id") + ":" + jokeToSave;
        //Log.d("turnin", "pre jsonify string:" + tempTransform);
        //jokeToSave = new JSONObject(tempTransform);
        Log.d("turnin", "joke being saved:" + jokeToSave);
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
        //context.getSharedPreferences("_", MODE_PRIVATE).edit().putString("joke", String.valueOf(tempARRAYJSON)).apply();
    }

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
        context.getSharedPreferences("_",MODE_PRIVATE).edit().putString("localids", String.valueOf(idJSON)).apply();
        Log.d("taste", "Whole thing:" + context.getSharedPreferences("_", MODE_PRIVATE).getString("localjokes", ""));
    }

    public static boolean checkIfJokeSaved(String jokeID, Context context) throws JSONException {
        Log.d("localchecks","Checking if saved");
        String JSONIdsString = context.getSharedPreferences("_", MODE_PRIVATE).getString("localids", "");
        JSONObject JSONIds = null;
        if (!JSONIdsString.equals("")) {
            Log.d("localchecks","If:" + JSONIdsString);
            JSONIds = new JSONObject(JSONIdsString);
        }else{
            Log.d("localchecks","else");
            return false;
        }
        return JSONIds.getString(jokeID).equals("true");
    }
}