package com.api.jokegenerator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

public class SignUp extends AppCompatActivity {
    private FirebaseAuth mAuth;
    TextView logInView;
    EditText editTextTextEmailAddress;
    EditText editTextTextPassword;
    Button signUpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        mAuth = FirebaseAuth.getInstance();
        logInView = findViewById(R.id.logInView);
        logInView.setOnClickListener(new LogInClicked());
        editTextTextEmailAddress = findViewById(R.id.editTextEmailAddressLogIn);
        editTextTextPassword = findViewById(R.id.editTextPasswordLogIn);
        signUpButton = findViewById(R.id.signUpButton);
        signUpButton.setOnClickListener(new LogInClicked());
    }

    class LogInClicked implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.logInView) {
                //Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                //startActivity(intent);
                finish();
            } else if (v.getId() == R.id.signUpButton) {
                registerUser();
            }
        }
    }

    public void registerUser() {
        String email = editTextTextEmailAddress.getText().toString();
        String password = editTextTextPassword.getText().toString();
        if (email.isEmpty()) {
            editTextTextEmailAddress.setError("Email is required");
            editTextTextEmailAddress.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextTextEmailAddress.setError("Enter a valid email");
            editTextTextEmailAddress.requestFocus();
            return;
        }
        if (password.length() < 10) {
            editTextTextPassword.setError("Password must be at least 10 characters long");
            editTextTextPassword.requestFocus();
            return;
        }
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Intent intent = new Intent(getApplicationContext(),JokeScreen.class);
                    startActivity(intent);
                    finish();
                }else{
                  if(task.getException() instanceof FirebaseAuthUserCollisionException){
                      Toast.makeText(getApplicationContext(),"email already registered",Toast.LENGTH_SHORT).show();
                  }
                }
            }
        });
    }
}