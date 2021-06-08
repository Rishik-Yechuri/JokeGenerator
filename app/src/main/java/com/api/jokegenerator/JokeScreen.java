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
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;

import java.io.IOException;

public class JokeScreen extends AppCompatActivity {

    //Holds the two tabs,and sets the colors
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Gets "currentTheme" and sets the theme based on that
        if(MainActivity.currentTheme.equals("dark")){setTheme(R.style.AppTheme);}else{setTheme(R.style.AppThemeLight);}
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_joke_screen);
        //SectionsPagerAdapter is used for the tbas
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        //Tabs can be added to viewpager
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setOnTouchListener((v, event) -> true);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
        //Sets the icons based on the theme
        if(MainActivity.currentTheme.equals("dark")){
            tabs.getTabAt(0).setIcon(R.drawable.generateicon);
            tabs.getTabAt(1).setIcon(R.drawable.savedicon);
            tabs.getTabAt(2).setIcon(R.drawable.groupsicon);
        }else if(MainActivity.currentTheme.equals("light")){
            tabs.getTabAt(0).setIcon(R.drawable.generateiconlight);
            tabs.getTabAt(1).setIcon(R.drawable.savediconlight);
            tabs.getTabAt(2).setIcon(R.drawable.groupsiconlight);
        }
    }
}