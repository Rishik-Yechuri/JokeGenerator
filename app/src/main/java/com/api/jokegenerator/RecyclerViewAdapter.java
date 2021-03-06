package com.api.jokegenerator;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    //Stores all the jokes to be displayed
    JSONArray jokes = new JSONArray();
    Context mContext;
    FragmentManager parentFrag;

    public RecyclerViewAdapter(JSONArray jokes, Context context, FragmentManager parentFrag) {
        //Initializes variables
        this.jokes = jokes;
        this.parentFrag = parentFrag;
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
        String id = "";
        try {
            String textOfJoke = Frag2.returnJokeString(String.valueOf(jokes.get(position)));
            JSONObject tempObj = (JSONObject) jokes.get(position);
            id = tempObj.getString("id");
            holder.jokeText.setText(textOfJoke);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        holder.mainLayout.setOnLongClickListener(new jokeClicked(id));
    }

    /*  private View.OnLongClickListener jokeClicked = new View.OnLongClickListener() {
          int id;
          public jokeClicked(String id){}
          @Override
          public boolean onLongClick(View v) {
              JokeBottomSheet bottomSheet = new JokeBottomSheet();
              bottomSheet.setArguments();
              bottomSheet.show(parentFrag,"MainSheet");
              return false;
          }
      };*/
    public class jokeClicked implements View.OnLongClickListener {
        String id;

        public jokeClicked(String id) {
            this.id = id;
        }

        @Override
        public boolean onLongClick(View v) {
            JokeBottomSheet bottomSheet = new JokeBottomSheet();
            Bundle bundle = new Bundle();
            bundle.putString("id", String.valueOf(id));
            bottomSheet.setArguments(bundle);
            bottomSheet.show(parentFrag, "MainSheet");
            return false;
        }

    }

    ;

    @Override
    public int getItemCount() {
        return jokes.length();
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
