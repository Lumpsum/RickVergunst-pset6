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

public class UserInfo extends AppCompatActivity {

    protected Button logOutButton;
    protected Button searchPageButton;
    protected Button homeButton;
    Button userInfoAddRemoveButton;
    TextView userInfoName;
    ListView userInfoArtistListView;
    ArrayList<String> userInfoArtistList;
    ArrayAdapter<String> userInfoArtistAdapter;
    ListView userInfoAlbumListView;
    ArrayList<String> userInfoAlbumList;
    ArrayAdapter<String> userInfoAlbumAdapter;
    ListView userInfoTrackListView;
    ArrayList<String> userInfoTrackList;
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

        Intent intent = getIntent();
        name = intent.getStringExtra("name");

        logOutButton = (Button) findViewById(R.id.logOutButton);
        searchPageButton = (Button) findViewById(R.id.toSearchPageButton);
        homeButton = (Button) findViewById(R.id.toHomeButton);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance().getReference();
        userId = firebaseUser.getUid();

        userInfoAddRemoveButton = (Button) findViewById(R.id.userInfoAddRemoveButton);
        ref = database.child("users").child(userId).child("favourites").orderByChild("user").equalTo(name);
        MainActivity.setButtonText(ref, userInfoAddRemoveButton);

        userInfoName = (TextView) findViewById(R.id.userInfoName);
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

        userInfoArtistList = new ArrayList<String>();
        userInfoArtistListView = (ListView) findViewById(R.id.userInfoArtistListView);
        userInfoArtistAdapter = new ArrayAdapter<String>(UserInfo.this, R.layout.list_item, userInfoArtistList);
        userInfoArtistListView.setAdapter(userInfoArtistAdapter);

        userInfoAlbumList = new ArrayList<String>();
        userInfoAlbumListView = (ListView) findViewById(R.id.userInfoAlbumListView);
        userInfoAlbumAdapter = new ArrayAdapter<String>(UserInfo.this, R.layout.list_item, userInfoAlbumList);
        userInfoAlbumListView.setAdapter(userInfoAlbumAdapter);

        userInfoTrackList = new ArrayList<String>();
        userInfoTrackListView = (ListView) findViewById(R.id.userInfoTrackListView);
        userInfoTrackAdapter = new ArrayAdapter<String>(UserInfo.this, R.layout.list_item, userInfoTrackList);
        userInfoTrackListView.setAdapter(userInfoTrackAdapter);

        DatabaseReference ref = database.child("users").child(name).child("favourites");
        MainActivity.fillArrayFireBase(ref, "artist", userInfoArtistList, userInfoArtistAdapter, name);
        MainActivity.fillArrayFireBase(ref, "album", userInfoAlbumList, userInfoAlbumAdapter, name);
        MainActivity.fillArrayFireBase(ref, "track", userInfoTrackList, userInfoTrackAdapter, name);

        onListItemClick(userInfoArtistListView, UserInfo.this, ArtistInfo.class, "");
        onListItemClick(userInfoAlbumListView, UserInfo.this, AlbumInfo.class, "");
        onListItemClick(userInfoTrackListView, UserInfo.this, TrackInfo.class, "");

        userInfoAddRemoveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userInfoAddRemoveButton.getText().equals("Add")) {
                    database.child("users").child(userId).child("favourites").push().child("user").setValue(name);
                    userInfoAddRemoveButton.setText("Remove");
                } else {
                    Query ref = database.child("users").child(userId).child("favourites")
                            .orderByChild("user").equalTo(name);
                    MainActivity.removeChildFireBase(ref);
                    userInfoAddRemoveButton.setText("Add");
                }
            }
        });

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
