package com.api.jokegenerator;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Objects;

public class Frag2 extends Fragment {
    View view;
    LinearLayout coordinatorLayout;
    BroadcastReceiver _updateJokes;
    ;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle onSavedInstance) {
        view = inflater.inflate(R.layout.frag2_layout, container, false);
        IntentFilter intentFilter = new IntentFilter("UPDATEJOKE");
        _updateJokes = new SyncUpdate();
        getActivity().registerReceiver(_updateJokes, intentFilter);
        coordinatorLayout = view.findViewById(R.id.coordinatorLayout);
        //A JSON array that stores all the jokes,each of the jokes is a JSON.
        JSONArray jokeList = new JSONArray();
        try {
             jokeList = StoreJokesLocally.returnSavedJokes(getActivity());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("quicktest","jokeList:" + jokeList);
        return view;
    }

    public class SyncUpdate extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //String instruction = intent.getExtras().getString("instruction");
            showSync();
        }
    }

    public void showSync() {
        View rotatedCoordinator = view.findViewById(R.id.top_coordinator);
        rotatedCoordinator.setRotation(180);
        Snackbar snack = Snackbar.make(rotatedCoordinator, "Jokes Updated", Snackbar.LENGTH_LONG);
        Snackbar.SnackbarLayout view = (Snackbar.SnackbarLayout) snack.getView();
        View parent = view.getChildAt(0);
        TextView snackText = view.findViewById(com.google.android.material.R.id.snackbar_text);
        snackText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        view.setBackgroundColor(Color.parseColor("#b30000"));
        ((View)view).setBackground(ContextCompat.getDrawable(Objects.requireNonNull(getActivity()), R.drawable.circledrawable));
        view.setPadding(0, 0, 0, 0);
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) view.getLayoutParams();
        params.gravity = Gravity.CENTER_HORIZONTAL;
        params.height = (int) convertDpToPixel(48,getActivity());
        params.width = (int) convertDpToPixel(130,getActivity());
        params.setMargins(0, 0, 0, 0);
        if (parent instanceof LinearLayout) {
            ((LinearLayout) parent).setRotation(180);
        }
        view.setLayoutParams(params);
        snack.show();
    }
    public static float convertDpToPixel(float dp, Context context){
        return dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }
}