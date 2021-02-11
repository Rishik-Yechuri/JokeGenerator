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

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    //Stores all the jokes to be displayed
    ArrayList<String> jokes = new ArrayList<>();
    Context mContext;

    public RecyclerViewAdapter(ArrayList<String> jokes, Context context) {
        //Initializes variables
        this.jokes = jokes;
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
        holder.jokeText.setText(jokes.get(position));
    }

    @Override
    public int getItemCount() {
        return jokes.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout mainLayout;
        TextView jokeText;

        public ViewHolder(View itemView) {
            super(itemView);
            mainLayout = itemView.findViewById(R.id.recyclableLayout);
            jokeText = itemView.findViewById(R.id.JokeText);
        }
    }
}
