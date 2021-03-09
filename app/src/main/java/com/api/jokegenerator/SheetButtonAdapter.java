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

import java.lang.reflect.Array;
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
    HashMap<String, ArrayList<String>> jokeGroups = new HashMap<>();
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

    View.OnClickListener removeClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            returnGroupMap();
            Toast.makeText(mContext, "Remove Clicked:", Toast.LENGTH_SHORT).show();
            String groupName = "";
            String[] nameParts = v.toString().split(" ");
            for (int x = 2; x <= v.toString().split(" ").length; x++) {
                groupName += nameParts[x];
            }
            ArrayList<String> tempJokeList = jokeGroups.get(groupName);
            tempJokeList.remove(jokeID);
            jokeGroups.put(groupName, tempJokeList);
            Log.d("sheetslide", "jokeGroups:" + jokeGroups);
            mContext.getSharedPreferences("_", MODE_PRIVATE).edit().putString("groupmap", String.valueOf(jokeGroups)).apply();
        }
    };

    public class OptionClicked implements View.OnClickListener {

        String Text;

        public OptionClicked(String Text) {
            this.Text = Text;
        }

        @Override
        public void onClick(View v) {
            String firstWord = Text.split(" ")[0];
            SheetClickListener.DismissSheet();
            HashMap<String, ArrayList<String>> groupMap = returnGroupMap();
            String groupName = "";
            String[] nameParts = Text.split(" ");
          /*  for (int x = 2; x < nameParts.length; x++) {
                groupName += nameParts[x];
            }*/
           /* if (Text.split(" ")[0].equals("Remove")) {
                tempJokeList.remove(jokeID);
                jokeGroups.put(groupName, tempJokeList);
                Log.d("sheetslide", "jokeGroups:" + jokeGroups);
                mContext.getSharedPreferences("_", MODE_PRIVATE).edit().putString("groupmap", String.valueOf(jokeGroups)).apply();
            } else if (Text.split(" ")[0].equals("Delete")) {
                Toast.makeText(mContext, "Delete Clicked", Toast.LENGTH_LONG).show();
            }*/
            if (firstWord.equals("Remove")) {
                for (int x = 2; x < nameParts.length; x++) {
                    groupName += nameParts[x];
                }
                ArrayList<String> tempJokeList = jokeGroups.get(groupName);
                Toast.makeText(mContext, "Remove Clicked:", Toast.LENGTH_SHORT).show();
                tempJokeList.remove(jokeID);
                jokeGroups.put(groupName, tempJokeList);
                mContext.getSharedPreferences("_", MODE_PRIVATE).edit().putString("groupmap", String.valueOf(jokeGroups)).apply();
            } else if (firstWord.equals("Delete")) {
                Intent deleteJoke = new Intent("UPDATEJOKE");
                try {
                    Frag2.deleteJoke(jokeID,mContext);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                deleteJoke.putExtra("instruction","delete");
                deleteJoke.putExtra("id",String.valueOf(jokeID));
                mContext.sendBroadcast(deleteJoke);
            } else if (firstWord.equals("Move")) {
                for (HashMap.Entry<String, ArrayList<String>> entry : groupMap.entrySet()) {
                    String key = entry.getKey();
                    ArrayList<String> jokeIDs = entry.getValue();
                    if(jokeIDs.contains(jokeID)){
                        groupName = key;
                    }
                }
                if(!groupName.equals("")){
                    ArrayList<String> tempJokeList = jokeGroups.get(groupName);
                    tempJokeList.remove(jokeID);
                    jokeGroups.put(groupName, tempJokeList);
                }
                groupName = "";
                for (int x = 2; x < nameParts.length; x++) {
                    groupName += nameParts[x];
                }
                ArrayList<String> jokeListAdd = jokeGroups.get(groupName);
                jokeListAdd.add(jokeID);
                jokeGroups.put(groupName, jokeListAdd);
                mContext.getSharedPreferences("_", MODE_PRIVATE).edit().putString("groupmap", String.valueOf(jokeGroups)).apply();
            }
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
            // mainLayout.setOnClickListener(removeClicked);
            optionText = itemView.findViewById(R.id.optiontext);
        }
    }

    public HashMap<String, ArrayList<String>> returnGroupMap() {
        String holdMap = mContext.getSharedPreferences("_", MODE_PRIVATE).getString("groupmap", "");
        String[] splitMap = holdMap.split("], ");
        for (int x = 0; x < splitMap.length; x++) {
            String filteredString = splitMap[x].replaceAll("\\[", "").replaceAll("]", "").replaceAll("\\{", "");
            filteredString = filteredString.replace("}", "");
            String[] basicSplit = filteredString.split("=");
            String groupName = basicSplit[0];
            String[] tempGroupIDs;
            ArrayList<String> jokesInGroup = new ArrayList<>();
            if (basicSplit.length > 1) {
                tempGroupIDs = basicSplit[1].split(", ");
                jokesInGroup = new ArrayList<>(Arrays.asList(tempGroupIDs));
            }
            jokeGroups.put(groupName, jokesInGroup);
        }
        return jokeGroups;
    }
}
