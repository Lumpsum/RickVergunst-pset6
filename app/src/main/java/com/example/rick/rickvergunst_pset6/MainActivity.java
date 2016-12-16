package com.example.rick.rickvergunst_pset6;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
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

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import static com.example.rick.rickvergunst_pset6.LoadFonts.retrieveTypeFace;

/**
 * Main class that holds all the general function and functions as the main hub of the app, furthermore shows information
 * of the user itself
 */
public class MainActivity extends AppCompatActivity {

    // Initialize variables
    protected Button logOutButton;
    protected Button searchPageButton;
    protected Button homeButton;
    Button mainChangeUserButton;
    TextView welcomeText;
    ListView mainArtistListView;
    ListView mainAlbumListView;
    ListView mainTrackListView;
    ListView mainUserListView;
    ArrayList<String> mainFavouriteArtists;
    ArrayList<String> mainFavouriteAlbums;
    ArrayList<String> mainFavouriteTracks;
    ArrayList<String> mainFavouriteUsers;
    ArrayList<String> mainFavouriteUsersNames;
    ArrayList<String> mainFavouriteUsersNamesId;
    ArrayAdapter artistAdapter;
    ArrayAdapter albumAdapter;
    ArrayAdapter trackAdapter;
    ArrayAdapter userAdapter;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference database;
    private String userId;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    public static final String Name = "nameKey";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Assign the layout elements to variables
        logOutButton = (Button)findViewById(R.id.logOutButton);
        searchPageButton = (Button)findViewById(R.id.toSearchPageButton);
        homeButton = (Button)findViewById(R.id.toHomeButton);
        mainChangeUserButton = (Button)findViewById(R.id.mainChangeUserButton);

        welcomeText = (TextView)findViewById(R.id.mainWelcomeText);

        mainArtistListView = (ListView)findViewById(R.id.mainArtistListView);
        mainAlbumListView = (ListView)findViewById(R.id.mainAlbumListView);
        mainTrackListView = (ListView)findViewById(R.id.mainTrackListView);
        mainUserListView = (ListView)findViewById(R.id.mainUserListView);

        // Assign firebase variables
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance().getReference();

        //Initaite the arraylists
        mainFavouriteArtists = new ArrayList<String>();
        mainFavouriteAlbums = new ArrayList<String>();
        mainFavouriteTracks = new ArrayList<String>();
        mainFavouriteUsers = new ArrayList<String>();
        mainFavouriteUsersNames = new ArrayList<String>();
        mainFavouriteUsersNamesId = new ArrayList<String>();

        //Iniate the array adapters
        artistAdapter = new ArrayAdapter(this, R.layout.list_item, mainFavouriteArtists);
        albumAdapter = new ArrayAdapter(this, R.layout.list_item, mainFavouriteAlbums);
        trackAdapter = new ArrayAdapter(this, R.layout.list_item, mainFavouriteTracks);
        userAdapter = new ArrayAdapter(this, R.layout.list_item, mainFavouriteUsersNames);

        //Assign the adapter to the listview
        mainArtistListView.setAdapter(artistAdapter);
        mainAlbumListView.setAdapter(albumAdapter);
        mainTrackListView.setAdapter(trackAdapter);
        mainUserListView.setAdapter(userAdapter);

        //Assign the shared preferences and editor
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        editor = sharedPreferences.edit();

