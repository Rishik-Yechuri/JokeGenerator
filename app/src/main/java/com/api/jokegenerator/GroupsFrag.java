package com.api.jokegenerator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

import static android.content.Context.MODE_PRIVATE;

public class GroupsFrag extends Fragment implements GroupDialog.DialogInterface {
    View view;
    ArrayList<String> jokeGroups;
    RecyclerView groupRecyclerView;
    JokeGroupAdapter groupAdapter;
    JSONObject jokeGroupMap;
    BroadcastReceiver _updateGroups;
    FloatingActionButton addFab;
    GroupDialog dialog;

    @Nullable
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_groups_frag, container, false);
        try {
            initializeRecyclerView();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        _updateGroups = new GroupUpdate();
        IntentFilter intentFilter = new IntentFilter("UPDATEGROUP");
        getActivity().registerReceiver(_updateGroups, intentFilter);
        addFab = view.findViewById(R.id.addFab);
        addFab.setOnClickListener(addClicked);
        return view;
    }

    @Override
    public void okClicked(String groupName) throws JSONException {
        jokeGroups.add(groupName);
        jokeGroupMap.put(groupName, new ArrayList<>());
        getContext().getSharedPreferences("_", MODE_PRIVATE).edit().putString("groupmap", String.valueOf(jokeGroupMap)).apply();
        groupAdapter.notifyDataSetChanged();
        addGroupAndIDs(groupName, null);
    }

    public class GroupUpdate extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                updateGroups(/*intent.getExtras().getString("id")*/);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void initializeRecyclerView() throws JSONException {
        groupRecyclerView = view.findViewById(R.id.groupRecyclerView);
        groupRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        new ItemTouchHelper(jokeTouched).attachToRecyclerView(groupRecyclerView);
        jokeGroups = new ArrayList<String>();
        jokeGroupMap = new JSONObject();
        groupAdapter = new JokeGroupAdapter(jokeGroups, jokeGroupMap, getContext());
        groupRecyclerView.setAdapter(groupAdapter);
        updateGroups();
    }

    public void updateGroups() throws JSONException {
        jokeGroups.clear();
        //jokeGroupMap.;

        JSONObject groupMapJSON = new JSONObject(getContext().getSharedPreferences("_", MODE_PRIVATE).getString("groupmap", ""));
        JSONArray key = groupMapJSON.names();
        int keyLength = 0;
        if(key != null){keyLength = key.length();}
        for (int i = 0; i < keyLength; ++i) {
            String groupName = key.getString (i);
            String value = groupMapJSON.getString (groupName);
            ArrayList<String> jokesInGroup = new ArrayList<>(Arrays.asList(value.split(",")));
            jokeGroupMap.remove(groupName);
            if(!groupName.equals("")){
                jokeGroupMap.put(groupName,jokesInGroup);
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
            ArrayList<String> jokesSavedInGroup = new ArrayList(Arrays.asList(jokeGroupMap.remove(groupString)));
            getContext().getSharedPreferences("_", MODE_PRIVATE).edit().putString("groupmap", String.valueOf(jokeGroupMap)).apply();
            groupAdapter.notifyDataSetChanged();
            try {
                removeGroup(groupString, getContext());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String finalGroup = groupString;
            int finalGroupPosition = groupPosition;
            Snackbar undoAction = Snackbar.make(view.findViewById(R.id.groupMainLayout), "Group Removed", Snackbar.LENGTH_LONG).setAction("UNDO", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Undo deleting the group
                    jokeGroups.add(finalGroupPosition, finalGroup);
                    try {
                        jokeGroupMap.put(finalGroup, jokesSavedInGroup);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    groupAdapter.notifyDataSetChanged();
                    getContext().getSharedPreferences("_", MODE_PRIVATE).edit().putString("groupmap", String.valueOf(jokeGroupMap)).apply();
                    try {
                        addGroupAndIDs(finalGroup, jokesSavedInGroup);
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
            new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                    .addSwipeLeftBackgroundColor(Color.RED)
                    .addSwipeLeftActionIcon(R.drawable.deleteicon)
                    .create()
                    .decorate();
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
    };

    public void removeGroup(String name, Context context) throws JSONException {
        final String[] idToken = {""};
        Map<String, Object> data = new HashMap<>();
        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
        mUser.getIdToken(true)
                .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                    public void onComplete(@NonNull Task<GetTokenResult> task) {
                        if (task.isSuccessful()) {
                            idToken[0] = task.getResult().getToken();
                            data.put("token", idToken[0]);
                            data.put("groupName", name);
                            FirebaseFunctions functions = FirebaseFunctions.getInstance();
                            functions.useEmulator("10.0.2.2.", 5001);
                            //FirebaseFunctions.getInstance()
                            functions
                                    .getHttpsCallable("removeGroup")
                                    .call(data)
                                    .continueWith(new Continuation<HttpsCallableResult, String>() {
                                        @Override
                                        public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                                            return null;
                                        }
                                    });
                        }
                    }
                });
    }

    View.OnClickListener addClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            openDialog();
        }
    };

    public void openDialog() {
        dialog = new GroupDialog();
        dialog.listener = this;
        dialog.show(getFragmentManager(), "something");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    public void addGroupAndIDs(String name, ArrayList<String> savedIds) throws JSONException {
        final String[] idToken = {""};
        Map<String, Object> data = new HashMap<>();
        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
        mUser.getIdToken(true)
                .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                    public void onComplete(@NonNull Task<GetTokenResult> task) {
                        if (task.isSuccessful()) {
                            idToken[0] = task.getResult().getToken();
                            data.put("token", idToken[0]);
                            data.put("groupName", name);
                            FirebaseFunctions functions = FirebaseFunctions.getInstance();
                            functions.useEmulator("10.0.2.2.", 5001);
                            //FirebaseFunctions.getInstance()
                            functions
                                    .getHttpsCallable("addGroup")
                                    .call(data)
                                    .continueWith(new Continuation<HttpsCallableResult, String>() {
                                        @Override
                                        public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                                            return null;
                                        }
                                    });
                        }
                    }
                });
        final String[] token = {""};
        Map<String, Object> data2 = new HashMap<>();
        mUser.getIdToken(true)
                .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                    public void onComplete(@NonNull Task<GetTokenResult> task) {
                        if (task.isSuccessful()) {
                            token[0] = task.getResult().getToken();
                            data2.put("token", idToken[0]);
                            data2.put("groupName", name);
                            data2.put("id", String.valueOf(savedIds));
                            FirebaseFunctions functions = FirebaseFunctions.getInstance();
                            functions.useEmulator("10.0.2.2.", 5001);
                            //FirebaseFunctions.getInstance()
                            functions
                                    .getHttpsCallable("addJokeToGroup")
                                    .call(data2)
                                    .continueWith(new Continuation<HttpsCallableResult, String>() {
                                        @Override
                                        public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                                            return null;
                                        }
                                    });
                        }
                    }
                });
    }
}