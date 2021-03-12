package com.api.jokegenerator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

import static android.content.Context.MODE_PRIVATE;

public class GroupsFrag extends Fragment {
    View view;
    ArrayList<String> jokeGroups;
    RecyclerView groupRecyclerView;
    JokeGroupAdapter groupAdapter;
    HashMap<String, ArrayList<String>> jokeGroupMap;
    BroadcastReceiver _updateGroups;

    @Nullable
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_groups_frag, container, false);
        initializeRecyclerView();
        _updateGroups = new GroupUpdate();
        IntentFilter intentFilter = new IntentFilter("UPDATEGROUP");
        getActivity().registerReceiver(_updateGroups, intentFilter);
        return view;
    }
    public class GroupUpdate extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateGroups(/*intent.getExtras().getString("id")*/);
        }
    }
    private void initializeRecyclerView() {
        groupRecyclerView = view.findViewById(R.id.groupRecyclerView);
        groupRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        new ItemTouchHelper(jokeTouched).attachToRecyclerView(groupRecyclerView);
        jokeGroups = new ArrayList<String>();
        jokeGroupMap = new HashMap<>();
        groupAdapter = new JokeGroupAdapter(jokeGroups, jokeGroupMap, getContext());
        groupRecyclerView.setAdapter(groupAdapter);
        updateGroups();
    }

    public void updateGroups() {
        jokeGroups.clear();
        jokeGroupMap.clear();
        String groupMapString = getContext().getSharedPreferences("_", MODE_PRIVATE).getString("groupmap", "");
        String[] splitMap = groupMapString.split("], ");
        for (int x = 0; x < splitMap.length; x++) {
            String filteredString = splitMap[x].replaceAll("\\[", "").replaceAll("]", "").replaceAll("\\{", "");
            filteredString = filteredString.replace("}", "");
            String[] basicSplit = filteredString.split("=");
            String groupName = basicSplit[0];
            String[] tempGroupIDs = null;
            ArrayList<String> jokesInGroup = new ArrayList<>();
            if (basicSplit.length > 1) {
                tempGroupIDs = basicSplit[1].split(", ");
                jokesInGroup = new ArrayList<>(Arrays.asList(tempGroupIDs));
            }
            if (!groupName.equals("")) {
                jokeGroupMap.put(groupName, jokesInGroup);
                jokeGroups.add(groupName);
            }
        }
        groupAdapter.notifyDataSetChanged();
    }

    ItemTouchHelper.SimpleCallback jokeTouched = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            String groupString = "";
            int groupPosition = 0;
            //Remove the group
            groupPosition = viewHolder.getAdapterPosition();
            groupString = jokeGroups.remove(viewHolder.getAdapterPosition());
            ArrayList<String> jokesSavedInGroup = jokeGroupMap.remove(groupString);
            getContext().getSharedPreferences("_",MODE_PRIVATE).edit().putString("groupmap",String.valueOf(jokeGroupMap)).apply();
            groupAdapter.notifyDataSetChanged();
            String finalGroup = groupString;
            int finalGroupPosition = groupPosition;
            Snackbar undoAction = Snackbar.make(view.findViewById(R.id.groupMainLayout), "Joke Removed", Snackbar.LENGTH_LONG).setAction("UNDO", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Undo deleting the group
                    jokeGroups.add(finalGroupPosition, finalGroup);
                    jokeGroupMap.put(finalGroup,jokesSavedInGroup);
                    groupAdapter.notifyDataSetChanged();
                    getContext().getSharedPreferences("_",MODE_PRIVATE).edit().putString("groupmap",String.valueOf(jokeGroupMap)).apply();
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