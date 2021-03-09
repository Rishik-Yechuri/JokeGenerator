package com.api.jokegenerator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.google.android.material.tabs.TabLayout;

import java.io.IOException;

public class JokeScreen extends AppCompatActivity {

    //Holds the two tabs,and sets the colors
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_joke_screen);
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setOnTouchListener((v, event) -> true);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
        tabs.setTabTextColors(Color.parseColor("#808080"), Color.parseColor("#FFFFFF"));
        int color = Color.parseColor("#b30000");
        tabs.setSelectedTabIndicatorColor(color);
        tabs.getTabAt(0).setIcon(R.drawable.generateicon);
        tabs.getTabAt(1).setIcon(R.drawable.savedicon);
        tabs.getTabAt(2).setIcon(R.drawable.groupsicon);
    }
}