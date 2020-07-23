package com.api.jokegenerator;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class SignUp extends AppCompatActivity {
    TextView logInView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        logInView = findViewById(R.id.logInView);
        logInView.setOnClickListener(new LogInClicked());
    }
    class LogInClicked implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if(v.getId() == R.id.logInView){
                //Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                //startActivity(intent);
                finish();
            }
        }
    }
}