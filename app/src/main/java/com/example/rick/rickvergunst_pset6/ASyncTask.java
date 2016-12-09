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

public class ASyncTask extends AsyncTask<String, String, StringBuilder> {
    @Override
    protected StringBuilder doInBackground(String... params) {
        InputStream input;
        try {
            if (!params[4].equals("")) {
                input = new URL("http://ws.audioscrobbler.com/2.0/?method=" + params[2] +
                        "." + params[3] + "&" + params[2] + "=" +
                        URLEncoder.encode(params[0], "UTF-8") +
                        "&artist=" + params[4] +
                        "&api_key=" + params[1] +
                        "&format=json"

                ).openStream();
            }
            else {
                input = new URL("http://ws.audioscrobbler.com/2.0/?method=" + params[2] +
                        "." + params[3] + "&" + params[2] + "=" +
                        URLEncoder.encode(params[0], "UTF-8") +
                        "&api_key=" + params[1] +
                        "&format=json"

                ).openStream();
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            StringBuilder result = new StringBuilder();
            String line;
            while((line = reader.readLine()) != null) {
                result.append(line);
            }
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
