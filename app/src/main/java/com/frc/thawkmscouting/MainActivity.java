package com.frc.thawkmscouting;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.regions.Regions;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.AWSStartupHandler;
import com.amazonaws.mobile.client.AWSStartupResult;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.ClientConfiguration;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.io.File;
import java.io.FileOutputStream;

/**q
 * Combines data from 6 QR codes and pushes the data to AWS
 *
 * @author Aniketh Dandu - Team 1100
 */
public class MainActivity extends AppCompatActivity {

    /* The data from the six QR codes */
    final String[] DATA_FILES = new String[6];
    /* The labels for the six files */
    final TextView[] FILE_LABELS = new TextView[6];

    /* REPLACE THIS WITH THE COGNITO USER POOL ID */
    final String ID = "XXXXXXXXXXXXXXXXXXXXXXXXXXXX";
    /* REPLACE THIS WITH THE REGION YOUR DATABASE IS SET UP IN */
    final Regions REGION = Regions.DEFAULT_REGION;

    DynamoDBMapper dynamoDBMapper;

    /**
     * Set the UI elements and the button listeners
     *
     * @param savedInstanceState The instance of the screen
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        /* Create the screen and set te view */
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AWSMobileClient.getInstance().initialize(this, new AWSStartupHandler() {

            @Override
            public void onComplete(AWSStartupResult awsStartupResult) {

                CognitoCachingCredentialsProvider cognitoCachingCredentialsProvider = new CognitoCachingCredentialsProvider(
                        getApplicationContext(),
                        ID,
                        REGION
                );

                // Add code to instantiate a AmazonDynamoDBClient
                AmazonDynamoDBClient ddbClient = Region.getRegion(REGION)
                        .createClient(
                                AmazonDynamoDBClient.class,
                                cognitoCachingCredentialsProvider,
                                new ClientConfiguration()
                        );

                dynamoDBMapper = DynamoDBMapper.builder()
                        .dynamoDBClient(ddbClient)
                        .awsConfiguration(
                                AWSMobileClient.getInstance().getConfiguration())
                        .build();

            }
        }).execute();

        /* ID the labels displaying the file names */
        FILE_LABELS[0] = findViewById(R.id.textView);
        FILE_LABELS[1] = findViewById(R.id.textView2);
        FILE_LABELS[2] = findViewById(R.id.textView3);
        FILE_LABELS[3] = findViewById(R.id.textView4);
        FILE_LABELS[4] = findViewById(R.id.textView5);
        FILE_LABELS[5] = findViewById(R.id.textView6);

        /* At the start, set all the text to "Empty" */
        for (TextView text : FILE_LABELS) {
            text.setText(String.format("%s", "Empty"));
        }

