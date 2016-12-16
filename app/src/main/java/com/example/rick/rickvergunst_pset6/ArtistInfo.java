package com.example.rick.rickvergunst_pset6;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.*;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import static com.example.rick.rickvergunst_pset6.LoadFonts.retrieveTypeFace;

/**
 * Shows artist info based on the last.fm api and user data based on a firebase server
 */
public class ArtistInfo extends AppCompatActivity {

    //Initialize variables
    protected Button logOutButton;
    protected Button searchPageButton;
    protected Button homeButton;
    ListView artistInfoAlbumListView;
    ListView artistInfoTrackListView;
    ListView artistInfoSimilarListView;
    ListView artistInfoSimilarUsers;
    TextView artistInfoNameText;
    Button artistAddButton;
    String artist;
    ArrayList<String> topAlbums;
    ArrayList<String> topTracks;
    ArrayList<String> similarArtist;
    ArrayList<String> similarUsers;
    ArrayList<String> similarUsersNames;
    ArrayList<String> similarUsersNamesId;
    ArrayAdapter<String> topAlbumAdapter;
    ArrayAdapter<String> topTrackAdapter;
    ArrayAdapter<String> similarArtistAdapater;
    ArrayAdapter<String> similarUsersAdapter;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference database;
    private String userId;
    Query ref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_info);

        //Retrieve data from source
        Intent intent = getIntent();
        artist = intent.getStringExtra("name");

        //Initalize firebase variables
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance().getReference();
        userId = firebaseUser.getUid();

        //Assign the different layout elements
        logOutButton = (Button)findViewById(R.id.logOutButton);
        searchPageButton = (Button)findViewById(R.id.toSearchPageButton);
        homeButton = (Button)findViewById(R.id.toHomeButton);
        artistAddButton = (Button)findViewById(R.id.artistAddButton);

        artistInfoAlbumListView = (ListView)findViewById(R.id.artistTopAlbums);
        artistInfoTrackListView = (ListView)findViewById(R.id.artistTopTracks);
        artistInfoSimilarListView = (ListView)findViewById(R.id.artistSimilarListView);
        artistInfoSimilarUsers = (ListView)findViewById(R.id.artistSimilarUsersList);

        artistInfoNameText = (TextView)findViewById(R.id.artistNameText);

        //Set text and font of the textview
        artistInfoNameText.setText(artist);
        artistInfoNameText.setTypeface(retrieveTypeFace(this, 0));

        //Set the button text according to whether the node is a favourite or not
        ref = database.child("users").child(userId).child("favourites").orderByChild("artist").equalTo(artist);
        MainActivity.setButtonText(ref, artistAddButton);

        //Initialize different arraylists
        topAlbums = new ArrayList<String>();
        topTracks = new ArrayList<String>();
        similarArtist = new ArrayList<String>();
        similarUsers = new ArrayList<String>();
        similarUsersNames = new ArrayList<String>();
        similarUsersNamesId = new ArrayList<String>();

        //Initialize the adapter for the listviews
        topAlbumAdapter = new ArrayAdapter<String>(this, R.layout.list_item, topAlbums);
        topTrackAdapter = new ArrayAdapter<String>(this, R.layout.list_item, topTracks);
        similarArtistAdapater = new ArrayAdapter<String>(this, R.layout.list_item, similarArtist);
        similarUsersAdapter = new ArrayAdapter<String>(this, R.layout.list_item, similarUsersNames);

        //Fill the adapter arrays with data from the api
        MainActivity.fillArray(artist, "getTopAlbums", "album", "topalbums", "", "artist", topAlbums);
        MainActivity.fillArray(artist, "getTopTracks", "track", "toptracks", "", "artist", topTracks);
        MainActivity.fillArray(artist, "getSimilar", "artist", "similarartists", "", "artist", similarArtist);

        //Retrieve the array data from the firebase, instead of api
        findUsers(new Runnable() {
            @Override
            public void run() {
                MainActivity.setUserArrays(database.child("usernames"), similarUsers, similarUsersNames, similarUsersNamesId, similarUsersAdapter);
            }
        });

        //Assign the adapters to the listview
        artistInfoAlbumListView.setAdapter(topAlbumAdapter);
        artistInfoTrackListView.setAdapter(topTrackAdapter);
        artistInfoSimilarListView.setAdapter(similarArtistAdapater);
        artistInfoSimilarUsers.setAdapter(similarUsersAdapter);

        //Set the on click listeners for the different listviews
        onListItemClick(artistInfoSimilarListView, ArtistInfo.this, ArtistInfo.class, "");
        onListItemClick(artistInfoAlbumListView, ArtistInfo.this, AlbumInfo.class, artist + "-");
        onListItemClick(artistInfoTrackListView, ArtistInfo.this, TrackInfo.class, artist + "-");
        artistInfoSimilarUsers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = MainActivity.newIntent(ArtistInfo.this, UserInfo.class);
                intent.putExtra("name", similarUsersNamesId.get(position));
                startActivity(intent);
            }
        });

        //Either adds or removes the artist from the user favourites
        artistAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Adds the artist to the favourites and adds the userid to the artist node
                if (artistAddButton.getText().equals("Add")) {
                    database.child("users").child(userId).child("favourites").push().child("artist").setValue(artist);
                    database.child("artist").child(artist).push().child("user").setValue(userId);
                    artistAddButton.setText("Remove");
                }

                //Removes the artist from the favourites and removes the userid from the artist node
                else {
                    ref = database.child("users").child(userId).child("favourites")
                            .orderByChild("artist").equalTo(artist);
                    MainActivity.removeChildFireBase(ref);
                    ref = database.child("artist").child(artist)
                            .orderByChild("user").equalTo(userId);
                    MainActivity.removeChildFireBase(ref);
                    artistAddButton.setText("Add");
                }
            }
        });

        //General button handlers for the bottom menu
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(MainActivity.newIntent(ArtistInfo.this, MainActivity.class));;
            }
        });

        logOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(MainActivity.signOut(ArtistInfo.this, firebaseAuth));
            }
        });

        searchPageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(MainActivity.newIntent(ArtistInfo.this, SearchPage.class));
            }
        });
    }

    private void onListItemClick(ListView lv, final Context context, final Class thisClass, final String data) {
        /**
         * Method to handles the list clicks and starts a new activity accordingly
         *
         * @param lv the listview that the listener should be attached to
         * @param context the context of activity that calls the function
         * @param thisClass the new activity that should be started
         * @param data optional extra data to make the next activity function (album and track need an artist as well)
         */
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                //Retrieves the text of the specific list item
                TextView textView = (TextView)view.findViewById(R.id.textview);
                String text = textView.getText().toString();

                //Creates an intent and starts it
                Intent intent = MainActivity.newIntent(context, thisClass);
                intent.putExtra("name", data + text);
                startActivity(intent);
            }
        });
    }

    public void findUsers(final Runnable onLoaded) {
        /**
         * Method that finds the user id's that are in your favourites or like the same things and afterwards
         * finds the usernames that correspond to those found id's
         */
        DatabaseReference ref = database.child("artist").child(artist);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //Fills the array with the userIds
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    String value = postSnapshot.child("user").getValue(String.class);
                    if (value != null) {
                        if (!value.equals(userId)) {
                            similarUsers.add(value);
                        }
                    }
                }
                onLoaded.run();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
