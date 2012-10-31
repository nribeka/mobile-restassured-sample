package com.nribeka.search.activities;

import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;
import com.burkeware.search.api.module.SearchModule;
import com.burkeware.search.api.service.SearchService;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.nribeka.search.R;
import com.nribeka.search.adapter.EncounterAdapter;
import com.nribeka.search.module.AndroidModule;
import com.nribeka.search.sample.Observation;
import com.nribeka.search.sample.Patient;
import com.nribeka.search.util.Constants;
import com.nribeka.search.util.FileUtils;

import java.util.ArrayList;

public class ObservationTimelineActivity extends ListActivity {

    private Patient patient;

    private String fieldUuid;

    private String fieldName;

    private ArrayList<Observation> observations = new ArrayList<Observation>();

    private ArrayAdapter<Observation> observationAdapter;

    private Injector injector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.observation_timeline);

        Module searchModule = new SearchModule();
        Module androidModule = new AndroidModule();
        injector = Guice.createInjector(searchModule, androidModule);

        if (!FileUtils.storageReady()) {
            showCustomToast(getString(R.string.error, R.string.storage_error));
            finish();
        }

        String patientUuid = getIntent().getStringExtra(Constants.KEY_PATIENT_ID);
        patient = getPatient(patientUuid);

        fieldUuid = getIntent().getStringExtra(Constants.KEY_OBSERVATION_FIELD_ID);
        fieldName = getIntent().getStringExtra(Constants.KEY_OBSERVATION_FIELD_NAME);

        setTitle(getString(R.string.app_name) + " > " + getString(R.string.view_patient_detail));

        TextView textView = (TextView) findViewById(R.id.title_text);
        textView.setText(fieldName);
    }

    private Patient getPatient(final String uuid) {
        SearchService searchService = injector.getInstance(SearchService.class);
        return searchService.getObject(Patient.class, "uuid:" + uuid);
    }

    private void getObservations(final String uuid, final String fieldUuid) {
        SearchService searchService = injector.getInstance(SearchService.class);
        observations.addAll(searchService.getObjects(Observation.class,
                "patient_uuid:" + uuid + " AND concept_uuid:" + fieldUuid));
        observationAdapter = new EncounterAdapter(this, R.layout.encounter_list_item, observations);
        setListAdapter(observationAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (patient != null && fieldUuid != null) {
            getObservations(patient.getUuid(), fieldUuid);
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
