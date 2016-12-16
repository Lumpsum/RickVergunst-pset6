package com.example.rick.rickvergunst_pset6;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Class that handles the setting or changing of an username
 */
public class UserNameActivity extends AppCompatActivity {

    //Initialize variables
    Button userNameButton;
    EditText userNameEdit;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    public static final String Name = "nameKey";
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference database;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_name);

        //Assign the shared preferences variables
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        editor = sharedPreferences.edit();

        //Assign the layout elements to variables
        userNameButton = (Button)findViewById(R.id.userNameButton);

        userNameEdit = (EditText)findViewById(R.id.userNameEdit);

        //Initialize firebase variables
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance().getReference();
        userId = firebaseUser.getUid();

        //Listener that puts the given uservalue in the shared preferences and creates the username node for this userId
        userNameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userName = userNameEdit.getText().toString();
                editor.putString(Name, userName);
                editor.commit();
                database.child("usernames").push().child(userId).setValue(userName);
                startActivity(MainActivity.newIntent(UserNameActivity.this, MainActivity.class));;
            }
        });
    }

}
