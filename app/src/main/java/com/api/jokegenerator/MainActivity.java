package com.api.jokegenerator;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    TextView signUpView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        signUpView = findViewById(R.id.logInView);
        signUpView.setOnClickListener(new ViewClicked());
    }
    class ViewClicked implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if(v.getId() == R.id.logInView){
                Intent intent = new Intent(getApplicationContext(),SignUp.class);
                startActivity(intent);
            }
        }
    }
}