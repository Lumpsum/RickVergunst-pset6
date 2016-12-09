package com.example.rick.rickvergunst_pset6;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    // Initialize variables
    protected Button logOutButton;
    protected Button searchPageButton;
    protected Button homeButton;
    TextView welcomeText;
    ListView mainArtistListView;
    ListView mainUserListView;
    ArrayList<String> mainFavouriteArtists;
    ArrayList<String> mainFavouriteUsers;
    ArrayAdapter artistAdapter;
    ArrayAdapter userAdapter;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference database;
    private String userId;
    private SharedPreferences preferenceSettings;
    private static final int PREFERENCE_MODE_PRIVATE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        logOutButton = (Button)findViewById(R.id.logOutButton);
        searchPageButton = (Button)findViewById(R.id.toSearchPageButton);
        homeButton = (Button)findViewById(R.id.toHomeButton);
        welcomeText = (TextView)findViewById(R.id.mainWelcomeText);
        mainArtistListView = (ListView)findViewById(R.id.mainArtistListView);
        mainUserListView = (ListView)findViewById(R.id.mainUserListView);

        // Assign firebase variables
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance().getReference();

        mainFavouriteArtists = new ArrayList<String>();
        artistAdapter = new ArrayAdapter(this, R.layout.list_item, mainFavouriteArtists);
        mainArtistListView.setAdapter(artistAdapter);

        mainFavouriteUsers = new ArrayList<String>();
        userAdapter = new ArrayAdapter(this, R.layout.list_item, mainFavouriteUsers);
        mainUserListView.setAdapter(userAdapter);

        preferenceSettings = PreferenceManager.getDefaultSharedPreferences(this);

        String userName = preferenceSettings.getString("name", null);

        // If user is not logged in, launch log in activity
        if (firebaseUser == null) {
            startActivity(newIntent(this, LogInActivity.class));
        }
        else {
            userId = firebaseUser.getUid();
            welcomeText.setText(welcomeText.getText().toString() + userName);

            homeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(newIntent(MainActivity.this, MainActivity.class));;
                }
            });

            logOutButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(signOut(MainActivity.this, firebaseAuth));
                }
            });

            searchPageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(newIntent(MainActivity.this, SearchPage.class));
                }
            });

            DatabaseReference ref = database.child("users").child(userId).child("favourites");
            fillArrayFireBase(ref, "artist", mainFavouriteArtists, artistAdapter, "");
            fillArrayFireBase(ref, "user", mainFavouriteUsers, userAdapter, "");

            onListItemClick(mainArtistListView, MainActivity.this, ArtistInfo.class, "");
            onListItemClick(mainUserListView, MainActivity.this, UserInfo.class, "");
        }
    }

    public static void fillArrayFireBase(DatabaseReference ref, final String specificChild,
                                         final ArrayList<String> array,
                                         final ArrayAdapter<String> adapter, final String ID) {
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    String value = postSnapshot.child(specificChild).getValue(String.class);
                    if (value != null) {
                        if (!value.equals(ID)) {
                            array.add(value);
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //General function to call a new activity and clear the stack
    public static Intent newIntent(Context context, Class newClass) {
        Intent intent = new Intent(context, newClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return intent;
    }

    // Function to log out the user
    public static Intent signOut(Context context, FirebaseAuth firebaseAuth) {
        firebaseAuth.signOut();
        return (newIntent(context, LogInActivity.class));
    }

    public static void fillArray(String userValue, String method, String specific,
                                 String general, String moreSpecific,
                                 String selected, ArrayList<String> array) {
        AsyncTask<String, String, StringBuilder> aSyncTask = new ASyncTask();
        StringBuilder result;
        String apiKey = "eb4c34f0485ffa97337735fa01fe3b36";
        String artist = "";
        if (method.equals("getInfo") && (selected.equals("album") || selected.equals("track"))) {
            artist = userValue.split("\\-")[0];
            userValue = userValue.split("\\-")[1];
        }
        try {
            result = aSyncTask.execute(userValue, apiKey, selected, method, artist).get();
            JSONObject jsonObject = new JSONObject(result.toString());
            JSONArray results;
            if (method.equals("getInfo") && selected.equals("track")) {
                JSONObject trackAlbum = jsonObject.getJSONObject(general).getJSONObject(specific);
                array.add(trackAlbum.get("title").toString());
            }
            else {
                if (!moreSpecific.equals("")) {
                    results = jsonObject.getJSONObject(general).getJSONObject(moreSpecific).getJSONArray(specific);
                } else {
                    results = jsonObject.getJSONObject(general).getJSONArray(specific);
                }
                int range;
                if (method.equals("search") || !artist.equals("")) {
                    range = results.length();
                } else {
                    if (results.length() > 5) {
                        range = 5;
                    } else {
                        range = results.length();
                    }
                }
                for (int i = 0; i < range; i++) {
                    JSONObject jObj = results.getJSONObject(i);
                    String name = jObj.getString("name");
                    if (method.equals("search") && (selected.equals("album") || selected.equals("track"))) {
                        name = jObj.getString("artist") + "-" + name;
                    }
                    array.add(name);
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void setButtonText(Query ref, final Button button) {
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    button.setText("Remove");
                }
                else {
                    button.setText("Add");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public static void removeChildFireBase(Query ref) {
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    DataSnapshot firstChild = dataSnapshot.getChildren().iterator().next();
                    firstChild.getRef().removeValue();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
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
