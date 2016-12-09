package com.example.rick.rickvergunst_pset6;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class TrackInfo extends AppCompatActivity {

    protected Button logOutButton;
    protected Button searchPageButton;
    protected Button homeButton;
    String track;
    TextView trackInfoTitle;
    TextView trackInfoArtist;
    TextView trackInfoAlbum;
    ArrayList<String> array;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_info);

        Intent intent = getIntent();
        track = intent.getStringExtra("name");

        logOutButton = (Button)findViewById(R.id.logOutButton);
        searchPageButton = (Button)findViewById(R.id.toSearchPageButton);
        homeButton = (Button)findViewById(R.id.toHomeButton);
        firebaseAuth = FirebaseAuth.getInstance();

        trackInfoTitle = (TextView)findViewById(R.id.trackInfoTitle);
        trackInfoArtist = (TextView)findViewById(R.id.trackInfoArtist);
        trackInfoAlbum = (TextView)findViewById(R.id.trackInfoAlbum);

        trackInfoTitle.setText(track.split("\\-")[1]);
        trackInfoArtist.setText(track.split("\\-")[0]);

        array = new ArrayList<String>();

        MainActivity.fillArray(track, "getInfo", "album", "track", "", "track", array);
        trackInfoAlbum.setText(array.get(0));

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
}
