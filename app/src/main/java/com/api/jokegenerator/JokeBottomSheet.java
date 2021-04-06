package com.api.jokegenerator;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static android.content.Context.MODE_PRIVATE;

public class JokeBottomSheet extends BottomSheetDialogFragment implements SheetButtonAdapter.DismissSheet {
    View v;
    RecyclerView sheetRecyclerView;
    SheetButtonAdapter sheetAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable
            ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.jokebottomsheet,
                container, false);
        try {
            initializeSheetRecycler();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //super.dismiss();
        //Button okButton = v.findViewById(R.id.okbutton);
        //okButton.setOnClickListener(jokeClicked);

        return v;
    }

    private void initializeSheetRecycler() throws JSONException {
        if(MainActivity.currentTheme.equals("dark")){getContext().setTheme(R.style.AppTheme);}else{getContext().setTheme(R.style.AppThemeLight);}
        sheetRecyclerView = v.findViewById(R.id.optionSheetRecyclerView);
        sheetRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        JSONObject jokeGroups = new JSONObject(getContext().getSharedPreferences("_", MODE_PRIVATE).getString("groupmap", ""));
        JSONArray key = jokeGroups.names();
        String currentGroup = "";
        ArrayList<String> otherGroups = new ArrayList<>();
        int keyLength = key != null ? key.length() : 0;
        for (int i = 0; i < keyLength; ++i) {
            String groupName = key.getString(i);
            String value = jokeGroups.getString(groupName);
            ArrayList<String> jokesInGroup = new ArrayList<>(Arrays.asList(value.replace("[", "").replace("]", "").split(",")));
            for (int x = 0; x < jokesInGroup.size(); x++) {
                String tempString = jokesInGroup.get(x);
                tempString = tempString.replace(" ", "");
                jokesInGroup.remove(x);
                jokesInGroup.add(x, tempString);
            }
            if (jokesInGroup.contains(String.valueOf(this.getArguments().getString("id")))) {
                currentGroup = groupName;
            } else if (!groupName.equals("")) {
                otherGroups.add(groupName);
            }
        }
        Log.d("hashcheck", "holdmap:" + jokeGroups);
        ArrayList<String> tempArraylist = new ArrayList<>();
        if (!currentGroup.equals("")) {
            tempArraylist.add("Remove from " + currentGroup);
        }
        tempArraylist.add("Delete");
        for (int x = 0; x < otherGroups.size(); x++) {
            tempArraylist.add("Move to " + otherGroups.get(x));
        }
        sheetAdapter = new SheetButtonAdapter(tempArraylist, getContext(), this);
        sheetAdapter.setJokeID(this.getArguments().getString("id"));
        sheetRecyclerView.setAdapter(sheetAdapter);
    }

    private View.OnClickListener jokeClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Toast.makeText(getContext(), "Sheet Clicked", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    public void DismissSheet() {
        super.dismiss();
    }
}