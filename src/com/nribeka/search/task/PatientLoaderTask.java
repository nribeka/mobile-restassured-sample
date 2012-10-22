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

import java.io.InputStream;

import android.os.AsyncTask;
import com.burkeware.search.api.JsonLuceneConfig;
import com.burkeware.search.api.service.IndexService;
import com.google.inject.Injector;

public class PatientLoaderTask extends AsyncTask<String, String, String> {

    private final Injector injector;

    private final JsonLuceneConfig config;

    private final InputStream inputStream;

    public PatientLoaderTask(final Injector injector, final JsonLuceneConfig config, final InputStream inputStream) {
        this.config = config;
        this.injector = injector;
        this.inputStream = inputStream;
    }

    @Override
    protected String doInBackground(String... strings) {
        String result = "Success";
        IndexService indexService = injector.getInstance(IndexService.class);
        indexService.updateIndex(config, inputStream);
        return result;
    }
}
