package com.api.jokegenerator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class GroupsFrag extends Fragment {
    View view;
    ArrayList<String> jokeGroups;
    RecyclerView groupRecyclerView;
    JokeGroupAdapter groupAdapter;
    @Nullable
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_groups_frag, container, false);
        initializeRecyclerView();
        return view;
    }
    private void initializeRecyclerView() {
        groupRecyclerView = view.findViewById(R.id.groupRecyclerView);
        groupRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        new ItemTouchHelper(jokeTouched).attachToRecyclerView(groupRecyclerView);
        //Temp array
        jokeGroups = new ArrayList<>(Arrays.asList("Funny", "Meme", "Diseased", "Cursed"));
        groupAdapter = new JokeGroupAdapter(jokeGroups, getContext());
        groupRecyclerView.setAdapter(groupAdapter);
    }
    ItemTouchHelper.SimpleCallback jokeTouched = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            JSONObject currentJokeJSON = null;
            String groupString = "";
            int position = 0;
            int groupPosition = 0;
                //Remove the group
                groupPosition = viewHolder.getAdapterPosition();
                groupString = jokeGroups.remove(viewHolder.getAdapterPosition());
                groupAdapter.notifyDataSetChanged();
                Toast.makeText(getContext(), "Group Removed", Toast.LENGTH_SHORT).show();
            String finalGroup = groupString;
            int finalGroupPosition = groupPosition;
            Snackbar undoAction = Snackbar.make(view.findViewById(R.id.groupMainLayout), "Joke Removed", Snackbar.LENGTH_LONG).setAction("UNDO", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                        //Undo deleting the group
                        jokeGroups.add(finalGroupPosition,finalGroup);
                        groupAdapter.notifyDataSetChanged();
                        Toast.makeText(getContext(), "Undo Group Remove", Toast.LENGTH_SHORT).show();
                    }
            });
            undoAction.setActionTextColor(Color.rgb(255, 200, 35));
            undoAction.show();
        }
        @Override
        public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                    .addSwipeLeftBackgroundColor(Color.RED)
                    .addSwipeLeftActionIcon(R.drawable.deleteicon)
                    .create()
                    .decorate();
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
    };
}