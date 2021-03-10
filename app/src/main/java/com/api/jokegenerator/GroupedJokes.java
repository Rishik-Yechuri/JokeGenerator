package com.api.jokegenerator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.graphics.Color;
import android.os.Bundle;

public class GroupedJokes extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grouped_jokes);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryVariant));
        setSupportActionBar(toolbar);
        Bundle extras = getIntent().getExtras();
        getSupportActionBar().setTitle(extras.getString("groupname"));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportFragmentManager().addOnBackStackChangedListener(this);
    }

    @Override
    public boolean onSupportNavigateUp() {
        return goBack();
    }
    @Override
    public void onBackPressed() {
        goBack();
    }
    public boolean goBack() {
        getSupportFragmentManager().popBackStack();
        finish();
        overridePendingTransition(0, R.anim.slide_out_right);
        return true;
    }
}