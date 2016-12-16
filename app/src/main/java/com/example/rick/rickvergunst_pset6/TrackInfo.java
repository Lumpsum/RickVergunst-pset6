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

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import static com.example.rick.rickvergunst_pset6.LoadFonts.retrieveTypeFace;

/**
 * Class that shows track info and users that like this track
 */
public class TrackInfo extends AppCompatActivity {

    //Initiate variables
    protected Button logOutButton;
    protected Button searchPageButton;
    protected Button homeButton;
    Button trackInfoButton;
    String track;
    TextView trackInfoTitle;
    TextView trackInfoArtist;
    TextView trackInfoAlbum;
    ListView trackInfoSimilarUsers;
    ArrayList<String> array;
    ArrayList<String> similarUsers;
    ArrayList<String> similarUsersNames;
    ArrayList<String> similarUsersNamesId;
    ArrayAdapter<String> similarUsersAdapter;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference database;
    private String userId;
    Query ref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_info);

        //Retrieve data from the source activity
        Intent intent = getIntent();
        track = intent.getStringExtra("name");

        //Removes [Explicit] from the track, in order for Firebase to add the track
        if (track.contains("[Explicit]")) {
            track = track.replace("[Explicit]", "");
            track.trim();
        }

        //Assign the layout elements to variables
        logOutButton = (Button)findViewById(R.id.logOutButton);
        searchPageButton = (Button)findViewById(R.id.toSearchPageButton);
        homeButton = (Button)findViewById(R.id.toHomeButton);
        trackInfoButton = (Button)findViewById(R.id.trackInfoButton);

        trackInfoSimilarUsers = (ListView)findViewById(R.id.trackInfoUserList);

        trackInfoTitle = (TextView)findViewById(R.id.trackInfoTitle);
        trackInfoArtist = (TextView)findViewById(R.id.trackInfoArtist);
        trackInfoAlbum = (TextView)findViewById(R.id.trackInfoAlbum);

        //Initiate the firebase variables
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance().getReference();
        userId = firebaseUser.getUid();

        //Set the button text according to whether a node exists
        ref = database.child("users").child(userId).child("favourites").orderByChild("track").equalTo(track);
        MainActivity.setButtonText(ref, trackInfoButton);

        //Set the textview fonts and text
        trackInfoTitle.setTypeface(retrieveTypeFace(this, 0));
        trackInfoTitle.setText(track.split("\\-")[1]);
        trackInfoArtist.setTypeface(retrieveTypeFace(this, 0));
        trackInfoArtist.setText(track.split("\\-")[0]);

        //Initiate the array lists needed
        array = new ArrayList<String>();
        similarUsers = new ArrayList<String>();
        similarUsersNames = new ArrayList<String>();
        similarUsersNamesId = new ArrayList<String>();

        //Fill the array and set the font and text accordingly
        MainActivity.fillArray(track, "getInfo", "album", "track", "", "track", array);
        trackInfoAlbum.setTypeface(retrieveTypeFace(this, 0));
        if (array.size() != 0) {
            trackInfoAlbum.setText(array.get(0));
        }

        //Initaite the adapter
        similarUsersAdapter = new ArrayAdapter<String>(this, R.layout.list_item, similarUsersNames);

        //Assign the adapter to the listview
        trackInfoSimilarUsers.setAdapter(similarUsersAdapter);

        //ListView listener that starts the correct new activity
        trackInfoSimilarUsers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = MainActivity.newIntent(TrackInfo.this, UserInfo.class);
                intent.putExtra("name", similarUsersNamesId.get(position));
                startActivity(intent);
            }
        });

        trackInfoArtist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = MainActivity.newIntent(TrackInfo.this, ArtistInfo.class);
                intent.putExtra("name", track.split("\\-")[0]);
                startActivity(intent);
            }
        });
        trackInfoAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = MainActivity.newIntent(TrackInfo.this, AlbumInfo.class);
                intent.putExtra("name", track.split("\\-")[0] + "-" + trackInfoAlbum.getText());
                startActivity(intent);
            }
        });

        //Button listener that either adds or removes a node
        trackInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Adds the track to the favourites and add the user to the track
                if (trackInfoButton.getText().equals("Add")) {
                    database.child("users").child(userId).child("favourites").push().child("track").setValue(track);
                    database.child("track").child(track).push().child("user").setValue(userId);
                    trackInfoButton.setText("Remove");
                }

                //Removes the nodes from the favourites and the track node
                else {
                    ref = database.child("users").child(userId).child("favourites")
                            .orderByChild("track").equalTo(track);
                    MainActivity.removeChildFireBase(ref);
                    ref = database.child("track").child(track)
                            .orderByChild("user").equalTo(userId);
                    MainActivity.removeChildFireBase(ref);
                    trackInfoButton.setText("Add");
                }
            }
        });

        //Fills the user array with the usernames
        findUsers(new Runnable() {
            @Override
            public void run() {
                MainActivity.setUserArrays(database.child("usernames"), similarUsers, similarUsersNames, similarUsersNamesId, similarUsersAdapter);
            }
        });

        //General button handlers of the bottom menu
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(MainActivity.newIntent(TrackInfo.this, MainActivity.class));;
            }
        });

        logOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(MainActivity.signOut(TrackInfo.this, firebaseAuth));
            }
        });

        searchPageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(MainActivity.newIntent(TrackInfo.this, SearchPage.class));
            }
        });
    }

    public void findUsers(final Runnable onLoaded) {
        /**
         * Method to handles the list clicks and starts a new activity accordingly
         *
         * @param lv the listview that the listener should be attached to
         * @param context the context of activity that calls the function
         * @param thisClass the new activity that should be started
         * @param data optional extra data to make the next activity function (album and track need an artist as well)
         */
        DatabaseReference ref = database.child("track").child(track);
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
