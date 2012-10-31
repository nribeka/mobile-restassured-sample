package com.nribeka.search;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import com.burkeware.search.api.JsonLuceneConfig;
import com.burkeware.search.api.module.SearchModule;
import com.burkeware.search.api.provider.SearchProvider;
import com.burkeware.search.api.service.ConfigService;
import com.burkeware.search.api.service.SearchService;
import com.burkeware.search.api.util.JsonLuceneUtil;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import com.nribeka.search.adapter.PatientAdapter;
import com.nribeka.search.module.AndroidModule;
import com.nribeka.search.sample.Patient;
import com.nribeka.search.sample.PatientAlgorithm;
import com.nribeka.search.task.PatientLoaderTask;
import junit.framework.Assert;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;

public class SearchPatientActivity extends Activity {

    private Injector injector;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        final EditText editText = (EditText) findViewById(R.id.searchPatientEdit);
        final ListView listView = (ListView) findViewById(R.id.searchPatientValue);

        Module searchModule = new SearchModule();
        Module androidModule = new AndroidModule();
        injector = Guice.createInjector(searchModule, androidModule);

        try {
            Analyzer analyzer = injector.getInstance(Analyzer.class);
            Assert.assertEquals(StandardAnalyzer.class, analyzer.getClass());
            Log.e("Win Log", "Injected Analyzer class: " + analyzer.getClass().getName());

            Key<SearchProvider<Directory>> directoryKey = Key.get(new TypeLiteral<SearchProvider<Directory>>() {
            });
            SearchProvider<Directory> directoryProvider = injector.getInstance(directoryKey);
            Directory directory = directoryProvider.get();
            Assert.assertEquals(NIOFSDirectory.class, directory.getClass());
            Log.e("Win Log", "Injected Directory class: " + directory.getClass().getName());

            Key<SearchProvider<IndexReader>> indexReaderKey = Key.get(new TypeLiteral<SearchProvider<IndexReader>>() {
            });
            SearchProvider<IndexReader> indexReaderProvider = injector.getInstance(indexReaderKey);
            IndexReader indexReader = indexReaderProvider.get();
            Assert.assertTrue(IndexReader.class.isAssignableFrom(indexReader.getClass()));
            Log.e("Win Log", "Injected IndexReader class: " + indexReader.getClass().getName());

            Key<SearchProvider<IndexSearcher>> indexSearcherKey = Key.get(new TypeLiteral<SearchProvider<IndexSearcher>>() {
            });
            SearchProvider<IndexSearcher> indexSearcherProvider = injector.getInstance(indexSearcherKey);
            IndexSearcher indexSearcher = indexSearcherProvider.get();
            Assert.assertTrue(IndexSearcher.class.isAssignableFrom(indexSearcher.getClass()));
            Log.e("Win Log", "Injected IndexSearcher class: " + indexSearcher.getClass().getName());

            Key<SearchProvider<IndexWriter>> indexWriterKey = Key.get(new TypeLiteral<SearchProvider<IndexWriter>>() {
            });
            SearchProvider<IndexWriter> indexWriterProvider = injector.getInstance(indexWriterKey);
            IndexWriter indexWriter = indexWriterProvider.get();
            Assert.assertTrue(IndexWriter.class.isAssignableFrom(indexWriter.getClass()));
            Log.e("Win Log", "Injected IndexWriter class: " + indexWriter.getClass().getName());
        } catch (IOException e) {
            Log.e("Win Log", "Exception caught when trying to debug the Injection process!");
        }

        ConfigService configService = injector.getInstance(ConfigService.class);
        configService.registerAlgorithm(Patient.class, new PatientAlgorithm());

        final List<Patient> patientsArray = new ArrayList<Patient>();
        final ArrayAdapter<Patient> patientsAdapter = new PatientAdapter(this, R.layout.patient_list_item, patientsArray);
        listView.setAdapter(patientsAdapter);

        try {

            findViewById(R.id.searchPatientButton).setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    CharSequence queryString = editText.getText();
                    SearchService searchService = injector.getInstance(SearchService.class);
                    patientsArray.addAll(searchService.getObjects(Patient.class, String.valueOf(queryString)));
                    patientsAdapter.notifyDataSetChanged();
                }
            });

            findViewById(R.id.loadPatientButton).setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    try {
                        URL url = new URL("http://149.166.216.226:8080/openmrs/ws/rest/v1/cohort/19aaa4f4-ce95-4b91-a305-250818101f98/member?v=full");
                        URLConnection connection = url.openConnection();
                        String auth = "admin:test";
                        String basicAuth = "Basic " + new String(Base64.encode(auth.getBytes(), Base64.NO_WRAP));
                        connection.setRequestProperty("Authorization", basicAuth);

                        InputStream inputStream = getResources().openRawResource(R.raw.cohorttemplate);
                        JsonLuceneConfig config = JsonLuceneUtil.load(inputStream);

                        PatientLoaderTask patientLoaderTask = new PatientLoaderTask(injector);
                        patientLoaderTask.execute("Loading Patient");
                    } catch (IOException e) {
                        Log.e("Win Log", "Exception caught when trying to read HTTP connection.", e);
                    }
                }
            });

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
