package com.api.jokegenerator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;

import java.util.Objects;

public class Frag2 extends Fragment {
    View view;
    BroadcastReceiver _updateJokes;;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle onSavedInstance) {
        view = inflater.inflate(R.layout.frag2_layout, container, false);
        IntentFilter intentFilter = new IntentFilter("UPDATEJOKE");
        _updateJokes = new SyncUpdate();
        getActivity().registerReceiver(_updateJokes, intentFilter);
        return view;
    }
    public class SyncUpdate extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //String instruction = intent.getExtras().getString("instruction");
            Snackbar snack = Snackbar.make(view.findViewById(android.R.id.content), "Sup mate", Snackbar.LENGTH_LONG);
            View view = snack.getView();
            FrameLayout.LayoutParams params =(FrameLayout.LayoutParams)view.getLayoutParams();
            params.gravity = Gravity.TOP;
            view.setLayoutParams(params);
            snack.show();
        }
    }
}