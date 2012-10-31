package com.nribeka.search.activities;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.burkeware.search.api.module.SearchModule;
import com.burkeware.search.api.service.SearchService;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.nribeka.search.R;
import com.nribeka.search.adapter.ObservationAdapter;
import com.nribeka.search.module.AndroidModule;
import com.nribeka.search.sample.Observation;
import com.nribeka.search.sample.Patient;
import com.nribeka.search.util.Constants;
import com.nribeka.search.util.FileUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class ViewPatientActivity extends ListActivity {

    private Injector injector;

    private static Patient patient;

    private static ArrayList<Observation> observations = new ArrayList<Observation>();

    private ArrayAdapter<Observation> observationAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_patient);

        setListAdapter(observationAdapter);

        Module searchModule = new SearchModule();
        Module androidModule = new AndroidModule();
        injector = Guice.createInjector(searchModule, androidModule);

        if (!FileUtils.storageReady()) {
            showCustomToast(getString(R.string.error, R.string.storage_error));
            finish();
        }

        String patientUuid = getIntent().getStringExtra(Constants.KEY_PATIENT_ID);
        patient = getPatient(patientUuid);

        setTitle(getString(R.string.app_name) + " > " + getString(R.string.view_patient));

        View patientView = findViewById(R.id.patient_info);
        patientView.setBackgroundResource(R.drawable.search_gradient);

        TextView textView = (TextView) findViewById(R.id.identifier_text);
        String[] identifierElements = patient.getIdentifier().split("=");
        textView.setText(identifierElements[1].trim());

        textView = (TextView) findViewById(R.id.name_text);
        textView.setText(patient.getName());

        DateFormat df = new SimpleDateFormat("MMM dd, yyyy");
        textView = (TextView) findViewById(R.id.birthdate_text);
        textView.setText(df.format(patient.getBirthdate()));

        ImageView imageView = (ImageView) findViewById(R.id.gender_image);
        if (imageView != null) {
            if (patient.getGender().equals("M")) {
                imageView.setImageResource(R.drawable.male);
            } else if (patient.getGender().equals("F")) {
                imageView.setImageResource(R.drawable.female);
            }
        }
    }

    private Patient getPatient(final String uuid) {
        SearchService searchService = injector.getInstance(SearchService.class);
        return  searchService.getObject(Patient.class, "uuid:" + uuid);
    }

    private void getAllObservations(final String uuid) {
        SearchService searchService = injector.getInstance(SearchService.class);
        observations.addAll(searchService.getObjects(Observation.class, String.valueOf("patient_uuid:" + uuid)));
        observationAdapter = new ObservationAdapter(this, R.layout.observation_list_item, observations);
        setListAdapter(observationAdapter);
    }

    @Override
    protected void onListItemClick(ListView listView, View view, int position, long id) {

        if (patient != null) {
            Observation obs = (Observation) getListAdapter().getItem(position);

            Intent ip;
            int dataType = obs.getDataType();
            if (dataType == Constants.TYPE_INT || dataType == Constants.TYPE_FLOAT) {
                ip = new Intent(getApplicationContext(), ObservationChartActivity.class);
                ip.putExtra(Constants.KEY_PATIENT_ID, patient.getUuid());
                ip.putExtra(Constants.KEY_OBSERVATION_FIELD_ID, obs.getFieldUuid());
                startActivity(ip);
            } else {
                ip = new Intent(getApplicationContext(), ObservationTimelineActivity.class);
                ip.putExtra(Constants.KEY_PATIENT_ID, patient.getUuid());
                ip.putExtra(Constants.KEY_OBSERVATION_FIELD_ID, obs.getFieldUuid());
                startActivity(ip);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (patient != null) {
            getAllObservations(patient.getUuid());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
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