package com.api.jokegenerator;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;
import static com.api.jokegenerator.Frag2.jokeList;
import static java.security.AccessController.getContext;

public class SheetButtonAdapter extends RecyclerView.Adapter<SheetButtonAdapter.ViewHolder> {
    //Stores all the jokes to be displayed
    ArrayList<String> groups;
    Context mContext;
    //HashMap<String, ArrayList<String>> jokeGroups = new HashMap<>();
    JSONObject jokeGroups = new JSONObject();
    String jokeID = "";
    DismissSheet SheetClickListener = null;

    public SheetButtonAdapter(ArrayList<String> groups, Context context, DismissSheet SheetClickListener) {
        //Initializes variables
        this.groups = groups;
        this.SheetClickListener = SheetClickListener;
        mContext = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclablesheetbutton, parent, false);
        ViewHolder holder = new ViewHolder(view, SheetClickListener);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        //Sets the jokes text
        holder.optionText.setText(groups.get(position));
        View tempView = holder.mainLayout;
        tempView.setOnClickListener(new OptionClicked((String) holder.optionText.getText()));
        if (groups.get(position).equals("Delete")) {
            holder.optionText.setTextColor(Color.RED);
        } else if (groups.get(position).split(" ")[0].equals("Remove")) {

        } else {
            holder.optionText.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimaryVariant));
        }
    }

    public class OptionClicked implements View.OnClickListener {

        String Text;

        public OptionClicked(String Text) {
            this.Text = Text;
        }

        @Override
        public void onClick(View v) {
            Intent updategroup = new Intent("UPDATEGROUP");
            String firstWord = Text.split(" ")[0];
            SheetClickListener.DismissSheet();
            try {
                jokeGroups = returnGroupMap(mContext, jokeGroups);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String groupName = "";

            JSONObject holdMap = null;
            try {
                holdMap = new JSONObject(mContext.getSharedPreferences("_", MODE_PRIVATE).getString("groupmap", "").replace("[","").replace("]",""));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            JSONArray key = holdMap.names();
            int keyLength = key != null ? key.length() : 0;
            for (int i = 0; i < keyLength; ++i) {
                try {
                    String tempGroupName = key.getString(i);
                    String value = holdMap.getString(tempGroupName);
                    ArrayList<String> jokesInGroup = new ArrayList<>(Arrays.asList(value.replace("[","").replace("]","").replace(" ","").split(",")));
                    if(jokesInGroup.contains(jokeID)){
                        groupName = tempGroupName;
                        jokesInGroup.remove(jokeID);
                        jokeGroups.put(groupName,jokesInGroup);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            /*String[] nameParts = Text.split(" ");
            for (int x = 2; x < nameParts.length; x++) {
                if(x!=2){
                    groupName += " ";
                }
                groupName += nameParts[x];
            }*/
            updategroup.putExtra("idtoremove", jokeID);
            updategroup.putExtra("grouptoremovefrom", groupName);
            updategroup.putExtra("idlistoremovefromgroup", String.valueOf(new ArrayList<String>(Arrays.asList(jokeID))));
            if (firstWord.equals("Remove")) {
                try {
                    ArrayList<String> tempJokeList = new ArrayList(Arrays.asList(jokeGroups.get(groupName).toString().replace("[", "").replace("]", "").replace("null", "").split(",")));
                    tempJokeList.remove(jokeID);
                    jokeGroups.put(groupName, tempJokeList);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                /*if (!groupName.equals("")) {
                    ArrayList<String> tempJokeList = null;
                    try {
                        tempJokeList = new ArrayList(Arrays.asList(jokeGroups.get(groupName).toString().split(",")));
                        //tempJokeList = jokeGroups.get(groupName);
                        tempJokeList.remove(jokeID);
                        jokeGroups.put(groupName, tempJokeList);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }*/
                if (firstWord.equals("Delete")) {
                    Intent deleteJoke = new Intent("UPDATEJOKE");
                    try {
                        Frag2.deleteJoke(jokeID, mContext);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    deleteJoke.putExtra("instruction", "delete");
                    deleteJoke.putExtra("id", String.valueOf(jokeID));
                    mContext.sendBroadcast(deleteJoke);
                } else if (firstWord.equals("Move")) {
                    String groupToMoveTo = "";
                    String[] nameParts = Text.split(" ");
                    for (int x = 2; x < nameParts.length; x++) {
                        if(x!=2){
                            groupToMoveTo += " ";
                        }
                        groupToMoveTo += nameParts[x];
                    }
                    /*groupName = "";
                    for (int x = 2; x < nameParts.length; x++) {
                        if (x != 2) {
                            groupName += " ";
                        }
                        groupName += nameParts[x];
                    }*/
                    ArrayList<String> jokeListAdd = null;
                    try {
                        //ArrayList<String> arrayList = new ArrayList(Arrays.asList(jokeGroups.get(groupName)));
                        jokeListAdd = new ArrayList(Arrays.asList(jokeGroups.get(groupToMoveTo).toString().replace("[", "").replace("]", "").replace("null", "").split(",")));
                        //jokeListAdd = jokeGroups.get(groupName);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    jokeListAdd.add(jokeID);
                    try {
                        jokeGroups.put(groupToMoveTo, jokeListAdd);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    updategroup.putExtra("idtoremove", jokeID);
                    ArrayList<String> tempStuff = new ArrayList<String>(Arrays.asList(jokeID));
                    String tempString = String.valueOf(tempStuff);
                    updategroup.putExtra("idlistotaddtogroup", tempString);
                    updategroup.putExtra("grouptoaddto", groupToMoveTo);
                }
            }
            mContext.getSharedPreferences("_", MODE_PRIVATE).edit().putString("groupmap", String.valueOf(jokeGroups)).apply();
            updategroup.putExtra("id", jokeID);
            mContext.sendBroadcast(updategroup);
        }
    }

    interface DismissSheet {
        void DismissSheet();
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    public void setJokeID(String id) {
        this.jokeID = id;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout mainLayout;
        TextView optionText;
        DismissSheet SheetClickListener;

        public ViewHolder(View itemView, DismissSheet SheetClickListener) {
            super(itemView);
            this.SheetClickListener = SheetClickListener;
            mainLayout = itemView.findViewById(R.id.recyclableSheetLayout);
            optionText = itemView.findViewById(R.id.optiontext);
        }
    }

    public static JSONObject returnGroupMap(Context context, JSONObject jokeGroups) throws JSONException {
        JSONObject holdMap = new JSONObject(context.getSharedPreferences("_", MODE_PRIVATE).getString("groupmap", ""));
        JSONArray key = holdMap.names();
        int keyLength = key != null ? key.length() : 0;
        for (int i = 0; i < keyLength; ++i) {
            String groupName = key.getString(i);
            String value = holdMap.getString(groupName);
            ArrayList<String> jokesInGroup = new ArrayList<>(Arrays.asList(value.split(",")));
            jokeGroups.put(groupName, jokesInGroup);
        }
        return jokeGroups;
    }
}
