package com.api.jokegenerator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;

import org.json.JSONException;

public class MainActivity extends AppCompatActivity {
    TextView signUpView;
    Button signUpButton;
    EditText editTextEmailAddressLogIn;
    EditText editTextPasswordLogIn;
    FirebaseAuth mAuth;
    FirebaseUser user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // User is signed in
            Intent i = new Intent(MainActivity.this, JokeScreen.class);
            startActivity(i);
            finish();
        }
        signUpView = findViewById(R.id.logInView);
        signUpView.setOnClickListener(new ViewClicked());
        signUpButton = findViewById(R.id.signUpButton);
        signUpButton.setOnClickListener(new ViewClicked());
        mAuth = FirebaseAuth.getInstance();
        editTextEmailAddressLogIn = findViewById(R.id.editTextEmailAddressLogIn);
        editTextPasswordLogIn = findViewById(R.id.editTextPasswordLogIn);
    }
    class ViewClicked implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if(v.getId() == R.id.logInView){
                Intent intent = new Intent(getApplicationContext(),SignUp.class);
                startActivity(intent);
            }else if(v.getId() == R.id.signUpButton){
                logIn();
            }
        }
    }
    public void logIn(){
        String email = editTextEmailAddressLogIn.getText().toString();
        String password = editTextPasswordLogIn.getText().toString();
        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Intent intent = new Intent(getApplicationContext(),JokeScreen.class);
                    FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
                    mUser.getIdToken(true)
                            .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                                public void onComplete(@NonNull Task<GetTokenResult> task) {
                                    if (task.isSuccessful()) {
                                        //[0] = task.getResult().getToken();
                                        Log.d("digitaldash","UID val:" + task.getResult().getToken());
                                        String topicToSubscribe = task.getResult().getToken().split("\\.")[0];
                                        FirebaseMessaging.getInstance().subscribeToTopic(topicToSubscribe);
                                        Toast.makeText(getApplicationContext(), "Subscirbed", Toast.LENGTH_SHORT).show();
                                    } else {
                                    }
                                }
                            });
                    //FirebaseMessaging.getInstance().subscribeToTopic(mUser.getIdToken(true));
                    startActivity(intent);
                    finish();
                }else{
                    Toast.makeText(getApplicationContext(),task.getException().getMessage(),Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}