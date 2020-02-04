package com.frc.thawkmscouting;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

/**
 * Combines data from 6 QR codes and pushes the data to Firebase
 *
 * @author Aniketh Dandu - Team 1100
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    /* The data from the six QR codes */
    final String[] DATA_FILES = new String[6];
    /* The labels for the six files */
    final TextView[] FILE_LABELS = new TextView[6];

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

        /* ID the QR button and set a listener */
        final Button SCAN_BUTTON = findViewById(R.id.button);
        SCAN_BUTTON.setOnClickListener(this);

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
        findViewById(R.id.subtractBttn).setOnClickListener(new View.OnClickListener() {
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

        /* When the export button is clicked, push the data to Firebase */
        findViewById(R.id.exportButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadData();
            }
        });
    }

    /**
     * If the QR button is clicked, grab the data
     *
     * @param view The view of the clicked button
      */
    @Override
    public void onClick(View view) {
        if (view.getId() == (R.id.button)) {
            final IntentIntegrator INTEGRATOR = new IntentIntegrator(this);
            INTEGRATOR.initiateScan();
        }
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
                            final int COLOR = DATA_ARRAY[2].equals("red")
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
     * Upload data to Firebase and update a list of teams
     */
    private void uploadData() {
        try {
            /* The event entered by the user */
            String event = ((TextView)findViewById(R.id.eventText)).getText().toString();
            /* If the user did not enter an event, notify the user and set a default name */
            if (event.isEmpty()) {
                Toast.makeText(MainActivity.this,"Entered empty event", Toast.LENGTH_LONG).show();
                event = "Non-existent event";
            }

            /* Create OR locate a temporary file to hold the list of teams who have data */
            final File TEAMS_LIST = File.createTempFile(event, ".txt", this.getFilesDir());
            /* Create a reader for the file */
            final BufferedReader BUFFERED_READER = new BufferedReader(new FileReader(TEAMS_LIST));
            /* Create a map containing all the scouted teams */
            final HashMap<String, Object> TEAMS = new HashMap<>();

            /* Update the map with previously entered teams */
            for (int i = 0; i < TEAMS_LIST.length(); i++) {
                TEAMS.put(String.valueOf(i+1), BUFFERED_READER.readLine());
            }

            /* Create a reference to the database and document */
            final DocumentReference REFERENCE = FirebaseFirestore.getInstance()
                    .collection("Events")
                    .document(event);
            /* Loop through the six strings of data */
            for(int i = 0; i < 6; i++) {
                /* Split the data into an array */
                final String DATA = DATA_FILES[i];
                /* Set the team to the first item in the array */
                final String TEAM = DATA.split(",")[0];
                final String MATCH = DATA.split(",")[1];
                /* Locate the correct reference to the specified team and push the corresponding data */
                REFERENCE.collection(TEAM).document("Match data").set(returnData(DATA, MATCH), SetOptions.merge());
                if (!TEAMS.containsValue(TEAM)) {
                    /* If the list of teams does not already contain the team, update the list */
                    TEAMS.put(String.valueOf(TEAMS.size()+1), TEAM);
                }
            }
            /* Update the database with the list of teams who have been scouted */
            REFERENCE.set(TEAMS);

            Toast.makeText(MainActivity.this, "Database updated", Toast.LENGTH_LONG).show();
        }
        /* Display error message if user did not scan six QR codes */
        catch (NullPointerException e) {
            Toast.makeText(MainActivity.this, "One or more empty data slots", Toast.LENGTH_LONG).show();
        }
        /* Display an error message for any other error */
        catch (Exception e) {
            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /* Return inputted data as a map */
    @SuppressLint("DefaultLocale")
    private Map returnData(String values, String match) {
        Map <String, Object> data = new HashMap<>();
        data.put(String.format("Match %s", match), values);
        return data;
    }
}