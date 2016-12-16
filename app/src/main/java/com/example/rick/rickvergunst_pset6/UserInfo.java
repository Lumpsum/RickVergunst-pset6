package com.example.rick.rickvergunst_pset6;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
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
 * Class that shows information about another user in the likes of the artist, album or track that he or she favourites
 */
public class UserInfo extends AppCompatActivity {

    //Initialize variables
    protected Button logOutButton;
    protected Button searchPageButton;
    protected Button homeButton;
    Button userInfoAddRemoveButton;
    TextView userInfoName;
    ListView userInfoArtistListView;
    ListView userInfoAlbumListView;
    ListView userInfoTrackListView;
    ArrayList<String> userInfoAlbumList;
    ArrayList<String> userInfoArtistList;
    ArrayList<String> userInfoTrackList;
    ArrayAdapter<String> userInfoAlbumAdapter;
    ArrayAdapter<String> userInfoArtistAdapter;
    ArrayAdapter<String> userInfoTrackAdapter;
    String name;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference database;
    private String userId;
    Query ref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        //Retrieve the data from the source activity
        Intent intent = getIntent();
        name = intent.getStringExtra("name");

        //Assign the layout elements to variables
        logOutButton = (Button) findViewById(R.id.logOutButton);
        searchPageButton = (Button) findViewById(R.id.toSearchPageButton);
        homeButton = (Button) findViewById(R.id.toHomeButton);
        userInfoAddRemoveButton = (Button) findViewById(R.id.userInfoAddRemoveButton);

        userInfoName = (TextView) findViewById(R.id.userInfoName);

        userInfoArtistListView = (ListView) findViewById(R.id.userInfoArtistListView);
        userInfoAlbumListView = (ListView) findViewById(R.id.userInfoAlbumListView);
        userInfoTrackListView = (ListView) findViewById(R.id.userInfoTrackListView);

        //Initialize firebase variables
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance().getReference();
        userId = firebaseUser.getUid();

        //Set the button text according to whether a node exists
        ref = database.child("users").child(userId).child("favourites").orderByChild("user").equalTo(name);
        MainActivity.setButtonText(ref, userInfoAddRemoveButton);

        //Set the font of the textview
        userInfoName.setTypeface(retrieveTypeFace(this, 0));

        //Retrieve the username of the useId and sets that as the textview text
        database.child("usernames").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    if (postSnapshot.hasChild(name)) {
                        userInfoName.setText(postSnapshot.child(name).getValue().toString());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //Initiate the array lists
        userInfoArtistList = new ArrayList<String>();
        userInfoAlbumList = new ArrayList<String>();
        userInfoTrackList = new ArrayList<String>();

        //Initiate the adapter for the different listviews
        userInfoArtistAdapter = new ArrayAdapter<String>(UserInfo.this, R.layout.list_item, userInfoArtistList);
        userInfoAlbumAdapter = new ArrayAdapter<String>(UserInfo.this, R.layout.list_item, userInfoAlbumList);
        userInfoTrackAdapter = new ArrayAdapter<String>(UserInfo.this, R.layout.list_item, userInfoTrackList);

        //Assigns the adapters to the listviews
        userInfoAlbumListView.setAdapter(userInfoAlbumAdapter);
        userInfoArtistListView.setAdapter(userInfoArtistAdapter);
        userInfoTrackListView.setAdapter(userInfoTrackAdapter);

        //Fills the arrays with data from the firebase
        DatabaseReference ref = database.child("users").child(name).child("favourites");
        MainActivity.fillArrayFireBase(ref, "artist", userInfoArtistList, userInfoArtistAdapter, name);
        MainActivity.fillArrayFireBase(ref, "album", userInfoAlbumList, userInfoAlbumAdapter, name);
        MainActivity.fillArrayFireBase(ref, "track", userInfoTrackList, userInfoTrackAdapter, name);

        //Sets listeners of the list items
        onListItemClick(userInfoArtistListView, UserInfo.this, ArtistInfo.class, "");
        onListItemClick(userInfoAlbumListView, UserInfo.this, AlbumInfo.class, "");
        onListItemClick(userInfoTrackListView, UserInfo.this, TrackInfo.class, "");

        //Button listener that either adds or removes the user as a node
        userInfoAddRemoveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Adds the found user to the favourites of the current user
                if (userInfoAddRemoveButton.getText().equals("Add")) {
                    database.child("users").child(userId).child("favourites").push().child("user").setValue(name);
                    userInfoAddRemoveButton.setText("Remove");
                }

                //Removes the user from the favourites
                else {
                    Query ref = database.child("users").child(userId).child("favourites")
                            .orderByChild("user").equalTo(name);
                    MainActivity.removeChildFireBase(ref);
                    userInfoAddRemoveButton.setText("Add");
                }
            }
        });

        //General handlers of the bottom menu
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(MainActivity.newIntent(UserInfo.this, MainActivity.class));
                ;
            }
        });

        logOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(MainActivity.signOut(UserInfo.this, firebaseAuth));
            }
        });

        searchPageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(MainActivity.newIntent(UserInfo.this, SearchPage.class));
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
                TextView textView = (TextView) view.findViewById(R.id.textview);
                String text = textView.getText().toString();
                Intent intent = MainActivity.newIntent(context, thisClass);
                intent.putExtra("name", data + text);
                startActivity(intent);
            }
        });
    }
}
