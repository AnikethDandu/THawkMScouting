package com.frc.thawkmscouting;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
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
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Reads 6 QR codes, pushes the data to DynamoDB, and stores the data locally in JSON format
 *
 * @author Aniketh Dandu - Team 1100
 */
public class MainActivity extends AppCompatActivity {
    // TODO: CLEAN UP SCHEMA FILE

    private static final int MY_PERMISSION_WRITE_EXTERNAL_STORAGE = 1;
    private static final String TAG = MainActivity.class.getSimpleName();
    private final String[] DATA_FILES = new String[6];
    private final TextView[] FILE_LABELS = new TextView[6];
    private DynamoDBMapper m_dynamoDBMapper;
    private JSONArray m_jsonArray = new JSONArray();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.i(TAG, (new File((Environment.getExternalStorageDirectory() + "/THawkScouting")).mkdirs())
                ? "Directory successfully created"
                : "Directory not created");

        final String ID = "ENTER_COGNITO_USER_POOL_ID";
        final Regions REGION = Regions.valueOf("default-name");

        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSION_WRITE_EXTERNAL_STORAGE);

        AWSMobileClient.getInstance().initialize(MainActivity.this, (awsStartupResult) -> {
            CognitoCachingCredentialsProvider cognitoCachingCredentialsProvider = new CognitoCachingCredentialsProvider(
                    getApplicationContext(),
                    ID,
                    REGION
            );

            AmazonDynamoDBClient ddbClient = Region.getRegion(REGION)
                    .createClient(
                            AmazonDynamoDBClient.class,
                            cognitoCachingCredentialsProvider,
                            new ClientConfiguration()
                    );

            m_dynamoDBMapper = DynamoDBMapper.builder()
                    .dynamoDBClient(ddbClient)
                    .awsConfiguration(
                            AWSMobileClient.getInstance().getConfiguration())
                    .build();
        }).execute();

        FILE_LABELS[0] = findViewById(R.id.main_text_qr_1);
        FILE_LABELS[1] = findViewById(R.id.main_text_qr_2);
        FILE_LABELS[2] = findViewById(R.id.main_text_qr_3);
        FILE_LABELS[3] = findViewById(R.id.main_text_qr_4);
        FILE_LABELS[4] = findViewById(R.id.main_text_qr_5);
        FILE_LABELS[5] = findViewById(R.id.main_text_qr_6);

        final EditText REMOVE_NUMBER_TEXT = findViewById(R.id.main_button_remove_index);
        REMOVE_NUMBER_TEXT.setText("0");

        final Button REMOVE_BUTTON = findViewById(R.id.main_button_remove_qr);
        REMOVE_BUTTON.setOnClickListener((View view) -> {
            final int QR_REMOVE_INDEX = (REMOVE_NUMBER_TEXT.getText().toString().equals(""))
                    ? 0
                    : Integer.valueOf(REMOVE_NUMBER_TEXT.getText().toString()) - 1;

            if (QR_REMOVE_INDEX <= 5 && QR_REMOVE_INDEX >= 0) {
                if (DATA_FILES[QR_REMOVE_INDEX] != null)
                    clearQRCell(QR_REMOVE_INDEX);
            } else if (QR_REMOVE_INDEX == -1) {
                for (int i = 5; i > -1; i--) {
                    if (DATA_FILES[i] != null) {
                        clearQRCell(i);
                        break;
                    }
                }
            }
        });

        final Button EXPORT_BUTTON = findViewById(R.id.main_button_export);
        EXPORT_BUTTON.setOnClickListener((View view) ->
            uploadData()
        );

        final Button SCAN_BUTTON = findViewById(R.id.main_button_scan_qr);
        SCAN_BUTTON.setOnClickListener((View view) -> {
            final IntentIntegrator INTEGRATOR = new IntentIntegrator(MainActivity.this);
            INTEGRATOR.initiateScan();
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        final IntentResult RESULT = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (RESULT != null)
            if (RESULT.getContents() == null)
                Toast.makeText(MainActivity.this, getString(R.string.error_empty_qr), Toast.LENGTH_LONG).show();
            else {
                boolean slotOpen = false;
                for (int i = 0; i < 6; i++) {
                    if (DATA_FILES[i] == null || DATA_FILES[i].equals("")) {
                        slotOpen = true;
                        DATA_FILES[i] = RESULT.getContents();
                        String[] DATA_ARRAY = DATA_FILES[i].split(",");
                        FILE_LABELS[i].setTextColor((DATA_ARRAY[2].toLowerCase().equals("red"))
                                ? getResources().getColor(R.color.allianceRed)
                                : getResources().getColor(R.color.allianceBlue));
                        FILE_LABELS[i].setText(String.format("Match %s: %s", DATA_ARRAY[1], DATA_ARRAY[0]));
                        break;
                    }
                }
                if (!slotOpen)
                    Toast.makeText(MainActivity.this, getString(R.string.error_overflow_qr), Toast.LENGTH_LONG).show();
            }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
    private void uploadData() {
        try {
            if(checkInternet()) {
                new Thread(() -> {
                    for(int i = 0; i < 6; i++) {
                        final String[] DATA = DATA_FILES[i].split(",");
                        m_jsonArray.put(createJSONObject(DATA));
                        exportJSON(DATA[1]);
                        m_dynamoDBMapper.save(returnMatch(DATA));
                    }
                }).start();
                Toast.makeText(getApplicationContext(), getString(R.string.text_wifi_updated), Toast.LENGTH_LONG).show();
            }
            else
                Toast.makeText(getApplicationContext(), getString(R.string.text_no_wifi_updated), Toast.LENGTH_LONG).show();
        }
        catch (NullPointerException e) {
            Toast.makeText(getApplicationContext(), getString(R.string.error_less_than_6_qr), Toast.LENGTH_LONG).show();
        }
        catch (Exception e) {
            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private MatchData returnMatch(String[] data) {
        final MatchData MATCH_DATA = new MatchData();
        MATCH_DATA.setTeam(Integer.valueOf(data[0]));
        MATCH_DATA.setMatch(Integer.valueOf(data[1]));
        MATCH_DATA.setColor(data[2].toLowerCase());
        MATCH_DATA.setDriverStation(Integer.valueOf(data[3]));
        MATCH_DATA.setCrossedLine(Boolean.valueOf(data[4]));
        MATCH_DATA.setAutoHits(getAutoHits(data));
        MATCH_DATA.setAutoMiss(getAutoMiss(data));
        MATCH_DATA.setTimePlayingDefense(Integer.valueOf(data[10]));
        MATCH_DATA.setTimeDefenseOnTeam(Integer.valueOf(data[11]));
        MATCH_DATA.setPenalties(Integer.valueOf(data[12]));
        MATCH_DATA.setScoring(getScoring(data));
        MATCH_DATA.setRotationControl(Boolean.valueOf(data[43]));
        MATCH_DATA.setColorControl(Boolean.valueOf(data[44]));
        MATCH_DATA.setAttemptedClimb(Boolean.valueOf(data[45]));
        MATCH_DATA.setClimb(Boolean.valueOf(data[46]));
        MATCH_DATA.setLevel(Boolean.valueOf(data[47]));
        MATCH_DATA.setAttemptedDoubleClimb(Boolean.valueOf(data[48]));
        MATCH_DATA.setDoubleClimb(Boolean.valueOf(data[49]));
        MATCH_DATA.setBrownedOut(Boolean.valueOf(data[50]));
        MATCH_DATA.setDisabled(Boolean.valueOf(data[51]));
        MATCH_DATA.setYellowCard(Boolean.valueOf(data[52]));
        MATCH_DATA.setRedCard(Boolean.valueOf(data[53]));
        MATCH_DATA.setName(data[54]);
        MATCH_DATA.setNotes(data[55]);
        return MATCH_DATA;
    }

    private JSONObject createJSONObject(String[] data) {
        final Map<String, Object> JSONMap = new HashMap<>();
        JSONMap.put("Team", Integer.valueOf(data[0]));
        JSONMap.put("Match", Integer.valueOf(data[1]));
        JSONMap.put("Color", String.valueOf(data[2]).toLowerCase());
        JSONMap.put("Driver Station", Integer.valueOf(data[3]));
        JSONMap.put("Crossed Line", Boolean.valueOf(data[4]));
        JSONMap.put("Auto Hits", getAutoHits(data));
        JSONMap.put("Auto Miss", getAutoMiss(data));
        JSONMap.put("Time Playing Defense", (Integer.valueOf(data[10])));
        JSONMap.put("Time Defense On Team", (Integer.valueOf(data[11])));
        JSONMap.put("Penalties", (Integer.valueOf(data[12])));
        JSONMap.put("Scoring", getScoring(data));
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

    private void exportJSON(String match) {
        try {
            final File JSON_FILE = new File(Environment.getExternalStorageDirectory() + "/THawkScouting", (match + ".txt"));
            Log.d(TAG, String.valueOf(JSON_FILE.exists()));
            final PrintWriter PRINT_WRITER = new PrintWriter(new FileOutputStream(JSON_FILE, false));
            PRINT_WRITER.println(m_jsonArray.toString(0));
            PRINT_WRITER.flush();
            PRINT_WRITER.close();
        }
        catch (org.json.JSONException e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
        catch (IOException e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private HashMap<String, Integer> getAutoHits(String[] data) {
        final HashMap<String, Integer> AUTO_HITS = new HashMap<>();
        AUTO_HITS.put("Inner", Integer.valueOf(data[5]));
        AUTO_HITS.put("Outer", Integer.valueOf(data[6]));
        AUTO_HITS.put("Bottom", Integer.valueOf(data[7]));
        return AUTO_HITS;
    }

    private HashMap<String, Integer> getAutoMiss(String[] data) {
        final HashMap<String, Integer> AUTO_MISS = new HashMap<>();
        AUTO_MISS.put("High", Integer.valueOf(data[8]));
        AUTO_MISS.put("Low", Integer.valueOf(data[9]));
        return AUTO_MISS;
    }

    @SuppressWarnings("unchecked")
    private HashMap<String, HashMap<String, Integer>> getScoring(String[] data) {
        final HashMap<String, HashMap<String, Integer>> SCORING = new HashMap<>();
        for(int i = 0; i < 6; i++) {
            final int INDEX = i;
            final HashMap SCORING_MAP = new HashMap<String, Integer>() {
                {
                    put("Inner", Integer.valueOf(data[5*INDEX+13]));
                    put("Outer", Integer.valueOf(data[5*INDEX+14]));
                    put("Bottom", Integer.valueOf(data[5*INDEX+15]));
                    put("High", Integer.valueOf(data[5*INDEX+16]));
                    put("Low", Integer.valueOf(data[5*INDEX+17]));
                }
            };
            SCORING.put("Position: " + INDEX, SCORING_MAP);
        }
        return SCORING;
    }

    private void clearQRCell(int index) {
        FILE_LABELS[index].setText(getString(R.string.text_empty_qr_slot));
        DATA_FILES[index] = null;
        FILE_LABELS[index].setTextColor(Color.WHITE);
    }

    private boolean checkInternet() {
        try
        {
            final URL URL = new URL("https://aws.amazon.com");
            HttpURLConnection HTTP_URL_CONNECTION = (HttpURLConnection) URL.openConnection();
            HTTP_URL_CONNECTION.setRequestProperty("User-Agent", "Android Application:1");
            HTTP_URL_CONNECTION.setRequestProperty("Connection", "close");
            HTTP_URL_CONNECTION.setConnectTimeout(1000 * 30);
            HTTP_URL_CONNECTION.connect();

            return (HTTP_URL_CONNECTION.getResponseCode() == 200 || HTTP_URL_CONNECTION.getResponseCode() > 400);
        }
        catch (Exception e)
        {
            return false;
        }
    }
}