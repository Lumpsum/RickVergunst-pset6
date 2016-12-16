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

/**
 * Handles the log in of users and allows for navigation to the sign up, furthermore the page where users will land first
 */
public class LogInActivity extends AppCompatActivity {

    // Initialize variables
    protected EditText userEdit;
    protected EditText passEdit;
    protected Button logInButton;
    protected TextView signUpText;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        // Firebase variables
        firebaseAuth = FirebaseAuth.getInstance();

        //Assign the layout elements to variables
        userEdit = (EditText)findViewById(R.id.logInUserEdit);
        passEdit = (EditText)findViewById(R.id.logInPassEdit);

        logInButton = (Button)findViewById(R.id.logInButton);

        signUpText = (TextView)findViewById(R.id.signUpText);

        // Start sign up activity
        signUpText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(MainActivity.newIntent(LogInActivity.this, SignUpActivity.class));
            }
        });

        // On click listener for the log in button
        logInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Retrieve the user input and remove any whitespace
                String user = userEdit.getText().toString();
                String pass = passEdit.getText().toString();

                //Trims the data for whitespace
                user = user.trim();
                pass = pass.trim();

                // If either field is empty, error message is shown
                if (user.isEmpty() || pass.isEmpty()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(LogInActivity.this);
                    builder.setMessage(R.string.logInErrorMes)
                            .setTitle(R.string.logInErrorTit)
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                else {
                    // Tries to sign in with the given user values
                    firebaseAuth.signInWithEmailAndPassword(user, pass)
                            .addOnCompleteListener(LogInActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {

                                    // If log in is succesful, return to main activity
                                    if (task.isSuccessful()) {
                                        startActivity(MainActivity.newIntent(LogInActivity.this, MainActivity.class));
                                    }
                                    // Else error is shown
                                    else {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(LogInActivity.this);
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