        /* When the subtract button is clicked, remove the last slot and file label text */
        final Button REMOVE_BUTTON = findViewById(R.id.removeButton);
        REMOVE_BUTTON.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (int index = 6; index > 0; index--) {
                    if (DATA_FILES[index - 1] != null) {
                        FILE_LABELS[index - 1].setText(String.format("%s", "Empty"));
                        DATA_FILES[index - 1] = null;
                        FILE_LABELS[index-1].setTextColor(getResources().getColor(R.color.gray));
                        break;
                    }
                }
            }
        });

        /* When the export button is clicked, push the data to AWS */
        final Button EXPORT_BUTTON = findViewById(R.id.exportButton);
        EXPORT_BUTTON.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadData();
            }
        });

        final Button SCAN_BUTTON = findViewById(R.id.scanButton);
        SCAN_BUTTON.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final IntentIntegrator INTEGRATOR = new IntentIntegrator(MainActivity.this);
                INTEGRATOR.initiateScan();
            }
        });
    }

    /**
     * Update the arrays holding the file name and file data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        /* The result from scanning the QR */
        final IntentResult RESULT = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        /* If data is present, proceed */
        if (RESULT != null) {
            /* If the QR code has no data */
            if (RESULT.getContents() == null) {
                /* Notify the user that data was not scanned */
                Toast.makeText(MainActivity.this, "No data in QR code", Toast.LENGTH_LONG).show();
            } else {
                /* if QR contains data, modify the arrays */
                /* If the last slot is empty, then at least one slot can be filled */
                if (DATA_FILES[5] == null) {
                    /* Loop through the data slots */
                    for(int i = 0; i < 6; i++) {
                        /* If a data slot is empty, put the data in that slot */
                        if (DATA_FILES[i] == null) {
                            /* Grab the data from the QR code */
                            DATA_FILES[i] = RESULT.getContents();
                            /* Split the data into an array */
                            String[] DATA_ARRAY = DATA_FILES[i].split(",");
                            /* Determine the correct color corresponding to that team */
                            final int COLOR = DATA_ARRAY[2].toLowerCase().equals("red")
                                    ? getResources().getColor(R.color.red)
                                    : getResources().getColor(R.color.blue);
                            /* Set the label text color the corresponding color (for the user) */
                            FILE_LABELS[i].setTextColor(COLOR);
                            /* Set the label to the corresponding team and match */
                            FILE_LABELS[i].setText(String.format("Match %s: %s", DATA_ARRAY[1], DATA_ARRAY[0]));
                            /* Break so only one data slot is filled */
                            break;
                        }
                    }
                } else {
                    /* If a slot is not open, notify the user */
                    Toast.makeText(MainActivity.this, "Already have 6 files\n Please remove a file", Toast.LENGTH_LONG).show();
                }
            }
        } else {
            /* If the QR scan fails */
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * Upload data to AWS
     */
    private void uploadData() {
        try {
            /* Loop through the six strings of data */
            exportJSON();
            Toast.makeText(MainActivity.this, "JSON file updated", Toast.LENGTH_SHORT).show();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    for(int i = 0; i < 6; i++) {
                        dynamoDBMapper.save(returnMatch(i));
                    }
                }
            }).start();

            Toast.makeText(MainActivity.this, "Database updated", Toast.LENGTH_LONG).show();
        }
        /* Display error message if user did not scan six QR codes */
        catch (NullPointerException e) {
            Toast.makeText(MainActivity.this, "One or more empty data slots. Please scan all 6 QR codes", Toast.LENGTH_LONG).show();
        }
        /* Display an error message for any other error */
        catch (Exception e) {
            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @SuppressLint("DefaultLocale")
    private MatchData returnMatch(int index) {
        final String[] DATA = DATA_FILES[index].split(",");
        MatchData matchData = new MatchData();
        matchData.setTeam(Integer.valueOf(DATA[0]));
        matchData.setMatch(Integer.valueOf(DATA[1]));
        matchData.setColor(DATA[2].toLowerCase());
        matchData.setDriverStation(Integer.valueOf(DATA[3]));
        final HashMap<String, Integer> AUTO_HITS = new HashMap<String, Integer>() {
            {
                put("Inner", Integer.valueOf(DATA[4]));
                put("Outer", Integer.valueOf(DATA[5]));
                put("Bottom", Integer.valueOf(DATA[6]));
            }
        };
        matchData.setAutoHits(AUTO_HITS);
        final HashMap<String, Integer> AUTO_MISS = new HashMap<String, Integer>() {
            {
                put("High", Integer.valueOf(DATA[7]));
                put("Low", Integer.valueOf(DATA[8]));
            }
        };
        matchData.setAutoMiss(AUTO_MISS);
        matchData.setTimePlayingDefense(Integer.valueOf(DATA[9]));
        matchData.setTimeDefenseOnTeam(Integer.valueOf(DATA[10]));
        matchData.setPenalties(Integer.valueOf(DATA[11]));
        final HashMap<String, HashMap<String, Integer>> SCORING = new HashMap<>();
        for(int i = 0; i < 6; i++) {
            final int INDEX = i;
            final HashMap SCORING_MAP = new HashMap<String, Integer>() {
                {
                    put("Inner", Integer.valueOf(DATA[5*INDEX+12]));
                    put("Outer", Integer.valueOf(DATA[5*INDEX+13]));
                    put("Bottom", Integer.valueOf(DATA[5*INDEX+14]));
                    put("High", Integer.valueOf(DATA[5*INDEX+15]));
                    put("Low", Integer.valueOf(DATA[5*INDEX+16]));
                }
            };
            SCORING.put(String.format("Position: %d", INDEX), SCORING_MAP);
        }
        matchData.setScoring(SCORING);
        matchData.setRotationControl(Boolean.valueOf(DATA[42]));
        matchData.setColorControl(Boolean.valueOf(DATA[43]));
        matchData.setAttemptedClimb(Boolean.valueOf(DATA[44]));
        matchData.setClimb(Boolean.valueOf(DATA[45]));
        matchData.setLevel(Boolean.valueOf(DATA[46]));
        matchData.setAttemptedDoubleClimb(Boolean.valueOf(DATA[47]));
        matchData.setDoubleClimb(Boolean.valueOf(DATA[48]));
        matchData.setBrownedOut(Boolean.valueOf(DATA[49]));
        matchData.setDisabled(Boolean.valueOf(DATA[50]));
        matchData.setYellowCard(Boolean.valueOf(DATA[51]));
        matchData.setRedCard(Boolean.valueOf(DATA[52]));
        matchData.setNotes((DATA[53]));
        return matchData;
    }

    @SuppressLint("DefaultLocale")
    private void exportJSON() {
        final String MATCH = DATA_FILES[0].split(",")[1];
        final JSONArray JSON_ARRAY = new JSONArray();
        for(int i = 0; i < 6; i++) {
            Map<String, Object> JSONMap = new HashMap<>();
            final String[] DATA = DATA_FILES[i].split(",");
            JSONMap.put("Teams", DATA[0]);
            JSONMap.put("Match", DATA[1]);
            JSONMap.put("Color", DATA[2].toLowerCase());
            JSONMap.put("Driver Station", Integer.valueOf(DATA[3]));
            final HashMap<String, Integer> AUTO_HITS = new HashMap<String, Integer>() {
                {
                    put("Inner", Integer.valueOf(DATA[4]));
                    put("Outer", Integer.valueOf(DATA[5]));
                    put("Bottom", Integer.valueOf(DATA[6]));
                }
            };
            JSONMap.put("Auto Hits", AUTO_HITS);
            final HashMap<String, Integer> AUTO_MISS = new HashMap<String, Integer>() {
                {
                    put("High", Integer.valueOf(DATA[7]));
                    put("Low", Integer.valueOf(DATA[8]));
                }
            };
            JSONMap.put("Auto Miss", AUTO_MISS);
            JSONMap.put("Time Playing Defense", (Integer.valueOf(DATA[9])));
            JSONMap.put("Time Defense On Team", (Integer.valueOf(DATA[9])));
            JSONMap.put("Penalties", (Integer.valueOf(DATA[11])));
            final HashMap<String, HashMap<String, Integer>> SCORING = new HashMap<>();
            for(int x = 0; x < 6; x++) {
                final int INDEX = x;
                final HashMap SCORING_MAP = new HashMap<String, Integer>() {
                    {
                        put("Inner", Integer.valueOf(DATA[5*INDEX+12]));
                        put("Outer", Integer.valueOf(DATA[5*INDEX+13]));
                        put("Bottom", Integer.valueOf(DATA[5*INDEX+14]));
                        put("High", Integer.valueOf(DATA[5*INDEX+15]));
                        put("Low", Integer.valueOf(DATA[5*INDEX+16]));
                    }
                };
                SCORING.put(String.format("Position: %d", INDEX), SCORING_MAP);
            }
            JSONMap.put("Scoring", SCORING);
            JSONMap.put("Rotation Control", (Boolean.valueOf(DATA[42])));
            JSONMap.put("Color Control", (Boolean.valueOf(DATA[43])));
            JSONMap.put("Attempted CLimb", (Boolean.valueOf(DATA[44])));
            JSONMap.put("Climb", (Boolean.valueOf(DATA[45])));
            JSONMap.put("Level", (Boolean.valueOf(DATA[46])));
            JSONMap.put("Attempted Double Climb", (Boolean.valueOf(DATA[47])));
            JSONMap.put("Double Climb", (Boolean.valueOf(DATA[48])));
            JSONMap.put("Browned Out", (Boolean.valueOf(DATA[49])));
            JSONMap.put("Disabled", (Boolean.valueOf(DATA[50])));
            JSONMap.put("Yellow Card", (Boolean.valueOf(DATA[51])));
            JSONMap.put("Red Card", (Boolean.valueOf(DATA[52])));
            JSONMap.put("Notes", (DATA[53]));
            JSON_ARRAY.put(new JSONObject(JSONMap));
        }
        try {
            final File JSON_FILE = new File((Environment.getExternalStorageDirectory() + "/THawkScouting"), (MATCH + ".txt"));
            final PrintWriter PRINT_WRITER = new PrintWriter(new FileOutputStream(JSON_FILE, false));
            PRINT_WRITER.println(JSON_ARRAY.toString(0));
            PRINT_WRITER.flush();
            PRINT_WRITER.close();
        }
        catch (org.json.JSONException e) {
            Toast.makeText(getApplicationContext(), "JSON error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
        catch (IOException e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
