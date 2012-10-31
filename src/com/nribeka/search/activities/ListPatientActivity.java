package com.nribeka.search.activities;

import android.app.ListActivity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.burkeware.search.api.JsonLuceneConfig;
import com.burkeware.search.api.module.SearchModule;
import com.burkeware.search.api.service.ConfigService;
import com.burkeware.search.api.service.SearchService;
import com.burkeware.search.api.util.JsonLuceneUtil;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.jayway.jsonpath.JsonPath;
import com.nribeka.search.R;
import com.nribeka.search.adapter.PatientAdapter;
import com.nribeka.search.module.AndroidModule;
import com.nribeka.search.sample.Observation;
import com.nribeka.search.sample.ObservationAlgorithm;
import com.nribeka.search.sample.Patient;
import com.nribeka.search.sample.PatientAlgorithm;
import com.nribeka.search.task.PatientLoaderTask;
import com.nribeka.search.util.Constants;
import com.nribeka.search.util.FileUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class ListPatientActivity extends ListActivity {

    private static final int MENU_PREFERENCES = Menu.FIRST;

    private static final String DOWNLOAD_PATIENT_CANCELED_KEY = "downloadPatientCanceled";

    public static final int BARCODE_CAPTURE = 2;

    public static final int DOWNLOAD_PATIENT = 1;

    private EditText editText;

    private TextWatcher textWatcher;

    private ArrayList<Patient> patients = new ArrayList<Patient>();

    private ArrayAdapter<Patient> patientAdapter;

    private boolean downloadCanceled = false;

    private Injector injector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.find_patient);

        setListAdapter(patientAdapter);

        Module searchModule = new SearchModule();
        Module androidModule = new AndroidModule();
        injector = Guice.createInjector(searchModule, androidModule);

        ConfigService configService = injector.getInstance(ConfigService.class);
        configService.registerAlgorithm(Patient.class, new PatientAlgorithm());
        configService.registerAlgorithm(Observation.class, new ObservationAlgorithm());

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(DOWNLOAD_PATIENT_CANCELED_KEY)) {
                downloadCanceled = savedInstanceState.getBoolean(DOWNLOAD_PATIENT_CANCELED_KEY);
            }
        }

        setTitle(getString(R.string.app_name) + " > " + getString(R.string.find_patient));

        if (!FileUtils.storageReady()) {
            showCustomToast(getString(R.string.error, getString(R.string.storage_error)));
            finish();
        }

        textWatcher = new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                getPatients(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
        };

        editText = (EditText) findViewById(R.id.search_text);
        editText.addTextChangedListener(textWatcher);

        ImageButton barcodeButton = (ImageButton) findViewById(R.id.barcode_button);
        barcodeButton.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                Intent i = new Intent("com.google.zxing.client.android.SCAN");
                try {
                    startActivityForResult(i, BARCODE_CAPTURE);
                } catch (ActivityNotFoundException e) {
                    Toast t = Toast.makeText(getApplicationContext(),
                            getString(R.string.error, getString(R.string.barcode_error)),
                            Toast.LENGTH_LONG);
                    t.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                    t.show();
                }
            }
        });

        Button downloadButton = (Button) findViewById(R.id.download_patients);
        downloadButton.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                try {
                    SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                    String server = settings.getString(
                            PreferencesActivity.KEY_SERVER, getString(R.string.default_server));
                    String username = settings.getString(
                            PreferencesActivity.KEY_USERNAME, getString(R.string.default_username));
                    String password = settings.getString(
                            PreferencesActivity.KEY_PASSWORD, getString(R.string.default_password));

                    InputStream cohortConfigStream = getResources().openRawResource(R.raw.cohorttemplate);
                    String cohortConfig = readInputStream(cohortConfigStream);

                    InputStream observationConfigStream = getResources().openRawResource(R.raw.observationtemplate);
                    String observationConfig = readInputStream(observationConfigStream);

                    PatientLoaderTask patientLoaderTask = new PatientLoaderTask(injector);
                    patientLoaderTask.execute(server, username, password, cohortConfig, observationConfig);
                } catch (IOException e) {
                    Log.e("Win Log", "Exception caught when trying to prepare patient loader.", e);
                }
            }
        });
    }

    private String readInputStream(final InputStream inputStream) throws IOException {
        String line;
        BufferedReader reader = null;
        StringBuilder builder = new StringBuilder();
        try {
            reader = new BufferedReader(new InputStreamReader(inputStream));
            while ((line = reader.readLine()) != null)
                builder.append(line).append("\n");
        } finally {
            if (reader != null)
                reader.close();
        }
        return builder.toString();
    }

    @Override
    protected void onListItemClick(ListView listView, View view, int position, long id) {
        // Get selected patient
        Patient patient = (Patient) getListAdapter().getItem(position);
        String patientUuid = patient.getUuid();

        Intent ip = new Intent(getApplicationContext(), ViewPatientActivity.class);
        ip.putExtra(Constants.KEY_PATIENT_ID, patientUuid);
        startActivity(ip);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, MENU_PREFERENCES, 0,
                getString(R.string.server_preferences)).setIcon(android.R.drawable.ic_menu_preferences);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_PREFERENCES:
                Intent ip = new Intent(getApplicationContext(), PreferencesActivity.class);
                startActivity(ip);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

        if (resultCode == RESULT_CANCELED) {
            if (requestCode == DOWNLOAD_PATIENT) {
                downloadCanceled = true;
            }
            return;
        }

        if (requestCode == BARCODE_CAPTURE && intent != null) {
            String sb = intent.getStringExtra("SCAN_RESULT");
            if (sb != null && sb.length() > 0) {
                editText.setText(sb);
            }
        }
        super.onActivityResult(requestCode, resultCode, intent);

    }

    private void getPatients() {
        getPatients("*");
    }

    private void getPatients(final String searchStr) {
        patients.clear();
        SearchService searchService = injector.getInstance(SearchService.class);
        patients.addAll(searchService.getObjects(Patient.class, String.valueOf(searchStr + "*")));
        patientAdapter = new PatientAdapter(this, R.layout.patient_list_item, patients);
        setListAdapter(patientAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        editText.removeTextChangedListener(textWatcher);
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        boolean firstRun = settings.getBoolean(PreferencesActivity.KEY_FIRST_RUN, true);

        if (firstRun) {
            // Save first run status
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean(PreferencesActivity.KEY_FIRST_RUN, false);
            editor.commit();

            // Start preferences activity
            Intent ip = new Intent(getApplicationContext(), PreferencesActivity.class);
            startActivity(ip);

        } else {
            getPatients();
            editText.setText(editText.getText().toString());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(DOWNLOAD_PATIENT_CANCELED_KEY, downloadCanceled);
    }

    private void showCustomToast(String message) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.toast_view, null);

        // set the text in the view
        TextView tv = (TextView) view.findViewById(R.id.message);
        tv.setText(message);

        Toast t = new Toast(this);
        t.setView(view);
        t.setDuration(Toast.LENGTH_LONG);
        t.setGravity(Gravity.CENTER, 0, 0);
        t.show();
    }
}
