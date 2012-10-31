package com.nribeka.search.activities;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.burkeware.search.api.module.SearchModule;
import com.burkeware.search.api.service.SearchService;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.nribeka.search.R;
import com.nribeka.search.module.AndroidModule;
import com.nribeka.search.sample.Observation;
import com.nribeka.search.sample.Patient;
import com.nribeka.search.util.Constants;
import com.nribeka.search.util.FileUtils;
import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.util.List;

public class ObservationChartActivity extends Activity {

    private Patient mPatient;

    private String mObservationFieldName;

    private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();

    private XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();

    private GraphicalView mChartView;

    private Injector injector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.observation_chart);

        Module searchModule = new SearchModule();
        Module androidModule = new AndroidModule();
        injector = Guice.createInjector(searchModule, androidModule);

        if (!FileUtils.storageReady()) {
            showCustomToast(getString(R.string.error, R.string.storage_error));
            finish();
        }

        String patientUuid = getIntent().getStringExtra(Constants.KEY_PATIENT_ID);
        mPatient = getPatient(patientUuid);

        mObservationFieldName = getIntent().getStringExtra(Constants.KEY_OBSERVATION_FIELD_ID);

        setTitle(getString(R.string.app_name) + " > " + getString(R.string.view_patient_detail));

        TextView textView = (TextView) findViewById(R.id.title_text);
        textView.setText(mObservationFieldName);

        XYSeriesRenderer r = new XYSeriesRenderer();
        r.setLineWidth(3.0f);
        r.setColor(getResources().getColor(R.color.chart_red));
        r.setPointStyle(PointStyle.CIRCLE);
        r.setFillPoints(true);

        mRenderer.addSeriesRenderer(r);
        mRenderer.setShowLegend(false);
        mRenderer.setLabelsTextSize(11.0f);
        mRenderer.setShowGrid(true);
        mRenderer.setLabelsColor(getResources().getColor(android.R.color.black));
    }

    private Patient getPatient(final String uuid) {
        SearchService searchService = injector.getInstance(SearchService.class);
        return searchService.getObject(Patient.class, "uuid:" + uuid);
    }

    private void getObservations(final String uuid, final String fieldName) {
        SearchService searchService = injector.getInstance(SearchService.class);
        List<Observation> observations = searchService.getObjects(Observation.class,
                "patient_uuid:" + uuid + " AND concept_name:" + fieldName);

        XYSeries series;
        if (mDataset.getSeriesCount() > 0) {
            series = mDataset.getSeriesAt(0);
            series.clear();
        } else {
            series = new XYSeries(fieldName);
            mDataset.addSeries(series);
        }

        for (Observation observation : observations) {
            int dataType = observation.getDataType();
            if (dataType == Constants.TYPE_INT)
                series.add(observation.getObservationDate().getTime(), observation.getValueInt());
            else if (dataType == Constants.TYPE_FLOAT) {
                series.add(observation.getObservationDate().getTime(), observation.getValueNumeric());
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mPatient != null && mObservationFieldName != null) {
            getObservations(mPatient.getUuid(), mObservationFieldName);
        }

        if (mChartView == null) {
            LinearLayout layout = (LinearLayout) findViewById(R.id.chart);
            mChartView = ChartFactory.getTimeChartView(this, mDataset, mRenderer, null);
            layout.addView(mChartView, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        } else {
            mChartView.repaint();
        }
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
