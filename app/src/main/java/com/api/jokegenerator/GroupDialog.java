package com.api.jokegenerator;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import org.json.JSONException;

public class GroupDialog extends AppCompatDialogFragment {
    //Creates some variables
    DialogInterface listener;
    EditText groupNameEditText;
    Button cancelButton;
    Button okButton;
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        //Creates a dialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        //Creates a view
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_group_dialog,null);
        //Sets the view for the builder
        builder.setView(view);
        //Intiializes some views
        groupNameEditText = view.findViewById(R.id.ediTextGroupName);
        cancelButton = view.findViewById(R.id.cancelButton);
        okButton = view.findViewById(R.id.okButton);
        //Sets a listener for the cancel button
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                try {
                    listener.okClicked(groupNameEditText.getText().toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        return  builder.create();
    }
    public interface DialogInterface {
        void okClicked(String groupName) throws JSONException;
    }
}
