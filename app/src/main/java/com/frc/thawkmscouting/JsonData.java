package com.frc.thawkmscouting;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

/**
 * Class to create and export JSON object with match data
 *
 * @author Aniketh Dandu - FRC Team 1100
 */
final class JsonData {

    // **************************************************
    // Static methods
    // **************************************************

    /**
     * Creates a HashMap to store the data read from the QR code
     * Creates a JSON object from the HashMap
     *
     * @param data The String array of data read from the QR code
     * @return Returns a JSON object holding all of the data from one QR code
     */
    static JSONObject createJSONObject(String[] data) {
        final HashMap<String, Object> JSONMap = new HashMap<>();
        JSONMap.put("Team", Integer.valueOf(data[0]));
        JSONMap.put("Match", Integer.valueOf(data[1]));
        JSONMap.put("Color", String.valueOf(data[2]).toLowerCase());
        JSONMap.put("Driver Station", Integer.valueOf(data[3]));
        JSONMap.put("Crossed Line", Boolean.valueOf(data[4]));
        JSONMap.put("Auto Hits", HashMapData.getAutoHits(data));
        JSONMap.put("Auto Miss", HashMapData.getAutoMiss(data));
        JSONMap.put("Time Playing Defense", (Integer.valueOf(data[10])));
        JSONMap.put("Time Defense On Team", (Integer.valueOf(data[11])));
        JSONMap.put("Penalties", (Integer.valueOf(data[12])));
        JSONMap.put("Scoring", HashMapData.getScoring(data));
        JSONMap.put("Rotation Control", (Boolean.valueOf(data[43])));
        JSONMap.put("Color Control", (Boolean.valueOf(data[44])));
        JSONMap.put("Attempted Climb", (Boolean.valueOf(data[45])));
        JSONMap.put("Climb", (Boolean.valueOf(data[46])));
        JSONMap.put("Level", (Boolean.valueOf(data[47])));
        JSONMap.put("Attempted Double Climb", (Boolean.valueOf(data[48])));
        JSONMap.put("Double Climb", (Boolean.valueOf(data[49])));
        JSONMap.put("Browned Out", (Boolean.valueOf(data[50])));
        JSONMap.put("Disabled", (Boolean.valueOf(data[51])));
        JSONMap.put("Yellow Card", (Boolean.valueOf(data[52])));
        JSONMap.put("Red Card", (Boolean.valueOf(data[53])));
        JSONMap.put("Scouter Name", (data[54]));
        JSONMap.put("Notes", (data[55]));
        return new JSONObject(JSONMap);
    }

    /**
     * Access a directory in internal storage and create a text file according to the match number
     * Write a JSON array to the file
     *
     * @param match The value of the match for the QR codes scanned
     * @param jsonArray The JSON array holding the six JSON objects (one per team)
     * @param context Application context for Toasts
     */
    static void exportJSON(String match, JSONArray jsonArray, Context context) {
        try {
            final File JSON_FILE = new File(Environment.getExternalStorageDirectory() + "/THawkScouting", (match + ".txt"));
            final PrintWriter PRINT_WRITER = new PrintWriter(new FileOutputStream(JSON_FILE, false));
            PRINT_WRITER.println(jsonArray.toString(0));
            PRINT_WRITER.flush();
            PRINT_WRITER.close();
        } catch (org.json.JSONException e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}