        // If user is not logged in, launch log in activity
        if (firebaseUser == null) {
            startActivity(newIntent(this, LogInActivity.class));
        }
        else {
            //If there is no name present in the shared preferences start set username activity
            if (!sharedPreferences.contains(Name)) {
                startActivity(newIntent(MainActivity.this, UserNameActivity.class));
            } else {
                //Set the firebase user
                userId = firebaseUser.getUid();

                //Set the textview text and the title font
                welcomeText.setText(welcomeText.getText().toString() + " " + sharedPreferences.getString(Name, ""));
                welcomeText.setTypeface(retrieveTypeFace(this, 0));

                //Fill the adapter arrays with data of the api
                DatabaseReference ref = database.child("users").child(userId).child("favourites");
                fillArrayFireBase(ref, "artist", mainFavouriteArtists, artistAdapter, "");
                fillArrayFireBase(ref, "album", mainFavouriteAlbums, albumAdapter, "");
                fillArrayFireBase(ref, "track", mainFavouriteTracks, trackAdapter, "");

                //Fill the array with similar users from the firebase
                findUsers(new Runnable() {
                    @Override
                    public void run() {
                        setUserArrays(database.child("usernames"), mainFavouriteUsers, mainFavouriteUsersNames,
                                mainFavouriteUsersNamesId, userAdapter);
                    }
                });

                //Set listener for hte listview items
                onListItemClick(mainArtistListView, MainActivity.this, ArtistInfo.class, "");
                onListItemClick(mainAlbumListView, MainActivity.this, AlbumInfo.class, "");
                onListItemClick(mainTrackListView, MainActivity.this, TrackInfo.class, "");
                mainUserListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent intent = MainActivity.newIntent(MainActivity.this, UserInfo.class);
                        intent.putExtra("name", mainFavouriteUsersNamesId.get(position));
                        startActivity(intent);
                    }
                });

                //Change usernames button handler
                mainChangeUserButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        //Removes the username from the id
                        Query ref = database.child("usernames").orderByChild(userId).equalTo(sharedPreferences.getString(Name, ""));
                        removeChildFireBase(ref);

                        //Clears the preferences and starts this activity again
                        editor.clear();
                        editor.commit();
                        startActivity(newIntent(MainActivity.this, MainActivity.class));
                    }
                });

                //General bottom menu button handlers
                homeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(newIntent(MainActivity.this, MainActivity.class));
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
            }
        }
    }

    public static void fillArrayFireBase(DatabaseReference ref, final String specificChild,
                                         final ArrayList<String> array,
                                         final ArrayAdapter<String> adapter, final String ID) {
        /**
         * Basic method that fills an given array based on a given database reference
         *
         * @param ref basic reference that is used to call the value listener
         * @param specificChild specifies the child node that should be searched for
         * @param array the array that should be filled with data
         * @param adapter the adapter that contains the array
         * @param ID arguments that specifies a String that should not be put in the array, usually the userID
         */
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Loops trough the nodes and adds data to the array
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    String value = postSnapshot.child(specificChild).getValue(String.class);
                    if (value != null) {
                        if (!value.equals(ID)) {
                            array.add(value);
                        }
                    }
                }
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }

    public static Intent newIntent(Context context, Class newClass) {
        /**
         * Basic method that clear the activity stack and returns an intent to start
         *
         * @param context the context of the activity that calls the function
         * @param newClass the new java class that is called through the intent
         * @return an intent that calls the new activity
         */
        Intent intent = new Intent(context, newClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return intent;
    }

    public static Intent signOut(Context context, FirebaseAuth firebaseAuth) {
        /**
         * Method that signs out the user
         *
         * @param context context of the activity that calls the function
         * @param firebaseAuth variable that contains the authentication
         * @return returns a intent created by the newIntent method
         */
        firebaseAuth.signOut();
        return (newIntent(context, LogInActivity.class));
    }

    public static void fillArray(String userValue, String method, String specific,
                                 String general, String moreSpecific,
                                 String selected, ArrayList<String> array) {
        /**
         * Function that calls an AsyncTask and fills an array based on the results and given parameters
         * Changes behaviour based on the given parameters
         *
         * @param userValue the user query
         * @param method the method that is used by the api to get certain information
         * @param specific parameter that is used in the json to find the data
         * @param general general parameters that is always needed by the api call
         * @param moreSpecific extra parameter that allows for deeper search if needed by the method in the json
         * @param selected the type of data that is requested from the api (artist, album, track)
         * @param array array that is filled by the method
         */
        AsyncTask<String, String, StringBuilder> aSyncTask = new ASyncTask();
        StringBuilder result;
        String apiKey = "eb4c34f0485ffa97337735fa01fe3b36";
        String artist = "";

        //Checks for a specific method or type of data that needs further processing
        if (method.equals("getInfo") && (selected.equals("album") || selected.equals("track"))) {
            artist = userValue.split("\\-")[0];
            userValue = userValue.split("\\-")[1];
        }
        try {

            //Calls an Asynctask to retrieve the api json result
            result = aSyncTask.execute(userValue, apiKey, selected, method, artist).get();
            JSONObject jsonObject = new JSONObject(result.toString());
            JSONArray results;

            //Check for method and if data is track and processes the data based on those parameters
            if (method.equals("getInfo") && selected.equals("track")) {
                JSONObject trackAlbum = jsonObject.getJSONObject(general).getJSONObject(specific);
                array.add(trackAlbum.get("title").toString());
            }
            else {

                //Parses the json based on whether moreSpecific has a value or not
                if (!moreSpecific.equals("")) {
                    results = jsonObject.getJSONObject(general).getJSONObject(moreSpecific).getJSONArray(specific);
                } else {
                    results = jsonObject.getJSONObject(general).getJSONArray(specific);
                }

                //Differentiate between a search and other methods to adjust the array length accordingly
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

                //Fills the array based on created json Object and fills it accordingly
                for (int i = 0; i < range; i++) {
                    JSONObject jObj = results.getJSONObject(i);
                    String name = jObj.getString("name");

                    //Checks for an album or track search which need an artist to query correct
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
        /**
         * Method that sets the button text according to whether a node exists
         *
         * @param ref basic Query that references the basic database path to the node
         * @param button the button that should be adjusted accordingly
         */
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //If the node exists, it should me removable, else addable
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
        /**
         * Method to remove a child from the database
         *
         * @param ref the given node that should be removed from the database
         */
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    //Retrieves the child that corresponds to the ref and removes it
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
                TextView textView = (TextView)view.findViewById(R.id.textview);
                String text = textView.getText().toString();
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
        DatabaseReference ref = database.child("users").child(userId).child("favourites");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    String value = postSnapshot.child("user").getValue(String.class);
                    if (value != null) {

                        //Check to not add the user itself
                        if (!value.equals(userId)) {
                            mainFavouriteUsers.add(value);
                        }
                    }
                }

                //Call after the completion of finding the users that finds the usernames corresponding to the id's
                onLoaded.run();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public static void setUserArrays(final DatabaseReference ref ,final ArrayList ids, final ArrayList userNames,
                                     final ArrayList userNamesId, final ArrayAdapter<String> adapter) {
        /**
         * Method that finds the correct usernames based on the user id's, gets called after findUsers
         *
         * @param ref firebase reference that references to where the usernames are
         * @param ids the arraylist that contains all the found id's in findUsers
         * @param userNames arrayList that will contain the usernames
         * @param userNamesId arrayList that contains the id's that are at the same position as the userNames array
         * @param adapter adatper that contains the array and gets updated at the end
         */
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    for (Object id : ids) {
                        if (postSnapshot.hasChild(id.toString())) {

                            //Fills both the arrays with either the usernames or the id
                            String finalUserValue = postSnapshot.child(id.toString()).getValue().toString();
                            userNames.add(finalUserValue);
                            userNamesId.add(id);
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

}
