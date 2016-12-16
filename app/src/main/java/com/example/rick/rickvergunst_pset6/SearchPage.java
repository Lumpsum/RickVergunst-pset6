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

    //Initailize variables
    protected Button logOutButton;
    protected Button toSearchPageButton;
    protected Button homeButton;
    Button searchPageButton;
    EditText searchPageEdit;
    ListView searchPageListView;
    String spinnerValue;
    ArrayList<String> results;
    ArrayAdapter<String> adapter;
    Spinner searchPageSpinner;
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_page);

        //Assign the layout elements to variables
        logOutButton = (Button)findViewById(R.id.logOutButton);
        toSearchPageButton = (Button)findViewById(R.id.toSearchPageButton);
        homeButton = (Button)findViewById(R.id.toHomeButton);

        searchPageButton = (Button)findViewById(R.id.searchPageButton);
        searchPageEdit = (EditText)findViewById(R.id.searchPageEdit);
        searchPageListView = (ListView)findViewById(R.id.searchPageListView);
        searchPageSpinner = (Spinner)findViewById(R.id.searchPageSpinner);

        //Fill the array with items for the spinner
        String[] dropdown = new String[]{"Artist", "Album", "Tracks"};

        //Iniate the adapter for the spinner with the array
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, dropdown);

        //Assign the adapter to the spinner
        searchPageSpinner.setAdapter(spinnerAdapter);

        //Initiate the array that contains the search results
        results = new ArrayList<String>();

        // Assign firebase variables
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        // If user is not logged in, launch log in activity
        if (firebaseUser == null) {
            startActivity(newIntent(this, LogInActivity.class));
        }

        //Checks whether the activity is called from orientation change and fills the array accordingly
        if (savedInstanceState != null) {
            results = savedInstanceState.getStringArrayList("results");
        }

        //Iniate the adapter for the search results
        adapter = new ArrayAdapter<String>(this, R.layout.list_item, results);


        //Assign the adapter to the search listview
        searchPageListView.setAdapter(adapter);

        //On click listener that handles based on the given query and chosen spinner
        searchPageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userValue = searchPageEdit.getText().toString();
                results.clear();
                AsyncTask<String, String, StringBuilder> aSyncTask = new ASyncTask();
                spinnerValue = searchPageSpinner.getSelectedItem().toString();
                String selected = "";

                //Switch that adjusts the variables based on the spinner value
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

                //Resets the editText
                searchPageEdit.setText(null);
                adapter.notifyDataSetChanged();
            }
        });

        //Listener that acts based on the given results
        searchPageListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView textView = (TextView)view.findViewById(R.id.textview);
                String text = textView.getText().toString();
                Intent intent = null;

                //Switch that creates an intent based on the spinner value
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

        //Basic listeners for the bottom menu
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        /**
         * Retrieves the search results and passes it to the new activity if the rotation is changed
         */
        super.onSaveInstanceState(outState);
        outState.putStringArrayList("results", results);
    }
}
