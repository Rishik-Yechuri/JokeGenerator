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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;

public class SignUp extends AppCompatActivity {
    //Used for authentication
    private FirebaseAuth mAuth;
    //Sets up Views
    TextView logInView;
    EditText editTextTextEmailAddress;
    EditText editTextTextPassword;
    Button signUpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        //Initializes mAuth
        mAuth = FirebaseAuth.getInstance();
        //Initializes views and sets onclicklisteners
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
            //If log in is pressed it exits
            if (v.getId() == R.id.logInView) {
                finish();
            }
            //If the user clicks sign up,they get registered
            else if (v.getId() == R.id.signUpButton) {
                registerUser();
            }
        }
    }

    public void registerUser() {
        //Gets input from fields
        String email = editTextTextEmailAddress.getText().toString();
        String password = editTextTextPassword.getText().toString();
        //Makes sure email isn't empty
        if (email.isEmpty()) {
            editTextTextEmailAddress.setError("Email is required");
            editTextTextEmailAddress.requestFocus();
            return;
        }
        //Makes sure the email is valid
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextTextEmailAddress.setError("Enter a valid email");
            editTextTextEmailAddress.requestFocus();
            return;
        }
        //Checks if the password is long enough
        if (password.length() < 10) {
            editTextTextPassword.setError("Password must be at least 10 characters long");
            editTextTextPassword.requestFocus();
            return;
        }
        //Once all the checks are passed,a new user is created
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Intent intent = new Intent(getApplicationContext(),JokeScreen.class);
                    FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
                    mUser.getIdToken(true)
                            .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                                public void onComplete(@NonNull Task<GetTokenResult> task) {
                                    if (task.isSuccessful()) {
                                        //Subscribes to topic with the name of token
                                        FirebaseMessaging.getInstance().subscribeToTopic(task.getResult().getToken());
                                    } else {
                                    }
                                }
                            });
                    startActivity(intent);
                    finish();
                }else{
                    //If the user is already registered,displays a message
                  if(task.getException() instanceof FirebaseAuthUserCollisionException){
                      Toast.makeText(getApplicationContext(),"email already registered",Toast.LENGTH_SHORT).show();
                  }
                }
            }
        });
    }
}