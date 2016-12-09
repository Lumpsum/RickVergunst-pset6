package com.example.rick.rickvergunst_pset6;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static com.example.rick.rickvergunst_pset6.MainActivity.newIntent;

public class SearchPage extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    protected Button logOutButton;
    protected Button toSearchPageButton;
    protected Button homeButton;

    Button searchPageButton;
    EditText searchPageEdit;
    ListView searchPageListView;
    String apiKey;
    ArrayList<String> results;
    ArrayAdapter<String> adapter;
    Spinner searchPageSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_page);

        logOutButton = (Button)findViewById(R.id.logOutButton);
        toSearchPageButton = (Button)findViewById(R.id.toSearchPageButton);
        homeButton = (Button)findViewById(R.id.toHomeButton);

        searchPageButton = (Button)findViewById(R.id.searchPageButton);
        searchPageEdit = (EditText)findViewById(R.id.searchPageEdit);
        searchPageListView = (ListView)findViewById(R.id.searchPageListView);
        searchPageSpinner = (Spinner)findViewById(R.id.searchPageSpinner);
        String[] dropdown = new String[]{"Artist", "Album", "Tracks"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, dropdown);
        searchPageSpinner.setAdapter(spinnerAdapter);

        apiKey = "eb4c34f0485ffa97337735fa01fe3b36";

        results = new ArrayList<String>();

        // Assign firebase variables
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        // If user is not logged in, launch log in activity
        if (firebaseUser == null) {
            startActivity(newIntent(this, LogInActivity.class));
        }

        adapter = new ArrayAdapter<String>(this, R.layout.list_item, results);
        searchPageListView.setAdapter(adapter);

        searchPageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userValue = searchPageEdit.getText().toString();
                results.clear();
                AsyncTask<String, String, StringBuilder> aSyncTask = new ASyncTask();
                String spinnerValue = searchPageSpinner.getSelectedItem().toString();
                String selected = "";
                switch (spinnerValue) {
                    case "Artist":
                        selected = "artist";
                        break;
                    case "Album":
                        selected = "album";
                        break;
                    case "Tracks":
                        selected = "track";
                        break;
                }
                MainActivity.fillArray(userValue, "search", selected, "results", selected + "matches", selected, results);

                searchPageEdit.setText(null);
                adapter.notifyDataSetChanged();
            }
        });

        searchPageListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView textView = (TextView)view.findViewById(R.id.textview);
                String text = textView.getText().toString();
                String spinnerValue = searchPageSpinner.getSelectedItem().toString();
                Intent intent = null;
                switch (spinnerValue) {
                    case "Artist":
                        intent = MainActivity.newIntent(SearchPage.this, ArtistInfo.class);
                        break;
                    case "Album":
                        intent = MainActivity.newIntent(SearchPage.this, AlbumInfo.class);
                        break;
                    case "Tracks":
                        intent = MainActivity.newIntent(SearchPage.this, TrackInfo.class);
                        break;
                }
                intent.putExtra("name", text);
                startActivity(intent);
            }
        });

        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(MainActivity.newIntent(SearchPage.this, MainActivity.class));;
            }
        });

        logOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(MainActivity.signOut(SearchPage.this, firebaseAuth));
            }
        });

        toSearchPageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(MainActivity.newIntent(SearchPage.this, SearchPage.class));
            }
        });
    }
}
