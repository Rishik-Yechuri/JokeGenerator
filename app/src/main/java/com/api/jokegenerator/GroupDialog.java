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

public class GroupDialog extends AppCompatDialogFragment {
    DialogInterface listener;
    EditText groupNameEditText;
    Button cancelButton;
    Button okButton;
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_group_dialog,null);
        builder.setView(view);
        groupNameEditText = view.findViewById(R.id.ediTextGroupName);
        cancelButton = view.findViewById(R.id.cancelButton);
        okButton = view.findViewById(R.id.okButton);
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
                listener.okClicked(groupNameEditText.getText().toString());
            }
        });
        return  builder.create();
    }
    public interface DialogInterface {
        void okClicked(String groupName);
    }
}
