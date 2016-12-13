package com.example.rick.rickvergunst_pset6;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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

import java.util.ArrayList;

public class AlbumInfo extends AppCompatActivity {

    protected Button logOutButton;
    protected Button searchPageButton;
    protected Button homeButton;
    Button albumInfoButton;
    ListView albumInfoTracksList;
    ListView albumInfoSimilarUsers;
    ArrayList<String> array;
    ArrayList<String> similarUsers;
    ArrayList<String> similarUsersNames;
    ArrayList<String> similarUsersNamesId;
    ArrayAdapter<String> albumTracksAdapter;
    ArrayAdapter<String> similarUsersAdapter;
    TextView albumInfoTitle;
    TextView albumInfoArtist;
    String album;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference database;
    private String userId;
    Query ref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_info);

        Intent intent = getIntent();
        album = intent.getStringExtra("name");

        logOutButton = (Button)findViewById(R.id.logOutButton);
        searchPageButton = (Button)findViewById(R.id.toSearchPageButton);
        homeButton = (Button)findViewById(R.id.toHomeButton);
        albumInfoButton = (Button)findViewById(R.id.albumInfoButton);

        albumInfoTracksList = (ListView)findViewById(R.id.albumInfoTracksList);
        albumInfoSimilarUsers = (ListView)findViewById(R.id.albumInfoUserList);
        albumInfoTitle = (TextView)findViewById(R.id.albumInfoTitle);
        albumInfoArtist = (TextView)findViewById(R.id.albumInfoArtist);

        albumInfoTitle.setText(album.split("\\-")[1]);
        albumInfoArtist.setText(album.split("\\-")[0]);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance().getReference();
        userId = firebaseUser.getUid();

        ref = database.child("users").child(userId).child("favourites").orderByChild("album").equalTo(album);
        MainActivity.setButtonText(ref, albumInfoButton);

        array = new ArrayList<String>();
        similarUsers = new ArrayList<String>();
        similarUsersNames = new ArrayList<String>();
        similarUsersNamesId = new ArrayList<String>();

        albumTracksAdapter = new ArrayAdapter<String>(this, R.layout.list_item, array);
        MainActivity.fillArray(album, "getInfo", "track", "album", "tracks", "album", array);
        albumInfoTracksList.setAdapter(albumTracksAdapter);

        similarUsersAdapter = new ArrayAdapter<String>(this, R.layout.list_item, similarUsersNames);
        albumInfoSimilarUsers.setAdapter(similarUsersAdapter);

        albumInfoSimilarUsers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = MainActivity.newIntent(AlbumInfo.this, UserInfo.class);
                intent.putExtra("name", similarUsersNamesId.get(position));
                startActivity(intent);
            }
        });

        albumInfoArtist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = MainActivity.newIntent(AlbumInfo.this, ArtistInfo.class);
                intent.putExtra("name", album.split("\\-")[0]);
                startActivity(intent);
            }
        });

        albumInfoTracksList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView textView = (TextView)view.findViewById(R.id.textview);
                String text = textView.getText().toString();
                Intent intent = MainActivity.newIntent(AlbumInfo.this, TrackInfo.class);
                intent.putExtra("name", album.split("\\-")[0] + "-" + text);
                startActivity(intent);
            }
        });

        albumInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (albumInfoButton.getText().equals("Add")) {
                    database.child("users").child(userId).child("favourites").push().child("album").setValue(album);
                    database.child("album").child(album).push().child("user").setValue(userId);
                    albumInfoButton.setText("Remove");
                }
                else {
                    ref = database.child("users").child(userId).child("favourites")
                            .orderByChild("album").equalTo(album);
                    MainActivity.removeChildFireBase(ref);
                    ref = database.child("album").child(album)
                            .orderByChild("user").equalTo(userId);
                    MainActivity.removeChildFireBase(ref);
                    albumInfoButton.setText("Add");
                }
            }
        });

        findUsers(new Runnable() {
            @Override
            public void run() {
                MainActivity.setUserArrays(database.child("usernames"), similarUsers, similarUsersNames, similarUsersNamesId, similarUsersAdapter);
            }
        });

        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(MainActivity.newIntent(AlbumInfo.this, MainActivity.class));;
            }
        });

        logOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(MainActivity.signOut(AlbumInfo.this, firebaseAuth));
            }
        });

        searchPageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(MainActivity.newIntent(AlbumInfo.this, SearchPage.class));
            }
        });
    }

    public void findUsers(final Runnable onLoaded) {
        DatabaseReference ref = database.child("album").child(album);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
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
