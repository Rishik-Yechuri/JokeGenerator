package com.api.jokegenerator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

import static com.api.jokegenerator.Frag2.jokeListIDArray;
import static com.api.jokegenerator.Frag2.returnJokeString;

public class GroupedJokes extends AppCompatActivity {
    //Initializes views
    LinearLayout groupJokesMain;
    RecyclerView recyclerView;
    //Initializes other things
    RecyclerViewAdapter adapter;
    ArrayList<String> savedJokeIDs;
    JSONArray allJokes = new JSONArray();
    JSONArray jokeList = new JSONArray();
    BroadcastReceiver _updateJokes;
    BroadcastReceiver _syncUpdate;
    Bundle extras;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Sets the colors based on the theme
        if(MainActivity.currentTheme.equals("dark")){setTheme(R.style.AppTheme);}else{setTheme(R.style.AppThemeLight);}
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grouped_jokes);
        //Declares groupJokesMain
        groupJokesMain = findViewById(R.id.groupedJokesMain);
        //Initialize toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        //Sets the colors of the toolbar based on the theme
        if(MainActivity.currentTheme.equals("light")){
            toolbar.getContext().setTheme(R.style.ToolbarLight);
            toolbar.setTitleTextColor(ContextCompat.getColor(getApplicationContext(),R.color.colorSecondaryVariant));
            groupJokesMain.setBackgroundColor(Color.parseColor("#FFFFFF"));
        }else {
            toolbar.getContext().setTheme(R.style.Toolbar);
            toolbar.setTitleTextColor(ContextCompat.getColor(getApplicationContext(),R.color.colorPrimaryVariant));
            groupJokesMain.setBackgroundColor(Color.parseColor("#292424"));
        }
        //Add the toolbar up top
        setSupportActionBar(toolbar);
        //Set the title of the toolbar to the group name
        extras = getIntent().getExtras();
        getSupportActionBar().setTitle(extras.getString("groupname"));
        //Enable the back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        try {
            //Get the stored jokes
            allJokes = StoreJokesLocally.returnSavedJokes(getApplicationContext());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //Display all the jokes
        initializeRecyclerView();
        try {
            //Update the group
            updateJokesInGroup(extras.getStringArrayList("jokesingroup"), extras.getString("groupname"), null);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        IntentFilter intentFilter = new IntentFilter("UPDATEGROUP");
        //Sets up stuff for the BroadcastReceiver
        _updateJokes = new GroupUpdate();
        getApplicationContext().registerReceiver(_updateJokes, intentFilter);
        IntentFilter intentFilter2 = new IntentFilter("UPDATEJOKE");
        //Sets up stuff for the BroadcastReceiver
        _syncUpdate = new SyncUpdate();
        getApplication().registerReceiver(_syncUpdate, intentFilter2);
    }

    //Go back when the back button is pressed
    @Override
    public boolean onSupportNavigateUp() {
        return goBack();
    }

    //Go back when the physical back button is pressed
    @Override
    public void onBackPressed() {
        goBack();
    }

    //Goes back to the previous activity,and adds a animation
    public boolean goBack() {
        getSupportFragmentManager().popBackStack();
        finish();
        overridePendingTransition(0, R.anim.slide_out_right);
        return true;
    }

    //Gets called whenever there is a update
    public class SyncUpdate extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Gets the instruction from the broadcast
            String instruction = intent.getExtras().getString("instruction");
            //Saves the joke if the instruction is to save
            if (instruction.equals("save")) {
                JSONObject tempJokeHolder = null;
                String jokeString = null;
                try {
                    //Initializes the current joke
                    tempJokeHolder = new JSONObject(intent.getExtras().getString("joke"));
                    //Goes through the saved jokes checking for duplicates
                    boolean canAdd = true;
                    //tempJoke is used to temporarily store a joke from "jokeListArray"
                    String tempJoke = intent.getExtras().getString("joke");
                    ;
                    for (int i = 0; i < jokeList.length(); i++) {
                        //If the received joke is locally saved,set canAdd to false
                        if (tempJoke.equals(jokeList.get(i))) {
                            canAdd = false;
                        }
                    }
                    //Checks if the joke can be added
                    if (!tempJokeHolder.getString("id").equals("-1") && canAdd) {
                        try {
                            jokeString = returnJokeString(intent.getExtras().getString("joke"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        //Saves the joke information and notifies the recycler view of the changes
                        int jokePosition = -5;
                        if (intent.getExtras().getString("position") != null) {
                            jokePosition = Integer.parseInt(intent.getExtras().getString("position"));
                        }
                        if (jokePosition != -5) {
                            JSONArray updateJokeList = new JSONArray();
                            for (int i = 0; i < jokeList.length(); i++) {
                                updateJokeList.put(jokeList.get(i));
                            }
                            int numOfIterations = jokeList.length();
                            for (int i = 0; i < numOfIterations; i++) {
                                jokeList.remove(0);
                            }
                            for (int x = 0; x <= updateJokeList.length(); x++) {
                                if (x == jokePosition) {
                                    jokeList.put(tempJokeHolder);
                                }
                                if (x < updateJokeList.length()) {
                                    jokeList.put(updateJokeList.get(x));
                                }
                            }
                        } else {
                            jokeList.put(tempJokeHolder);
                            jokePosition = jokeList.length() - 1;
                        }
                        adapter.notifyItemInserted(jokePosition);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //When it is called,a joke is removed from the group
    public class GroupUpdate extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                updateJokesInGroup(extras.getStringArrayList("jokesingroup"), extras.getString("groupname"), intent.getStringExtra("idtoremove"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    //Removes a joke from the group
    public void updateJokesInGroup(ArrayList<String> savedIDs, String tempGroupName, String idToRemove) throws JSONException {
        int lengthOfList = jokeList.length();
        for (int x = 0; x < lengthOfList; x++) {
            jokeList.remove(0);
        }
        JSONObject groupMapJSON = new JSONObject(getApplicationContext().getSharedPreferences("_", MODE_PRIVATE).getString("groupmap", ""));
        JSONArray key = groupMapJSON.names();
        int keyLength = key != null?key.length():0;
        for (int i = 0; i < keyLength; ++i) {
            String groupName = key.getString (i);
            String value = groupMapJSON.getString (groupName);
            ArrayList<String> jokesInGroup = new ArrayList<>(Arrays.asList(value.split(",")));

        }
        savedJokeIDs = savedIDs;
        for (int x = 0; x < allJokes.length(); x++) {
            try {
                JSONObject currentJoke = allJokes.getJSONObject(x);
                if (savedJokeIDs.contains(currentJoke.getString("id"))) {
                    if (idToRemove != null && currentJoke.getString("id").equals(idToRemove)) {
                        savedJokeIDs.remove(currentJoke.getString("id"));
                        //Frag2.jokeList.remove(x);
                    } else {
                        jokeList.put(currentJoke);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        //Notifies the adapter about the new data
        adapter.notifyDataSetChanged();
    }

    //Initializes the things needed for the recycler view
    private void initializeRecyclerView() {
        recyclerView = findViewById(R.id.jokeRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        new ItemTouchHelper(jokeTouched).attachToRecyclerView(recyclerView);
        adapter = new RecyclerViewAdapter(jokeList, getApplicationContext(), getSupportFragmentManager());
        recyclerView.setAdapter(adapter);
    }

    //Responds when a item is swiped left on
    ItemTouchHelper.SimpleCallback jokeTouched = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            JSONObject currentJokeJSON = null;
            int position = 0;
            //Deletes the joke from firebase(it will later get notified to delete it locally too)
            ArrayList<Integer> tempQuick = jokeListIDArray;
            String jokeID = String.valueOf(Frag2.jokeListIDArray.get(viewHolder.getAdapterPosition()));
            try {
                Frag2.deleteJoke(String.valueOf(jokeID), getApplicationContext());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            //Removes it from the recycler view,and notifies the recycler view
            Intent deleteJoke = new Intent("UPDATEJOKE");
            deleteJoke.putExtra("instruction", "delete");
            deleteJoke.putExtra("id", String.valueOf(jokeID));
            getApplicationContext().sendBroadcast(deleteJoke);
            Intent updategroup = new Intent("UPDATEGROUP");
            updategroup.putExtra("grouptoremovefrom", extras.getString("groupname"));
            updategroup.putExtra("idlistoremovefromgroup",String.valueOf(new ArrayList<String>(Arrays.asList(jokeID))));
            updategroup.putExtra("idtoremove", jokeID);
            updategroup.putExtra("id", jokeID);
            updategroup.putExtra("fromswipe","true");
            position = viewHolder.getAdapterPosition();
            currentJokeJSON = (JSONObject) jokeList.remove(viewHolder.getAdapterPosition());
            JSONObject jokeGroups = new JSONObject();
            JSONObject groupMap = null;
            try {
                groupMap = SheetButtonAdapter.returnGroupMap(getApplicationContext(), jokeGroups);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            JSONArray key = groupMap.names();
            String groupName = "";
            for (int i = 0; i < key.length(); ++i) {
                String tempGroupName = null;
                try {
                    tempGroupName = key.getString(i);
                    String value = jokeGroups.getString(tempGroupName);
                    ArrayList<String> jokesInGroup = new ArrayList<>(Arrays.asList(value.replace("[","").replace("]","").replace(" ","").split(",")));
                    if(jokesInGroup.contains(jokeID)){
                        groupName = tempGroupName;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if (!groupName.equals("")) {
                try{
                ArrayList<String> tempJokeList = new ArrayList<String>(Arrays.asList(jokeGroups.get(groupName).toString().replace("[","").replace("]","").replace(" ","").split(",")));
                tempJokeList.remove(jokeID);
                jokeGroups.put(groupName, tempJokeList);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            adapter.notifyDataSetChanged();
            //Creates some final variables to be used if undo is clicked
            JSONObject finalCurrentJokeJSON = currentJokeJSON;
            int finalPosition = position;
            String finalGroupName = groupName;
            //Create an undo option
            Snackbar undoAction = Snackbar.make(findViewById(R.id.groupedJokesMain), "Joke Removed", Snackbar.LENGTH_LONG).setAction("UNDO", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try{
                    ArrayList<String> jokeListAdd = new ArrayList<String>(Arrays.asList(jokeGroups.get(finalGroupName).toString().replace("[","").replace("]","").replace(" ","").split(",")));
                    jokeListAdd.add(jokeID);
                    jokeGroups.put(finalGroupName, jokeListAdd);
                    getApplicationContext().getSharedPreferences("_", MODE_PRIVATE).edit().putString("groupmap", String.valueOf(jokeGroups)).apply();
                    Intent updategroup = new Intent("UPDATEGROUP");
                    updategroup.putExtra("idlistotaddtogroup", String.valueOf(new ArrayList<String>(Arrays.asList(jokeID))));
                    updategroup.putExtra("grouptoaddto", finalGroupName);
                    getApplicationContext().sendBroadcast(updategroup);
                    jokeList.put(finalCurrentJokeJSON);
                    adapter.notifyDataSetChanged();
                    try {
                        ListAllTask listAllTask = new ListAllTask(false, finalCurrentJokeJSON, finalPosition);
                        listAllTask.storeJoke(getApplicationContext());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
            //Set some properties for the undo option,and show it
            undoAction.setActionTextColor(Color.rgb(255, 200, 35));
            undoAction.show();
            if (!finalGroupName.equals("")) {
                getApplicationContext().getSharedPreferences("_", MODE_PRIVATE).edit().putString("groupmap", String.valueOf(jokeGroups)).apply();
                getApplicationContext().sendBroadcast(updategroup);
            }
        }

        //Adds some effects to the swipe
        @Override
        public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive).addSwipeLeftBackgroundColor(Color.RED).addSwipeLeftActionIcon(R.drawable.deleteicon).create().decorate();
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
    };
}