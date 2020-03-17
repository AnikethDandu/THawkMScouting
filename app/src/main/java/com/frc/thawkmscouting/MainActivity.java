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
import com.frc.thawkmscouting.databinding.ActivityMainBinding;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.regions.Region;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

import org.json.JSONArray;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Reads 6 QR codes, pushes the data to DynamoDB, and stores the data locally in JSON format
 *
 * @author Aniketh Dandu - FRC Team 1100
 */
public class MainActivity extends AppCompatActivity {
    // TODO: DOCUMENT CODE

    // **************************************************
    // Constants
    // **************************************************

    /**
     * Integer for permissions to write to external storage
     */
    private static final int MY_PERMISSION_WRITE_EXTERNAL_STORAGE = 1;

    /**
     * Name of the class for Logs
     */
    private static final String MAIN_ACTIVITY_TAG = MainActivity.class.getSimpleName();

    // **************************************************
    // Private fields
    // **************************************************

    /**
     * String array holding values read from QR codes
     */
    private final String[] DATA_FILES = new String[6];

    /**
     * TextView array holding labels for each QR code
     */
    private final TextView[] FILE_LABELS = new TextView[6];

    /**
     * Object mapper for AWS DynamoDB
     */
    private DynamoDBMapper m_dynamoDBMapper;

    /**
     * JSON array to hold JSON objects created from each QR code
     */
    private JSONArray m_jsonArray = new JSONArray();

    // **************************************************
    // Protected methods
    // **************************************************

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ActivityMainBinding BINDING = ActivityMainBinding.inflate(getLayoutInflater());
        final View VIEW = BINDING.getRoot();
        setContentView(VIEW);

