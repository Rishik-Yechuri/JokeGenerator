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
        initializeSheetRecycler();
        //super.dismiss();
        //Button okButton = v.findViewById(R.id.okbutton);
        //okButton.setOnClickListener(jokeClicked);

        return v;
    }

    private void initializeSheetRecycler() {
        sheetRecyclerView = v.findViewById(R.id.optionSheetRecyclerView);
        sheetRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        HashMap<String, ArrayList<String>> jokeGroups = new HashMap<>();
        String currentGroup = "";
        ArrayList<String> otherGroups = new ArrayList<>();
      /*HashMap<String,ArrayList<Integer>> holdStuff =  new HashMap<String, ArrayList<Integer>>();
        holdStuff.put("Funny",new ArrayList<Integer>(Arrays.asList(268)));
        holdStuff.put("Hey",new ArrayList<Integer>(Arrays.asList(59,69)));
        holdStuff.put("Meme",new ArrayList<Integer>(Arrays.asList(93,222)));
        getContext().getSharedPreferences("_",MODE_PRIVATE).edit().putString("groupmap", String.valueOf(holdStuff)).apply();*/
        String holdMap = getContext().getSharedPreferences("_", MODE_PRIVATE).getString("groupmap", "");
        String[] splitMap = holdMap.split("], ");
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
            jokeGroups.put(groupName, jokesInGroup);
            if (jokesInGroup.contains(String.valueOf(this.getArguments().getString("id")))) {
                currentGroup = groupName;
            } else if(!groupName.equals("")){
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
        sheetAdapter = new SheetButtonAdapter(tempArraylist, getContext(),this);
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