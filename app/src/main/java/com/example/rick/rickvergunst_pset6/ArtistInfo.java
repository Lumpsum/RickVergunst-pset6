package com.example.rick.rickvergunst_pset6;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

public class ArtistInfo extends AppCompatActivity {

    protected Button logOutButton;
    protected Button searchPageButton;
    protected Button homeButton;
    ListView artistInfoAlbumListView;
    ListView artistInfoTrackListView;
    ListView artistInfoSimilarListView;
    ListView artistInfoSimilarUsers;
    TextView artistInfoNameText;
    Button artistAddButton;
    String apiKey;
    String artist;
    ArrayList<String> topAlbums;
    ArrayList<String> topTracks;
    ArrayList<String> similarArtist;
    ArrayList<String> similarUsers;
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

        Intent intent = getIntent();
        artist = intent.getStringExtra("name");

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance().getReference();
        userId = firebaseUser.getUid();

        logOutButton = (Button)findViewById(R.id.logOutButton);
        searchPageButton = (Button)findViewById(R.id.toSearchPageButton);
        homeButton = (Button)findViewById(R.id.toHomeButton);

        artistInfoNameText = (TextView)findViewById(R.id.artistNameText);
        artistInfoNameText.setText(artist);
        artistAddButton = (Button)findViewById(R.id.artistAddButton);

        ref = database.child("users").child(userId).child("favourites").orderByChild("artist").equalTo(artist);
        MainActivity.setButtonText(ref, artistAddButton);

        apiKey = "eb4c34f0485ffa97337735fa01fe3b36";
        topAlbums = new ArrayList<String>();
        topTracks = new ArrayList<String>();
        similarArtist = new ArrayList<String>();
        similarUsers = new ArrayList<String>();

        artistInfoAlbumListView = (ListView)findViewById(R.id.artistTopAlbums);
        artistInfoTrackListView = (ListView)findViewById(R.id.artistTopTracks);
        artistInfoSimilarListView = (ListView)findViewById(R.id.artistSimilarListView);
        artistInfoSimilarUsers = (ListView)findViewById(R.id.artistSimilarUsersList);

        topAlbumAdapter = new ArrayAdapter<String>(this, R.layout.list_item, topAlbums);
        topTrackAdapter = new ArrayAdapter<String>(this, R.layout.list_item, topTracks);
        similarArtistAdapater = new ArrayAdapter<String>(this, R.layout.list_item, similarArtist);
        similarUsersAdapter = new ArrayAdapter<String>(this, R.layout.list_item, similarUsers);

        MainActivity.fillArray(artist, "getTopAlbums", "album", "topalbums", "", "artist", topAlbums);
        MainActivity.fillArray(artist, "getTopTracks", "track", "toptracks", "", "artist", topTracks);
        MainActivity.fillArray(artist, "getSimilar", "artist", "similarartists", "", "artist", similarArtist);

        artistInfoAlbumListView.setAdapter(topAlbumAdapter);
        artistInfoTrackListView.setAdapter(topTrackAdapter);
        artistInfoSimilarListView.setAdapter(similarArtistAdapater);
        artistInfoSimilarUsers.setAdapter(similarUsersAdapter);

        onListItemClick(artistInfoSimilarListView, ArtistInfo.this, ArtistInfo.class, "");
        onListItemClick(artistInfoAlbumListView, ArtistInfo.this, AlbumInfo.class, artist + "-");
        onListItemClick(artistInfoTrackListView, ArtistInfo.this, TrackInfo.class, artist + "-");
        onListItemClick(artistInfoSimilarUsers, ArtistInfo.this, UserInfo.class, "");

        artistAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (artistAddButton.getText().equals("Add")) {
                    database.child("users").child(userId).child("favourites").push().child("artist").setValue(artist);
                    database.child("artist").child(artist).push().child("user").setValue(userId);
                    artistAddButton.setText("Remove");
                }
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

        DatabaseReference ref = database.child("artist").child(artist);
        MainActivity.fillArrayFireBase(ref, "user", similarUsers, similarUsersAdapter, userId);

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
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView textView = (TextView)view.findViewById(R.id.textview);
                String text = textView.getText().toString();
                Intent intent = MainActivity.newIntent(context, thisClass);
                intent.putExtra("name", data + text);
                startActivity(intent);
            }
        });
    }
}
