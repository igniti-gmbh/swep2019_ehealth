package uni.jena.swep.ehealth.ui.movementdata;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.room.Room;

import uni.jena.swep.ehealth.FirebaseInterface;
import uni.jena.swep.ehealth.R;
import uni.jena.swep.ehealth.data_visualisation.TotalStepHourly;
import uni.jena.swep.ehealth.data_visualisation.VisualDatabase;
import uni.jena.swep.ehealth.data_visualisation.XAxisFormatterDay;
import uni.jena.swep.ehealth.data_visualisation.XAxisFormatterDayHours;
import uni.jena.swep.ehealth.measure_movement.StepEntity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.gson.Gson;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MovementDataFragment extends Fragment {
    private MovementDataViewModel movementDataViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        movementDataViewModel = ViewModelProviders.of(this).get(MovementDataViewModel.class);
        View root = inflater.inflate(R.layout.fragment_movement_data, container, false);

        // init firebase interface
        FirebaseInterface firebaseInterface = new FirebaseInterface(getActivity().getApplicationContext());
        firebaseInterface.updateDailySteps();

        // get steps
        List<TotalStepHourly> steps = getStepsOfDay();

        // TODO check if sorting is necessary
        Collections.sort(steps, new StepComparatorTotalStepHourly());

        Log.v("datavisual", "Amount steps today: " + steps.size());

        for (TotalStepHourly step: steps) {
            Log.v("datavisual", "step: " + step.getTimestamp() + " have " + step.getNumber_steps());
        }

        // create bar chart of daily steps
        root = this.createBarChartMovementDaily(root, steps);

        return root;
    }

    // TODO make bar chart beautifuler
    private View createBarChartMovementDaily(View root, List<TotalStepHourly> steps) {
        // create bar chart
        BarChart chart = (BarChart) root.findViewById(R.id.steps_chart);

        // create data for chart
        List<BarEntry> entries = new ArrayList<BarEntry>();

        if (steps.size() != 0) {
            for (int i = 0; i < steps.size(); i++) {
                entries.add(new BarEntry(steps.get(i).getTimestamp().atZone(ZoneId.systemDefault()).toEpochSecond(), steps.get(i).getNumber_steps()));
            }
        }

        BarDataSet set = new BarDataSet(entries, "Schritte");
        BarData data = new BarData(set);
        data.setBarWidth(3600.f / 1.5f); // set custom bar width depending on timestamp(nanoseconds)
        chart.setData(data);
        chart.setFitBars(true); // make the x-axis fit exactly all bars
        chart.setMinOffset(50);
        chart.setTouchEnabled(true);
        chart.setNoDataText("No data available yet");
        chart.setVisibleXRangeMaximum(1000 * 60 * 60 * 2);
        // chart.zoom(2, 1, 0,0);  // TODO zoom in on start?
        // chart.getAxisRight().setEnabled(false); // TODO disable right axis?
        // chart.setDrawGridBackground(false);
        // chart.setDrawValueAboveBar(false);

        XAxis xaxis = chart.getXAxis();
        xaxis.setValueFormatter(new XAxisFormatterDayHours());
        xaxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        YAxis yaxis = chart.getAxisLeft();
        yaxis.setDrawZeroLine(false);
        yaxis.setDrawGridLines(false);

        chart.getDescription().setEnabled(false);

        chart.invalidate(); // refresh

        return root;
    }

    private View createLineChartMovement(View root, List<StepEntity> steps) {
        // create line chart
        LineChart chart = (LineChart) root.findViewById(R.id.steps_chart);

        // create data for chart
        List<Entry> entries = new ArrayList<Entry>();
        long reference_timestamp = 0;
        int last_step = 0;

        if (steps.size() > 0) {
            reference_timestamp = steps.get(0).getTime();

            for (int i = 0; i < steps.size(); i++) {
                entries.add(new Entry(steps.get(i).getTime() - reference_timestamp, steps.get(i).getAmount() - last_step));
                last_step = steps.get(i).getAmount();
            }
        }

        LineDataSet dataSet = new LineDataSet(entries, "Steps gone"); // add entries to dataset

        LineData lineData = new LineData(dataSet);
        // lineData.setValueFormatter(new XAxisFormatterDay(reference_timestamp));
        chart.setData(lineData);

        // configure line chart
        chart.setMinOffset(50);
        chart.setTouchEnabled(true);
        chart.setNoDataText("No data available yet");
        chart.setVisibleXRangeMaximum(1000 * 60 * 60 * 2);
        chart.getAxisRight().setEnabled(false);

        XAxis xaxis = chart.getXAxis();
        xaxis.setValueFormatter(new XAxisFormatterDay(reference_timestamp));
        xaxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        chart.getDescription().setEnabled(false);

        chart.invalidate(); // refresh chart

        return root;
    }

    public List<TotalStepHourly> getStepsOfDay() {
        // create Bundle for data fragment
        Gson gson = new Gson();

        VisualDatabase visual_db = Room.databaseBuilder(getActivity().getApplicationContext(), VisualDatabase.class, "visualDB").allowMainThreadQueries().build();

        // TODO rebuild calculation
        LocalDateTime actual = LocalDateTime.now();
        long begin_day = LocalDateTime.of(actual.getYear(), actual.getMonth(), actual.getDayOfMonth(), 0, 0, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        LocalDate date = LocalDate.now();
        LocalDateTime last_date = LocalDateTime.now().minusDays(1);

        List<TotalStepHourly> hourly_steps_all = visual_db.getVisualDAO().getAllTotalStepsHourly();
        List<TotalStepHourly> hourly_steps = new ArrayList<TotalStepHourly>();

        // filter out steps of last day
        for (TotalStepHourly step: hourly_steps_all) {
            if (step.getTimestamp().isBefore(actual) && step.getTimestamp().isAfter(last_date)) {
                hourly_steps.add(step);
            }
        }

        return hourly_steps;
    }

    public class StepComparatorTotalStepHourly implements Comparator<TotalStepHourly> {
        @Override
        public int compare(TotalStepHourly o1, TotalStepHourly o2) {
            return o1.getTimestamp().compareTo(o2.getTimestamp());
        }
    }

    public class StepComparatorStepEntity implements Comparator<StepEntity> {
        @Override
        public int compare(StepEntity o1, StepEntity o2) {
            return new Long(o1.getTime()).compareTo(new Long(o2.getTime()));
        }
    }
}
