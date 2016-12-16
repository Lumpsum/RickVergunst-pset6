package com.example.rick.rickvergunst_pset6;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by Rick on 12/6/2016.
 */

/**
 * Async class that handles the API request and gives back the response of those requests
 */
public class ASyncTask extends AsyncTask<String, String, StringBuilder> {

    //Returns a stringbuilder with the data from the api
    @Override
    protected StringBuilder doInBackground(String... params) {
        InputStream input;
        try {

            //If and else that check whether the call is for an artist or album/track
            //The urls of these two types differ

            //This call is for albums or tracks
            if (!params[4].equals("")) {

                //Adds an extra artist parameter
                input = new URL("http://ws.audioscrobbler.com/2.0/?method=" + params[2] +
                        "." + params[3] + "&" + params[2] + "=" +
                        URLEncoder.encode(params[0], "UTF-8") +
                        "&artist=" + params[4] +
                        "&api_key=" + params[1] +
                        "&format=json"

                ).openStream();
            }

            //Artist call
            else {

                //Does not have the extra parameter
                input = new URL("http://ws.audioscrobbler.com/2.0/?method=" + params[2] +
                        "." + params[3] + "&" + params[2] + "=" +
                        URLEncoder.encode(params[0], "UTF-8") +
                        "&api_key=" + params[1] +
                        "&format=json"

                ).openStream();
            }

            //Retrieves the data and builds a stringbuilder
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            StringBuilder result = new StringBuilder();
            String line;

            //Reads every line and adds that to the stringbuilder
            while((line = reader.readLine()) != null) {
                result.append(line);
            }

            //Returns the stringbuilder
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
