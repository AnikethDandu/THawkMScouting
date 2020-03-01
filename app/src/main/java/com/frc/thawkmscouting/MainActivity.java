package com.frc.thawkmscouting;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.regions.Regions;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.AWSStartupHandler;
import com.amazonaws.mobile.client.AWSStartupResult;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.regions.Region;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.io.IOException;
import java.util.Map;
import java.io.PrintWriter;

/**
 * Reads 6 QR codes, pushes the data to DynamoDB, and stores the data locally in JSON format
 *
 * @author Aniketh Dandu - Team 1100
 */
public class MainActivity extends AppCompatActivity {

    /**
     * Permission integer to write to external storage
     */
    private static final int MY_PERMISSION_WRITE_EXTERNAL_STORAGE = 1;
    /**
     * The Tag for Logging from this class
     */
    private static final String TAG = MainActivity.class.getSimpleName();
    /**
     * An array of Strings holding data from each QR code
     */
    private final String[] DATA_FILES = new String[6];
    /**
     * The labels corresponding to each QR code
     */
    private final TextView[] FILE_LABELS = new TextView[6];
    /**
     * The Cognito Pool ID (Needs to be replaced with the ID from an AWS account)
     */
    private final String ID = "REPLACE_ME_WITH_COGNITO_USER_POOL";
    /**
     * The region the AWS account is in (needs to be replaced)
     */
    private final Regions REGION = Regions.DEFAULT_REGION;
    /**
     * The object mapper for DynamoDB
     */
    private DynamoDBMapper m_dynamoDBMapper;

