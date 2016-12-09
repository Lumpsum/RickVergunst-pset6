package com.example.rick.rickvergunst_pset6;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignUpActivity extends AppCompatActivity {

    // Initialize variables
    protected EditText userEdit;
    protected EditText passEdit;
    protected Button signUpButton;
    protected TextView logInText;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Firebase variables
        firebaseAuth = FirebaseAuth.getInstance();

        userEdit = (EditText)findViewById(R.id.signUpUserEdit);
        passEdit = (EditText)findViewById(R.id.signUpPassEdit);
        signUpButton = (Button)findViewById(R.id.signUpButton);
        logInText = (TextView)findViewById(R.id.logInText);

        // Start log up activity
        logInText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(MainActivity.newIntent(SignUpActivity.this, LogInActivity.class));
            }
        });

        // On click listener for the log in button
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Retrieve the user input and remove any whitespace
                String user = userEdit.getText().toString();
                String pass = passEdit.getText().toString();

                user = user.trim();
                pass = pass.trim();

                // If either field is empty, error message is shown
                if (user.isEmpty() || pass.isEmpty()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
                    builder.setMessage(R.string.logInErrorMes)
                            .setTitle(R.string.logInErrorTit)
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                else {
                    // Tries to create a user with the given user values
                    firebaseAuth.createUserWithEmailAndPassword(user, pass)
                            .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {

                                    // If the creation is succesful, return to main activity
                                    if (task.isSuccessful()) {
                                        startActivity(MainActivity.newIntent(SignUpActivity.this, MainActivity.class));
                                    }
                                    // Else error is shown
                                    else {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
                                        builder.setMessage(task.getException().getMessage())
                                                .setTitle(R.string.logInErrorTit)
                                                .setPositiveButton(android.R.string.ok, null);
                                        AlertDialog dialog = builder.create();
                                        dialog.show();
                                    }
                                }
                            });
                }
            }
        });
    }
}
