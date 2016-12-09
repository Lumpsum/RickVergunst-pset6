package com.example.rick.rickvergunst_pset6;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class UserNameActivity extends AppCompatActivity {

    Button userNameButton;
    EditText userNameEdit;
    private SharedPreferences preferenceSettings;
    private SharedPreferences.Editor preferenceEditor;
    private static final int PREFERENCE_MODE_PRIVATE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_name);

        preferenceSettings = getPreferences(PREFERENCE_MODE_PRIVATE);
        preferenceEditor = preferenceSettings.edit();

        userNameButton = (Button)findViewById(R.id.userNameButton);
        userNameEdit = (EditText)findViewById(R.id.userNameEdit);

        userNameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Test", "Test" + userNameEdit.getText().toString());
                preferenceEditor.putString("name", userNameEdit.getText().toString());
                preferenceEditor.apply();
                preferenceEditor.commit();
                startActivity(MainActivity.newIntent(UserNameActivity.this, MainActivity.class));;
            }
        });
    }
}