    /**
     * Set the UI elements and the btn_scan_qr listeners
     *
     * @param savedInstanceState The instance of the screen
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Create the screen and set te view
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Make a directory to store for the JSON files and log the result
        if (new File((Environment.getExternalStorageDirectory() + "/THawkScouting")).mkdirs()) {
            Log.i(TAG, "Directory successfully created");
        } else {
            Log.w(TAG, "Directory not created");
        }

        // Request permissions to write to external storage
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSION_WRITE_EXTERNAL_STORAGE);
        }

        // Initialize an AWS Mobile Client
        AWSMobileClient.getInstance().initialize(this, new AWSStartupHandler() {

            /**
             * Uses permissions from AWS Cognito and creates a DynamoDB Client and object mapper
             * @param awsStartupResult The result from initializing the AWS client
             */
            @Override
            public void onComplete(AWSStartupResult awsStartupResult) {

                // Get permissions from Cognito using the User Pool ID and region
                CognitoCachingCredentialsProvider cognitoCachingCredentialsProvider = new CognitoCachingCredentialsProvider(
                        getApplicationContext(),
                        ID,
                        REGION
                );

                // Instantiate a AmazonDynamoDBClient using the permissions
                AmazonDynamoDBClient ddbClient = Region.getRegion(REGION)
                        .createClient(
                                AmazonDynamoDBClient.class,
                                cognitoCachingCredentialsProvider,
                                new ClientConfiguration()
                        );

                // Create the DynamoDB object mapper
                m_dynamoDBMapper = DynamoDBMapper.builder()
                        .dynamoDBClient(ddbClient)
                        .awsConfiguration(
                                AWSMobileClient.getInstance().getConfiguration())
                        .build();

            }
        }).execute();

        // ID the labels displaying the file names
        FILE_LABELS[0] = findViewById(R.id.main_text_qr_1);
        FILE_LABELS[1] = findViewById(R.id.main_text_qr_2);
        FILE_LABELS[2] = findViewById(R.id.main_text_qr_3);
        FILE_LABELS[3] = findViewById(R.id.main_text_qr_4);
        FILE_LABELS[4] = findViewById(R.id.main_text_qr_5);
        FILE_LABELS[5] = findViewById(R.id.main_text_qr_6);

        final EditText REMOVE_NUMBER_TEXT = findViewById(R.id.remove_number);
        REMOVE_NUMBER_TEXT.setText("0");

        // When the remove button is clicked, remove the last QR code scanned
        final Button REMOVE_BUTTON = findViewById(R.id.main_button_remove_qr);
        REMOVE_BUTTON.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)   {
                // Store the index of the QR code the user wants to remove
                final int REMOVE_NUMBER = (REMOVE_NUMBER_TEXT.getText().toString().equals("")) ?
                        0
                        :  Integer.valueOf(REMOVE_NUMBER_TEXT.getText().toString()) - 1;
                // If the index is from 0 to 5, remove that QR code
                if (REMOVE_NUMBER <= 5 && REMOVE_NUMBER >= 0) {
                    if (DATA_FILES[REMOVE_NUMBER] != null) {
                        FILE_LABELS[REMOVE_NUMBER].setText(String.format("%s", "Empty"));
                        DATA_FILES[REMOVE_NUMBER] = null;
                        FILE_LABELS[REMOVE_NUMBER].setTextColor(Color.WHITE);
                    }
                    // If the index is 0, remove the last QR code entered
                } else if (REMOVE_NUMBER == -1) {
                    for (int i = 5; i > -1; i--) {
                        if (DATA_FILES[i] != null) {
                            FILE_LABELS[i].setText(String.format("%s", "Empty"));
                            DATA_FILES[i] = null;
                            FILE_LABELS[i].setTextColor(Color.WHITE);
                            break;
                        }
                    }
                }
            }
        });

        // When the export button is clicked, push the data to AWS */
        final Button EXPORT_BUTTON = findViewById(R.id.main_button_export);
        EXPORT_BUTTON.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadData();
            }
        });

        // When the scan button is pressed, scan the QR code
        final Button SCAN_BUTTON = findViewById(R.id.main_button_scan_qr);
        SCAN_BUTTON.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final IntentIntegrator INTEGRATOR = new IntentIntegrator(MainActivity.this);
                INTEGRATOR.initiateScan();
            }
        });
    }

    /**
     * Update the String holding the QR code data and the corresponding label
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // The result from scanning the QR code
        final IntentResult RESULT = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (RESULT != null) {
            // If the QR code has no data, notify the user
            if (RESULT.getContents() == null) {
                Toast.makeText(MainActivity.this, "No data in QR code", Toast.LENGTH_LONG).show();
            } else {
                // If QR contains data, update the label and store the data in a String
                // Check if slot is open
                boolean slotOpen = false;
                for (int s = 0; s < 6; s++) {
                    if (DATA_FILES[s] == null || DATA_FILES[s].equals("")) {
                        slotOpen = true;
                        break;
                    }
                }
                // If the slot is open, add the QR data to the array
                if (slotOpen) {
                    for(int i = 0; i < 6; i++) {
                        if (DATA_FILES[i] == null || DATA_FILES[i].equals("")) {
                            // Update the String holding the data
                            DATA_FILES[i] = RESULT.getContents();
                            // Update the label with the color, team, and match
                            String[] DATA_ARRAY = DATA_FILES[i].split(",");
                            final int COLOR = DATA_ARRAY[2].toLowerCase().equals("red")
                                    ? getResources().getColor(R.color.allianceRed)
                                    : getResources().getColor(R.color.allianceBlue);
                            FILE_LABELS[i].setTextColor(COLOR);
                            FILE_LABELS[i].setText(String.format("Match %s: %s", DATA_ARRAY[1], DATA_ARRAY[0]));
                            // Break so only one data slot is filled
                            break;
                        }
                    }
                } else {
                    // If a slot is not open, notify the user
                    Toast.makeText(MainActivity.this, "Already have 6 files\n Please remove a file", Toast.LENGTH_LONG).show();
                }
            }
        } else {
            // If the QR scan fails
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * Upload data to AWS and save locally in a JSON file
     */
    private void uploadData() {
        try {
            // Export the JSON file to internal storage
            exportJSON();
            Toast.makeText(getApplicationContext(), "JSON file updated", Toast.LENGTH_SHORT).show();
            final ConnectivityManager CONNECTIVITY_MANAGER = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            final NetworkInfo NETWORK_INFO = CONNECTIVITY_MANAGER.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

            if (NETWORK_INFO.isConnected()) {
                // Loop through the six strings of data
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for(int i = 0; i < 6; i++) {
                            m_dynamoDBMapper.save(returnMatch(i));
                        }
                    }
                }).start();
                Toast.makeText(getApplicationContext(), "Database updated", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), "Cannot upload to DynamoDB no Wi-Fi | JSON File updated", Toast.LENGTH_LONG).show();
            }
        }
        // Display error message if user did not scan six QR codes
        catch (NullPointerException e) {
            Toast.makeText(getApplicationContext(), "One or more empty data slots. Please scan all 6 QR codes", Toast.LENGTH_LONG).show();
        }
        // Display an error message for any other error
        catch (Exception e) {
            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     *
     * @param index The index of the array of the team's data used to push to AWS
     * @return Return an instance of the MatchData schema class
     */
    @SuppressWarnings("unchecked")
    @SuppressLint("DefaultLocale")
    private MatchData returnMatch(int index) {
        // Split the data into a String array
        final String[] DATA = DATA_FILES[index].split(",");
        // Create the instance of the MatchData class
        MatchData matchData = new MatchData();
        // Set all the required data fields
        matchData.setTeam(Integer.valueOf(DATA[0]));
        matchData.setMatch(Integer.valueOf(DATA[1]));
        matchData.setColor(DATA[2].toLowerCase());
        matchData.setDriverStation(Integer.valueOf(DATA[3]));
        matchData.setCrossedLine(Boolean.valueOf(DATA[4]));
        final HashMap<String, Integer> AUTO_HITS = new HashMap<String, Integer>() {
            {
                put("Inner", Integer.valueOf(DATA[5]));
                put("Outer", Integer.valueOf(DATA[6]));
                put("Bottom", Integer.valueOf(DATA[7]));
            }
        };
        matchData.setAutoHits(AUTO_HITS);
        final HashMap<String, Integer> AUTO_MISS = new HashMap<String, Integer>() {
            {
                put("High", Integer.valueOf(DATA[8]));
                put("Low", Integer.valueOf(DATA[9]));
            }
        };
        matchData.setAutoMiss(AUTO_MISS);
        matchData.setTimePlayingDefense(Integer.valueOf(DATA[10]));
        matchData.setTimeDefenseOnTeam(Integer.valueOf(DATA[11]));
        matchData.setPenalties(Integer.valueOf(DATA[12]));
        final HashMap<String, HashMap<String, Integer>> SCORING = new HashMap<>();
        for(int i = 0; i < 6; i++) {
            final int INDEX = i;
            final HashMap SCORING_MAP = new HashMap<String, Integer>() {
                {
                    put("Inner", Integer.valueOf(DATA[5*INDEX+13]));
                    put("Outer", Integer.valueOf(DATA[5*INDEX+14]));
                    put("Bottom", Integer.valueOf(DATA[5*INDEX+15]));
                    put("High", Integer.valueOf(DATA[5*INDEX+16]));
                    put("Low", Integer.valueOf(DATA[5*INDEX+17]));
                }
            };
            SCORING.put(String.format("Position: %d", INDEX), SCORING_MAP);
        }
        matchData.setScoring(SCORING);
        matchData.setRotationControl(Boolean.valueOf(DATA[43]));
        matchData.setColorControl(Boolean.valueOf(DATA[44]));
        matchData.setAttemptedClimb(Boolean.valueOf(DATA[45]));
        matchData.setClimb(Boolean.valueOf(DATA[46]));
        matchData.setLevel(Boolean.valueOf(DATA[47]));
        matchData.setAttemptedDoubleClimb(Boolean.valueOf(DATA[48]));
        matchData.setDoubleClimb(Boolean.valueOf(DATA[49]));
        matchData.setBrownedOut(Boolean.valueOf(DATA[50]));
        matchData.setDisabled(Boolean.valueOf(DATA[51]));
        matchData.setYellowCard(Boolean.valueOf(DATA[52]));
        matchData.setRedCard(Boolean.valueOf(DATA[53]));
        matchData.setName((DATA[54]));
        matchData.setNotes((DATA[55]));
        return matchData;
    }

    /**
     * Turn the data in the String into a JSON object
     * Group all 6 JSON objects into a JSON array
     * Write the JSON array to a file
     */
    @SuppressWarnings("unchecked")
    @SuppressLint("DefaultLocale")
    private void exportJSON() {
        // Get the match (will be the same for all QR codes)
        final String MATCH = DATA_FILES[0].split(",")[1];
        // Create the array
        final JSONArray JSON_ARRAY = new JSONArray();
        // Create 6 JSON objects corresponding to each team's data
        for(int i = 0; i < 6; i++) {
            Map<String, Object> JSONMap = new HashMap<>();
            final String[] DATA = DATA_FILES[i].split(",");
            JSONMap.put("Team", Integer.valueOf(DATA[0]));
            JSONMap.put("Match", Integer.valueOf(MATCH));

            JSONMap.put("Color", DATA[2].toLowerCase());
            JSONMap.put("Driver Station", Integer.valueOf(DATA[3]));
            JSONMap.put("Crossed Line", Boolean.valueOf(DATA[4]));
            final HashMap<String, Integer> AUTO_HITS = new HashMap<String, Integer>() {
                {
                    put("Inner", Integer.valueOf(DATA[5]));
                    put("Outer", Integer.valueOf(DATA[6]));
                    put("Bottom", Integer.valueOf(DATA[7]));
                }
            };
            JSONMap.put("Auto Hits", AUTO_HITS);
            final HashMap<String, Integer> AUTO_MISS = new HashMap<String, Integer>() {
                {
                    put("High", Integer.valueOf(DATA[8]));
                    put("Low", Integer.valueOf(DATA[9]));
                }
            };
            JSONMap.put("Auto Miss", AUTO_MISS);
            JSONMap.put("Time Playing Defense", (Integer.valueOf(DATA[10])));
            JSONMap.put("Time Defense On Team", (Integer.valueOf(DATA[11])));
            JSONMap.put("Penalties", (Integer.valueOf(DATA[12])));
            final HashMap<String, HashMap<String, Integer>> SCORING = new HashMap<>();
            for(int x = 0; x < 6; x++) {
                final int INDEX = x;
                final HashMap SCORING_MAP = new HashMap<String, Integer>() {
                    {
                        put("Inner", Integer.valueOf(DATA[5*INDEX+13]));
                        put("Outer", Integer.valueOf(DATA[5*INDEX+14]));
                        put("Bottom", Integer.valueOf(DATA[5*INDEX+15]));
                        put("High", Integer.valueOf(DATA[5*INDEX+16]));
                        put("Low", Integer.valueOf(DATA[5*INDEX+17]));
                    }
                };
                SCORING.put(String.format("Position: %d", INDEX), SCORING_MAP);
            }
            JSONMap.put("Scoring", SCORING);
            JSONMap.put("Rotation Control", (Boolean.valueOf(DATA[43])));
            JSONMap.put("Color Control", (Boolean.valueOf(DATA[44])));
            JSONMap.put("Attempted CLimb", (Boolean.valueOf(DATA[45])));
            JSONMap.put("Climb", (Boolean.valueOf(DATA[46])));
            JSONMap.put("Level", (Boolean.valueOf(DATA[47])));
            JSONMap.put("Attempted Double Climb", (Boolean.valueOf(DATA[48])));
            JSONMap.put("Double Climb", (Boolean.valueOf(DATA[49])));
            JSONMap.put("Browned Out", (Boolean.valueOf(DATA[50])));
            JSONMap.put("Disabled", (Boolean.valueOf(DATA[51])));
            JSONMap.put("Yellow Card", (Boolean.valueOf(DATA[52])));
            JSONMap.put("Red Card", (Boolean.valueOf(DATA[53])));
            JSONMap.put("Scouter Name", (DATA[54]));
            JSONMap.put("Notes", (DATA[55]));
            // Put the JSON object into the JSON array
            JSON_ARRAY.put(new JSONObject(JSONMap));
        }
        // Write the JSON array to a text file in internal storage
        try {
            final File JSON_FILE = new File(Environment.getExternalStorageDirectory() + "/THawkScouting", (MATCH + ".txt"));
            Log.d(TAG, String.valueOf(JSON_FILE.exists()));
            final PrintWriter PRINT_WRITER = new PrintWriter(new FileOutputStream(JSON_FILE, false));
            PRINT_WRITER.println(JSON_ARRAY.toString(0));
            PRINT_WRITER.flush();
            PRINT_WRITER.close();
        }
        // Display the JSON error to the user with the prefix JSON error:
        catch (org.json.JSONException e) {
            Toast.makeText(getApplicationContext(), "JSON error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
        // Display any other exception to the user
        catch (IOException e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}