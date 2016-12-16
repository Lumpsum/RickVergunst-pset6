package com.example.rick.rickvergunst_pset6;

import android.content.Context;
import android.graphics.Typeface;

/**
 * Created by Rick on 12/14/2016.
 */

/**
 * Class that reads the fonts if not done yet and returns a typeface
 */
public class LoadFonts {

    //Initailize variables
    private static final int numOfFonts = 1;

    private static boolean fontsLoaded = false;

    private static Typeface[] fonts = new Typeface[numOfFonts];

    private static String[] fontDir = {
            //Used font, found in assets/fonts
            "fonts/ADAM.CG PRO.otf"
    };

    public static Typeface retrieveTypeFace(Context context, int fontNum) {
        /**
         * Method that loads the fonts if needed and returns the requested font
         *
         * @param context the context of the calling activity
         * @param fontNum the number of the font that is requested
         * @return returns a TypeFace that is used to define the font
         */

        //Variables to check whether the fonts are loaded
        if (!fontsLoaded) {
            loadFonts(context);
        }
        return fonts[fontNum];
    }

    private static void loadFonts(Context context) {
        /**
         * Method that loads all the fonts if not done yet
         *
         * @param context the context of the source activity
         */

        //Loops through the fonts and loads them, sets the variable to true, so no redundant loading
        for (int i = 0; i < numOfFonts; i++) {
            fonts[i] = Typeface.createFromAsset(context.getAssets(), fontDir[i]);
        }
        fontsLoaded = true;
    }
}
