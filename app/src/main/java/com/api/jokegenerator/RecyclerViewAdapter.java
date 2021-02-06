package com.api.jokegenerator;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>{


    ArrayList<String> jokes = new ArrayList<>();
    Context mContext;
    public RecyclerViewAdapter(ArrayList<String> jokes,Context context){
        this.jokes = jokes;
        mContext = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclablejoke,parent,false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d("recycle","onBind Called");
        if(!jokes.contains(jokes.get(position))){
            holder.jokeText.setText(jokes.get(position));
        }
        //holder.jokeText.setText("HEY");
        /*holder.mainLayout.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Toast.makeText(mContext,"Something clicked",Toast.LENGTH_SHORT).show();
            }
        });*/
    }

    @Override
    public int getItemCount() {
        Log.d("recycle","Joke Size:" + jokes.size());
        Log.d("recycle","Jokes:" + jokes.toString());
        return jokes.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        LinearLayout mainLayout;
        TextView jokeText;
        public ViewHolder(View itemView){
            super(itemView);
            mainLayout = itemView.findViewById(R.id.recyclableLayout);
            jokeText = itemView.findViewById(R.id.JokeText);
        }
    }
}
