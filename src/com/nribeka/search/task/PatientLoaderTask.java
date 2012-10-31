/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */

package com.nribeka.search.task;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;
import com.burkeware.search.api.JsonLuceneConfig;
import com.burkeware.search.api.service.IndexService;
import com.burkeware.search.api.service.SearchService;
import com.burkeware.search.api.util.JsonLuceneUtil;
import com.google.inject.Injector;
import com.jayway.jsonpath.JsonPath;
import com.nribeka.search.R;
import com.nribeka.search.sample.Patient;

public class PatientLoaderTask extends AsyncTask<String, String, String> {

    private final Injector injector;

    public PatientLoaderTask(final Injector injector) {
        this.injector = injector;
    }

    @Override
    protected String doInBackground(String... strings) {

        String server = strings[0];
        String username = strings[1];
        String password = strings[2];
        String cohortConfig = strings[3];
        String observationConfig = strings[4];

        String auth = username + ":" + password;
        String basicAuth = "Basic " + new String(Base64.encode(auth.getBytes(), Base64.NO_WRAP));

        try {
            URLConnection cohortUrlConnection = new URL(server + "/ws/rest/v1/cohort/").openConnection();
            cohortUrlConnection.setRequestProperty("Authorization", basicAuth);
            String cohortJson = readInputStream(cohortUrlConnection.getInputStream());
            if (cohortJson != null) {
                String cohortUuid = JsonPath.read(cohortJson, "$.results[0].uuid");
                Log.i("Win Log", "Trying to retrieve cohort member for: " + cohortUuid);
                URL memberUrl = new URL(server + "/ws/rest/v1/cohort/" + cohortUuid + "/member?v=full");
                URLConnection memberUrlConnection = memberUrl.openConnection();
                memberUrlConnection.setRequestProperty("Authorization", basicAuth);

                InputStream inputStream = new ByteArrayInputStream(cohortConfig.getBytes());
                JsonLuceneConfig cohortLuceneConfig = JsonLuceneUtil.load(inputStream);

                IndexService indexService = injector.getInstance(IndexService.class);
                indexService.updateIndex(cohortLuceneConfig, memberUrlConnection.getInputStream());
            }

            SearchService searchService = injector.getInstance(SearchService.class);
            List<Patient> patients = searchService.getObjects(Patient.class, "a*");

            InputStream inputStream = new ByteArrayInputStream(observationConfig.getBytes());
            JsonLuceneConfig observationLuceneConfig = JsonLuceneUtil.load(inputStream);

            for (Patient patient : patients) {
                Log.i("Win Log", "Patient uuid: " + patient.getUuid());

                String patientUuid = patient.getUuid();
                URL patientObs = new URL(server + "/ws/rest/v1/obs?patient=" + patientUuid);
                URLConnection patientObsConnection = patientObs.openConnection();
                patientObsConnection.setRequestProperty("Authorization", basicAuth);

                IndexService indexService = injector.getInstance(IndexService.class);
                indexService.updateIndex(observationLuceneConfig, patientObsConnection.getInputStream());
            }
        } catch (IOException e) {
            Log.e("Win Log", "Exception caught when loading patient and observation data.", e);
        }
        return "Success";
    }

    private String readInputStream(final InputStream inputStream) throws IOException {
        String line;
        BufferedReader reader = null;
        StringBuilder builder = new StringBuilder();
        try {
            reader = new BufferedReader(new InputStreamReader(inputStream));
            while ((line = reader.readLine()) != null)
                builder.append(line);
        } finally {
            if (reader != null)
                reader.close();
        }
        return builder.toString();
    }
}
