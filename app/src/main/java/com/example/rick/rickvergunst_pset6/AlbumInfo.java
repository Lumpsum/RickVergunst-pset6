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

import java.util.ArrayList;

public class AlbumInfo extends AppCompatActivity {

    protected Button logOutButton;
    protected Button searchPageButton;
    protected Button homeButton;
    ListView albumInfoTracksList;
    ArrayList<String> array;
    ArrayAdapter<String> albumTracksAdapter;
    TextView albumInfoTitle;
    TextView albumInfoArtist;
    String album;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_info);

        Intent intent = getIntent();
        album = intent.getStringExtra("name");

        logOutButton = (Button)findViewById(R.id.logOutButton);
        searchPageButton = (Button)findViewById(R.id.toSearchPageButton);
        homeButton = (Button)findViewById(R.id.toHomeButton);
        firebaseAuth = FirebaseAuth.getInstance();

        albumInfoTracksList = (ListView)findViewById(R.id.albumInfoTracksList);
        albumInfoTitle = (TextView)findViewById(R.id.albumInfoTitle);
        albumInfoArtist = (TextView)findViewById(R.id.albumInfoArtist);

        albumInfoTitle.setText(album.split("\\-")[1]);
        albumInfoArtist.setText(album.split("\\-")[0]);

        array = new ArrayList<String>();
        albumTracksAdapter = new ArrayAdapter<String>(this, R.layout.list_item, array);
        MainActivity.fillArray(album, "getInfo", "track", "album", "tracks", "album", array);
        albumInfoTracksList.setAdapter(albumTracksAdapter);

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
}
