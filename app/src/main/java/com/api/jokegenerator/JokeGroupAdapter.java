package com.api.jokegenerator;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.acl.Group;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class JokeGroupAdapter extends RecyclerView.Adapter<JokeGroupAdapter.ViewHolder> {
    //Stores all the jokes to be displayed
    JSONObject groupMap;
    ArrayList<String> groupNames = new ArrayList<>();
    Context mContext;

    public JokeGroupAdapter(ArrayList<String> groupNames, JSONObject groupMap, Context context) {
        //Initializes variables
        this.groupNames = groupNames;
        this.groupMap = groupMap;
        mContext = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclablejoke, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        //Sets the jokes text
        holder.jokeText.setText(groupNames.get(position));
        if(MainActivity.currentTheme.equals("light")){holder.divider.setBackgroundColor(Color.parseColor("#CCCCCC"));}
        groupClicked(holder.mainLayout,groupNames.get(position));
    }

    @Override
    public int getItemCount() {
        return groupNames.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout mainLayout;
        TextView jokeText;
        View divider;
        public ViewHolder(View itemView) {
            super(itemView);
            mainLayout = itemView.findViewById(R.id.recyclableLayout);
            jokeText = itemView.findViewById(R.id.JokeText);
            divider = itemView.findViewById(R.id.divider);
        }
    }
    private void groupClicked(View view,String str){
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, GroupedJokes.class);
                intent.putExtra("groupname",str);
                try {
                    String thing = String.valueOf(groupMap.get(str));
                    ArrayList listToSend = new ArrayList(Arrays.asList(groupMap.get(str).toString().replace("[","").replace("]","").replace(" ","").split(",")));
                    intent.putExtra("jokesingroup",listToSend);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mContext.startActivity(intent);
                ((Activity)mContext).overridePendingTransition(R.anim.slide_in_right,R.anim.no_animation);
            }
        });
    }
}