        // Checks for permission to write to external storage
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSION_WRITE_EXTERNAL_STORAGE);

        // Attempts to create a directory to store JSON files and logs the result
        Log.i(MAIN_ACTIVITY_TAG, (new File(Environment.getExternalStorageDirectory() + "/THawkScouting").mkdirs())
                ? "Directory successfully created"
                : "Directory not created");

        // Creates the AWS client and Object mapper
        AWSMobileClient.getInstance().initialize(MainActivity.this, (awsStartupResult) -> {
            CognitoCachingCredentialsProvider cognitoCachingCredentialsProvider = new CognitoCachingCredentialsProvider(
                    getApplicationContext(),
                    BuildConfig.ID,
                    Regions.fromName(BuildConfig.REGION)
            );

            AmazonDynamoDBClient ddbClient = Region.getRegion(Regions.fromName(BuildConfig.REGION))
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

        // Binds the QR code labels
        FILE_LABELS[0] = BINDING.mainTextQr1;
        FILE_LABELS[1] = BINDING.mainTextQr2;
        FILE_LABELS[2] = BINDING.mainTextQr3;
        FILE_LABELS[3] = BINDING.mainTextQr4;
        FILE_LABELS[4] = BINDING.mainTextQr5;
        FILE_LABELS[5] = BINDING.mainTextQr6;

        // Bnds and defaults the remove index button
        final EditText REMOVE_NUMBER_TEXT = BINDING.mainButtonRemoveIndex;
        REMOVE_NUMBER_TEXT.setText("0");

        // Binds the button to remove QR codes
        final Button REMOVE_BUTTON = BINDING.mainButtonRemoveQr;
        REMOVE_BUTTON.setOnClickListener((View view) -> {
            // Set the value of the remove index
            final int QR_REMOVE_INDEX = (REMOVE_NUMBER_TEXT.getText().toString().equals(""))
                    ? 0
                    : Integer.parseInt(REMOVE_NUMBER_TEXT.getText().toString()) - 1;

            // If the index matches the indices of the QR slots, clear the corresponding slot
            if (QR_REMOVE_INDEX <= 5 && QR_REMOVE_INDEX >= 0) {
                if (DATA_FILES[QR_REMOVE_INDEX] != null)
                    clearQRCell(QR_REMOVE_INDEX);
            } else if (QR_REMOVE_INDEX == -1) {
                // If the index is 0, clear the last slot with data present
                for (int i = 5; i > -1; i--) {
                    if (DATA_FILES[i] != null) {
                        clearQRCell(i);
                        break;
                    }
                }
            }
        });

        // Bind the export button
        final Button EXPORT_BUTTON = BINDING.mainButtonExport;
        EXPORT_BUTTON.setOnClickListener((View view) ->
                uploadData()
        );

        // Bind the scan button
        // If the scan button is pressed, initiate a scan with the current context
        final Button SCAN_BUTTON = BINDING.mainButtonScanQr;
        SCAN_BUTTON.setOnClickListener((View view) -> {
            final IntentIntegrator INTEGRATOR = new IntentIntegrator(MainActivity.this);
            INTEGRATOR.initiateScan();
        });
    }

    /**
     * Opens the camera and records the result of a scanned QR code
     * Checks if data exists in the QR code
     * Checks if there is an open space for the data
     * If there is space, updates the array at the desired index and the corresponding label
     * If there is no space, notifies the user
     *
     * @param requestCode The request code to access the camera
     * @param resultCode The result code after the QR code is scanned
     * @param data The data stored in the QR code
     */
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

    // **************************************************
    // Private methods
    // **************************************************

    /**
     * Creates a new thread to push data
     * Saves data in the internal storage in JSON format regardless of Internet
     * If an Internet connection exists, pushes data to AWS DynamoDB
     * Else notifies the user
     *
     */
    private void uploadData() {
        try {
            new Thread(() -> {
                for (int i = 0; i < 6; i++) {
                    final String[] DATA = DATA_FILES[i].split(",");
                    m_jsonArray.put(JsonData.createJSONObject(DATA));
                    JsonData.exportJSON(DATA[1], m_jsonArray, getApplicationContext());
                    if (checkInternet())
                        m_dynamoDBMapper.save(returnMatch(DATA));
                }
            }).start();
            Toast.makeText(getApplicationContext(), (checkInternet())
                    ? getString(R.string.text_wifi_updated)
                    : getString(R.string.text_no_wifi_updated),Toast.LENGTH_LONG).show();
        } catch (NullPointerException e) {
            Toast.makeText(getApplicationContext(), getString(R.string.error_less_than_6_qr), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Wipe a QR code slot and update the corresponding label
     *
     * @param index The index of the QR code to be cleared
     */
    private void clearQRCell(int index) {
        FILE_LABELS[index].setText(getString(R.string.text_empty_qr_slot));
        DATA_FILES[index] = null;
        FILE_LABELS[index].setTextColor(Color.WHITE);
    }

    /**
     * Creates a MatchData object that can be pushed to AWS DynamoDB
     * Populates the object using values read from the QR code
     *
     * @param data The String array of data read from the QR code
     * @return Returns the MatchData object
     */
    private MatchData returnMatch(String[] data) {
        final MatchData MATCH_DATA = new MatchData();
        MATCH_DATA.setTeam(Integer.parseInt(data[0]));
        MATCH_DATA.setMatch(Integer.parseInt(data[1]));
        MATCH_DATA.setColor(data[2].toLowerCase());
        MATCH_DATA.setDriverStation(Integer.parseInt(data[3]));
        MATCH_DATA.setCrossedLine(Boolean.parseBoolean(data[4]));
        MATCH_DATA.setAutoHits(HashMapData.getAutoHits(data));
        MATCH_DATA.setAutoMiss(HashMapData.getAutoMiss(data));
        MATCH_DATA.setTimePlayingDefense(Integer.parseInt(data[10]));
        MATCH_DATA.setTimeDefenseOnTeam(Integer.parseInt(data[11]));
        MATCH_DATA.setPenalties(Integer.parseInt(data[12]));
        MATCH_DATA.setScoring(HashMapData.getScoring(data));
        MATCH_DATA.setRotationControl(Boolean.parseBoolean(data[43]));
        MATCH_DATA.setColorControl(Boolean.parseBoolean(data[44]));
        MATCH_DATA.setAttemptedClimb(Boolean.parseBoolean(data[45]));
        MATCH_DATA.setClimb(Boolean.parseBoolean(data[46]));
        MATCH_DATA.setLevel(Boolean.parseBoolean(data[47]));
        MATCH_DATA.setAttemptedDoubleClimb(Boolean.parseBoolean(data[48]));
        MATCH_DATA.setDoubleClimb(Boolean.parseBoolean(data[49]));
        MATCH_DATA.setBrownedOut(Boolean.parseBoolean(data[50]));
        MATCH_DATA.setDisabled(Boolean.parseBoolean(data[51]));
        MATCH_DATA.setYellowCard(Boolean.parseBoolean(data[52]));
        MATCH_DATA.setRedCard(Boolean.parseBoolean(data[53]));
        MATCH_DATA.setName(data[54]);
        MATCH_DATA.setNotes(data[55]);
        return MATCH_DATA;
    }

    /**
     * Creates an HTTP request to AWS
     *
     * @return Returns the success of the connection
     */
    private boolean checkInternet() {
        try {
            final URL URL = new URL("https://aws.amazon.com");
            HttpURLConnection HTTP_URL_CONNECTION = (HttpURLConnection) URL.openConnection();
            HTTP_URL_CONNECTION.setRequestProperty("User-Agent", "Android Application:1");
            HTTP_URL_CONNECTION.setRequestProperty("Connection", "close");
            HTTP_URL_CONNECTION.setConnectTimeout(1000 * 30);
            HTTP_URL_CONNECTION.connect();

            return (HTTP_URL_CONNECTION.getResponseCode() == 200 || HTTP_URL_CONNECTION.getResponseCode() > 400);
        } catch (Exception e) {
            return false;
        }
    }
}