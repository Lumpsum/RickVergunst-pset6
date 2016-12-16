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

import static com.example.rick.rickvergunst_pset6.LoadFonts.retrieveTypeFace;

/**
 * Class that defines an album retrieved from the last.fm api, furthermore shows users that like this album based on Firebase
 */
public class AlbumInfo extends AppCompatActivity {

    //Initialize variables
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

        //Retrieve data from source
        Intent intent = getIntent();
        album = intent.getStringExtra("name");

        //Find the different elements of hte layout
        logOutButton = (Button)findViewById(R.id.logOutButton);
        searchPageButton = (Button)findViewById(R.id.toSearchPageButton);
        homeButton = (Button)findViewById(R.id.toHomeButton);
        albumInfoButton = (Button)findViewById(R.id.albumInfoButton);

        albumInfoTracksList = (ListView)findViewById(R.id.albumInfoTracksList);
        albumInfoSimilarUsers = (ListView)findViewById(R.id.albumInfoUserList);

        albumInfoTitle = (TextView)findViewById(R.id.albumInfoTitle);
        albumInfoArtist = (TextView)findViewById(R.id.albumInfoArtist);

        //Set text and font of textviews
        albumInfoTitle.setTypeface(retrieveTypeFace(this, 0));
        albumInfoTitle.setText(album.split("\\-")[1]);
        albumInfoArtist.setTypeface(retrieveTypeFace(this, 0));
        albumInfoArtist.setText(album.split("\\-")[0]);

        //Initialize firebase variables
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance().getReference();
        userId = firebaseUser.getUid();

        //Check whether artist is in favourites and adjust button text accordingly
        ref = database.child("users").child(userId).child("favourites").orderByChild("album").equalTo(album);
        MainActivity.setButtonText(ref, albumInfoButton);

        //Initliaze different arraylists
        array = new ArrayList<String>();
        similarUsers = new ArrayList<String>();
        similarUsersNames = new ArrayList<String>();
        similarUsersNamesId = new ArrayList<String>();

        //Initialize the adapters
        albumTracksAdapter = new ArrayAdapter<String>(this, R.layout.list_item, array);
        similarUsersAdapter = new ArrayAdapter<String>(this, R.layout.list_item, similarUsersNames);

        //Fill the array adapter with API data
        MainActivity.fillArray(album, "getInfo", "track", "album", "tracks", "album", array);

        //Fiill the array with data from the firebase
        findUsers(new Runnable() {
            @Override
            public void run() {
                MainActivity.setUserArrays(database.child("usernames"), similarUsers, similarUsersNames, similarUsersNamesId, similarUsersAdapter);
            }
        });

        //Assign the adapters to the listviews
        albumInfoTracksList.setAdapter(albumTracksAdapter);
        albumInfoSimilarUsers.setAdapter(similarUsersAdapter);

        //Set the on item clickers along with the needed data for the next activity
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

        //Button handler that handles the instance where the user wants to add the labum
        albumInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Adds the album by creating a node for the album and adding the user to the album node
                if (albumInfoButton.getText().equals("Add")) {
                    database.child("users").child(userId).child("favourites").push().child("album").setValue(album);
                    database.child("album").child(album).push().child("user").setValue(userId);
                    albumInfoButton.setText("Remove");
                }

                //Removes the album node from the favourites and removes the user from the album node
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

        //General bottom menu button handlers
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
        /**
         * Method that finds the user id's that are in your favourites or like the same things and afterwards
         * finds the usernames that correspond to those found id's
         */
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
