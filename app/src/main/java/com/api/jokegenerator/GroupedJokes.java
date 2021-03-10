package com.api.jokegenerator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

import static java.security.AccessController.getContext;

public class GroupedJokes extends AppCompatActivity {
    RecyclerView recyclerView;
    RecyclerViewAdapter adapter;
    ArrayList<String> savedJokeIDs;
    JSONArray allJokes = new JSONArray();
    JSONArray jokeList = new JSONArray();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grouped_jokes);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryVariant));
        setSupportActionBar(toolbar);
        Bundle extras = getIntent().getExtras();
        getSupportActionBar().setTitle(extras.getString("groupname"));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        savedJokeIDs = extras.getStringArrayList("jokesingroup");
        try {
            allJokes = StoreJokesLocally.returnSavedJokes(getApplicationContext());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        for(int x=0;x<allJokes.length();x++){
            try {
                JSONObject currentJoke = allJokes.getJSONObject(x);
                if(savedJokeIDs.contains(currentJoke.getString("id"))){
                    jokeList.put(currentJoke);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        initializeRecyclerView();
    }

    @Override
    public boolean onSupportNavigateUp() {
        return goBack();
    }
    @Override
    public void onBackPressed() {
        goBack();
    }

    public boolean goBack() {
        getSupportFragmentManager().popBackStack();
        finish();
        overridePendingTransition(0, R.anim.slide_out_right);
        return true;
    }

    private void initializeRecyclerView() {
        recyclerView = findViewById(R.id.jokeRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        new ItemTouchHelper(jokeTouched).attachToRecyclerView(recyclerView);
        adapter = new RecyclerViewAdapter(jokeList, getApplicationContext(), getSupportFragmentManager());
        recyclerView.setAdapter(adapter);
    }

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
            String jokeID = String.valueOf(Frag2.jokeListIDArray.remove(viewHolder.getAdapterPosition()));
            try {
                Frag2.deleteJoke(String.valueOf(jokeID), getApplicationContext());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            //Removes it from the recycler view,and notifies the recycler view
            position = viewHolder.getAdapterPosition();
            currentJokeJSON = (JSONObject) jokeList.remove(viewHolder.getAdapterPosition());
            adapter.notifyDataSetChanged();
            JSONObject finalCurrentJokeJSON = currentJokeJSON;
            int finalPosition = position;
            Snackbar undoAction = Snackbar.make(findViewById(R.id.groupedJokesMain), "Joke Removed", Snackbar.LENGTH_LONG).setAction("UNDO", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        ListAllTask listAllTask = new ListAllTask(false, finalCurrentJokeJSON, finalPosition);
                        listAllTask.storeJoke(getApplicationContext());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
            undoAction.setActionTextColor(Color.rgb(255, 200, 35));
            undoAction.show();
        }
        @Override
        public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive).addSwipeLeftBackgroundColor(Color.RED).addSwipeLeftActionIcon(R.drawable.deleteicon).create().decorate();
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
    };
}