package uni.jena.swep.ehealth.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.room.Room;

import java.util.List;

import uni.jena.swep.ehealth.R;
import uni.jena.swep.ehealth.data_visualisation.RoomData;
import uni.jena.swep.ehealth.data_visualisation.StepGoal;
import uni.jena.swep.ehealth.data_visualisation.TotalStepDaily;
import uni.jena.swep.ehealth.data_visualisation.VisualDatabase;

public class HomeFragment extends Fragment {
    private VisualDatabase db;

    private HomeViewModel homeViewModel;
    private ProgressBar progressBar;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        // init database
        this.db = Room.databaseBuilder(getActivity().getApplicationContext(), VisualDatabase.class, "visualDB").allowMainThreadQueries().fallbackToDestructiveMigration().build();

        // get data and init view model values
        List<TotalStepDaily> total_steps = this.db.getVisualDAO().getActualDailySteps();
        List<StepGoal> step_goals = this.db.getVisualDAO().getAllStepGoals();
        List<RoomData> room_data = this.db.getVisualDAO().getAllRoomData();

        // update data in view model
        MutableLiveData<Integer> steps_taken_mut = homeViewModel.getSteps_taken();
        MutableLiveData<Integer> step_goal_mut = homeViewModel.getStep_goal();
        MutableLiveData<RoomData> room_data_mut = homeViewModel.getRoom_data();

        if (total_steps.size() > 0) {
            steps_taken_mut.setValue(total_steps.get(total_steps.size()-1).getNumber_steps());
        }
        else {
            steps_taken_mut.setValue(0);
        }

        if (step_goals.size() > 0) {
            step_goal_mut.setValue(step_goals.get(0).getNumber_steps());
        }
        else {
            step_goal_mut.setValue(0);
        }

        if (room_data.size() > 0) {
            room_data_mut.setValue(room_data.get(0));
        }
        else {
            RoomData rd = new RoomData();
            rd.setHumidity(0);
            rd.setTemp(0);
            rd.setPressure(0);
            rd.setGas(0);
            room_data_mut.setValue(rd);
        }

        homeViewModel.setSteps_taken(steps_taken_mut);
        homeViewModel.setStep_goal(step_goal_mut);
        homeViewModel.updateProgress();

        // set steps taken
        final TextView steps_taken_home = root.findViewById(R.id.steps_taken_home);
        homeViewModel.getSteps_taken().observe(this, new Observer<Integer>() {
           @Override
           public void onChanged(@Nullable Integer value) {
               steps_taken_home.setText("Schritte gegangen: " + value);
           }
        });

        // set step goal
        final TextView step_goal_home = root.findViewById(R.id.step_goal_home);
        homeViewModel.getStep_goal().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                step_goal_home.setText("Tagesziel: " + integer);
            }
        });

        // set room data
        final TextView room_temp = root.findViewById(R.id.room_temp);
        final TextView room_air_humidity = root.findViewById(R.id.room_air_humidity);
        final TextView room_co2 = root.findViewById(R.id.room_co2);
        final TextView room_pressure = root.findViewById(R.id.room_pressure);

        homeViewModel.getRoom_data().observe(this,new Observer<RoomData>() {
            @Override
            public void onChanged(RoomData rd) {
                room_temp.setText("Raumtemperatur: " + rd.getTemp() + " Celsius");
                room_air_humidity.setText("Luftfeuchtigkeit: " + rd.getHumidity() + "%");
                room_co2.setText("CO2: " + rd.getGas());
                room_pressure.setText("Druck: " + rd.getPressure());
            }
        });

        // set step progress
        progressBar = (ProgressBar) root.findViewById(R.id.step_progress);
        homeViewModel.getStep_progress().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                progressBar.setProgress(integer);
                Log.v("display", "set progressbar to " + integer);
            }
        });

        return root;
    }
}