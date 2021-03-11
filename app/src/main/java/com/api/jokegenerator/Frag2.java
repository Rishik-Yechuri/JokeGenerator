package com.api.jokegenerator;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
import java.util.Objects;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

import static android.content.Context.MODE_PRIVATE;

public class Frag2 extends Fragment {
    View view;
    //The layout that stores the notification widget(upside down snackbar)
    LinearLayout coordinatorLayout;
    //_updateJokes is a BroadcastReceiver that waits for messages from other activities
    BroadcastReceiver _updateJokes;
    //Stores jokes,and their IDs
    public static JSONArray jokeList;
    public static ArrayList<Integer> jokeListIDArray = new ArrayList<>();
    //Used for the RecyclerView
    RecyclerView recyclerView;
    RecyclerViewAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle onSavedInstance) {
        view = inflater.inflate(R.layout.frag2_layout, container, false);
        //Creates an IntentFilter that waits for "UPDATEJOKE"
        IntentFilter intentFilter = new IntentFilter("UPDATEJOKE");
        //Sets up stuff for the BroadcastReceiver
        _updateJokes = new SyncUpdate();
        getActivity().registerReceiver(_updateJokes, intentFilter);
        coordinatorLayout = view.findViewById(R.id.coordinatorLayout);
        //A JSON array that stores all the jokes,each of the jokes is a JSON.
        jokeList = new JSONArray();
        try {
            //Gets the jokes from sharedPreferences and updates jokeList
            jokeList = StoreJokesLocally.returnSavedJokes(getActivity());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //Goes through each joke
        for (int x = 0; x < jokeList.length(); x++) {
            //Stores the current joke
            JSONObject tempJokeHolder;

            try {
                tempJokeHolder = (JSONObject) jokeList.get(x);
                jokeListIDArray.add(Integer.valueOf(tempJokeHolder.getString("id")));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        //Initializes the recycler view
        initializeRecyclerView();

        return view;
    }

    private void initializeRecyclerView() {
        recyclerView = view.findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        new ItemTouchHelper(jokeTouched).attachToRecyclerView(recyclerView);
        adapter = new RecyclerViewAdapter(jokeList, getContext(),getFragmentManager());
        recyclerView.setAdapter(adapter);
    }
    public class SyncUpdate extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Gets the instruction from the message
            String instruction = intent.getExtras().getString("instruction");
            //Saves if the instruction is "save"
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
                            //jokeList.put(Integer.parseInt(intent.getExtras().getString("position")),tempJokeHolder);
                            jokeListIDArray.add(Integer.parseInt(intent.getExtras().getString("position")), Integer.valueOf(tempJokeHolder.getString("id")));
                        } else {
                            jokeList.put(tempJokeHolder);
                            jokeListIDArray.add(Integer.valueOf(tempJokeHolder.getString("id")));
                            jokePosition = jokeList.length() - 1;
                        }
                        adapter.notifyItemInserted(jokePosition);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            //Deletes if the instruction is "delete"
            else if (instruction.equals("delete")) {
                int id = Integer.parseInt(intent.getExtras().getString("id"));
                int index = -1;
                if(jokeListIDArray.size()>0){
                     index = jokeListIDArray.indexOf(id);
                }
               // int index = jokeListIDArray.indexOf(id);
                if (index != -1) {
                    //Deletes the joke and notifies the recycler view
                    jokeListIDArray.remove(index);
                    jokeList.remove(index);
                    //jokeListArray.remove(index);
                }
                jokeList.length();
                jokeList.remove(0);
                adapter.notifyDataSetChanged();
            }
            //showSync();
        }
    }

    //Given the JSON of the joke,only the joke part is returned(joke or setup/delivery)
    public static String returnJokeString(String tempJokeString) throws JSONException {
        JSONObject tempJokeJSON = new JSONObject(tempJokeString);
        String tempJoke = "";
        if (tempJokeJSON.getString("type").equals("single")) {
            tempJoke = tempJokeJSON.getString("joke");
        } else {
            tempJoke = tempJokeJSON.getString("setup") + tempJokeJSON.getString("delivery");
        }
        return tempJoke;
    }

    //Shows a Snackbar from the top that says "Jokes Synced"
    public void showSync() {
        View rotatedCoordinator = view.findViewById(R.id.top_coordinator);
        rotatedCoordinator.setRotation(180);
        Snackbar snack = Snackbar.make(rotatedCoordinator, "Jokes Updated", Snackbar.LENGTH_LONG);
        Snackbar.SnackbarLayout view = (Snackbar.SnackbarLayout) snack.getView();
        View parent = view.getChildAt(0);
        TextView snackText = view.findViewById(com.google.android.material.R.id.snackbar_text);
        snackText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        view.setBackgroundColor(Color.parseColor("#b30000"));
        ((View) view).setBackground(ContextCompat.getDrawable(Objects.requireNonNull(getActivity()), R.drawable.updatenotification));
        view.setPadding(0, 0, 0, 0);
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) view.getLayoutParams();
        params.gravity = Gravity.CENTER_HORIZONTAL;
        params.height = (int) convertDpToPixel(48, getActivity());
        params.width = (int) convertDpToPixel(130, getActivity());
        params.setMargins(0, 0, 0, 0);
        if (parent instanceof LinearLayout) {
            ((LinearLayout) parent).setRotation(180);
        }
        view.setLayoutParams(params);
        snack.show();
    }


    //Is used to check if a joke has been swiped left,and deletes them
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
            //Deletes the joke from firebase(it will later get notified to delete it locally too)
            String jokeID = String.valueOf(jokeListIDArray.remove(viewHolder.getAdapterPosition()));
            try {
                deleteJoke(String.valueOf(jokeID),getContext());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            //Removes it from the recycler view,and notifies the recycler view
            position = viewHolder.getAdapterPosition();
            currentJokeJSON = (JSONObject) jokeList.remove(viewHolder.getAdapterPosition());
            HashMap<String, ArrayList<String>> jokeGroups = new HashMap<>();
            HashMap<String, ArrayList<String>> groupMap = SheetButtonAdapter.returnGroupMap(getContext(),jokeGroups);
            String groupName = "";
            for (HashMap.Entry<String, ArrayList<String>> entry : groupMap.entrySet()) {
                String key = entry.getKey();
                ArrayList<String> jokeIDs = entry.getValue();
                if (jokeIDs.contains(jokeID)) {
                    groupName = key;
                }
            }
            if (!groupName.equals("")) {
                ArrayList<String> tempJokeList = jokeGroups.get(groupName);
                tempJokeList.remove(jokeID);
                jokeGroups.put(groupName, tempJokeList);
            }
            adapter.notifyDataSetChanged();
            JSONObject finalCurrentJokeJSON = currentJokeJSON;
            int finalPosition = position;
            String finalGroupName = groupName;
            Snackbar undoAction = Snackbar.make(view.findViewById(R.id.coordinatorLayout), "Joke Removed", Snackbar.LENGTH_LONG).setAction("UNDO", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!finalGroupName.equals("")){
                        ArrayList<String> jokeListAdd = jokeGroups.get(finalGroupName);
                        jokeListAdd.add(jokeID);
                        jokeGroups.put(finalGroupName, jokeListAdd);
                        getContext().getSharedPreferences("_", MODE_PRIVATE).edit().putString("groupmap", String.valueOf(jokeGroups)).apply();
                        Intent updategroup = new Intent("UPDATEGROUP");
                        getContext().sendBroadcast(updategroup);
                    }
                    try {
                        ListAllTask listAllTask = new ListAllTask(false, finalCurrentJokeJSON, finalPosition);
                        listAllTask.storeJoke(getContext());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
            undoAction.setActionTextColor(Color.rgb(255, 200, 35));
            undoAction.show();
            if(!finalGroupName.equals("")) {
                getContext().getSharedPreferences("_", MODE_PRIVATE).edit().putString("groupmap", String.valueOf(jokeGroups)).apply();
                Intent updategroup = new Intent("UPDATEGROUP");
                getContext().sendBroadcast(updategroup);
            }
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

    public static float convertDpToPixel(float dp, Context context) {
        return dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    //Calls firebase to delete the joke
    public static void deleteJoke(String id,Context context) throws JSONException {
        final String[] idToken = {""};
        Map<String, Object> data = new HashMap<>();
        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
        mUser.getIdToken(true)
                .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                    public void onComplete(@NonNull Task<GetTokenResult> task) {
                        if (task.isSuccessful()) {
                            idToken[0] = task.getResult().getToken();
                            data.put("token", idToken[0]);
                            data.put("fcmtoken", MyFirebaseMessagingService.getToken(context));
                            data.put("jokeid", id);
                            FirebaseFunctions.getInstance()
                                    .getHttpsCallable("deleteJoke")
                                    .call(data)
                                    .continueWith(new Continuation<HttpsCallableResult, String>() {
                                        @Override
                                        public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                                            return null;
                                        }
                                    });
                            // ...
                        }
                    }
                });
    }
}